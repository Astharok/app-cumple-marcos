# App Cumple Marcos

Aplicacion de escritorio Java Swing que abre maximizada y muestra un mapa cartografico preciso, limpio y en blanco y negro de Europa y el Mediterraneo con sus principales capitales, incluida Tunez.

## Abrir en NetBeans

1. Cierra el selector de proyecto anterior si sigue abierto.
2. En NetBeans, selecciona **File > Open Project**.
3. Elige la carpeta completa `App Cumple Marcos`, que contiene el archivo `pom.xml`.
4. NetBeans la mostrara como proyecto Maven con el nombre **App Cumple Marcos**.
5. Pulsa **Run Project** o `F6`.

Requiere JDK 17 o superior. El proyecto usa Maven y no necesita dependencias externas.

## Uso

- Haz clic en una capital para abrir su galeria.
- El mapa no muestra nombres de paises: solo aparecen nombres y marcadores de capitales.
- Coloca imagenes JPG, JPEG, PNG, GIF o BMP dentro de `imagenes-capitales/<capital>/`.
- Activa **Modo unir capitales** y pulsa dos capitales para dibujar una linea entre ellas.
- Puedes repetir el proceso para crear todas las uniones necesarias.
- Selecciona una union de la lista y pulsa **Eliminar union seleccionada** para borrarla.
- El botón **Cómo usar** abre una guía completa dentro de la aplicación.
- **Cancelar selección** permite reiniciar una conexión que todavía no se ha completado.

La aplicacion crea automaticamente las carpetas que falten al iniciarse. El repositorio ya incluye una carpeta para cada capital.

El fondo se genera a partir de geometria publica de Natural Earth mediante `tools/generate_map.py`. El mapa y los marcadores comparten exactamente los limites -25/45 de longitud y 30/72 de latitud.
Natural Earth distribuye estos datos cartograficos como dominio publico.

El JAR generado por NetBeans queda en `target/app-cumple-marcos-1.0-SNAPSHOT.jar`.
Tambien se incluye una compilacion directa en `dist/AppCumpleMarcos.jar`.
