@echo off
rem 一键启动：先把 src 里的 Java 源码编译到 out，再开始游戏
rem 注意：本文件用 GBK 编码保存，cmd 才能正确读取中文
cd /d "%~dp0"

echo 正在编译...
javac -encoding UTF-8 -d out src\game\*.java
if errorlevel 1 (
    echo 编译失败，请检查上面的报错信息。
    pause
    exit /b 1
)

rem 切到 UTF-8 编码，让游戏里的中文和表情符号正常显示
chcp 65001 >nul
java -Dstdout.encoding=UTF-8 -cp out game.Main
pause
