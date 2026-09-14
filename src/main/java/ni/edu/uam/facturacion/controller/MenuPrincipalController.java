package ni.edu.uam.facturacion.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MenuPrincipalController {

    @FXML
    private BorderPane rootPane;

    @FXML
    private VBox vistaInicio;

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
    private Button btnProductos;

    @FXML
    private Button btnCategorias;

    @FXML
    private Button btnCargos;

    private Node vistaProductos;
    private Node vistaCategorias;
    private Node vistaCargos;

    private static final String ESTILO_NORMAL = "-fx-background-color: #f1f5f9; -fx-cursor: hand; -fx-background-radius: 6px; -fx-padding: 6px 8px;";
    private static final String ESTILO_ACTIVO = "-fx-background-color: #dbeafe; -fx-border-color: #2563eb; -fx-border-width: 1.5px; -fx-background-radius: 6px; -fx-border-radius: 6px; -fx-padding: 4.5px 6.5px; -fx-cursor: hand;";

    @FXML
    public void mostrarInicio() {
        rootPane.setCenter(vistaInicio);
        actualizarEstiloBotones(null);
        if (lblEstado != null) {
            lblEstado.setText("● Sistema listo y en línea");
        }
    }

    @FXML
    private void abrirProductos() {
        try {
            if (vistaProductos == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ni/edu/uam/facturacion/fxml/producto-view.fxml"));
                vistaProductos = loader.load();
                ProductoController controller = loader.getController();
                controller.setAlCerrar(this::mostrarInicio);
            }
            rootPane.setCenter(vistaProductos);
            actualizarEstiloBotones(btnProductos);
            if (lblEstado != null) {
                lblEstado.setText("● Módulo activo: Gestión de Productos");
            }
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR,
                "No fue posible cargar Productos: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void abrirCategorias() {
        try {
            if (vistaCategorias == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ni/edu/uam/facturacion/fxml/categoria-view.fxml"));
                vistaCategorias = loader.load();
                CategoriaController controller = loader.getController();
                controller.setAlCerrar(this::mostrarInicio);
            }
            rootPane.setCenter(vistaCategorias);
            actualizarEstiloBotones(btnCategorias);
            if (lblEstado != null) {
                lblEstado.setText("● Módulo activo: Gestión de Categorías");
            }
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR,
                "No fue posible cargar Categorías: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void abrirCargos() {
        try {
            if (vistaCargos == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ni/edu/uam/facturacion/fxml/cargo-view.fxml"));
                vistaCargos = loader.load();
                CargoController controller = loader.getController();
                controller.setAlCerrar(this::mostrarInicio);
            }
            rootPane.setCenter(vistaCargos);
            actualizarEstiloBotones(btnCargos);
            if (lblEstado != null) {
                lblEstado.setText("● Módulo activo: Gestión de Cargos");
            }
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR,
                "No fue posible cargar Cargos: " + e.getMessage()).showAndWait();
        }
    }

    private void actualizarEstiloBotones(Button botonActivo) {
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
