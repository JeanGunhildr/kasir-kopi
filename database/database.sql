-- ========================================================
-- DATABASE: db_kasir_kopi
-- SISTEM MESIN KASIR KEDAI KOPI (PBO JAVA)
-- Kelompok 4 - Informatika UNSOED 2026
-- ========================================================

CREATE DATABASE IF NOT EXISTS db_kasir_kopi;
USE db_kasir_kopi;

-- 1. Tabel Pengguna (Admin & Kasir) - FR-02
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nama VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'KASIR') NOT NULL DEFAULT 'KASIR',
    status ENUM('AKTIF', 'NONAKTIF') NOT NULL DEFAULT 'AKTIF'
) ENGINE=InnoDB;

-- 2. Tabel Menu & Stok Kopi / Makanan - FR-01 & FR-03
CREATE TABLE IF NOT EXISTS menu (
    id INT AUTO_INCREMENT PRIMARY KEY,
    kode VARCHAR(20) NOT NULL UNIQUE,
    nama VARCHAR(100) NOT NULL,
    kategori ENUM('Kopi', 'Non-Kopi', 'Makanan') NOT NULL,
    harga DOUBLE NOT NULL,
    stok INT NOT NULL DEFAULT 0
) ENGINE=InnoDB;

-- 3. Tabel Kode Promo & Diskon - FR-07 & FR-08
CREATE TABLE IF NOT EXISTS promo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    kode VARCHAR(30) NOT NULL UNIQUE,
    nama_promo VARCHAR(100) NOT NULL,
    tipe_diskon ENUM('PERSEN', 'NOMINAL') NOT NULL,
    nilai_diskon DOUBLE NOT NULL,
    min_belanja DOUBLE NOT NULL DEFAULT 0,
    tgl_mulai DATE NOT NULL,
    tgl_selesai DATE NOT NULL,
    status ENUM('AKTIF', 'NONAKTIF') NOT NULL DEFAULT 'AKTIF'
) ENGINE=InnoDB;

-- 4. Tabel Transaksi Penjualan - FR-05
CREATE TABLE IF NOT EXISTS transaksi (
    id INT AUTO_INCREMENT PRIMARY KEY,
    no_faktur VARCHAR(50) NOT NULL UNIQUE,
    kasir_nama VARCHAR(100) NOT NULL,
    kode_promo VARCHAR(30),
    subtotal DOUBLE NOT NULL,
    diskon DOUBLE NOT NULL DEFAULT 0,
    pajak DOUBLE NOT NULL DEFAULT 0,
    total DOUBLE NOT NULL,
    bayar DOUBLE NOT NULL,
    kembalian DOUBLE NOT NULL,
    waktu DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 5. Tabel Detail Item Transaksi
CREATE TABLE IF NOT EXISTS detail_transaksi (
    id INT AUTO_INCREMENT PRIMARY KEY,
    transaksi_id INT NOT NULL,
    menu_id INT NOT NULL,
    nama_menu VARCHAR(100) NOT NULL,
    harga DOUBLE NOT NULL,
    qty INT NOT NULL,
    subtotal DOUBLE NOT NULL,
    CONSTRAINT fk_transaksi FOREIGN KEY (transaksi_id) REFERENCES transaksi(id) ON DELETE CASCADE,
    CONSTRAINT fk_menu FOREIGN KEY (menu_id) REFERENCES menu(id) ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ========================================================
-- DATA AWAL (SEED DATA DEFAULT)
-- ========================================================

-- Akun Default: admin/admin123 dan kasir/kasir123
INSERT INTO users (username, password, nama, role, status) VALUES
('admin', 'admin123', 'Admin Kedai Kopi', 'ADMIN', 'AKTIF'),
('kasir1', 'kasir123', 'Siti Rahma (Kasir)', 'KASIR', 'AKTIF')
ON DUPLICATE KEY UPDATE nama=VALUES(nama);

-- Menu Kopi, Non-Kopi, dan Makanan (FR-01) dengan Stok (FR-03)
INSERT INTO menu (kode, nama, kategori, harga, stok) VALUES
('KOP01', 'Espresso', 'Kopi', 15000, 50),
('KOP02', 'Americano', 'Kopi', 18000, 45),
('KOP03', 'Kopi Susu Gula Aren', 'Kopi', 20000, 60),
('KOP04', 'Caffe Latte', 'Kopi', 22000, 40),
('KOP05', 'Caramel Macchiato', 'Kopi', 25000, 35),
('NON01', 'Matcha Latte', 'Non-Kopi', 22000, 35),
('NON02', 'Chocolate Signature', 'Non-Kopi', 20000, 40),
('NON03', 'Earl Grey Tea', 'Non-Kopi', 18000, 30),
('MKN01', 'Croissant Butter', 'Makanan', 18000, 20),
('MKN02', 'Roti Bakar Coklat Keju', 'Makanan', 17000, 25),
('MKN03', 'French Fries', 'Makanan', 15000, 30)
ON DUPLICATE KEY UPDATE nama=VALUES(nama);

-- Promo Bawaan (FR-07)
INSERT INTO promo (kode, nama_promo, tipe_diskon, nilai_diskon, min_belanja, tgl_mulai, tgl_selesai, status) VALUES
('NGOPIHEMAT', 'Promo Ngopi Hemat 10%', 'PERSEN', 10, 30000, '2026-01-01', '2026-12-31', 'AKTIF'),
('DISKON5RB', 'Potongan Rp 5.000', 'NOMINAL', 5000, 25000, '2026-01-01', '2026-12-31', 'AKTIF')
ON DUPLICATE KEY UPDATE nama_promo=VALUES(nama_promo);
