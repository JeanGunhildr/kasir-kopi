package ui;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Admin;
import model.DetailTransaksi;
import model.Kasir;
import model.Menu;
import model.Pengguna;
import model.Promo;
import model.Transaksi;
import service.*;
import util.PDFUtil;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard Antarmuka Administrator Modern (AdminDashboardView).
 * Fitur Lengkap: POS Kasir, Katalog Menu & Stok (FR-01, FR-03), Akun Kasir (FR-02),
 * Kode Promo (FR-07), Laporan Penjualan Interaktif dengan BarChart (FR-04),
 * dan Riwayat Transaksi Master-Detail (FR-06).
 */
public class AdminDashboardView extends BorderPane {

    private final Pengguna currentUser;
    private final Runnable onLogout;

    private final MenuService menuService = new MenuService();
    private final AuthService authService = AuthService.getInstance();
    private final PromoService promoService = new PromoService();
    private final LaporanService laporanService = new LaporanService();
    private final TransaksiService transaksiService = new TransaksiService();

    private StackPane contentArea;
    private final List<Button> sidebarButtons = new ArrayList<>();

    public AdminDashboardView(Pengguna currentUser, Runnable onLogout) {
        this.currentUser = currentUser;
        this.onLogout = onLogout;
        initUI();
    }

    private void initUI() {
        getStyleClass().add("canvas-bg");
        setStyle("-fx-background-color: #F8FAFC;");

        // 1. Top Header Bar
        setTop(createTopBar());

        // 2. Sidebar Navigation
        setLeft(createSidebar());

        // 3. Dynamic Content Area
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(14, 16, 14, 16));
        setCenter(contentArea);

