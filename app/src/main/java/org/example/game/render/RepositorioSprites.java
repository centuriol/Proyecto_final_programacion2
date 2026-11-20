package org.example.game.render;

import javafx.scene.image.Image;
import org.example.game.cuerpo.TipoCuerpo;

import java.io.InputStream;

/**
 * Contrato compuesto para administración, precarga y consulta del catálogo de sprites (ISP / DIP).
 */
public interface RepositorioSprites extends CatalogoSprites, ConstructorIconosSprite {
    void asociarMapeo(TipoCuerpo tipo, String nombreRecurso);
    Image decodificarPixil(InputStream is);
    void precargar();
    void limpiarCache();
    void setNombreFondoActivo(String nombre);
}
