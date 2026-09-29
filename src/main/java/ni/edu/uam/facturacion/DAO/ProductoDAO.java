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

    @Override
    public boolean crear(Producto p) {
        var sql = "INSERT INTO producto (codigo, nombre, categoria_id, precio_venta, existencia, ruta_imagen, activo) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, p.getCodigo());
            stmt.setString(2, p.getNombre());

            // Manejo de llave foránea opcional o nula
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
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error al crear producto: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean actualizar(Producto p) {
        var sql = "UPDATE producto SET codigo = ?, nombre = ?, categoria_id = ?, precio_venta = ?, "
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

            int filas = stmt.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean eliminar(Integer id) {
        var sql = "DELETE FROM producto WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int filas = stmt.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
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
