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
 * - SRP: Cargar, decodificar y almacenar sprites en memoria.
 * - DIP: Implementa RepositorioSprites para proveer inyección de dependencias como instancia.
 * - OCP: Registro extensible de nuevos mapeos sprite/tipo.
 */
public class GestorImagenes implements RepositorioSprites {

    private static final GestorImagenes INSTANCIA = new GestorImagenes();

    private final Map<String, Image> cacheImagenes = new HashMap<>();
    private final Set<String> recursosFallidos = new HashSet<>();
    private final Map<TipoCuerpo, String> mapeoCuerpos = new EnumMap<>(TipoCuerpo.class);
    private String nombreFondoActivo = "fondo_final";

    private static final List<String> DIRECTORIOS_BUSQUEDA = List.of(
            "app/src/main/resources/org/example/game/Imagenes/",
            "src/main/resources/org/example/game/Imagenes/",
            "app/src/main/java/org/example/game/Imagenes/",
            "src/main/java/org/example/game/Imagenes/",
            "app/src/main/resources/imagenes/",
            "src/main/resources/imagenes/"
    );

    public GestorImagenes() {
        mapeoCuerpos.put(TipoCuerpo.ESTRELLA, "sol");
        mapeoCuerpos.put(TipoCuerpo.PLANETA_ROCOSO, "planeta porro");
        mapeoCuerpos.put(TipoCuerpo.LUNA, "luna");
        mapeoCuerpos.put(TipoCuerpo.PLANETA_AGUA, "planeta_de_agua");
        mapeoCuerpos.put(TipoCuerpo.PLANETA_LAVA, "planeta lava");
    }

    public static GestorImagenes getInstancia() {
        return INSTANCIA;
    }

    public void precargar() {
        for (String nombre : mapeoCuerpos.values()) {
            obtenerImagenPorNombre(nombre);
        }
        obtenerImagenFondo();
    }

    @Override
    public Image obtenerImagen(TipoCuerpo tipo) {
        if (tipo == null) return null;
        String clave = mapeoCuerpos.get(tipo);
        if (clave == null) return null;
        return obtenerImagenPorNombre(clave);
    }

    @Override
    public boolean tieneSprite(TipoCuerpo tipo) {
        return obtenerImagen(tipo) != null;
    }

    @Override
    public void asociarMapeo(TipoCuerpo tipo, String nombreRecurso) {
        if (tipo != null && nombreRecurso != null) {
            mapeoCuerpos.put(tipo, nombreRecurso);
        }
    }

    @Override
    public Image obtenerImagenPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) return null;
        String key = nombre.toLowerCase().trim();

        if (cacheImagenes.containsKey(key)) {
            return cacheImagenes.get(key);
        }
        if (recursosFallidos.contains(key)) {
            return null;
        }

        Image img = cargarImagen(nombre);
        if (img != null && !img.isError()) {
            cacheImagenes.put(key, img);
        } else {
            recursosFallidos.add(key);
        }
        return img;
    }

    @Override
    public ImageView crearVistaIcono(TipoCuerpo tipo, double ancho, double alto) {
        Image img = obtenerImagen(tipo);
        if (img == null) return null;

        ImageView iv = new ImageView(img);
        iv.setFitWidth(ancho);
        iv.setFitHeight(alto);
        iv.setPreserveRatio(true);
        iv.setSmooth(false);
        return iv;
    }

    @Override
    public StackPane crearContenedorSprite(TipoCuerpo tipo, double ancho, double alto) {
        ImageView iv = crearVistaIcono(tipo, ancho, alto);
        StackPane contenedor = new StackPane();
        contenedor.setAlignment(Pos.CENTER);
        contenedor.setPrefSize(ancho + 4, alto + 4);
        contenedor.setStyle("-fx-background-color: #1a1c26; -fx-border-color: #000000; -fx-border-width: 1px; -fx-background-radius: 3px; -fx-border-radius: 3px;");
        if (iv != null) {
            contenedor.getChildren().add(iv);
        } else if (tipo != null) {
            javafx.scene.control.Label lbl = new javafx.scene.control.Label(tipo.getIcono());
            lbl.setFont(javafx.scene.text.Font.font("Monospace", javafx.scene.text.FontWeight.BOLD, 10));
            lbl.setTextFill(javafx.scene.paint.Color.web(tipo.getColorHexString()));
            contenedor.getChildren().add(lbl);
        }
        return contenedor;
    }

    public void setNombreFondoActivoInstancia(String nombre) {
        if (nombre != null && !nombre.isBlank()) {
            this.nombreFondoActivo = nombre.trim();
        }
    }

    @Override
    public Image obtenerImagenFondo() {
        Image fondo = (nombreFondoActivo != null) ? obtenerImagenPorNombre(nombreFondoActivo) : null;
        if (fondo == null && (nombreFondoActivo == null || !nombreFondoActivo.equalsIgnoreCase("fondo_final"))) {
            fondo = obtenerImagenPorNombre("fondo_final");
        }
        if (fondo == null) {
            fondo = obtenerImagenPorNombre("fondo_prime");
        }
        if (fondo == null) {
            fondo = obtenerImagenPorNombre("imagen_fonde");
        }
        if (fondo == null) {
            fondo = obtenerImagenPorNombre("imagen_fondo");
        }
        if (fondo == null) {
            fondo = obtenerImagenPorNombre("fondo");
        }
        return fondo;
    }

    public void limpiarCacheInstancia() {
        cacheImagenes.clear();
        recursosFallidos.clear();
    }

    // ===== Métodos Estáticos para Compatibilidad (Delegación a INSTANCIA) =====

    public static void precargarImagenes() { INSTANCIA.precargar(); }
    public static Image getImagen(TipoCuerpo tipo) { return INSTANCIA.obtenerImagen(tipo); }
    public static boolean tieneImagen(TipoCuerpo tipo) { return INSTANCIA.tieneSprite(tipo); }
    public static void registrarMapeo(TipoCuerpo tipo, String nombreRecurso) { INSTANCIA.asociarMapeo(tipo, nombreRecurso); }
    public static Image getImagenPorNombre(String nombre) { return INSTANCIA.obtenerImagenPorNombre(nombre); }
    public static ImageView crearImageView(TipoCuerpo tipo, double ancho, double alto) { return INSTANCIA.crearVistaIcono(tipo, ancho, alto); }
    public static StackPane crearContenedorIcono(TipoCuerpo tipo, double ancho, double alto) { return INSTANCIA.crearContenedorSprite(tipo, ancho, alto); }
    public static void setNombreFondoActivo(String nombre) { INSTANCIA.setNombreFondoActivoInstancia(nombre); }
    public static Image getImagenFondo() { return INSTANCIA.obtenerImagenFondo(); }
    public static void limpiarCache() { INSTANCIA.limpiarCacheInstancia(); }

    private Image cargarImagen(String nombre) {
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

    private InputStream obtenerStreamClasspath(String nombre, String extension) {
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
}
