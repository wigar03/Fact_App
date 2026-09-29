package ni.edu.uam.facturacion.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ni.edu.uam.facturacion.DAO.CategoriaDAO;
import ni.edu.uam.facturacion.model.Categoria;

public class CategoriaController {

    @FXML
    private TextField txtNombre;

    @FXML
    private CheckBox chkActiva;

    @FXML
    private TableView<Categoria> tblCategorias;

    @FXML
    private TableColumn<Categoria, Integer> colId;

    @FXML
    private TableColumn<Categoria, String> colNombre;

    @FXML
    private TableColumn<Categoria, Boolean> colActiva;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnCerrar;

    private final ObservableList<Categoria> categorias = FXCollections.observableArrayList();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();

    @FXML
    private void initialize() {
        tblCategorias.setItems(categorias);
        chkActiva.setSelected(true);

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colActiva.setCellValueFactory(new PropertyValueFactory<>("activa"));

        cargarCategorias();
    }

    public void cargarCategorias() {
        categorias.setAll(categoriaDAO.findAll());
    }

    public ObservableList<Categoria> getCategorias() {
        return categorias;
    }

    @FXML
    private void guardar() {
        if (txtNombre.getText() == null || txtNombre.getText().isBlank()) {
            mensaje(Alert.AlertType.WARNING, "El nombre de la categoría es obligatorio.");
            return;
        }

        Categoria nueva = new Categoria(
            null,
            txtNombre.getText().trim(),
            chkActiva.isSelected()
        );

        if (categoriaDAO.crear(nueva)) {
            cargarCategorias();
            mensaje(Alert.AlertType.INFORMATION, "Categoría agregada correctamente en la base de datos.");
            limpiar();
        } else {
            String err = categoriaDAO.getUltimoError() != null ? categoriaDAO.getUltimoError() : "No se pudo guardar la categoría en la base de datos.";
            mensaje(Alert.AlertType.ERROR, err);
        }
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
        chkActiva.setSelected(true);
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}
