package service;

import model.Menu;
import repository.MenuRepository;

import java.sql.SQLException;
import java.util.List;

/**
 * Layanan Logika Bisnis untuk Pengelolaan Menu dan Stok Real-Time (FR-01, FR-03).
 */
public class MenuService {
    private final MenuRepository menuRepository = new MenuRepository();

    public List<Menu> getDaftarMenu(String kategoriFilter) throws SQLException {
        return menuRepository.findAll(kategoriFilter);
    }

    public boolean simpanMenu(Menu m) throws SQLException {
        return menuRepository.save(m);
    }

    public boolean updateStok(int id, int stokBaru) throws SQLException {
        return menuRepository.updateStok(id, stokBaru);
    }

    public boolean hapusMenu(int id) throws SQLException {
        return menuRepository.delete(id);
    }

    public Menu getMenuById(int id) throws SQLException {
        return menuRepository.findById(id);
    }
}
