# Sistema de Facturación e Inventario - JavaFX

**Universidad Americana (UAM)**  
**Facultad de Ingeniería y Arquitectura**  
**Asignatura:** Programación de Sistemas Empresariales / Desarrollo de Aplicaciones  
**Sesión 1 y 2:** Estructura profesional, modelos, vistas FXML, controladores y navegación

---

## 📋 Descripción del Proyecto

El **Sistema de Facturación** es una aplicación de escritorio modular desarrollada en **Java 21** con **JavaFX 21** y **Maven**, orientada a la administración de productos, inventario, categorías, empleados y facturación comercial. 

La solución implementa una arquitectura profesional MVC (Modelo-Vista-Controlador), soporte modular mediante `module-info.java`, reducción de boilerplate con **Lombok**, interfaz limpia y moderna tipo *dashboard* empresarial en JavaFX, navegación modal con `SceneManager` y tablas reactivas en memoria con `TableView`.

---

## 🏗️ Estructura del Proyecto

```
Fact_App/
├── pom.xml
├── README.md
├── .gitignore
└── src/
    └── main/
        ├── java/
        │   ├── module-info.java
        │   └── ni/edu/uam/facturacion/
        │       ├── application/
        │       │   └── FacturacionApplication.java
        │       ├── controller/
        │       │   ├── CargoController.java
        │       │   ├── CategoriaController.java
        │       │   ├── MenuPrincipalController.java
        │       │   └── ProductoController.java
        │       ├── model/
        │       │   ├── Cargo.java
        │       │   ├── Categoria.java
        │       │   ├── Empleado.java
        │       │   └── Producto.java
        │       └── util/
        │           └── SceneManager.java
        └── resources/
            └── ni/edu/uam/facturacion/
                ├── fxml/
                │   ├── cargo-view.fxml
                │   ├── categoria-view.fxml
                │   ├── menu-principal.fxml
                │   └── producto-view.fxml
                ├── icons/
                │   ├── agregar.png
                │   └── cerrar.png
                └── images/
                    ├── logo.png
                    └── productos/
                        └── .gitkeep
```

---

## 📦 Modelos de Dominio (Project Lombok)

Todos los modelos de datos utilizan anotaciones de **Lombok** (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`):

1. **`Producto`**: Representa los productos del inventario comercial.
   - Atributos: `id`, `codigo`, `nombre`, `categoria`, `precioVenta` (`BigDecimal`), `existencia` (`int`), `rutaImagen`, `activo`.
2. **`Categoria`**: Clasificación del catálogo con sobreescritura amigable de `toString()`.
   - Atributos: `id`, `nombre`, `activa`.
3. **`Cargo`**: Posiciones y roles del personal en la empresa.
   - Atributos: `id`, `nombre`, `descripcion`.
4. **`Empleado`**: Registro del personal administrativo y operativo.
   - Atributos: `id`, `nombres`, `apellidos`, `cargo`, `fechaContratacion` (`LocalDate`), `activo`.

---

## 🖥️ Módulos y Vistas Implementadas

### 1. Panel Principal (`menu-principal.fxml` & `MenuPrincipalController`)
- **Diseño Moderno & Clean**: Estilo *dashboard* empresarial que reemplaza vistas saturadas por una interfaz ligera y ordenada.
  - **MenuBar**: Menú "Catálogos" con accesos a Productos, Categorías, Cargos y Salir.
  - **ToolBar**: Barra de herramientas superior con botones de acceso directo.
  - **Dashboard Cards**: Tres tarjetas modulares interactivas (Productos, Categorías, Cargos) con insignias temáticas (*Inventario*, *Clasificación*, *Personal*), descripciones de cada módulo y botones de acción rápida.
  - **Barra de Estado**: Indicador inferior de sistema operativo, versión y estado en línea.
- **Controlador**: Manejo de eventos `#abrirProductos`, `#abrirCategorias`, `#abrirCargos` y `#salir` con confirmación modal.

### 2. Gestión de Productos (`producto-view.fxml` & `ProductoController`)
- **Formulario lateral (`GridPane`)**: Código único, nombre, categoría (`ComboBox<Categoria>`), precio de venta, existencia, estado activo y previsualización de imagen seleccionada con `FileChooser`.
- **Listado interactivo (`TableView<Producto>`)**: 6 columnas enlazadas mediante `PropertyValueFactory` (`colCodigo`, `colNombre`, `colCategoria`, `colPrecio`, `colExistencia`, `colActivo`).
- **Validaciones**: Comprobación de campos obligatorios, control de formato numérico y validación de reglas de negocio (`precio > 0` y `existencia >= 0`).

### 3. Gestión de Categorías (`categoria-view.fxml` & `CategoriaController`)
- **Formulario lateral**: Nombre de categoría y casilla de estado activa.
- **Listado (`TableView<Categoria>`)**: Columnas ID, Nombre y Estado Activo, inicializado con categorías base (*Alimentos*, *Bebidas*, *Limpieza*) y adición dinámica.

### 4. Gestión de Cargos (`cargo-view.fxml` & `CargoController`)
- **Formulario lateral**: Nombre del cargo y descripción de responsabilidades.
- **Listado (`TableView<Cargo>`)**: Columnas ID, Nombre del Cargo y Descripción, inicializado con roles operativos (*Administrador*, *Cajero*, *Bodeguero*).

### 5. Gestor de Navegación (`SceneManager`)
- Método `abrirVentana(String recurso, String titulo)` que carga ventanas secundarias con `Modality.APPLICATION_MODAL`, asigna el icono institucional y ejecuta `showAndWait()`.

---

## 🧪 Matriz de Pruebas

| # | Prueba | Acción | Resultado Esperado | Estado |
|---|--------|--------|--------------------|--------|
| 1 | **Inicio limpio del proyecto** | Ejecutar con `mvn javafx:run` | Inicia el menú principal sin rastros de clases de ejemplo. | ✅ Superado |
| 2 | **Navegación a Productos** | Clic en Productos desde MenuBar, ToolBar o Card | Abre la ventana modal de gestión de productos. | ✅ Superado |
| 3 | **Navegación a Categorías** | Clic en Categorías desde MenuBar, ToolBar o Card | Abre la ventana modal de gestión de categorías con su tabla. | ✅ Superado |
| 4 | **Navegación a Cargos** | Clic en Cargos desde MenuBar, ToolBar o Card | Abre la ventana modal de gestión de cargos con su tabla. | ✅ Superado |
| 5 | **Validación de campos vacíos** | Clic en Guardar con campos requeridos vacíos | Muestra `Alert` de advertencia indicando completar campos. | ✅ Superado |
| 6 | **Validación de valores numéricos** | Letras en precio o existencia en productos | Muestra `Alert` de error por formato inválido. | ✅ Superado |
| 7 | **Reglas de negocio numéricas** | Precio `<= 0` o Existencia `< 0` | Registro denegado con mensaje explicativo. | ✅ Superado |
| 8 | **Previsualización de imagen** | Selección de archivo mediante `FileChooser` | Imagen mostrada correctamente en el `ImageView`. | ✅ Superado |
| 9 | **Registro temporal en TableView** | Guardar datos válidos en cualquiera de los formularios | El nuevo ítem aparece en el `TableView` de inmediato. | ✅ Superado |
| 10 | **Compilación limpia** | Ejecución de `mvn clean compile` | Compilación exitosa de los 11 archivos Java sin errores. | ✅ Superado |

---

## 🚀 Compilación y Ejecución

```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar la aplicación
mvn javafx:run
```
