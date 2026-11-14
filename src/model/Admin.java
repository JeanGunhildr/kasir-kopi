package model;

/**
 * Subclass Admin mewarisi Pengguna (Penerapan Inheritance & Polymorphism).
 * Memiliki hak akses penuh untuk mengelola Menu, Stok, Akun, Promo, dan Laporan.
 */
public class Admin extends Pengguna {
    public Admin(int id, String username, String password, String nama, String status) {
        super(id, username, password, nama, "ADMIN", status);
    }

    @Override
    public String getDeskripsiAkses() {
        return "Administrator: Hak akses penuh mengelola Menu, Stok, Kasir, Promo, dan Laporan Penjualan.";
    }
}
