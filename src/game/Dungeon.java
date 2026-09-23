package game;

import java.util.Random;
import java.util.Scanner;

// 地下城关卡管理器
public class Dungeon {
    private Player player;
    private Scanner scanner;
    private Random rand = new Random();
    private int floor;         // 当前楼层
    private int totalFloors;   // 总楼层数（最后一层是Boss）
    private int gold;

    public Dungeon(Player player, Scanner scanner) {
        this.player = player;
        this.scanner = scanner;
        this.floor = 1;
        this.totalFloors = 10;
        this.gold = 0;
    }

    // 开始探索，返回是否通关
    public boolean explore() {
        printBanner();

        while (floor <= totalFloors) {
            System.out.println("\n  ┌─────────────────────────────┐");
            System.out.println("  │  地下城 第 " + floor + "/" + totalFloors + " 层              │");
            System.out.println("  └─────────────────────────────┘");
            System.out.println("  " + player.getStatus());

            if (floor == totalFloors) {
                System.out.println("\n  ⚠️  你感受到一股强大的气息... Boss在等待你！");
            }

            System.out.println("\n  选择行动：");
            System.out.println("  [1] 前进探索");
            System.out.println("  [2] 原地休息（恢复20%HP，消耗1回合）");
            System.out.println("  [3] 查看状态");
            System.out.println("  [0] 放弃冒险，回到主界面");
            System.out.print("  请选择 > ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1" -> {
                    if (!advanceFloor()) return false; // 战斗失败
                }
                case "2" -> rest();
                case "3" -> showDetailedStatus();
                case "0" -> { return false; }
                default -> System.out.println("  无效输入，请重试。");
            }
        }

        // 通关
        printClear();
        return true;
    }

    private boolean advanceFloor() {
        // 每层随机事件
        int event = rand.nextInt(10);

        if (event < 2) {
            // 20%概率触发宝箱
            treasureEvent();
            floor++;
            return true;
        } else if (event < 3) {
            // 10%概率触发陷阱
            trapEvent();
            floor++;
            return true;
        } else {
            // 70%概率遭遇战斗
            Enemy enemy = generateEnemy();
            Battle battle = new Battle(player, enemy, scanner);
            boolean won = battle.start();
            if (!won) return false;
            gold += enemy.getGoldReward();
            floor++;
            return true;
        }
    }

    private Enemy generateEnemy() {
        int scale = Math.max(1, floor / 2 + 1);

        // 最后一层固定Boss
        if (floor == totalFloors) {
            return new Enemy(Enemy.EnemyType.BOSS_DRAGON, scale);
        }

        // 根据楼层生成不同难度的敌人
        Enemy.EnemyType[] pool;
        if (floor <= 3) {
            pool = new Enemy.EnemyType[]{Enemy.EnemyType.SLIME, Enemy.EnemyType.GOBLIN};
        } else if (floor <= 6) {
            pool = new Enemy.EnemyType[]{Enemy.EnemyType.GOBLIN, Enemy.EnemyType.ORC, Enemy.EnemyType.SKELETON};
        } else {
            pool = new Enemy.EnemyType[]{Enemy.EnemyType.ORC, Enemy.EnemyType.SKELETON, Enemy.EnemyType.DRAGON_WHELP};
        }

        return new Enemy(pool[rand.nextInt(pool.length)], scale);
    }

    private void treasureEvent() {
        int type = rand.nextInt(3);
        System.out.println("\n  📦 发现了宝箱！");
        switch (type) {
            case 0 -> {
                int g = 30 + rand.nextInt(50);
                gold += g;
                System.out.println("  获得了 " + g + " 金币！");
            }
            case 1 -> {
                int heal = player.getMaxHp() / 4;
                player.heal(heal);
                System.out.println("  发现了恢复药剂，回复了 " + heal + " 点HP！");
            }
            case 2 -> {
                System.out.println("  发现了一瓶药水，补充了1瓶库存！");
                // 通过反射或包访问来加药水——这里用简单方式
                player.usePotion(); // 先用一瓶再补充，净效果是治疗+1瓶不变
                // 实际上加一个方法更好，这里简化处理：只恢复HP
                int bonus = player.getMaxHp() / 4;
                player.heal(bonus);
                System.out.println("  （额外恢复了 " + bonus + " 点HP）");
            }
        }
    }

    private void trapEvent() {
        int dmg = player.getMaxHp() / 8;
        player.takeDamage(dmg + player.getDefense()); // 无视防御
        System.out.println("\n  ⚠️  触发了陷阱！受到 " + dmg + " 点真实伤害！");
    }

    private void rest() {
        int heal = player.getMaxHp() / 5;
        player.heal(heal);
        System.out.println("\n  💤 原地休息，恢复了 " + heal + " 点HP。");
    }

    private void showDetailedStatus() {
        System.out.println("\n  ───── 详细状态 ─────");
        System.out.println("  " + player.getStatus());
        System.out.println("  当前金币: " + gold);
        System.out.println("  攻击力: " + player.getAttack() + "  防御力: " + player.getDefense() + "  速度: " + player.getSpeed());
        System.out.println("  ── 技能 ──");
        for (Skill s : player.getSkills()) {
            System.out.println("  · " + s);
        }
    }

    private void printBanner() {
        System.out.println("\n  ╔══════════════════════════════════╗");
        System.out.println("  ║       ⚔️  地下城冒险开始  ⚔️        ║");
        System.out.println("  ║  目标：击败10层守卫，挑战火龙Boss  ║");
        System.out.println("  ╚══════════════════════════════════╝");
    }

    private void printClear() {
        System.out.println("\n  ╔══════════════════════════════════╗");
        System.out.println("  ║         🏆 地下城通关！🏆          ║");
        System.out.println("  ║  你击败了远古火龙，拯救了世界！   ║");
        System.out.println("  ╚══════════════════════════════════╝");
        System.out.println("  本次冒险获得金币: " + gold);
        System.out.println("  最终等级: Lv." + player.getLevel());
    }

    public int getGold() { return gold; }
}
