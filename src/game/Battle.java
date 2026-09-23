package game;

import java.util.Scanner;

// 战斗逻辑管理器
public class Battle {
    private Player player;
    private Enemy enemy;
    private Scanner scanner;
    private int round;

    public Battle(Player player, Enemy enemy, Scanner scanner) {
        this.player = player;
        this.enemy = enemy;
        this.scanner = scanner;
        this.round = 1;
    }

    // 返回战斗结果：true=胜利，false=失败
    public boolean start() {
        printLine();
        System.out.println("  ⚔️  遭遇战斗！");
        System.out.println("  敌人出现：" + enemy.getStatus());
        printLine();

        // 速度决定先手
        boolean playerFirst = player.getSpeed() >= enemy.getSpeed();
        System.out.println(playerFirst ? "  你的速度更快，先手出击！" : "  敌人速度更快，敌人先手！");

        while (player.isAlive() && enemy.isAlive()) {
            System.out.println("\n  ═══ 第 " + round + " 回合 ═══");
            printBattleStatus();

            if (playerFirst) {
                if (!playerTurn()) return false; // 逃跑
                if (!enemy.isAlive()) break;
                enemyTurn();
            } else {
                enemyTurn();
                if (!player.isAlive()) break;
                if (!playerTurn()) return false;
            }

            player.onTurnEnd();
            round++;
        }

        if (player.isAlive()) {
            printVictory();
            return true;
        } else {
            printDefeat();
            return false;
        }
    }

    // 玩家回合，返回false表示逃跑
    private boolean playerTurn() {
        while (true) {
            System.out.println("\n  你的行动：");
            System.out.println("  [1] 普通攻击");
            System.out.println("  [2] 使用技能");
            System.out.println("  [3] 使用药水（剩余:" + player.getPotions() + "瓶）");
            System.out.println("  [4] 逃跑（50%成功率）");
            System.out.print("  请选择 > ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1" -> {
                    int dmg = enemy.takeDamage(player.getAttack());
                    System.out.println("  你发动普通攻击，对 " + enemy.getName() + " 造成 " + dmg + " 点伤害！");
                    return true;
                }
                case "2" -> {
                    if (showSkillMenu()) return true;
                }
                case "3" -> {
                    if (player.getPotions() <= 0) {
                        System.out.println("  药水已用完！");
                    } else {
                        int before = player.getHp();
                        player.usePotion();
                        System.out.println("  使用了药水，回复 " + (player.getHp() - before) + " 点HP！");
                        return true;
                    }
                }
                case "4" -> {
                    if (Math.random() < 0.5) {
                        System.out.println("  成功逃跑！");
                        return false;
                    } else {
                        System.out.println("  逃跑失败！");
                        enemyTurn();
                        return true; // 逃跑失败继续战斗
                    }
                }
                default -> System.out.println("  请输入1-4之间的数字。");
            }
        }
    }

    // 技能菜单，返回是否成功使用
    private boolean showSkillMenu() {
        var skills = player.getSkills();
        System.out.println("  ── 技能列表 ──");
        for (int i = 0; i < skills.size(); i++) {
            Skill s = skills.get(i);
            String status = !s.isReady() ? " [冷却中]" : (player.getMp() < s.getManaCost() ? " [MP不足]" : "");
            System.out.println("  [" + (i + 1) + "] " + s + status);
        }
        System.out.println("  [0] 返回");
        System.out.print("  选择技能 > ");

        String input = scanner.nextLine().trim();
        if (input.equals("0")) return false;

        try {
            int idx = Integer.parseInt(input) - 1;
            if (idx < 0 || idx >= skills.size()) {
                System.out.println("  无效选择。");
                return false;
            }
            Skill skill = skills.get(idx);
            if (!skill.isReady()) {
                System.out.println("  技能冷却中，还需 " + skill.getCurrentCooldown() + " 回合！");
                return false;
            }
            if (player.getMp() < skill.getManaCost()) {
                System.out.println("  MP不足！");
                return false;
            }

            int result = player.useSkill(idx, enemy);
            switch (skill.getType()) {
                case DAMAGE -> System.out.println("  你使用【" + skill.getName() + "】，造成 " + result + " 点伤害！");
                case HEAL -> System.out.println("  你使用【" + skill.getName() + "】，恢复了 " + result + " 点HP！");
                case BUFF_ATTACK -> System.out.println("  你使用【" + skill.getName() + "】，攻击力提升了 " + result + " 点！");
                case BUFF_DEFENSE -> System.out.println("  你使用【" + skill.getName() + "】，防御力提升了 " + result + " 点！");
            }
            return true;
        } catch (NumberFormatException e) {
            System.out.println("  请输入有效数字。");
            return false;
        }
    }

    private void enemyTurn() {
        String action = enemy.act(player);
        System.out.println("  " + action);
    }

    private void printBattleStatus() {
        System.out.println("  " + player.getName() + "  HP: " + player.hpBar() + "  MP:" + player.getMp() + "/" + player.getMaxMp());
        System.out.println("  " + enemy.getStatus());
    }

    private void printVictory() {
        printLine();
        System.out.println("  🎉 战斗胜利！");
        System.out.println("  获得经验值: +" + enemy.getExpReward());
        System.out.println("  获得金币: +" + enemy.getGoldReward());
        boolean levelUp = player.gainExp(enemy.getExpReward());
        if (levelUp) {
            System.out.println("  ✨ 升级！当前等级: Lv." + player.getLevel());
        }
        printLine();
    }

    private void printDefeat() {
        printLine();
        System.out.println("  💀 战斗失败...");
        System.out.println("  你被 " + enemy.getName() + " 击败了。");
        printLine();
    }

    private void printLine() {
        System.out.println("  " + "─".repeat(50));
    }
}
