@echo off
echo ========================================================
echo   MENJALANKAN APLIKASI KASIR KEDAI KOPI (PACBUCK POS)
echo   Informatika UNSOED - Kelompok 4
echo ========================================================

if not exist bin\ui\LoginFrame.class (
    echo Mengompilasi program terlebih dahulu...
    javac -encoding UTF-8 -cp "lib/*" -d bin src/model/*.java src/util/*.java src/repository/*.java src/service/*.java src/ui/*.java
)

if not exist receipts mkdir receipts

echo Memulai aplikasi Kasir Kopi...
start javaw -cp "bin;lib/*" ui.LoginFrame
