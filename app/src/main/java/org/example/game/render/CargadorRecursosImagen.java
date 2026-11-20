package org.example.game.render;

import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * SRP: Resuelve rutas de recursos (classpath y disco) y decodifica imágenes estándar y formato .pixil.
 */
public class CargadorRecursosImagen {

    private static final List<String> EXTENSIONES_SOPORTADAS = List.of(".png", ".jpg", ".jpeg", ".pixil");
    private static final List<String> DIRECTORIOS_BUSQUEDA = List.of(
            "app/src/main/resources/org/example/game/Imagenes/",
            "src/main/resources/org/example/game/Imagenes/",
            "app/src/main/java/org/example/game/Imagenes/",
            "src/main/java/org/example/game/Imagenes/",
            "app/src/main/resources/imagenes/",
            "src/main/resources/imagenes/"
    );

    public Image cargarImagen(String nombre) {
        String baseNombre = extraerNombreBase(nombre);
        List<String> extensiones = resolverExtensiones(nombre);
        for (String ext : extensiones) {
            Image desdeClasspath = intentarDesdeClasspath(baseNombre, ext);
            if (desdeClasspath != null) return desdeClasspath;
            Image desdeDisco = intentarDesdeDisco(baseNombre, ext);
            if (desdeDisco != null) return desdeDisco;
        }
        return null;
    }

    private String extraerNombreBase(String nombre) {
        int dotIdx = nombre.lastIndexOf('.');
        if (dotIdx == -1) return nombre;
        String ext = nombre.substring(dotIdx).toLowerCase();
        return EXTENSIONES_SOPORTADAS.contains(ext) ? nombre.substring(0, dotIdx) : nombre;
    }

    private List<String> resolverExtensiones(String nombre) {
        int dotIdx = nombre.lastIndexOf('.');
        if (dotIdx != -1) {
            String ext = nombre.substring(dotIdx).toLowerCase();
            if (EXTENSIONES_SOPORTADAS.contains(ext)) {
                return List.of(ext);
            }
        }
        return EXTENSIONES_SOPORTADAS;
    }

    private Image intentarDesdeClasspath(String baseNombre, String ext) {
        InputStream is = obtenerStreamClasspath(baseNombre, ext);
        if (is == null) return null;
        try {
            return leerImagenDesdeStream(is, ext);
        } catch (Exception ex) {
            System.err.println("[WARN] No se pudo decodificar imagen desde classpath (" + baseNombre + ext + "): " + ex.getMessage());
            return null;
        }
    }

    private Image intentarDesdeDisco(String baseNombre, String ext) {
        for (String dir : DIRECTORIOS_BUSQUEDA) {
            File f = new File(dir, baseNombre + ext);
            if (!f.exists() || !f.isFile()) continue;
            try (InputStream fis = new FileInputStream(f)) {
                Image img = leerImagenDesdeStream(fis, ext);
                if (img != null) return img;
            } catch (Exception ex) {
                System.err.println("[WARN] No se pudo cargar imagen desde archivo de disco (" + f.getAbsolutePath() + "): " + ex.getMessage());
            }
        }
        return null;
    }

    private Image leerImagenDesdeStream(InputStream is, String ext) {
        return ".pixil".equals(ext) ? decodificarPixil(is) : new Image(is);
    }

    private InputStream obtenerStreamClasspath(String nombre, String extension) {
        List<String> variantes = List.of(
                "/org/example/game/Imagenes/" + nombre + extension,
                "/Imagenes/" + nombre + extension,
                "/imagenes/" + nombre + extension,
                "/" + nombre + extension
        );
        for (String ruta : variantes) {
            InputStream is = CargadorRecursosImagen.class.getResourceAsStream(ruta);
            if (is != null) return is;
        }
        return null;
    }

    public Image decodificarPixil(InputStream is) {
        try {
            String contenido = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            int idx = contenido.indexOf("base64,");
            if (idx == -1) return null;
            int start = idx + "base64,".length();
            int end = contenido.indexOf('"', start);
            if (end == -1) return null;
            byte[] rawPng = Base64.getDecoder().decode(contenido.substring(start, end).trim());
            return new Image(new ByteArrayInputStream(rawPng));
        } catch (Exception e) {
            return null;
        }
    }
}
