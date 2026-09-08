# Sistema de Facturación JavaFX (UAM)

Proyecto modular en **JavaFX 21** administrado con **Maven**, desarrollado bajo el paquete base `ni.edu.uam.facturacion` para la Universidad Americana (UAM).

---

## 📋 Requisitos del Entorno

- **Java Development Kit (JDK):** OpenJDK / Eclipse Temurin `21.0.12.1` LTS o superior.
- **Apache Maven:** 3.8+ (probado con Maven 3.9.16).
- **Lombok:** 1.18.48 (con Annotation Processing activado en el IDE).
- **IDE Recomendado:** IntelliJ IDEA, Eclipse o VS Code.

---

## 🗂️ Estructura del Proyecto

```text
sistema-facturacion-javafx/
├── pom.xml
├── README.md
├── .gitignore
└── src/main/
    ├── java/
    │   ├── module-info.java
    │   └── ni/edu/uam/facturacion/
    │       ├── application/
    │       │   └── FacturacionApplication.java
    │       ├── controller/
    │       │   ├── MenuPrincipalController.java
    │       │   └── ProductoController.java
    │       ├── model/
    │       │   ├── Categoria.java
    │       │   ├── Producto.java
    │       │   ├── Cargo.java
    │       │   └── Empleado.java
    │       └── util/
    │           └── SceneManager.java
    └── resources/ni/edu/uam/facturacion/
        ├── fxml/
        │   ├── menu-principal.fxml
        │   └── producto-view.fxml
        ├── images/
        │   ├── logo.png
        │   └── productos/
        └── icons/
            ├── agregar.png
            └── cerrar.png
```

---

## 📦 Modelos de Dominio (Lombok)

- **`Categoria`**: Manejo de categorías (`id`, `nombre`, `activa`).
- **`Producto`**: Productos con alta precisión monetaria (`id`, `codigo`, `nombre`, `categoria`, `precioVenta: BigDecimal`, `existencia: int`, `rutaImagen`, `activo`).
- **`Cargo`**: Cargos de la empresa (`id`, `nombre`, `descripcion`).
- **`Empleado`**: Registro de empleados (`id`, `nombres`, `apellidos`, `cargo`, `fechaContratacion: LocalDate`, `activo`).

---

## 🚀 Compilación y Ejecución

### 1. Compilar el proyecto
```bash
mvn clean compile
```

### 2. Ejecutar la aplicación
```bash
mvn javafx:run
```
