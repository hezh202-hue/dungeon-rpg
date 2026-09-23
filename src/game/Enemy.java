package game;

import java.util.Random;

public class Enemy extends Entity {
    public enum EnemyType { SLIME, GOBLIN, ORC, SKELETON, DRAGON_WHELP, BOSS_DRAGON }

    private EnemyType type;
    private int expReward;
    private int goldReward;
    private Random rand = new Random();

    // 特殊技能冷却
    private int specialCooldown = 0;

    public Enemy(EnemyType type, int levelScale) {
        super(enemyName(type), baseHp(type, levelScale), baseAtk(type, levelScale),
              baseDef(type, levelScale), baseSpd(type));
        this.type = type;
        this.expReward = expReward(type, levelScale);
        this.goldReward = goldReward(type, levelScale);
    }

    private static String enemyName(EnemyType t) {
        return switch (t) {
            case SLIME -> "史莱姆";
            case GOBLIN -> "哥布林";
            case ORC -> "兽人战士";
            case SKELETON -> "骷髅弓手";
            case DRAGON_WHELP -> "幼龙";
            case BOSS_DRAGON -> "★ 远古火龙 ★";
        };
    }
    private static int baseHp(EnemyType t, int s) {
        return switch (t) {
            case SLIME -> 30 * s; case GOBLIN -> 45 * s; case ORC -> 80 * s;
            case SKELETON -> 55 * s; case DRAGON_WHELP -> 100 * s; case BOSS_DRAGON -> 300 * s;
        };
    }
    private static int baseAtk(EnemyType t, int s) {
        return switch (t) {
            case SLIME -> 5 * s; case GOBLIN -> 8 * s; case ORC -> 14 * s;
            case SKELETON -> 11 * s; case DRAGON_WHELP -> 18 * s; case BOSS_DRAGON -> 35 * s;
        };
    }
    private static int baseDef(EnemyType t, int s) {
        return switch (t) {
            case SLIME -> 0; case GOBLIN -> 2; case ORC -> 6;
            case SKELETON -> 3; case DRAGON_WHELP -> 8; case BOSS_DRAGON -> 15;
        };
    }
    private static int baseSpd(EnemyType t) {
        return switch (t) {
            case SLIME -> 3; case GOBLIN -> 10; case ORC -> 6;
            case SKELETON -> 9; case DRAGON_WHELP -> 12; case BOSS_DRAGON -> 10;
        };
    }
    private static int expReward(EnemyType t, int s) {
        return switch (t) {
            case SLIME -> 15 * s; case GOBLIN -> 25 * s; case ORC -> 50 * s;
            case SKELETON -> 35 * s; case DRAGON_WHELP -> 80 * s; case BOSS_DRAGON -> 300 * s;
        };
    }
    private static int goldReward(EnemyType t, int s) {
        return switch (t) {
            case SLIME -> 5 * s; case GOBLIN -> 10 * s; case ORC -> 20 * s;
            case SKELETON -> 15 * s; case DRAGON_WHELP -> 40 * s; case BOSS_DRAGON -> 200 * s;
        };
    }

    // AI行动，返回行动描述
    public String act(Player player) {
        // Boss有特殊技能
        if (type == EnemyType.BOSS_DRAGON && specialCooldown == 0 && rand.nextInt(3) == 0) {
            specialCooldown = 3;
            int dmg = player.takeDamage(attack * 2);
            return getName() + " 释放【龙息】！造成 " + dmg + " 点伤害！";
        }
        if (type == EnemyType.ORC && specialCooldown == 0 && rand.nextInt(4) == 0) {
            specialCooldown = 3;
            int dmg = player.takeDamage(attack + 10);
            return getName() + " 使用【猛击】！造成 " + dmg + " 点伤害！";
        }
        if (specialCooldown > 0) specialCooldown--;

        // 普通攻击，加一点随机波动
        int rawDmg = attack + rand.nextInt(attack / 3 + 1);
        int dmg = player.takeDamage(rawDmg);
        return getName() + " 攻击了你，造成 " + dmg + " 点伤害！";
    }

    public String getTypeEmoji() {
        return switch (type) {
            case SLIME -> "🟢"; case GOBLIN -> "👺"; case ORC -> "💪";
            case SKELETON -> "💀"; case DRAGON_WHELP -> "🐲"; case BOSS_DRAGON -> "🔥";
        };
    }

    @Override
    public String getStatus() {
        return String.format("%s %s  HP: %s", getTypeEmoji(), name, hpBar());
    }

    public int getExpReward() { return expReward; }
    public int getGoldReward() { return goldReward; }
    public EnemyType getType() { return type; }
}
