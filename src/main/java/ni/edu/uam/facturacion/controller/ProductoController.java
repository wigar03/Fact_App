package ni.edu.uam.facturacion.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ni.edu.uam.facturacion.model.Categoria;
import ni.edu.uam.facturacion.model.Producto;
import ni.edu.uam.facturacion.util.SceneManager;

import java.math.BigDecimal;

public class ProductoController {

    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtPrecio;

    @FXML
    private TextField txtStock;

    @FXML
    private ComboBox<Categoria> cmbCategoria;

    @FXML
    private TableView<Producto> tblProductos;

    @FXML
    private TableColumn<Producto, String> colCodigo;

    @FXML
    private TableColumn<Producto, String> colNombre;

    @FXML
    private TableColumn<Producto, Categoria> colCategoria;

    @FXML
    private TableColumn<Producto, BigDecimal> colPrecio;

    @FXML
    private TableColumn<Producto, Integer> colStock;

    @FXML
    private Label lblMensaje;

    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
    private final ObservableList<Categoria> listaCategorias = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("existencia"));

        tblProductos.setItems(listaProductos);

        Categoria cat1 = new Categoria(1, "Electrónica", true);
        Categoria cat2 = new Categoria(2, "Papelería", true);
        Categoria cat3 = new Categoria(3, "Bebidas", true);
        Categoria cat4 = new Categoria(4, "Snacks", true);

        listaCategorias.addAll(cat1, cat2, cat3, cat4);
        cmbCategoria.setItems(listaCategorias);
        if (!listaCategorias.isEmpty()) {
            cmbCategoria.getSelectionModel().selectFirst();
        }

        listaProductos.add(new Producto(1, "PRD-001", "Laptop HP 15", cat1, new BigDecimal("750.00"), 10, null, true));
        listaProductos.add(new Producto(2, "PRD-002", "Cuaderno Espiral Universitario", cat2, new BigDecimal("2.50"), 50, null, true));
        listaProductos.add(new Producto(3, "PRD-003", "Café Helado 250ml", cat3, new BigDecimal("1.80"), 25, null, true));
    }

    @FXML
    public void onAgregarClick(ActionEvent event) {
        String codigo = txtCodigo.getText() != null ? txtCodigo.getText().trim() : "";
        String nombre = txtNombre.getText() != null ? txtNombre.getText().trim() : "";
        String strPrecio = txtPrecio.getText() != null ? txtPrecio.getText().trim() : "";
        String strStock = txtStock.getText() != null ? txtStock.getText().trim() : "";
        Categoria categoria = cmbCategoria.getValue();

        if (codigo.isEmpty() || nombre.isEmpty() || strPrecio.isEmpty() || strStock.isEmpty() || categoria == null) {
            mostrarMensaje("Por favor complete todos los campos.", true);
            return;
        }

        BigDecimal precio;
        int stock;

        try {
            precio = new BigDecimal(strPrecio);
            stock = Integer.parseInt(strStock);
            if (precio.compareTo(BigDecimal.ZERO) < 0 || stock < 0) {
                mostrarMensaje("El precio y stock deben ser valores positivos.", true);
                return;
            }
        } catch (NumberFormatException e) {
            mostrarMensaje("Precio y stock deben ser valores numéricos válidos.", true);
            return;
        }

        boolean existe = listaProductos.stream().anyMatch(p -> p.getCodigo().equalsIgnoreCase(codigo));
        if (existe) {
            mostrarMensaje("Ya existe un producto con el código: " + codigo, true);
            return;
        }

        Producto nuevo = new Producto(listaProductos.size() + 1, codigo, nombre, categoria, precio, stock, null, true);
        listaProductos.add(nuevo);
        mostrarMensaje("Producto agregado exitosamente.", false);
        limpiarFormulario();
    }

    @FXML
    public void onLimpiarClick(ActionEvent event) {
        limpiarFormulario();
        lblMensaje.setText("");
    }

    @FXML
    public void onRegresarClick(ActionEvent event) {
        SceneManager.cambiarEscena("menu-principal.fxml", "Sistema de Facturación - UAM");
    }

    private void limpiarFormulario() {
        txtCodigo.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtStock.clear();
        if (!listaCategorias.isEmpty()) {
            cmbCategoria.getSelectionModel().selectFirst();
        }
        txtCodigo.requestFocus();
    }

    private void mostrarMensaje(String mensaje, boolean esError) {
        lblMensaje.setText(mensaje);
        if (esError) {
            lblMensaje.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
        } else {
            lblMensaje.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        }
    }
}
