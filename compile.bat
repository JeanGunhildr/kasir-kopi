@echo off
echo ========================================================
echo   KOMPILASI PROYEK KASIR KEDAI KOPI (PACBUCK POS)
echo   GUI Modern JavaFX - Informatika UNSOED Kelompok 4
echo ========================================================

if not exist bin mkdir bin
if not exist receipts mkdir receipts

echo Mengompilasi kode program dengan JavaFX SDK ke bin/...
javac -encoding UTF-8 --module-path "lib/javafx-sdk/lib" --add-modules javafx.controls,javafx.fxml -cp "lib/*;lib/javafx-sdk/lib/*" -d bin src/model/*.java src/util/*.java src/repository/*.java src/service/*.java src/ui/*.java

if %ERRORLEVEL% equ 0 (
    echo Menyalin aset stylesheet CSS ke bin/...
    if not exist bin\ui mkdir bin\ui
    copy /Y src\ui\styles.css bin\ui\styles.css >nul 2>&1
    echo.
    echo [SUKSES] Kompilasi berhasil! Semua file class tersimpan di bin/
    echo Jalankan 'run.bat' untuk memulai aplikasi modern JavaFX.
) else (
    echo.
    echo [GAGAL] Terjadi kesalahan saat kompilasi.
)
pause
