package ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Admin;
import model.Pengguna;
import service.AuthService;
import util.DatabaseUtil;

import java.sql.Connection;
import java.util.function.Consumer;

/**
 * Tampilan Login Modern JavaFX untuk Pacbuck Coffee POS.
 * Mengusung kartu otentikasi fintech modern dengan Quick Fill Pills dan pengecekan koneksi MySQL real-time.
 */
public class LoginView extends StackPane {

    private final AuthService authService = AuthService.getInstance();
    private final Consumer<Pengguna> onLoginSuccess;

    private TextField txtUsername;
    private PasswordField txtPassword;
    private Label lblDbStatus;
    private Button btnLogin;

    public LoginView(Consumer<Pengguna> onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        initUI();
        checkDatabaseStatus();
    }

    private void initUI() {
        getStyleClass().add("canvas-bg");
        setStyle("-fx-background-color: #F1F5F9;");

        // Container Kartu Otentikasi Tengah
        VBox card = new VBox(0);
        card.setMaxWidth(420);
        card.setMaxHeight(540);
        card.getStyleClass().add("card");
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14px; -fx-border-radius: 14px; -fx-border-color: #CBD5E1; -fx-effect: dropshadow(three-pass-box, rgba(15, 23, 42, 0.08), 24, 0, 0, 8);");

        // 1. Header Emerald Banner
        VBox header = new VBox(6);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(26, 24, 24, 24));
        header.setStyle("-fx-background-color: #0B3B2C; -fx-background-radius: 14px 14px 0 0;");

        Label lblLogo = new Label("☕");
        lblLogo.setStyle("-fx-font-size: 34px; -fx-text-fill: white;");

        Label lblBrand = new Label("Pacbuck Coffee POS");
        lblBrand.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label lblSub = new Label("Modern Point of Sale • Informatika UNSOED Kelompok 4");
        lblSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #A7F3D0;");

        header.getChildren().addAll(lblLogo, lblBrand, lblSub);

        // 2. Form Input Panel
        VBox form = new VBox(14);
        form.setPadding(new Insets(24, 30, 20, 30));

        Label lblUser = new Label("Username");
        lblUser.setStyle("-fx-font-weight: bold; -fx-text-fill: #0F172A; -fx-font-size: 12px;");

        txtUsername = new TextField();
        txtUsername.setPromptText("Masukkan username...");
        txtUsername.setPrefHeight(38);

        Label lblPass = new Label("Password");
        lblPass.setStyle("-fx-font-weight: bold; -fx-text-fill: #0F172A; -fx-font-size: 12px;");

        txtPassword = new PasswordField();
        txtPassword.setPromptText("Masukkan password...");
        txtPassword.setPrefHeight(38);

        btnLogin = new Button("Masuk Sistem (Login)");
        btnLogin.getStyleClass().add("btn-primary");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setPrefHeight(42);
        btnLogin.setStyle("-fx-font-size: 14px;");

        // Quick Demo Fill Pills
        Label lblQuick = new Label("Login Cepat (Akun Demo):");
        lblQuick.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        HBox pnlQuick = new HBox(8);
        pnlQuick.setAlignment(Pos.CENTER);

        Button btnDemoAdmin = new Button("👑 Admin (admin)");
        btnDemoAdmin.getStyleClass().add("btn-pill");
        HBox.setHgrow(btnDemoAdmin, Priority.ALWAYS);
        btnDemoAdmin.setMaxWidth(Double.MAX_VALUE);
        btnDemoAdmin.setOnAction(e -> {
            txtUsername.setText("admin");
            txtPassword.setText("admin123");
            txtPassword.requestFocus();
        });

        Button btnDemoKasir = new Button("☕ Kasir (kasir)");
        btnDemoKasir.getStyleClass().add("btn-pill");
        HBox.setHgrow(btnDemoKasir, Priority.ALWAYS);
        btnDemoKasir.setMaxWidth(Double.MAX_VALUE);
        btnDemoKasir.setOnAction(e -> {
            txtUsername.setText("kasir");
            txtPassword.setText("kasir123");
            txtPassword.requestFocus();
        });

        pnlQuick.getChildren().addAll(btnDemoAdmin, btnDemoKasir);

        form.getChildren().addAll(lblUser, txtUsername, lblPass, txtPassword, btnLogin, lblQuick, pnlQuick);

        // 3. Status Footer
        HBox footer = new HBox(8);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(12, 20, 16, 20));

        lblDbStatus = new Label("● Menghubungkan ke MySQL...");
        lblDbStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
        footer.getChildren().add(lblDbStatus);

        card.getChildren().addAll(header, form, footer);
        getChildren().add(card);
        setAlignment(Pos.CENTER);

        // Action Events
        btnLogin.setOnAction(e -> prosesLogin());

        txtUsername.setOnAction(e -> txtPassword.requestFocus());
        txtPassword.setOnAction(e -> prosesLogin());
    }

    private void checkDatabaseStatus() {
        new Thread(() -> {
            try {
                // Inisialisasi struktur database otomatis
                DatabaseUtil.initDatabase();
                try (Connection conn = DatabaseUtil.getConnection()) {
                    if (conn != null && !conn.isClosed()) {
                        Platform.runLater(() -> {
                            lblDbStatus.setText("● Database: MySQL Connected (db_kasir_kopi)");
                            lblDbStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #059669; -fx-font-weight: bold;");
                        });
                        return;
                    }
                }
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    lblDbStatus.setText("● Menunggu MySQL (Nyalakan Laragon / XAMPP)");
                    lblDbStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #D97706;");
                });
            }
        }).start();
    }

    private void prosesLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            UITheme.showWarning("Validasi Input", "Silakan masukkan username dan password Anda!");
            return;
        }

        try {
            Pengguna user = authService.login(username, password);
            if (user == null) {
                UITheme.showError("Gagal Masuk", "Username atau password salah, atau akun nonaktif!");
                return;
            }

            if (onLoginSuccess != null) {
                onLoginSuccess.accept(user);
            }

        } catch (Exception ex) {
            UITheme.showError("Koneksi Database Error",
                    "Gagal terhubung ke basis data MySQL:\n" + ex.getMessage() +
                            "\n\nPastikan MySQL di Laragon / XAMPP sudah berjalan di port 3306.");
        }
    }
}
