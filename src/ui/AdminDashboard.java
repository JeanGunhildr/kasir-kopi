package ui;

import model.Admin;
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

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Dashboard Antarmuka Administrator (Admin).
 * Menyediakan transaksi kasir terintegrasi (POS), pengelolaan Menu & Stok (FR-01, FR-03),
 * Akun Kasir (FR-02), Kode Promo & Diskon (FR-07), Laporan Penjualan (FR-04),
 * dan Riwayat Transaksi lengkap dengan Rincian Barang Terjual (Master-Detail).
 * Didesain dengan estetika Pacbuck Fintech Dashboard dan tombol kustom berkontras tinggi.
 */
public class AdminDashboard extends JFrame {

    // Palet Warna Fintech Dashboard (High Contrast Solid Colors)
    public static final Color COLOR_PRIMARY_DARK = new Color(11, 59, 44);   // Deep Emerald Forest (#0B3B2C)
    public static final Color COLOR_SUCCESS = new Color(5, 150, 105);       // Solid Mint Green (#059669)
    public static final Color COLOR_SLATE_BTN = new Color(51, 65, 85);      // Slate Button (#334155)
    public static final Color COLOR_DANGER = new Color(220, 38, 38);        // Solid Crimson Red (#DC2626)
    public static final Color COLOR_AMBER = new Color(217, 119, 6);         // Amber Warning (#D97706)

    public static final Color FINTECH_CANVAS = new Color(241, 245, 249);    // Canvas Slate 100 (#F1F5F9)
    public static final Color FINTECH_CARD = Color.WHITE;                   // White Card Container
    public static final Color FINTECH_BORDER = new Color(203, 213, 225);    // Border Slate 300 (#CBD5E1)

