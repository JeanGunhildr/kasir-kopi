package ui;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.DetailTransaksi;
import model.Menu;
import model.Pengguna;
import model.Promo;
import model.Transaksi;
import service.AuthService;
import service.MenuService;
import service.PromoService;
import service.TransaksiService;
import util.PDFUtil;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Dashboard Antarmuka Kasir Operasional (POS) versi JavaFX.
 * Menangani transaksi pemesanan menu, kuantitas +1/-1, kode promo, template nominal uang,
 * konfirmasi pembayaran langsung, pencetakan struk PDF, dan riwayat transaksi dengan
 * rincian barang terjual (FR-05, FR-06, FR-08).
 */
public class KasirDashboard {

    // Palet Warna Fintech Dashboard (Hex & JavaFX Color)
    public static final String HEX_PRIMARY_DARK = "#0B3B2C"; // Deep Emerald Forest
    public static final String HEX_SUCCESS      = "#059669"; // Solid Mint Green
    public static final String HEX_SLATE_BTN    = "#334155"; // Slate Button
    public static final String HEX_DANGER       = "#DC2626"; // Solid Crimson Red
    public static final String HEX_AMBER        = "#D97706"; // Amber Warning

    public static final String HEX_CANVAS       = "#F1F5F9"; // Canvas Slate 100
    public static final String HEX_CARD         = "#FFFFFF"; // White Card Container
    public static final String HEX_BORDER       = "#CBD5E1"; // Border Slate 300

    public static final String HEX_TEXT_MAIN    = "#0F172A"; // Dark Text
    public static final String HEX_TEXT_MUTED   = "#64748B"; // Muted Text

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

    private final MenuService menuService = new MenuService();
    private final PromoService promoService = new PromoService();
    private final TransaksiService transaksiService = new TransaksiService();
    private final Pengguna currentUser;
    private Stage stage;

    public static String formatRupiah(double nilai) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("id-ID"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("Rp #,##0", symbols);
        return df.format(nilai);
    }

