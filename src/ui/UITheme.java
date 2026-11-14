package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Optional;

/**
 * Tema dan Utilitas Antarmuka Modern JavaFX (Fintech Aesthetic).
 * Menyediakan palet warna, format mata uang, helpers dialog, dan kartu metrik KPI.
 */
public class UITheme {

    // Palet Warna Fintech Modern
    public static final String COLOR_PRIMARY_DARK = "#0B3B2C";  // Deep Emerald Forest
    public static final String COLOR_SUCCESS = "#059669";       // Mint Green
    public static final String COLOR_SLATE_BTN = "#334155";     // Slate Gray
    public static final String COLOR_DANGER = "#DC2626";        // Crimson Red
    public static final String COLOR_WARNING = "#D97706";       // Amber Gold
    public static final String COLOR_TEXT_MAIN = "#0F172A";     // Slate 900
    public static final String COLOR_TEXT_MUTED = "#64748B";    // Slate 500
    public static final String COLOR_BORDER = "#E2E8F0";        // Slate 200
    public static final String COLOR_CANVAS = "#F8FAFC";        // Canvas Light

    public static String formatRupiah(double nilai) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("id", "ID"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("Rp #,##0", symbols);
        return df.format(nilai);
    }

    public static void applyTheme(Scene scene) {
        try {
            // Coba ambil dari classpath resource
            URL res = UITheme.class.getResource("/ui/styles.css");
            if (res != null) {
                scene.getStylesheets().add(res.toExternalForm());
                return;
            }
            // Fallback: ambil dari file path relatif
            File cssFile = new File("src/ui/styles.css");
            if (cssFile.exists()) {
                scene.getStylesheets().add(cssFile.toURI().toURL().toExternalForm());
            }
        } catch (Exception e) {
            System.err.println("Gagal memuat stylesheet CSS: " + e.getMessage());
        }
    }

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleDialog(alert);
        alert.showAndWait();
    }

    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleDialog(alert);
        alert.showAndWait();
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleDialog(alert);
        alert.showAndWait();
    }

    public static boolean showConfirm(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        styleDialog(alert);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static Optional<String> showTextInput(String title, String header, String prompt, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(prompt);
        styleDialog(dialog);
        return dialog.showAndWait();
    }

    private static void styleDialog(Dialog<?> dialog) {
        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("-fx-font-family: 'Segoe UI'; -fx-background-color: white;");
        if (pane.getScene() != null) {
            applyTheme(pane.getScene());
        }
    }

    public static VBox createKpiCard(String title, Label valLabel, String sub, String accentHex) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setStyle("-fx-border-color: " + accentHex + " " + COLOR_BORDER + " " + COLOR_BORDER + " " + COLOR_BORDER + "; -fx-border-width: 3px 1px 1px 1px;");

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("text-subtitle");
        lblTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: " + COLOR_TEXT_MUTED + ";");

        valLabel.getStyleClass().add("text-hero");
        valLabel.setStyle("-fx-text-fill: " + accentHex + "; -fx-font-size: 22px;");

        Label lblSub = new Label(sub);
        lblSub.getStyleClass().add("text-subtitle");
        lblSub.setStyle("-fx-font-size: 11px; -fx-text-fill: " + COLOR_TEXT_MUTED + ";");

        card.getChildren().addAll(lblTitle, valLabel, lblSub);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    public static Label createStockBadge(int stok) {
        Label badge = new Label();
        if (stok <= 0) {
            badge.setText("Habis (0)");
            badge.getStyleClass().add("badge-stock-empty");
        } else if (stok <= 5) {
            badge.setText("Menipis (" + stok + ")");
            badge.getStyleClass().add("badge-stock-low");
        } else {
            badge.setText(stok + " unit");
            badge.getStyleClass().add("badge-stock-ok");
        }
        return badge;
    }

    public static Label createStatusBadge(String status) {
        Label badge = new Label(status);
        if ("AKTIF".equalsIgnoreCase(status)) {
            badge.getStyleClass().add("badge-active");
        } else {
            badge.getStyleClass().add("badge-inactive");
        }
        return badge;
    }
}
