package game;

public class Skill {
    private String name;
    private String description;
    private SkillType type;
    private int power;        // 技能威力（倍率百分比 或 固定值）
    private int manaCost;
    private int cooldown;     // 冷却回合数
    private int currentCooldown;

    public Skill(String name, String description, SkillType type, int power, int manaCost, int cooldown) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.power = power;
        this.manaCost = manaCost;
        this.cooldown = cooldown;
        this.currentCooldown = 0;
    }

    public boolean isReady() { return currentCooldown == 0; }

    public void use() { currentCooldown = cooldown; }

    // 每回合冷却减少1
    public void tick() { if (currentCooldown > 0) currentCooldown--; }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public SkillType getType() { return type; }
    public int getPower() { return power; }
    public int getManaCost() { return manaCost; }
    public int getCurrentCooldown() { return currentCooldown; }

    @Override
    public String toString() {
        String cdStr = currentCooldown > 0 ? " [冷却:" + currentCooldown + "]" : "";
        return name + " - " + description + " (MP:" + manaCost + ")" + cdStr;
    }
}
