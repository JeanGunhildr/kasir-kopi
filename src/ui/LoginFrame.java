package ui;

import model.Admin;
import model.Pengguna;
import service.AuthService;
import util.DatabaseUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Antarmuka Login Sistem Kasir Kedai Kopi (Pacbuck Coffee).
 * Titik masuk utama aplikasi (Main Entry Point) yang mengarahkan pengguna ke
 * AdminDashboard atau KasirDashboard sesuai hak akses (Role-Based Access Control).
 * Menggunakan ContrastButton solid untuk menjamin 100% keterbacaan pada Windows.
 */
public class LoginFrame extends JFrame {

    // Palet Warna Fintech Dashboard
    public static final Color COLOR_PRIMARY_DARK = new Color(11, 59, 44);   // Deep Emerald Forest (#0B3B2C)
    public static final Color COLOR_SUCCESS = new Color(5, 150, 105);       // Mint Green (#059669)
    public static final Color COLOR_SLATE_BTN = new Color(51, 65, 85);      // Slate Button (#334155)
    public static final Color FINTECH_CANVAS = new Color(241, 245, 249);    // Canvas Slate 100 (#F1F5F9)
    public static final Color FINTECH_BORDER = new Color(203, 213, 225);    // Border (#CBD5E1)
    public static final Color COLOR_TEXT_MAIN = new Color(15, 23, 42);      // Slate 900 (#0F172A)
    public static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);  // Slate 500 (#64748B)

    private final AuthService authService = AuthService.getInstance();
    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public LoginFrame() {
        super("Pacbuck Coffee - Otentikasi Masuk Sistem");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 560);
        setMinimumSize(new Dimension(420, 520));
        setLocationRelativeTo(null);
        getContentPane().setBackground(FINTECH_CANVAS);
        setLayout(new GridBagLayout());

        // Card Container Tengah
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(FINTECH_BORDER, 1, true),
                new EmptyBorder(0, 0, 24, 0)
        ));
        card.setPreferredSize(new Dimension(380, 480));

        // 1. Header Banner Atas (Deep Emerald)
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(COLOR_PRIMARY_DARK);
        header.setBorder(new EmptyBorder(22, 20, 22, 20));
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 115));

        JLabel lblLogo = new JLabel("☕");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblLogo.setForeground(Color.WHITE);
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(lblLogo);

        header.add(Box.createVerticalStrut(4));

        JLabel lblBrand = new JLabel("Pacbuck Coffee POS");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblBrand.setForeground(Color.WHITE);
        lblBrand.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(lblBrand);

        JLabel lblSub = new JLabel("Fintech Point of Sale • Kelompok 4 PBO");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(167, 243, 208));
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(lblSub);

        card.add(header);
        card.add(Box.createVerticalStrut(20));

        // 2. Form Input Panel
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(0, 28, 0, 28));
        form.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(COLOR_TEXT_MAIN);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblUser);

        form.add(Box.createVerticalStrut(6));

        txtUsername = new JTextField();
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtUsername.setBorder(new CompoundBorder(new LineBorder(FINTECH_BORDER, 1, true), new EmptyBorder(8, 12, 8, 12)));
        txtUsername.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtUsername.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(txtUsername);

        form.add(Box.createVerticalStrut(14));

        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPass.setForeground(COLOR_TEXT_MAIN);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblPass);

        form.add(Box.createVerticalStrut(6));

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPassword.setBorder(new CompoundBorder(new LineBorder(FINTECH_BORDER, 1, true), new EmptyBorder(8, 12, 8, 12)));
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(txtPassword);

        form.add(Box.createVerticalStrut(20));

        // Tombol Login (Solid Emerald Kontras Tinggi)
        AdminDashboard.ContrastButton btnLogin = new AdminDashboard.ContrastButton("Masuk Sistem (Login)", COLOR_PRIMARY_DARK, Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(btnLogin);

        form.add(Box.createVerticalStrut(16));

        // Quick Fill Pills untuk kemudahan demo dosen / penguji
        JLabel lblQuick = new JLabel("Login Cepat (Akun Demo):");
        lblQuick.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblQuick.setForeground(COLOR_TEXT_MUTED);
        lblQuick.setAlignmentX(Component.CENTER_ALIGNMENT);
        form.add(lblQuick);

        form.add(Box.createVerticalStrut(6));

        JPanel pnlQuickBtns = new JPanel(new GridLayout(1, 2, 8, 0));
        pnlQuickBtns.setBackground(Color.WHITE);
        pnlQuickBtns.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        pnlQuickBtns.setAlignmentX(Component.LEFT_ALIGNMENT);

        AdminDashboard.ContrastButton btnDemoAdmin = new AdminDashboard.ContrastButton("Admin (admin)", COLOR_SLATE_BTN, Color.WHITE);
        btnDemoAdmin.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnDemoAdmin.addActionListener(e -> {
            txtUsername.setText("admin");
            txtPassword.setText("admin123");
            txtPassword.requestFocusInWindow();
        });

        AdminDashboard.ContrastButton btnDemoKasir = new AdminDashboard.ContrastButton("Kasir (kasir1)", COLOR_SLATE_BTN, Color.WHITE);
        btnDemoKasir.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnDemoKasir.addActionListener(e -> {
            txtUsername.setText("kasir1");
            txtPassword.setText("kasir123");
            txtPassword.requestFocusInWindow();
        });

        pnlQuickBtns.add(btnDemoAdmin);
        pnlQuickBtns.add(btnDemoKasir);
        form.add(pnlQuickBtns);

        card.add(form);
        card.add(Box.createVerticalGlue());

        // Status Footer
        JLabel lblDbStatus = new JLabel("● Database: MySQL Connected (db_kasir_kopi)");
        lblDbStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDbStatus.setForeground(COLOR_SUCCESS);
        lblDbStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblDbStatus);

        add(card);

        // Action Events
        btnLogin.addActionListener(e -> prosesLogin());

        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    prosesLogin();
                }
            }
        };
        txtUsername.addKeyListener(enterListener);
        txtPassword.addKeyListener(enterListener);
    }

    private void prosesLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silakan masukkan username dan password!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Pengguna user = authService.login(username, password);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Username atau password salah, atau akun nonaktif!", "Gagal Masuk", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Role-based routing (Admin atau Kasir)
            if (user instanceof Admin || "ADMIN".equalsIgnoreCase(user.getRole())) {
                new AdminDashboard(user).setVisible(true);
            } else {
                new KasirDashboard(user).setVisible(true);
            }
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal terhubung ke database: " + ex.getMessage() + "\nPastikan MySQL di Laragon / XAMPP sudah berjalan di port 3306.", "Koneksi Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Set Look and Feel Modern Sistem
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Inisialisasi Database dan Data Default Otomatis
        DatabaseUtil.initDatabase();

        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}
