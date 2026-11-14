package ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.Admin;
import model.Pengguna;
import service.AuthService;
import util.DatabaseUtil;

/**
 * Antarmuka Login Sistem Kasir Kedai Kopi (Pacbuck Coffee) versi JavaFX.
 * Titik masuk utama aplikasi (Main Entry Point) yang mengarahkan pengguna ke
 * AdminDashboard atau KasirDashboard sesuai hak akses (Role-Based Access Control).
 */
public class LoginFrame extends Application {

    // Palet Warna Fintech Dashboard (Hex & JavaFX Color)
    public static final String HEX_PRIMARY_DARK = "#0B3B2C"; // Deep Emerald Forest
    public static final String HEX_SUCCESS      = "#059669"; // Mint Green
    public static final String HEX_SLATE_BTN    = "#334155"; // Slate Button
    public static final String HEX_CANVAS       = "#F1F5F9"; // Canvas Slate 100
    public static final String HEX_BORDER       = "#CBD5E1"; // Border
    public static final String HEX_TEXT_MAIN    = "#0F172A"; // Slate 900
    public static final String HEX_TEXT_MUTED   = "#64748B"; // Slate 500

    public static final Color COLOR_PRIMARY_DARK = Color.web(HEX_PRIMARY_DARK);
    public static final Color COLOR_SUCCESS      = Color.web(HEX_SUCCESS);
    public static final Color COLOR_SLATE_BTN    = Color.web(HEX_SLATE_BTN);
    public static final Color FINTECH_CANVAS     = Color.web(HEX_CANVAS);
    public static final Color FINTECH_BORDER     = Color.web(HEX_BORDER);
    public static final Color COLOR_TEXT_MAIN    = Color.web(HEX_TEXT_MAIN);
    public static final Color COLOR_TEXT_MUTED   = Color.web(HEX_TEXT_MUTED);

