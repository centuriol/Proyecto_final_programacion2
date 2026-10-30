package org.example;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.BarraInventarioHotbar;
import org.example.game.ui.VentanaDescripcionCuerpo;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(JavaFXExtension.class)
class HotbarYVentanaTest {

    @Test
    void testVentanaDescripcionCuerpoLore() {
        String loreSol = VentanaDescripcionCuerpo.obtenerLore(TipoCuerpo.ESTRELLA);
        assertNotNull(loreSol);
        assertFalse(loreSol.isBlank());

        String lorePlaneta = VentanaDescripcionCuerpo.obtenerLore(TipoCuerpo.PLANETA_ROCOSO);
        assertNotNull(lorePlaneta);
        assertFalse(lorePlaneta.isBlank());

        String loreLuna = VentanaDescripcionCuerpo.obtenerLore(TipoCuerpo.LUNA);
        assertNotNull(loreLuna);
        assertFalse(loreLuna.isBlank());
    }

    @Test
    void testFormatoHotbarResumen() {
        String resumenEstrella = BarraInventarioHotbar.formatearResumenCosto(TipoCuerpo.ESTRELLA);
        assertTrue(resumenEstrella.contains("Requiere:"));
        String prodEstrella = BarraInventarioHotbar.formatearResumenProduccion(TipoCuerpo.ESTRELLA);
        assertTrue(prodEstrella.contains("Da/t:"));

        String tooltip = BarraInventarioHotbar.formatearTooltipDetallado(TipoCuerpo.PLANETA_ROCOSO, 2);
        assertTrue(tooltip.contains("MATERIALES NECESARIOS PARA PONERLO"));
        assertTrue(tooltip.contains("MATERIALES QUE TE DA POR TICK"));
        assertFalse(tooltip.contains("Arrastrar para lanzar"));
    }

    @Test
    void testHotbarSinMasaYSinTooltipsHover() {
        SimulacionSolar sim = new SimulacionSolar(1000, 1000);
        BarraInventarioHotbar hotbar = new BarraInventarioHotbar(sim);

        assertEquals(BarraInventarioHotbar.CUERPOS_HOTBAR.length, hotbar.getChildren().size());

        for (Node node : hotbar.getChildren()) {
            assertNull(node.getProperties().get("javafx.scene.control.Tooltip"));
        }
    }

    @Test
    void testHotbarSlotsSimplificadosSolamenteImagenYNombre() {
        SimulacionSolar sim = new SimulacionSolar(1000, 1000);
        BarraInventarioHotbar hotbar = new BarraInventarioHotbar(sim);

        assertEquals(BarraInventarioHotbar.CUERPOS_HOTBAR.length, hotbar.getChildren().size());

        for (int i = 0; i < hotbar.getChildren().size(); i++) {
            VBox slot = (VBox) hotbar.getChildren().get(i);
            assertEquals(3, slot.getChildren().size());

            Label lblKey = (Label) slot.getChildren().get(0);
            StackPane iconBox = (StackPane) slot.getChildren().get(1);
            Label lblNom = (Label) slot.getChildren().get(2);

            assertTrue(lblKey.getText().startsWith("["));
            assertNotNull(iconBox);
            assertFalse(lblNom.getText().isBlank());

            assertFalse(lblNom.getText().contains("Requiere:"));
            assertFalse(lblNom.getText().contains("Da/t:"));
        }
    }

    @Test
    void testHotbarRetractilToggleYVisibilidad() {
        SimulacionSolar sim = new SimulacionSolar(1000, 1000);
        BarraInventarioHotbar hotbar = new BarraInventarioHotbar(sim);

        assertFalse(hotbar.isRetraida());
        assertTrue(hotbar.isVisible());
        assertTrue(hotbar.isManaged());

        Button btnToggle = hotbar.getBotonToggle();
        assertNotNull(btnToggle);
        assertEquals("Ocultar Planetas", btnToggle.getText());

        hotbar.alternarRetraccion();
        assertTrue(hotbar.isRetraida());
        assertFalse(hotbar.isVisible());
        assertFalse(hotbar.isManaged());
        assertEquals("Mostrar Planetas", btnToggle.getText());

        hotbar.alternarRetraccion();
        assertFalse(hotbar.isRetraida());
        assertTrue(hotbar.isVisible());
        assertTrue(hotbar.isManaged());
        assertEquals("Ocultar Planetas", btnToggle.getText());
    }

    @Test
    void testVentanaDescripcionCaracteristicas() {
        for (TipoCuerpo tipo : new TipoCuerpo[]{TipoCuerpo.ESTRELLA, TipoCuerpo.PLANETA_ROCOSO, TipoCuerpo.LUNA}) {
            VBox caracBox = VentanaDescripcionCuerpo.crearSeccionCaracteristicas(tipo);
            assertNotNull(caracBox);
            assertEquals(2, caracBox.getChildren().size());

            Label lblTitulo = (Label) caracBox.getChildren().get(0);
            assertEquals("CARACTERÍSTICAS Y PROPIEDADES FÍSICAS", lblTitulo.getText());

            GridPane grid = (GridPane) caracBox.getChildren().get(1);
            assertTrue(grid.getChildren().size() >= 14);
        }
    }
}
