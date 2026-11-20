package org.example.game.render;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.example.game.cuerpo.TipoCuerpo;

import java.io.InputStream;
import java.util.*;

/**
 * Gestor centralizado de caché de sprites Pixel Art que delega carga y construcción visual (SRP / DIP / OCP).
 */
public class GestorImagenes implements RepositorioSprites {

    private static final List<String> FONDOS_FALLBACK = List.of(
            "fondo_final", "fondo_prime", "imagen_fonde", "imagen_fondo", "fondo"
    );

    private static class EstadoCacheSprites {
        final Map<String, Image> cacheImagenes = new HashMap<>();
        final Set<String> recursosFallidos = new HashSet<>();
        final Map<TipoCuerpo, String> mapeoCuerpos = new EnumMap<>(TipoCuerpo.class);
        String nombreFondoActivo = "fondo_final";

        EstadoCacheSprites() {
            mapeoCuerpos.put(TipoCuerpo.ESTRELLA, "sol");
            mapeoCuerpos.put(TipoCuerpo.PLANETA_ROCOSO, "planeta porro");
            mapeoCuerpos.put(TipoCuerpo.LUNA, "luna");
            mapeoCuerpos.put(TipoCuerpo.PLANETA_AGUA, "planeta_de_agua");
            mapeoCuerpos.put(TipoCuerpo.PLANETA_LAVA, "planeta lava");
        }

        void limpiar() {
            cacheImagenes.clear();
            recursosFallidos.clear();
        }
    }

    private final EstadoCacheSprites estado = new EstadoCacheSprites();
    private final CargadorRecursosImagen cargador = new CargadorRecursosImagen();
    private static final ConstructorVistaSprite CONSTRUCTOR_VISTA = new ConstructorVistaSprite();

    public GestorImagenes() {}

    @Override
    public void precargar() {
        for (String nombre : estado.mapeoCuerpos.values()) obtenerImagenPorNombre(nombre);
        obtenerImagenPorNombre(org.example.game.render.capa.CapaEscudoPlanetario.NOMBRE_SPRITE_ESCUDO);
        for (String persona : org.example.game.render.cuerpo.RenderizadorPlaneta.SPRITES_PERSONAS) obtenerImagenPorNombre(persona);
        for (String explosion : org.example.game.efectos.GestorEfectos.SPRITES_EXPLOSION) obtenerImagenPorNombre(explosion);
        obtenerImagenFondo();
    }

    @Override
    public Image obtenerImagen(TipoCuerpo tipo) {
        if (tipo == null) return null;
        String clave = estado.mapeoCuerpos.get(tipo);
        return clave != null ? obtenerImagenPorNombre(clave) : null;
    }

    @Override
    public boolean tieneSprite(TipoCuerpo tipo) {
        return obtenerImagen(tipo) != null;
    }

    @Override
    public void asociarMapeo(TipoCuerpo tipo, String nombreRecurso) {
        if (tipo != null && nombreRecurso != null) {
            estado.mapeoCuerpos.put(tipo, nombreRecurso);
        }
    }

    @Override
    public Image obtenerImagenPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) return null;
        String key = nombre.toLowerCase().trim();
        if (estado.cacheImagenes.containsKey(key)) return estado.cacheImagenes.get(key);
        if (estado.recursosFallidos.contains(key)) return null;

        Image img = cargador.cargarImagen(nombre);
        if (img != null && !img.isError()) {
            estado.cacheImagenes.put(key, img);
        } else {
            estado.recursosFallidos.add(key);
        }
        return img;
    }

    @Override
    public ImageView crearVistaIcono(TipoCuerpo tipo, double ancho, double alto) {
        return CONSTRUCTOR_VISTA.crearVistaIcono(obtenerImagen(tipo), ancho, alto);
    }

    @Override
    public StackPane crearContenedorSprite(TipoCuerpo tipo, double ancho, double alto) {
        return CONSTRUCTOR_VISTA.crearContenedorSprite(obtenerImagen(tipo), tipo, ancho, alto);
    }

    @Override
    public void setNombreFondoActivo(String nombre) {
        if (nombre != null && !nombre.isBlank()) {
            estado.nombreFondoActivo = nombre.trim();
        }
    }

    @Override
    public Image obtenerImagenFondo() {
        if (estado.nombreFondoActivo != null) {
            Image fondo = obtenerImagenPorNombre(estado.nombreFondoActivo);
            if (fondo != null) return fondo;
        }
        for (String nombre : FONDOS_FALLBACK) {
            Image fondo = obtenerImagenPorNombre(nombre);
            if (fondo != null) return fondo;
        }
        return null;
    }

    @Override
    public void limpiarCache() {
        estado.limpiar();
    }

    @Override
    public Image decodificarPixil(InputStream is) {
        return cargador.decodificarPixil(is);
    }
}
