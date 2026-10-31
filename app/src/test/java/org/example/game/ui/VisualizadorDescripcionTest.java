package org.example.game.ui;

import org.example.game.cuerpo.TipoCuerpo;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(JavaFXExtension.class)
class VisualizadorDescripcionTest {

    private final VisualizadorDescripcion visualizador = new VentanaDescripcionCuerpo();

    @Test
    @DisplayName("Visualizador retorna nombres y lore delegados")
    void testPropiedadesVisuales() {
        assertEquals("Sol", visualizador.nombrePorDefecto(TipoCuerpo.ESTRELLA));
        assertNotNull(visualizador.icono(TipoCuerpo.PLANETA_ROCOSO));
        assertNotNull(visualizador.subtitulo(TipoCuerpo.LUNA));
        assertNotNull(visualizador.lore(TipoCuerpo.PLANETA_AGUA));
        assertNotNull(visualizador.caracteristicas(TipoCuerpo.PLANETA_LAVA));
    }
}
