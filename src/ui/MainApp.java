package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Admin;
import model.Pengguna;
import util.DatabaseUtil;

/**
 * Aplikasi Utama JavaFX (Main Application Stage Manager).
 * Mengatur alur transisi antarmuka: Login -> AdminDashboard / KasirDashboard -> Logout.
 */
public class MainApp extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.primaryStage.setTitle("Pacbuck Coffee POS - Sistem Kasir Kedai Kopi Modern");

        // Inisialisasi Database dan Data Default di Background
        new Thread(DatabaseUtil::initDatabase).start();

        // Tampilkan Layar Login Utama
        showLoginView();

        primaryStage.show();
    }

    public void showLoginView() {
        LoginView loginView = new LoginView(this::onLoginSuccess);
        Scene scene = new Scene(loginView, 960, 680);
        UITheme.applyTheme(scene);

        primaryStage.setMinWidth(460);
        primaryStage.setMinHeight(560);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Pacbuck Coffee - Otentikasi Masuk Sistem");
        primaryStage.centerOnScreen();
    }

    private void onLoginSuccess(Pengguna user) {
        Platform.runLater(() -> {
            if (user instanceof Admin || "ADMIN".equalsIgnoreCase(user.getRole())) {
                showAdminDashboard(user);
            } else {
                showKasirDashboard(user);
            }
        });
    }

    public void showAdminDashboard(Pengguna user) {
        AdminDashboardView dashboard = new AdminDashboardView(user, this::showLoginView);
        Scene scene = new Scene(dashboard, 1280, 820);
        UITheme.applyTheme(scene);

        primaryStage.setMinWidth(1080);
        primaryStage.setMinHeight(720);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Pacbuck Coffee POS - Portal Administrator (" + user.getNama() + ")");
        primaryStage.centerOnScreen();
    }

    public void showKasirDashboard(Pengguna user) {
        KasirDashboardView dashboard = new KasirDashboardView(user, this::showLoginView);
        Scene scene = new Scene(dashboard, 1240, 800);
        UITheme.applyTheme(scene);

        primaryStage.setMinWidth(1040);
        primaryStage.setMinHeight(700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Pacbuck Coffee POS - Terminal Kasir (" + user.getNama() + ")");
        primaryStage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
