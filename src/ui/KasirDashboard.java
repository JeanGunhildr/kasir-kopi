package ui;

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

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Dashboard Antarmuka Kasir Operasional (POS).
 * Menangani transaksi pemesanan menu, kuantitas +1/-1, kode promo, template nominal uang,
 * konfirmasi pembayaran langsung, pencetakan struk PDF, dan riwayat transaksi dengan
 * rincian barang terjual (FR-05, FR-06, FR-08).
 * Menggunakan ContrastButton solid untuk menjamin 100% keterbacaan pada Windows.
 */
public class KasirDashboard extends JFrame {

    // Palet Warna Fintech Dashboard (High Contrast Solid Colors)
    public static final Color COLOR_PRIMARY_DARK = new Color(11, 59, 44);   // Deep Emerald Forest (#0B3B2C)
    public static final Color COLOR_SUCCESS = new Color(5, 150, 105);       // Solid Mint Green (#059669)
    public static final Color COLOR_SLATE_BTN = new Color(51, 65, 85);      // Slate Button (#334155)
    public static final Color COLOR_DANGER = new Color(220, 38, 38);        // Solid Crimson Red (#DC2626)
    public static final Color COLOR_AMBER = new Color(217, 119, 6);         // Amber Warning (#D97706)

    public static final Color FINTECH_CANVAS = new Color(241, 245, 249);    // Canvas Slate 100 (#F1F5F9)
    public static final Color FINTECH_CARD = Color.WHITE;                   // White Card Container
    public static final Color FINTECH_BORDER = new Color(203, 213, 225);    // Border Slate 300 (#CBD5E1)

