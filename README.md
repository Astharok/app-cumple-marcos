# App Cumple Marcos

Aplicacion de escritorio Java Swing que abre maximizada y muestra un mapa interactivo de Europa con sus capitales.

## Abrir en NetBeans

1. Cierra el selector de proyecto anterior si sigue abierto.
2. En NetBeans, selecciona **File > Open Project**.
3. Elige la carpeta completa `App Cumple Marcos`, que contiene el archivo `pom.xml`.
4. NetBeans la mostrara como proyecto Maven con el nombre **App Cumple Marcos**.
5. Pulsa **Run Project** o `F6`.

Requiere JDK 17 o superior. El proyecto usa Maven y no necesita dependencias externas.

## Uso

- Haz clic en una capital para abrir su galeria.
- Coloca imagenes JPG, JPEG, PNG, GIF o BMP dentro de `imagenes-capitales/<capital>/`.
- Activa **Modo unir capitales** y pulsa dos capitales para dibujar una linea entre ellas.
- Puedes repetir el proceso para crear todas las uniones necesarias.
- Selecciona una union de la lista y pulsa **Eliminar union seleccionada** para borrarla.

La aplicacion crea automaticamente las carpetas que falten al iniciarse. El repositorio ya incluye una carpeta para cada capital.

El JAR generado por NetBeans queda en `target/app-cumple-marcos-1.0-SNAPSHOT.jar`.
Tambien se incluye una compilacion directa en `dist/AppCumpleMarcos.jar`.
