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

    /**
     * Verifica si ya existe una categoría con el mismo nombre (insensible a mayúsculas/minúsculas).
     * Sección 5 de la guía.
     */
    public boolean existeNombre(String nombre) throws SQLException {
        String sql = """
            SELECT COUNT(*)
            FROM categoria
            WHERE LOWER(nombre) = LOWER(?)
            """;

        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, nombre != null ? nombre.trim() : "");

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Verifica si ya existe otra categoría con el mismo nombre excluyendo el ID actual (para UPDATE).
     * Sección 5 de la guía.
     */
    public boolean existeNombre(String nombre, Integer idExcluir) throws SQLException {
        if (idExcluir == null) {
            return existeNombre(nombre);
        }

        String sql = """
            SELECT COUNT(*)
            FROM categoria
            WHERE LOWER(nombre) = LOWER(?) AND id <> ?
            """;

        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, nombre != null ? nombre.trim() : "");
            ps.setInt(2, idExcluir);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Verifica si existen productos asociados a una categoría antes de eliminarla.
     * Sección 7 de la guía.
     */
    public boolean tieneProductos(int categoriaId) throws SQLException {
        String sql = """
            SELECT COUNT(*)
            FROM producto
            WHERE categoria_id = ?
            """;

        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, categoriaId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Registra una categoría en la base de datos lanzando SQLException.
     * Sección 15 y 16 de la guía.
     */
    public void guardar(Categoria cat) throws SQLException {
        String sql = "INSERT INTO categoria (nombre, activa) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, cat.getNombre());
            stmt.setBoolean(2, cat.isActiva());

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        cat.setId(rs.getInt(1));
                    }
                }
            }
        }
    }

    /**
     * Actualiza una categoría en la base de datos lanzando SQLException.
     */
    public void actualizarCategoria(Categoria cat) throws SQLException {
        String sql = "UPDATE categoria SET nombre = ?, activa = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cat.getNombre());
            stmt.setBoolean(2, cat.isActiva());
            stmt.setInt(3, cat.getId());

            stmt.executeUpdate();
        }
    }

    /**
     * Elimina una categoría por su ID lanzando SQLException.
     */
    public void eliminarCategoria(Integer id) throws SQLException {
        String sql = "DELETE FROM categoria WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private String ultimoError;

    public String getUltimoError() {
        return ultimoError;
    }

    @Override
    public boolean crear(Categoria cat) {
        ultimoError = null;
        try {
            guardar(cat);
            return true;
        } catch (SQLException e) {
            System.err.println("Error al crear categoría: " + e.getMessage());
            if ("23505".equals(e.getSQLState())) {
                ultimoError = "Ya existe una categoría con el nombre '" + cat.getNombre() + "'.";
            } else {
                ultimoError = "Error al crear la categoría: " + e.getMessage();
            }
            return false;
        }
    }

    @Override
    public boolean actualizar(Categoria cat) {
        ultimoError = null;
        try {
            actualizarCategoria(cat);
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar categoría: " + e.getMessage());
            if ("23505".equals(e.getSQLState())) {
                ultimoError = "Ya existe una categoría con el nombre '" + cat.getNombre() + "'.";
            } else {
                ultimoError = "Error al actualizar la categoría: " + e.getMessage();
            }
            return false;
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        ultimoError = null;
        try {
            eliminarCategoria(id);
            return true;
        } catch (SQLException e) {
            System.err.println("Error al eliminar categoría: " + e.getMessage());
            if ("23503".equals(e.getSQLState())) {
                ultimoError = "No se puede eliminar la categoría porque tiene productos asignados.";
            } else {
                ultimoError = "Error al eliminar la categoría: " + e.getMessage();
            }
            return false;
        }
    }
}
