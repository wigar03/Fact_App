package ni.edu.uam.facturacion.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class SceneManager {
    private static Stage primaryStage;

    public static void setStage(Stage stage) {
        primaryStage = stage;
        try {
            InputStream iconStream = SceneManager.class.getResourceAsStream("/ni/edu/uam/facturacion/images/logo.png");
            if (iconStream != null) {
                primaryStage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception ignored) {
        }
    }

    public static Stage getStage() {
        return primaryStage;
    }

    public static void cambiarEscena(String fxmlFile, String titulo) {
        try {
            String path = fxmlFile.startsWith("/") ? fxmlFile : "/ni/edu/uam/facturacion/fxml/" + fxmlFile;
            URL url = SceneManager.class.getResource(path);
            if (url == null) {
                throw new IOException("No se encontró el archivo FXML en: " + path);
            }
            Parent root = FXMLLoader.load(url);
            Scene scene = new Scene(root);
            primaryStage.setTitle(titulo);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error al cargar la escena: " + fxmlFile);
            e.printStackTrace();
        }
    }
}
