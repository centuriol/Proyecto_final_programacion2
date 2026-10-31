package org.example.game.render;

import javafx.scene.paint.Color;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(JavaFXExtension.class)
class RegistroEstrategiasVisualesTest {

    @Test
    @DisplayName("Estrategias visuales retornan valores consistentes por tipo")
    void testEstrategiasPorDefecto() {
        EstrategiaVisualTipo estEstrella = RegistroEstrategiasVisuales.get(TipoCuerpo.ESTRELLA);
        assertNotNull(estEstrella);
        assertFalse(estEstrella.tieneRotacionAnimada());
        assertTrue(estEstrella.tieneAreaAtraccion());
        assertTrue(estEstrella.tieneLineasReferenciaOrbital());
        assertTrue(estEstrella.calcularTamanoGhost(15.0) > 30.0);

        EstrategiaVisualTipo estPlaneta = RegistroEstrategiasVisuales.get(TipoCuerpo.PLANETA_ROCOSO);
        assertNotNull(estPlaneta);
        assertTrue(estPlaneta.tieneRotacionAnimada());
        assertTrue(estPlaneta.tieneAreaAtraccion());
        assertFalse(estPlaneta.tieneLineasReferenciaOrbital());

        EstrategiaVisualTipo estSatelite = RegistroEstrategiasVisuales.get(TipoCuerpo.SATELITE);
        assertNotNull(estSatelite);
        assertFalse(estSatelite.tieneAreaAtraccion());
    }

    @Test
    @DisplayName("Registrar nueva estrategia permite extender sin modificar vistas")
    void testRegistrarNuevaEstrategia() {
        EstrategiaVisualTipo custom = new EstrategiaVisualTipo() {
            @Override public double calcularTamanoGhost(double radio) { return 42.0; }
            @Override public double calcularRadioAtraccionPreview(double factorMasa) { return 99.0; }
            @Override public boolean tieneRotacionAnimada() { return false; }
            @Override public boolean tieneAreaAtraccion() { return true; }
            @Override public Color getColorAreaAtraccion() { return Color.MAGENTA; }
            @Override public boolean tieneLineasReferenciaOrbital() { return false; }
            @Override public void dibujarSuperficie(javafx.scene.canvas.GraphicsContext gc, CuerpoCeleste c, double r) {}
        };

        RegistroEstrategiasVisuales.registrar(TipoCuerpo.PLANETA_HELADO, custom);
        assertSame(custom, RegistroEstrategiasVisuales.get(TipoCuerpo.PLANETA_HELADO));
    }
}
