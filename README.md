# Sistema de Facturación JavaFX (UAM)

Proyecto modular en **JavaFX 21** administrado con **Maven**, desarrollado bajo el paquete base `ni.edu.uam.facturacion` para la Universidad Americana (UAM).

---

## 📋 Requisitos del Entorno

- **Java Development Kit (JDK):** OpenJDK / Eclipse Temurin `21.0.12.1` LTS o superior.
- **Apache Maven:** 3.8+ (probado con Maven 3.9.16).
- **IDE Recomendado:** IntelliJ IDEA, Eclipse o VS Code con extensión JavaFX.

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

> **Nota sobre los recursos:** Los archivos `.fxml`, imágenes e iconos se encuentran organizados dentro de subdirectorios en `src/main/resources/ni/edu/uam/facturacion/`. Maven los empaqueta automáticamente en el classpath de la aplicación para su carga dinámica.

---

## 🚀 Compilación y Ejecución

### 1. Compilar el proyecto
Para limpiar el directorio de salida y compilar las clases del módulo:
```bash
mvn clean compile
```

### 2. Ejecutar la aplicación
Para iniciar la interfaz gráfica con el plugin de JavaFX:
```bash
mvn javafx:run
```

---

## 📦 Módulos y Paquetes

- **`application`**: Contiene la clase de arranque `FacturacionApplication` que extiende `javafx.application.Application`.
- **`controller`**: Controladores de eventos e interfaz para las vistas FXML (`MenuPrincipalController`, `ProductoController`).
- **`model`**: Clases de dominio del sistema: `Categoria`, `Producto`, `Cargo` y `Empleado`.
- **`util`**: Utilidades generales como `SceneManager` para la navegación centralizada entre vistas.
