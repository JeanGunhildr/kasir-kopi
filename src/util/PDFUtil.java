package util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import model.DetailTransaksi;
import model.Transaksi;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;

/**
 * Utilitas Pencetakan Struk Bukti Transaksi PDF & Dokumen Teks TXT Lokal (FR-06).
 */
public class PDFUtil {
    private static final String FOLDER_STRUK = "receipts";

    public static File cetakStruk(Transaksi t) {
        try {
            File dir = new File(FOLDER_STRUK);
            if (!dir.exists()) dir.mkdirs();

            String namaFile = "STRUK_" + t.getNoFaktur() + ".pdf";
            File filePdf = new File(dir, namaFile);

            Rectangle ukuranStruk = new Rectangle(230, 480);
            Document doc = new Document(ukuranStruk, 12, 12, 12, 12);
            PdfWriter.getInstance(doc, new FileOutputStream(filePdf));
            doc.open();

            Font fontJudul = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font fontSub = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
            Font fontBold = new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD);
            Font fontRegular = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);

            Paragraph pJudul = new Paragraph("PACBUCK COFFEE", fontJudul);
            pJudul.setAlignment(Element.ALIGN_CENTER);
            doc.add(pJudul);

            Paragraph pAlamat = new Paragraph("Kedai Kopi Nusantara - PBO UNSOED\nTelp: 0281-635299", fontSub);
            pAlamat.setAlignment(Element.ALIGN_CENTER);
            pAlamat.setSpacingAfter(6);
            doc.add(pAlamat);

            LineSeparator line = new LineSeparator();
            line.setLineColor(BaseColor.LIGHT_GRAY);
            doc.add(line);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            Paragraph pInfo = new Paragraph();
            pInfo.setFont(fontRegular);
            pInfo.setSpacingBefore(4);
            pInfo.setSpacingAfter(4);
            pInfo.add("No. Faktur : " + t.getNoFaktur() + "\n");
            pInfo.add("Waktu      : " + (t.getWaktu() != null ? t.getWaktu().format(dtf) : "-") + "\n");
            pInfo.add("Kasir      : " + t.getKasirNama());
            doc.add(pInfo);
            doc.add(line);

            // Tabel Detail Item
            PdfPTable table = new PdfPTable(new float[]{4.5f, 1.2f, 2.3f, 2.5f});
            table.setWidthPercentage(100);
            table.setSpacingBefore(4);
            table.setSpacingAfter(4);

            for (DetailTransaksi d : t.getItems()) {
                addCell(table, d.getNamaMenu(), fontRegular, Element.ALIGN_LEFT);
                addCell(table, String.valueOf(d.getQty()), fontRegular, Element.ALIGN_CENTER);
                addCell(table, String.valueOf((long) d.getHarga()), fontRegular, Element.ALIGN_RIGHT);
                addCell(table, String.valueOf((long) d.getSubtotal()), fontRegular, Element.ALIGN_RIGHT);
            }
            doc.add(table);
            doc.add(line);

            // Ringkasan Total
            PdfPTable summary = new PdfPTable(new float[]{5f, 5f});
            summary.setWidthPercentage(100);
            summary.setSpacingBefore(4);

            addRow(summary, "Subtotal", "Rp " + (long) t.getSubtotal(), fontRegular);
            if (t.getDiskon() > 0) {
                addRow(summary, "Diskon (" + t.getKodePromo() + ")", "-Rp " + (long) t.getDiskon(), fontRegular);
            }
            addRow(summary, "PPN (10%)", "Rp " + (long) t.getPajak(), fontRegular);
            addRow(summary, "TOTAL", "Rp " + (long) t.getTotal(), fontBold);
            addRow(summary, "Bayar", "Rp " + (long) t.getBayar(), fontRegular);
            addRow(summary, "Kembalian", "Rp " + (long) t.getKembalian(), fontBold);
            doc.add(summary);

            Paragraph pFooter = new Paragraph("\nTerima Kasih Atas Kunjungan Anda!\nNikmati Kopi Terbaik Kami", fontSub);
            pFooter.setAlignment(Element.ALIGN_CENTER);
            doc.add(pFooter);

            doc.close();

            // Cetak juga file teks .txt secara lokal (sesuai batasan masalah)
            cetakStrukTXT(t);

            return filePdf;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void cetakStrukTXT(Transaksi t) {
        try {
            File dir = new File(FOLDER_STRUK);
            if (!dir.exists()) dir.mkdirs();

            File fileTxt = new File(dir, "STRUK_" + t.getNoFaktur() + ".txt");
            try (PrintWriter pw = new PrintWriter(new FileWriter(fileTxt))) {
                pw.println("========================================");
                pw.println("            PACBUCK COFFEE              ");
                pw.println("   Kedai Kopi Nusantara - PBO UNSOED    ");
                pw.println("========================================");
                pw.println("No Faktur : " + t.getNoFaktur());
                pw.println("Kasir     : " + t.getKasirNama());
                pw.println("----------------------------------------");
                for (DetailTransaksi d : t.getItems()) {
                    pw.printf("%-18s %2dx %8d %8d%n", d.getNamaMenu(), d.getQty(), (long) d.getHarga(), (long) d.getSubtotal());
                }
                pw.println("----------------------------------------");
                pw.printf("%-20s : Rp %d%n", "Subtotal", (long) t.getSubtotal());
                if (t.getDiskon() > 0) {
                    pw.printf("%-20s : -Rp %d%n", "Diskon (" + t.getKodePromo() + ")", (long) t.getDiskon());
                }
                pw.printf("%-20s : Rp %d%n", "PPN (10%)", (long) t.getPajak());
                pw.printf("%-20s : Rp %d%n", "TOTAL", (long) t.getTotal());
                pw.printf("%-20s : Rp %d%n", "Bayar", (long) t.getBayar());
                pw.printf("%-20s : Rp %d%n", "Kembalian", (long) t.getKembalian());
                pw.println("========================================");
                pw.println("    TERIMA KASIH ATAS KUNJUNGAN ANDA    ");
                pw.println("========================================");
            }
        } catch (Exception ignored) {
        }
    }

    private static void addCell(PdfPTable table, String text, Font font, int align) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setBorder(Rectangle.NO_BORDER);
        c.setHorizontalAlignment(align);
        c.setPaddingBottom(2);
        table.addCell(c);
    }

    private static void addRow(PdfPTable table, String col1, String col2, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(col1, font));
        c1.setBorder(Rectangle.NO_BORDER);
        PdfPCell c2 = new PdfPCell(new Phrase(col2, font));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(c1);
        table.addCell(c2);
    }

    public static void bukaFile(File file) {
        try {
            if (file != null && file.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception ignored) {
        }
    }
}
