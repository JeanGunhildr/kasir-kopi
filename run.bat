@echo off
echo ========================================================
echo   MENJALANKAN APLIKASI KASIR KEDAI KOPI (PACBUCK POS)
echo   GUI Modern JavaFX - Informatika UNSOED Kelompok 4
echo ========================================================

if not exist bin\ui\AppLauncher.class (
    echo Mengompilasi program JavaFX terlebih dahulu...
    call compile.bat
)

if not exist receipts mkdir receipts

echo Memulai aplikasi Kasir Kopi Modern (JavaFX)...
start javaw --module-path "lib/javafx-sdk/lib" --add-modules javafx.controls,javafx.fxml -cp "bin;lib/*;lib/javafx-sdk/lib/*" ui.AppLauncher
