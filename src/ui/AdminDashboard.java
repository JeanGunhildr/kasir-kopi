package ui;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import model.DetailTransaksi;
import model.Kasir;
import model.Menu;
import model.Pengguna;
import model.Promo;
import model.Transaksi;
import service.AuthService;
import service.LaporanService;
import service.MenuService;
import service.PromoService;
import service.TransaksiService;
import util.PDFUtil;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Dashboard Antarmuka Administrator (Admin) versi JavaFX.
 * Menyediakan transaksi kasir terintegrasi (POS), pengelolaan Menu & Stok (FR-01, FR-03),
 * Akun Kasir (FR-02), Kode Promo & Diskon (FR-07), Laporan Penjualan (FR-04),
 * dan Riwayat Transaksi lengkap dengan Rincian Barang Terjual (Master-Detail).
 */
public class AdminDashboard extends Application {

    // Palet Warna Fintech Dashboard (Hex & JavaFX Color)
    public static final String HEX_PRIMARY_DARK = "#0B3B2C"; // Deep Emerald Forest
    public static final String HEX_SUCCESS      = "#059669"; // Solid Mint Green
    public static final String HEX_SLATE_BTN    = "#334155"; // Slate Button
    public static final String HEX_DANGER       = "#DC2626"; // Solid Crimson Red
    public static final String HEX_AMBER        = "#D97706"; // Amber Warning

    public static final String HEX_CANVAS       = "#F1F5F9"; // Canvas Slate 100
    public static final String HEX_CARD         = "#FFFFFF"; // White Card Container
    public static final String HEX_BORDER       = "#CBD5E1"; // Border Slate 300

    public static final String HEX_TEXT_MAIN    = "#0F172A"; // Dark Slate
    public static final String HEX_TEXT_MUTED   = "#64748B"; // Muted Slate

    public static final Color COLOR_PRIMARY_DARK = Color.web(HEX_PRIMARY_DARK);
    public static final Color COLOR_SUCCESS      = Color.web(HEX_SUCCESS);
    public static final Color COLOR_SLATE_BTN    = Color.web(HEX_SLATE_BTN);
    public static final Color COLOR_DANGER       = Color.web(HEX_DANGER);
    public static final Color COLOR_AMBER        = Color.web(HEX_AMBER);

    public static final Color FINTECH_CANVAS     = Color.web(HEX_CANVAS);
    public static final Color FINTECH_CARD       = Color.web(HEX_CARD);
    public static final Color FINTECH_BORDER     = Color.web(HEX_BORDER);

    public static final Color COLOR_TEXT_MAIN    = Color.web(HEX_TEXT_MAIN);
    public static final Color COLOR_TEXT_MUTED   = Color.web(HEX_TEXT_MUTED);

    public static final Font FONT_LARGE_TITLE = Font.font("Segoe UI", FontWeight.BOLD, 20);
    public static final Font FONT_TITLE       = Font.font("Segoe UI", FontWeight.BOLD, 15);
    public static final Font FONT_REGULAR     = Font.font("Segoe UI", FontWeight.NORMAL, 13);
    public static final Font FONT_BOLD        = Font.font("Segoe UI", FontWeight.BOLD, 13);
    public static final Font FONT_HERO_NUM    = Font.font("Segoe UI", FontWeight.BOLD, 24);

    private final MenuService menuService = new MenuService();
    private final AuthService authService = AuthService.getInstance();
    private final PromoService promoService = new PromoService();
    private final LaporanService laporanService = new LaporanService();
    private final TransaksiService transaksiService = new TransaksiService();
    private final Pengguna currentUser;

    private Stage primaryStage;
    private Runnable refreshPosMenuHook;
    private Runnable refreshLaporanHook;
    private Runnable refreshRiwayatHook;

    public static String formatRupiah(double nilai) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("id-ID"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("Rp #,##0", symbols);
        return df.format(nilai);
    }

