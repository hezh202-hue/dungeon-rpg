package game;

// 所有战斗单位（玩家、敌人）的基类
public abstract class Entity {
    protected String name;
    protected int hp;
    protected int maxHp;
    protected int attack;
    protected int defense;
    protected int speed;

    public Entity(String name, int maxHp, int attack, int defense, int speed) {
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
    }

    // 受到伤害，返回实际扣血量
    public int takeDamage(int damage) {
        int actual = Math.max(1, damage - defense);
        hp = Math.max(0, hp - actual);
        return actual;
    }

    // 治疗
    public void heal(int amount) {
        hp = Math.min(maxHp, hp + amount);
    }

    public boolean isAlive() { return hp > 0; }
    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getAttack() { return attack; }
    public int getDefense() { return defense; }
    public int getSpeed() { return speed; }

    // 显示血条
    public String hpBar() {
        int bars = (int) ((double) hp / maxHp * 20);
        return "[" + "█".repeat(bars) + "░".repeat(20 - bars) + "] " + hp + "/" + maxHp;
    }

    public abstract String getStatus();
}
