package ni.edu.uam.facturacion.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ni.edu.uam.facturacion.DAO.CategoriaDAO;
import ni.edu.uam.facturacion.DAO.ProductoDAO;
import ni.edu.uam.facturacion.model.Categoria;
import ni.edu.uam.facturacion.model.Producto;

import java.io.File;
import java.math.BigDecimal;

public class ProductoController {

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
    private TableView<Producto> tblProductos;

    @FXML
    private TableColumn<Producto, String> colImagen;

    @FXML
    private TableColumn<Producto, String> colCodigo;

    @FXML
    private TableColumn<Producto, String> colNombre;

    @FXML
    private TableColumn<Producto, Categoria> colCategoria;

    @FXML
    private TableColumn<Producto, BigDecimal> colPrecio;

    @FXML
    private TableColumn<Producto, Integer> colExistencia;

    @FXML
    private TableColumn<Producto, Boolean> colActivo;

    @FXML
    private Button btnImagen;

    @FXML
    private Button btnQuitarImagen;

    @FXML
    private Button btnEliminar;

    @FXML
    private Button btnNuevo;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnCerrar;

    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private String rutaImagen;
    private Producto productoSeleccionado;
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    @FXML
    private void initialize() {
        tblProductos.setItems(productos);
        tblProductos.setFixedCellSize(46.0);
        chkActivo.setSelected(true);
        if (btnEliminar != null) {
            btnEliminar.setDisable(true);
        }

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
            }

            @Override
            protected void updateItem(String ruta, boolean empty) {
                super.updateItem(ruta, empty);
                if (empty || ruta == null || ruta.isBlank()) {
                    setGraphic(null);
                    setText(empty ? null : "—");
                } else {
                    Image img = cargarImagenSegura(ruta, 36, 36);
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

        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colExistencia.setCellValueFactory(new PropertyValueFactory<>("existencia"));
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));

        tblProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cargarDetalle(newVal);
            }
        });

        cargarCategorias();
        cargarProductos();
        actualizarVistaPreviaImagen(null);
    }

    public void cargarCategorias() {
        cmbCategoria.setItems(FXCollections.observableArrayList(categoriaDAO.findAll()));
    }

    public void cargarProductos() {
        productos.setAll(productoDAO.findAll());
    }

    public ObservableList<Producto> getProductos() {
        return productos;
    }

    public ComboBox<Categoria> getCmbCategoria() {
        return cmbCategoria;
    }

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

        if (btnEliminar != null) btnEliminar.setDisable(false);
        if (btnGuardar != null) btnGuardar.setText("Actualizar");
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

    @FXML
    private void seleccionarImagen() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
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

    @FXML
    private void guardar() {
        if (cmbCategoria.getItems().isEmpty()) {
            mensaje(Alert.AlertType.WARNING, "No hay categorías registradas en la base de datos.\nDebe crear al menos una categoría primero.");
            return;
        }

        if (txtCodigo.getText() == null || txtCodigo.getText().isBlank()
            || txtNombre.getText() == null || txtNombre.getText().isBlank()
            || txtPrecio.getText() == null || txtPrecio.getText().isBlank()
            || txtExistencia.getText() == null || txtExistencia.getText().isBlank()
            || cmbCategoria.getValue() == null) {
            mensaje(Alert.AlertType.WARNING, "Complete los campos obligatorios.");
            return;
        }

        try {
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            int existencia = Integer.parseInt(txtExistencia.getText().trim());

            if (precio.signum() <= 0 || existencia < 0) {
                mensaje(Alert.AlertType.WARNING,
                    "Precio mayor que cero y existencia no negativa.");
                return;
            }

            if (productoSeleccionado != null && productoSeleccionado.getId() != null) {
                Producto actualizado = new Producto(
                    productoSeleccionado.getId(),
                    txtCodigo.getText().trim(),
                    txtNombre.getText().trim(),
                    cmbCategoria.getValue(),
                    precio,
                    existencia,
                    rutaImagen,
                    chkActivo.isSelected()
                );

                if (productoDAO.actualizar(actualizado)) {
                    cargarProductos();
                    mensaje(Alert.AlertType.INFORMATION, "Producto actualizado correctamente en la base de datos.");
                    limpiar();
                } else {
                    String err = productoDAO.getUltimoError() != null ? productoDAO.getUltimoError() : "No se pudo actualizar el producto en la base de datos.";
                    mensaje(Alert.AlertType.ERROR, err);
                }
            } else {
                Producto nuevo = new Producto(
                    null,
                    txtCodigo.getText().trim(),
                    txtNombre.getText().trim(),
                    cmbCategoria.getValue(),
                    precio,
                    existencia,
                    rutaImagen,
                    chkActivo.isSelected()
                );

                if (productoDAO.crear(nuevo)) {
                    cargarProductos();
                    mensaje(Alert.AlertType.INFORMATION, "Producto guardado correctamente en la base de datos.");
                    limpiar();
                } else {
                    String err = productoDAO.getUltimoError() != null ? productoDAO.getUltimoError() : "No se pudo guardar el producto en la base de datos.";
                    mensaje(Alert.AlertType.ERROR, err);
                }
            }
        } catch (NumberFormatException e) {
            mensaje(Alert.AlertType.ERROR, "Precio o existencia no válidos.");
        }
    }

    @FXML
    private void eliminar() {
        if (productoSeleccionado == null || productoSeleccionado.getId() == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione un producto de la tabla para eliminarlo.");
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
                mensaje(Alert.AlertType.INFORMATION, "Producto eliminado correctamente.");
                limpiar();
            } else {
                String err = productoDAO.getUltimoError() != null ? productoDAO.getUltimoError() : "No se pudo eliminar el producto.";
                mensaje(Alert.AlertType.ERROR, err);
            }
        }
    }

    private Runnable alCerrar;

    public void setAlCerrar(Runnable alCerrar) {
        this.alCerrar = alCerrar;
    }

    @FXML
    private void cerrar() {
        if (alCerrar != null) {
            alCerrar.run();
        } else {
            Stage stage = (Stage) txtCodigo.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        }
    }

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
        if (btnEliminar != null) btnEliminar.setDisable(true);
        if (btnGuardar != null) btnGuardar.setText("Guardar");
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}
