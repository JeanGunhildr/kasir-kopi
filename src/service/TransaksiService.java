package service;

import model.Transaksi;
import repository.TransaksiRepository;
import util.PDFUtil;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

/**
 * Layanan Logika Bisnis untuk Pemrosesan Transaksi Kasir & Cetak Struk (FR-05, FR-06).
 */
public class TransaksiService {
    private final TransaksiRepository transaksiRepository = new TransaksiRepository();

    public String generateNoFaktur() {
        return transaksiRepository.generateNoFaktur();
    }

    public boolean prosesTransaksi(Transaksi t) throws SQLException {
        if (t.getItems().isEmpty()) {
            throw new IllegalArgumentException("Keranjang belanja masih kosong!");
        }
        if (t.getBayar() < t.getTotal()) {
            throw new IllegalArgumentException("Nominal pembayaran kurang dari total tagihan!");
        }
        return transaksiRepository.save(t);
    }

    public File cetakStruk(Transaksi t) {
        return PDFUtil.cetakStruk(t);
    }

    public List<Transaksi> getRiwayatTransaksi() throws SQLException {
        return transaksiRepository.findAll();
    }

    public List<model.DetailTransaksi> getDetailTransaksi(int transaksiId) throws SQLException {
        return transaksiRepository.findDetailsByTransaksiId(transaksiId);
    }
}