    public KasirDashboard(Pengguna user) {
        this.currentUser = user;
    }

    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        initUI();
    }

    private void initUI() {
        if (this.stage == null) {
            this.stage = new Stage();
        }

        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        double maxW = visualBounds.getWidth();
        double maxH = visualBounds.getHeight();

        stage.setTitle("Pacbuck Coffee - Terminal Kasir POS (" + currentUser.getNama() + ")");
        stage.setMinWidth(Math.min(960, maxW));
        stage.setMinHeight(Math.min(600, maxH));

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + HEX_CANVAS + ";");

        // Header Top Bar
        root.setTop(buatTopBar());

        // Sidebar Kiri & Area Konten
        StackPane contentArea = new StackPane();
        contentArea.setPadding(new Insets(12));
        contentArea.setStyle("-fx-background-color: " + HEX_CANVAS + ";");

        Region panelPOS = buatPanelPOS();
        Region panelRiwayat = buatPanelRiwayat();
        panelRiwayat.setVisible(false);

        contentArea.getChildren().addAll(panelPOS, panelRiwayat);

        root.setLeft(buatSidebar(panelPOS, panelRiwayat));
        root.setCenter(contentArea);

        double initW = Math.min(1240, maxW);
        double initH = Math.min(740, maxH);

        Scene scene = new Scene(root, initW, initH);
        stage.setScene(scene);
        stage.setX(visualBounds.getMinX() + (maxW - initW) / 2);
        stage.setY(visualBounds.getMinY() + (maxH - initH) / 2);
        stage.setMaximized(true);
        stage.show();
    }

    private HBox buatTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(10, 20, 10, 20));
        bar.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: " + HEX_BORDER + ";" +
                        "-fx-border-width: 0 0 1 0;"
        );

        HBox left = new HBox(10);
        left.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label("Pacbuck Coffee Terminal Kasir POS");
        lblTitle.setFont(FONT_LARGE_TITLE);
        lblTitle.setTextFill(COLOR_PRIMARY_DARK);

        Label lblSubtitle = new Label("|  Sistem Transaksi Kasir Cepat & Cetak Struk");
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

        Label lblUser = new Label("Kasir: " + currentUser.getNama());
        lblUser.setFont(FONT_BOLD);
        lblUser.setTextFill(COLOR_TEXT_MAIN);
        userBadge.getChildren().add(lblUser);

        AdminDashboard.ContrastButton btnLogout = new AdminDashboard.ContrastButton("Logout", HEX_DANGER, "#FFFFFF");
        btnLogout.setOnAction(e -> {
            if (showConfirm("Konfirmasi Logout", "Keluar dari sesi kasir?")) {
                AuthService.getInstance().logout();
                new LoginFrame().start(new Stage());
                stage.close();
            }
        });

        right.getChildren().addAll(userBadge, btnLogout);
        bar.getChildren().addAll(left, spacer, right);
        return bar;
    }

    private VBox buatSidebar(Region panelPOS, Region panelRiwayat) {
        VBox sidebar = new VBox(6);
        sidebar.setPrefWidth(230);
        sidebar.setPadding(new Insets(18, 14, 18, 14));
        sidebar.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: " + HEX_BORDER + ";" +
                        "-fx-border-width: 0 1 0 0;"
        );

        // Brand Logo
        HBox brand = new HBox(8);
        brand.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(brand, new Insets(0, 0, 14, 0));

        Label lblLogo = new Label("☕");
        lblLogo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));

        Label lblBrandName = new Label("Pacbuck Kasir");
        lblBrandName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        lblBrandName.setTextFill(COLOR_PRIMARY_DARK);
        brand.getChildren().addAll(lblLogo, lblBrandName);

        sidebar.getChildren().add(brand);

        AdminDashboard.ContrastButton btnPos = createSidebarButton("Transaksi Kasir", true);
        AdminDashboard.ContrastButton btnHist = createSidebarButton("Riwayat Transaksi", false);

        btnPos.setOnAction(e -> {
            panelPOS.setVisible(true);
            panelRiwayat.setVisible(false);
            btnPos.setColors(HEX_PRIMARY_DARK, "#FFFFFF");
            btnHist.setColors(HEX_CANVAS, HEX_TEXT_MAIN);
        });

        btnHist.setOnAction(e -> {
            panelPOS.setVisible(false);
            panelRiwayat.setVisible(true);
            btnHist.setColors(HEX_PRIMARY_DARK, "#FFFFFF");
            btnPos.setColors(HEX_CANVAS, HEX_TEXT_MAIN);
        });

        sidebar.getChildren().addAll(btnPos, btnHist);

        Region glue = new Region();
        VBox.setVgrow(glue, Priority.ALWAYS);
        sidebar.getChildren().add(glue);

        // Mini Card Info
        VBox miniCard = new VBox(2);
        miniCard.setPadding(new Insets(12, 14, 12, 14));
        miniCard.setStyle(
                "-fx-background-color: #0F172A;" +
                        "-fx-background-radius: 12;"
        );

        Label lblCardTitle = new Label("Kasir Terminal");
        lblCardTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblCardTitle.setTextFill(Color.WHITE);

        Label lblCardSub = new Label("Kelompok 4 PBO UNSOED");
        lblCardSub.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 10));
        lblCardSub.setTextFill(Color.web("#94A3B8"));

        Label lblCardNumber = new Label("•••• 2026 POS");
        lblCardNumber.setFont(Font.font("Consolas", FontWeight.BOLD, 11));
        lblCardNumber.setTextFill(Color.web("#CBD5E1"));
        VBox.setMargin(lblCardNumber, new Insets(8, 0, 0, 0));

        miniCard.getChildren().addAll(lblCardTitle, lblCardSub, lblCardNumber);
        sidebar.getChildren().add(miniCard);

        return sidebar;
    }

    private AdminDashboard.ContrastButton createSidebarButton(String text, boolean active) {
        AdminDashboard.ContrastButton btn = new AdminDashboard.ContrastButton(
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
    // PANEL 1: TRANSAKSI KASIR (POS)
    // =========================================================================
    private Region buatPanelPOS() {
        Transaksi transaksi = new Transaksi(transaksiService.generateNoFaktur(), currentUser.getNama());
        final Promo[] appliedPromo = {null};

        // --- KOLOM KIRI (55%): KATALOG MENU ---
        BorderPane pnlKiri = new BorderPane();
        AdminDashboard.styleFintechCard(pnlKiri, 12, 14);

        HBox pnlHeaderMenu = new HBox(8);
        pnlHeaderMenu.setAlignment(Pos.CENTER_LEFT);
        BorderPane.setMargin(pnlHeaderMenu, new Insets(0, 0, 8, 0));

        HBox pnlFilterPills = new HBox(6);
        pnlFilterPills.setAlignment(Pos.CENTER_LEFT);

        AdminDashboard.ContrastButton tabAll = AdminDashboard.createFilterPill("Semua", true);
        AdminDashboard.ContrastButton tabKopi = AdminDashboard.createFilterPill("Kopi", false);
        AdminDashboard.ContrastButton tabNonKopi = AdminDashboard.createFilterPill("Non-Kopi", false);
        AdminDashboard.ContrastButton tabMakanan = AdminDashboard.createFilterPill("Makanan", false);
        pnlFilterPills.getChildren().addAll(tabAll, tabKopi, tabNonKopi, tabMakanan);

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        TextField txtSearch = createPillTextField("", "Cari nama / kode menu...", true);
        txtSearch.setPrefWidth(185);
        txtSearch.setPrefHeight(34);

        pnlHeaderMenu.getChildren().addAll(pnlFilterPills, headerSpacer, txtSearch);
        pnlKiri.setTop(pnlHeaderMenu);

        ObservableList<Menu> menuData = FXCollections.observableArrayList();
        TableView<Menu> tblMenu = new TableView<>(menuData);
        styleFintechTable(tblMenu);

        TableColumn<Menu, Integer> colMId = new TableColumn<>("ID");
        colMId.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getId()));
        colMId.setPrefWidth(40);

        TableColumn<Menu, String> colMKode = new TableColumn<>("Kode");
        colMKode.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getKode()));
        colMKode.setPrefWidth(65);

        TableColumn<Menu, String> colMNama = new TableColumn<>("Nama Menu");
        colMNama.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getNama()));
        colMNama.setPrefWidth(170);

        TableColumn<Menu, String> colMKat = new TableColumn<>("Kategori");
        colMKat.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getKategori()));
        colMKat.setPrefWidth(85);

        TableColumn<Menu, String> colMHarga = new TableColumn<>("Harga");
        colMHarga.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(formatRupiah(d.getValue().getHarga())));
        colMHarga.setPrefWidth(95);

        TableColumn<Menu, Integer> colMStok = new TableColumn<>("Stok");
        colMStok.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getStok()));
        colMStok.setPrefWidth(55);

        tblMenu.getColumns().addAll(List.of(colMId, colMKode, colMNama, colMKat, colMHarga, colMStok));
        pnlKiri.setCenter(tblMenu);

        final String[] currentFilter = {"Semua"};
        Runnable loadMenuData = () -> {
            try {
                menuData.clear();
                List<Menu> list = menuService.getDaftarMenu(currentFilter[0]);
                String query = txtSearch.getText().trim().toLowerCase();
                for (Menu m : list) {
                    if (query.isEmpty() || m.getNama().toLowerCase().contains(query) || m.getKode().toLowerCase().contains(query)) {
                        menuData.add(m);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };
        loadMenuData.run();

        AdminDashboard.ContrastButton[] filterTabs = {tabAll, tabKopi, tabNonKopi, tabMakanan};
        String[] filterNames = {"Semua", "Kopi", "Non-Kopi", "Makanan"};
        for (int i = 0; i < filterTabs.length; i++) {
            final int idx = i;
            filterTabs[i].setOnAction(e -> {
                currentFilter[0] = filterNames[idx];
                for (AdminDashboard.ContrastButton b : filterTabs) {
                    b.setColors(HEX_CANVAS, HEX_TEXT_MAIN);
                }
                filterTabs[idx].setColors(HEX_PRIMARY_DARK, "#FFFFFF");
                loadMenuData.run();
            });
        }
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> loadMenuData.run());

        AdminDashboard.ContrastButton btnPesanMenu = new AdminDashboard.ContrastButton("+ Tambahkan ke Keranjang", HEX_PRIMARY_DARK, "#FFFFFF");
        btnPesanMenu.setFont(FONT_BOLD);
        btnPesanMenu.setMaxWidth(Double.MAX_VALUE);
        btnPesanMenu.setPrefHeight(38);
        BorderPane.setMargin(btnPesanMenu, new Insets(8, 0, 0, 0));
        pnlKiri.setBottom(btnPesanMenu);

        // --- KOLOM KANAN (45%): KERANJANG & CHECKOUT ---
        BorderPane pnlKanan = new BorderPane();
        AdminDashboard.styleFintechCard(pnlKanan, 12, 14);

        HBox pnlHeaderCart = new HBox();
        pnlHeaderCart.setAlignment(Pos.CENTER_LEFT);
        BorderPane.setMargin(pnlHeaderCart, new Insets(0, 0, 8, 0));

        Label lblFaktur = new Label("Faktur: " + transaksi.getNoFaktur());
        lblFaktur.setFont(FONT_TITLE);
        lblFaktur.setTextFill(COLOR_PRIMARY_DARK);

        Region cartHeaderSpacer = new Region();
        HBox.setHgrow(cartHeaderSpacer, Priority.ALWAYS);

        Label lblKasirInfo = new Label("Kasir: " + currentUser.getNama());
        lblKasirInfo.setFont(FONT_REGULAR);
        lblKasirInfo.setTextFill(COLOR_TEXT_MUTED);

        pnlHeaderCart.getChildren().addAll(lblFaktur, cartHeaderSpacer, lblKasirInfo);
        pnlKanan.setTop(pnlHeaderCart);

        ObservableList<DetailTransaksi> cartData = FXCollections.observableArrayList();
        TableView<DetailTransaksi> tblCart = new TableView<>(cartData);
        styleFintechTable(tblCart);

        TableColumn<DetailTransaksi, Integer> colCId = new TableColumn<>("ID");
        colCId.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getMenuId()));
        colCId.setPrefWidth(35);

        TableColumn<DetailTransaksi, String> colCNama = new TableColumn<>("Nama Menu");
        colCNama.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getNamaMenu()));
        colCNama.setPrefWidth(145);

        TableColumn<DetailTransaksi, String> colCHarga = new TableColumn<>("Harga");
        colCHarga.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(formatRupiah(d.getValue().getHarga())));
        colCHarga.setPrefWidth(80);

        TableColumn<DetailTransaksi, Integer> colCQty = new TableColumn<>("Qty");
        colCQty.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getQty()));
        colCQty.setPrefWidth(45);

        TableColumn<DetailTransaksi, String> colCSub = new TableColumn<>("Subtotal");
        colCSub.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(formatRupiah(d.getValue().getSubtotal())));
        colCSub.setPrefWidth(90);

        tblCart.getColumns().addAll(List.of(colCId, colCNama, colCHarga, colCQty, colCSub));

        // Action Toolbar Cart (+1, -1, Hapus, Reset)
        HBox pnlCartControls = new HBox(6);
        pnlCartControls.setAlignment(Pos.CENTER_RIGHT);

        AdminDashboard.ContrastButton btnPlus1 = new AdminDashboard.ContrastButton("+1", HEX_SUCCESS, "#FFFFFF");
        AdminDashboard.ContrastButton btnMinus1 = new AdminDashboard.ContrastButton("-1", HEX_AMBER, "#FFFFFF");
        AdminDashboard.ContrastButton btnHapus = new AdminDashboard.ContrastButton("Hapus", HEX_DANGER, "#FFFFFF");
        AdminDashboard.ContrastButton btnKosongkan = new AdminDashboard.ContrastButton("Reset", HEX_SLATE_BTN, "#FFFFFF");
        pnlCartControls.getChildren().addAll(btnPlus1, btnMinus1, btnHapus, btnKosongkan);

        VBox pnlCartCenter = new VBox(6);
        VBox.setVgrow(tblCart, Priority.ALWAYS);
        pnlCartCenter.getChildren().addAll(tblCart, pnlCartControls);
        pnlKanan.setCenter(pnlCartCenter);

        // Panel Pembayaran & Rincian
        VBox pnlCheckout = new VBox(8);
        pnlCheckout.setPadding(new Insets(10, 0, 0, 0));

        // Promo Row
        HBox pnlPromoRow = new HBox(6);
        pnlPromoRow.setAlignment(Pos.CENTER_LEFT);
        Label lblKodePromo = new Label("Kode Promo:");
        lblKodePromo.setFont(FONT_REGULAR);
        TextField txtKodePromo = createPillTextField("", "Masukkan kode promo...", true);
        HBox.setHgrow(txtKodePromo, Priority.ALWAYS);
        AdminDashboard.ContrastButton btnTerapkanPromo = new AdminDashboard.ContrastButton("Terapkan", HEX_PRIMARY_DARK, "#FFFFFF");
        pnlPromoRow.getChildren().addAll(lblKodePromo, txtKodePromo, btnTerapkanPromo);

        // Ringkasan Total
        Label lblSubtotalVal = new Label("Rp 0");
        lblSubtotalVal.setFont(FONT_REGULAR);
        Label lblDiskonVal = new Label("Rp 0");
        lblDiskonVal.setFont(FONT_REGULAR);
        Label lblPajakVal = new Label("Rp 0");
        lblPajakVal.setFont(FONT_REGULAR);
        Label lblTotalVal = new Label("Rp 0");
        lblTotalVal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblTotalVal.setTextFill(COLOR_PRIMARY_DARK);

        VBox pnlSummary = new VBox(3);
        pnlSummary.getChildren().addAll(
                buatBarisLabel("Subtotal Pesanan:", lblSubtotalVal),
                buatBarisLabel("Diskon Promo:", lblDiskonVal),
                buatBarisLabel("PPN (10%):", lblPajakVal),
                buatBarisLabel("Total Tagihan:", lblTotalVal)
        );

        // Template Nominal Uang
        HBox pnlQuickCash = new HBox(4);
        AdminDashboard.ContrastButton btnUangPas = new AdminDashboard.ContrastButton("Pas", HEX_PRIMARY_DARK, "#FFFFFF");
        AdminDashboard.ContrastButton btnUang20k = new AdminDashboard.ContrastButton("20k", HEX_SLATE_BTN, "#FFFFFF");
        AdminDashboard.ContrastButton btnUang50k = new AdminDashboard.ContrastButton("50k", HEX_SLATE_BTN, "#FFFFFF");
        AdminDashboard.ContrastButton btnUang100k = new AdminDashboard.ContrastButton("100k", HEX_SLATE_BTN, "#FFFFFF");
        AdminDashboard.ContrastButton btnUang200k = new AdminDashboard.ContrastButton("200k", HEX_SLATE_BTN, "#FFFFFF");

        for (AdminDashboard.ContrastButton b : new AdminDashboard.ContrastButton[]{btnUangPas, btnUang20k, btnUang50k, btnUang100k, btnUang200k}) {
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

        Label lblBayarTitle = new Label("Nominal Bayar:");
        lblBayarTitle.setFont(FONT_REGULAR);
        Label lblKembaliTitle = new Label("Kembalian:");
        lblKembaliTitle.setFont(FONT_REGULAR);

        pnlBayarRow.add(lblBayarTitle, 0, 0);
        pnlBayarRow.add(txtBayar, 1, 0);
        pnlBayarRow.add(lblKembaliTitle, 0, 1);
        pnlBayarRow.add(lblKembalian, 1, 1);

        // Tombol Bayar
        AdminDashboard.ContrastButton btnBayar = new AdminDashboard.ContrastButton("Bayar & Cetak Struk", HEX_SUCCESS, "#FFFFFF");
        btnBayar.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btnBayar.setMaxWidth(Double.MAX_VALUE);
        btnBayar.setPrefHeight(40);

        pnlCheckout.getChildren().addAll(pnlPromoRow, pnlSummary, pnlQuickCash, pnlBayarRow, btnBayar);
        pnlKanan.setBottom(pnlCheckout);

        // Split Layout 55% / 45%
        SplitPane splitPane = new SplitPane(pnlKiri, pnlKanan);
        splitPane.setDividerPositions(0.55);
        splitPane.setStyle("-fx-background-color: " + HEX_CANVAS + "; -fx-box-border: transparent;");

        // Logic Helper
        Runnable refreshCart = () -> {
            cartData.setAll(transaksi.getItems());
            tblCart.refresh();
            transaksi.hitungTotal(appliedPromo[0]);
            lblSubtotalVal.setText(formatRupiah(transaksi.getSubtotal()));
            lblDiskonVal.setText("- " + formatRupiah(transaksi.getDiskon()));
            lblPajakVal.setText("+ " + formatRupiah(transaksi.getPajak()));
            lblTotalVal.setText(formatRupiah(transaksi.getTotal()));

            try {
                double bayar = Double.parseDouble(txtBayar.getText().trim());
                double kembali = bayar - transaksi.getTotal();
                if (kembali >= 0) {
                    lblKembalian.setText(formatRupiah(kembali));
                    lblKembalian.setTextFill(COLOR_SUCCESS);
                } else {
                    lblKembalian.setText("Kurang " + formatRupiah(Math.abs(kembali)));
                    lblKembalian.setTextFill(COLOR_DANGER);
                }
            } catch (Exception ex) {
                lblKembalian.setText("Rp 0");
                lblKembalian.setTextFill(COLOR_SUCCESS);
            }
        };

        txtBayar.textProperty().addListener((obs, o, n) -> refreshCart.run());

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
            txtKodePromo.setText("");
            txtBayar.setText("");
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
                showAlert(Alert.AlertType.WARNING, "Uang Kurang", "Uang pembayaran kurang dari total tagihan!\nTotal: " + formatRupiah(transaksi.getTotal()));
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
                            "Transaksi Berhasil Diproses!\nStruk resmi telah dicetak dan disimpan di:\n" + fileStruk.getAbsolutePath());

                    // Reset form
                    transaksi.setNoFaktur(transaksiService.generateNoFaktur());
                    lblFaktur.setText("Faktur: " + transaksi.getNoFaktur());
                    transaksi.getItems().clear();
                    appliedPromo[0] = null;
                    txtKodePromo.setText("");
                    txtBayar.setText("");
                    refreshCart.run();
                    loadMenuData.run();
                }
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error Transaksi", "Gagal memproses transaksi: " + ex.getMessage());
            }
        });

        return splitPane;
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
    // PANEL 2: RIWAYAT TRANSAKSI & RINCIAN BARANG TERJUAL (MASTER-DETAIL)
    // =========================================================================
    private Region buatPanelRiwayat() {
        // Master (Tabel Transaksi)
        BorderPane pnlMaster = new BorderPane();
        AdminDashboard.styleFintechCard(pnlMaster, 14, 16);

        HBox topMaster = new HBox(10);
        topMaster.setAlignment(Pos.CENTER_LEFT);
        BorderPane.setMargin(topMaster, new Insets(0, 0, 8, 0));

        Label lblTitle = new Label("Riwayat Transaksi Penjualan Toko");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setTextFill(COLOR_TEXT_MAIN);

        Region masterSpacer = new Region();
        HBox.setHgrow(masterSpacer, Priority.ALWAYS);

        AdminDashboard.ContrastButton btnRefresh = new AdminDashboard.ContrastButton("Refresh Riwayat", HEX_SLATE_BTN, "#FFFFFF");
        topMaster.getChildren().addAll(lblTitle, masterSpacer, btnRefresh);
        pnlMaster.setTop(topMaster);

        ObservableList<Transaksi> trxData = FXCollections.observableArrayList();
        TableView<Transaksi> tblTrx = new TableView<>(trxData);
        styleFintechTable(tblTrx);

        TableColumn<Transaksi, Integer> colTId = new TableColumn<>("ID");
        colTId.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getId()));
        colTId.setPrefWidth(45);

        TableColumn<Transaksi, String> colTFaktur = new TableColumn<>("No Faktur");
        colTFaktur.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getNoFaktur()));
        colTFaktur.setPrefWidth(150);

        TableColumn<Transaksi, String> colTKasir = new TableColumn<>("Kasir");
        colTKasir.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getKasirNama()));
        colTKasir.setPrefWidth(150);

        TableColumn<Transaksi, String> colTSub = new TableColumn<>("Subtotal");
        colTSub.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(formatRupiah(d.getValue().getSubtotal())));
        colTSub.setPrefWidth(105);

        TableColumn<Transaksi, String> colTDiskon = new TableColumn<>("Diskon");
        colTDiskon.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(formatRupiah(d.getValue().getDiskon())));
        colTDiskon.setPrefWidth(95);

        TableColumn<Transaksi, String> colTPajak = new TableColumn<>("PPN (10%)");
        colTPajak.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(formatRupiah(d.getValue().getPajak())));
        colTPajak.setPrefWidth(95);

        TableColumn<Transaksi, String> colTTotal = new TableColumn<>("Total Tagihan");
        colTTotal.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(formatRupiah(d.getValue().getTotal())));
        colTTotal.setPrefWidth(115);

        TableColumn<Transaksi, String> colTBayar = new TableColumn<>("Nominal Bayar");
        colTBayar.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(formatRupiah(d.getValue().getBayar())));
        colTBayar.setPrefWidth(115);

        TableColumn<Transaksi, String> colTKembali = new TableColumn<>("Kembalian");
        colTKembali.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(formatRupiah(d.getValue().getKembalian())));
        colTKembali.setPrefWidth(105);

        tblTrx.getColumns().addAll(List.of(colTId, colTFaktur, colTKasir, colTSub, colTDiskon, colTPajak, colTTotal, colTBayar, colTKembali));
        pnlMaster.setCenter(tblTrx);

        // Detail (Tabel Rincian Barang Terjual)
        BorderPane pnlDetail = new BorderPane();
        AdminDashboard.styleFintechCard(pnlDetail, 14, 16);

        HBox topDetail = new HBox(10);
        topDetail.setAlignment(Pos.CENTER_LEFT);
        BorderPane.setMargin(topDetail, new Insets(0, 0, 8, 0));

        Label lblDetailTitle = new Label("Rincian Barang Terjual (Klik baris transaksi di atas untuk melihat detail)");
        lblDetailTitle.setFont(FONT_TITLE);
        lblDetailTitle.setTextFill(COLOR_PRIMARY_DARK);

        Region detailSpacer = new Region();
        HBox.setHgrow(detailSpacer, Priority.ALWAYS);

        AdminDashboard.ContrastButton btnBukaPDF = new AdminDashboard.ContrastButton("Buka Dokumen Struk (PDF)", HEX_PRIMARY_DARK, "#FFFFFF");
        topDetail.getChildren().addAll(lblDetailTitle, detailSpacer, btnBukaPDF);
        pnlDetail.setTop(topDetail);

        ObservableList<Object[]> itemData = FXCollections.observableArrayList();
        TableView<Object[]> tblItems = new TableView<>(itemData);
        styleFintechTable(tblItems);

        TableColumn<Object[], String> colINo = new TableColumn<>("No");
        colINo.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(String.valueOf(d.getValue()[0])));
        colINo.setPrefWidth(55);

        TableColumn<Object[], String> colINama = new TableColumn<>("Nama Menu / Minuman");
        colINama.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(String.valueOf(d.getValue()[1])));
        colINama.setPrefWidth(280);

        TableColumn<Object[], String> colIHarga = new TableColumn<>("Harga Satuan");
        colIHarga.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(String.valueOf(d.getValue()[2])));
        colIHarga.setPrefWidth(140);

        TableColumn<Object[], String> colIQty = new TableColumn<>("Qty Terjual");
        colIQty.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(String.valueOf(d.getValue()[3])));
        colIQty.setPrefWidth(130);

        TableColumn<Object[], String> colISub = new TableColumn<>("Subtotal");
        colISub.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(String.valueOf(d.getValue()[4])));
        colISub.setPrefWidth(150);

        tblItems.getColumns().addAll(List.of(colINo, colINama, colIHarga, colIQty, colISub));
        pnlDetail.setCenter(tblItems);

        Runnable loadTrx = () -> {
            try {
                itemData.clear();
                lblDetailTitle.setText("Rincian Barang Terjual (Klik baris transaksi di atas untuk melihat detail)");
                trxData.setAll(transaksiService.getRiwayatTransaksi());
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Gagal memuat riwayat: " + ex.getMessage());
            }
        };
        loadTrx.run();

        // Event Klik Baris Transaksi -> Menampilkan Detail Barang Terjual
        tblTrx.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, t) -> {
            if (t != null) {
                lblDetailTitle.setText("Rincian Barang Terjual - No Faktur: " + t.getNoFaktur() + " (Kasir: " + t.getKasirNama() + ")");
                itemData.clear();

                int no = 1;
                for (DetailTransaksi dt : t.getItems()) {
                    itemData.add(new Object[]{
                            no++, dt.getNamaMenu(), formatRupiah(dt.getHarga()),
                            dt.getQty() + " cup/porsi", formatRupiah(dt.getSubtotal())
                    });
                }

                if (t.getItems().isEmpty()) {
                    itemData.add(new Object[]{"-", "(Tidak ada rincian item)", "-", "-", "-"});
                }
            }
        });

        btnRefresh.setOnAction(e -> loadTrx.run());

        btnBukaPDF.setOnAction(e -> {
            Transaksi t = tblTrx.getSelectionModel().getSelectedItem();
            if (t == null) {
                showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih salah satu baris transaksi terlebih dahulu!");
                return;
            }
            File file = transaksiService.cetakStruk(t);
            PDFUtil.bukaFile(file);
        });

        // Split Master-Detail Vertikal
        SplitPane splitRiwayat = new SplitPane(pnlMaster, pnlDetail);
        splitRiwayat.setOrientation(Orientation.VERTICAL);
        splitRiwayat.setDividerPositions(0.52);
        splitRiwayat.setStyle("-fx-background-color: " + HEX_CANVAS + "; -fx-box-border: transparent;");
        return splitRiwayat;
    }

    // =========================================================================
    // DIALOG HELPER JAVAFX
    // =========================================================================
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean showConfirm(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Optional<ButtonType> res = alert.showAndWait();
        return res.isPresent() && res.get() == ButtonType.OK;
    }

    // =========================================================================
    // HELPER STYLING UMUM (JAVAFX CSS)
    // =========================================================================
    public static <T> void styleFintechTable(TableView<T> table) {
        table.setFixedCellSize(34);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("Belum ada data"));
        table.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: " + HEX_BORDER + ";" +
                        "-fx-border-width: 1;" +
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

        tf.setStyle(normalStyle);
        tf.focusedProperty().addListener((obs, wasFocused, isFocused) ->
                tf.setStyle(isFocused ? focusedStyle : normalStyle)
        );
        return tf;
    }
}