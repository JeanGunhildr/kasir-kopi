package model;

/**
 * Model item pesanan dalam suatu transaksi (Relasi Komposisi dengan Transaksi).
 */
public class DetailTransaksi {
    private int id;
    private int transaksiId;
    private int menuId;
    private String namaMenu;
    private double harga;
    private int qty;
    private double subtotal;

    public DetailTransaksi(int menuId, String namaMenu, double harga, int qty) {
        this.menuId = menuId;
        this.namaMenu = namaMenu;
        this.harga = harga;
        this.qty = qty;
        hitungSubtotal();
    }

    public DetailTransaksi(int id, int transaksiId, int menuId, String namaMenu, double harga, int qty, double subtotal) {
        this.id = id;
        this.transaksiId = transaksiId;
        this.menuId = menuId;
        this.namaMenu = namaMenu;
        this.harga = harga;
        this.qty = qty;
        this.subtotal = subtotal;
    }

    public void hitungSubtotal() {
        this.subtotal = this.harga * this.qty;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTransaksiId() { return transaksiId; }
    public void setTransaksiId(int transaksiId) { this.transaksiId = transaksiId; }

    public int getMenuId() { return menuId; }
    public void setMenuId(int menuId) { this.menuId = menuId; }

    public String getNamaMenu() { return namaMenu; }
    public void setNamaMenu(String namaMenu) { this.namaMenu = namaMenu; }

    public double getHarga() { return harga; }
    public void setHarga(double harga) {
        this.harga = harga;
        hitungSubtotal();
    }

    public int getQty() { return qty; }
    public void setQty(int qty) {
        this.qty = qty;
        hitungSubtotal();
    }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}
