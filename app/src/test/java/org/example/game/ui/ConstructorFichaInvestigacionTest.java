package org.example.game.ui;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.game.progresion.RamaProgresion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ConstructorFichaInvestigacionTest {

    @BeforeAll
    static void initJavaFX() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            latch.countDown();
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void construyeFichaInvestigadaDisponibleYBloqueada() {
        ConstructorFichaInvestigacion constructor = new ConstructorFichaInvestigacion();
        ConstructorFichaInvestigacion.NodoInfo[] nodos = new ConstructorFichaInvestigacion.NodoInfo[] {
                new ConstructorFichaInvestigacion.NodoInfo(1, "Nivel 1", "Desc 1", 100.0),
                new ConstructorFichaInvestigacion.NodoInfo(2, "Nivel 2", "Desc 2", 200.0),
                new ConstructorFichaInvestigacion.NodoInfo(3, "Nivel 3", "Desc 3", 300.0)
        };

        // Nivel actual = 1, ciencia = 500
        VBox columna = constructor.construirColumnaRama(
                RamaProgresion.PLANETAS,
                "[TEST]",
                "#5be3ff",
                nodos,
                1,
                500.0,
                null,
                null
        );

        assertNotNull(columna);
        // Header items (4) + 3 cards = 7
        assertEquals(7, columna.getChildren().size());

        // Card 1 (investigada)
        VBox cardInvestigada = constructor.construirTarjetaNodo(
                RamaProgresion.PLANETAS, nodos[0], 1, 500.0, null, null
        );
        assertTrue(cardInvestigada.getStyle().contains("#50fa7b"));

        // Card 2 (siguiente a investigar, puede pagar)
        VBox cardDisponible = constructor.construirTarjetaNodo(
                RamaProgresion.PLANETAS, nodos[1], 1, 500.0, null, null
        );
        assertTrue(cardDisponible.getStyle().contains("#ffd700"));
        boolean tieneBoton = cardDisponible.getChildren().stream().anyMatch(n -> n instanceof Button);
        assertTrue(tieneBoton);

        // Card 3 (bloqueada)
        VBox cardBloqueada = constructor.construirTarjetaNodo(
                RamaProgresion.PLANETAS, nodos[2], 1, 500.0, null, null
        );
        assertTrue(cardBloqueada.getStyle().contains("#2b2d3a"));
    }
}
