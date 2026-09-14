package ni.edu.uam.facturacion.controller;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import ni.edu.uam.facturacion.model.Cargo;
import ni.edu.uam.facturacion.model.Categoria;
import ni.edu.uam.facturacion.model.Producto;

import java.io.IOException;

public class MenuPrincipalController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private ScrollPane vistaInicio;

    @FXML
    private Label lblEstado;

    @FXML
    private MenuItem mnuInicio;

    @FXML
    private MenuItem mnuProductos;

    @FXML
    private MenuItem mnuCategorias;

    @FXML
    private MenuItem mnuCargos;

    @FXML
    private MenuItem mnuSalir;

    @FXML
    private Button btnHome;

    @FXML
    private Button btnProductos;

    @FXML
    private Button btnCategorias;

    @FXML
    private Button btnCargos;

    @FXML
    private Label lblTotalProductos;

    @FXML
    private Label lblDetalleProductos;

    @FXML
    private Label lblTotalCategorias;

    @FXML
    private Label lblDetalleCategorias;

    @FXML
    private Label lblTotalCargos;

    @FXML
    private Label lblDetalleCargos;

    @FXML
    private Label lblValorInventario;

    @FXML
    private Label lblTotalUnidades;

    @FXML
    private Label lblAlertaStock;

    @FXML
    private Label lblDetalleAlerta;

    private Node vistaProductos;
    private Node vistaCategorias;
    private Node vistaCargos;

    private ProductoController productoController;
    private CategoriaController categoriaController;
    private CargoController cargoController;

    private static final String ESTILO_NORMAL = "-fx-background-color: #f1f5f9; -fx-cursor: hand; -fx-background-radius: 6px; -fx-padding: 6px 8px;";
    private static final String ESTILO_ACTIVO = "-fx-background-color: #dbeafe; -fx-border-color: #2563eb; -fx-border-width: 1.5px; -fx-background-radius: 6px; -fx-border-radius: 6px; -fx-padding: 4.5px 6.5px; -fx-cursor: hand;";

    @FXML
    private void initialize() {
        cargarVistas();
        actualizarDashboard();
        actualizarEstiloBotones(btnHome);
    }

    private void cargarVistas() {
        try {
            if (vistaCategorias == null) {
                FXMLLoader loaderCat = new FXMLLoader(getClass().getResource("/ni/edu/uam/facturacion/fxml/categoria-view.fxml"));
                vistaCategorias = loaderCat.load();
                categoriaController = loaderCat.getController();
                categoriaController.setAlCerrar(this::mostrarInicio);
                categoriaController.getCategorias().addListener((ListChangeListener<Categoria>) c -> actualizarDashboard());
            }

            if (vistaProductos == null) {
                FXMLLoader loaderProd = new FXMLLoader(getClass().getResource("/ni/edu/uam/facturacion/fxml/producto-view.fxml"));
                vistaProductos = loaderProd.load();
                productoController = loaderProd.getController();
                productoController.setAlCerrar(this::mostrarInicio);
                if (categoriaController != null) {
                    productoController.getCmbCategoria().setItems(categoriaController.getCategorias());
                }
                productoController.getProductos().addListener((ListChangeListener<Producto>) c -> actualizarDashboard());
            }

            if (vistaCargos == null) {
                FXMLLoader loaderCar = new FXMLLoader(getClass().getResource("/ni/edu/uam/facturacion/fxml/cargo-view.fxml"));
                vistaCargos = loaderCar.load();
                cargoController = loaderCar.getController();
                cargoController.setAlCerrar(this::mostrarInicio);
                cargoController.getCargos().addListener((ListChangeListener<Cargo>) c -> actualizarDashboard());
            }
        } catch (IOException e) {
            System.err.println("Error al cargar vistas: " + e.getMessage());
        }
    }

    private void actualizarDashboard() {
        if (productoController != null) {
            var lista = productoController.getProductos();
            int total = lista.size();
            int totalStock = lista.stream()
                .mapToInt(Producto::getExistencia).sum();

            java.math.BigDecimal valorTotal = lista.stream()
                .filter(p -> p.getPrecioVenta() != null)
                .map(p -> p.getPrecioVenta().multiply(java.math.BigDecimal.valueOf(p.getExistencia())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

            long bajoStock = lista.stream()
                .filter(p -> p.getExistencia() <= 5)
                .count();

            long agotados = lista.stream()
                .filter(p -> p.getExistencia() == 0)
                .count();

            if (lblTotalProductos != null) {
                lblTotalProductos.setText(String.valueOf(total));
            }
            if (lblDetalleProductos != null) {
                lblDetalleProductos.setText(totalStock + " unidades en existencias");
            }
            if (lblValorInventario != null) {
                lblValorInventario.setText(String.format(java.util.Locale.US, "C$ %,.2f", valorTotal));
            }
            if (lblTotalUnidades != null) {
                lblTotalUnidades.setText(totalStock + " unidades");
            }
            if (lblAlertaStock != null && lblDetalleAlerta != null) {
                if (total == 0) {
                    lblAlertaStock.setText("● Sin productos");
                    lblAlertaStock.setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold;");
                    lblDetalleAlerta.setText("Inventario vacío");
                } else if (agotados > 0) {
                    lblAlertaStock.setText("⚠️ " + agotados + " producto(s) agotado(s)");
                    lblAlertaStock.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                    lblDetalleAlerta.setText(bajoStock + " con stock bajo (≤ 5 un.)");
                } else if (bajoStock > 0) {
                    lblAlertaStock.setText("⚠️ " + bajoStock + " producto(s) con bajo stock");
                    lblAlertaStock.setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
                    lblDetalleAlerta.setText("Existencias menores o iguales a 5");
                } else {
                    lblAlertaStock.setText("● Stock óptimo");
                    lblAlertaStock.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                    lblDetalleAlerta.setText("Todos los artículos con buen nivel");
                }
            }
        } else {
            if (lblTotalProductos != null) lblTotalProductos.setText("0");
            if (lblDetalleProductos != null) lblDetalleProductos.setText("0 unidades en existencias");
            if (lblValorInventario != null) lblValorInventario.setText("C$ 0.00");
            if (lblTotalUnidades != null) lblTotalUnidades.setText("0 unidades");
            if (lblAlertaStock != null) {
                lblAlertaStock.setText("● Sin productos");
                lblAlertaStock.setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold;");
            }
            if (lblDetalleAlerta != null) lblDetalleAlerta.setText("Inventario vacío");
        }

        if (categoriaController != null) {
            int totalCat = categoriaController.getCategorias().size();
            if (lblTotalCategorias != null) {
                lblTotalCategorias.setText(String.valueOf(totalCat));
            }
            if (lblDetalleCategorias != null) {
                String nombres = categoriaController.getCategorias().stream()
                    .map(Categoria::getNombre)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Sin categorías registradas");
                lblDetalleCategorias.setText(nombres);
            }
        } else {
            if (lblTotalCategorias != null) lblTotalCategorias.setText("3");
            if (lblDetalleCategorias != null) lblDetalleCategorias.setText("Alimentos, Bebidas, Limpieza");
        }

        if (cargoController != null) {
            int totalCar = cargoController.getCargos().size();
            if (lblTotalCargos != null) {
                lblTotalCargos.setText(String.valueOf(totalCar));
            }
            if (lblDetalleCargos != null) {
                String nombres = cargoController.getCargos().stream()
                    .map(Cargo::getNombre)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Sin cargos registrados");
                lblDetalleCargos.setText(nombres);
            }
        } else {
            if (lblTotalCargos != null) lblTotalCargos.setText("3");
            if (lblDetalleCargos != null) lblDetalleCargos.setText("Roles de administración y operación");
        }
    }

    @FXML
    public void mostrarInicio() {
        cargarVistas();
        actualizarDashboard();
        rootPane.setCenter(vistaInicio);
        actualizarEstiloBotones(btnHome);
        if (lblEstado != null) {
            lblEstado.setText("● Sistema listo y en línea - Dashboard principal");
        }
    }

    @FXML
    private void abrirProductos() {
        cargarVistas();
        rootPane.setCenter(vistaProductos);
        actualizarEstiloBotones(btnProductos);
        if (lblEstado != null) {
            lblEstado.setText("● Módulo activo: Gestión de Productos");
        }
    }

    @FXML
    private void abrirCategorias() {
        cargarVistas();
        rootPane.setCenter(vistaCategorias);
        actualizarEstiloBotones(btnCategorias);
        if (lblEstado != null) {
            lblEstado.setText("● Módulo activo: Gestión de Categorías");
        }
    }

    @FXML
    private void abrirCargos() {
        cargarVistas();
        rootPane.setCenter(vistaCargos);
        actualizarEstiloBotones(btnCargos);
        if (lblEstado != null) {
            lblEstado.setText("● Módulo activo: Gestión de Cargos");
        }
    }

    private void actualizarEstiloBotones(Button botonActivo) {
        if (btnHome != null) {
            btnHome.setStyle(botonActivo == btnHome ? ESTILO_ACTIVO : ESTILO_NORMAL);
        }
        if (btnProductos != null) {
            btnProductos.setStyle(botonActivo == btnProductos ? ESTILO_ACTIVO : ESTILO_NORMAL);
        }
        if (btnCategorias != null) {
            btnCategorias.setStyle(botonActivo == btnCategorias ? ESTILO_ACTIVO : ESTILO_NORMAL);
        }
        if (btnCargos != null) {
            btnCargos.setStyle(botonActivo == btnCargos ? ESTILO_ACTIVO : ESTILO_NORMAL);
        }
    }

    @FXML
    private void salir() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
            "¿Desea cerrar la aplicación?", ButtonType.OK, ButtonType.CANCEL);
        if (a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            Platform.exit();
        }
    }
}
