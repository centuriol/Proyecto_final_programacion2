package org.example.game.render;

import javafx.scene.image.Image;
import org.example.game.cuerpo.TipoCuerpo;

/**
 * Capacidad segregada de solo lectura para consultar imágenes y sprites cargados (ISP).
 */
public interface CatalogoSprites {
    Image obtenerImagen(TipoCuerpo tipo);
    Image obtenerImagenPorNombre(String nombre);
    Image obtenerImagenFondo();
    boolean tieneSprite(TipoCuerpo tipo);
}
