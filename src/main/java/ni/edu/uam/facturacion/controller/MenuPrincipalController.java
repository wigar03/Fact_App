package ni.edu.uam.facturacion.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import ni.edu.uam.facturacion.util.SceneManager;

public class MenuPrincipalController {

    @FXML
    public void initialize() {
    }

    @FXML
    public void onProductosClick(ActionEvent event) {
        SceneManager.cambiarEscena("producto-view.fxml", "Gestión de Productos - UAM");
    }

    @FXML
    public void onSalirClick(ActionEvent event) {
        Platform.exit();
    }
}
