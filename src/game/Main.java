package game;

import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        printTitle();

        while (true) {
            System.out.println("\n  主菜单：");
            System.out.println("  [1] 开始新游戏");
            System.out.println("  [2] 游戏说明");
            System.out.println("  [0] 退出");
            System.out.print("  请选择 > ");

            String input = scanner.nextLine().trim();
            switch (input) {
                case "1" -> startGame();
                case "2" -> showHelp();
                case "0" -> {
                    System.out.println("\n  感谢游玩！再见！👋");
                    return;
                }
                default -> System.out.println("  请输入有效选项。");
            }
        }
    }

    private static void startGame() {
        // 输入角色名
        System.out.print("\n  输入你的角色名称 > ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) name = "勇者";

        // 选择职业
        System.out.println("\n  选择职业：");
        System.out.println("  [1] ⚔️  战士  - HP:120 MP:40  高防御，技能有增益");
        System.out.println("  [2] 🔮 法师  - HP:75  MP:100 高爆发，有治疗技能");
        System.out.println("  [3] 🏹 弓手  - HP:90  MP:60  高速度，多段攻击");

        Player.JobClass job = null;
        while (job == null) {
            System.out.print("  请选择 > ");
            switch (scanner.nextLine().trim()) {
                case "1" -> job = Player.JobClass.WARRIOR;
                case "2" -> job = Player.JobClass.MAGE;
                case "3" -> job = Player.JobClass.ARCHER;
                default -> System.out.println("  请输入1、2或3。");
            }
        }

        Player player = new Player(name, job);
        System.out.println("\n  " + player.getJobName() + "「" + name + "」，踏上冒险之旅！");

        // 进入地下城
        Dungeon dungeon = new Dungeon(player, scanner);
        boolean cleared = dungeon.explore();

        if (cleared) {
            System.out.println("\n  恭喜通关！金币总计: " + dungeon.getGold());
        } else {
            System.out.println("\n  冒险结束。下次再挑战吧！");
        }
    }

    private static void showHelp() {
        System.out.println("\n  ─────────── 游戏说明 ───────────");
        System.out.println("  目标: 探索10层地下城，击败最终Boss远古火龙");
        System.out.println();
        System.out.println("  战斗行动:");
        System.out.println("  · 普通攻击 - 造成 攻击力-防御力 的伤害");
        System.out.println("  · 技能     - 消耗MP，产生强力效果，有冷却");
        System.out.println("  · 药水     - 恢复最大HP的1/3，初始3瓶");
        System.out.println("  · 逃跑     - 50%成功率，失败则敌人追击");
        System.out.println();
        System.out.println("  职业特点:");
        System.out.println("  · 战士: 技能「重斩」「战吼」「铁壁」");
        System.out.println("  · 法师: 技能「火球术」「治愈术」「奥术爆发」");
        System.out.println("  · 弓手: 技能「连射」「鹰眼」「穿心箭」");
        System.out.println();
        System.out.println("  地下城事件:");
        System.out.println("  · 70% 遭遇敌人战斗");
        System.out.println("  · 20% 发现宝箱（金币/药剂）");
        System.out.println("  · 10% 触发陷阱（真实伤害）");
        System.out.println("  ─────────────────────────────────");
    }

    private static void printTitle() {
        System.out.println();
        System.out.println("  ██████╗ ██╗   ██╗███╗   ██╗ ██████╗ ███████╗ ██████╗ ███╗   ██╗");
        System.out.println("  ██╔══██╗██║   ██║████╗  ██║██╔════╝ ██╔════╝██╔═══██╗████╗  ██║");
        System.out.println("  ██║  ██║██║   ██║██╔██╗ ██║██║  ███╗█████╗  ██║   ██║██╔██╗ ██║");
        System.out.println("  ██║  ██║██║   ██║██║╚██╗██║██║   ██║██╔══╝  ██║   ██║██║╚██╗██║");
        System.out.println("  ██████╔╝╚██████╔╝██║ ╚████║╚██████╔╝███████╗╚██████╔╝██║ ╚████║");
        System.out.println("  ╚═════╝  ╚═════╝ ╚═╝  ╚═══╝ ╚═════╝ ╚══════╝ ╚═════╝ ╚═╝  ╚═══╝");
        System.out.println("                      地下城回合制RPG  v1.0");
        System.out.println();
    }
}
