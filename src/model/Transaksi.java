package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model Transaksi Penjualan (FR-05).
 * Memiliki relasi Komposisi dengan DetailTransaksi.
 */
public class Transaksi {
    private int id;
    private String noFaktur;
    private String kasirNama;
    private String kodePromo;
    private double subtotal;
    private double diskon;
    private double pajak; // PPN 10%
    private double total;
    private double bayar;
    private double kembalian;
    private LocalDateTime waktu = LocalDateTime.now();

    private List<DetailTransaksi> items = new ArrayList<>();

    public Transaksi(String noFaktur, String kasirNama) {
        this.noFaktur = noFaktur;
        this.kasirNama = kasirNama;
    }

    public void tambahItem(Menu menu, int qty) {
        for (DetailTransaksi item : items) {
            if (item.getMenuId() == menu.getId()) {
                item.setQty(item.getQty() + qty);
                return;
            }
        }
        items.add(new DetailTransaksi(menu.getId(), menu.getNama(), menu.getHarga(), qty));
    }

    public void ubahQtyItem(int menuId, int delta) {
        for (int i = 0; i < items.size(); i++) {
            DetailTransaksi item = items.get(i);
            if (item.getMenuId() == menuId) {
                int newQty = item.getQty() + delta;
                if (newQty <= 0) {
                    items.remove(i);
                } else {
                    item.setQty(newQty);
                }
                return;
            }
        }
    }

    public void hapusItem(int menuId) {
        items.removeIf(item -> item.getMenuId() == menuId);
    }

    public void hitungTotal(Promo promo) {
        this.subtotal = 0.0;
        for (DetailTransaksi d : items) {
            this.subtotal += d.getSubtotal();
        }

        if (promo != null && promo.isValid()) {
            this.kodePromo = promo.getKode();
            this.diskon = promo.hitungPotongan(this.subtotal);
        } else {
            this.kodePromo = null;
            this.diskon = 0.0;
        }

        double kenaPajak = Math.max(0.0, this.subtotal - this.diskon);
        this.pajak = kenaPajak * 0.10; // Pajak PPN 10%
        this.total = kenaPajak + this.pajak;

        if (this.bayar >= this.total) {
            this.kembalian = this.bayar - this.total;
        } else {
            this.kembalian = 0.0;
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNoFaktur() { return noFaktur; }
    public void setNoFaktur(String noFaktur) { this.noFaktur = noFaktur; }

    public String getKasirNama() { return kasirNama; }
    public void setKasirNama(String kasirNama) { this.kasirNama = kasirNama; }

    public String getKodePromo() { return kodePromo; }
    public void setKodePromo(String kodePromo) { this.kodePromo = kodePromo; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getDiskon() { return diskon; }
    public void setDiskon(double diskon) { this.diskon = diskon; }

    public double getPajak() { return pajak; }
    public void setPajak(double pajak) { this.pajak = pajak; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public double getBayar() { return bayar; }
    public void setBayar(double bayar) { this.bayar = bayar; }

    public double getKembalian() { return kembalian; }
    public void setKembalian(double kembalian) { this.kembalian = kembalian; }

    public LocalDateTime getWaktu() { return waktu; }
    public void setWaktu(LocalDateTime waktu) { this.waktu = waktu; }

    public List<DetailTransaksi> getItems() { return items; }
    public void setItems(List<DetailTransaksi> items) { this.items = items; }
}
