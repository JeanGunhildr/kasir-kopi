package ui;

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
import javafx.scene.control.*;
import javafx.scene.layout.*;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard Antarmuka Kasir Operasional Modern (KasirDashboardView).
 * Didesain fokus untuk produktivitas kasir cepat (Fast POS Checkout) dan Riwayat Transaksi.
 */
public class KasirDashboardView extends BorderPane {

    private final Pengguna currentUser;
    private final Runnable onLogout;

    private final MenuService menuService = new MenuService();
    private final AuthService authService = AuthService.getInstance();
    private final PromoService promoService = new PromoService();
    private final TransaksiService transaksiService = new TransaksiService();

    private StackPane contentArea;
    private final List<Button> sidebarButtons = new ArrayList<>();

    public KasirDashboardView(Pengguna currentUser, Runnable onLogout) {
        this.currentUser = currentUser;
        this.onLogout = onLogout;
        initUI();
    }

    private void initUI() {
        getStyleClass().add("canvas-bg");
        setStyle("-fx-background-color: #F8FAFC;");

        setTop(createTopBar());
        setLeft(createSidebar());

        contentArea = new StackPane();
        contentArea.setPadding(new Insets(14, 16, 14, 16));
        setCenter(contentArea);

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
        Label lblBrand = new Label("Pacbuck Coffee POS - Terminal Kasir");
        lblBrand.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0B3B2C;");

        Label lblSub = new Label("Stasiun Kasir Operasional • Layanan Cepat Point of Sale");
        lblSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
        brandBox.getChildren().addAll(lblBrand, lblSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // User Badge
        HBox userBadge = new HBox(8);
        userBadge.setAlignment(Pos.CENTER);
        userBadge.setPadding(new Insets(5, 12, 5, 12));
        userBadge.setStyle("-fx-background-color: #ECFDF5; -fx-background-radius: 20px; -fx-border-color: #A7F3D0; -fx-border-radius: 20px;");

        Label lblUser = new Label("☕ Kasir: " + currentUser.getNama());
        lblUser.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #065F46;");
        userBadge.getChildren().add(lblUser);

        Button btnLogout = new Button("Keluar (Logout)");
        btnLogout.getStyleClass().add("btn-danger");
        btnLogout.setStyle("-fx-font-size: 12px; -fx-padding: 6px 14px;");
        btnLogout.setOnAction(e -> {
            if (UITheme.showConfirm("Konfirmasi Logout", "Apakah Anda yakin ingin logout?", "Sesi kasir akan diakhiri dan kembali ke layar login.")) {
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

        Label lblNav = new Label("TERMINAL KASIR");
        lblNav.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #94A3B8; -fx-padding: 4px 10px;");
        sidebar.getChildren().add(lblNav);

        Button btnPOS = createSidebarButton("💳  Transaksi Kasir (POS)", () -> showPOSPanel());
        Button btnRiwayat = createSidebarButton("📜  Riwayat Transaksi & Struk", () -> showRiwayatPanel());

        sidebar.getChildren().addAll(btnPOS, btnRiwayat);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        VBox sysInfo = new VBox(4);
        sysInfo.setPadding(new Insets(10, 12, 10, 12));
        sysInfo.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8px; -fx-border-color: #E2E8F0; -fx-border-radius: 8px;");

        Label lblSys = new Label("● Mode Kasir Aktif");
        lblSys.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #059669;");
        Label lblDb = new Label("Koneksi Database Normal");
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
    // PANEL 1: TRANSAKSI KASIR POS
    // =========================================================================
    private void showPOSPanel() {
        setActiveSidebarButton(0);
        contentArea.getChildren().clear();

        Transaksi transaksi = new Transaksi(transaksiService.generateNoFaktur(), currentUser.getNama());
        final Promo[] appliedPromo = {null};

        SplitPane split = new SplitPane();
        split.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        // --- KOLOM KIRI (56%): KATALOG MENU ---
        VBox pnlKiri = new VBox(10);
        pnlKiri.getStyleClass().add("card");
        pnlKiri.setPadding(new Insets(14, 16, 14, 16));

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

        Label lblOperator = new Label("Kasir: " + currentUser.getNama());
        lblOperator.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
        cartHeader.getChildren().addAll(lblFaktur, spacerCart, lblOperator);

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
                UITheme.showInfo("Promo Berhasil", res.pesan);
            } else {
                appliedPromo[0] = null;
                UITheme.showWarning("Peringatan Promo", res.pesan);
            }
            refreshCart.run();
        });

        btnBayar.setOnAction(e -> {
            if (transaksi.getItems().isEmpty()) {
                UITheme.showWarning("Keranjang Kosong", "Silakan tambahkan pesanan ke keranjang!");
                return;
            }
            double bayar;
            try {
                bayar = Double.parseDouble(txtBayar.getText().trim());
            } catch (Exception ex) {
                UITheme.showWarning("Validasi Bayar", "Silakan ketik atau pilih nominal uang pembayaran!");
                return;
            }

            if (bayar < transaksi.getTotal()) {
                UITheme.showWarning("Uang Kurang", "Uang bayar kurang dari total tagihan!\nTotal: " + UITheme.formatRupiah(transaksi.getTotal()));
                return;
            }

            double kembalian = bayar - transaksi.getTotal();
            boolean ok = UITheme.showConfirm("Konfirmasi Pembayaran Kasir",
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
    // PANEL 2: RIWAYAT TRANSAKSI MASTER-DETAIL
    // =========================================================================
    private void showRiwayatPanel() {
        setActiveSidebarButton(1);
        contentArea.getChildren().clear();

        VBox panel = new VBox(10);
        VBox.setVgrow(panel, Priority.ALWAYS);

        SplitPane split = new SplitPane();
        split.setOrientation(Orientation.VERTICAL);
        VBox.setVgrow(split, Priority.ALWAYS);

        // Master
        VBox pnlMaster = new VBox(8);
        pnlMaster.getStyleClass().add("card");
        pnlMaster.setPadding(new Insets(12, 16, 12, 16));

        HBox topMaster = new HBox(12);
        topMaster.setAlignment(Pos.CENTER_LEFT);
        Label lblMasterTitle = new Label("Riwayat Transaksi Penjualan Saya (Master)");
        lblMasterTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        Region spacerM = new Region();
        HBox.setHgrow(spacerM, Priority.ALWAYS);

        Button btnRefresh = new Button("🔄 Refresh Riwayat");
        btnRefresh.getStyleClass().add("btn-slate");
        topMaster.getChildren().addAll(lblMasterTitle, spacerM, btnRefresh);

        TableView<Transaksi> tblTrx = new TableView<>();
        tblTrx.setPlaceholder(new Label("Belum ada riwayat transaksi."));

        TableColumn<Transaksi, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colId.setPrefWidth(45);

        TableColumn<Transaksi, String> colFaktur = new TableColumn<>("No Faktur");
        colFaktur.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNoFaktur()));
        colFaktur.setPrefWidth(140);

        TableColumn<Transaksi, String> colKasir = new TableColumn<>("Kasir");
        colKasir.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKasirNama()));
        colKasir.setPrefWidth(140);

        TableColumn<Transaksi, String> colSub = new TableColumn<>("Subtotal");
        colSub.setCellValueFactory(c -> new SimpleStringProperty(UITheme.formatRupiah(c.getValue().getSubtotal())));
        colSub.setPrefWidth(100);

        TableColumn<Transaksi, String> colDiskon = new TableColumn<>("Diskon");
        colDiskon.setCellValueFactory(c -> new SimpleStringProperty(UITheme.formatRupiah(c.getValue().getDiskon())));
        colDiskon.setPrefWidth(90);

        TableColumn<Transaksi, String> colPajak = new TableColumn<>("PPN (10%)");
        colPajak.setCellValueFactory(c -> new SimpleStringProperty(UITheme.formatRupiah(c.getValue().getPajak())));
        colPajak.setPrefWidth(90);

        TableColumn<Transaksi, String> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(UITheme.formatRupiah(c.getValue().getTotal())));
        colTotal.setPrefWidth(110);

        TableColumn<Transaksi, String> colBayar = new TableColumn<>("Bayar");
        colBayar.setCellValueFactory(c -> new SimpleStringProperty(UITheme.formatRupiah(c.getValue().getBayar())));
        colBayar.setPrefWidth(110);

        TableColumn<Transaksi, String> colKembali = new TableColumn<>("Kembalian");
        colKembali.setCellValueFactory(c -> new SimpleStringProperty(UITheme.formatRupiah(c.getValue().getKembalian())));
        colKembali.setPrefWidth(100);

        tblTrx.getColumns().addAll(colId, colFaktur, colKasir, colSub, colDiskon, colPajak, colTotal, colBayar, colKembali);
        VBox.setVgrow(tblTrx, Priority.ALWAYS);

        pnlMaster.getChildren().addAll(topMaster, tblTrx);

        // Detail
        VBox pnlDetail = new VBox(8);
        pnlDetail.getStyleClass().add("card");
        pnlDetail.setPadding(new Insets(12, 16, 12, 16));

        HBox topDetail = new HBox(12);
        topDetail.setAlignment(Pos.CENTER_LEFT);

        Label lblDetailTitle = new Label("Rincian Barang Terjual (Klik baris transaksi)");
        lblDetailTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0B3B2C;");

        Region spacerD = new Region();
        HBox.setHgrow(spacerD, Priority.ALWAYS);

        Button btnBukaPDF = new Button("📄 Buka Dokumen Struk (PDF)");
        btnBukaPDF.getStyleClass().add("btn-primary");
        topDetail.getChildren().addAll(lblDetailTitle, spacerD, btnBukaPDF);

        TableView<DetailTransaksi> tblDetail = new TableView<>();
        tblDetail.setPlaceholder(new Label("Pilih baris transaksi di atas untuk melihat rincian barang."));

        TableColumn<DetailTransaksi, String> colItemNama = new TableColumn<>("Nama Menu / Minuman");
        colItemNama.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNamaMenu()));
        colItemNama.setPrefWidth(220);