    public static String toHexString(Color color) {
        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);
        return String.format("#%02X%02X%02X", r, g, b);
    }

    /**
     * Komponen Tombol Kustom Berkontras Tinggi (ContrastButton) versi JavaFX.
     */
    public static class ContrastButton extends Button {
        private String normalBgHex;
        private String hoverBgHex;
        private String pressedBgHex;
        private String normalFgHex;
        private int cornerRadius = 8;
        private String paddingCss = "8 14 8 14";

        public ContrastButton(String text, Color bg, Color fg) {
            this(text, toHexString(bg), toHexString(fg));
        }

        public ContrastButton(String text, String bgHex, String fgHex) {
            super(text);
            setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            setColors(bgHex, fgHex);

            setOnMouseEntered(e -> {
                if (!isDisabled()) applyStateStyle(hoverBgHex, normalFgHex);
            });
            setOnMouseExited(e -> {
                if (!isDisabled()) applyStateStyle(normalBgHex, normalFgHex);
            });
            setOnMousePressed(e -> {
                if (!isDisabled()) applyStateStyle(pressedBgHex, normalFgHex);
            });
            setOnMouseReleased(e -> {
                if (!isDisabled()) applyStateStyle(isHover() ? hoverBgHex : normalBgHex, normalFgHex);
            });
            disabledProperty().addListener((obs, oldVal, isDis) -> {
                if (isDis) {
                    applyStateStyle("#CBD5E1", "#94A3B8");
                } else {
                    applyStateStyle(normalBgHex, normalFgHex);
                }
            });
        }

        private static String adjustBrightnessHex(String hex, float factor) {
            Color c = Color.web(hex);
            int r = Math.min(255, Math.max(0, (int) (c.getRed() * 255 * factor)));
            int g = Math.min(255, Math.max(0, (int) (c.getGreen() * 255 * factor)));
            int b = Math.min(255, Math.max(0, (int) (c.getBlue() * 255 * factor)));
            return String.format("#%02X%02X%02X", r, g, b);
        }

        public void setColors(Color bg, Color fg) {
            setColors(toHexString(bg), toHexString(fg));
        }

        public void setColors(String bgHex, String fgHex) {
            this.normalBgHex = bgHex;
            this.normalFgHex = fgHex;
            this.hoverBgHex = adjustBrightnessHex(bgHex, 1.18f);
            this.pressedBgHex = adjustBrightnessHex(bgHex, 0.82f);
            applyStateStyle(normalBgHex, normalFgHex);
        }

        public void setPaddingCss(String paddingCss) {
            this.paddingCss = paddingCss;
            applyStateStyle(normalBgHex, normalFgHex);
        }

        private void applyStateStyle(String bg, String fg) {
            setStyle(
                    "-fx-background-color: " + bg + ";" +
                            "-fx-text-fill: " + fg + ";" +
                            "-fx-background-radius: " + cornerRadius + ";" +
                            "-fx-padding: " + paddingCss + ";" +
                            "-fx-cursor: hand;"
            );
        }
    }

    public AdminDashboard(Pengguna user) {
        this.currentUser = user;
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initUI();
    }

    private void initUI() {
        if (this.primaryStage == null) {
            this.primaryStage = new Stage();
        }

        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        double maxW = visualBounds.getWidth();
        double maxH = visualBounds.getHeight();

        primaryStage.setTitle("Pacbuck Coffee - Panel Administrator (" + currentUser.getNama() + ")");
        primaryStage.setMinWidth(Math.min(960, maxW));
        primaryStage.setMinHeight(Math.min(600, maxH));

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + HEX_CANVAS + ";");

        // Header Top Bar
        root.setTop(buatTopBar());

        // Content Area
        StackPane contentArea = new StackPane();
        contentArea.setPadding(new Insets(12));
        contentArea.setStyle("-fx-background-color: " + HEX_CANVAS + ";");

        Region panelPOS = buatPanelPOS();
        Region panelMenu = buatPanelMenu();
        Region panelKasir = buatPanelKasir();
        Region panelPromo = buatPanelPromo();
        Region panelLaporan = buatPanelLaporan();
        Region panelRiwayat = buatPanelRiwayat();

        Region[] panels = {panelPOS, panelMenu, panelKasir, panelPromo, panelLaporan, panelRiwayat};
        for (int i = 0; i < panels.length; i++) {
            panels[i].setVisible(i == 0);
            panels[i].setManaged(i == 0);
            contentArea.getChildren().add(panels[i]);
        }

        root.setLeft(buatSidebar(panels));
        root.setCenter(contentArea);

        double initW = Math.min(1280, maxW);
        double initH = Math.min(760, maxH);

        Scene scene = new Scene(root, initW, initH);
        primaryStage.setScene(scene);
        primaryStage.setX(visualBounds.getMinX() + (maxW - initW) / 2);
        primaryStage.setY(visualBounds.getMinY() + (maxH - initH) / 2);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private HBox buatTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 20, 12, 20));
        bar.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: " + HEX_BORDER + ";" +
                        "-fx-border-width: 0 0 1 0;"
        );

        HBox left = new HBox(10);
        left.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label("Pacbuck Coffee Management System");
        lblTitle.setFont(FONT_LARGE_TITLE);
        lblTitle.setTextFill(COLOR_PRIMARY_DARK);

        Label lblSubtitle = new Label("|  Portal Eksekutif & Administrator Kedai Kopi");
        lblSubtitle.setFont(FONT_REGULAR);
        lblSubtitle.setTextFill(COLOR_TEXT_MUTED);

        left.getChildren().addAll(lblTitle, lblSubtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox right = new HBox(12);
        right.setAlignment(Pos.CENTER_RIGHT);

        // User Profile Badge (Pill Style)
        HBox userBadge = new HBox();
        userBadge.setAlignment(Pos.CENTER);
        userBadge.setPadding(new Insets(6, 14, 6, 14));
        userBadge.setStyle(
                "-fx-background-color: " + HEX_CANVAS + ";" +
                        "-fx-border-color: " + HEX_BORDER + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 999;" +
                        "-fx-background-radius: 999;"
        );

        Label lblUser = new Label("Admin: " + currentUser.getNama());
        lblUser.setFont(FONT_BOLD);
        lblUser.setTextFill(COLOR_TEXT_MAIN);
        userBadge.getChildren().add(lblUser);

        ContrastButton btnLogout = new ContrastButton("Logout", HEX_DANGER, "#FFFFFF");
        btnLogout.setOnAction(e -> {
            if (showConfirm("Konfirmasi Logout", "Apakah Anda yakin ingin logout dari sistem?")) {
                authService.logout();
                try {
                    new LoginFrame().start(new Stage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                primaryStage.close();
            }
        });

        right.getChildren().addAll(userBadge, btnLogout);
        bar.getChildren().addAll(left, spacer, right);
        return bar;
    }

    private VBox buatSidebar(Region[] panels) {
        VBox sidebar = new VBox(6);
        sidebar.setPrefWidth(240);
        sidebar.setPadding(new Insets(18, 14, 18, 14));
        sidebar.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: " + HEX_BORDER + ";" +
                        "-fx-border-width: 0 1 0 0;"
        );

        // Brand Logo
        HBox brand = new HBox(8);
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.setPadding(new Insets(0, 0, 14, 4));

        Label lblLogo = new Label("☕");
        lblLogo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));

        Label lblBrandName = new Label("Pacbuck Admin");
        lblBrandName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        lblBrandName.setTextFill(COLOR_PRIMARY_DARK);

        brand.getChildren().addAll(lblLogo, lblBrandName);
        sidebar.getChildren().add(brand);

        // Tombol Navigasi Sidebar
        ContrastButton btnPos = createSidebarButton("Transaksi Kasir (POS)", true);
        ContrastButton btnMenu = createSidebarButton("Kelola Menu & Stok", false);
        ContrastButton btnKasir = createSidebarButton("Kelola Akun Kasir", false);
        ContrastButton btnPromo = createSidebarButton("Kelola Promo & Diskon", false);
        ContrastButton btnLaporan = createSidebarButton("Laporan Penjualan", false);
        ContrastButton btnRiwayat = createSidebarButton("Riwayat Transaksi", false);

        ContrastButton[] buttons = {btnPos, btnMenu, btnKasir, btnPromo, btnLaporan, btnRiwayat};

        for (int i = 0; i < buttons.length; i++) {
            final int idx = i;
            buttons[i].setOnAction(e -> {
                for (int j = 0; j < panels.length; j++) {
                    boolean active = (j == idx);
                    panels[j].setVisible(active);
                    panels[j].setManaged(active);
                    buttons[j].setColors(active ? HEX_PRIMARY_DARK : HEX_CANVAS, active ? "#FFFFFF" : HEX_TEXT_MAIN);
                }
                if (idx == 0 && refreshPosMenuHook != null) refreshPosMenuHook.run();
                if (idx == 4 && refreshLaporanHook != null) refreshLaporanHook.run();
                if (idx == 5 && refreshRiwayatHook != null) refreshRiwayatHook.run();
            });
            sidebar.getChildren().add(buttons[i]);
        }

        Region verticalGlue = new Region();
        VBox.setVgrow(verticalGlue, Priority.ALWAYS);
        sidebar.getChildren().add(verticalGlue);

        // Mini Card Info
        VBox miniCard = new VBox(2);
        miniCard.setPadding(new Insets(12, 14, 12, 14));
        miniCard.setStyle(
                "-fx-background-color: #0F172A;" +
                        "-fx-background-radius: 12;"
        );

        Label lblCardTitle = new Label("Pacbuck Enterprise");
        lblCardTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblCardTitle.setTextFill(Color.WHITE);

        Label lblCardSub = new Label("Kelompok 4 PBO UNSOED");
        lblCardSub.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        lblCardSub.setTextFill(Color.web("#94A3B8"));

        Label lblCardNumber = new Label("•••• 2026 ADMIN");
        lblCardNumber.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
        lblCardNumber.setTextFill(Color.web("#CBD5E1"));
        VBox.setMargin(lblCardNumber, new Insets(8, 0, 0, 0));

        miniCard.getChildren().addAll(lblCardTitle, lblCardSub, lblCardNumber);
        sidebar.getChildren().add(miniCard);

        return sidebar;
    }

    private ContrastButton createSidebarButton(String text, boolean active) {
        ContrastButton btn = new ContrastButton(
                text,
                active ? HEX_PRIMARY_DARK : HEX_CANVAS,
                active ? "#FFFFFF" : HEX_TEXT_MAIN
        );
        btn.setFont(FONT_BOLD);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(40);
        return btn;
    }

    // =========================================================================
    // PANEL 1: TRANSAKSI KASIR (POS) UNTUK ADMIN
    // =========================================================================
    private Region buatPanelPOS() {
        BorderPane panel = new BorderPane();
        panel.setStyle("-fx-background-color: " + HEX_CANVAS + ";");

        Transaksi transaksi = new Transaksi(transaksiService.generateNoFaktur(), "Admin: " + currentUser.getNama());
        final Promo[] appliedPromo = {null};

        // --- KOLOM KIRI (55%): KATALOG MENU ---
        VBox pnlKiri = new VBox(8);
        styleFintechCard(pnlKiri, 12, 14);

        HBox pnlHeaderMenu = new HBox(8);
        pnlHeaderMenu.setAlignment(Pos.CENTER_LEFT);

        HBox pnlFilterPills = new HBox(6);
        pnlFilterPills.setAlignment(Pos.CENTER_LEFT);

        ContrastButton tabAll = createFilterPill("Semua", true);
        ContrastButton tabKopi = createFilterPill("Kopi", false);
        ContrastButton tabNonKopi = createFilterPill("Non-Kopi", false);
        ContrastButton tabMakanan = createFilterPill("Makanan", false);
        pnlFilterPills.getChildren().addAll(tabAll, tabKopi, tabNonKopi, tabMakanan);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        TextField txtSearch = createPillTextField("", "Cari nama / kode menu...", true);
        txtSearch.setPrefWidth(195);
        txtSearch.setPrefHeight(34);

        pnlHeaderMenu.getChildren().addAll(pnlFilterPills, headerSpacer, txtSearch);

        ObservableList<Menu> menuItems = FXCollections.observableArrayList();
        TableView<Menu> tblMenu = new TableView<>(menuItems);
        styleFintechTable(tblMenu);
        VBox.setVgrow(tblMenu, Priority.ALWAYS);

        TableColumn<Menu, Integer> colMenuId = new TableColumn<>("ID");
        colMenuId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        colMenuId.setPrefWidth(40);

        TableColumn<Menu, String> colMenuKode = new TableColumn<>("Kode");
        colMenuKode.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getKode()));
        colMenuKode.setPrefWidth(65);

        TableColumn<Menu, String> colMenuNama = new TableColumn<>("Nama Menu");
        colMenuNama.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getNama()));
        colMenuNama.setPrefWidth(165);

        TableColumn<Menu, String> colMenuKat = new TableColumn<>("Kategori");
        colMenuKat.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getKategori()));
        colMenuKat.setPrefWidth(85);

        TableColumn<Menu, String> colMenuHarga = new TableColumn<>("Harga");
        colMenuHarga.setCellValueFactory(c -> new ReadOnlyStringWrapper(formatRupiah(c.getValue().getHarga())));
        colMenuHarga.setPrefWidth(95);

        TableColumn<Menu, Integer> colMenuStok = new TableColumn<>("Stok");
        colMenuStok.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getStok()));
        colMenuStok.setPrefWidth(55);

        tblMenu.getColumns().addAll(List.of(colMenuId, colMenuKode, colMenuNama, colMenuKat, colMenuHarga, colMenuStok));

        final String[] currentFilter = {"Semua"};
        Runnable loadMenuData = () -> {
            try {
                menuItems.clear();
                List<Menu> list = menuService.getDaftarMenu(currentFilter[0]);
                String query = txtSearch.getText().trim().toLowerCase();
                for (Menu m : list) {
                    if (query.isEmpty() || m.getNama().toLowerCase().contains(query) || m.getKode().toLowerCase().contains(query)) {
                        menuItems.add(m);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };
        loadMenuData.run();
        this.refreshPosMenuHook = loadMenuData;

        ContrastButton[] filterTabs = {tabAll, tabKopi, tabNonKopi, tabMakanan};
        String[] filterNames = {"Semua", "Kopi", "Non-Kopi", "Makanan"};
        for (int i = 0; i < filterTabs.length; i++) {
            final int idx = i;
            filterTabs[i].setOnAction(e -> {
                currentFilter[0] = filterNames[idx];
                for (ContrastButton b : filterTabs) {
                    b.setColors(HEX_CANVAS, HEX_TEXT_MAIN);
                }
                filterTabs[idx].setColors(HEX_PRIMARY_DARK, "#FFFFFF");
                loadMenuData.run();
            });
        }
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> loadMenuData.run());

        ContrastButton btnPesanMenu = new ContrastButton("+ Tambahkan ke Keranjang", HEX_PRIMARY_DARK, "#FFFFFF");
        btnPesanMenu.setFont(FONT_BOLD);
        btnPesanMenu.setMaxWidth(Double.MAX_VALUE);
        btnPesanMenu.setPrefHeight(38);

        pnlKiri.getChildren().addAll(pnlHeaderMenu, tblMenu, btnPesanMenu);

        // --- KOLOM KANAN (45%): KERANJANG & CHECKOUT ---
        VBox pnlKanan = new VBox(8);
        styleFintechCard(pnlKanan, 12, 14);

        HBox pnlHeaderCart = new HBox();
        pnlHeaderCart.setAlignment(Pos.CENTER_LEFT);

        Label lblFaktur = new Label("Faktur: " + transaksi.getNoFaktur());
        lblFaktur.setFont(FONT_TITLE);
        lblFaktur.setTextFill(COLOR_PRIMARY_DARK);

        Region cartHeaderSpacer = new Region();
        HBox.setHgrow(cartHeaderSpacer, Priority.ALWAYS);

        Label lblKasirInfo = new Label("Operator: " + currentUser.getNama());
        lblKasirInfo.setFont(FONT_REGULAR);
        lblKasirInfo.setTextFill(COLOR_TEXT_MUTED);

        pnlHeaderCart.getChildren().addAll(lblFaktur, cartHeaderSpacer, lblKasirInfo);

        ObservableList<DetailTransaksi> cartItems = FXCollections.observableArrayList();
        TableView<DetailTransaksi> tblCart = new TableView<>(cartItems);
        styleFintechTable(tblCart);
        VBox.setVgrow(tblCart, Priority.ALWAYS);

        TableColumn<DetailTransaksi, Integer> colCartId = new TableColumn<>("ID");
        colCartId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getMenuId()));
        colCartId.setPrefWidth(35);

        TableColumn<DetailTransaksi, String> colCartNama = new TableColumn<>("Nama Menu");
        colCartNama.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getNamaMenu()));
        colCartNama.setPrefWidth(140);

        TableColumn<DetailTransaksi, String> colCartHarga = new TableColumn<>("Harga");
        colCartHarga.setCellValueFactory(c -> new ReadOnlyStringWrapper(formatRupiah(c.getValue().getHarga())));
        colCartHarga.setPrefWidth(80);

        TableColumn<DetailTransaksi, Integer> colCartQty = new TableColumn<>("Qty");
        colCartQty.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getQty()));
        colCartQty.setPrefWidth(45);

        TableColumn<DetailTransaksi, String> colCartSub = new TableColumn<>("Subtotal");
        colCartSub.setCellValueFactory(c -> new ReadOnlyStringWrapper(formatRupiah(c.getValue().getSubtotal())));
        colCartSub.setPrefWidth(90);

        tblCart.getColumns().addAll(List.of(colCartId, colCartNama, colCartHarga, colCartQty, colCartSub));

        // Action Toolbar Cart (+1, -1, Hapus, Reset)
        HBox pnlCartControls = new HBox(6);
        pnlCartControls.setAlignment(Pos.CENTER_RIGHT);

        ContrastButton btnPlus1 = new ContrastButton("+1", HEX_SUCCESS, "#FFFFFF");
        ContrastButton btnMinus1 = new ContrastButton("-1", HEX_AMBER, "#FFFFFF");
        ContrastButton btnHapus = new ContrastButton("Hapus", HEX_DANGER, "#FFFFFF");
        ContrastButton btnKosongkan = new ContrastButton("Reset", HEX_SLATE_BTN, "#FFFFFF");
        pnlCartControls.getChildren().addAll(btnPlus1, btnMinus1, btnHapus, btnKosongkan);

        // Panel Pembayaran & Rincian
        VBox pnlCheckout = new VBox(8);
        pnlCheckout.setPadding(new Insets(6, 0, 0, 0));

        // Promo Row
        HBox pnlPromoRow = new HBox(6);
        pnlPromoRow.setAlignment(Pos.CENTER_LEFT);
        Label lblPromoTitle = new Label("Kode Promo: ");
        lblPromoTitle.setFont(FONT_REGULAR);
        TextField txtKodePromo = createPillTextField("", "Masukkan kode promo...", true);
        HBox.setHgrow(txtKodePromo, Priority.ALWAYS);
        ContrastButton btnTerapkanPromo = new ContrastButton("Terapkan", HEX_PRIMARY_DARK, "#FFFFFF");
        pnlPromoRow.getChildren().addAll(lblPromoTitle, txtKodePromo, btnTerapkanPromo);

        // Ringkasan Total
        VBox pnlSummary = new VBox(3);
        Label lblSubtotalVal = new Label("Rp 0");
        Label lblDiskonVal = new Label("Rp 0");
        Label lblPajakVal = new Label("Rp 0");
        Label lblTotalVal = new Label("Rp 0");
        lblTotalVal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblTotalVal.setTextFill(COLOR_PRIMARY_DARK);

        pnlSummary.getChildren().addAll(
                buatBarisLabel("Subtotal Pesanan:", lblSubtotalVal),
                buatBarisLabel("Diskon Promo:", lblDiskonVal),
                buatBarisLabel("PPN (10%):", lblPajakVal),
                buatBarisLabel("Total Tagihan:", lblTotalVal)
        );

        // Template Nominal Uang
        HBox pnlQuickCash = new HBox(4);
        ContrastButton btnUangPas = new ContrastButton("Pas", HEX_PRIMARY_DARK, "#FFFFFF");
        ContrastButton btnUang20k = new ContrastButton("20k", HEX_SLATE_BTN, "#FFFFFF");
        ContrastButton btnUang50k = new ContrastButton("50k", HEX_SLATE_BTN, "#FFFFFF");
        ContrastButton btnUang100k = new ContrastButton("100k", HEX_SLATE_BTN, "#FFFFFF");
        ContrastButton btnUang200k = new ContrastButton("200k", HEX_SLATE_BTN, "#FFFFFF");

        for (ContrastButton b : new ContrastButton[]{btnUangPas, btnUang20k, btnUang50k, btnUang100k, btnUang200k}) {
            b.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(b, Priority.ALWAYS);
        }
        pnlQuickCash.getChildren().addAll(btnUangPas, btnUang20k, btnUang50k, btnUang100k, btnUang200k);

        // Input Bayar & Kembalian
        GridPane pnlBayarRow = new GridPane();
        pnlBayarRow.setHgap(8);
        pnlBayarRow.setVgap(6);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(45);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(55);
        pnlBayarRow.getColumnConstraints().addAll(col1, col2);

        TextField txtBayar = createPillTextField("", "Nominal tunai...", false);
        Label lblKembalian = new Label("Rp 0");
        lblKembalian.setFont(FONT_BOLD);
        lblKembalian.setTextFill(COLOR_SUCCESS);

        pnlBayarRow.add(new Label("Nominal Bayar:"), 0, 0);
        pnlBayarRow.add(txtBayar, 1, 0);
        pnlBayarRow.add(new Label("Kembalian:"), 0, 1);
        pnlBayarRow.add(lblKembalian, 1, 1);

        // Tombol Bayar
        ContrastButton btnBayar = new ContrastButton("Bayar & Cetak Struk", HEX_SUCCESS, "#FFFFFF");
        btnBayar.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btnBayar.setMaxWidth(Double.MAX_VALUE);
        btnBayar.setPrefHeight(40);

        pnlCheckout.getChildren().addAll(pnlPromoRow, pnlSummary, pnlQuickCash, pnlBayarRow, btnBayar);
        pnlKanan.getChildren().addAll(pnlHeaderCart, tblCart, pnlCartControls, pnlCheckout);

        // Split Layout 55% / 45%
        SplitPane splitPane = new SplitPane(pnlKiri, pnlKanan);
        splitPane.setDividerPositions(0.55);
        splitPane.setStyle("-fx-background-color: " + HEX_CANVAS + "; -fx-box-border: transparent;");
        panel.setCenter(splitPane);

        // Logic Helper
        Runnable refreshCart = () -> {
            cartItems.setAll(transaksi.getItems());
            tblCart.refresh();
            transaksi.hitungTotal(appliedPromo[0]);
            lblSubtotalVal.setText(formatRupiah(transaksi.getSubtotal()));
            lblDiskonVal.setText("- " + formatRupiah(transaksi.getDiskon()));
            lblPajakVal.setText("+ " + formatRupiah(transaksi.getPajak()));
            lblTotalVal.setText(formatRupiah(transaksi.getTotal()));

            try {
                double bayar = Double.parseDouble(txtBayar.getText().trim());
                double kembali = bayar - transaksi.getTotal();
                lblKembalian.setText(kembali >= 0 ? formatRupiah(kembali) : "Kurang " + formatRupiah(Math.abs(kembali)));
            } catch (Exception ex) {
                lblKembalian.setText("Rp 0");
            }
        };

        txtBayar.textProperty().addListener((obs, oldVal, newVal) -> refreshCart.run());

        // Event Add Item
        Runnable doAddItem = () -> {
            Menu selected = tblMenu.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            if (selected.getStok() <= 0) {
                showAlert(Alert.AlertType.WARNING, "Stok Habis", "Stok menu [" + selected.getNama() + "] sudah habis!");
                return;
            }

            try {
                Menu m = menuService.getMenuById(selected.getId());
                transaksi.tambahItem(m, 1);
                refreshCart.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };

        btnPesanMenu.setOnAction(e -> doAddItem.run());
        tblMenu.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) doAddItem.run();
        });

        // Cart Actions
        btnPlus1.setOnAction(e -> {
            DetailTransaksi sel = tblCart.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            transaksi.ubahQtyItem(sel.getMenuId(), 1);
            refreshCart.run();
        });

        btnMinus1.setOnAction(e -> {
            DetailTransaksi sel = tblCart.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            transaksi.ubahQtyItem(sel.getMenuId(), -1);
            refreshCart.run();
        });

        btnHapus.setOnAction(e -> {
            DetailTransaksi sel = tblCart.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            transaksi.hapusItem(sel.getMenuId());
            refreshCart.run();
        });

        btnKosongkan.setOnAction(e -> {
            transaksi.getItems().clear();
            appliedPromo[0] = null;
            txtKodePromo.clear();
            txtBayar.clear();
            refreshCart.run();
        });

        // Template Uang Cepat
        btnUangPas.setOnAction(e -> txtBayar.setText(String.valueOf((long) Math.ceil(transaksi.getTotal()))));
        btnUang20k.setOnAction(e -> txtBayar.setText("20000"));
        btnUang50k.setOnAction(e -> txtBayar.setText("50000"));
        btnUang100k.setOnAction(e -> txtBayar.setText("100000"));
        btnUang200k.setOnAction(e -> txtBayar.setText("200000"));

        btnTerapkanPromo.setOnAction(e -> {
            String kode = txtKodePromo.getText().trim();
            PromoService.PromoValidationResult res = promoService.validasiPromo(kode, transaksi.getSubtotal());
            if (res.valid) {
                appliedPromo[0] = res.promo;
                showAlert(Alert.AlertType.INFORMATION, "Promo Berhasil", res.pesan);
            } else {
                appliedPromo[0] = null;
                showAlert(Alert.AlertType.WARNING, "Peringatan Promo", res.pesan);
            }
            refreshCart.run();
        });

        btnBayar.setOnAction(e -> {
            if (transaksi.getItems().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Peringatan", "Keranjang pesanan masih kosong!");
                return;
            }

            double bayar;
            try {
                bayar = Double.parseDouble(txtBayar.getText().trim());
            } catch (Exception ex) {
                showAlert(Alert.AlertType.WARNING, "Validasi Bayar", "Silakan pilih nominal uang atau ketik nominal bayar!");
                return;
            }

            if (bayar < transaksi.getTotal()) {
                showAlert(Alert.AlertType.WARNING, "Uang Kurang",
                        "Uang pembayaran kurang dari total tagihan!\nTotal: " + formatRupiah(transaksi.getTotal()));
                return;
            }

            double kembalian = bayar - transaksi.getTotal();
            boolean confirm = showConfirm("Konfirmasi Pembayaran",
                    "Konfirmasi Pembayaran Transaksi:\n\n" +
                            "• No Faktur   : " + transaksi.getNoFaktur() + "\n" +
                            "• Kasir       : " + transaksi.getKasirNama() + "\n" +
                            "• Total Bayar : " + formatRupiah(transaksi.getTotal()) + "\n" +
                            "• Tunai       : " + formatRupiah(bayar) + "\n" +
                            "• Kembalian   : " + formatRupiah(kembalian) + "\n\n" +
                            "Proses transaksi dan cetak struk sekarang?");

            if (!confirm) return;

            transaksi.setBayar(bayar);
            transaksi.setKembalian(kembalian);

            try {
                boolean sukses = transaksiService.prosesTransaksi(transaksi);
                if (sukses) {
                    File fileStruk = transaksiService.cetakStruk(transaksi);
                    PDFUtil.bukaFile(fileStruk);

                    showAlert(Alert.AlertType.INFORMATION, "Pembayaran Sukses",
                            "Transaksi Berhasil Diproses!\n" +
                                    "Struk resmi telah dicetak dan disimpan di:\n" + fileStruk.getAbsolutePath());

                    // Reset form
                    transaksi.setNoFaktur(transaksiService.generateNoFaktur());
                    lblFaktur.setText("Faktur: " + transaksi.getNoFaktur());
                    transaksi.getItems().clear();
                    appliedPromo[0] = null;
                    txtKodePromo.clear();
                    txtBayar.clear();
                    refreshCart.run();
                    loadMenuData.run();
                }
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error Transaksi", "Gagal memproses transaksi: " + ex.getMessage());
            }
        });

        return panel;
    }

    private HBox buatBarisLabel(String label, Label valLabel) {
        HBox pnl = new HBox();
        pnl.setAlignment(Pos.CENTER_LEFT);
        Label l = new Label(label);
        l.setFont(FONT_REGULAR);
        l.setTextFill(COLOR_TEXT_MAIN);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        pnl.getChildren().addAll(l, spacer, valLabel);
        return pnl;
    }

    // =========================================================================
    // PANEL 2: KELOLA MENU & STOK (FR-01, FR-03)
    // =========================================================================
    private Region buatPanelMenu() {
        VBox panel = new VBox(12);
        styleFintechCard(panel, 16, 16);

        ObservableList<Menu> menuList = FXCollections.observableArrayList();
        TableView<Menu> table = new TableView<>(menuList);
        styleFintechTable(table);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Menu, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        colId.setPrefWidth(50);

        TableColumn<Menu, String> colKode = new TableColumn<>("Kode");
        colKode.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getKode()));
        colKode.setPrefWidth(90);

        TableColumn<Menu, String> colNama = new TableColumn<>("Nama Menu");
        colNama.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getNama()));
        colNama.setPrefWidth(220);

        TableColumn<Menu, String> colKat = new TableColumn<>("Kategori");
        colKat.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getKategori()));
        colKat.setPrefWidth(110);

        TableColumn<Menu, String> colHarga = new TableColumn<>("Harga");
        colHarga.setCellValueFactory(c -> new ReadOnlyStringWrapper(formatRupiah(c.getValue().getHarga())));
        colHarga.setPrefWidth(120);

        TableColumn<Menu, Integer> colStok = new TableColumn<>("Stok");
        colStok.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getStok()));
        colStok.setPrefWidth(80);

        TableColumn<Menu, String> colKet = new TableColumn<>("Keterangan Rasa/Sajian");
        colKet.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getJenisPenyajian()));
        colKet.setPrefWidth(220);

        table.getColumns().addAll(List.of(colId, colKode, colNama, colKat, colHarga, colStok, colKet));

        final String[] currentFilter = {"Semua"};

        Runnable loadMenu = () -> {
            try {
                menuList.setAll(menuService.getDaftarMenu(currentFilter[0]));
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat daftar menu: " + ex.getMessage());
            }
        };
        loadMenu.run();

        // Header and Toolbar
        HBox top = new HBox(10);
        top.setAlignment(Pos.BOTTOM_LEFT);

        VBox pnlHeaderLeft = new VBox(8);
        Label lblTitle = new Label("Katalog Menu & Manajemen Stok Real-Time (FR-01, FR-03)");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setTextFill(COLOR_TEXT_MAIN);

        HBox pnlFilters = new HBox(6);
        ContrastButton tabAll = createFilterPill("Semua", true);
        ContrastButton tabKopi = createFilterPill("Kopi", false);
        ContrastButton tabNonKopi = createFilterPill("Non-Kopi", false);
        ContrastButton tabMakanan = createFilterPill("Makanan", false);

        ContrastButton[] filterButtons = {tabAll, tabKopi, tabNonKopi, tabMakanan};
        String[] categories = {"Semua", "Kopi", "Non-Kopi", "Makanan"};

        for (int i = 0; i < filterButtons.length; i++) {
            final int idx = i;
            filterButtons[i].setOnAction(e -> {
                currentFilter[0] = categories[idx];
                for (ContrastButton b : filterButtons) {
                    b.setColors(HEX_CANVAS, HEX_TEXT_MAIN);
                }
                filterButtons[idx].setColors(HEX_PRIMARY_DARK, "#FFFFFF");
                loadMenu.run();
            });
            pnlFilters.getChildren().add(filterButtons[i]);
        }
        pnlHeaderLeft.getChildren().addAll(lblTitle, pnlFilters);

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        HBox toolbar = new HBox(8);
        toolbar.setAlignment(Pos.CENTER_RIGHT);

        ContrastButton btnTambah = new ContrastButton("+ Tambah Menu", HEX_SUCCESS, "#FFFFFF");
        ContrastButton btnEdit = new ContrastButton("Edit Menu", HEX_SLATE_BTN, "#FFFFFF");
        ContrastButton btnStok = new ContrastButton("Update Stok", HEX_PRIMARY_DARK, "#FFFFFF");
        ContrastButton btnHapus = new ContrastButton("Hapus Menu", HEX_DANGER, "#FFFFFF");
        ContrastButton btnRefresh = new ContrastButton("Refresh", HEX_SLATE_BTN, "#FFFFFF");

        toolbar.getChildren().addAll(btnTambah, btnEdit, btnStok, btnHapus, btnRefresh);
        top.getChildren().addAll(pnlHeaderLeft, topSpacer, toolbar);

        btnRefresh.setOnAction(e -> loadMenu.run());
        btnTambah.setOnAction(e -> tampilkanFormMenu(null, loadMenu));

        btnEdit.setOnAction(e -> {
            Menu sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert(Alert.AlertType.WARNING, "Perhatian", "Pilih menu pada tabel yang ingin diedit!");
                return;
            }
            try {
                Menu m = menuService.getMenuById(sel.getId());
                if (m != null) tampilkanFormMenu(m, loadMenu);
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal mengambil data menu: " + ex.getMessage());
            }
        });

        btnStok.setOnAction(e -> {
            Menu sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert(Alert.AlertType.WARNING, "Perhatian", "Pilih menu pada tabel yang ingin diperbarui stoknya!");
                return;
            }

            Optional<String> result = showInputDialog("Update Stok",
                    "Masukkan jumlah stok baru untuk [" + sel.getNama() + "]:",
                    String.valueOf(sel.getStok()));

            if (result.isPresent() && !result.get().trim().isEmpty()) {
                try {
                    int stokBaru = Integer.parseInt(result.get().trim());
                    if (stokBaru < 0) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Stok tidak boleh negatif!");
                        return;
                    }
                    menuService.updateStok(sel.getId(), stokBaru);
                    showAlert(Alert.AlertType.INFORMATION, "Sukses",
                            "Stok " + sel.getNama() + " berhasil diperbarui menjadi " + stokBaru + " unit.");
                    loadMenu.run();
                } catch (NumberFormatException nfe) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Format angka stok tidak valid!");
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Gagal update stok: " + ex.getMessage());
                }
            }
        });

        btnHapus.setOnAction(e -> {
            Menu sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert(Alert.AlertType.WARNING, "Perhatian", "Pilih menu pada tabel yang ingin dihapus!");
                return;
            }

            if (showConfirm("Konfirmasi Hapus", "Yakin ingin menghapus menu '" + sel.getNama() + "'?")) {
                try {
                    menuService.hapusMenu(sel.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Menu berhasil dihapus.");
                    loadMenu.run();
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Gagal menghapus menu: " + ex.getMessage());
                }
            }
        });

        panel.getChildren().addAll(top, table);
        return panel;
    }

    private void tampilkanFormMenu(Menu editMenu, Runnable onSaved) {
        boolean isEdit = editMenu != null;
        Stage dialog = createModalStage(isEdit ? "Edit Data Menu" : "Tambah Menu Baru", 440, 360);

        BorderPane root = new BorderPane();
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white;");

        ColumnConstraints c1 = new ColumnConstraints(120);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(c1, c2);

        TextField txtKode = createPillTextField(isEdit ? editMenu.getKode() : "CF" + (System.currentTimeMillis() % 1000), "Kode unik menu", false);
        TextField txtNama = createPillTextField(isEdit ? editMenu.getNama() : "", "Nama menu...", false);
        ComboBox<String> cmbKategori = new ComboBox<>(FXCollections.observableArrayList("Kopi", "Non-Kopi", "Makanan"));
        cmbKategori.setMaxWidth(Double.MAX_VALUE);
        cmbKategori.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: " + HEX_BORDER + "; -fx-background-color: white;");
        cmbKategori.setValue(isEdit ? editMenu.getKategori() : "Kopi");
        TextField txtHarga = createPillTextField(isEdit ? String.valueOf((long) editMenu.getHarga()) : "", "Contoh: 25000", false);
        TextField txtStok = createPillTextField(isEdit ? String.valueOf(editMenu.getStok()) : "50", "Jumlah stok", false);

        form.add(new Label("Kode Menu:"), 0, 0);
        form.add(txtKode, 1, 0);
        form.add(new Label("Nama Menu:"), 0, 1);
        form.add(txtNama, 1, 1);
        form.add(new Label("Kategori:"), 0, 2);
        form.add(cmbKategori, 1, 2);
        form.add(new Label("Harga (Rp):"), 0, 3);
        form.add(txtHarga, 1, 3);
        form.add(new Label("Stok Awal:"), 0, 4);
        form.add(txtStok, 1, 4);

        HBox pnlBtn = new HBox(8);
        pnlBtn.setAlignment(Pos.CENTER_RIGHT);
        pnlBtn.setPadding(new Insets(12, 20, 12, 20));
        pnlBtn.setStyle("-fx-background-color: " + HEX_CANVAS + ";");

        ContrastButton btnBatal = new ContrastButton("Batal", HEX_SLATE_BTN, "#FFFFFF");
        btnBatal.setOnAction(e -> dialog.close());

        ContrastButton btnSimpan = new ContrastButton("Simpan Data", HEX_SUCCESS, "#FFFFFF");
        btnSimpan.setOnAction(e -> {
            try {
                String kode = txtKode.getText().trim();
                String nama = txtNama.getText().trim();
                String kategori = cmbKategori.getValue();
                double harga = Double.parseDouble(txtHarga.getText().trim());
                int stok = Integer.parseInt(txtStok.getText().trim());

                if (nama.isEmpty() || kode.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Nama dan kode menu tidak boleh kosong!");
                    return;
                }

                Menu m = new Menu(isEdit ? editMenu.getId() : 0, kode, nama, kategori, harga, stok);
                menuService.simpanMenu(m);

                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data menu berhasil disimpan!");
                dialog.close();
                onSaved.run();
            } catch (NumberFormatException nfe) {
                showAlert(Alert.AlertType.ERROR, "Error", "Format angka harga atau stok tidak valid!");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal menyimpan menu: " + ex.getMessage());
            }
        });

        pnlBtn.getChildren().addAll(btnBatal, btnSimpan);
        root.setCenter(form);
        root.setBottom(pnlBtn);

        dialog.setScene(new Scene(root));
        dialog.showAndWait();
    }

    // =========================================================================
    // PANEL 3: KELOLA KASIR (FR-02)
    // =========================================================================
    private Region buatPanelKasir() {
        VBox panel = new VBox(12);
        styleFintechCard(panel, 16, 16);

        ObservableList<Pengguna> kasirList = FXCollections.observableArrayList();
        TableView<Pengguna> table = new TableView<>(kasirList);
        styleFintechTable(table);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Pengguna, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        colId.setPrefWidth(60);

        TableColumn<Pengguna, String> colUser = new TableColumn<>("Username");
        colUser.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getUsername()));
        colUser.setPrefWidth(160);

        TableColumn<Pengguna, String> colNama = new TableColumn<>("Nama Kasir");
        colNama.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getNama()));
        colNama.setPrefWidth(220);

        TableColumn<Pengguna, String> colStatus = new TableColumn<>("Status Akun");
        colStatus.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getStatus()));
        colStatus.setPrefWidth(130);

        TableColumn<Pengguna, String> colAkses = new TableColumn<>("Hak Akses");
        colAkses.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getDeskripsiAkses()));
        colAkses.setPrefWidth(280);

        table.getColumns().addAll(List.of(colId, colUser, colNama, colStatus, colAkses));

        Runnable loadKasir = () -> {
            try {
                kasirList.setAll(authService.getDaftarKasir());
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat kasir: " + ex.getMessage());
            }
        };
        loadKasir.run();

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label("Manajemen Akun Kasir Operasional (FR-02)");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setTextFill(COLOR_TEXT_MAIN);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox toolbar = new HBox(8);
        ContrastButton btnTambah = new ContrastButton("+ Tambah Kasir Baru", HEX_SUCCESS, "#FFFFFF");
        ContrastButton btnToggleStatus = new ContrastButton("Toggle Status (Aktif/Nonaktif)", HEX_PRIMARY_DARK, "#FFFFFF");
        ContrastButton btnResetPwd = new ContrastButton("Reset Password", HEX_SLATE_BTN, "#FFFFFF");
        ContrastButton btnRefresh = new ContrastButton("Refresh", HEX_SLATE_BTN, "#FFFFFF");

        toolbar.getChildren().addAll(btnTambah, btnToggleStatus, btnResetPwd, btnRefresh);
        top.getChildren().addAll(lblTitle, spacer, toolbar);

        btnRefresh.setOnAction(e -> loadKasir.run());

        btnTambah.setOnAction(e -> {
            Stage dialog = createModalStage("Tambah Kasir Baru", 400, 270);

            BorderPane root = new BorderPane();
            GridPane form = new GridPane();
            form.setHgap(12);
            form.setVgap(12);
            form.setPadding(new Insets(20));
            form.setStyle("-fx-background-color: white;");

            ColumnConstraints c1 = new ColumnConstraints(115);
            ColumnConstraints c2 = new ColumnConstraints();
            c2.setHgrow(Priority.ALWAYS);
            form.getColumnConstraints().addAll(c1, c2);

            TextField txtUsername = createPillTextField("", "Username kasir...", false);
            PasswordField txtPassword = new PasswordField();
            txtPassword.setFont(FONT_REGULAR);
            txtPassword.setPromptText("Password kasir...");
            applyFieldStyle(txtPassword);
            TextField txtNama = createPillTextField("", "Nama lengkap...", false);

            form.add(new Label("Username:"), 0, 0);
            form.add(txtUsername, 1, 0);
            form.add(new Label("Password:"), 0, 1);
            form.add(txtPassword, 1, 1);
            form.add(new Label("Nama Lengkap:"), 0, 2);
            form.add(txtNama, 1, 2);

            HBox pnlBtn = new HBox(8);
            pnlBtn.setAlignment(Pos.CENTER_RIGHT);
            pnlBtn.setPadding(new Insets(12, 20, 12, 20));
            pnlBtn.setStyle("-fx-background-color: " + HEX_CANVAS + ";");

            ContrastButton btnBatal = new ContrastButton("Batal", HEX_SLATE_BTN, "#FFFFFF");
            btnBatal.setOnAction(ev -> dialog.close());

            ContrastButton btnSimpan = new ContrastButton("Simpan Akun", HEX_SUCCESS, "#FFFFFF");
            btnSimpan.setOnAction(ev -> {
                String u = txtUsername.getText().trim();
                String p = txtPassword.getText().trim();
                String n = txtNama.getText().trim();

                if (u.isEmpty() || p.isEmpty() || n.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Semua field wajib diisi!");
                    return;
                }

                try {
                    Pengguna baru = new Kasir(0, u, p, n, "AKTIF");
                    authService.simpanKasir(baru);
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Akun kasir berhasil didaftarkan!");
                    dialog.close();
                    loadKasir.run();
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuat kasir: " + ex.getMessage());
                }
            });

            pnlBtn.getChildren().addAll(btnBatal, btnSimpan);
            root.setCenter(form);
            root.setBottom(pnlBtn);

            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        });

        btnToggleStatus.setOnAction(e -> {
            Pengguna sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert(Alert.AlertType.WARNING, "Perhatian", "Pilih kasir yang ingin diubah statusnya!");
                return;
            }
            String statusTarget = "AKTIF".equalsIgnoreCase(sel.getStatus()) ? "NONAKTIF" : "AKTIF";

            if (showConfirm("Ubah Status", "Ubah status akun '" + sel.getNama() + "' menjadi " + statusTarget + "?")) {
                try {
                    authService.ubahStatusKasir(sel.getId(), statusTarget);
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Status berhasil diperbarui.");
                    loadKasir.run();
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Gagal ubah status: " + ex.getMessage());
                }
            }
        });

        btnResetPwd.setOnAction(e -> {
            Pengguna sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert(Alert.AlertType.WARNING, "Perhatian", "Pilih kasir yang ingin direset passwordnya!");
                return;
            }

            Optional<String> pwdBaru = showInputDialog("Reset Password",
                    "Masukkan password baru untuk kasir [" + sel.getUsername() + "]:", "");

            if (pwdBaru.isPresent() && !pwdBaru.get().trim().isEmpty()) {
                try {
                    Pengguna u = new Kasir(sel.getId(), sel.getUsername(), pwdBaru.get().trim(), sel.getNama(), "AKTIF");
                    authService.simpanKasir(u);
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Password berhasil diperbarui!");
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Gagal reset password: " + ex.getMessage());
                }
            }
        });

        panel.getChildren().addAll(top, table);
        return panel;
    }

    // =========================================================================
    // PANEL 4: KELOLA PROMO (FR-07)
    // =========================================================================
    private Region buatPanelPromo() {
        VBox panel = new VBox(12);
        styleFintechCard(panel, 16, 16);

        ObservableList<Promo> promoList = FXCollections.observableArrayList();
        TableView<Promo> table = new TableView<>(promoList);
        styleFintechTable(table);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Promo, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        colId.setPrefWidth(45);

        TableColumn<Promo, String> colKode = new TableColumn<>("Kode Promo");
        colKode.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getKode()));
        colKode.setPrefWidth(120);

        TableColumn<Promo, String> colNama = new TableColumn<>("Nama Promo");
        colNama.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getNamaPromo()));
        colNama.setPrefWidth(180);

        TableColumn<Promo, String> colTipe = new TableColumn<>("Tipe");
        colTipe.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getTipeDiskon()));
        colTipe.setPrefWidth(95);

        TableColumn<Promo, String> colNilai = new TableColumn<>("Nilai Diskon");
        colNilai.setCellValueFactory(c -> {
            Promo p = c.getValue();
            String val = "PERSEN".equalsIgnoreCase(p.getTipeDiskon())
                    ? ((int) p.getNilaiDiskon() + "%")
                    : formatRupiah(p.getNilaiDiskon());
            return new ReadOnlyStringWrapper(val);
        });
        colNilai.setPrefWidth(115);

        TableColumn<Promo, String> colMin = new TableColumn<>("Min Belanja");
        colMin.setCellValueFactory(c -> new ReadOnlyStringWrapper(formatRupiah(c.getValue().getMinBelanja())));
        colMin.setPrefWidth(120);

        TableColumn<Promo, String> colMulai = new TableColumn<>("Tgl Mulai");
        colMulai.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.valueOf(c.getValue().getTglMulai())));
        colMulai.setPrefWidth(105);

        TableColumn<Promo, String> colSelesai = new TableColumn<>("Tgl Selesai");
        colSelesai.setCellValueFactory(c -> new ReadOnlyStringWrapper(String.valueOf(c.getValue().getTglSelesai())));
        colSelesai.setPrefWidth(105);

        TableColumn<Promo, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getStatus()));
        colStatus.setPrefWidth(90);

        table.getColumns().addAll(List.of(colId, colKode, colNama, colTipe, colNilai, colMin, colMulai, colSelesai, colStatus));

        Runnable loadPromo = () -> {
            try {
                promoList.setAll(promoService.getDaftarPromo());
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat promo: " + ex.getMessage());
            }
        };
        loadPromo.run();

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label("Manajemen Kode Promo & Diskon Penjualan (FR-07)");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setTextFill(COLOR_TEXT_MAIN);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox toolbar = new HBox(8);
        ContrastButton btnTambah = new ContrastButton("+ Tambah Promo Baru", HEX_SUCCESS, "#FFFFFF");
        ContrastButton btnHapus = new ContrastButton("Hapus Promo", HEX_DANGER, "#FFFFFF");
        ContrastButton btnRefresh = new ContrastButton("Refresh", HEX_SLATE_BTN, "#FFFFFF");

        toolbar.getChildren().addAll(btnTambah, btnHapus, btnRefresh);
        top.getChildren().addAll(lblTitle, spacer, toolbar);

        btnRefresh.setOnAction(e -> loadPromo.run());

        btnTambah.setOnAction(e -> {
            Stage dialog = createModalStage("Tambah Promo Baru", 440, 420);

            BorderPane root = new BorderPane();
            GridPane form = new GridPane();
            form.setHgap(12);
            form.setVgap(10);
            form.setPadding(new Insets(20));
            form.setStyle("-fx-background-color: white;");

            ColumnConstraints c1 = new ColumnConstraints(145);
            ColumnConstraints c2 = new ColumnConstraints();
            c2.setHgrow(Priority.ALWAYS);
            form.getColumnConstraints().addAll(c1, c2);

            TextField txtKode = createPillTextField("DISKON" + (int) (Math.random() * 90 + 10));
            TextField txtNama = createPillTextField("", "Nama promo...", false);
            ComboBox<String> cmbTipe = new ComboBox<>(FXCollections.observableArrayList("PERSEN", "NOMINAL"));
            cmbTipe.setMaxWidth(Double.MAX_VALUE);
            cmbTipe.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: " + HEX_BORDER + "; -fx-background-color: white;");
            cmbTipe.setValue("PERSEN");
            TextField txtNilai = createPillTextField("10");
            TextField txtMin = createPillTextField("50000");
            TextField txtMulai = createPillTextField(LocalDate.now().toString());
            TextField txtSelesai = createPillTextField(LocalDate.now().plusMonths(1).toString());

            form.add(new Label("Kode Promo:"), 0, 0);
            form.add(txtKode, 1, 0);
            form.add(new Label("Nama Promo:"), 0, 1);
            form.add(txtNama, 1, 1);
            form.add(new Label("Tipe Diskon:"), 0, 2);
            form.add(cmbTipe, 1, 2);
            form.add(new Label("Besaran Diskon:"), 0, 3);
            form.add(txtNilai, 1, 3);
            form.add(new Label("Min Belanja (Rp):"), 0, 4);
            form.add(txtMin, 1, 4);
            form.add(new Label("Tgl Mulai (YYYY-MM-DD):"), 0, 5);
            form.add(txtMulai, 1, 5);
            form.add(new Label("Tgl Selesai (YYYY-MM-DD):"), 0, 6);
            form.add(txtSelesai, 1, 6);

            HBox pnlBtn = new HBox(8);
            pnlBtn.setAlignment(Pos.CENTER_RIGHT);
            pnlBtn.setPadding(new Insets(12, 20, 12, 20));
            pnlBtn.setStyle("-fx-background-color: " + HEX_CANVAS + ";");

            ContrastButton btnBatal = new ContrastButton("Batal", HEX_SLATE_BTN, "#FFFFFF");
            btnBatal.setOnAction(ev -> dialog.close());

            ContrastButton btnSimpan = new ContrastButton("Simpan Promo", HEX_SUCCESS, "#FFFFFF");
            btnSimpan.setOnAction(ev -> {
                try {
                    String kode = txtKode.getText().trim().toUpperCase();
                    String nama = txtNama.getText().trim();
                    String tipe = cmbTipe.getValue();
                    double nilai = Double.parseDouble(txtNilai.getText().trim());
                    double min = Double.parseDouble(txtMin.getText().trim());
                    LocalDate tglMulai = LocalDate.parse(txtMulai.getText().trim());
                    LocalDate tglSelesai = LocalDate.parse(txtSelesai.getText().trim());

                    if (kode.isEmpty() || nama.isEmpty()) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Kode dan Nama promo tidak boleh kosong!");
                        return;
                    }

                    Promo p = new Promo(0, kode, nama, tipe, nilai, min, tglMulai, tglSelesai, "AKTIF");
                    promoService.simpanPromo(p);
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Promo berhasil ditambahkan!");
                    dialog.close();
                    loadPromo.run();
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Gagal simpan promo: " + ex.getMessage());
                }
            });

            pnlBtn.getChildren().addAll(btnBatal, btnSimpan);
            root.setCenter(form);
            root.setBottom(pnlBtn);

            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        });

        btnHapus.setOnAction(e -> {
            Promo sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert(Alert.AlertType.WARNING, "Perhatian", "Pilih promo yang ingin dihapus!");
                return;
            }

            if (showConfirm("Konfirmasi Hapus", "Yakin ingin menghapus promo '" + sel.getKode() + "'?")) {
                try {
                    promoService.hapusPromo(sel.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Promo berhasil dihapus.");
                    loadPromo.run();
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Gagal menghapus promo: " + ex.getMessage());
                }
            }
        });

        panel.getChildren().addAll(top, table);
        return panel;
    }

    // =========================================================================
    // PANEL 5: LAPORAN PENJUALAN (FR-04)
    // =========================================================================
    private Region buatPanelLaporan() {
        VBox panel = new VBox(14);
        panel.setStyle("-fx-background-color: " + HEX_CANVAS + ";");

        final String[] periode = {"Harian"};

        HBox filterCard = new HBox(10);
        filterCard.setAlignment(Pos.CENTER_LEFT);
        styleFintechCard(filterCard, 12, 16);

        Label lblTitle = new Label("Laporan Eksekutif Penjualan & Analitik Menu (FR-04)");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setTextFill(COLOR_TEXT_MAIN);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox pnlPills = new HBox(6);
        ContrastButton tabHarian = createFilterPill("Harian (Hari Ini)", true);
        ContrastButton tabMingguan = createFilterPill("Mingguan (Minggu Ini)", false);
        ContrastButton tabBulanan = createFilterPill("Bulanan (Bulan Ini)", false);

        ContrastButton[] periodBtns = {tabHarian, tabMingguan, tabBulanan};
        String[] periodKeys = {"Harian", "Mingguan", "Bulanan"};
        pnlPills.getChildren().addAll(tabHarian, tabMingguan, tabBulanan);
        filterCard.getChildren().addAll(lblTitle, spacer, pnlPills);

        // 3 KPI Cards
        HBox pnlKpis = new HBox(14);

        Label lblValOmset = new Label("Rp 0");
        lblValOmset.setFont(FONT_HERO_NUM);
        lblValOmset.setTextFill(COLOR_SUCCESS);

        Label lblValTrx = new Label("0 Transaksi");
        lblValTrx.setFont(FONT_HERO_NUM);
        lblValTrx.setTextFill(COLOR_PRIMARY_DARK);

        Label lblValAvg = new Label("Rp 0");
        lblValAvg.setFont(FONT_HERO_NUM);
        lblValAvg.setTextFill(COLOR_SLATE_BTN);

        VBox kpi1 = buatKpiCard("Total Omset Penjualan", lblValOmset, "Akumulasi pendapatan kotor");
        VBox kpi2 = buatKpiCard("Jumlah Transaksi", lblValTrx, "Total pesanan kasir sukses");
        VBox kpi3 = buatKpiCard("Rata-Rata per Transaksi", lblValAvg, "Nilai rata-rata per transaksi");
        HBox.setHgrow(kpi1, Priority.ALWAYS);
        HBox.setHgrow(kpi2, Priority.ALWAYS);
        HBox.setHgrow(kpi3, Priority.ALWAYS);
        pnlKpis.getChildren().addAll(kpi1, kpi2, kpi3);

        // 5 Menu Terlaris Table
        VBox pnlBestSellers = new VBox(10);
        styleFintechCard(pnlBestSellers, 14, 16);
        VBox.setVgrow(pnlBestSellers, Priority.ALWAYS);

        Label lblTableTitle = new Label("Top 5 Menu Terlaris (Best Sellers)");
        lblTableTitle.setFont(FONT_TITLE);
        lblTableTitle.setTextFill(COLOR_TEXT_MAIN);

        ObservableList<String[]> topRows = FXCollections.observableArrayList();
        TableView<String[]> tableTop = new TableView<>(topRows);
        styleFintechTable(tableTop);
        VBox.setVgrow(tableTop, Priority.ALWAYS);

        TableColumn<String[], String> colRank = new TableColumn<>("Peringkat");
        colRank.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue()[0]));
        colRank.setPrefWidth(120);

        TableColumn<String[], String> colMenuName = new TableColumn<>("Nama Menu");
        colMenuName.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue()[1]));
        colMenuName.setPrefWidth(280);

        TableColumn<String[], String> colSold = new TableColumn<>("Total Terjual (Cup/Porsi)");
        colSold.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue()[2]));
        colSold.setPrefWidth(200);

        TableColumn<String[], String> colSales = new TableColumn<>("Total Nilai Penjualan");
        colSales.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue()[3]));
        colSales.setPrefWidth(220);

        tableTop.getColumns().addAll(List.of(colRank, colMenuName, colSold, colSales));
        pnlBestSellers.getChildren().addAll(lblTableTitle, tableTop);

        Runnable refreshLaporan = () -> {
            try {
                LaporanService.LaporanData data = laporanService.getLaporan(periode[0]);
                lblValOmset.setText(formatRupiah(data.totalPendapatan));
                lblValTrx.setText(data.totalTransaksi + " Transaksi");

                double avg = data.totalTransaksi > 0 ? (data.totalPendapatan / data.totalTransaksi) : 0;
                lblValAvg.setText(formatRupiah(avg));

                topRows.clear();
                String[] badges = {"Juara 1", "Juara 2", "Juara 3", "#4", "#5"};
                int rank = 0;
                for (Object[] item : data.menuTerlaris) {
                    String badge = rank < badges.length ? badges[rank] : ("#" + (rank + 1));
                    topRows.add(new String[]{
                            badge,
                            String.valueOf(item[0]),
                            item[1] + " porsi",
                            formatRupiah((double) item[2])
                    });
                    rank++;
                }
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat laporan: " + ex.getMessage());
            }
        };
        this.refreshLaporanHook = refreshLaporan;

        for (int i = 0; i < periodBtns.length; i++) {
            final int idx = i;
            periodBtns[i].setOnAction(e -> {
                periode[0] = periodKeys[idx];
                for (ContrastButton b : periodBtns) {
                    b.setColors(HEX_CANVAS, HEX_TEXT_MAIN);
                }
                periodBtns[idx].setColors(HEX_PRIMARY_DARK, "#FFFFFF");
                refreshLaporan.run();
            });
        }

        refreshLaporan.run();
        panel.getChildren().addAll(filterCard, pnlKpis, pnlBestSellers);
        return panel;
    }

    private VBox buatKpiCard(String title, Label valLabel, String sub) {
        VBox card = new VBox(6);
        styleFintechCard(card, 16, 18);

        Label lblT = new Label(title);
        lblT.setFont(FONT_BOLD);
        lblT.setTextFill(COLOR_TEXT_MUTED);

        Label lblS = new Label(sub);
        lblS.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        lblS.setTextFill(COLOR_TEXT_MUTED);

        card.getChildren().addAll(lblT, valLabel, lblS);
        return card;
    }

    // =========================================================================
    // PANEL 6: RIWAYAT TRANSAKSI & RINCIAN BARANG TERJUAL (MASTER-DETAIL)
    // =========================================================================
    private Region buatPanelRiwayat() {
        BorderPane panel = new BorderPane();
        panel.setStyle("-fx-background-color: " + HEX_CANVAS + ";");

        // --- BAGIAN ATAS (MASTER): TABEL DAFTAR TRANSAKSI ---
        VBox pnlMaster = new VBox(8);
        styleFintechCard(pnlMaster, 14, 16);

        HBox topMaster = new HBox(10);
        topMaster.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label("Riwayat Seluruh Transaksi Penjualan");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setTextFill(COLOR_TEXT_MAIN);

        Region masterSpacer = new Region();
        HBox.setHgrow(masterSpacer, Priority.ALWAYS);

        ContrastButton btnRefresh = new ContrastButton("Refresh Riwayat", HEX_SLATE_BTN, "#FFFFFF");
        topMaster.getChildren().addAll(lblTitle, masterSpacer, btnRefresh);

        ObservableList<Transaksi> trxList = FXCollections.observableArrayList();
        TableView<Transaksi> tblTrx = new TableView<>(trxList);
        styleFintechTable(tblTrx);
        VBox.setVgrow(tblTrx, Priority.ALWAYS);

        TableColumn<Transaksi, Integer> colTrxId = new TableColumn<>("ID");
        colTrxId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        colTrxId.setPrefWidth(45);

        TableColumn<Transaksi, String> colFaktur = new TableColumn<>("No Faktur");
        colFaktur.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getNoFaktur()));
        colFaktur.setPrefWidth(155);

        TableColumn<Transaksi, String> colKasir = new TableColumn<>("Kasir/Operator");
        colKasir.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getKasirNama()));
        colKasir.setPrefWidth(150);

        TableColumn<Transaksi, String> colSub = new TableColumn<>("Subtotal");
        colSub.setCellValueFactory(c -> new ReadOnlyStringWrapper(formatRupiah(c.getValue().getSubtotal())));
        colSub.setPrefWidth(105);

        TableColumn<Transaksi, String> colDisc = new TableColumn<>("Diskon");
        colDisc.setCellValueFactory(c -> new ReadOnlyStringWrapper(formatRupiah(c.getValue().getDiskon())));
        colDisc.setPrefWidth(95);

        TableColumn<Transaksi, String> colTax = new TableColumn<>("PPN (10%)");
        colTax.setCellValueFactory(c -> new ReadOnlyStringWrapper(formatRupiah(c.getValue().getPajak())));
        colTax.setPrefWidth(95);

        TableColumn<Transaksi, String> colTot = new TableColumn<>("Total Tagihan");
        colTot.setCellValueFactory(c -> new ReadOnlyStringWrapper(formatRupiah(c.getValue().getTotal())));
        colTot.setPrefWidth(115);

        TableColumn<Transaksi, String> colBayar = new TableColumn<>("Nominal Bayar");
        colBayar.setCellValueFactory(c -> new ReadOnlyStringWrapper(formatRupiah(c.getValue().getBayar())));
        colBayar.setPrefWidth(115);

        TableColumn<Transaksi, String> colKembali = new TableColumn<>("Kembalian");
        colKembali.setCellValueFactory(c -> new ReadOnlyStringWrapper(formatRupiah(c.getValue().getKembalian())));
        colKembali.setPrefWidth(105);

        tblTrx.getColumns().addAll(List.of(colTrxId, colFaktur, colKasir, colSub, colDisc, colTax, colTot, colBayar, colKembali));
        pnlMaster.getChildren().addAll(topMaster, tblTrx);

        // --- BAGIAN BAWAH (DETAIL): TABEL RINCIAN BARANG TERJUAL ---
        VBox pnlDetail = new VBox(8);
        styleFintechCard(pnlDetail, 14, 16);

        HBox topDetail = new HBox(10);
        topDetail.setAlignment(Pos.CENTER_LEFT);

        Label lblDetailTitle = new Label("Rincian Barang Terjual (Pilih transaksi pada tabel di atas)");
        lblDetailTitle.setFont(FONT_TITLE);
        lblDetailTitle.setTextFill(COLOR_PRIMARY_DARK);

        Region detailSpacer = new Region();
        HBox.setHgrow(detailSpacer, Priority.ALWAYS);

        ContrastButton btnBukaPDF = new ContrastButton("Buka Dokumen Struk (PDF)", HEX_PRIMARY_DARK, "#FFFFFF");
        topDetail.getChildren().addAll(lblDetailTitle, detailSpacer, btnBukaPDF);

        ObservableList<String[]> itemRows = FXCollections.observableArrayList();
        TableView<String[]> tblItems = new TableView<>(itemRows);
        styleFintechTable(tblItems);
        VBox.setVgrow(tblItems, Priority.ALWAYS);

        TableColumn<String[], String> colNo = new TableColumn<>("No");
        colNo.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue()[0]));
        colNo.setPrefWidth(55);

        TableColumn<String[], String> colItemNama = new TableColumn<>("Nama Menu / Minuman");
        colItemNama.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue()[1]));
        colItemNama.setPrefWidth(260);

        TableColumn<String[], String> colItemHarga = new TableColumn<>("Harga Satuan");
        colItemHarga.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue()[2]));
        colItemHarga.setPrefWidth(140);

        TableColumn<String[], String> colItemQty = new TableColumn<>("Qty Terjual");
        colItemQty.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue()[3]));
        colItemQty.setPrefWidth(130);

        TableColumn<String[], String> colItemSub = new TableColumn<>("Subtotal");
        colItemSub.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue()[4]));
        colItemSub.setPrefWidth(150);

        tblItems.getColumns().addAll(List.of(colNo, colItemNama, colItemHarga, colItemQty, colItemSub));
        pnlDetail.getChildren().addAll(topDetail, tblItems);

        final List<Transaksi>[] cache = new List[]{new ArrayList<>()};

        Runnable loadTrx = () -> {
            try {
                itemRows.clear();
                lblDetailTitle.setText("Rincian Barang Terjual (Pilih transaksi pada tabel di atas)");
                cache[0] = transaksiService.getRiwayatTransaksi();
                trxList.setAll(cache[0]);
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat riwayat: " + ex.getMessage());
            }
        };
        this.refreshRiwayatHook = loadTrx;
        loadTrx.run();

        // Event Klik Baris Transaksi -> Menampilkan Detail Barang Terjual
        tblTrx.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, t) -> {
            if (t != null) {
                lblDetailTitle.setText("Rincian Barang Terjual - No Faktur: " + t.getNoFaktur() + " (Kasir: " + t.getKasirNama() + ")");
                itemRows.clear();

                int no = 1;
                for (DetailTransaksi dt : t.getItems()) {
                    itemRows.add(new String[]{
                            String.valueOf(no++),
                            dt.getNamaMenu(),
                            formatRupiah(dt.getHarga()),
                            dt.getQty() + " cup/porsi",
                            formatRupiah(dt.getSubtotal())
                    });
                }

                if (t.getItems().isEmpty()) {
                    itemRows.add(new String[]{"-", "(Tidak ada rincian item)", "-", "-", "-"});
                }
            }
        });

        btnRefresh.setOnAction(e -> loadTrx.run());

        btnBukaPDF.setOnAction(e -> {
            Transaksi sel = tblTrx.getSelectionModel().getSelectedItem();
            if (sel == null) {
                showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih salah satu baris transaksi terlebih dahulu!");
                return;
            }
            File file = transaksiService.cetakStruk(sel);
            PDFUtil.bukaFile(file);
        });

        // Split Master-Detail Vertikal
        SplitPane splitRiwayat = new SplitPane(pnlMaster, pnlDetail);
        splitRiwayat.setOrientation(Orientation.VERTICAL);
        splitRiwayat.setDividerPositions(0.52);
        splitRiwayat.setStyle("-fx-background-color: " + HEX_CANVAS + "; -fx-box-border: transparent;");

        panel.setCenter(splitRiwayat);
        return panel;
    }

    // =========================================================================
    // HELPER DIALOG & STYLING UMUM (JAVAFX)
    // =========================================================================
    private Stage createModalStage(String title, double width, double height) {
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(title);
        dialog.setWidth(width);
        dialog.setHeight(height);
        dialog.setResizable(false);
        return dialog;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.initOwner(primaryStage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean showConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(primaryStage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> res = alert.showAndWait();
        return res.isPresent() && res.get() == ButtonType.OK;
    }

    private Optional<String> showInputDialog(String title, String message, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.initOwner(primaryStage);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        return dialog.showAndWait();
    }

    public static void styleFintechCard(Region pane, double padV, double padH) {
        pane.setPadding(new Insets(padV, padH, padV, padH));
        pane.setStyle(
                "-fx-background-color: " + HEX_CARD + ";" +
                        "-fx-border-color: " + HEX_BORDER + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 14;" +
                        "-fx-background-radius: 14;"
        );
    }

    public static void styleFintechTable(TableView<?> table) {
        table.setFixedCellSize(34);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setStyle(
                "-fx-font-family: 'Segoe UI';" +
                        "-fx-font-size: 13px;" +
                        "-fx-background-color: white;" +
                        "-fx-border-color: " + HEX_BORDER + ";" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-selection-bar: #D1FAE5;" +
                        "-fx-selection-bar-non-focused: #D1FAE5;" +
                        "-fx-selection-bar-text: " + HEX_TEXT_MAIN + ";"
        );
    }

    public static TextField createPillTextField(String text) {
        return createPillTextField(text, null, false);
    }

    public static TextField createPillTextField(String text, String placeholder, boolean showClear) {
        TextField tf = new TextField(text);
        tf.setFont(FONT_REGULAR);
        if (placeholder != null && !placeholder.isEmpty()) {
            tf.setPromptText(placeholder);
        }
        applyFieldStyle(tf);
        return tf;
    }

    public static void applyFieldStyle(TextInputControl field) {
        String normalStyle =
                "-fx-background-color: white;" +
                        "-fx-border-color: " + HEX_BORDER + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 5 10 5 10;";

        String focusedStyle =
                "-fx-background-color: white;" +
                        "-fx-border-color: " + HEX_SUCCESS + ";" +
                        "-fx-border-width: 1.5;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 5 10 5 10;";

        field.setStyle(normalStyle);
        field.focusedProperty().addListener((obs, wasFocused, isFocused) ->
                field.setStyle(isFocused ? focusedStyle : normalStyle)
        );
    }

    public static ContrastButton createFilterPill(String text, boolean active) {
        ContrastButton btn = new ContrastButton(
                text,
                active ? HEX_PRIMARY_DARK : HEX_CANVAS,
                active ? "#FFFFFF" : HEX_TEXT_MAIN
        );
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        btn.setPaddingCss("6 12 6 12");
        return btn;
    }
}