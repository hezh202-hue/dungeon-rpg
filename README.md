# stickman-game

一个用 Java 写的命令行回合制地下城 RPG 游戏。

选择职业（战士 / 法师 / 弓手），探索 10 层地下城，击败最终 Boss「远古火龙」。

## 运行环境

- JDK 14 或更高版本（推荐 JDK 21）
- Python 3（仅自动游戏机器人需要）

## 怎么玩

**Windows：** 双击 `play.bat`，会自动编译并启动游戏。

**手动运行：**

```bash
javac -encoding UTF-8 -d out src/game/*.java
java -cp out game.Main
```

## 自动游戏机器人

`autoplay.py` 会用法师策略自动玩游戏：

```bash
javac -encoding UTF-8 -d out src/game/*.java
python autoplay.py
```

## 项目结构

```
src/game/
  Main.java      主菜单、职业选择
  Dungeon.java   地下城探索（战斗 / 宝箱 / 陷阱）
  Battle.java    回合制战斗
  Player.java    玩家与职业属性
  Enemy.java     敌人
  Skill.java     技能
autoplay.py      自动游戏机器人
play.bat         Windows 一键启动
```
