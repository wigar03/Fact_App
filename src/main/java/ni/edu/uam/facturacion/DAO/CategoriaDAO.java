package ni.edu.uam.facturacion.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import ni.edu.uam.facturacion.model.Categoria;
import ni.edu.uam.facturacion.util.CRUD;
import ni.edu.uam.facturacion.util.DatabaseConnection;

public class CategoriaDAO implements CRUD<Categoria, Integer> {

    @Override
    public List<Categoria> findAll() {
        var categorias = new ArrayList<Categoria>();

        // Tabla 'categoria' en singular y solo las 3 columnas reales
        var sql = "SELECT id, nombre, activa "
                + "FROM categoria "
                + "ORDER BY nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                var cat = new Categoria();
                cat.setId(rs.getInt("id"));
                cat.setNombre(rs.getString("nombre"));
                cat.setActiva(rs.getBoolean("activa"));
                categorias.add(cat);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar categorías: " + e.getMessage());
        }

        return categorias;
    }

    @Override
    public Categoria findById(Integer id) {
        var sql = "SELECT id, nombre, activa "
                + "FROM categoria "
                + "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    var cat = new Categoria();
                    cat.setId(rs.getInt("id"));
                    cat.setNombre(rs.getString("nombre"));
                    cat.setActiva(rs.getBoolean("activa"));
                    return cat;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar categoría: " + e.getMessage());
        }

        return null;
    }

    @Override
    public boolean crear(Categoria cat) {
        var sql = "INSERT INTO categoria (nombre, activa) "
                + "VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cat.getNombre());
            stmt.setBoolean(2, cat.isActiva());

            int filas = stmt.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
            System.err.println("Error al crear categoría: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean actualizar(Categoria cat) {
        var sql = "UPDATE categoria SET nombre = ?, activa = ? "
                + "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cat.getNombre());
            stmt.setBoolean(2, cat.isActiva());
            stmt.setInt(3, cat.getId());

            int filas = stmt.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar categoría: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        var sql = "DELETE FROM categoria WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int filas = stmt.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar categoría: " + e.getMessage());
            return false;
        }
    }
}
