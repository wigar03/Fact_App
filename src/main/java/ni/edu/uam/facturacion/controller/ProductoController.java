package ni.edu.uam.facturacion.controller;

import javafx.animation.ScaleTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import ni.edu.uam.facturacion.DAO.CategoriaDAO;
import ni.edu.uam.facturacion.DAO.ProductoDAO;
import ni.edu.uam.facturacion.model.Categoria;
import ni.edu.uam.facturacion.model.Producto;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

public class ProductoController {

    // Controles del formulario
    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtNombre;

    @FXML
    private ComboBox<Categoria> cmbCategoria;

    @FXML
    private TextField txtPrecio;

    @FXML
    private TextField txtExistencia;

    @FXML
    private CheckBox chkActivo;

    @FXML
    private Label lblSinImagen;

    @FXML
    private ImageView imgProducto;

    @FXML
    private StackPane paneContenedorImagen;

    // Botones de acción del formulario (CRUD)
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

    @FXML
    private Button btnImagen;

    @FXML
    private Button btnQuitarImagen;

    // Controles de Búsqueda y Filtros
    @FXML
    private TextField txtBuscar;

    @FXML
    private Button btnLimpiarBusqueda;

    @FXML
    private ComboBox<String> cmbFiltroEstado;

    @FXML
    private ComboBox<Categoria> cmbFiltroCategoria;

    @FXML
    private Label lblConteoRegistros;

    // Tabla y columnas
    @FXML
    private TableView<Producto> tblProductos;

    @FXML
    private Pane paneOverlayTabla;

    @FXML
    private VBox zoomCardTabla;

    @FXML
    private ImageView imgZoomTabla;

    @FXML
    private Label lblZoomNombreTabla;

    @FXML
    private TableColumn<Producto, String> colImagen;

    @FXML
    private TableColumn<Producto, String> colCodigo;

    @FXML
    private TableColumn<Producto, String> colNombre;

    @FXML
    private TableColumn<Producto, String> colCategoria;

    @FXML
    private TableColumn<Producto, BigDecimal> colPrecio;

    @FXML
    private TableColumn<Producto, Integer> colExistencia;

    @FXML
    private TableColumn<Producto, Boolean> colActivo;

    // Colecciones observables (Secciones 7 y 13: ObservableList -> FilteredList -> TableView)
    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private FilteredList<Producto> productosFiltrados;
    private SortedList<Producto> productosOrdenados;

    private final Categoria TODAS_CATEGORIAS = new Categoria(null, "Todas las categorías", true);
    private String rutaImagen;
    private Producto productoSeleccionado;
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private Runnable alCerrar;

