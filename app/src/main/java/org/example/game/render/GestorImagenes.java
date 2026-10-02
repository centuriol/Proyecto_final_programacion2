package org.example.game.render;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.example.game.cuerpo.TipoCuerpo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Gestor centralizado de imágenes y sprites Pixel Art del juego.
 *
 * Principios SOLID:
 * - SRP: Su única responsabilidad es cargar, decodificar (archivos PNG o JSON .pixil)
 *   y almacenar en memoria los sprites de los ítems y cuerpos celestes.
 * - OCP: Permite registrar dinámicamente nuevas imágenes o mapeos para futuros tipos
 *   de cuerpos (como planeta lava o planeta de agua) sin alterar el resto del código.
 */
public class GestorImagenes {

    private static final Map<String, Image> cacheImagenes = new HashMap<>();

    // Mapeo por defecto de TipoCuerpo a nombre de sprite
    private static final Map<TipoCuerpo, String> MAPEO_CUERPOS = new HashMap<>();

    // Rutas candidatas para búsqueda en el sistema de archivos
    private static final List<String> DIRECTORIOS_BUSQUEDA = List.of(
            "app/src/main/resources/org/example/game/Imagenes/",
            "src/main/resources/org/example/game/Imagenes/",
            "app/src/main/java/org/example/game/Imagenes/",
            "src/main/java/org/example/game/Imagenes/",
            "app/src/main/resources/imagenes/",
            "src/main/resources/imagenes/"
    );

    static {
        // Mapeo inicial de ítems con sprites Pixel Art
        MAPEO_CUERPOS.put(TipoCuerpo.ESTRELLA, "sol");
        MAPEO_CUERPOS.put(TipoCuerpo.PLANETA_ROCOSO, "planeta porro");
        MAPEO_CUERPOS.put(TipoCuerpo.LUNA, "luna");
        MAPEO_CUERPOS.put(TipoCuerpo.PLANETA_AGUA, "planeta_de_agua");
        MAPEO_CUERPOS.put(TipoCuerpo.PLANETA_LAVA, "planeta lava");
    }

    /**
     * Obtiene la imagen asociada a un TipoCuerpo según el mapeo actual.
     *
     * @param tipo Tipo de cuerpo celeste.
     * @return Image de JavaFX o null si no existe imagen registrada o no se pudo cargar.
     */
    public static Image getImagen(TipoCuerpo tipo) {
        if (tipo == null) return null;
        String clave = MAPEO_CUERPOS.get(tipo);
        if (clave == null) return null;
        return getImagenPorNombre(clave);
    }

    /**
     * Verifica si existe una imagen cargable para el TipoCuerpo dado.
     */
    public static boolean tieneImagen(TipoCuerpo tipo) {
        return getImagen(tipo) != null;
    }

    /**
     * Permite asociar o actualizar el nombre de sprite para un TipoCuerpo (OCP).
     */
    public static void registrarMapeo(TipoCuerpo tipo, String nombreRecurso) {
        if (tipo != null && nombreRecurso != null) {
            MAPEO_CUERPOS.put(tipo, nombreRecurso);
        }
    }

