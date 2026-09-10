package ni.edu.uam.facturacion.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import ni.edu.uam.facturacion.util.SceneManager;

import java.io.IOException;

public class MenuPrincipalController {

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

    @FXML
    private Button btnSalir;

    @FXML
    private void abrirProductos() {
        try {
            SceneManager.abrirVentana(
                "/ni/edu/uam/facturacion/fxml/producto-view.fxml",
                "Gestión de productos");
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR,
                "No fue posible abrir Productos: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void abrirCategorias() {
        try {
            SceneManager.abrirVentana(
                "/ni/edu/uam/facturacion/fxml/categoria-view.fxml",
                "Gestión de categorías");
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR,
                "No fue posible abrir Categorías: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    private void abrirCargos() {
        try {
            SceneManager.abrirVentana(
                "/ni/edu/uam/facturacion/fxml/cargo-view.fxml",
                "Gestión de cargos");
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR,
                "No fue posible abrir Cargos: " + e.getMessage()).showAndWait();
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
