package org.example.game.ui;

import org.example.game.cuerpo.TipoCuerpo;

/**
 * Capacidad segregada para consultar metadatos textuales y descriptores de un tipo de cuerpo (ISP).
 */
public interface ProveedorDescriptoresCuerpo {
    String obtenerNombrePorDefecto(TipoCuerpo tipo);
    String obtenerIcono(TipoCuerpo tipo);
    String obtenerSubtitulo(TipoCuerpo tipo);
    String obtenerLore(TipoCuerpo tipo);
}
