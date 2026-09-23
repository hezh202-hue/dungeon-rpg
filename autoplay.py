"""
自动玩游戏的机器人 v5
关键修复：每次输入后清空当前上下文，只看「上次输入后的新内容」
"""
import subprocess, sys, time, re, threading, queue, os

# 脚本所在的文件夹，项目挪到哪都能找到游戏
GAME_DIR = os.path.dirname(os.path.abspath(__file__))

ENCODING = 'gbk'

def get_hp_ratio(text):
    # 用数字解析：匹配 HP:数字/数字（GBK解码后数字是ASCII，可靠）
    # 只取玩家自己的HP（不取敌人的）
    # 玩家状态行格式: HP:[...] 24/75
    matches = re.findall(r'HP:.*?(\d+)/(\d+)', text)
    if matches:
        cur, mx = int(matches[-1][0]), int(matches[-1][1])
        if mx > 0:
            return cur / mx
    return 1.0

def get_mp(text):
    # 匹配 MP:40/100
    matches = re.findall(r'MP:(\d+)/(\d+)', text)
    if matches:
        return int(matches[-1][0])
    return 0

def get_enemy_hp_ratio(text):
    """解析敌人血量（取倒数第二个HP数值对）"""
    matches = re.findall(r'HP:.*?(\d+)/(\d+)', text)
    if len(matches) >= 2:
        cur, mx = int(matches[-1][0]), int(matches[-1][1])
        return cur / mx if mx > 0 else 0
    return 1.0

def decide(recent):
    """只看最近一段输出（上次操作之后）决定行动"""
    if '主菜单' in recent:
        return '1', '开始游戏'
    if '输入你的角色名称' in recent:
        return '机器人甲', '输入名字'
    if '选择职业' in recent:
        return '2', '选择法师'
    if '前进探索' in recent and '原地休息' in recent:
        ratio = get_hp_ratio(recent)
        # 血量低于60%先休息（法师血少，要保守）
        if ratio < 0.60:
            return '2', f'休息（血量{ratio:.0%}）'
        return '1', f'前进（血量{ratio:.0%}）'
    if '你的行动' in recent and '普通攻击' in recent:
        ratio = get_hp_ratio(recent)
        mp = get_mp(recent)
        enemy_ratio = get_enemy_hp_ratio(recent)
        # 敌人快死了直接打，别浪费药
        if enemy_ratio < 0.15:
            if mp >= 20:
                return '2', f'敌人快死了，用技能（MP={mp}）'
            return '1', f'敌人快死了，普通攻击'
        # 血量低于30%喝药
        if ratio < 0.30:
            if '剩余:0瓶' in recent:
                return '1', f'没药了，硬打（HP={ratio:.0%}）'
            return '3', f'喝药！血量{ratio:.0%}'
        # MP够20就用技能
        if mp >= 20:
            return '2', f'使用技能（MP={mp}）'
        return '1', f'普通攻击（HP={ratio:.0%} MP={mp}）'
    if '技能列表' in recent:
        for i in range(1, 4):
            line_match = re.findall(rf'\[{i}\] (.+)', recent)
            if line_match:
                line = line_match[0]
                if '冷却中' not in line and 'MP不足' not in line:
                    return str(i), f'选技能{i}'
        return '0', '技能不可用，返回'
    return '1', '默认选1'


def reader_thread(proc, q):
    while True:
        ch = proc.stdout.read(1)
        if not ch:
            q.put(None)
            break
        q.put(ch)


def drain_queue(q, wait=0.3):
    """等待一段时间后排空队列，返回额外读到的字节"""
    time.sleep(wait)
    extra = b''
    while True:
        try:
            ch = q.get_nowait()
            if ch is None:
                q.put(None)
                break
            extra += ch
        except:
            break
    return extra


def main():
    print('=' * 55)
    print('  机器人 v5 开始自动游戏（法师策略）')
    print('=' * 55)

    proc = subprocess.Popen(
        ['java', '-cp', 'out', 'game.Main'],
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        bufsize=0,
        cwd=GAME_DIR
    )

    q = queue.Queue()
    t = threading.Thread(target=reader_thread, args=(proc, q), daemon=True)
    t.start()

    # current_chunk：上次操作后到现在的输出（每次操作后清空）
    current_chunk = b''
    full_log = b''  # 全量日志，只用于最终判断结局

    try:
        while True:
            try:
                ch = q.get(timeout=15)
            except:
                break

            if ch is None:
                break

            current_chunk += ch
            full_log += ch

            try:
                sys.stdout.buffer.write(ch)
                sys.stdout.flush()
            except:
                pass

            # 检测到提示符
            decoded = current_chunk.decode(ENCODING, errors='ignore')
            if decoded.rstrip().endswith('>'):
                # 多等一会让游戏把剩余文字全输出
                extra = drain_queue(q, 0.35)
                current_chunk += extra
                full_log += extra
                try:
                    sys.stdout.buffer.write(extra)
                    sys.stdout.flush()
                except:
                    pass

                context = current_chunk.decode(ENCODING, errors='ignore')
                action, reason = decide(context)

                log_line = f'\n  [机器人] {reason} → [{action}]\n'
                sys.stdout.buffer.write(log_line.encode('utf-8', errors='replace'))
                sys.stdout.flush()

                proc.stdin.write((action + '\n').encode(ENCODING))
                proc.stdin.flush()

                # 关键：清空当前块，下次只看新输出
                current_chunk = b''

    except Exception as e:
        print(f'\n  [错误] {e}', flush=True)
    finally:
        proc.wait()

    result = full_log.decode(ENCODING, errors='ignore')
    print('\n' + '=' * 55)
    if '通关' in result or '拯救了世界' in result:
        print('  🏆 通关成功！！！')
    elif '战斗失败' in result or '被击败' in result:
        print('  💀 最终结果：阵亡')
    else:
        print('  游戏结束')
    print('=' * 55)


if __name__ == '__main__':
    main()
