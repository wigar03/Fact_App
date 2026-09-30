package ni.edu.uam.facturacion.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ni.edu.uam.facturacion.util.DatabaseConnection;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

public class FacturacionApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("✅ Conectado a la base de datos al iniciar la app.");
        } catch (SQLException e) {
            System.err.println("❌ No se pudo conectar a la base de datos: " + e.getMessage());
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
            "/ni/edu/uam/facturacion/fxml/menu-principal.fxml"));
        stage.setTitle("Sistema de facturación");
        stage.setScene(new Scene(loader.load(), 1200, 750));
        stage.setMinWidth(1050);
        stage.setMinHeight(680);
        stage.centerOnScreen();

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
