package ni.edu.uam.facturacion.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import ni.edu.uam.facturacion.model.Categoria;
import ni.edu.uam.facturacion.model.Producto;
import ni.edu.uam.facturacion.util.CRUD;
import ni.edu.uam.facturacion.util.DatabaseConnection;

public class ProductoDAO implements CRUD<Producto, Integer> {

    @Override
    public List<Producto> findAll() {
        var productos = new ArrayList<Producto>();

        var sql = "SELECT p.id, p.codigo, p.nombre, p.categoria_id, p.precio_venta, "
                + "       p.existencia, p.ruta_imagen, p.activo, "
                + "       c.nombre AS categoria_nombre, c.activa AS categoria_activa "
                + "FROM producto p "
                + "LEFT JOIN categoria c ON p.categoria_id = c.id "
                + "ORDER BY p.nombre";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                productos.add(mapearProducto(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar productos: " + e.getMessage());
        }

        return productos;
    }

    @Override
    public Producto findById(Integer id) {
        var sql = "SELECT p.id, p.codigo, p.nombre, p.categoria_id, p.precio_venta, "
                + "       p.existencia, p.ruta_imagen, p.activo, "
                + "       c.nombre AS categoria_nombre, c.activa AS categoria_activa "
                + "FROM producto p "
                + "LEFT JOIN categoria c ON p.categoria_id = c.id "
                + "WHERE p.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearProducto(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar producto: " + e.getMessage());
        }

        return null;
    }

    /**
     * Verifica si ya existe un producto con el código especificado.
     * Sección 14 de la guía.
     */
    public boolean existeCodigo(String codigo) throws SQLException {
        String sql = """
            SELECT COUNT(*)
            FROM producto
            WHERE LOWER(codigo) = LOWER(?)
            """;

        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, codigo != null ? codigo.trim() : "");

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Verifica si ya existe otro producto con el código especificado excluyendo el ID actual (para UPDATE).
     * Secciones 14 y 19 de la guía.
     */
    public boolean existeCodigo(String codigo, Integer idExcluir) throws SQLException {
        if (idExcluir == null) {
            return existeCodigo(codigo);
        }

        String sql = """
            SELECT COUNT(*)
            FROM producto
            WHERE LOWER(codigo) = LOWER(?) AND id <> ?
            """;

        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, codigo != null ? codigo.trim() : "");
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
     * Registra un nuevo producto en la base de datos lanzando SQLException.
     * Secciones 15, 16 y 18 de la guía.
     */
    public void guardar(Producto p) throws SQLException {
        String sql = "INSERT INTO producto (codigo, nombre, categoria_id, precio_venta, existencia, ruta_imagen, activo) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, p.getCodigo());
            stmt.setString(2, p.getNombre());

            if (p.getCategoria() != null && p.getCategoria().getId() != null) {
                stmt.setInt(3, p.getCategoria().getId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            stmt.setBigDecimal(4, p.getPrecioVenta());
            stmt.setInt(5, p.getExistencia());
            stmt.setString(6, p.getRutaImagen());
            stmt.setBoolean(7, p.isActivo());

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        p.setId(rs.getInt(1));
                    }
                }
            }
        }
    }

    /**
     * Actualiza un producto existente en la base de datos lanzando SQLException.
     * Sección 19 de la guía.
     */
    public void actualizarProducto(Producto p) throws SQLException {
        String sql = "UPDATE producto SET codigo = ?, nombre = ?, categoria_id = ?, precio_venta = ?, "
                   + "                   existencia = ?, ruta_imagen = ?, activo = ? "
                   + "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getCodigo());
            stmt.setString(2, p.getNombre());

            if (p.getCategoria() != null && p.getCategoria().getId() != null) {
                stmt.setInt(3, p.getCategoria().getId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            stmt.setBigDecimal(4, p.getPrecioVenta());
            stmt.setInt(5, p.getExistencia());
            stmt.setString(6, p.getRutaImagen());
            stmt.setBoolean(7, p.isActivo());
            stmt.setInt(8, p.getId());

            stmt.executeUpdate();
        }
    }

    /**
     * Elimina un producto por su identificador lanzando SQLException.
     * Sección 20 de la guía.
     */
    public void eliminarProducto(Integer id) throws SQLException {
        String sql = "DELETE FROM producto WHERE id = ?";

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
    public boolean crear(Producto p) {
        ultimoError = null;
        try {
            guardar(p);
            return true;
        } catch (SQLException e) {
            System.err.println("Error al crear producto: " + e.getMessage());
            if ("23505".equals(e.getSQLState())) {
                String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                if (msg.contains("uq_producto_nombre") || msg.contains("(nombre)")) {
                    ultimoError = "Ya existe un producto con el nombre '" + p.getNombre() + "'.";
                } else if (msg.contains("codigo")) {
                    ultimoError = "Ya existe un producto con el código '" + p.getCodigo() + "'.";
                } else {
                    ultimoError = "Ya existe un registro con datos duplicados.";
                }
            } else {
                ultimoError = "Error al crear el producto: " + e.getMessage();
            }
            return false;
        }
    }

    @Override
    public boolean actualizar(Producto p) {
        ultimoError = null;
        try {
            actualizarProducto(p);
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            if ("23505".equals(e.getSQLState())) {
                String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                if (msg.contains("uq_producto_nombre") || msg.contains("(nombre)")) {
                    ultimoError = "Ya existe un producto con el nombre '" + p.getNombre() + "'.";
                } else if (msg.contains("codigo")) {
                    ultimoError = "Ya existe un producto con el código '" + p.getCodigo() + "'.";
                } else {
                    ultimoError = "Ya existe un registro con datos duplicados.";
                }
            } else {
                ultimoError = "Error al actualizar el producto: " + e.getMessage();
            }
            return false;
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        ultimoError = null;
        try {
            eliminarProducto(id);
            return true;
        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            ultimoError = "Error al eliminar el producto: " + e.getMessage();
            return false;
        }
    }

    // Método auxiliar privado para no duplicar código de mapeo
    private Producto mapearProducto(ResultSet rs) throws SQLException {
        var p = new Producto();
        p.setId(rs.getInt("id"));
        p.setCodigo(rs.getString("codigo"));
        p.setNombre(rs.getString("nombre"));
        p.setPrecioVenta(rs.getBigDecimal("precio_venta"));
        p.setExistencia(rs.getInt("existencia"));
        p.setRutaImagen(rs.getString("ruta_imagen"));
        p.setActivo(rs.getBoolean("activo"));

        // Construir la categoría si existe categoria_id
        int catId = rs.getInt("categoria_id");
        if (!rs.wasNull()) {
            var cat = new Categoria();
            cat.setId(catId);
            cat.setNombre(rs.getString("categoria_nombre"));
            cat.setActiva(rs.getBoolean("categoria_activa"));
            p.setCategoria(cat);
        }

        return p;
    }
}
