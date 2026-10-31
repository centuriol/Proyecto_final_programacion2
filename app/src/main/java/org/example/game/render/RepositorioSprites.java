package org.example.game.render;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.example.game.cuerpo.TipoCuerpo;

import java.io.InputStream;

/**
 * Contrato para el acceso desacoplado al catálogo y sprites de la simulación (DIP / ISP).
 */
public interface RepositorioSprites {

    Image obtenerImagen(TipoCuerpo tipo);

    Image obtenerImagenPorNombre(String nombre);

    Image obtenerImagenFondo();

    boolean tieneSprite(TipoCuerpo tipo);

    void asociarMapeo(TipoCuerpo tipo, String nombreRecurso);

    ImageView crearVistaIcono(TipoCuerpo tipo, double ancho, double alto);

    StackPane crearContenedorSprite(TipoCuerpo tipo, double ancho, double alto);

    Image decodificarPixil(InputStream is);

    void precargar();

    void limpiarCache();

    void setNombreFondoActivo(String nombre);
}
