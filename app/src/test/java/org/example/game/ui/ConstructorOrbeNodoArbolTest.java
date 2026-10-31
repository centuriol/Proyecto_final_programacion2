package org.example.game.ui;

import javafx.scene.layout.VBox;
import org.example.game.progresion.RamaProgresion;
import org.example.game.simulacion.ConfiguracionEconomia;
import org.example.game.simulacion.SimulacionSolar;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(JavaFXExtension.class)
class ConstructorOrbeNodoArbolTest {

    private final ConstructorOrbeNodoArbol constructor = new ConstructorOrbeNodoArbol();

    @Test
    @DisplayName("Construye orbe investigado, orbe disponible y orbe bloqueado correctamente")
    void testConstruirOrbes() {
        SimulacionSolar sim = new SimulacionSolar(1280, 720);
        PantallaArbolProgresion.NodoArbol nodo1 = new PantallaArbolProgresion.NodoArbol(1, "[LUN]", "Luna", "Desc", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_1);
        PantallaArbolProgresion.NodoArbol nodo2 = new PantallaArbolProgresion.NodoArbol(2, "[POR]", "Planeta Porro", "Desc", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_2);
        PantallaArbolProgresion.NodoArbol nodo3 = new PantallaArbolProgresion.NodoArbol(3, "[SAT]", "Satélite", "Desc", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_3);

        // Nivel actual = 1
        VBox orbeInvestigado = constructor.construir(RamaProgresion.PLANETAS, nodo1, 1, 0, sim, null, null, new HashMap<>(), new HashMap<>());
        assertNotNull(orbeInvestigado);
        assertEquals(3, orbeInvestigado.getChildren().size());

        VBox orbeDisponible = constructor.construir(RamaProgresion.PLANETAS, nodo2, 1, 5000, sim, null, null, new HashMap<>(), new HashMap<>());
        assertNotNull(orbeDisponible);
        assertEquals(3, orbeDisponible.getChildren().size());

        VBox orbeBloqueado = constructor.construir(RamaProgresion.PLANETAS, nodo3, 1, 0, sim, null, null, new HashMap<>(), new HashMap<>());
        assertNotNull(orbeBloqueado);
        assertEquals(3, orbeBloqueado.getChildren().size());
    }
}
