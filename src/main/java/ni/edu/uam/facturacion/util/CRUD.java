package ni.edu.uam.facturacion.util;

import java.util.List;

public interface CRUD<T, ID> {
    List<T> findAll();
    T findById(ID id);
    boolean crear(T entidad);
    boolean actualizar(T entidad);
    boolean eliminar(ID id);
}
