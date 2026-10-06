package ni.edu.uam.facturacion.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ni.edu.uam.facturacion.DAO.CategoriaDAO;
import ni.edu.uam.facturacion.model.Categoria;
import java.sql.SQLException;

public class CategoriaController {

    // Formulario
    @FXML
    private TextField txtNombre;

    @FXML
    private CheckBox chkActiva;

    // Botones de acción CRUD
    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnActualizar;

    @FXML
    private Button btnEliminar;

    @FXML
    private Button btnNuevo;

    @FXML
    private Button btnCerrar;

    // Búsqueda y Filtros
    @FXML
    private TextField txtBuscar;

    @FXML
    private Button btnLimpiarBusqueda;

    @FXML
    private ComboBox<String> cmbFiltroEstado;

    @FXML
    private Label lblConteoRegistros;

    // Tabla y columnas
    @FXML
    private TableView<Categoria> tblCategorias;

    @FXML
    private TableColumn<Categoria, Integer> colId;

    @FXML
    private TableColumn<Categoria, String> colNombre;

    @FXML
    private TableColumn<Categoria, Boolean> colActiva;

    // Colecciones observables
    private final ObservableList<Categoria> categorias = FXCollections.observableArrayList();
    private FilteredList<Categoria> categoriasFiltradas;
    private SortedList<Categoria> categoriasOrdenadas;

    private Categoria categoriaSeleccionada;
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private Runnable alCerrar;

