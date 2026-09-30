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
     * Valida el formulario de categoría y verifica duplicados.
     */
    private Categoria validarFormulario(Integer idCategoriaActual) {
        String nombre = txtNombre.getText() != null ? txtNombre.getText().trim() : "";
        if (nombre.isEmpty()) {
            mensaje(Alert.AlertType.WARNING, "El nombre de la categoría es obligatorio.");
            txtNombre.requestFocus();
            return null;
        }

        // Validación de nombres duplicados (insensible a mayúsculas/minúsculas)
        for (Categoria cat : categorias) {
            if (cat.getNombre() != null && cat.getNombre().equalsIgnoreCase(nombre)) {
                if (idCategoriaActual == null || !cat.getId().equals(idCategoriaActual)) {
                    mensaje(Alert.AlertType.WARNING, "Ya existe una categoría con el nombre '" + nombre + "'. No se permiten duplicados.");
                    txtNombre.requestFocus();
                    return null;
                }
            }
        }

        boolean activa = chkActiva.isSelected();
        return new Categoria(idCategoriaActual, nombre, activa);
    }

    /**
     * CREATE: Guardar nueva categoría.
     */
    @FXML
    private void guardar() {
        Categoria nueva = validarFormulario(null);
        if (nueva == null) {
            return;
        }

        if (categoriaDAO.crear(nueva)) {
            cargarCategorias();
            limpiar();
            mensaje(Alert.AlertType.INFORMATION, "Categoría registrada correctamente.");
        } else {
            String err = categoriaDAO.getUltimoError() != null ? categoriaDAO.getUltimoError() : "No se pudo registrar la categoría en la base de datos.";
            mensaje(Alert.AlertType.ERROR, err);
        }
    }

    /**
     * UPDATE: Modificar la categoría seleccionada.
     */
    @FXML
    private void actualizar() {
        if (categoriaSeleccionada == null || categoriaSeleccionada.getId() == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione una categoría de la tabla para actualizarla.");
            return;
        }

        Categoria modificada = validarFormulario(categoriaSeleccionada.getId());
        if (modificada == null) {
            return;
        }

        if (categoriaDAO.actualizar(modificada)) {
            cargarCategorias();
            limpiar();
            mensaje(Alert.AlertType.INFORMATION, "Categoría actualizada correctamente.");
        } else {
            String err = categoriaDAO.getUltimoError() != null ? categoriaDAO.getUltimoError() : "No se pudo actualizar la categoría en la base de datos.";
            mensaje(Alert.AlertType.ERROR, err);
        }
    }

    /**
     * DELETE: Eliminar la categoría seleccionada con confirmación y control de integridad referencial.
     */
    @FXML
    private void eliminar() {
        if (categoriaSeleccionada == null || categoriaSeleccionada.getId() == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione una categoría de la tabla para eliminarla.");
            return;
        }

        Alert confirmacion = new Alert(
            Alert.AlertType.CONFIRMATION,
            "¿Está seguro de que desea eliminar la categoría \"" + categoriaSeleccionada.getNombre() + "\"?",
            ButtonType.YES, ButtonType.NO
        );
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText(null);

        if (confirmacion.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            if (categoriaDAO.eliminar(categoriaSeleccionada.getId())) {
                cargarCategorias();
                limpiar();
                mensaje(Alert.AlertType.INFORMATION, "Categoría eliminada correctamente.");
            } else {
                String err = categoriaDAO.getUltimoError() != null ? categoriaDAO.getUltimoError() : "No se pudo eliminar la categoría.";
                mensaje(Alert.AlertType.ERROR, err);
            }
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

    private void mensaje(Alert.AlertType tipo, String texto) {
        Alert alerta = new Alert(tipo, texto, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
