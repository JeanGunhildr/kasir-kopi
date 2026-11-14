package ui;

/**
 * Entry Point Launcher Utama JavaFX untuk Kasir Kedai Kopi (Pacbuck POS).
 * Kelas independen ini mencegah error 'JavaFX runtime components are missing'
 * saat dijalankan di lingkungan JDK 11+ / JDK 21 tanpa modul modular.
 */
public class AppLauncher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
