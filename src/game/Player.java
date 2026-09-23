package game;

import java.util.ArrayList;
import java.util.List;

public class Player extends Entity {
    public enum JobClass { WARRIOR, MAGE, ARCHER }

    private JobClass jobClass;
    private int mp;
    private int maxMp;
    private int exp;
    private int level;
    private int expToNext;
    private List<Skill> skills;
    private List<String> inventory;  // 道具背包（药水等）
    private int potions;             // 血量药水数量

    // 临时增益
    private int attackBuff;
    private int defenseBuff;
    private int buffDuration;

    public Player(String name, JobClass job) {
        super(name, baseHp(job), baseAtk(job), baseDef(job), baseSpd(job));
        this.jobClass = job;
        this.maxMp = baseMp(job);
        this.mp = maxMp;
        this.level = 1;
        this.exp = 0;
        this.expToNext = 100;
        this.potions = 3;
        this.skills = new ArrayList<>();
        this.inventory = new ArrayList<>();
        initSkills();
    }

    private static int baseHp(JobClass j) {
        return switch (j) { case WARRIOR -> 120; case MAGE -> 75; case ARCHER -> 90; };
    }
    private static int baseMp(JobClass j) {
        return switch (j) { case WARRIOR -> 40; case MAGE -> 100; case ARCHER -> 60; };
    }
    private static int baseAtk(JobClass j) {
        return switch (j) { case WARRIOR -> 18; case MAGE -> 22; case ARCHER -> 16; };
    }
    private static int baseDef(JobClass j) {
        return switch (j) { case WARRIOR -> 10; case MAGE -> 3; case ARCHER -> 6; };
    }
    private static int baseSpd(JobClass j) {
        return switch (j) { case WARRIOR -> 8; case MAGE -> 10; case ARCHER -> 13; };
    }

    private void initSkills() {
        switch (jobClass) {
            case WARRIOR -> {
                skills.add(new Skill("重斩", "造成150%攻击力伤害", SkillType.DAMAGE, 150, 10, 2));
                skills.add(new Skill("战吼", "提升自身攻击力30%，持续3回合", SkillType.BUFF_ATTACK, 30, 15, 4));
                skills.add(new Skill("铁壁", "提升自身防御力50%，持续3回合", SkillType.BUFF_DEFENSE, 50, 15, 4));
            }
            case MAGE -> {
                skills.add(new Skill("火球术", "造成200%攻击力伤害（无视防御50%）", SkillType.DAMAGE, 200, 20, 2));
                skills.add(new Skill("治愈术", "回复最大HP的30%", SkillType.HEAL, 30, 25, 3));
                skills.add(new Skill("奥术爆发", "造成300%攻击力伤害", SkillType.DAMAGE, 300, 40, 5));
            }
            case ARCHER -> {
                skills.add(new Skill("连射", "造成120%攻击力伤害，攻击两次", SkillType.DAMAGE, 120, 12, 2));
                skills.add(new Skill("鹰眼", "提升攻击力20%，持续4回合", SkillType.BUFF_ATTACK, 20, 10, 3));
                skills.add(new Skill("穿心箭", "造成250%攻击力伤害，无视全部防御", SkillType.DAMAGE, 250, 30, 4));
            }
        }
    }

    // 获得经验，返回是否升级
    public boolean gainExp(int amount) {
        exp += amount;
        if (exp >= expToNext) {
            levelUp();
            return true;
        }
        return false;
    }

    private void levelUp() {
        level++;
        exp -= expToNext;
        expToNext = (int) (expToNext * 1.5);
        // 升级加属性
        maxHp += switch (jobClass) { case WARRIOR -> 20; case MAGE -> 10; case ARCHER -> 15; };
        hp = maxHp;
        maxMp += switch (jobClass) { case WARRIOR -> 5; case MAGE -> 15; case ARCHER -> 8; };
        mp = maxMp;
        attack += switch (jobClass) { case WARRIOR -> 3; case MAGE -> 4; case ARCHER -> 3; };
        defense += switch (jobClass) { case WARRIOR -> 2; case MAGE -> 1; case ARCHER -> 1; };
        speed += 1;
    }

    // 使用技能，返回伤害/治疗量
    public int useSkill(int index, Entity target) {
        Skill skill = skills.get(index);
        mp -= skill.getManaCost();
        skill.use();

        return switch (skill.getType()) {
            case DAMAGE -> {
                int dmg = attack * skill.getPower() / 100;
                // 法师火球术减少50%防御效果
                if (jobClass == JobClass.MAGE && skill.getName().equals("火球术")) {
                    int actual = Math.max(1, dmg - target.getDefense() / 2);
                    target.hp = Math.max(0, target.hp - actual);
                    yield actual;
                } else if (jobClass == JobClass.ARCHER && skill.getName().equals("穿心箭")) {
                    // 穿心箭无视防御
                    target.hp = Math.max(0, target.hp - dmg);
                    yield dmg;
                } else if (jobClass == JobClass.ARCHER && skill.getName().equals("连射")) {
                    // 连射攻击两次
                    int total = target.takeDamage(dmg) + target.takeDamage(dmg);
                    yield total;
                } else {
                    yield target.takeDamage(dmg);
                }
            }
            case HEAL -> {
                int healAmt = maxHp * skill.getPower() / 100;
                heal(healAmt);
                yield healAmt;
            }
            case BUFF_ATTACK -> {
                attackBuff = attack * skill.getPower() / 100;
                attack += attackBuff;
                buffDuration = 3;
                yield attackBuff;
            }
            case BUFF_DEFENSE -> {
                defenseBuff = defense * skill.getPower() / 100;
                defense += defenseBuff;
                buffDuration = 3;
                yield defenseBuff;
            }
        };
    }

    // 每回合结束后处理增益衰减和技能冷却
    public void onTurnEnd() {
        if (buffDuration > 0) {
            buffDuration--;
            if (buffDuration == 0) {
                attack -= attackBuff;
                defense -= defenseBuff;
                attackBuff = 0;
                defenseBuff = 0;
            }
        }
        for (Skill s : skills) s.tick();
    }

    public boolean usePotion() {
        if (potions <= 0) return false;
        potions--;
        int healAmt = maxHp / 3;
        heal(healAmt);
        return true;
    }

    public String getJobName() {
        return switch (jobClass) { case WARRIOR -> "战士"; case MAGE -> "法师"; case ARCHER -> "弓手"; };
    }

    @Override
    public String getStatus() {
        return String.format("【%s】Lv.%d %s  HP:%s  MP:%d/%d  经验:%d/%d  药水:%d瓶",
            name, level, getJobName(), hpBar(), mp, maxMp, exp, expToNext, potions);
    }

    public int getMp() { return mp; }
    public int getMaxMp() { return maxMp; }
    public int getLevel() { return level; }
    public int getPotions() { return potions; }
    public List<Skill> getSkills() { return skills; }
    public JobClass getJobClass() { return jobClass; }
}
