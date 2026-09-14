package ni.edu.uam.facturacion.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;

public class FacturacionApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
            "/ni/edu/uam/facturacion/fxml/menu-principal.fxml"));
        stage.setTitle("Sistema de facturación");
        stage.setScene(new Scene(loader.load(), 980, 640));
        stage.setMinWidth(950);
        stage.setMinHeight(620);

        // Configurar icono de la aplicación si está disponible
        InputStream logoStream = getClass().getResourceAsStream("/ni/edu/uam/facturacion/images/logo.png");
        if (logoStream != null) {
            stage.getIcons().add(new Image(logoStream));
        }

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