    @FXML
    private void initialize() {
        tblCategorias.setFixedCellSize(40.0);
        chkActiva.setSelected(true);

        configurarColumnasTabla();

        // Cadena de datos observables: ObservableList -> FilteredList -> SortedList -> TableView
        categoriasFiltradas = new FilteredList<>(categorias, c -> true);
        categoriasOrdenadas = new SortedList<>(categoriasFiltradas);
        categoriasOrdenadas.comparatorProperty().bind(tblCategorias.comparatorProperty());
        tblCategorias.setItems(categoriasOrdenadas);

        // Listener de selección en la tabla
        tblCategorias.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cargarDetalle(newVal);
            }
        });

        configurarFiltros();
        actualizarEstadoBotones(false);
        cargarCategorias();
    }

    private void configurarColumnasTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(id));
                    setAlignment(Pos.CENTER);
                }
            }
        });

        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        colActiva.setCellValueFactory(new PropertyValueFactory<>("activa"));
        colActiva.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean activa, boolean empty) {
                super.updateItem(activa, empty);
                if (empty || activa == null) {
                    setText(null);
                    setStyle("");
                } else {
                    if (activa) {
                        setText("Activa");
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold; -fx-alignment: center;");
                    } else {
                        setText("Inactiva");
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-alignment: center;");
                    }
                }
            }
        });
    }

    private void configurarFiltros() {
        cmbFiltroEstado.setItems(FXCollections.observableArrayList(
            "Todas las categorías",
            "Categorías activas",
            "Categorías inactivas"
        ));
        cmbFiltroEstado.setValue("Todas las categorías");

        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        cmbFiltroEstado.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
    }

    private void aplicarFiltros() {
        String busqueda = txtBuscar.getText() != null ? txtBuscar.getText().trim().toLowerCase() : "";
        String estadoSeleccionado = cmbFiltroEstado.getValue() != null ? cmbFiltroEstado.getValue() : "Todas las categorías";

        categoriasFiltradas.setPredicate(cat -> {
            if (cat == null) return false;

            // Búsqueda insensible a mayúsculas/minúsculas por nombre
            if (!busqueda.isEmpty()) {
                boolean coincideNombre = cat.getNombre() != null && cat.getNombre().toLowerCase().contains(busqueda);
                if (!coincideNombre) {
                    return false;
                }
            }

            // Filtro por estado activo / inactivo
            if ("Categorías activas".equals(estadoSeleccionado) && !cat.isActiva()) {
                return false;
            }
            if ("Categorías inactivas".equals(estadoSeleccionado) && cat.isActiva()) {
                return false;
            }

            return true;
        });

        actualizarConteo();
    }

    @FXML
    private void limpiarBusqueda() {
        txtBuscar.clear();
    }

    private void actualizarConteo() {
        int total = categorias.size();
        int visibles = categoriasFiltradas != null ? categoriasFiltradas.size() : total;
        if (lblConteoRegistros != null) {
            lblConteoRegistros.setText(visibles + " de " + total + " categorías");
        }
    }

    public void cargarCategorias() {
        categorias.setAll(categoriaDAO.findAll());
        aplicarFiltros();
    }

    public ObservableList<Categoria> getCategorias() {
        return categorias;
    }

    private void cargarDetalle(Categoria c) {
        if (c == null) return;
        categoriaSeleccionada = c;
        txtNombre.setText(c.getNombre());
        chkActiva.setSelected(c.isActiva());
        actualizarEstadoBotones(true);
    }

    private void actualizarEstadoBotones(boolean seleccionada) {
        if (btnGuardar != null) btnGuardar.setDisable(seleccionada);
        if (btnActualizar != null) btnActualizar.setDisable(!seleccionada);
        if (btnEliminar != null) btnEliminar.setDisable(!seleccionada);
    }

    /**
     * Valida que el nombre de la categoría no esté vacío ni contenga solo espacios.
     * Sección 4 de la guía.
     */
    private boolean validarCategoria() {
        String nombre = txtNombre.getText() != null ? txtNombre.getText().trim() : "";
        if (nombre.isEmpty()) {
            mostrarError(
                "Validación",
                "El nombre de la categoría es obligatorio."
            );
            txtNombre.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * CREATE: Guardar nueva categoría.
     * Secciones 4, 5 y 15 de la guía.
     */
    @FXML
    private void guardar() {
        if (!validarCategoria()) {
            return;
        }

        String nombre = txtNombre.getText().trim();
        boolean activa = chkActiva.isSelected();

        try {
            // Sección 5: Evitar categorías duplicadas mediante consulta previa
            if (categoriaDAO.existeNombre(nombre)) {
                mostrarAdvertencia(
                    "Nombre duplicado",
                    "Ya existe una categoría con ese nombre."
                );
                txtNombre.requestFocus();
                return;
            }

            Categoria nueva = new Categoria(null, nombre, activa);
            categoriaDAO.guardar(nueva);

            mostrarExito(
                "Categoría registrada",
                "La información fue almacenada correctamente."
            );

            cargarCategorias();
            limpiar();

        } catch (SQLException e) {
            mostrarError(
                "Error de base de datos",
                "No fue posible registrar la categoría."
            );
            System.err.println(e.getMessage());
        }
    }

    /**
     * UPDATE: Modificar la categoría seleccionada.
     * Secciones 5, 6 y 19 de la guía.
     */
    @FXML
    private void actualizar() {
        // Sección 6: Comprobar selección previa
        Categoria seleccionada = tblCategorias.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAdvertencia(
                "Seleccione una categoría",
                "Debe seleccionar la categoría que desea actualizar."
            );
            return;
        }

        // Validar nuevamente el nombre antes de ejecutar el UPDATE
        if (!validarCategoria()) {
            return;
        }

        String nombre = txtNombre.getText().trim();
        boolean activa = chkActiva.isSelected();

        try {
            // Sección 5: Excluir de la búsqueda la categoría que se está modificando
            if (categoriaDAO.existeNombre(nombre, seleccionada.getId())) {
                mostrarAdvertencia(
                    "Nombre duplicado",
                    "Ya existe una categoría con ese nombre."
                );
                txtNombre.requestFocus();
                return;
            }

            seleccionada.setNombre(nombre);
            seleccionada.setActiva(activa);

            categoriaDAO.actualizarCategoria(seleccionada);

            mostrarExito(
                "Categoría actualizada",
                "La información fue actualizada correctamente."
            );

            cargarCategorias();
            limpiar();

        } catch (SQLException e) {
            mostrarError(
                "Error de base de datos",
                "No fue posible actualizar la categoría."
            );
            System.err.println(e.getMessage());
        }
    }

    /**
     * DELETE: Eliminar la categoría seleccionada previa validación de integridad referencial.
     * Secciones 6, 7, 20 y 21 de la guía.
     */
    @FXML
    private void eliminar() {
        Categoria seleccionada = tblCategorias.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAdvertencia(
                "Seleccione una categoría",
                "Debe seleccionar la categoría que desea eliminar."
            );
            return;
        }

        try {
            // Sección 7: Comprobar si existen productos asociados antes del DELETE
            if (categoriaDAO.tieneProductos(seleccionada.getId())) {
                mostrarAdvertencia(
                    "Operación no permitida",
                    "No puede eliminar la categoría porque tiene productos asociados."
                );
                return;
            }

            Alert confirmacion = new Alert(
                Alert.AlertType.CONFIRMATION,
                "¿Está seguro de que desea eliminar la categoría \"" + seleccionada.getNombre() + "\"?",
                ButtonType.YES, ButtonType.NO
            );
            confirmacion.setTitle("Confirmar Eliminación");
            confirmacion.setHeaderText(null);

            if (confirmacion.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                categoriaDAO.eliminarCategoria(seleccionada.getId());

                mostrarExito(
                    "Categoría eliminada",
                    "La categoría fue eliminada correctamente."
                );

                cargarCategorias();
                limpiar();
            }

        } catch (SQLException e) {
            mostrarError(
                "Error de base de datos",
                "No fue posible completar la operación."
            );
            System.err.println(e.getMessage());
        }
    }

    /**
     * Limpia el formulario y deselecciona la tabla.
     */
    @FXML
    private void limpiar() {
        categoriaSeleccionada = null;
        tblCategorias.getSelectionModel().clearSelection();
        txtNombre.clear();
        chkActiva.setSelected(true);
        actualizarEstadoBotones(false);
    }

    public void setAlCerrar(Runnable alCerrar) {
        this.alCerrar = alCerrar;
    }

    @FXML
    private void cerrar() {
        if (alCerrar != null) {
            alCerrar.run();
        } else {
            Stage stage = (Stage) txtNombre.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        }
    }

    private void mostrarError(String titulo, String contenido) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }

    private void mostrarAdvertencia(String titulo, String contenido) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }

    private void mostrarExito(String titulo, String contenido) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        Alert alerta = new Alert(tipo, texto, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