    /**
     * Obtiene una imagen por su nombre (ej. "sol", "luna", "planeta porro").
     * Si no se encuentra en caché, intenta cargarla desde PNG o decodificarla desde .pixil.
     */
    public static Image getImagenPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) return null;
        String key = nombre.toLowerCase().trim();

        if (cacheImagenes.containsKey(key)) {
            return cacheImagenes.get(key);
        }

        Image img = cargarImagen(key);
        if (img != null) {
            cacheImagenes.put(key, img);
        }
        return img;
    }

    /**
     * Crea un ImageView configurado para Pixel Art (smooth = false) con el tamaño deseado.
     */
    public static ImageView crearImageView(TipoCuerpo tipo, double ancho, double alto) {
        Image img = getImagen(tipo);
        if (img == null) return null;

        ImageView iv = new ImageView(img);
        iv.setFitWidth(ancho);
        iv.setFitHeight(alto);
        iv.setPreserveRatio(true);
        iv.setSmooth(false); // Pixel-art nítido sin borrosidad bilineal
        return iv;
    }

    /**
     * Crea un nodo decorativo con el sprite del ítem dentro de un recuadro oscuro.
     */
    public static StackPane crearContenedorIcono(TipoCuerpo tipo, double ancho, double alto) {
        ImageView iv = crearImageView(tipo, ancho, alto);
        StackPane contenedor = new StackPane();
        contenedor.setAlignment(Pos.CENTER);
        contenedor.setPrefSize(ancho + 4, alto + 4);
        contenedor.setStyle("-fx-background-color: #1a1c26; -fx-border-color: #000000; -fx-border-width: 1px; -fx-background-radius: 3px; -fx-border-radius: 3px;");
        if (iv != null) {
            contenedor.getChildren().add(iv);
        }
        return contenedor;
    }

    private static String nombreFondoActivo = "fondo_final";

    /**
     * Permite cambiar dinámicamente el nombre de la imagen de fondo espacial activa.
     */
    public static void setNombreFondoActivo(String nombre) {
        if (nombre != null && !nombre.isBlank()) {
            nombreFondoActivo = nombre.trim();
        }
    }

    /**
     * Obtiene la imagen de fondo espacial del juego (prioriza "fondo_final", luego "fondo_prime", "imagen_fonde" o "imagen_fondo").
     */
    public static Image getImagenFondo() {
        Image fondo = getImagenPorNombre(nombreFondoActivo);
        if (fondo == null && !nombreFondoActivo.equalsIgnoreCase("fondo_final")) {
            fondo = getImagenPorNombre("fondo_final");
        }
        if (fondo == null) {
            fondo = getImagenPorNombre("fondo_prime");
        }
        if (fondo == null) {
            fondo = getImagenPorNombre("imagen_fonde");
        }
        if (fondo == null) {
            fondo = getImagenPorNombre("imagen_fondo");
        }
        if (fondo == null) {
            fondo = getImagenPorNombre("fondo");
        }
        return fondo;
    }

    private static Image cargarImagen(String nombre) {
        String baseNombre = nombre;
        List<String> extensiones;

        int dotIdx = nombre.lastIndexOf('.');
        if (dotIdx != -1) {
            String ext = nombre.substring(dotIdx).toLowerCase();
            if (ext.equals(".png") || ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".pixil")) {
                baseNombre = nombre.substring(0, dotIdx);
                extensiones = List.of(ext);
            } else {
                extensiones = List.of(".png", ".jpg", ".jpeg", ".pixil");
            }
        } else {
            extensiones = List.of(".png", ".jpg", ".jpeg", ".pixil");
        }

        for (String ext : extensiones) {
            // 1. Intentar desde el Classpath
            InputStream isClasspath = obtenerStreamClasspath(baseNombre, ext);
            if (isClasspath != null) {
                try {
                    if (ext.equals(".pixil")) {
                        Image img = decodificarPixil(isClasspath);
                        if (img != null) return img;
                    } else {
                        return new Image(isClasspath);
                    }
                } catch (Exception ignored) {}
            }

            // 2. Intentar desde el sistema de archivos
            for (String dir : DIRECTORIOS_BUSQUEDA) {
                File f = new File(dir, baseNombre + ext);
                if (f.exists() && f.isFile()) {
                    try (InputStream fis = new FileInputStream(f)) {
                        if (ext.equals(".pixil")) {
                            Image img = decodificarPixil(fis);
                            if (img != null) return img;
                        } else {
                            return new Image(fis);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }

        return null;
    }

    private static InputStream obtenerStreamClasspath(String nombre, String extension) {
        // Variantes de nombres para classpath
        List<String> variantes = List.of(
                "/org/example/game/Imagenes/" + nombre + extension,
                "/Imagenes/" + nombre + extension,
                "/imagenes/" + nombre + extension,
                "/" + nombre + extension
        );

        for (String ruta : variantes) {
            InputStream is = GestorImagenes.class.getResourceAsStream(ruta);
            if (is != null) return is;
        }
        return null;
    }

    /**
     * Decodifica una imagen almacenada dentro de la estructura JSON de un archivo .pixil de Pixilart.
     * El archivo contiene una cadena Base64 con el payload del PNG después del prefijo "base64,".
     */
    public static Image decodificarPixil(InputStream is) {
        try {
            String contenido = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            int idx = contenido.indexOf("base64,");
            if (idx == -1) return null;

            int start = idx + "base64,".length();
            int end = contenido.indexOf('"', start);
            if (end == -1) return null;

            String base64Data = contenido.substring(start, end).trim();
            byte[] rawPng = Base64.getDecoder().decode(base64Data);

            return new Image(new ByteArrayInputStream(rawPng));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Limpia la caché en memoria (útil para pruebas unitarias).
     */
    public static void limpiarCache() {
        cacheImagenes.clear();
    }
}