    public static final Color COLOR_TEXT_MAIN = new Color(15, 23, 42);      // Dark Slate (#0F172A)
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);  // Muted Slate (#64748B)

    public static final Font FONT_LARGE_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_HERO_NUM = new Font("Segoe UI", Font.BOLD, 24);

    private final MenuService menuService = new MenuService();
    private final AuthService authService = AuthService.getInstance();
    private final PromoService promoService = new PromoService();
    private final LaporanService laporanService = new LaporanService();
    private final TransaksiService transaksiService = new TransaksiService();
    private final Pengguna currentUser;

    public static String formatRupiah(double nilai) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("id", "ID"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("Rp #,##0", symbols);
        return df.format(nilai);
    }

    /**
     * Komponen Tombol Kustom Berkontras Tinggi (ContrastButton).
     * Mencegah bug Windows Look-and-Feel yang menimpa warna latar belakang menjadi putih
     * dan memastikan teks selalu terbaca jelas (100% High Contrast).
     */
    public static class ContrastButton extends JButton {
        private Color normalBg;
        private Color hoverBg;
        private Color pressedBg;
        private Color normalFg;
        private int cornerRadius = 6;

        public ContrastButton(String text, Color bg, Color fg) {
            super(text);
            this.normalBg = bg;
            this.normalFg = fg;
            this.hoverBg = adjustBrightness(bg, 1.18f);
            this.pressedBg = adjustBrightness(bg, 0.82f);

            setOpaque(false);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            setForeground(fg);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 14, 8, 14));
        }

        private static Color adjustBrightness(Color c, float factor) {
            int r = Math.min(255, Math.max(0, (int) (c.getRed() * factor)));
            int g = Math.min(255, Math.max(0, (int) (c.getGreen() * factor)));
            int b = Math.min(255, Math.max(0, (int) (c.getBlue() * factor)));
            return new Color(r, g, b);
        }

        public void setColors(Color bg, Color fg) {
            this.normalBg = bg;
            this.normalFg = fg;
            this.hoverBg = adjustBrightness(bg, 1.18f);
            this.pressedBg = adjustBrightness(bg, 0.82f);
            setForeground(fg);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Color currentBg;
            if (!isEnabled()) {
                currentBg = new Color(203, 213, 225);
            } else if (getModel().isPressed()) {
                currentBg = pressedBg;
            } else if (getModel().isRollover()) {
                currentBg = hoverBg;
            } else {
                currentBg = normalBg;
            }

            g2.setColor(currentBg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

            FontMetrics fm = g2.getFontMetrics(getFont());
            Rectangle2D bounds = fm.getStringBounds(getText(), g2);
            int textX = (getWidth() - (int) bounds.getWidth()) / 2;
            int textY = (getHeight() - (int) bounds.getHeight()) / 2 + fm.getAscent() - 1;

            Color currentFg = isEnabled() ? normalFg : new Color(148, 163, 184);
            g2.setFont(getFont());
            g2.setColor(currentFg);
            g2.drawString(getText(), textX, textY);

            g2.dispose();
        }
    }

    public AdminDashboard(Pengguna user) {
        super("Pacbuck Coffee - Panel Administrator (" + user.getNama() + ")");
        this.currentUser = user;
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1320, 840);
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);
        getContentPane().setBackground(FINTECH_CANVAS);
        setLayout(new BorderLayout());

        // Header Top Bar
        add(buatTopBar(), BorderLayout.NORTH);

        // Content Area CardLayout
        CardLayout cardLayout = new CardLayout();
        JPanel contentArea = new JPanel(cardLayout);
        contentArea.setBackground(FINTECH_CANVAS);
        contentArea.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Tab-tab sistem (Admin juga memiliki akses Transaksi Kasir POS!)
        contentArea.add(buatPanelPOS(), "POS");
        contentArea.add(buatPanelMenu(), "MENU");
        contentArea.add(buatPanelKasir(), "KASIR");
        contentArea.add(buatPanelPromo(), "PROMO");
        contentArea.add(buatPanelLaporan(), "LAPORAN");
        contentArea.add(buatPanelRiwayat(), "RIWAYAT");

        add(buatSidebar(cardLayout, contentArea), BorderLayout.WEST);
        add(contentArea, BorderLayout.CENTER);
    }

    private JPanel buatTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(new CompoundBorder(new LineBorder(FINTECH_BORDER, 1), new EmptyBorder(12, 20, 12, 20)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Pacbuck Coffee Management System");
        lblTitle.setFont(FONT_LARGE_TITLE);
        lblTitle.setForeground(COLOR_PRIMARY_DARK);
        left.add(lblTitle);

        JLabel lblSubtitle = new JLabel("|  Portal Eksekutif & Administrator Kedai Kopi");
        lblSubtitle.setFont(FONT_REGULAR);
        lblSubtitle.setForeground(COLOR_TEXT_MUTED);
        left.add(lblSubtitle);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setBackground(Color.WHITE);

        // User Profile Badge
        JPanel userBadge = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        userBadge.setBackground(new Color(241, 245, 249));
        userBadge.setBorder(new LineBorder(FINTECH_BORDER, 1, true));

        JLabel lblUser = new JLabel("Admin: " + currentUser.getNama());
        lblUser.setFont(FONT_BOLD);
        lblUser.setForeground(COLOR_TEXT_MAIN);
        userBadge.add(lblUser);
        right.add(userBadge);

        ContrastButton btnLogout = new ContrastButton("Logout", COLOR_DANGER, Color.WHITE);
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Apakah Anda yakin ingin logout dari sistem?", "Konfirmasi Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                authService.logout();
                new LoginFrame().setVisible(true);
                dispose();
            }
        });
        right.add(btnLogout);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buatSidebar(CardLayout cardLayout, JPanel contentArea) {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(new CompoundBorder(new LineBorder(FINTECH_BORDER, 1), new EmptyBorder(18, 14, 18, 14)));
        sidebar.setPreferredSize(new Dimension(240, 0));

        // Brand Logo
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        brand.setBackground(Color.WHITE);
        JLabel lblLogo = new JLabel("☕");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brand.add(lblLogo);

        JLabel lblBrandName = new JLabel("Pacbuck Admin");
        lblBrandName.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblBrandName.setForeground(COLOR_PRIMARY_DARK);
        brand.add(lblBrandName);

        sidebar.add(brand);
        sidebar.add(Box.createVerticalStrut(20));

        // Tombol Navigasi Sidebar (Kontras Tinggi)
        ContrastButton btnPos = createSidebarButton("Transaksi Kasir (POS)", true);
        ContrastButton btnMenu = createSidebarButton("Kelola Menu & Stok", false);
        ContrastButton btnKasir = createSidebarButton("Kelola Akun Kasir", false);
        ContrastButton btnPromo = createSidebarButton("Kelola Promo & Diskon", false);
        ContrastButton btnLaporan = createSidebarButton("Laporan Penjualan", false);
        ContrastButton btnRiwayat = createSidebarButton("Riwayat Transaksi", false);

        ContrastButton[] buttons = {btnPos, btnMenu, btnKasir, btnPromo, btnLaporan, btnRiwayat};
        String[] keys = {"POS", "MENU", "KASIR", "PROMO", "LAPORAN", "RIWAYAT"};

        for (int i = 0; i < buttons.length; i++) {
            final int idx = i;
            buttons[i].addActionListener(e -> {
                cardLayout.show(contentArea, keys[idx]);
                for (ContrastButton b : buttons) {
                    b.setColors(new Color(241, 245, 249), COLOR_TEXT_MAIN);
                }
                buttons[idx].setColors(COLOR_PRIMARY_DARK, Color.WHITE);
            });
            sidebar.add(buttons[i]);
            sidebar.add(Box.createVerticalStrut(6));
        }

        sidebar.add(Box.createVerticalGlue());

        // Mini Card Info
        JPanel miniCard = new JPanel();
        miniCard.setLayout(new BoxLayout(miniCard, BoxLayout.Y_AXIS));
        miniCard.setBackground(new Color(15, 23, 42));
        miniCard.setBorder(new EmptyBorder(12, 14, 12, 14));
        miniCard.setMaximumSize(new Dimension(212, 95));
        miniCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblCardTitle = new JLabel("Pacbuck Enterprise");
        lblCardTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblCardTitle.setForeground(Color.WHITE);
        miniCard.add(lblCardTitle);

        JLabel lblCardSub = new JLabel("Kelompok 4 PBO UNSOED");
        lblCardSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblCardSub.setForeground(new Color(148, 163, 184));
        miniCard.add(lblCardSub);

        miniCard.add(Box.createVerticalStrut(8));

        JLabel lblCardNumber = new JLabel("•••• 2026 ADMIN");
        lblCardNumber.setFont(new Font("Consolas", Font.BOLD, 11));
        lblCardNumber.setForeground(new Color(203, 213, 225));
        miniCard.add(lblCardNumber);

        sidebar.add(miniCard);
        return sidebar;
    }

    private ContrastButton createSidebarButton(String text, boolean active) {
        ContrastButton btn = new ContrastButton(
                text,
                active ? COLOR_PRIMARY_DARK : new Color(241, 245, 249),
                active ? Color.WHITE : COLOR_TEXT_MAIN
        );
        btn.setFont(FONT_BOLD);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(212, 40));
        return btn;
    }

    // =========================================================================
    // PANEL 1: TRANSAKSI KASIR (POS) UNTUK ADMIN
    // =========================================================================
    private JPanel buatPanelPOS() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(FINTECH_CANVAS);

        Transaksi transaksi = new Transaksi(transaksiService.generateNoFaktur(), "Admin: " + currentUser.getNama());
        final Promo[] appliedPromo = {null};

        // --- KOLOM KIRI (55%): KATALOG MENU ---
        JPanel pnlKiri = new JPanel(new BorderLayout(8, 8));
        pnlKiri.setBackground(FINTECH_CARD);
        pnlKiri.setBorder(new CompoundBorder(new LineBorder(FINTECH_BORDER, 1, true), new EmptyBorder(12, 14, 12, 14)));

        JPanel pnlHeaderMenu = new JPanel(new BorderLayout(8, 4));
        pnlHeaderMenu.setBackground(FINTECH_CARD);

        JPanel pnlFilterPills = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlFilterPills.setBackground(FINTECH_CARD);

        ContrastButton tabAll = createFilterPill("Semua", true);
        ContrastButton tabKopi = createFilterPill("Kopi", false);
        ContrastButton tabNonKopi = createFilterPill("Non-Kopi", false);
        ContrastButton tabMakanan = createFilterPill("Makanan", false);

        pnlFilterPills.add(tabAll);
        pnlFilterPills.add(tabKopi);
        pnlFilterPills.add(tabNonKopi);
        pnlFilterPills.add(tabMakanan);

        JTextField txtSearch = createPillTextField("");
        txtSearch.setPreferredSize(new Dimension(180, 32));
        txtSearch.setToolTipText("Cari nama menu kopi...");

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

        ContrastButton[] filterTabs = {tabAll, tabKopi, tabNonKopi, tabMakanan};
        String[] filterNames = {"Semua", "Kopi", "Non-Kopi", "Makanan"};
        for (int i = 0; i < filterTabs.length; i++) {
            final int idx = i;
            filterTabs[i].addActionListener(e -> {
                currentFilter[0] = filterNames[idx];
                for (ContrastButton b : filterTabs) {
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
        ContrastButton btnPesanMenu = new ContrastButton("+ Tambahkan ke Keranjang", COLOR_PRIMARY_DARK, Color.WHITE);
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

        JLabel lblKasirInfo = new JLabel("Operator: " + currentUser.getNama());
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

        ContrastButton btnPlus1 = new ContrastButton("+1", COLOR_SUCCESS, Color.WHITE);
        ContrastButton btnMinus1 = new ContrastButton("-1", COLOR_AMBER, Color.WHITE);
        ContrastButton btnHapus = new ContrastButton("Hapus", COLOR_DANGER, Color.WHITE);
        ContrastButton btnKosongkan = new ContrastButton("Reset", COLOR_SLATE_BTN, Color.WHITE);

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
        ContrastButton btnTerapkanPromo = new ContrastButton("Terapkan", COLOR_PRIMARY_DARK, Color.WHITE);

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
        ContrastButton btnUangPas = new ContrastButton("Pas", COLOR_PRIMARY_DARK, Color.WHITE);
        ContrastButton btnUang20k = new ContrastButton("20k", COLOR_SLATE_BTN, Color.WHITE);
        ContrastButton btnUang50k = new ContrastButton("50k", COLOR_SLATE_BTN, Color.WHITE);
        ContrastButton btnUang100k = new ContrastButton("100k", COLOR_SLATE_BTN, Color.WHITE);
        ContrastButton btnUang200k = new ContrastButton("200k", COLOR_SLATE_BTN, Color.WHITE);

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
        ContrastButton btnBayar = new ContrastButton("Bayar & Cetak Struk", COLOR_SUCCESS, Color.WHITE);
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
    // PANEL 2: KELOLA MENU & STOK (FR-01, FR-03)
    // =========================================================================
    private JPanel buatPanelMenu() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(FINTECH_CARD);
        panel.setBorder(new CompoundBorder(new LineBorder(FINTECH_BORDER, 1, true), new EmptyBorder(16, 16, 16, 16)));

        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Kode", "Nama Menu", "Kategori", "Harga", "Stok", "Keterangan Rasa/Sajian"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(model);
        styleFintechTable(table);

        final String[] currentFilter = {"Semua"};

        Runnable loadMenu = () -> {
            try {
                model.setRowCount(0);
                List<Menu> list = menuService.getDaftarMenu(currentFilter[0]);
                for (Menu m : list) {
                    model.addRow(new Object[]{
                            m.getId(), m.getKode(), m.getNama(), m.getKategori(),
                            formatRupiah(m.getHarga()), m.getStok(), m.getJenisPenyajian()
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Gagal memuat daftar menu: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        };
        loadMenu.run();

        // Header and Toolbar
        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setBackground(FINTECH_CARD);

        JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlTitle.setBackground(FINTECH_CARD);
        JLabel lblTitle = new JLabel("Katalog Menu & Manajemen Stok Real-Time (FR-01, FR-03)");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(COLOR_TEXT_MAIN);
        pnlTitle.add(lblTitle);

        // Filter Pills
        JPanel pnlFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlFilters.setBackground(FINTECH_CARD);

        ContrastButton tabAll = createFilterPill("Semua", true);
        ContrastButton tabKopi = createFilterPill("Kopi", false);
        ContrastButton tabNonKopi = createFilterPill("Non-Kopi", false);
        ContrastButton tabMakanan = createFilterPill("Makanan", false);

        ContrastButton[] filterButtons = {tabAll, tabKopi, tabNonKopi, tabMakanan};
        String[] categories = {"Semua", "Kopi", "Non-Kopi", "Makanan"};

        for (int i = 0; i < filterButtons.length; i++) {
            final int idx = i;
            filterButtons[i].addActionListener(e -> {
                currentFilter[0] = categories[idx];
                for (ContrastButton b : filterButtons) {
                    b.setColors(new Color(241, 245, 249), COLOR_TEXT_MAIN);
                }
                filterButtons[idx].setColors(COLOR_PRIMARY_DARK, Color.WHITE);
                loadMenu.run();
            });
            pnlFilters.add(filterButtons[i]);
        }

        JPanel pnlHeaderLeft = new JPanel();
        pnlHeaderLeft.setLayout(new BoxLayout(pnlHeaderLeft, BoxLayout.Y_AXIS));
        pnlHeaderLeft.setBackground(FINTECH_CARD);
        pnlHeaderLeft.add(pnlTitle);
        pnlHeaderLeft.add(Box.createVerticalStrut(8));
        pnlHeaderLeft.add(pnlFilters);

        // Action Buttons Toolbar (Kontras Tinggi)
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setBackground(FINTECH_CARD);

        ContrastButton btnTambah = new ContrastButton("+ Tambah Menu", COLOR_SUCCESS, Color.WHITE);
        ContrastButton btnEdit = new ContrastButton("Edit Menu", COLOR_SLATE_BTN, Color.WHITE);
        ContrastButton btnStok = new ContrastButton("Update Stok", COLOR_PRIMARY_DARK, Color.WHITE);
        ContrastButton btnHapus = new ContrastButton("Hapus Menu", COLOR_DANGER, Color.WHITE);
        ContrastButton btnRefresh = new ContrastButton("Refresh", COLOR_SLATE_BTN, Color.WHITE);

        toolbar.add(btnTambah);
        toolbar.add(btnEdit);
        toolbar.add(btnStok);
        toolbar.add(btnHapus);
        toolbar.add(btnRefresh);

        top.add(pnlHeaderLeft, BorderLayout.WEST);
        top.add(toolbar, BorderLayout.EAST);

        btnRefresh.addActionListener(e -> loadMenu.run());
        btnTambah.addActionListener(e -> tampilkanFormMenu(null, panel, loadMenu));

        btnEdit.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(panel, "Pilih menu pada tabel yang ingin diedit!", "Perhatian", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) model.getValueAt(sel, 0);
            try {
                Menu m = menuService.getMenuById(id);
                if (m != null) tampilkanFormMenu(m, panel, loadMenu);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Gagal mengambil data menu: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnStok.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(panel, "Pilih menu pada tabel yang ingin diperbarui stoknya!", "Perhatian", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) model.getValueAt(sel, 0);
            String nama = (String) model.getValueAt(sel, 2);
            int stokSekarang = (int) model.getValueAt(sel, 5);

            String input = JOptionPane.showInputDialog(panel, "Masukkan jumlah stok baru untuk [" + nama + "]:", stokSekarang);
            if (input != null && !input.trim().isEmpty()) {
                try {
                    int stokBaru = Integer.parseInt(input.trim());
                    if (stokBaru < 0) {
                        JOptionPane.showMessageDialog(panel, "Stok tidak boleh negatif!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    menuService.updateStok(id, stokBaru);
                    JOptionPane.showMessageDialog(panel, "Stok " + nama + " berhasil diperbarui menjadi " + stokBaru + " unit.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    loadMenu.run();
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(panel, "Format angka stok tidak valid!", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, "Gagal update stok: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnHapus.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(panel, "Pilih menu pada tabel yang ingin dihapus!", "Perhatian", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) model.getValueAt(sel, 0);
            String nama = (String) model.getValueAt(sel, 2);

            int konfirmasi = JOptionPane.showConfirmDialog(panel, "Yakin ingin menghapus menu '" + nama + "'?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
            if (konfirmasi == JOptionPane.YES_OPTION) {
                try {
                    menuService.hapusMenu(id);
                    JOptionPane.showMessageDialog(panel, "Menu berhasil dihapus.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    loadMenu.run();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, "Gagal menghapus menu: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private void tampilkanFormMenu(Menu editMenu, JPanel parent, Runnable onSaved) {
        boolean isEdit = editMenu != null;
        JDialog dialog = new JDialog(this, isEdit ? "Edit Data Menu" : "Tambah Menu Baru", true);
        dialog.setSize(440, 380);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 12));
        form.setBorder(new EmptyBorder(16, 20, 16, 20));
        form.setBackground(Color.WHITE);

        JTextField txtKode = createPillTextField(isEdit ? editMenu.getKode() : "CF" + (System.currentTimeMillis() % 1000));
        JTextField txtNama = createPillTextField(isEdit ? editMenu.getNama() : "");
        JComboBox<String> cmbKategori = new JComboBox<>(new String[]{"Kopi", "Non-Kopi", "Makanan"});
        if (isEdit) cmbKategori.setSelectedItem(editMenu.getKategori());
        JTextField txtHarga = createPillTextField(isEdit ? String.valueOf((long) editMenu.getHarga()) : "");
        JTextField txtStok = createPillTextField(isEdit ? String.valueOf(editMenu.getStok()) : "50");

        form.add(new JLabel("Kode Menu:"));
        form.add(txtKode);
        form.add(new JLabel("Nama Menu:"));
        form.add(txtNama);
        form.add(new JLabel("Kategori:"));
        form.add(cmbKategori);
        form.add(new JLabel("Harga (Rp):"));
        form.add(txtHarga);
        form.add(new JLabel("Stok Awal:"));
        form.add(txtStok);

        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        pnlBtn.setBackground(new Color(241, 245, 249));

        ContrastButton btnBatal = new ContrastButton("Batal", COLOR_SLATE_BTN, Color.WHITE);
        btnBatal.addActionListener(e -> dialog.dispose());

        ContrastButton btnSimpan = new ContrastButton("Simpan Data", COLOR_SUCCESS, Color.WHITE);
        btnSimpan.addActionListener(e -> {
            try {
                String kode = txtKode.getText().trim();
                String nama = txtNama.getText().trim();
                String kategori = (String) cmbKategori.getSelectedItem();
                double harga = Double.parseDouble(txtHarga.getText().trim());
                int stok = Integer.parseInt(txtStok.getText().trim());

                if (nama.isEmpty() || kode.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Nama dan kode menu tidak boleh kosong!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Menu m = new Menu(isEdit ? editMenu.getId() : 0, kode, nama, kategori, harga, stok);
                menuService.simpanMenu(m);

                JOptionPane.showMessageDialog(dialog, "Data menu berhasil disimpan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                onSaved.run();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "Format angka harga atau stok tidak valid!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Gagal menyimpan menu: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        pnlBtn.add(btnBatal);
        pnlBtn.add(btnSimpan);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(pnlBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // =========================================================================
    // PANEL 3: KELOLA KASIR (FR-02)
    // =========================================================================
    private JPanel buatPanelKasir() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(FINTECH_CARD);
        panel.setBorder(new CompoundBorder(new LineBorder(FINTECH_BORDER, 1, true), new EmptyBorder(16, 16, 16, 16)));

        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Username", "Nama Kasir", "Status Akun", "Hak Akses"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(model);
        styleFintechTable(table);

        Runnable loadKasir = () -> {
            try {
                model.setRowCount(0);
                List<Pengguna> list = authService.getDaftarKasir();
                for (Pengguna u : list) {
                    model.addRow(new Object[]{
                            u.getId(), u.getUsername(), u.getNama(), u.getStatus(), u.getDeskripsiAkses()
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Gagal memuat kasir: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        };
        loadKasir.run();

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(FINTECH_CARD);

        JLabel lblTitle = new JLabel("Manajemen Akun Kasir Operasional (FR-02)");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(COLOR_TEXT_MAIN);
        top.add(lblTitle, BorderLayout.WEST);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setBackground(FINTECH_CARD);

        ContrastButton btnTambah = new ContrastButton("+ Tambah Kasir Baru", COLOR_SUCCESS, Color.WHITE);
        ContrastButton btnToggleStatus = new ContrastButton("Toggle Status (Aktif/Nonaktif)", COLOR_PRIMARY_DARK, Color.WHITE);
        ContrastButton btnResetPwd = new ContrastButton("Reset Password", COLOR_SLATE_BTN, Color.WHITE);
        ContrastButton btnRefresh = new ContrastButton("Refresh", COLOR_SLATE_BTN, Color.WHITE);

        toolbar.add(btnTambah);
        toolbar.add(btnToggleStatus);
        toolbar.add(btnResetPwd);
        toolbar.add(btnRefresh);

        top.add(toolbar, BorderLayout.EAST);

        btnRefresh.addActionListener(e -> loadKasir.run());

        btnTambah.addActionListener(e -> {
            JDialog dialog = new JDialog(this, "Tambah Kasir Baru", true);
            dialog.setSize(400, 280);
            dialog.setLocationRelativeTo(panel);
            dialog.setLayout(new BorderLayout());

            JPanel form = new JPanel(new GridLayout(3, 2, 8, 12));
            form.setBorder(new EmptyBorder(16, 20, 16, 20));
            form.setBackground(Color.WHITE);

            JTextField txtUsername = createPillTextField("");
            JPasswordField txtPassword = new JPasswordField();
            txtPassword.setFont(FONT_REGULAR);
            txtPassword.setBorder(new CompoundBorder(new LineBorder(FINTECH_BORDER, 1, true), new EmptyBorder(5, 10, 5, 10)));
            JTextField txtNama = createPillTextField("");

            form.add(new JLabel("Username:"));
            form.add(txtUsername);
            form.add(new JLabel("Password:"));
            form.add(txtPassword);
            form.add(new JLabel("Nama Lengkap:"));
            form.add(txtNama);

            JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
            pnlBtn.setBackground(new Color(241, 245, 249));

            ContrastButton btnBatal = new ContrastButton("Batal", COLOR_SLATE_BTN, Color.WHITE);
            btnBatal.addActionListener(ev -> dialog.dispose());

            ContrastButton btnSimpan = new ContrastButton("Simpan Akun", COLOR_SUCCESS, Color.WHITE);
            btnSimpan.addActionListener(ev -> {
                String u = txtUsername.getText().trim();
                String p = new String(txtPassword.getPassword()).trim();
                String n = txtNama.getText().trim();

                if (u.isEmpty() || p.isEmpty() || n.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Semua field wajib diisi!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    Pengguna baru = new Kasir(0, u, p, n, "AKTIF");
                    authService.simpanKasir(baru);
                    JOptionPane.showMessageDialog(dialog, "Akun kasir berhasil didaftarkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadKasir.run();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Gagal membuat kasir: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            pnlBtn.add(btnBatal);
            pnlBtn.add(btnSimpan);

            dialog.add(form, BorderLayout.CENTER);
            dialog.add(pnlBtn, BorderLayout.SOUTH);
            dialog.setVisible(true);
        });

        btnToggleStatus.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(panel, "Pilih kasir yang ingin diubah statusnya!", "Perhatian", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) model.getValueAt(sel, 0);
            String nama = (String) model.getValueAt(sel, 2);
            String statusSkrg = (String) model.getValueAt(sel, 3);
            String statusTarget = "AKTIF".equalsIgnoreCase(statusSkrg) ? "NONAKTIF" : "AKTIF";

            int konf = JOptionPane.showConfirmDialog(panel, "Ubah status akun '" + nama + "' menjadi " + statusTarget + "?", "Ubah Status", JOptionPane.YES_NO_OPTION);
            if (konf == JOptionPane.YES_OPTION) {
                try {
                    authService.ubahStatusKasir(id, statusTarget);
                    JOptionPane.showMessageDialog(panel, "Status berhasil diperbarui.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    loadKasir.run();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, "Gagal ubah status: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnResetPwd.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(panel, "Pilih kasir yang ingin direset passwordnya!", "Perhatian", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) model.getValueAt(sel, 0);
            String username = (String) model.getValueAt(sel, 1);
            String nama = (String) model.getValueAt(sel, 2);

            String pwdBaru = JOptionPane.showInputDialog(panel, "Masukkan password baru untuk kasir [" + username + "]:");
            if (pwdBaru != null && !pwdBaru.trim().isEmpty()) {
                try {
                    Pengguna u = new Kasir(id, username, pwdBaru.trim(), nama, "AKTIF");
                    authService.simpanKasir(u);
                    JOptionPane.showMessageDialog(panel, "Password berhasil diperbarui!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, "Gagal reset password: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // PANEL 4: KELOLA PROMO (FR-07)
    // =========================================================================
    private JPanel buatPanelPromo() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(FINTECH_CARD);
        panel.setBorder(new CompoundBorder(new LineBorder(FINTECH_BORDER, 1, true), new EmptyBorder(16, 16, 16, 16)));

        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Kode Promo", "Nama Promo", "Tipe", "Nilai Diskon", "Min Belanja", "Tgl Mulai", "Tgl Selesai", "Status"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(model);
        styleFintechTable(table);

        Runnable loadPromo = () -> {
            try {
                model.setRowCount(0);
                List<Promo> list = promoService.getDaftarPromo();
                for (Promo p : list) {
                    String strDiskon = "PERSEN".equalsIgnoreCase(p.getTipeDiskon()) ? ((int) p.getNilaiDiskon() + "%") : formatRupiah(p.getNilaiDiskon());
                    model.addRow(new Object[]{
                            p.getId(), p.getKode(), p.getNamaPromo(), p.getTipeDiskon(), strDiskon,
                            formatRupiah(p.getMinBelanja()), p.getTglMulai(), p.getTglSelesai(), p.getStatus()
                    });
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Gagal memuat promo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        };
        loadPromo.run();

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(FINTECH_CARD);

        JLabel lblTitle = new JLabel("Manajemen Kode Promo & Diskon Penjualan (FR-07)");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(COLOR_TEXT_MAIN);
        top.add(lblTitle, BorderLayout.WEST);

        // Action Toolbar (Solid ContrastButton - No white washed-out buttons!)
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        toolbar.setBackground(FINTECH_CARD);

        ContrastButton btnTambah = new ContrastButton("+ Tambah Promo Baru", COLOR_SUCCESS, Color.WHITE);
        ContrastButton btnHapus = new ContrastButton("Hapus Promo", COLOR_DANGER, Color.WHITE);
        ContrastButton btnRefresh = new ContrastButton("Refresh", COLOR_SLATE_BTN, Color.WHITE);

        toolbar.add(btnTambah);
        toolbar.add(btnHapus);
        toolbar.add(btnRefresh);

        top.add(toolbar, BorderLayout.EAST);

        btnRefresh.addActionListener(e -> loadPromo.run());

        btnTambah.addActionListener(e -> {
            JDialog dialog = new JDialog(this, "Tambah Promo Baru", true);
            dialog.setSize(440, 420);
            dialog.setLocationRelativeTo(panel);
            dialog.setLayout(new BorderLayout());

            JPanel form = new JPanel(new GridLayout(7, 2, 8, 10));
            form.setBorder(new EmptyBorder(16, 20, 16, 20));
            form.setBackground(Color.WHITE);

            JTextField txtKode = createPillTextField("DISKON" + (int)(Math.random() * 90 + 10));
            JTextField txtNama = createPillTextField("");
            JComboBox<String> cmbTipe = new JComboBox<>(new String[]{"PERSEN", "NOMINAL"});
            JTextField txtNilai = createPillTextField("10");
            JTextField txtMin = createPillTextField("50000");
            JTextField txtMulai = createPillTextField(LocalDate.now().toString());
            JTextField txtSelesai = createPillTextField(LocalDate.now().plusMonths(1).toString());

            form.add(new JLabel("Kode Promo:"));
            form.add(txtKode);
            form.add(new JLabel("Nama Promo:"));
            form.add(txtNama);
            form.add(new JLabel("Tipe Diskon:"));
            form.add(cmbTipe);
            form.add(new JLabel("Besaran Diskon:"));
            form.add(txtNilai);
            form.add(new JLabel("Min Belanja (Rp):"));
            form.add(txtMin);
            form.add(new JLabel("Tgl Mulai (YYYY-MM-DD):"));
            form.add(txtMulai);
            form.add(new JLabel("Tgl Selesai (YYYY-MM-DD):"));
            form.add(txtSelesai);

            JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
            pnlBtn.setBackground(new Color(241, 245, 249));

            ContrastButton btnBatal = new ContrastButton("Batal", COLOR_SLATE_BTN, Color.WHITE);
            btnBatal.addActionListener(ev -> dialog.dispose());

            ContrastButton btnSimpan = new ContrastButton("Simpan Promo", COLOR_SUCCESS, Color.WHITE);
            btnSimpan.addActionListener(ev -> {
                try {
                    String kode = txtKode.getText().trim().toUpperCase();
                    String nama = txtNama.getText().trim();
                    String tipe = (String) cmbTipe.getSelectedItem();
                    double nilai = Double.parseDouble(txtNilai.getText().trim());
                    double min = Double.parseDouble(txtMin.getText().trim());
                    LocalDate tglMulai = LocalDate.parse(txtMulai.getText().trim());
                    LocalDate tglSelesai = LocalDate.parse(txtSelesai.getText().trim());

                    if (kode.isEmpty() || nama.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "Kode dan Nama promo tidak boleh kosong!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    Promo p = new Promo(0, kode, nama, tipe, nilai, min, tglMulai, tglSelesai, "AKTIF");
                    promoService.simpanPromo(p);
                    JOptionPane.showMessageDialog(dialog, "Promo berhasil ditambahkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadPromo.run();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Gagal simpan promo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            pnlBtn.add(btnBatal);
            pnlBtn.add(btnSimpan);

            dialog.add(form, BorderLayout.CENTER);
            dialog.add(pnlBtn, BorderLayout.SOUTH);
            dialog.setVisible(true);
        });

        btnHapus.addActionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(panel, "Pilih promo yang ingin dihapus!", "Perhatian", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int id = (int) model.getValueAt(sel, 0);
            String kode = (String) model.getValueAt(sel, 1);

            int konf = JOptionPane.showConfirmDialog(panel, "Yakin ingin menghapus promo '" + kode + "'?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
            if (konf == JOptionPane.YES_OPTION) {
                try {
                    promoService.hapusPromo(id);
                    JOptionPane.showMessageDialog(panel, "Promo berhasil dihapus.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
                    loadPromo.run();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, "Gagal menghapus promo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // PANEL 5: LAPORAN PENJUALAN (FR-04)
    // =========================================================================
    private JPanel buatPanelLaporan() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(FINTECH_CANVAS);

        final String[] periode = {"Harian"};

        JPanel filterCard = new JPanel(new BorderLayout());
        filterCard.setBackground(FINTECH_CARD);
        filterCard.setBorder(new CompoundBorder(new LineBorder(FINTECH_BORDER, 1, true), new EmptyBorder(12, 16, 12, 16)));

        JLabel lblTitle = new JLabel("Laporan Eksekutif Penjualan & Analitik Menu (FR-04)");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(COLOR_TEXT_MAIN);
        filterCard.add(lblTitle, BorderLayout.WEST);

        JPanel pnlPills = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlPills.setBackground(FINTECH_CARD);

        ContrastButton tabHarian = createFilterPill("Harian (Hari Ini)", true);
        ContrastButton tabMingguan = createFilterPill("Mingguan (Minggu Ini)", false);
        ContrastButton tabBulanan = createFilterPill("Bulanan (Bulan Ini)", false);

        ContrastButton[] periodBtns = {tabHarian, tabMingguan, tabBulanan};
        String[] periodKeys = {"Harian", "Mingguan", "Bulanan"};

        pnlPills.add(tabHarian);
        pnlPills.add(tabMingguan);
        pnlPills.add(tabBulanan);
        filterCard.add(pnlPills, BorderLayout.EAST);

        panel.add(filterCard, BorderLayout.NORTH);

        JPanel centerContainer = new JPanel(new BorderLayout(14, 14));
        centerContainer.setBackground(FINTECH_CANVAS);

        // 3 KPI Cards
        JPanel pnlKpis = new JPanel(new GridLayout(1, 3, 14, 14));
        pnlKpis.setBackground(FINTECH_CANVAS);

        JLabel lblValOmset = new JLabel("Rp 0");
        lblValOmset.setFont(FONT_HERO_NUM);
        lblValOmset.setForeground(COLOR_SUCCESS);

        JLabel lblValTrx = new JLabel("0 Transaksi");
        lblValTrx.setFont(FONT_HERO_NUM);
        lblValTrx.setForeground(COLOR_PRIMARY_DARK);

        JLabel lblValAvg = new JLabel("Rp 0");
        lblValAvg.setFont(FONT_HERO_NUM);
        lblValAvg.setForeground(COLOR_SLATE_BTN);

        pnlKpis.add(buatKpiCard("Total Omset Penjualan", lblValOmset, "Akumulasi pendapatan kotor", COLOR_SUCCESS));
        pnlKpis.add(buatKpiCard("Jumlah Transaksi", lblValTrx, "Total pesanan kasir sukses", COLOR_PRIMARY_DARK));
        pnlKpis.add(buatKpiCard("Rata-Rata per Transaksi", lblValAvg, "Nilai rata-rata per transaksi", COLOR_SLATE_BTN));

        centerContainer.add(pnlKpis, BorderLayout.NORTH);

        // 5 Menu Terlaris Table
        JPanel pnlBestSellers = new JPanel(new BorderLayout(10, 10));
        pnlBestSellers.setBackground(FINTECH_CARD);
        pnlBestSellers.setBorder(new CompoundBorder(new LineBorder(FINTECH_BORDER, 1, true), new EmptyBorder(14, 16, 14, 16)));

        JLabel lblTableTitle = new JLabel("Top 5 Menu Terlaris (Best Sellers)");
        lblTableTitle.setFont(FONT_TITLE);
        lblTableTitle.setForeground(COLOR_TEXT_MAIN);
        pnlBestSellers.add(lblTableTitle, BorderLayout.NORTH);

        DefaultTableModel modelTop = new DefaultTableModel(new String[]{"Peringkat", "Nama Menu", "Total Terjual (Cup/Porsi)", "Total Nilai Penjualan"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tableTop = new JTable(modelTop);
        styleFintechTable(tableTop);

        pnlBestSellers.add(new JScrollPane(tableTop), BorderLayout.CENTER);
        centerContainer.add(pnlBestSellers, BorderLayout.CENTER);

        panel.add(centerContainer, BorderLayout.CENTER);

        Runnable refreshLaporan = () -> {
            try {
                LaporanService.LaporanData data = laporanService.getLaporan(periode[0]);
                lblValOmset.setText(formatRupiah(data.totalPendapatan));
                lblValTrx.setText(data.totalTransaksi + " Transaksi");

                double avg = data.totalTransaksi > 0 ? (data.totalPendapatan / data.totalTransaksi) : 0;
                lblValAvg.setText(formatRupiah(avg));

                modelTop.setRowCount(0);
                String[] badges = {"Juara 1", "Juara 2", "Juara 3", "#4", "#5"};
                int rank = 0;
                for (Object[] item : data.menuTerlaris) {
                    String badge = rank < badges.length ? badges[rank] : ("#" + (rank + 1));
                    modelTop.addRow(new Object[]{
                            badge, item[0], item[1] + " porsi", formatRupiah((double) item[2])
                    });
                    rank++;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Gagal memuat laporan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        };

        for (int i = 0; i < periodBtns.length; i++) {
            final int idx = i;
            periodBtns[i].addActionListener(e -> {
                periode[0] = periodKeys[idx];
                for (ContrastButton b : periodBtns) {
                    b.setColors(new Color(241, 245, 249), COLOR_TEXT_MAIN);
                }
                periodBtns[idx].setColors(COLOR_PRIMARY_DARK, Color.WHITE);
                refreshLaporan.run();
            });
        }

        refreshLaporan.run();
        return panel;
    }

    private JPanel buatKpiCard(String title, JLabel valLabel, String sub, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(FINTECH_CARD);
        card.setBorder(new CompoundBorder(new LineBorder(FINTECH_BORDER, 1, true), new EmptyBorder(16, 18, 16, 18)));

        JLabel lblT = new JLabel(title);
        lblT.setFont(FONT_BOLD);
        lblT.setForeground(COLOR_TEXT_MUTED);

        JLabel lblS = new JLabel(sub);
        lblS.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblS.setForeground(COLOR_TEXT_MUTED);

        card.add(lblT);
        card.add(Box.createVerticalStrut(8));
        card.add(valLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(lblS);

        return card;
    }

    // =========================================================================
    // PANEL 6: RIWAYAT TRANSAKSI & RINCIAN BARANG TERJUAL (MASTER-DETAIL)
    // =========================================================================
    private JPanel buatPanelRiwayat() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(FINTECH_CANVAS);

        // --- BAGIAN ATAS (MASTER): TABEL DAFTAR TRANSAKSI ---
        JPanel pnlMaster = new JPanel(new BorderLayout(8, 8));
        pnlMaster.setBackground(FINTECH_CARD);
        pnlMaster.setBorder(new CompoundBorder(new LineBorder(FINTECH_BORDER, 1, true), new EmptyBorder(14, 16, 14, 16)));

        JPanel topMaster = new JPanel(new BorderLayout());
        topMaster.setBackground(FINTECH_CARD);

        JLabel lblTitle = new JLabel("Riwayat Seluruh Transaksi Penjualan");
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(COLOR_TEXT_MAIN);
        topMaster.add(lblTitle, BorderLayout.WEST);

        ContrastButton btnRefresh = new ContrastButton("Refresh Riwayat", COLOR_SLATE_BTN, Color.WHITE);
        topMaster.add(btnRefresh, BorderLayout.EAST);
        pnlMaster.add(topMaster, BorderLayout.NORTH);

        DefaultTableModel modelTrx = new DefaultTableModel(new String[]{"ID", "No Faktur", "Kasir/Operator", "Subtotal", "Diskon", "PPN (10%)", "Total Tagihan", "Nominal Bayar", "Kembalian"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tblTrx = new JTable(modelTrx);
        styleFintechTable(tblTrx);
        pnlMaster.add(new JScrollPane(tblTrx), BorderLayout.CENTER);

        // --- BAGIAN BAWAH (DETAIL): TABEL RINCIAN BARANG TERJUAL ---
        JPanel pnlDetail = new JPanel(new BorderLayout(8, 8));
        pnlDetail.setBackground(FINTECH_CARD);
        pnlDetail.setBorder(new CompoundBorder(new LineBorder(FINTECH_BORDER, 1, true), new EmptyBorder(14, 16, 14, 16)));

        JPanel topDetail = new JPanel(new BorderLayout());
        topDetail.setBackground(FINTECH_CARD);

        JLabel lblDetailTitle = new JLabel("Rincian Barang Terjual (Pilih transaksi pada tabel di atas)");
        lblDetailTitle.setFont(FONT_TITLE);
        lblDetailTitle.setForeground(COLOR_PRIMARY_DARK);
        topDetail.add(lblDetailTitle, BorderLayout.WEST);

        JPanel pnlDetailActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pnlDetailActions.setBackground(FINTECH_CARD);

        ContrastButton btnBukaPDF = new ContrastButton("Buka Dokumen Struk (PDF)", COLOR_PRIMARY_DARK, Color.WHITE);
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
                lblDetailTitle.setText("Rincian Barang Terjual (Pilih transaksi pada tabel di atas)");
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

        // Event Klik Baris Transaksi -> Menampilkan Detail Barang Terjual!
        tblTrx.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int sel = tblTrx.getSelectedRow();
                if (sel >= 0 && sel < cache[0].size()) {
                    Transaksi t = cache[0].get(sel);
                    lblDetailTitle.setText("Rincian Barang Terjual - No Faktur: " + t.getNoFaktur() + " (Kasir: " + t.getKasirNama() + ")");
                    modelItems.setRowCount(0);

                    int no = 1;
                    int totalQty = 0;
                    for (DetailTransaksi dt : t.getItems()) {
                        modelItems.addRow(new Object[]{
                                no++, dt.getNamaMenu(), formatRupiah(dt.getHarga()),
                                dt.getQty() + " cup/porsi", formatRupiah(dt.getSubtotal())
                        });
                        totalQty += dt.getQty();
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

    // =========================================================================
    // HELPER STYLING UMUM
    // =========================================================================
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

    public static ContrastButton createFilterPill(String text, boolean active) {
        ContrastButton btn = new ContrastButton(
                text,
                active ? COLOR_PRIMARY_DARK : new Color(241, 245, 249),
                active ? Color.WHITE : COLOR_TEXT_MAIN
        );
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));
        return btn;
    }
}
