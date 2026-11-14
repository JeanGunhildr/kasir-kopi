@echo off
echo ========================================================
echo   KOMPILASI PROYEK KASIR KEDAI KOPI (PACBUCK POS)
echo   Informatika UNSOED - Kelompok 4
echo ========================================================

if not exist bin mkdir bin
if not exist receipts mkdir receipts

echo Mengompilasi kode program ke bin/...
javac -encoding UTF-8 -cp "lib/*" -d bin src/model/*.java src/util/*.java src/repository/*.java src/service/*.java src/ui/*.java

if %ERRORLEVEL% equ 0 (
    echo.
    echo [SUKSES] Kompilasi berhasil! Semua file class tersimpan di bin/
    echo Jalankan 'run.bat' untuk memulai aplikasi.
) else (
    echo.
    echo [GAGAL] Terjadi kesalahan saat kompilasi.
)
pause
