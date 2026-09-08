package ni.edu.uam.facturacion.application;

import javafx.application.Application;
import javafx.stage.Stage;
import ni.edu.uam.facturacion.util.SceneManager;

public class FacturacionApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        SceneManager.setStage(primaryStage);
        SceneManager.cambiarEscena("menu-principal.fxml", "Sistema de Facturación - UAM");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
