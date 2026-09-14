package ni.edu.uam.facturacion.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ni.edu.uam.facturacion.model.Cargo;

public class CargoController {

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtDescripcion;

    @FXML
    private TableView<Cargo> tblCargos;

    @FXML
    private TableColumn<Cargo, Integer> colId;

    @FXML
    private TableColumn<Cargo, String> colNombre;

    @FXML
    private TableColumn<Cargo, String> colDescripcion;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnCerrar;

    private final ObservableList<Cargo> cargos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        cargos.addAll(
            new Cargo(1, "Administrador", "Acceso total al sistema y configuración general"),
            new Cargo(2, "Cajero", "Gestión de cobros, facturación y caja"),
            new Cargo(3, "Bodeguero", "Recepción y control de inventario de mercancía")
        );
        tblCargos.setItems(cargos);

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
    }

    @FXML
    private void guardar() {
        if (txtNombre.getText() == null || txtNombre.getText().isBlank()) {
            mensaje(Alert.AlertType.WARNING, "El nombre del cargo es obligatorio.");
            return;
        }

        int siguienteId = cargos.stream()
            .mapToInt(c -> c.getId() != null ? c.getId() : 0)
            .max()
            .orElse(0) + 1;

        String descripcion = txtDescripcion.getText() != null ? txtDescripcion.getText().trim() : "";

        Cargo nuevo = new Cargo(
            siguienteId,
            txtNombre.getText().trim(),
            descripcion
        );

        cargos.add(nuevo);
        mensaje(Alert.AlertType.INFORMATION, "Cargo agregado correctamente.");
        limpiar();
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
            Stage stage = (Stage) txtNombre.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        }
    }

    private void limpiar() {
        txtNombre.clear();
        txtDescripcion.clear();
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}