        // Buka panel awal: POS
        showPOSPanel();
    }

    private Node createTopBar() {
        HBox topBar = new HBox(16);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(12, 20, 12, 20));
        topBar.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent #E2E8F0 transparent; -fx-border-width: 0 0 1px 0; -fx-effect: dropshadow(three-pass-box, rgba(15,23,42,0.03), 8, 0, 0, 1);");

        Label lblLogo = new Label("☕");
        lblLogo.setStyle("-fx-font-size: 22px;");

        VBox brandBox = new VBox(1);
        Label lblBrand = new Label("Pacbuck Coffee Management System");
        lblBrand.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0B3B2C;");

        Label lblSub = new Label("Portal Eksekutif & Administrator POS • PBO UNSOED Kelompok 4");
        lblSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
        brandBox.getChildren().addAll(lblBrand, lblSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // User Profile Badge
        HBox userBadge = new HBox(8);
        userBadge.setAlignment(Pos.CENTER);
        userBadge.setPadding(new Insets(5, 12, 5, 12));
        userBadge.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 20px; -fx-border-color: #CBD5E1; -fx-border-radius: 20px;");

        Label lblUser = new Label("👑 Admin: " + currentUser.getNama());
        lblUser.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");
        userBadge.getChildren().add(lblUser);

        Button btnLogout = new Button("Keluar (Logout)");
        btnLogout.getStyleClass().add("btn-danger");
        btnLogout.setStyle("-fx-font-size: 12px; -fx-padding: 6px 14px;");
        btnLogout.setOnAction(e -> {
            if (UITheme.showConfirm("Konfirmasi Logout", "Apakah Anda yakin ingin keluar?", "Sesi saat ini akan diakhiri dan kembali ke layar login.")) {
                authService.logout();
                if (onLogout != null) onLogout.run();
            }
        });

        topBar.getChildren().addAll(lblLogo, brandBox, spacer, userBadge, btnLogout);
        return topBar;
    }

    private Node createSidebar() {
        VBox sidebar = new VBox(6);
        sidebar.setPrefWidth(240);
        sidebar.setPadding(new Insets(16, 12, 16, 12));
        sidebar.setStyle("-fx-background-color: white; -fx-border-color: transparent #E2E8F0 transparent transparent; -fx-border-width: 0 1px 0 0;");

        Label lblNav = new Label("MENU UTAMA");
        lblNav.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #94A3B8; -fx-padding: 4px 10px;");
        sidebar.getChildren().add(lblNav);

        Button btnPOS = createSidebarButton("💳  Transaksi Kasir (POS)", () -> showPOSPanel());
        Button btnMenu = createSidebarButton("☕  Kelola Menu & Stok", () -> showMenuPanel());
        Button btnKasir = createSidebarButton("👤  Kelola Akun Kasir", () -> showKasirPanel());
        Button btnPromo = createSidebarButton("🏷️  Kode Promo & Diskon", () -> showPromoPanel());
        Button btnLaporan = createSidebarButton("📊  Laporan Penjualan", () -> showLaporanPanel());
        Button btnRiwayat = createSidebarButton("📜  Riwayat Transaksi", () -> showRiwayatPanel());

        sidebar.getChildren().addAll(btnPOS, btnMenu, btnKasir, btnPromo, btnLaporan, btnRiwayat);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        // Sidebar Footer Card
        VBox sysInfo = new VBox(4);
        sysInfo.setPadding(new Insets(10, 12, 10, 12));
        sysInfo.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8px; -fx-border-color: #E2E8F0; -fx-border-radius: 8px;");

        Label lblSys = new Label("● Sistem Kasir JavaFX v2.0");
        lblSys.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #059669;");
        Label lblDb = new Label("MySQL Database Connected");
        lblDb.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748B;");

        sysInfo.getChildren().addAll(lblSys, lblDb);
        sidebar.getChildren().add(sysInfo);

        return sidebar;
    }

    private Button createSidebarButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.getStyleClass().add("sidebar-btn");
        btn.setMaxWidth(Double.MAX_VALUE);
        sidebarButtons.add(btn);

        btn.setOnAction(e -> {
            for (Button b : sidebarButtons) {
                b.getStyleClass().remove("sidebar-btn-active");
                if (!b.getStyleClass().contains("sidebar-btn")) b.getStyleClass().add("sidebar-btn");
            }
            btn.getStyleClass().add("sidebar-btn-active");
            action.run();
        });

        return btn;
    }

    private void setActiveSidebarButton(int index) {
        for (int i = 0; i < sidebarButtons.size(); i++) {
            Button b = sidebarButtons.get(i);
            b.getStyleClass().remove("sidebar-btn-active");
            if (i == index) {
                b.getStyleClass().add("sidebar-btn-active");
            }
        }
    }

    // =========================================================================
    // PANEL 1: TRANSAKSI KASIR (POS)
    // =========================================================================
    private void showPOSPanel() {
        setActiveSidebarButton(0);
        contentArea.getChildren().clear();

        Transaksi transaksi = new Transaksi(transaksiService.generateNoFaktur(), "Admin: " + currentUser.getNama());
        final Promo[] appliedPromo = {null};

        SplitPane split = new SplitPane();
        split.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        // --- KOLOM KIRI (56%): KATALOG MENU ---
        VBox pnlKiri = new VBox(10);
        pnlKiri.getStyleClass().add("card");
        pnlKiri.setPadding(new Insets(14, 16, 14, 16));

        // Header Menu Filter & Search
        HBox topFilter = new HBox(8);
        topFilter.setAlignment(Pos.CENTER_LEFT);

        Button tabAll = new Button("Semua");
        Button tabKopi = new Button("Kopi");
        Button tabNonKopi = new Button("Non-Kopi");
        Button tabMakanan = new Button("Makanan");

        Button[] filterTabs = {tabAll, tabKopi, tabNonKopi, tabMakanan};
        String[] catNames = {"Semua", "Kopi", "Non-Kopi", "Makanan"};
        for (Button b : filterTabs) b.getStyleClass().add("btn-pill");
        tabAll.getStyleClass().add("btn-pill-active");

        TextField txtSearch = new TextField();
        txtSearch.setPromptText("🔍 Cari menu...");
        txtSearch.setPrefWidth(160);

        Region spacerKiri = new Region();
        HBox.setHgrow(spacerKiri, Priority.ALWAYS);

        topFilter.getChildren().addAll(tabAll, tabKopi, tabNonKopi, tabMakanan, spacerKiri, txtSearch);

        // Tabel Menu
        TableView<Menu> tblMenu = new TableView<>();
        tblMenu.setPlaceholder(new Label("Tidak ada menu yang sesuai"));

        TableColumn<Menu, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colId.setPrefWidth(45);

        TableColumn<Menu, String> colKode = new TableColumn<>("Kode");
        colKode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKode()));
        colKode.setPrefWidth(65);

        TableColumn<Menu, String> colNama = new TableColumn<>("Nama Menu");
        colNama.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNama()));
        colNama.setPrefWidth(160);

        TableColumn<Menu, String> colKat = new TableColumn<>("Kategori");
        colKat.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKategori()));
        colKat.setPrefWidth(85);

        TableColumn<Menu, String> colHarga = new TableColumn<>("Harga");
        colHarga.setCellValueFactory(c -> new SimpleStringProperty(UITheme.formatRupiah(c.getValue().getHarga())));
        colHarga.setPrefWidth(95);

        TableColumn<Menu, Label> colStok = new TableColumn<>("Stok");
        colStok.setCellValueFactory(c -> new SimpleObjectProperty<>(UITheme.createStockBadge(c.getValue().getStok())));
        colStok.setPrefWidth(90);

        tblMenu.getColumns().addAll(colId, colKode, colNama, colKat, colHarga, colStok);
        VBox.setVgrow(tblMenu, Priority.ALWAYS);

        ObservableList<Menu> masterMenuList = FXCollections.observableArrayList();
        FilteredList<Menu> filteredMenuList = new FilteredList<>(masterMenuList, p -> true);
        tblMenu.setItems(filteredMenuList);

        final String[] activeCat = {"Semua"};
        Runnable updateMenuFilter = () -> {
            String query = txtSearch.getText().trim().toLowerCase();
            filteredMenuList.setPredicate(m -> {
                boolean matchCat = activeCat[0].equals("Semua") || m.getKategori().equalsIgnoreCase(activeCat[0]);
                boolean matchQuery = query.isEmpty() || m.getNama().toLowerCase().contains(query) || m.getKode().toLowerCase().contains(query);
                return matchCat && matchQuery;
            });
        };

        for (int i = 0; i < filterTabs.length; i++) {
            final int idx = i;
            filterTabs[i].setOnAction(e -> {
                activeCat[0] = catNames[idx];
                for (Button b : filterTabs) b.getStyleClass().remove("btn-pill-active");
                filterTabs[idx].getStyleClass().add("btn-pill-active");
                updateMenuFilter.run();
            });
        }
        txtSearch.textProperty().addListener((obs, oldV, newV) -> updateMenuFilter.run());

        Runnable loadMenu = () -> {
            try {
                masterMenuList.setAll(menuService.getDaftarMenu("Semua"));
                updateMenuFilter.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };
        loadMenu.run();

        Button btnTambahKeranjang = new Button("➕ Tambahkan ke Keranjang");
        btnTambahKeranjang.getStyleClass().add("btn-primary");
        btnTambahKeranjang.setMaxWidth(Double.MAX_VALUE);
        btnTambahKeranjang.setPrefHeight(38);

        pnlKiri.getChildren().addAll(topFilter, tblMenu, btnTambahKeranjang);

        // --- KOLOM KANAN (44%): KERANJANG & CHECKOUT ---
        VBox pnlKanan = new VBox(10);
        pnlKanan.getStyleClass().add("card");
        pnlKanan.setPadding(new Insets(14, 16, 14, 16));

        HBox cartHeader = new HBox(8);
        cartHeader.setAlignment(Pos.CENTER_LEFT);
        Label lblFaktur = new Label("Faktur: " + transaksi.getNoFaktur());
        lblFaktur.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0B3B2C;");

        Region spacerCart = new Region();
        HBox.setHgrow(spacerCart, Priority.ALWAYS);

        Label lblOperator = new Label("Operator: " + currentUser.getNama());
        lblOperator.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
        cartHeader.getChildren().addAll(lblFaktur, spacerCart, lblOperator);

        // Tabel Cart
        TableView<DetailTransaksi> tblCart = new TableView<>();
        tblCart.setPlaceholder(new Label("Keranjang pesanan masih kosong"));

        TableColumn<DetailTransaksi, String> colCartNama = new TableColumn<>("Menu");
        colCartNama.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNamaMenu()));
        colCartNama.setPrefWidth(140);

        TableColumn<DetailTransaksi, String> colCartHarga = new TableColumn<>("Harga");
        colCartHarga.setCellValueFactory(c -> new SimpleStringProperty(UITheme.formatRupiah(c.getValue().getHarga())));
        colCartHarga.setPrefWidth(85);

        TableColumn<DetailTransaksi, Integer> colCartQty = new TableColumn<>("Qty");
        colCartQty.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQty()).asObject());
        colCartQty.setPrefWidth(50);

        TableColumn<DetailTransaksi, String> colCartSubtotal = new TableColumn<>("Subtotal");
        colCartSubtotal.setCellValueFactory(c -> new SimpleStringProperty(UITheme.formatRupiah(c.getValue().getSubtotal())));
        colCartSubtotal.setPrefWidth(95);

        tblCart.getColumns().addAll(colCartNama, colCartHarga, colCartQty, colCartSubtotal);
        VBox.setVgrow(tblCart, Priority.ALWAYS);

        ObservableList<DetailTransaksi> cartItems = FXCollections.observableArrayList();
        tblCart.setItems(cartItems);

        // Tombol Toolbar Cart (+1, -1, Hapus, Reset)
        HBox cartControls = new HBox(6);
        cartControls.setAlignment(Pos.CENTER_RIGHT);

        Button btnPlus = new Button("+1");
        btnPlus.getStyleClass().add("btn-success");
        btnPlus.setStyle("-fx-padding: 4px 10px;");

        Button btnMinus = new Button("-1");
        btnMinus.getStyleClass().add("btn-warning");
        btnMinus.setStyle("-fx-padding: 4px 10px;");

        Button btnHapusItem = new Button("Hapus");
        btnHapusItem.getStyleClass().add("btn-danger");
        btnHapusItem.setStyle("-fx-padding: 4px 10px;");

        Button btnResetCart = new Button("Kosongkan");
        btnResetCart.getStyleClass().add("btn-slate");
        btnResetCart.setStyle("-fx-padding: 4px 10px;");

        cartControls.getChildren().addAll(btnPlus, btnMinus, btnHapusItem, btnResetCart);

        // Promo Row
        HBox promoRow = new HBox(8);
        promoRow.setAlignment(Pos.CENTER_LEFT);
        Label lblPromo = new Label("Promo:");
        lblPromo.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155;");

        TextField txtPromo = new TextField();
        txtPromo.setPromptText("Kode promo...");
        HBox.setHgrow(txtPromo, Priority.ALWAYS);

        Button btnTerapkanPromo = new Button("Terapkan");
        btnTerapkanPromo.getStyleClass().add("btn-primary");
        btnTerapkanPromo.setStyle("-fx-padding: 6px 12px;");

        promoRow.getChildren().addAll(lblPromo, txtPromo, btnTerapkanPromo);

        // Summary Grid
        GridPane summaryGrid = new GridPane();
        summaryGrid.setHgap(8);
        summaryGrid.setVgap(4);
        summaryGrid.setPadding(new Insets(6, 4, 6, 4));

        Label lblSubtotalVal = new Label("Rp 0");
        Label lblDiskonVal = new Label("Rp 0");
        Label lblPajakVal = new Label("Rp 0");
        Label lblTotalVal = new Label("Rp 0");
        lblTotalVal.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0B3B2C;");

        summaryGrid.addRow(0, new Label("Subtotal Pesanan:"), lblSubtotalVal);
        summaryGrid.addRow(1, new Label("Diskon Promo:"), lblDiskonVal);
        summaryGrid.addRow(2, new Label("PPN (10%):"), lblPajakVal);
        summaryGrid.addRow(3, new Label("Total Tagihan:"), lblTotalVal);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        col2.setHalignment(javafx.geometry.HPos.RIGHT);
        summaryGrid.getColumnConstraints().addAll(col1, col2);

        // Quick Cash Buttons
        HBox quickCash = new HBox(4);
        quickCash.setAlignment(Pos.CENTER);

        Button btnPas = new Button("Pas");
        btnPas.getStyleClass().add("btn-pill");
        HBox.setHgrow(btnPas, Priority.ALWAYS);
        btnPas.setMaxWidth(Double.MAX_VALUE);

        Button btn20k = new Button("20k");
        btn20k.getStyleClass().add("btn-pill");
        HBox.setHgrow(btn20k, Priority.ALWAYS);
        btn20k.setMaxWidth(Double.MAX_VALUE);

        Button btn50k = new Button("50k");
        btn50k.getStyleClass().add("btn-pill");
        HBox.setHgrow(btn50k, Priority.ALWAYS);
        btn50k.setMaxWidth(Double.MAX_VALUE);

        Button btn100k = new Button("100k");
        btn100k.getStyleClass().add("btn-pill");
        HBox.setHgrow(btn100k, Priority.ALWAYS);
        btn100k.setMaxWidth(Double.MAX_VALUE);

        Button btn200k = new Button("200k");
        btn200k.getStyleClass().add("btn-pill");
        HBox.setHgrow(btn200k, Priority.ALWAYS);
        btn200k.setMaxWidth(Double.MAX_VALUE);

        quickCash.getChildren().addAll(btnPas, btn20k, btn50k, btn100k, btn200k);

        // Input Bayar & Kembalian
        HBox bayarRow = new HBox(8);
        bayarRow.setAlignment(Pos.CENTER_LEFT);

        VBox bayarBox = new VBox(2);
        Label lblBayarTitle = new Label("Nominal Bayar:");
        lblBayarTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #334155;");
        TextField txtBayar = new TextField();
        txtBayar.setPromptText("Rp 0");
        bayarBox.getChildren().addAll(lblBayarTitle, txtBayar);
        HBox.setHgrow(bayarBox, Priority.ALWAYS);

        VBox kembaliBox = new VBox(2);
        Label lblKembaliTitle = new Label("Kembalian:");
        lblKembaliTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #334155;");
        Label lblKembalian = new Label("Rp 0");
        lblKembalian.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #059669;");
        kembaliBox.getChildren().addAll(lblKembaliTitle, lblKembalian);
        HBox.setHgrow(kembaliBox, Priority.ALWAYS);

        bayarRow.getChildren().addAll(bayarBox, kembaliBox);

        Button btnBayar = new Button("💳 Bayar & Cetak Struk (PDF)");
        btnBayar.getStyleClass().add("btn-success");
        btnBayar.setMaxWidth(Double.MAX_VALUE);
        btnBayar.setPrefHeight(42);
        btnBayar.setStyle("-fx-font-size: 14px;");

        pnlKanan.getChildren().addAll(cartHeader, tblCart, cartControls, promoRow, summaryGrid, quickCash, bayarRow, btnBayar);

        split.getItems().addAll(pnlKiri, pnlKanan);
        split.setDividerPositions(0.56);
        contentArea.getChildren().add(split);

        // --- Event & Logic Helper POS ---
        Runnable refreshCart = () -> {
            cartItems.setAll(transaksi.getItems());
            transaksi.hitungTotal(appliedPromo[0]);

            lblSubtotalVal.setText(UITheme.formatRupiah(transaksi.getSubtotal()));
            lblDiskonVal.setText("- " + UITheme.formatRupiah(transaksi.getDiskon()));
            lblPajakVal.setText("+ " + UITheme.formatRupiah(transaksi.getPajak()));
            lblTotalVal.setText(UITheme.formatRupiah(transaksi.getTotal()));

            try {
                double bayar = Double.parseDouble(txtBayar.getText().trim());
                double kembali = bayar - transaksi.getTotal();
                if (kembali >= 0) {
                    lblKembalian.setText(UITheme.formatRupiah(kembali));
                    lblKembalian.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #059669;");
                } else {
                    lblKembalian.setText("Kurang " + UITheme.formatRupiah(Math.abs(kembali)));
                    lblKembalian.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #DC2626;");
                }
            } catch (Exception ex) {
                lblKembalian.setText("Rp 0");
            }
        };

        txtBayar.textProperty().addListener((obs, oldV, newV) -> refreshCart.run());

        // Quick cash click
        btnPas.setOnAction(e -> txtBayar.setText(String.valueOf((long) Math.ceil(transaksi.getTotal()))));
        btn20k.setOnAction(e -> txtBayar.setText("20000"));
        btn50k.setOnAction(e -> txtBayar.setText("50000"));
        btn100k.setOnAction(e -> txtBayar.setText("100000"));
        btn200k.setOnAction(e -> txtBayar.setText("200000"));

        Runnable doAddItem = () -> {
            Menu selected = tblMenu.getSelectionModel().getSelectedItem();
            if (selected == null) return;
            if (selected.getStok() <= 0) {
                UITheme.showWarning("Stok Habis", "Stok menu [" + selected.getNama() + "] sudah habis!");
                return;
            }
            try {
                transaksi.tambahItem(selected, 1);
                refreshCart.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };

        btnTambahKeranjang.setOnAction(e -> doAddItem.run());
        tblMenu.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) doAddItem.run();
        });

        btnPlus.setOnAction(e -> {
            DetailTransaksi dt = tblCart.getSelectionModel().getSelectedItem();
            if (dt != null) {
                transaksi.ubahQtyItem(dt.getMenuId(), 1);
                refreshCart.run();
            }
        });

        btnMinus.setOnAction(e -> {
            DetailTransaksi dt = tblCart.getSelectionModel().getSelectedItem();
            if (dt != null) {
                transaksi.ubahQtyItem(dt.getMenuId(), -1);
                refreshCart.run();
            }
        });

        btnHapusItem.setOnAction(e -> {
            DetailTransaksi dt = tblCart.getSelectionModel().getSelectedItem();
            if (dt != null) {
                transaksi.hapusItem(dt.getMenuId());
                refreshCart.run();
            }
        });

        btnResetCart.setOnAction(e -> {
            transaksi.getItems().clear();
            appliedPromo[0] = null;
            txtPromo.clear();
            txtBayar.clear();
            refreshCart.run();
        });

        btnTerapkanPromo.setOnAction(e -> {
            String kode = txtPromo.getText().trim();
            PromoService.PromoValidationResult res = promoService.validasiPromo(kode, transaksi.getSubtotal());
            if (res.valid) {
                appliedPromo[0] = res.promo;
                UITheme.showInfo("Promo Berhasil Diterapkan", res.pesan);
            } else {
                appliedPromo[0] = null;
                UITheme.showWarning("Peringatan Promo", res.pesan);
            }
            refreshCart.run();
        });

        btnBayar.setOnAction(e -> {
            if (transaksi.getItems().isEmpty()) {
                UITheme.showWarning("Keranjang Kosong", "Silakan tambahkan menu ke dalam keranjang terlebih dahulu!");
                return;
            }
            double bayar;
            try {
                bayar = Double.parseDouble(txtBayar.getText().trim());
            } catch (Exception ex) {
                UITheme.showWarning("Validasi Bayar", "Silakan pilih nominal uang atau ketik nominal bayar!");
                return;
            }

            if (bayar < transaksi.getTotal()) {
                UITheme.showWarning("Uang Kurang", "Nominal pembayaran kurang dari total tagihan!\nTotal: " + UITheme.formatRupiah(transaksi.getTotal()));
                return;
            }

            double kembalian = bayar - transaksi.getTotal();
            boolean ok = UITheme.showConfirm("Konfirmasi Pembayaran",
                    "Konfirmasi Pembayaran Transaksi:",
                    "• Faktur     : " + transaksi.getNoFaktur() + "\n" +
                            "• Kasir      : " + transaksi.getKasirNama() + "\n" +
                            "• Total      : " + UITheme.formatRupiah(transaksi.getTotal()) + "\n" +
                            "• Bayar      : " + UITheme.formatRupiah(bayar) + "\n" +
                            "• Kembalian  : " + UITheme.formatRupiah(kembalian) + "\n\n" +
                            "Proses transaksi dan cetak struk PDF sekarang?");
            if (!ok) return;

            transaksi.setBayar(bayar);
            transaksi.setKembalian(kembalian);

            try {
                boolean sukses = transaksiService.prosesTransaksi(transaksi);
                if (sukses) {
                    File fileStruk = transaksiService.cetakStruk(transaksi);
                    PDFUtil.bukaFile(fileStruk);

                    UITheme.showInfo("Pembayaran Sukses!",
                            "Transaksi berhasil diproses!\nStruk resmi tersimpan di:\n" + fileStruk.getAbsolutePath());

                    // Reset form & generate faktur baru
                    transaksi.setNoFaktur(transaksiService.generateNoFaktur());
                    lblFaktur.setText("Faktur: " + transaksi.getNoFaktur());
                    transaksi.getItems().clear();
                    appliedPromo[0] = null;
                    txtPromo.clear();
                    txtBayar.clear();
                    refreshCart.run();
                    loadMenu.run();
                }
            } catch (Exception ex) {
                UITheme.showError("Error Transaksi", "Gagal memproses transaksi: " + ex.getMessage());
            }
        });
    }

    // =========================================================================
    // PANEL 2: KELOLA MENU & STOK (FR-01, FR-03)
    // =========================================================================
    private void showMenuPanel() {
        setActiveSidebarButton(1);
        contentArea.getChildren().clear();

        VBox panel = new VBox(12);
        panel.getStyleClass().add("card");
        panel.setPadding(new Insets(16, 18, 16, 18));

        // Top Bar Panel
        HBox top = new HBox(12);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label lblTitle = new Label("Katalog Menu & Manajemen Stok Real-Time (FR-01, FR-03)");
        lblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");
        Label lblSub = new Label("Kelola harga, kategori (Kopi, Non-Kopi, Makanan), dan penyesuaian stok bahan");
        lblSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
        titleBox.getChildren().addAll(lblTitle, lblSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnTambah = new Button("➕ Tambah Menu");
        btnTambah.getStyleClass().add("btn-success");

        Button btnEdit = new Button("✏️ Edit Menu");
        btnEdit.getStyleClass().add("btn-slate");

        Button btnStok = new Button("📦 Update Stok");
        btnStok.getStyleClass().add("btn-primary");

        Button btnHapus = new Button("🗑️ Hapus");
        btnHapus.getStyleClass().add("btn-danger");

        Button btnRefresh = new Button("🔄 Refresh");
        btnRefresh.getStyleClass().add("btn-slate");

        top.getChildren().addAll(titleBox, spacer, btnTambah, btnEdit, btnStok, btnHapus, btnRefresh);

        // Filter Pills Row
        HBox filterRow = new HBox(8);
        filterRow.setAlignment(Pos.CENTER_LEFT);

        Button tabAll = new Button("Semua");
        Button tabKopi = new Button("Kopi");
        Button tabNonKopi = new Button("Non-Kopi");
        Button tabMakanan = new Button("Makanan");
        Button[] catButtons = {tabAll, tabKopi, tabNonKopi, tabMakanan};
        String[] catValues = {"Semua", "Kopi", "Non-Kopi", "Makanan"};
        for (Button b : catButtons) b.getStyleClass().add("btn-pill");
        tabAll.getStyleClass().add("btn-pill-active");

        TextField txtSearch = new TextField();
        txtSearch.setPromptText("🔍 Cari kode / nama menu...");
        txtSearch.setPrefWidth(220);

        Region spacerF = new Region();
        HBox.setHgrow(spacerF, Priority.ALWAYS);

        filterRow.getChildren().addAll(tabAll, tabKopi, tabNonKopi, tabMakanan, spacerF, txtSearch);

        // Tabel Menu Lengkap
        TableView<Menu> table = new TableView<>();
        table.setPlaceholder(new Label("Belum ada data menu."));

        TableColumn<Menu, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colId.setPrefWidth(45);

        TableColumn<Menu, String> colKode = new TableColumn<>("Kode");
        colKode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKode()));
        colKode.setPrefWidth(80);

        TableColumn<Menu, String> colNama = new TableColumn<>("Nama Menu");
        colNama.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNama()));
        colNama.setPrefWidth(180);

        TableColumn<Menu, String> colKategori = new TableColumn<>("Kategori");
        colKategori.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKategori()));
        colKategori.setPrefWidth(95);

        TableColumn<Menu, String> colHarga = new TableColumn<>("Harga Satuan");
        colHarga.setCellValueFactory(c -> new SimpleStringProperty(UITheme.formatRupiah(c.getValue().getHarga())));
        colHarga.setPrefWidth(110);

        TableColumn<Menu, Label> colStok = new TableColumn<>("Stok Real-Time");
        colStok.setCellValueFactory(c -> new SimpleObjectProperty<>(UITheme.createStockBadge(c.getValue().getStok())));
        colStok.setPrefWidth(110);

        TableColumn<Menu, String> colDesc = new TableColumn<>("Karakteristik & Jenis Penyajian (OOP Polimorfisme)");
        colDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getJenisPenyajian()));
        colDesc.setPrefWidth(320);

        table.getColumns().addAll(colId, colKode, colNama, colKategori, colHarga, colStok, colDesc);
        VBox.setVgrow(table, Priority.ALWAYS);

        ObservableList<Menu> masterList = FXCollections.observableArrayList();
        FilteredList<Menu> filteredList = new FilteredList<>(masterList, p -> true);
        table.setItems(filteredList);

        final String[] curCat = {"Semua"};
        Runnable filterTable = () -> {
            String q = txtSearch.getText().trim().toLowerCase();
            filteredList.setPredicate(m -> {
                boolean matchC = curCat[0].equals("Semua") || m.getKategori().equalsIgnoreCase(curCat[0]);
                boolean matchQ = q.isEmpty() || m.getNama().toLowerCase().contains(q) || m.getKode().toLowerCase().contains(q);
                return matchC && matchQ;
            });
        };

        for (int i = 0; i < catButtons.length; i++) {
            final int idx = i;
            catButtons[i].setOnAction(e -> {
                curCat[0] = catValues[idx];
                for (Button b : catButtons) b.getStyleClass().remove("btn-pill-active");
                catButtons[idx].getStyleClass().add("btn-pill-active");
                filterTable.run();
            });
        }
        txtSearch.textProperty().addListener((obs, oldV, newV) -> filterTable.run());

        Runnable loadData = () -> {
            try {
                masterList.setAll(menuService.getDaftarMenu("Semua"));
                filterTable.run();
            } catch (Exception ex) {
                UITheme.showError("Error", "Gagal memuat menu: " + ex.getMessage());
            }
        };
        loadData.run();

        btnRefresh.setOnAction(e -> loadData.run());

        btnTambah.setOnAction(e -> showFormMenuDialog(null, loadData));
        btnEdit.setOnAction(e -> {
            Menu sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                UITheme.showWarning("Pilih Baris", "Pilih menu pada tabel yang ingin diedit!");
                return;
            }
            showFormMenuDialog(sel, loadData);
        });

        btnStok.setOnAction(e -> {
            Menu sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                UITheme.showWarning("Pilih Baris", "Pilih menu pada tabel yang ingin diupdate stoknya!");
                return;
            }
            UITheme.showTextInput("Update Stok Menu", "Update Stok: " + sel.getNama(), "Masukkan jumlah stok baru:", String.valueOf(sel.getStok()))
                    .ifPresent(input -> {
                        try {
                            int newStok = Integer.parseInt(input.trim());
                            if (newStok < 0) {
                                UITheme.showWarning("Nilai Negatif", "Stok tidak boleh negatif!");
                                return;
                            }
                            menuService.updateStok(sel.getId(), newStok);
                            UITheme.showInfo("Sukses", "Stok [" + sel.getNama() + "] berhasil diperbarui menjadi " + newStok + " unit.");
                            loadData.run();
                        } catch (NumberFormatException nfe) {
                            UITheme.showError("Format Salah", "Format angka stok tidak valid!");
                        } catch (Exception ex) {
                            UITheme.showError("Error", "Gagal memperbarui stok: " + ex.getMessage());
                        }
                    });
        });

        btnHapus.setOnAction(e -> {
            Menu sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                UITheme.showWarning("Pilih Baris", "Pilih menu pada tabel yang ingin dihapus!");
                return;
            }
            if (UITheme.showConfirm("Konfirmasi Hapus", "Hapus Menu", "Apakah Anda yakin ingin menghapus menu [" + sel.getNama() + "]?")) {
                try {
                    menuService.hapusMenu(sel.getId());
                    UITheme.showInfo("Sukses", "Menu berhasil dihapus.");
                    loadData.run();
                } catch (Exception ex) {
                    UITheme.showError("Error", "Gagal menghapus menu: " + ex.getMessage());
                }
            }
        });

        panel.getChildren().addAll(top, filterRow, table);
        contentArea.getChildren().add(panel);
    }

    private void showFormMenuDialog(Menu editMenu, Runnable onSaved) {
        boolean isEdit = editMenu != null;
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(isEdit ? "Edit Menu" : "Tambah Menu Baru");
        dialog.setHeaderText(isEdit ? "Perbarui informasi menu kedai kopi" : "Tambahkan produk menu baru ke dalam sistem");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 24, 10, 24));

        TextField txtKode = new TextField(isEdit ? editMenu.getKode() : "CF" + (System.currentTimeMillis() % 1000));
        TextField txtNama = new TextField(isEdit ? editMenu.getNama() : "");
        ComboBox<String> cmbKategori = new ComboBox<>(FXCollections.observableArrayList("Kopi", "Non-Kopi", "Makanan"));
        cmbKategori.setValue(isEdit ? editMenu.getKategori() : "Kopi");
        TextField txtHarga = new TextField(isEdit ? String.valueOf((long) editMenu.getHarga()) : "");
        TextField txtStok = new TextField(isEdit ? String.valueOf(editMenu.getStok()) : "50");

        grid.addRow(0, new Label("Kode Menu:"), txtKode);
        grid.addRow(1, new Label("Nama Menu:"), txtNama);
        grid.addRow(2, new Label("Kategori:"), cmbKategori);
        grid.addRow(3, new Label("Harga (Rp):"), txtHarga);
        grid.addRow(4, new Label("Stok Awal:"), txtStok);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.setText("Simpan Data");
        btnOk.getStyleClass().add("btn-success");

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                try {
                    String kode = txtKode.getText().trim();
                    String nama = txtNama.getText().trim();
                    String kategori = cmbKategori.getValue();
                    double harga = Double.parseDouble(txtHarga.getText().trim());
                    int stok = Integer.parseInt(txtStok.getText().trim());

                    if (nama.isEmpty() || kode.isEmpty()) {
                        UITheme.showWarning("Validasi", "Nama dan kode menu tidak boleh kosong!");
                        return null;
                    }

                    Menu m = new Menu(isEdit ? editMenu.getId() : 0, kode, nama, kategori, harga, stok);
                    menuService.simpanMenu(m);
                    UITheme.showInfo("Sukses", "Data menu berhasil disimpan!");
                    onSaved.run();
                } catch (Exception ex) {
                    UITheme.showError("Gagal", "Gagal menyimpan menu: " + ex.getMessage());
                }
            }
            return buttonType;
        });

        dialog.showAndWait();
    }

    // =========================================================================
    // PANEL 3: KELOLA AKUN KASIR (FR-02)
    // =========================================================================
    private void showKasirPanel() {
        setActiveSidebarButton(2);
        contentArea.getChildren().clear();

        VBox panel = new VBox(12);
        panel.getStyleClass().add("card");
        panel.setPadding(new Insets(16, 18, 16, 18));

        HBox top = new HBox(12);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label lblTitle = new Label("Manajemen Akun Kasir Operasional (FR-02)");
        lblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");
        Label lblSub = new Label("Tambah kasir baru, aktifkan/nonaktifkan akun, dan reset kredensial");
        lblSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
        titleBox.getChildren().addAll(lblTitle, lblSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnTambah = new Button("➕ Tambah Kasir");
        btnTambah.getStyleClass().add("btn-success");

        Button btnToggle = new Button("🔄 Ubah Status (Aktif/Nonaktif)");
        btnToggle.getStyleClass().add("btn-primary");

        Button btnReset = new Button("🔑 Reset Password");
        btnReset.getStyleClass().add("btn-slate");

        Button btnRefresh = new Button("🔄 Refresh");
        btnRefresh.getStyleClass().add("btn-slate");

        top.getChildren().addAll(titleBox, spacer, btnTambah, btnToggle, btnReset, btnRefresh);

        TableView<Pengguna> table = new TableView<>();
        table.setPlaceholder(new Label("Belum ada data akun kasir."));

        TableColumn<Pengguna, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colId.setPrefWidth(45);

        TableColumn<Pengguna, String> colUser = new TableColumn<>("Username");
        colUser.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));
        colUser.setPrefWidth(120);

        TableColumn<Pengguna, String> colNama = new TableColumn<>("Nama Kasir");
        colNama.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNama()));
        colNama.setPrefWidth(180);

        TableColumn<Pengguna, Label> colStatus = new TableColumn<>("Status Akun");
        colStatus.setCellValueFactory(c -> new SimpleObjectProperty<>(UITheme.createStatusBadge(c.getValue().getStatus())));
        colStatus.setPrefWidth(110);

        TableColumn<Pengguna, String> colAkses = new TableColumn<>("Hak Akses (OOP Polimorfisme)");
        colAkses.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDeskripsiAkses()));
        colAkses.setPrefWidth(320);

        table.getColumns().addAll(colId, colUser, colNama, colStatus, colAkses);
        VBox.setVgrow(table, Priority.ALWAYS);

        ObservableList<Pengguna> listKasir = FXCollections.observableArrayList();
        table.setItems(listKasir);

        Runnable loadKasir = () -> {
            try {
                listKasir.setAll(authService.getDaftarKasir());
            } catch (Exception ex) {
                UITheme.showError("Error", "Gagal memuat kasir: " + ex.getMessage());
            }
        };
        loadKasir.run();

        btnRefresh.setOnAction(e -> loadKasir.run());

        btnTambah.setOnAction(e -> {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Tambah Akun Kasir Baru");
            dialog.setHeaderText("Daftarkan staf kasir baru untuk operasional kedai kopi");

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(12);
            grid.setPadding(new Insets(20, 24, 10, 24));

            TextField txtU = new TextField();
            txtU.setPromptText("misal: kasir2");
            PasswordField txtP = new PasswordField();
            txtP.setPromptText("password");
            TextField txtN = new TextField();
            txtN.setPromptText("Nama lengkap kasir");

            grid.addRow(0, new Label("Username:"), txtU);
            grid.addRow(1, new Label("Password:"), txtP);
            grid.addRow(2, new Label("Nama Lengkap:"), txtN);

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setResultConverter(btnType -> {
                if (btnType == ButtonType.OK) {
                    String u = txtU.getText().trim();
                    String p = txtP.getText().trim();
                    String n = txtN.getText().trim();
                    if (u.isEmpty() || p.isEmpty() || n.isEmpty()) {
                        UITheme.showWarning("Validasi", "Semua kolom wajib diisi!");
                        return null;
                    }
                    try {
                        Pengguna baru = new Kasir(0, u, p, n, "AKTIF");
                        authService.simpanKasir(baru);
                        UITheme.showInfo("Sukses", "Akun kasir [" + u + "] berhasil didaftarkan!");
                        loadKasir.run();
                    } catch (Exception ex) {
                        UITheme.showError("Gagal", "Gagal membuat kasir: " + ex.getMessage());
                    }
                }
                return btnType;
            });

            dialog.showAndWait();
        });

        btnToggle.setOnAction(e -> {
            Pengguna sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                UITheme.showWarning("Pilih Baris", "Pilih akun kasir pada tabel yang ingin diubah statusnya!");
                return;
            }
            String targetStatus = "AKTIF".equalsIgnoreCase(sel.getStatus()) ? "NONAKTIF" : "AKTIF";
            if (UITheme.showConfirm("Ubah Status", "Konfirmasi", "Ubah status akun [" + sel.getNama() + "] menjadi " + targetStatus + "?")) {
                try {
                    authService.ubahStatusKasir(sel.getId(), targetStatus);
                    UITheme.showInfo("Sukses", "Status berhasil diperbarui menjadi " + targetStatus + ".");
                    loadKasir.run();
                } catch (Exception ex) {
                    UITheme.showError("Error", "Gagal ubah status: " + ex.getMessage());
                }
            }
        });

        btnReset.setOnAction(e -> {
            Pengguna sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                UITheme.showWarning("Pilih Baris", "Pilih akun kasir yang ingin direset passwordnya!");
                return;
            }
            UITheme.showTextInput("Reset Password", "Reset Password: " + sel.getUsername(), "Masukkan password baru:", "")
                    .ifPresent(pwdBaru -> {
                        if (pwdBaru.trim().isEmpty()) {
                            UITheme.showWarning("Kosong", "Password tidak boleh kosong!");
                            return;
                        }
                        try {
                            Pengguna u = new Kasir(sel.getId(), sel.getUsername(), pwdBaru.trim(), sel.getNama(), "AKTIF");
                            authService.simpanKasir(u);
                            UITheme.showInfo("Sukses", "Password untuk [" + sel.getUsername() + "] berhasil diperbarui!");
                        } catch (Exception ex) {
                            UITheme.showError("Error", "Gagal reset password: " + ex.getMessage());
                        }
                    });
        });

        panel.getChildren().addAll(top, table);
        contentArea.getChildren().add(panel);
    }

    // =========================================================================
    // PANEL 4: KELOLA PROMO (FR-07)
    // =========================================================================
    private void showPromoPanel() {
        setActiveSidebarButton(3);
        contentArea.getChildren().clear();

        VBox panel = new VBox(12);
        panel.getStyleClass().add("card");
        panel.setPadding(new Insets(16, 18, 16, 18));

        HBox top = new HBox(12);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Label lblTitle = new Label("Manajemen Kode Promo & Diskon Penjualan (FR-07)");
        lblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");
        Label lblSub = new Label("Kelola kode kupon, diskon persentase / potongan nominal, dan masa berlaku");
        lblSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
        titleBox.getChildren().addAll(lblTitle, lblSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnTambah = new Button("➕ Tambah Promo");
        btnTambah.getStyleClass().add("btn-success");

        Button btnHapus = new Button("🗑️ Hapus Promo");
        btnHapus.getStyleClass().add("btn-danger");

        Button btnRefresh = new Button("🔄 Refresh");
        btnRefresh.getStyleClass().add("btn-slate");

        top.getChildren().addAll(titleBox, spacer, btnTambah, btnHapus, btnRefresh);

        TableView<Promo> table = new TableView<>();
        table.setPlaceholder(new Label("Belum ada data promo aktif."));

        TableColumn<Promo, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colId.setPrefWidth(45);

        TableColumn<Promo, String> colKode = new TableColumn<>("Kode Promo");
        colKode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKode()));
        colKode.setPrefWidth(120);

        TableColumn<Promo, String> colNama = new TableColumn<>("Nama Promo");
        colNama.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNamaPromo()));
        colNama.setPrefWidth(170);

        TableColumn<Promo, String> colTipe = new TableColumn<>("Tipe Diskon");
        colTipe.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipeDiskon()));
        colTipe.setPrefWidth(90);

        TableColumn<Promo, String> colNilai = new TableColumn<>("Besaran Nilai");
        colNilai.setCellValueFactory(c -> {
            Promo p = c.getValue();
            String s = "PERSEN".equalsIgnoreCase(p.getTipeDiskon()) ? ((int) p.getNilaiDiskon() + "%") : UITheme.formatRupiah(p.getNilaiDiskon());
            return new SimpleStringProperty(s);
        });
        colNilai.setPrefWidth(100);

        TableColumn<Promo, String> colMin = new TableColumn<>("Min. Belanja");
        colMin.setCellValueFactory(c -> new SimpleStringProperty(UITheme.formatRupiah(c.getValue().getMinBelanja())));
        colMin.setPrefWidth(110);

        TableColumn<Promo, String> colPeriode = new TableColumn<>("Periode Berlaku");
        colPeriode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTglMulai() + " s/d " + c.getValue().getTglSelesai()));
        colPeriode.setPrefWidth(170);

        TableColumn<Promo, Label> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c -> new SimpleObjectProperty<>(UITheme.createStatusBadge(c.getValue().getStatus())));
        colStatus.setPrefWidth(90);

        table.getColumns().addAll(colId, colKode, colNama, colTipe, colNilai, colMin, colPeriode, colStatus);
        VBox.setVgrow(table, Priority.ALWAYS);

        ObservableList<Promo> listPromo = FXCollections.observableArrayList();
        table.setItems(listPromo);

        Runnable loadPromo = () -> {
            try {
                listPromo.setAll(promoService.getDaftarPromo());
            } catch (Exception ex) {
                UITheme.showError("Error", "Gagal memuat promo: " + ex.getMessage());
            }
        };
        loadPromo.run();

        btnRefresh.setOnAction(e -> loadPromo.run());

        btnTambah.setOnAction(e -> {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Tambah Promo Baru");
            dialog.setHeaderText("Tambahkan voucher diskon baru untuk pelanggan kedai kopi");

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 24, 10, 24));

            TextField txtK = new TextField("DISKON" + (int)(Math.random() * 90 + 10));
            TextField txtN = new TextField();
            ComboBox<String> cmbT = new ComboBox<>(FXCollections.observableArrayList("PERSEN", "NOMINAL"));
            cmbT.setValue("PERSEN");
            TextField txtVal = new TextField("10");
            TextField txtMin = new TextField("50000");
            TextField txtMulai = new TextField(LocalDate.now().toString());
            TextField txtSelesai = new TextField(LocalDate.now().plusMonths(1).toString());

            grid.addRow(0, new Label("Kode Promo:"), txtK);
            grid.addRow(1, new Label("Nama Promo:"), txtN);
            grid.addRow(2, new Label("Tipe Diskon:"), cmbT);
            grid.addRow(3, new Label("Besaran Nilai:"), txtVal);
            grid.addRow(4, new Label("Min Belanja (Rp):"), txtMin);
            grid.addRow(5, new Label("Tgl Mulai (YYYY-MM-DD):"), txtMulai);
            grid.addRow(6, new Label("Tgl Selesai (YYYY-MM-DD):"), txtSelesai);

            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setResultConverter(btnType -> {
                if (btnType == ButtonType.OK) {
                    try {
                        String kode = txtK.getText().trim().toUpperCase();
                        String nama = txtN.getText().trim();
                        String tipe = cmbT.getValue();
                        double nilai = Double.parseDouble(txtVal.getText().trim());
                        double min = Double.parseDouble(txtMin.getText().trim());
                        LocalDate tglM = LocalDate.parse(txtMulai.getText().trim());
                        LocalDate tglS = LocalDate.parse(txtSelesai.getText().trim());

                        if (kode.isEmpty() || nama.isEmpty()) {
                            UITheme.showWarning("Validasi", "Kode dan nama promo tidak boleh kosong!");
                            return null;
                        }

                        Promo p = new Promo(0, kode, nama, tipe, nilai, min, tglM, tglS, "AKTIF");
                        promoService.simpanPromo(p);
                        UITheme.showInfo("Sukses", "Promo [" + kode + "] berhasil ditambahkan!");
                        loadPromo.run();
                    } catch (Exception ex) {
                        UITheme.showError("Gagal", "Gagal menyimpan promo: " + ex.getMessage());
                    }
                }
                return btnType;
            });

            dialog.showAndWait();
        });

        btnHapus.setOnAction(e -> {
            Promo sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) {
                UITheme.showWarning("Pilih Baris", "Pilih promo yang ingin dihapus!");
                return;
            }
            if (UITheme.showConfirm("Konfirmasi Hapus", "Hapus Promo", "Apakah Anda yakin ingin menghapus kode promo [" + sel.getKode() + "]?")) {
                try {
                    promoService.hapusPromo(sel.getId());
                    UITheme.showInfo("Sukses", "Promo berhasil dihapus.");
                    loadPromo.run();
                } catch (Exception ex) {
                    UITheme.showError("Error", "Gagal menghapus promo: " + ex.getMessage());
                }
            }
        });

        panel.getChildren().addAll(top, table);
        contentArea.getChildren().add(panel);
    }

    // =========================================================================
    // PANEL 5: LAPORAN PENJUALAN (FR-04) + JAVAFX BARCHART
    // =========================================================================
    private void showLaporanPanel() {
        setActiveSidebarButton(4);
        contentArea.getChildren().clear();

        VBox panel = new VBox(14);

        // Header Filter Card
        HBox filterCard = new HBox(12);
        filterCard.getStyleClass().add("card");
        filterCard.setAlignment(Pos.CENTER_LEFT);
        filterCard.setPadding(new Insets(12, 18, 12, 18));

        VBox titleBox = new VBox(2);
        Label lblTitle = new Label("Laporan Eksekutif Penjualan & Analitik Menu (FR-04)");
        lblTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");
        Label lblSub = new Label("Ringkasan performa omset, volume transaksi, dan visualisasi menu paling laris");
        lblSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
        titleBox.getChildren().addAll(lblTitle, lblSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button tabHarian = new Button("Harian (Hari Ini)");
        Button tabMingguan = new Button("Mingguan (Minggu Ini)");
        Button tabBulanan = new Button("Bulanan (Bulan Ini)");
        Button[] periodBtns = {tabHarian, tabMingguan, tabBulanan};
        String[] periodKeys = {"Harian", "Mingguan", "Bulanan"};
        for (Button b : periodBtns) b.getStyleClass().add("btn-pill");
        tabHarian.getStyleClass().add("btn-pill-active");

        filterCard.getChildren().addAll(titleBox, spacer, tabHarian, tabMingguan, tabBulanan);

        // 3 Kartu Metrik KPI
        HBox kpiBox = new HBox(14);
        Label lblOmset = new Label("Rp 0");
        Label lblTrx = new Label("0 Transaksi");
        Label lblAvg = new Label("Rp 0");

        VBox cardOmset = UITheme.createKpiCard("Total Omset Penjualan", lblOmset, "Akumulasi pendapatan kotor periode ini", "#059669");
        VBox cardTrx = UITheme.createKpiCard("Jumlah Transaksi", lblTrx, "Total pesanan kasir yang berhasil", "#0B3B2C");
        VBox cardAvg = UITheme.createKpiCard("Rata-Rata per Transaksi", lblAvg, "Nilai rata-rata per transaksi belanja", "#334155");

        kpiBox.getChildren().addAll(cardOmset, cardTrx, cardAvg);

        // Split Visual & Data: JavaFX BarChart di Kiri, Tabel Best Sellers di Kanan
        HBox visualAndTable = new HBox(14);
        VBox.setVgrow(visualAndTable, Priority.ALWAYS);

        // Chart Card (Modern JavaFX BarChart)
        VBox chartCard = new VBox(8);
        chartCard.getStyleClass().add("card");
        chartCard.setPadding(new Insets(12, 14, 12, 14));
        HBox.setHgrow(chartCard, Priority.ALWAYS);

        Label lblChartTitle = new Label("📊 Visualisasi Top 5 Menu Paling Laku");
        lblChartTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Menu Minuman / Makanan");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Porsi Terjual");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setLegendVisible(false);
        barChart.setAnimated(false);
        VBox.setVgrow(barChart, Priority.ALWAYS);

        chartCard.getChildren().addAll(lblChartTitle, barChart);

        // Best Sellers Table Card
        VBox tableCard = new VBox(8);
        tableCard.getStyleClass().add("card");
        tableCard.setPadding(new Insets(12, 14, 12, 14));
        tableCard.setPrefWidth(420);

        Label lblTableTitle = new Label("🏆 Peringkat Menu Terlaris");
        lblTableTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        TableView<Object[]> tblTop = new TableView<>();
        tblTop.setPlaceholder(new Label("Belum ada data penjualan pada periode ini."));

        TableColumn<Object[], String> colRank = new TableColumn<>("Rank");
        colRank.setCellValueFactory(c -> new SimpleStringProperty((String) c.getValue()[0]));
        colRank.setPrefWidth(65);

        TableColumn<Object[], String> colMenuName = new TableColumn<>("Nama Menu");
        colMenuName.setCellValueFactory(c -> new SimpleStringProperty((String) c.getValue()[1]));
        colMenuName.setPrefWidth(140);

        TableColumn<Object[], String> colQty = new TableColumn<>("Terjual");
        colQty.setCellValueFactory(c -> new SimpleStringProperty(c.getValue()[2] + " porsi"));
        colQty.setPrefWidth(75);

        TableColumn<Object[], String> colOmset = new TableColumn<>("Omset");
        colOmset.setCellValueFactory(c -> new SimpleStringProperty(UITheme.formatRupiah((double) c.getValue()[3])));
        colOmset.setPrefWidth(100);

        tblTop.getColumns().addAll(colRank, colMenuName, colQty, colOmset);
        VBox.setVgrow(tblTop, Priority.ALWAYS);

        tableCard.getChildren().addAll(lblTableTitle, tblTop);

        visualAndTable.getChildren().addAll(chartCard, tableCard);

        final String[] curPeriode = {"Harian"};
        Runnable refreshLaporan = () -> {
            try {
                LaporanService.LaporanData data = laporanService.getLaporan(curPeriode[0]);
                lblOmset.setText(UITheme.formatRupiah(data.totalPendapatan));
                lblTrx.setText(data.totalTransaksi + " Transaksi");

                double avg = data.totalTransaksi > 0 ? (data.totalPendapatan / data.totalTransaksi) : 0;
                lblAvg.setText(UITheme.formatRupiah(avg));

                // Update Chart
                barChart.getData().clear();
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("Porsi Terjual");

                ObservableList<Object[]> topItems = FXCollections.observableArrayList();
                String[] badges = {"🥇 #1", "🥈 #2", "🥉 #3", "#4", "#5"};
                int idx = 0;
                for (Object[] item : data.menuTerlaris) {
                    String badge = idx < badges.length ? badges[idx] : ("#" + (idx + 1));
                    String nama = (String) item[0];
                    int qty = (int) item[1];
                    double uang = (double) item[2];

                    series.getData().add(new XYChart.Data<>(nama, qty));
                    topItems.add(new Object[]{badge, nama, qty, uang});
                    idx++;
                }
                barChart.getData().add(series);
                tblTop.setItems(topItems);

            } catch (Exception ex) {
                UITheme.showError("Error", "Gagal memuat data laporan: " + ex.getMessage());
            }
        };

        for (int i = 0; i < periodBtns.length; i++) {
            final int idx = i;
            periodBtns[i].setOnAction(e -> {
                curPeriode[0] = periodKeys[idx];
                for (Button b : periodBtns) b.getStyleClass().remove("btn-pill-active");
                periodBtns[idx].getStyleClass().add("btn-pill-active");
                refreshLaporan.run();
            });
        }

        refreshLaporan.run();

        panel.getChildren().addAll(filterCard, kpiBox, visualAndTable);
        contentArea.getChildren().add(panel);
    }

    // =========================================================================
    // PANEL 6: RIWAYAT TRANSAKSI MASTER-DETAIL (FR-06)
    // =========================================================================
    private void showRiwayatPanel() {
        setActiveSidebarButton(5);
        contentArea.getChildren().clear();

        VBox panel = new VBox(10);
        VBox.setVgrow(panel, Priority.ALWAYS);

        SplitPane split = new SplitPane();
        split.setOrientation(Orientation.VERTICAL);
        VBox.setVgrow(split, Priority.ALWAYS);

        // --- BAGIAN ATAS (MASTER): TABEL TRANSAKSI ---
        VBox pnlMaster = new VBox(8);
        pnlMaster.getStyleClass().add("card");
        pnlMaster.setPadding(new Insets(12, 16, 12, 16));

        HBox topMaster = new HBox(12);
        topMaster.setAlignment(Pos.CENTER_LEFT);
        Label lblMasterTitle = new Label("Riwayat Seluruh Transaksi Penjualan (Master Table)");
        lblMasterTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        Region spacerM = new Region();
        HBox.setHgrow(spacerM, Priority.ALWAYS);

        Button btnRefresh = new Button("🔄 Refresh Riwayat");
        btnRefresh.getStyleClass().add("btn-slate");
        topMaster.getChildren().addAll(lblMasterTitle, spacerM, btnRefresh);

        TableView<Transaksi> tblTrx = new TableView<>();
        tblTrx.setPlaceholder(new Label("Belum ada riwayat transaksi tercatat."));

        TableColumn<Transaksi, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colId.setPrefWidth(45);

        TableColumn<Transaksi, String> colFaktur = new TableColumn<>("No Faktur");
        colFaktur.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNoFaktur()));
        colFaktur.setPrefWidth(140);

        TableColumn<Transaksi, String> colKasir = new TableColumn<>("Kasir / Operator");
        colKasir.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKasirNama()));
        colKasir.setPrefWidth(150);

        TableColumn<Transaksi, String> colSub = new TableColumn<>("Subtotal");
        colSub.setCellValueFactory(c -> new SimpleStringProperty(UITheme.formatRupiah(c.getValue().getSubtotal())));
        colSub.setPrefWidth(100);

        TableColumn<Transaksi, String> colDiskon = new TableColumn<>("Diskon");
        colDiskon.setCellValueFactory(c -> new SimpleStringProperty(UITheme.formatRupiah(c.getValue().getDiskon())));
        colDiskon.setPrefWidth(90);

        TableColumn<Transaksi, String> colPajak = new TableColumn<>("PPN (10%)");
        colPajak.setCellValueFactory(c -> new SimpleStringProperty(UITheme.formatRupiah(c.getValue().getPajak())));
        colPajak.setPrefWidth(90);

        TableColumn<Transaksi, String> colTotal = new TableColumn<>("Total Tagihan");
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(UITheme.formatRupiah(c.getValue().getTotal())));
        colTotal.setPrefWidth(110);

        TableColumn<Transaksi, String> colBayar = new TableColumn<>("Nominal Bayar");
        colBayar.setCellValueFactory(c -> new SimpleStringProperty(UITheme.formatRupiah(c.getValue().getBayar())));
        colBayar.setPrefWidth(110);

        TableColumn<Transaksi, String> colKembali = new TableColumn<>("Kembalian");
        colKembali.setCellValueFactory(c -> new SimpleStringProperty(UITheme.formatRupiah(c.getValue().getKembalian())));
        colKembali.setPrefWidth(100);

        tblTrx.getColumns().addAll(colId, colFaktur, colKasir, colSub, colDiskon, colPajak, colTotal, colBayar, colKembali);
        VBox.setVgrow(tblTrx, Priority.ALWAYS);

        pnlMaster.getChildren().addAll(topMaster, tblTrx);

        // --- BAGIAN BAWAH (DETAIL): TABEL RINCIAN BARANG TERJUAL ---
        VBox pnlDetail = new VBox(8);
        pnlDetail.getStyleClass().add("card");
        pnlDetail.setPadding(new Insets(12, 16, 12, 16));

        HBox topDetail = new HBox(12);
        topDetail.setAlignment(Pos.CENTER_LEFT);

        Label lblDetailTitle = new Label("Rincian Barang Terjual (Klik baris transaksi pada tabel di atas)");
        lblDetailTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0B3B2C;");

        Region spacerD = new Region();
        HBox.setHgrow(spacerD, Priority.ALWAYS);

        Button btnBukaPDF = new Button("📄 Buka Dokumen Struk (PDF)");
        btnBukaPDF.getStyleClass().add("btn-primary");
        topDetail.getChildren().addAll(lblDetailTitle, spacerD, btnBukaPDF);

        TableView<DetailTransaksi> tblDetail = new TableView<>();
        tblDetail.setPlaceholder(new Label("Pilih salah satu transaksi pada tabel di atas untuk melihat rincian barang."));

        TableColumn<DetailTransaksi, String> colItemNama = new TableColumn<>("Nama Menu / Minuman");
        colItemNama.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNamaMenu()));
        colItemNama.setPrefWidth(220);

        TableColumn<DetailTransaksi, String> colItemHarga = new TableColumn<>("Harga Satuan");
        colItemHarga.setCellValueFactory(c -> new SimpleStringProperty(UITheme.formatRupiah(c.getValue().getHarga())));
        colItemHarga.setPrefWidth(120);

        TableColumn<DetailTransaksi, String> colItemQty = new TableColumn<>("Qty Terjual");
        colItemQty.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getQty() + " cup/porsi"));
        colItemQty.setPrefWidth(120);

        TableColumn<DetailTransaksi, String> colItemSubtotal = new TableColumn<>("Subtotal");
        colItemSubtotal.setCellValueFactory(c -> new SimpleStringProperty(UITheme.formatRupiah(c.getValue().getSubtotal())));
        colItemSubtotal.setPrefWidth(140);

        tblDetail.getColumns().addAll(colItemNama, colItemHarga, colItemQty, colItemSubtotal);
        VBox.setVgrow(tblDetail, Priority.ALWAYS);

        pnlDetail.getChildren().addAll(topDetail, tblDetail);

        split.getItems().addAll(pnlMaster, pnlDetail);
        split.setDividerPositions(0.55);

        panel.getChildren().add(split);
        contentArea.getChildren().add(panel);

        // State & Load
        ObservableList<Transaksi> listTrx = FXCollections.observableArrayList();
        tblTrx.setItems(listTrx);

        ObservableList<DetailTransaksi> listItems = FXCollections.observableArrayList();
        tblDetail.setItems(listItems);

        Runnable loadTrx = () -> {
            try {
                listTrx.setAll(transaksiService.getRiwayatTransaksi());
                listItems.clear();
                lblDetailTitle.setText("Rincian Barang Terjual (Klik baris transaksi pada tabel di atas)");
            } catch (Exception ex) {
                UITheme.showError("Error", "Gagal memuat riwayat: " + ex.getMessage());
            }
        };
        loadTrx.run();

        btnRefresh.setOnAction(e -> loadTrx.run());

        // Event Klik Baris Master -> Tampilkan Detail Item!
        tblTrx.getSelectionModel().selectedItemProperty().addListener((obs, oldV, sel) -> {
            if (sel != null) {
                lblDetailTitle.setText("Rincian Barang Terjual - No Faktur: " + sel.getNoFaktur() + " (Kasir: " + sel.getKasirNama() + ")");
                listItems.setAll(sel.getItems());
            }
        });

        btnBukaPDF.setOnAction(e -> {
            Transaksi sel = tblTrx.getSelectionModel().getSelectedItem();
            if (sel == null) {
                UITheme.showWarning("Pilih Transaksi", "Pilih salah satu baris transaksi terlebih dahulu!");
                return;
            }
            try {
                File file = transaksiService.cetakStruk(sel);
                PDFUtil.bukaFile(file);
            } catch (Exception ex) {
                UITheme.showError("Gagal Membuka PDF", ex.getMessage());
            }
        });
    }
}