    private final AuthService authService = AuthService.getInstance();
    private TextField txtUsername;
    private PasswordField txtPassword;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initUI();
    }

    private void initUI() {
        primaryStage.setTitle("Pacbuck Coffee - Otentikasi Masuk Sistem");
        primaryStage.setMinWidth(420);
        primaryStage.setMinHeight(520);

        // Root Container (Menggantikan GridBagLayout untuk memusatkan Card)
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: " + HEX_CANVAS + ";");

        // Card Container Tengah
        VBox card = new VBox();
        card.setPrefSize(380, 480);
        card.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        card.setPadding(new Insets(0, 0, 24, 0));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: " + HEX_BORDER + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
        );

        // 1. Header Banner Atas (Deep Emerald)
        VBox header = new VBox(4);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(22, 20, 22, 20));
        header.setPrefHeight(115);
        header.setStyle(
                "-fx-background-color: " + HEX_PRIMARY_DARK + ";" +
                        "-fx-background-radius: 9 9 0 0;"
        );

        Label lblLogo = new Label("☕");
        lblLogo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        lblLogo.setTextFill(Color.WHITE);

        Label lblBrand = new Label("Pacbuck Coffee POS");
        lblBrand.setFont(Font.font("Segoe UI", FontWeight.BOLD, 19));
        lblBrand.setTextFill(Color.WHITE);

        Label lblSub = new Label("Fintech Point of Sale • Kelompok 4 PBO");
        lblSub.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        lblSub.setTextFill(Color.rgb(167, 243, 208));

        header.getChildren().addAll(lblLogo, lblBrand, lblSub);
        card.getChildren().add(header);

        // 2. Form Input Panel
        VBox form = new VBox();
        form.setPadding(new Insets(20, 28, 0, 28));
        form.setAlignment(Pos.CENTER_LEFT);

        Label lblUser = new Label("Username");
        lblUser.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblUser.setTextFill(COLOR_TEXT_MAIN);

        txtUsername = new TextField();
        txtUsername.setPromptText("Masukkan username...");
        txtUsername.setFont(Font.font("Segoe UI", 13));
        txtUsername.setPrefHeight(38);
        applyInputStyle(txtUsername);
        VBox.setMargin(txtUsername, new Insets(6, 0, 14, 0));

        Label lblPass = new Label("Password");
        lblPass.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblPass.setTextFill(COLOR_TEXT_MAIN);

        txtPassword = new PasswordField();
        txtPassword.setPromptText("Masukkan password...");
        txtPassword.setFont(Font.font("Segoe UI", 13));
        txtPassword.setPrefHeight(38);
        applyInputStyle(txtPassword);
        VBox.setMargin(txtPassword, new Insets(6, 0, 20, 0));

        // Tombol Login (Solid Emerald Kontras Tinggi)
        Button btnLogin = createContrastButton("Masuk Sistem (Login)", HEX_PRIMARY_DARK, "#FFFFFF", 14, 10);
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setPrefHeight(42);
        VBox.setMargin(btnLogin, new Insets(0, 0, 16, 0));

        // Quick Fill Pills untuk kemudahan demo dosen / penguji
        Label lblQuick = new Label("Login Cepat (Akun Demo):");
        lblQuick.setFont(Font.font("Segoe UI", 11));
        lblQuick.setTextFill(COLOR_TEXT_MUTED);
        lblQuick.setMaxWidth(Double.MAX_VALUE);
        lblQuick.setAlignment(Pos.CENTER);
        VBox.setMargin(lblQuick, new Insets(0, 0, 6, 0));

        HBox pnlQuickBtns = new HBox(8);
        pnlQuickBtns.setPrefHeight(32);

        Button btnDemoAdmin = createContrastButton("Admin (admin)", HEX_SLATE_BTN, "#FFFFFF", 11, 8);
        btnDemoAdmin.setMaxWidth(Double.MAX_VALUE);
        btnDemoAdmin.setPrefHeight(32);
        HBox.setHgrow(btnDemoAdmin, Priority.ALWAYS);
        btnDemoAdmin.setOnAction(e -> {
            txtUsername.setText("admin");
            txtPassword.setText("admin123");
            txtPassword.requestFocus();
        });

        Button btnDemoKasir = createContrastButton("Kasir (kasir1)", HEX_SLATE_BTN, "#FFFFFF", 11, 8);
        btnDemoKasir.setMaxWidth(Double.MAX_VALUE);
        btnDemoKasir.setPrefHeight(32);
        HBox.setHgrow(btnDemoKasir, Priority.ALWAYS);
        btnDemoKasir.setOnAction(e -> {
            txtUsername.setText("kasir1");
            txtPassword.setText("kasir123");
            txtPassword.requestFocus();
        });

        pnlQuickBtns.getChildren().addAll(btnDemoAdmin, btnDemoKasir);
        form.getChildren().addAll(lblUser, txtUsername, lblPass, txtPassword, btnLogin, lblQuick, pnlQuickBtns);
        card.getChildren().add(form);

        // Spacer Fleksibel (Menggantikan Box.createVerticalGlue())
        Region verticalGlue = new Region();
        VBox.setVgrow(verticalGlue, Priority.ALWAYS);
        card.getChildren().add(verticalGlue);

        // Status Footer
        Label lblDbStatus = new Label("● Database: MySQL Connected (db_kasir_kopi)");
        lblDbStatus.setFont(Font.font("Segoe UI", 11));
        lblDbStatus.setTextFill(COLOR_SUCCESS);
        lblDbStatus.setMaxWidth(Double.MAX_VALUE);
        lblDbStatus.setAlignment(Pos.CENTER);
        card.getChildren().add(lblDbStatus);

        root.getChildren().add(card);

        // Action Events
        btnLogin.setOnAction(e -> prosesLogin());

        txtUsername.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) prosesLogin();
        });
        txtPassword.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) prosesLogin();
        });

        Scene scene = new Scene(root, 460, 560);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    /**
     * Meniru styling rounded & focus border FlatLaf pada TextField/PasswordField.
     */
    private void applyInputStyle(TextInputControl field) {
        String normalStyle =
                "-fx-background-color: white;" +
                        "-fx-border-color: " + HEX_BORDER + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 4 10 4 10;";

        String focusedStyle =
                "-fx-background-color: white;" +
                        "-fx-border-color: " + HEX_SUCCESS + ";" +
                        "-fx-border-width: 1.5;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 4 10 4 10;";

        field.setStyle(normalStyle);
        field.focusedProperty().addListener((obs, wasFocused, isFocused) ->
                field.setStyle(isFocused ? focusedStyle : normalStyle)
        );
    }

    /**
     * Pengganti AdminDashboard.ContrastButton dengan efek hover di JavaFX.
     */
    private Button createContrastButton(String text, String bgHex, String fgHex, int fontSize, int radius) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, fontSize));
        String baseStyle =
                "-fx-background-color: " + bgHex + ";" +
                        "-fx-text-fill: " + fgHex + ";" +
                        "-fx-background-radius: " + radius + ";" +
                        "-fx-cursor: hand;";
        String hoverStyle = baseStyle + "-fx-opacity: 0.9;";

        btn.setStyle(baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        return btn;
    }

    private void prosesLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Silakan masukkan username dan password!");
            return;
        }

        try {
            Pengguna user = authService.login(username, password);
            if (user == null) {
                showAlert(Alert.AlertType.ERROR, "Gagal Masuk", "Username atau password salah, atau akun nonaktif!");
                return;
            }

            // Role-based routing (Asumsi AdminDashboard & KasirDashboard juga berbasis JavaFX Stage)
            Stage dashboardStage = new Stage();
            if (user instanceof Admin || "ADMIN".equalsIgnoreCase(user.getRole())) {
                new AdminDashboard(user).start(dashboardStage);
            } else {
                new KasirDashboard(user).start(dashboardStage);
            }
            primaryStage.close();

        } catch (Exception ex) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Koneksi Database Error",
                    "Gagal terhubung ke database: " + ex.getMessage() +
                            "\nPastikan MySQL di Laragon / XAMPP sudah berjalan di port 3306."
            );
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.initOwner(primaryStage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        // Inisialisasi Database dan Data Default Otomatis
        DatabaseUtil.initDatabase();

        // Menjalankan siklus hidup JavaFX
        launch(args);
    }
}