    @FXML
    private void initialize() {
        // 1. Configurar la tabla y el tamaño de fila
        tblProductos.setFixedCellSize(46.0);
        chkActivo.setSelected(true);

        // 2. Configuración de columnas
        configurarColumnasTabla();

        // 3. Cadena de datos observables (ObservableList -> FilteredList -> SortedList -> TableView)
        productosFiltrados = new FilteredList<>(productos, p -> true);
        productosOrdenados = new SortedList<>(productosFiltrados);
        productosOrdenados.comparatorProperty().bind(tblProductos.comparatorProperty());
        tblProductos.setItems(productosOrdenados);

        // 4. Listener de selección del TableView (Sección 9)
        tblProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            ocultarZoomTabla();
            if (newVal != null) {
                cargarDetalle(newVal);
            }
        });
        tblProductos.setOnScroll(e -> ocultarZoomTabla());

        // 5. Configurar opciones de filtros (Secciones 12 y 14)
        configurarFiltros();

        // 6. Estado inicial de los botones CRUD
        actualizarEstadoBotones(false);

        // 7. Cargar datos de la BD
        cargarCategorias();
        cargarProductos();
        actualizarVistaPreviaImagen(null);

        // 8. Configurar efecto de zoom al hacer hover en la imagen del formulario
        configurarEfectoHoverImagenFormulario();
    }

    private void configurarColumnasTabla() {
        // Columna de Foto / Imagen con zoom flotante en capa superior al hacer hover
        colImagen.setCellValueFactory(new PropertyValueFactory<>("rutaImagen"));
        colImagen.setCellFactory(col -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            private final Rectangle clip = new Rectangle(36, 36);

            {
                imageView.setFitWidth(36);
                imageView.setFitHeight(36);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                clip.setArcWidth(6);
                clip.setArcHeight(6);
                imageView.setClip(clip);

                setAlignment(Pos.CENTER);
                setCursor(Cursor.HAND);

                // Al entrar con el mouse a la celda: solo mostrar la tarjeta de imagen ampliada si ese producto está seleccionado
                setOnMouseEntered(e -> {
                    if (imageView.getImage() != null && getTableRow() != null) {
                        Producto p = getTableRow().getItem();
                        boolean estaSeleccionado = getTableRow().isSelected()
                                || (p != null && productoSeleccionado != null && p.getId() != null && p.getId().equals(productoSeleccionado.getId()));
                        if (estaSeleccionado) {
                            mostrarZoomTabla(this, p, imageView.getImage());
                        }
                    }
                });

                // Si se hace clic en la celda y se selecciona la fila, mostrar también el zoom
                setOnMouseClicked(e -> {
                    if (imageView.getImage() != null && getTableRow() != null) {
                        Producto p = getTableRow().getItem();
                        if (p != null) {
                            mostrarZoomTabla(this, p, imageView.getImage());
                        }
                    }
                });

                // Al salir con el mouse de la celda: ocultar la tarjeta ampliada
                setOnMouseExited(e -> ocultarZoomTabla());
            }

            @Override
            protected void updateItem(String ruta, boolean empty) {
                super.updateItem(ruta, empty);
                if (empty || ruta == null || ruta.isBlank()) {
                    setGraphic(null);
                    setText(empty ? null : "—");
                } else {
                    Image img = cargarImagenSegura(ruta, 72, 72);
                    if (img != null && !img.isError()) {
                        imageView.setImage(img);
                        setGraphic(imageView);
                        setText(null);
                    } else {
                        imageView.setImage(null);
                        setGraphic(null);
                        setText("—");
                    }
                }
            }
        });

        // Columna Código
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));

        // Columna Nombre
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        // Columna Categoría (muestra el nombre de la categoría del objeto Categoria)
        colCategoria.setCellValueFactory(cellData -> {
            Categoria cat = cellData.getValue().getCategoria();
            return new SimpleStringProperty(cat != null ? cat.getNombre() : "—");
        });

        // Columna Precio de Venta (formato moneda)
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colPrecio.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal precio, boolean empty) {
                super.updateItem(precio, empty);
                if (empty || precio == null) {
                    setText(null);
                } else {
                    setText(String.format("C$ %,.2f", precio));
                    setAlignment(Pos.CENTER_RIGHT);
                }
            }
        });

        // Columna Existencia
        colExistencia.setCellValueFactory(new PropertyValueFactory<>("existencia"));
        colExistencia.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer existencia, boolean empty) {
                super.updateItem(existencia, empty);
                if (empty || existencia == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(existencia));
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Columna Activo (badge visual de texto)
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));
        colActivo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean activo, boolean empty) {
                super.updateItem(activo, empty);
                if (empty || activo == null) {
                    setText(null);
                    setStyle("");
                } else {
                    if (activo) {
                        setText("Activo");
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold; -fx-alignment: center;");
                    } else {
                        setText("Inactivo");
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-alignment: center;");
                    }
                }
            }
        });
    }

    private void configurarFiltros() {
        // Opciones de filtro de estado
        cmbFiltroEstado.setItems(FXCollections.observableArrayList(
            "Todos los productos",
            "Productos activos",
            "Productos inactivos"
        ));
        cmbFiltroEstado.setValue("Todos los productos");

        // Listeners para búsqueda en vivo y filtros
        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        cmbFiltroEstado.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        cmbFiltroCategoria.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
    }

    /**
     * Aplica el predicado de filtrado a la FilteredList trabajando conjuntamente
     * con el texto de búsqueda y los selectores de estado y categoría (Secciones 12, 13 y 14).
     */
    private void aplicarFiltros() {
        ocultarZoomTabla();
        String busqueda = txtBuscar.getText() != null ? txtBuscar.getText().trim().toLowerCase() : "";
        String estadoSeleccionado = cmbFiltroEstado.getValue() != null ? cmbFiltroEstado.getValue() : "Todos los productos";
        Categoria categoriaSeleccionada = cmbFiltroCategoria.getValue();

        productosFiltrados.setPredicate(p -> {
            if (p == null) return false;

            // 1. Filtro de búsqueda (insensible a mayúsculas/minúsculas por código, nombre o categoría)
            if (!busqueda.isEmpty()) {
                boolean coincideCodigo = p.getCodigo() != null && p.getCodigo().toLowerCase().contains(busqueda);
                boolean coincideNombre = p.getNombre() != null && p.getNombre().toLowerCase().contains(busqueda);
                boolean coincideCategoria = p.getCategoria() != null && p.getCategoria().getNombre() != null
                        && p.getCategoria().getNombre().toLowerCase().contains(busqueda);

                if (!coincideCodigo && !coincideNombre && !coincideCategoria) {
                    return false;
                }
            }

            // 2. Filtro por estado activo / inactivo
            if ("Productos activos".equals(estadoSeleccionado) && !p.isActivo()) {
                return false;
            }
            if ("Productos inactivos".equals(estadoSeleccionado) && p.isActivo()) {
                return false;
            }

            // 3. Filtro por categoría seleccionada
            if (categoriaSeleccionada != null && categoriaSeleccionada.getId() != null) {
                if (p.getCategoria() == null || !categoriaSeleccionada.getId().equals(p.getCategoria().getId())) {
                    return false;
                }
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
        int total = productos.size();
        int visibles = productosFiltrados != null ? productosFiltrados.size() : total;
        if (lblConteoRegistros != null) {
            lblConteoRegistros.setText(visibles + " de " + total + " productos");
        }
    }

    public void cargarCategorias() {
        List<Categoria> listaDB = categoriaDAO.findAll();
        cmbCategoria.setItems(FXCollections.observableArrayList(listaDB));

        ObservableList<Categoria> opcionesFiltro = FXCollections.observableArrayList();
        opcionesFiltro.add(TODAS_CATEGORIAS);
        opcionesFiltro.addAll(listaDB);

        Categoria seleccionActual = cmbFiltroCategoria.getValue();
        cmbFiltroCategoria.setItems(opcionesFiltro);

        if (seleccionActual != null && seleccionActual.getId() != null) {
            for (Categoria c : opcionesFiltro) {
                if (seleccionActual.getId().equals(c.getId())) {
                    cmbFiltroCategoria.setValue(c);
                    return;
                }
            }
        }
        cmbFiltroCategoria.setValue(TODAS_CATEGORIAS);
    }

    public void cargarProductos() {
        productos.setAll(productoDAO.findAll());
        aplicarFiltros();
    }

    public ObservableList<Producto> getProductos() {
        return productos;
    }

    public ComboBox<Categoria> getCmbCategoria() {
        return cmbCategoria;
    }

    /**
     * Carga en el formulario los datos del producto seleccionado en la tabla (Sección 9).
     */
    private void cargarDetalle(Producto p) {
        if (p == null) return;
        productoSeleccionado = p;

        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());

        if (p.getCategoria() != null && p.getCategoria().getId() != null) {
            for (Categoria cat : cmbCategoria.getItems()) {
                if (cat.getId() != null && cat.getId().equals(p.getCategoria().getId())) {
                    cmbCategoria.setValue(cat);
                    break;
                }
            }
        } else {
            cmbCategoria.getSelectionModel().clearSelection();
        }

        txtPrecio.setText(p.getPrecioVenta() != null ? p.getPrecioVenta().toPlainString() : "");
        txtExistencia.setText(String.valueOf(p.getExistencia()));
        chkActivo.setSelected(p.isActivo());

        rutaImagen = p.getRutaImagen();
        actualizarVistaPreviaImagen(rutaImagen);

        actualizarEstadoBotones(true);
    }

    private void actualizarEstadoBotones(boolean productoSeleccionado) {
        if (btnGuardar != null) btnGuardar.setDisable(productoSeleccionado);
        if (btnActualizar != null) btnActualizar.setDisable(!productoSeleccionado);
        if (btnEliminar != null) btnEliminar.setDisable(!productoSeleccionado);
    }

    /**
     * Valida exhaustivamente los datos del formulario según los requisitos de la Sección 15.
     * @param idProductoActual null si es creación (CREATE), o el ID del producto si es actualización (UPDATE).
     * @return El objeto Producto listo para persistir, o null si falla alguna validación.
     */
    private Producto validarFormulario(Integer idProductoActual) {
        // 1. Código obligatorio
        String codigo = txtCodigo.getText() != null ? txtCodigo.getText().trim() : "";
        if (codigo.isEmpty()) {
            mensaje(Alert.AlertType.WARNING, "El código es obligatorio.");
            txtCodigo.requestFocus();
            return null;
        }

        // 2. Nombre obligatorio
        String nombre = txtNombre.getText() != null ? txtNombre.getText().trim() : "";
        if (nombre.isEmpty()) {
            mensaje(Alert.AlertType.WARNING, "El nombre es obligatorio.");
            txtNombre.requestFocus();
            return null;
        }

        // 3. Debe seleccionar una categoría
        Categoria categoria = cmbCategoria.getValue();
        if (categoria == null || categoria.getId() == null) {
            mensaje(Alert.AlertType.WARNING, "Debe seleccionar una categoría.");
            cmbCategoria.requestFocus();
            return null;
        }

        // 4 y 5. El precio debe ser numérico y mayor que cero
        String precioStr = txtPrecio.getText() != null ? txtPrecio.getText().trim() : "";
        if (precioStr.isEmpty()) {
            mensaje(Alert.AlertType.WARNING, "El precio es obligatorio.");
            txtPrecio.requestFocus();
            return null;
        }
        BigDecimal precio;
        try {
            precio = new BigDecimal(precioStr);
        } catch (NumberFormatException e) {
            mensaje(Alert.AlertType.WARNING, "El precio debe ser numérico.");
            txtPrecio.requestFocus();
            return null;
        }
        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            mensaje(Alert.AlertType.WARNING, "El precio debe ser mayor que cero.");
            txtPrecio.requestFocus();
            return null;
        }

        // 6 y 7. La existencia debe ser un número entero y no puede ser negativa
        String existenciaStr = txtExistencia.getText() != null ? txtExistencia.getText().trim() : "";
        if (existenciaStr.isEmpty()) {
            mensaje(Alert.AlertType.WARNING, "La existencia es obligatoria.");
            txtExistencia.requestFocus();
            return null;
        }
        int existencia;
        try {
            existencia = Integer.parseInt(existenciaStr);
        } catch (NumberFormatException e) {
            mensaje(Alert.AlertType.WARNING, "La existencia debe ser un número entero.");
            txtExistencia.requestFocus();
            return null;
        }
        if (existencia < 0) {
            mensaje(Alert.AlertType.WARNING, "La existencia no puede ser negativa.");
            txtExistencia.requestFocus();
            return null;
        }

        // 8. No se permiten códigos duplicados
        for (Producto prod : productos) {
            if (prod.getCodigo() != null && prod.getCodigo().equalsIgnoreCase(codigo)) {
                if (idProductoActual == null || !prod.getId().equals(idProductoActual)) {
                    mensaje(Alert.AlertType.WARNING, "Ya existe un producto con el código '" + codigo + "'. No se permiten códigos duplicados.");
                    txtCodigo.requestFocus();
                    return null;
                }
            }
        }

        boolean activo = chkActivo.isSelected();
        return new Producto(idProductoActual, codigo, nombre, categoria, precio, existencia, rutaImagen, activo);
    }

    /**
     * Operación CREATE (Sección 8):
     * Guarda un nuevo producto a partir de los datos del formulario.
     */
    @FXML
    private void guardar() {
        if (cmbCategoria.getItems().isEmpty()) {
            mensaje(Alert.AlertType.WARNING, "No hay categorías registradas.\nDebe crear al menos una categoría primero.");
            return;
        }

        Producto nuevo = validarFormulario(null);
        if (nuevo == null) {
            return;
        }

        if (productoDAO.crear(nuevo)) {
            cargarProductos();
            limpiar();
            mensaje(Alert.AlertType.INFORMATION, "Producto registrado correctamente.");
        } else {
            String err = productoDAO.getUltimoError() != null ? productoDAO.getUltimoError() : "No se pudo registrar el producto en la base de datos.";
            mensaje(Alert.AlertType.ERROR, err);
        }
    }

    /**
     * Operación UPDATE (Sección 10):
     * Modifica el producto seleccionado en el TableView. No crea un nuevo registro.
     */
    @FXML
    private void actualizar() {
        if (productoSeleccionado == null || productoSeleccionado.getId() == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione un producto en la tabla para actualizar sus datos.");
            return;
        }

        Producto modificado = validarFormulario(productoSeleccionado.getId());
        if (modificado == null) {
            return;
        }

        if (productoDAO.actualizar(modificado)) {
            cargarProductos();
            limpiar();
            mensaje(Alert.AlertType.INFORMATION, "Producto actualizado correctamente.");
        } else {
            String err = productoDAO.getUltimoError() != null ? productoDAO.getUltimoError() : "No se pudo actualizar el producto en la base de datos.";
            mensaje(Alert.AlertType.ERROR, err);
        }
    }

    /**
     * Operación DELETE (Sección 11):
     * Solicita confirmación y elimina el producto seleccionado.
     */
    @FXML
    private void eliminar() {
        if (productoSeleccionado == null || productoSeleccionado.getId() == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione un producto en la tabla para eliminarlo.");
            return;
        }

        Alert confirmacion = new Alert(
            Alert.AlertType.CONFIRMATION,
            "¿Está seguro de que desea eliminar el producto \"" + productoSeleccionado.getNombre() + "\"?",
            ButtonType.YES, ButtonType.NO
        );
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText(null);

        if (confirmacion.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            if (productoDAO.eliminar(productoSeleccionado.getId())) {
                cargarProductos();
                limpiar();
                mensaje(Alert.AlertType.INFORMATION, "Producto eliminado correctamente.");
            } else {
                String err = productoDAO.getUltimoError() != null ? productoDAO.getUltimoError() : "No se pudo eliminar el producto.";
                mensaje(Alert.AlertType.ERROR, err);
            }
        }
    }

    /**
     * Limpia el formulario y deselecciona la tabla, dejando listos los controles para crear un nuevo producto.
     */
    @FXML
    private void limpiar() {
        productoSeleccionado = null;
        tblProductos.getSelectionModel().clearSelection();
        txtCodigo.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtExistencia.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        chkActivo.setSelected(true);
        rutaImagen = null;
        actualizarVistaPreviaImagen(null);
        actualizarEstadoBotones(false);

        if (imgProducto != null) {
            imgProducto.setScaleX(1.0);
            imgProducto.setScaleY(1.0);
            imgProducto.setEffect(null);
        }
        ocultarZoomTabla();
    }

    private void mostrarZoomTabla(TableCell<Producto, String> cell, Producto p, Image thumbnailImg) {
        if (paneOverlayTabla == null || zoomCardTabla == null || cell == null) {
            return;
        }

        String ruta = p != null ? p.getRutaImagen() : null;
        Image imgGrande = (ruta != null && !ruta.isBlank()) ? cargarImagenSegura(ruta, 200, 200) : thumbnailImg;
        if (imgGrande == null || imgGrande.isError()) {
            return;
        }

        imgZoomTabla.setImage(imgGrande);
        if (p != null && p.getNombre() != null) {
            lblZoomNombreTabla.setText(p.getNombre());
            lblZoomNombreTabla.setVisible(true);
        } else {
            lblZoomNombreTabla.setVisible(false);
        }

        // Posicionar en coordenadas locales de paneOverlayTabla (capa superior no recortada)
        Point2D cellScene = cell.localToScene(0, 0);
        Point2D local = paneOverlayTabla.sceneToLocal(cellScene);
        if (local != null) {
            double x = Math.max(8.0, local.getX() + cell.getWidth() + 8.0);
            double y = Math.max(8.0, local.getY() - 45.0);

            if (paneOverlayTabla.getHeight() > 0 && y + 175.0 > paneOverlayTabla.getHeight()) {
                y = Math.max(8.0, paneOverlayTabla.getHeight() - 180.0);
            }

            zoomCardTabla.setLayoutX(x);
            zoomCardTabla.setLayoutY(y);
            zoomCardTabla.setVisible(true);

            ScaleTransition st = new ScaleTransition(Duration.millis(120), zoomCardTabla);
            st.setFromX(0.7);
            st.setFromY(0.7);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        }
    }

    private void ocultarZoomTabla() {
        if (zoomCardTabla != null) {
            zoomCardTabla.setVisible(false);
        }
    }

    private void configurarEfectoHoverImagenFormulario() {
        if (paneContenedorImagen != null) {
            paneContenedorImagen.setCursor(Cursor.HAND);

            paneContenedorImagen.setOnMouseEntered(e -> {
                if (productoSeleccionado != null && imgProducto != null && imgProducto.getImage() != null) {
                    paneContenedorImagen.setViewOrder(-10.0);
                    imgProducto.setViewOrder(-10.0);
                    ScaleTransition st = new ScaleTransition(Duration.millis(180), imgProducto);
                    st.setToX(1.75);
                    st.setToY(1.75);
                    st.play();
                    imgProducto.setEffect(new DropShadow(18, Color.rgb(0, 0, 0, 0.45)));
                }
            });

            paneContenedorImagen.setOnMouseExited(e -> {
                if (imgProducto != null) {
                    ScaleTransition st = new ScaleTransition(Duration.millis(180), imgProducto);
                    st.setToX(1.0);
                    st.setToY(1.0);
                    st.play();
                    imgProducto.setEffect(null);
                    imgProducto.setViewOrder(0.0);
                    paneContenedorImagen.setViewOrder(0.0);
                }
            });
        }
    }

    @FXML
    private void seleccionarImagen() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Imágenes (*.png, *.jpg, *.jpeg)", "*.png", "*.jpg", "*.jpeg")
        );
        File archivo = chooser.showOpenDialog(txtCodigo.getScene().getWindow());
        if (archivo != null) {
            rutaImagen = archivo.toURI().toString();
            actualizarVistaPreviaImagen(rutaImagen);
        }
    }

    @FXML
    private void quitarImagen() {
        rutaImagen = null;
        actualizarVistaPreviaImagen(null);
    }

    private void actualizarVistaPreviaImagen(String ruta) {
        if (ruta != null && !ruta.isBlank()) {
            Image img = cargarImagenSegura(ruta, 96, 96);
            if (img != null && !img.isError()) {
                imgProducto.setImage(img);
                if (lblSinImagen != null) lblSinImagen.setVisible(false);
                return;
            }
        }
        imgProducto.setImage(null);
        if (lblSinImagen != null) lblSinImagen.setVisible(true);
    }

    private Image cargarImagenSegura(String ruta, double w, double h) {
        if (ruta == null || ruta.isBlank()) {
            return null;
        }
        try {
            String url = ruta.trim();
            if (!url.startsWith("file:") && !url.startsWith("http:") && !url.startsWith("https:")) {
                File f = new File(url);
                if (f.exists()) {
                    url = f.toURI().toString();
                } else {
                    return null;
                }
            }
            Image img = new Image(url, w, h, true, true, false);
            return img.isError() ? null : img;
        } catch (Exception e) {
            return null;
        }
    }

    public void setAlCerrar(Runnable alCerrar) {
        this.alCerrar = alCerrar;
    }

    @FXML
    private void cerrar() {
        ocultarZoomTabla();
        if (alCerrar != null) {
            alCerrar.run();
        } else {
            Stage stage = (Stage) txtCodigo.getScene().getWindow();
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