    public static final Color COLOR_TEXT_MAIN = new Color(15, 23, 42);      // Dark Text (#0F172A)
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);  // Muted Text (#64748B)

    public static final Font FONT_LARGE_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    private final MenuService menuService = new MenuService();
    private final PromoService promoService = new PromoService();
    private final TransaksiService transaksiService = new TransaksiService();
    private final Pengguna currentUser;

    public static String formatRupiah(double nilai) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("id", "ID"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("Rp #,##0", symbols);
        return df.format(nilai);
    }

    public KasirDashboard(Pengguna user) {
        super("Pacbuck Coffee - Terminal Kasir POS (" + user.getNama() + ")");
        this.currentUser = user;
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1080, 700));
        setLocationRelativeTo(null);
        getContentPane().setBackground(FINTECH_CANVAS);
        setLayout(new BorderLayout());

        // Header Top Bar
        add(buatTopBar(), BorderLayout.NORTH);

        // Sidebar Kiri & Area Konten
        CardLayout cardLayout = new CardLayout();
        JPanel contentArea = new JPanel(cardLayout);
        contentArea.setBackground(FINTECH_CANVAS);
        contentArea.setBorder(new EmptyBorder(12, 12, 12, 12));

        contentArea.add(buatPanelPOS(), "POS");
        contentArea.add(buatPanelRiwayat(), "HISTORY");

        add(buatSidebar(cardLayout, contentArea), BorderLayout.WEST);
        add(contentArea, BorderLayout.CENTER);
    }

    private JPanel buatTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(new CompoundBorder(new LineBorder(FINTECH_BORDER, 1), new EmptyBorder(10, 20, 10, 20)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Pacbuck Coffee Terminal Kasir POS");
        lblTitle.setFont(FONT_LARGE_TITLE);
        lblTitle.setForeground(COLOR_PRIMARY_DARK);
        left.add(lblTitle);

        JLabel lblSubtitle = new JLabel("|  Sistem Transaksi Kasir Cepat & Cetak Struk");
        lblSubtitle.setFont(FONT_REGULAR);
        lblSubtitle.setForeground(COLOR_TEXT_MUTED);
        left.add(lblSubtitle);
        bar.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setBackground(Color.WHITE);

        JPanel userBadge = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        userBadge.setBackground(new Color(241, 245, 249));
        userBadge.setBorder(new LineBorder(FINTECH_BORDER, 1, true));

        JLabel lblUser = new JLabel("Kasir: " + currentUser.getNama());
        lblUser.setFont(FONT_BOLD);
        lblUser.setForeground(COLOR_TEXT_MAIN);
        userBadge.add(lblUser);
        right.add(userBadge);

        AdminDashboard.ContrastButton btnLogout = new AdminDashboard.ContrastButton("Logout", COLOR_DANGER, Color.WHITE);
        btnLogout.addActionListener(e -> {
            int tanya = JOptionPane.showConfirmDialog(this, "Keluar dari sesi kasir?", "Konfirmasi Logout", JOptionPane.YES_NO_OPTION);
            if (tanya == JOptionPane.YES_OPTION) {
                AuthService.getInstance().logout();
                new LoginFrame().setVisible(true);
                dispose();
            }
        });
        right.add(btnLogout);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    private JPanel buatSidebar(CardLayout cardLayout, JPanel contentArea) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(new CompoundBorder(new LineBorder(FINTECH_BORDER, 1), new EmptyBorder(18, 14, 18, 14)));
        sidebar.setPreferredSize(new Dimension(230, 0));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        brand.setBackground(Color.WHITE);
        JLabel lblLogo = new JLabel("☕");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brand.add(lblLogo);

        JLabel lblBrandName = new JLabel("Pacbuck Kasir");
        lblBrandName.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblBrandName.setForeground(COLOR_PRIMARY_DARK);
        brand.add(lblBrandName);

        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(20));

        AdminDashboard.ContrastButton btnPos = createSidebarButton("Transaksi Kasir", true);
        AdminDashboard.ContrastButton btnHist = createSidebarButton("Riwayat Transaksi", false);

        btnPos.addActionListener(e -> {
            cardLayout.show(contentArea, "POS");
            btnPos.setColors(COLOR_PRIMARY_DARK, Color.WHITE);
            btnHist.setColors(new Color(241, 245, 249), COLOR_TEXT_MAIN);
        });

        btnHist.addActionListener(e -> {
            cardLayout.show(contentArea, "HISTORY");
            btnHist.setColors(COLOR_PRIMARY_DARK, Color.WHITE);
            btnPos.setColors(new Color(241, 245, 249), COLOR_TEXT_MAIN);
        });

        sidebar.add(btnPos);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(btnHist);

        sidebar.add(Box.createVerticalGlue());

        // Mini Card
        JPanel miniCard = new JPanel();
        miniCard.setLayout(new BoxLayout(miniCard, BoxLayout.Y_AXIS));
        miniCard.setBackground(new Color(15, 23, 42));
        miniCard.setBorder(new EmptyBorder(12, 14, 12, 14));
        miniCard.setMaximumSize(new Dimension(200, 95));
        miniCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblCardTitle = new JLabel("Kasir Terminal");
        lblCardTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCardTitle.setForeground(Color.WHITE);
        miniCard.add(lblCardTitle);

        JLabel lblCardSub = new JLabel("Kelompok 4 PBO UNSOED");
        lblCardSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblCardSub.setForeground(new Color(148, 163, 184));
        miniCard.add(lblCardSub);

        miniCard.add(Box.createVerticalStrut(8));

        JLabel lblCardNumber = new JLabel("•••• 2026 POS");
        lblCardNumber.setFont(new Font("Consolas", Font.BOLD, 11));
        lblCardNumber.setForeground(new Color(203, 213, 225));
        miniCard.add(lblCardNumber);

        sidebar.add(miniCard);
        return sidebar;
    }

    private AdminDashboard.ContrastButton createSidebarButton(String text, boolean active) {
        AdminDashboard.ContrastButton btn = new AdminDashboard.ContrastButton(
                text,
                active ? COLOR_PRIMARY_DARK : new Color(241, 245, 249),
                active ? Color.WHITE : COLOR_TEXT_MAIN
        );
        btn.setFont(FONT_BOLD);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(202, 40));
        return btn;
    }

    private JPanel buatPanelPOS() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(FINTECH_CANVAS);

        Transaksi transaksi = new Transaksi(transaksiService.generateNoFaktur(), currentUser.getNama());
        final Promo[] appliedPromo = {null};

        // --- KOLOM KIRI (55%): KATALOG MENU ---
        JPanel pnlKiri = new JPanel(new BorderLayout(8, 8));
        pnlKiri.setBackground(FINTECH_CARD);
        pnlKiri.setBorder(new CompoundBorder(new LineBorder(FINTECH_BORDER, 1, true), new EmptyBorder(12, 14, 12, 14)));

        JPanel pnlHeaderMenu = new JPanel(new BorderLayout(8, 4));
        pnlHeaderMenu.setBackground(FINTECH_CARD);

        JPanel pnlFilterPills = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlFilterPills.setBackground(FINTECH_CARD);

        AdminDashboard.ContrastButton tabAll = AdminDashboard.createFilterPill("Semua", true);
        AdminDashboard.ContrastButton tabKopi = AdminDashboard.createFilterPill("Kopi", false);
        AdminDashboard.ContrastButton tabNonKopi = AdminDashboard.createFilterPill("Non-Kopi", false);
        AdminDashboard.ContrastButton tabMakanan = AdminDashboard.createFilterPill("Makanan", false);

        pnlFilterPills.add(tabAll);
        pnlFilterPills.add(tabKopi);
        pnlFilterPills.add(tabNonKopi);
        pnlFilterPills.add(tabMakanan);

        JTextField txtSearch = createPillTextField("");
        txtSearch.setPreferredSize(new Dimension(170, 32));
        txtSearch.setToolTipText("Cari nama menu...");

        pnlHeaderMenu.add(pnlFilterPills, BorderLayout.WEST);
        pnlHeaderMenu.add(txtSearch, BorderLayout.EAST);
        pnlKiri.add(pnlHeaderMenu, BorderLayout.NORTH);

        DefaultTableModel modelMenu = new DefaultTableModel(new String[]{"ID", "Kode", "Nama Menu", "Kategori", "Harga", "Stok"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tblMenu = new JTable(modelMenu);
        styleFintechTable(tblMenu);

        tblMenu.getColumnModel().getColumn(0).setPreferredWidth(35);
        tblMenu.getColumnModel().getColumn(1).setPreferredWidth(60);
        tblMenu.getColumnModel().getColumn(2).setPreferredWidth(160);
        tblMenu.getColumnModel().getColumn(3).setPreferredWidth(80);
        tblMenu.getColumnModel().getColumn(4).setPreferredWidth(90);
        tblMenu.getColumnModel().getColumn(5).setPreferredWidth(50);

        final String[] currentFilter = {"Semua"};
        Runnable loadMenuData = () -> {
            try {
                modelMenu.setRowCount(0);
                List<Menu> list = menuService.getDaftarMenu(currentFilter[0]);
                String query = txtSearch.getText().trim().toLowerCase();
                for (Menu m : list) {
                    if (query.isEmpty() || m.getNama().toLowerCase().contains(query) || m.getKode().toLowerCase().contains(query)) {
                        modelMenu.addRow(new Object[]{
                                m.getId(), m.getKode(), m.getNama(), m.getKategori(),
                                formatRupiah(m.getHarga()), m.getStok()
                        });
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
            filterTabs[i].addActionListener(e -> {
                currentFilter[0] = filterNames[idx];
                for (AdminDashboard.ContrastButton b : filterTabs) {
                    b.setColors(new Color(241, 245, 249), COLOR_TEXT_MAIN);
                }
                filterTabs[idx].setColors(COLOR_PRIMARY_DARK, Color.WHITE);
                loadMenuData.run();
            });
        }
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadMenuData.run(); }
            public void removeUpdate(DocumentEvent e) { loadMenuData.run(); }
            public void changedUpdate(DocumentEvent e) { loadMenuData.run(); }
        });

        pnlKiri.add(new JScrollPane(tblMenu), BorderLayout.CENTER);

        JPanel pnlBawahKiri = new JPanel(new BorderLayout());
        pnlBawahKiri.setBackground(FINTECH_CARD);
        AdminDashboard.ContrastButton btnPesanMenu = new AdminDashboard.ContrastButton("+ Tambahkan ke Keranjang", COLOR_PRIMARY_DARK, Color.WHITE);
        btnPesanMenu.setFont(FONT_BOLD);
        btnPesanMenu.setPreferredSize(new Dimension(0, 36));
        pnlBawahKiri.add(btnPesanMenu, BorderLayout.CENTER);
        pnlKiri.add(pnlBawahKiri, BorderLayout.SOUTH);

        // --- KOLOM KANAN (45%): KERANJANG & CHECKOUT ---
        JPanel pnlKanan = new JPanel(new BorderLayout(8, 8));
        pnlKanan.setBackground(FINTECH_CARD);
        pnlKanan.setBorder(new CompoundBorder(new LineBorder(FINTECH_BORDER, 1, true), new EmptyBorder(12, 14, 12, 14)));

        JPanel pnlHeaderCart = new JPanel(new BorderLayout());
        pnlHeaderCart.setBackground(FINTECH_CARD);
        JLabel lblFaktur = new JLabel("Faktur: " + transaksi.getNoFaktur());
        lblFaktur.setFont(FONT_TITLE);
        lblFaktur.setForeground(COLOR_PRIMARY_DARK);
        pnlHeaderCart.add(lblFaktur, BorderLayout.WEST);

        JLabel lblKasirInfo = new JLabel("Kasir: " + currentUser.getNama());
        lblKasirInfo.setFont(FONT_REGULAR);
        lblKasirInfo.setForeground(COLOR_TEXT_MUTED);
        pnlHeaderCart.add(lblKasirInfo, BorderLayout.EAST);
        pnlKanan.add(pnlHeaderCart, BorderLayout.NORTH);

        DefaultTableModel modelCart = new DefaultTableModel(new String[]{"ID", "Nama Menu", "Harga", "Qty", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tblCart = new JTable(modelCart);
        styleFintechTable(tblCart);

        tblCart.getColumnModel().getColumn(0).setPreferredWidth(30);
        tblCart.getColumnModel().getColumn(1).setPreferredWidth(140);
        tblCart.getColumnModel().getColumn(2).setPreferredWidth(75);
        tblCart.getColumnModel().getColumn(3).setPreferredWidth(45);
        tblCart.getColumnModel().getColumn(4).setPreferredWidth(85);

        // Action Toolbar Cart (+1, -1, Hapus, Kosongkan)
        JPanel pnlCartControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlCartControls.setBackground(FINTECH_CARD);

        AdminDashboard.ContrastButton btnPlus1 = new AdminDashboard.ContrastButton("+1", COLOR_SUCCESS, Color.WHITE);
        AdminDashboard.ContrastButton btnMinus1 = new AdminDashboard.ContrastButton("-1", COLOR_AMBER, Color.WHITE);
        AdminDashboard.ContrastButton btnHapus = new AdminDashboard.ContrastButton("Hapus", COLOR_DANGER, Color.WHITE);
        AdminDashboard.ContrastButton btnKosongkan = new AdminDashboard.ContrastButton("Reset", COLOR_SLATE_BTN, Color.WHITE);

        pnlCartControls.add(btnPlus1);
        pnlCartControls.add(btnMinus1);
        pnlCartControls.add(btnHapus);
        pnlCartControls.add(btnKosongkan);

        JPanel pnlCartCenter = new JPanel(new BorderLayout(4, 4));
        pnlCartCenter.setBackground(FINTECH_CARD);
        pnlCartCenter.add(new JScrollPane(tblCart), BorderLayout.CENTER);
        pnlCartCenter.add(pnlCartControls, BorderLayout.SOUTH);
        pnlKanan.add(pnlCartCenter, BorderLayout.CENTER);

        // Panel Pembayaran & Rincian
        JPanel pnlCheckout = new JPanel();
        pnlCheckout.setLayout(new BoxLayout(pnlCheckout, BoxLayout.Y_AXIS));
        pnlCheckout.setBackground(FINTECH_CARD);
        pnlCheckout.setBorder(new EmptyBorder(8, 0, 0, 0));

        // Promo Row
        JPanel pnlPromoRow = new JPanel(new BorderLayout(6, 0));
        pnlPromoRow.setBackground(FINTECH_CARD);
        JTextField txtKodePromo = createPillTextField("");
        txtKodePromo.setToolTipText("Masukkan kode promo...");
        AdminDashboard.ContrastButton btnTerapkanPromo = new AdminDashboard.ContrastButton("Terapkan", COLOR_PRIMARY_DARK, Color.WHITE);

        pnlPromoRow.add(new JLabel("Kode Promo: "), BorderLayout.WEST);
        pnlPromoRow.add(txtKodePromo, BorderLayout.CENTER);
        pnlPromoRow.add(btnTerapkanPromo, BorderLayout.EAST);
        pnlCheckout.add(pnlPromoRow);
        pnlCheckout.add(Box.createVerticalStrut(8));

        // Ringkasan Total
        JLabel lblSubtotalVal = new JLabel("Rp 0");
        JLabel lblDiskonVal = new JLabel("Rp 0");
        JLabel lblPajakVal = new JLabel("Rp 0");
        JLabel lblTotalVal = new JLabel("Rp 0");
        lblTotalVal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalVal.setForeground(COLOR_PRIMARY_DARK);

        pnlCheckout.add(buatBarisLabel("Subtotal Pesanan:", lblSubtotalVal));
        pnlCheckout.add(buatBarisLabel("Diskon Promo:", lblDiskonVal));
        pnlCheckout.add(buatBarisLabel("PPN (10%):", lblPajakVal));
        pnlCheckout.add(buatBarisLabel("Total Tagihan:", lblTotalVal));
        pnlCheckout.add(Box.createVerticalStrut(8));

        // Template Nominal Uang
        JPanel pnlQuickCash = new JPanel(new GridLayout(1, 5, 4, 0));
        pnlQuickCash.setBackground(FINTECH_CARD);
        AdminDashboard.ContrastButton btnUangPas = new AdminDashboard.ContrastButton("Pas", COLOR_PRIMARY_DARK, Color.WHITE);
        AdminDashboard.ContrastButton btnUang20k = new AdminDashboard.ContrastButton("20k", COLOR_SLATE_BTN, Color.WHITE);
        AdminDashboard.ContrastButton btnUang50k = new AdminDashboard.ContrastButton("50k", COLOR_SLATE_BTN, Color.WHITE);
        AdminDashboard.ContrastButton btnUang100k = new AdminDashboard.ContrastButton("100k", COLOR_SLATE_BTN, Color.WHITE);
        AdminDashboard.ContrastButton btnUang200k = new AdminDashboard.ContrastButton("200k", COLOR_SLATE_BTN, Color.WHITE);

        pnlQuickCash.add(btnUangPas);
        pnlQuickCash.add(btnUang20k);
        pnlQuickCash.add(btnUang50k);
        pnlQuickCash.add(btnUang100k);
        pnlQuickCash.add(btnUang200k);
        pnlCheckout.add(pnlQuickCash);
        pnlCheckout.add(Box.createVerticalStrut(8));

        // Input Bayar & Kembalian
        JPanel pnlBayarRow = new JPanel(new GridLayout(2, 2, 8, 4));
        pnlBayarRow.setBackground(FINTECH_CARD);
        JTextField txtBayar = createPillTextField("");
        JLabel lblKembalian = new JLabel("Rp 0");
        lblKembalian.setFont(FONT_BOLD);
        lblKembalian.setForeground(COLOR_SUCCESS);

        pnlBayarRow.add(new JLabel("Nominal Bayar:"));
        pnlBayarRow.add(txtBayar);
        pnlBayarRow.add(new JLabel("Kembalian:"));
        pnlBayarRow.add(lblKembalian);
        pnlCheckout.add(pnlBayarRow);
        pnlCheckout.add(Box.createVerticalStrut(10));

        // Tombol Bayar
        AdminDashboard.ContrastButton btnBayar = new AdminDashboard.ContrastButton("Bayar & Cetak Struk", COLOR_SUCCESS, Color.WHITE);
        btnBayar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBayar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        pnlCheckout.add(btnBayar);

        pnlKanan.add(pnlCheckout, BorderLayout.SOUTH);

        // Split Layout 55% / 45%
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pnlKiri, pnlKanan);
        splitPane.setResizeWeight(0.55);
        splitPane.setDividerSize(4);
        splitPane.setBackground(FINTECH_CANVAS);
        panel.add(splitPane, BorderLayout.CENTER);

        // Logic Helper
        Runnable refreshCart = () -> {
            modelCart.setRowCount(0);
            for (DetailTransaksi dt : transaksi.getItems()) {
                modelCart.addRow(new Object[]{
                        dt.getMenuId(), dt.getNamaMenu(), formatRupiah(dt.getHarga()),
                        dt.getQty(), formatRupiah(dt.getSubtotal())
                });
            }
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

        txtBayar.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshCart.run(); }
            public void removeUpdate(DocumentEvent e) { refreshCart.run(); }
            public void changedUpdate(DocumentEvent e) { refreshCart.run(); }
        });

        // Event Add Item
        Runnable doAddItem = () -> {
            int sel = tblMenu.getSelectedRow();
            if (sel < 0) return;
            int menuId = (int) modelMenu.getValueAt(sel, 0);
            String nama = (String) modelMenu.getValueAt(sel, 2);
            int stok = (int) modelMenu.getValueAt(sel, 5);

            if (stok <= 0) {
                JOptionPane.showMessageDialog(panel, "Stok menu [" + nama + "] sudah habis!", "Stok Habis", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                Menu m = menuService.getMenuById(menuId);
                transaksi.tambahItem(m, 1);
                refreshCart.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };

        btnPesanMenu.addActionListener(e -> doAddItem.run());
        tblMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) doAddItem.run();
            }
        });

        // Cart Actions
        btnPlus1.addActionListener(e -> {
            int sel = tblCart.getSelectedRow();
            if (sel < 0) return;
            int menuId = (int) modelCart.getValueAt(sel, 0);
            transaksi.ubahQtyItem(menuId, 1);
            refreshCart.run();
        });

        btnMinus1.addActionListener(e -> {
            int sel = tblCart.getSelectedRow();
            if (sel < 0) return;
            int menuId = (int) modelCart.getValueAt(sel, 0);
            transaksi.ubahQtyItem(menuId, -1);
            refreshCart.run();
        });

        btnHapus.addActionListener(e -> {
            int sel = tblCart.getSelectedRow();
            if (sel < 0) return;
            int menuId = (int) modelCart.getValueAt(sel, 0);
            transaksi.hapusItem(menuId);
            refreshCart.run();
        });

        btnKosongkan.addActionListener(e -> {
            transaksi.getItems().clear();
            appliedPromo[0] = null;
            txtKodePromo.setText("");
            txtBayar.setText("");
            refreshCart.run();
        });

        // Template Uang Cepat
        btnUangPas.addActionListener(e -> txtBayar.setText(String.valueOf((long) Math.ceil(transaksi.getTotal()))));
        btnUang20k.addActionListener(e -> txtBayar.setText("20000"));
        btnUang50k.addActionListener(e -> txtBayar.setText("50000"));
        btnUang100k.addActionListener(e -> txtBayar.setText("100000"));
        btnUang200k.addActionListener(e -> txtBayar.setText("200000"));

        btnTerapkanPromo.addActionListener(e -> {
            String kode = txtKodePromo.getText().trim();
            PromoService.PromoValidationResult res = promoService.validasiPromo(kode, transaksi.getSubtotal());
            if (res.valid) {
                appliedPromo[0] = res.promo;
                JOptionPane.showMessageDialog(panel, res.pesan, "Promo Berhasil", JOptionPane.INFORMATION_MESSAGE);
            } else {
                appliedPromo[0] = null;
                JOptionPane.showMessageDialog(panel, res.pesan, "Peringatan Promo", JOptionPane.WARNING_MESSAGE);
            }
            refreshCart.run();
        });

        btnBayar.addActionListener(e -> {
            if (transaksi.getItems().isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Keranjang pesanan masih kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double bayar;
            try {
                bayar = Double.parseDouble(txtBayar.getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Silakan pilih nominal uang atau ketik nominal bayar!", "Validasi Bayar", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (bayar < transaksi.getTotal()) {
                JOptionPane.showMessageDialog(panel, "Uang pembayaran kurang dari total tagihan!\nTotal: " + formatRupiah(transaksi.getTotal()), "Uang Kurang", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double kembalian = bayar - transaksi.getTotal();
            int confirm = JOptionPane.showConfirmDialog(panel,
                    "Konfirmasi Pembayaran Transaksi:\n\n" +
                            "• No Faktur   : " + transaksi.getNoFaktur() + "\n" +
                            "• Kasir       : " + transaksi.getKasirNama() + "\n" +
                            "• Total Bayar : " + formatRupiah(transaksi.getTotal()) + "\n" +
                            "• Tunai       : " + formatRupiah(bayar) + "\n" +
                            "• Kembalian   : " + formatRupiah(kembalian) + "\n\n" +
                            "Proses transaksi dan cetak struk sekarang?",
                    "Konfirmasi Pembayaran", JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) return;

            transaksi.setBayar(bayar);
            transaksi.setKembalian(kembalian);

            try {
                boolean sukses = transaksiService.prosesTransaksi(transaksi);
                if (sukses) {
                    File fileStruk = transaksiService.cetakStruk(transaksi);
                    PDFUtil.bukaFile(fileStruk);

                    JOptionPane.showMessageDialog(panel,
                            "Transaksi Berhasil Diproses!\n" +
                                    "Struk resmi telah dicetak dan disimpan di:\n" + fileStruk.getAbsolutePath(),
                            "Pembayaran Sukses", JOptionPane.INFORMATION_MESSAGE);

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
                JOptionPane.showMessageDialog(panel, "Gagal memproses transaksi: " + ex.getMessage(), "Error Transaksi", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private JPanel buatBarisLabel(String label, JLabel valLabel) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(FINTECH_CARD);
        JLabel l = new JLabel(label);
        l.setFont(FONT_REGULAR);
        l.setForeground(COLOR_TEXT_MAIN);
        pnl.add(l, BorderLayout.WEST);
        pnl.add(valLabel, BorderLayout.EAST);
        return pnl;
    }

    // =========================================================================
    // PANEL 2: RIWAYAT TRANSAKSI & RINCIAN BARANG TERJUAL (MASTER-DETAIL)
    // =========================================================================
    private JPanel buatPanelRiwayat() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(FINTECH_CANVAS);

        // Master (Tabel Transaksi)
        JPanel pnlMaster = new JPanel(new BorderLayout(8, 8));
        pnlMaster.setBackground(FINTECH_CARD);
        pnlMaster.setBorder(new CompoundBorder(new LineBorder(FINTECH_BORDER, 1, true), new EmptyBorder(14, 16, 14, 16)));

        JPanel topMaster = new JPanel(new BorderLayout());
        topMaster.setBackground(FINTECH_CARD);

        JLabel lblTitle = new JLabel("Riwayat Transaksi Penjualan Toko");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(COLOR_TEXT_MAIN);
        topMaster.add(lblTitle, BorderLayout.WEST);

        AdminDashboard.ContrastButton btnRefresh = new AdminDashboard.ContrastButton("Refresh Riwayat", COLOR_SLATE_BTN, Color.WHITE);
        topMaster.add(btnRefresh, BorderLayout.EAST);
        pnlMaster.add(topMaster, BorderLayout.NORTH);

        DefaultTableModel modelTrx = new DefaultTableModel(new String[]{"ID", "No Faktur", "Kasir", "Subtotal", "Diskon", "PPN (10%)", "Total Tagihan", "Nominal Bayar", "Kembalian"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tblTrx = new JTable(modelTrx);
        styleFintechTable(tblTrx);
        pnlMaster.add(new JScrollPane(tblTrx), BorderLayout.CENTER);

        // Detail (Tabel Rincian Barang Terjual)
        JPanel pnlDetail = new JPanel(new BorderLayout(8, 8));
        pnlDetail.setBackground(FINTECH_CARD);
        pnlDetail.setBorder(new CompoundBorder(new LineBorder(FINTECH_BORDER, 1, true), new EmptyBorder(14, 16, 14, 16)));

        JPanel topDetail = new JPanel(new BorderLayout());
        topDetail.setBackground(FINTECH_CARD);

        JLabel lblDetailTitle = new JLabel("Rincian Barang Terjual (Klik baris transaksi di atas untuk melihat detail)");
        lblDetailTitle.setFont(FONT_TITLE);
        lblDetailTitle.setForeground(COLOR_PRIMARY_DARK);
        topDetail.add(lblDetailTitle, BorderLayout.WEST);

        JPanel pnlDetailActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlDetailActions.setBackground(FINTECH_CARD);

        AdminDashboard.ContrastButton btnBukaPDF = new AdminDashboard.ContrastButton("Buka Dokumen Struk (PDF)", COLOR_PRIMARY_DARK, Color.WHITE);
        pnlDetailActions.add(btnBukaPDF);
        topDetail.add(pnlDetailActions, BorderLayout.EAST);
        pnlDetail.add(topDetail, BorderLayout.NORTH);

        DefaultTableModel modelItems = new DefaultTableModel(new String[]{"No", "Nama Menu / Minuman", "Harga Satuan", "Qty Terjual", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tblItems = new JTable(modelItems);
        styleFintechTable(tblItems);
        pnlDetail.add(new JScrollPane(tblItems), BorderLayout.CENTER);

        // State Cache
        final List<Transaksi>[] cache = new List[]{new ArrayList<>()};

        Runnable loadTrx = () -> {
            try {
                modelTrx.setRowCount(0);
                modelItems.setRowCount(0);
                lblDetailTitle.setText("Rincian Barang Terjual (Klik baris transaksi di atas untuk melihat detail)");
                cache[0] = transaksiService.getRiwayatTransaksi();
                for (Transaksi t : cache[0]) {
                    modelTrx.addRow(new Object[]{
                            t.getId(), t.getNoFaktur(), t.getKasirNama(), formatRupiah(t.getSubtotal()),
                            formatRupiah(t.getDiskon()), formatRupiah(t.getPajak()), formatRupiah(t.getTotal()),
                            formatRupiah(t.getBayar()), formatRupiah(t.getKembalian())
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Gagal memuat riwayat: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        };
        loadTrx.run();

        // Event Klik Baris Transaksi -> Menampilkan Detail Barang Terjual
        tblTrx.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int sel = tblTrx.getSelectedRow();
                if (sel >= 0 && sel < cache[0].size()) {
                    Transaksi t = cache[0].get(sel);
                    lblDetailTitle.setText("Rincian Barang Terjual - No Faktur: " + t.getNoFaktur() + " (Kasir: " + t.getKasirNama() + ")");
                    modelItems.setRowCount(0);

                    int no = 1;
                    for (DetailTransaksi dt : t.getItems()) {
                        modelItems.addRow(new Object[]{
                                no++, dt.getNamaMenu(), formatRupiah(dt.getHarga()),
                                dt.getQty() + " cup/porsi", formatRupiah(dt.getSubtotal())
                        });
                    }

                    if (t.getItems().isEmpty()) {
                        modelItems.addRow(new Object[]{"-", "(Tidak ada rincian item)", "-", "-", "-"});
                    }
                }
            }
        });

        btnRefresh.addActionListener(e -> loadTrx.run());

        btnBukaPDF.addActionListener(e -> {
            int sel = tblTrx.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(panel, "Pilih salah satu baris transaksi terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (sel < cache[0].size()) {
                Transaksi t = cache[0].get(sel);
                File file = transaksiService.cetakStruk(t);
                PDFUtil.bukaFile(file);
            }
        });

        // Split Master-Detail Vertikal
        JSplitPane splitRiwayat = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pnlMaster, pnlDetail);
        splitRiwayat.setResizeWeight(0.52);
        splitRiwayat.setDividerSize(6);
        splitRiwayat.setBackground(FINTECH_CANVAS);

        panel.add(splitRiwayat, BorderLayout.CENTER);
        return panel;
    }

    public static void styleFintechTable(JTable table) {
        table.setFont(FONT_REGULAR);
        table.setRowHeight(32);
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(209, 250, 229));
        table.setSelectionForeground(COLOR_TEXT_MAIN);
        table.getTableHeader().setFont(FONT_BOLD);
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(COLOR_TEXT_MAIN);
        table.getTableHeader().setPreferredSize(new Dimension(0, 34));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
    }

    public static JTextField createPillTextField(String text) {
        JTextField tf = new JTextField(text);
        tf.setFont(FONT_REGULAR);
        tf.setBorder(new CompoundBorder(new LineBorder(FINTECH_BORDER, 1, true), new EmptyBorder(5, 10, 5, 10)));
        return tf;
    }
}