        TableColumn<DetailTransaksi, String> colItemHarga = new TableColumn<>("Harga Satuan");
        colItemHarga.setCellValueFactory(c -> new SimpleStringProperty(UITheme.formatRupiah(c.getValue().getHarga())));
        colItemHarga.setPrefWidth(120);

        TableColumn<DetailTransaksi, String> colItemQty = new TableColumn<>("Qty");
        colItemQty.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getQty() + " cup/porsi"));
        colItemQty.setPrefWidth(100);

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

        ObservableList<Transaksi> listTrx = FXCollections.observableArrayList();
        tblTrx.setItems(listTrx);

        ObservableList<DetailTransaksi> listItems = FXCollections.observableArrayList();
        tblDetail.setItems(listItems);

        Runnable loadTrx = () -> {
            try {
                listTrx.setAll(transaksiService.getRiwayatTransaksi());
                listItems.clear();
                lblDetailTitle.setText("Rincian Barang Terjual (Klik baris transaksi)");
            } catch (Exception ex) {
                UITheme.showError("Error", "Gagal memuat riwayat: " + ex.getMessage());
            }
        };
        loadTrx.run();

        btnRefresh.setOnAction(e -> loadTrx.run());

        tblTrx.getSelectionModel().selectedItemProperty().addListener((obs, oldV, sel) -> {
            if (sel != null) {
                lblDetailTitle.setText("Rincian Barang Terjual - No Faktur: " + sel.getNoFaktur());
                listItems.setAll(sel.getItems());
            }
        });

        btnBukaPDF.setOnAction(e -> {
            Transaksi sel = tblTrx.getSelectionModel().getSelectedItem();
            if (sel == null) {
                UITheme.showWarning("Pilih Transaksi", "Pilih transaksi terlebih dahulu!");
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
