package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Planeta;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ActualizadorProcesosTransformacionTest {

    @Test
    void abortaSiCuerpoSaleDeRadioActivacion() {
        ActualizadorProcesosTransformacion actualizador = new ActualizadorProcesosTransformacion();

        CuerpoCeleste estrella = new Planeta("Sol", 1.989e30, 0, 0, TipoCuerpo.ESTRELLA);
        CuerpoCeleste planeta = new Planeta("Agua", 5.97e24, 500, 0, TipoCuerpo.PLANETA_AGUA);

        TransformadorCuerpos.ProcesoTransformacion proceso = new TransformadorCuerpos.ProcesoTransformacion(
                planeta, estrella, TipoCuerpo.PLANETA_ROCOSO, "Porro", "Transformado", 200.0
        );

        Map<CuerpoCeleste, TransformadorCuerpos.ProcesoTransformacion> procesosActivos = new HashMap<>();
        procesosActivos.put(planeta, proceso);

        // Distancia es 500, radioActivacion es 200 -> debe abortar y removerse de activos
        actualizador.actualizarProceso(proceso, List.of(estrella, planeta), procesosActivos, Map.of(), null);

        assertFalse(procesosActivos.containsKey(planeta));
    }

    @Test
    void avanzaTicksEsperaHastaIniciarMigracion() {
        ActualizadorProcesosTransformacion actualizador = new ActualizadorProcesosTransformacion();

        CuerpoCeleste estrella = new Planeta("Sol", 1.989e30, 0, 0, TipoCuerpo.ESTRELLA);
        Planeta planeta = new Planeta("Agua", 5.97e24, 150, 0, TipoCuerpo.PLANETA_AGUA);

        TransformadorCuerpos.ProcesoTransformacion proceso = new TransformadorCuerpos.ProcesoTransformacion(
                planeta, estrella, TipoCuerpo.PLANETA_ROCOSO, "Porro", "Transformado", 200.0
        );

        Map<CuerpoCeleste, TransformadorCuerpos.ProcesoTransformacion> procesosActivos = new HashMap<>();
        procesosActivos.put(planeta, proceso);

        List<CuerpoCeleste> cuerpos = List.of(estrella, planeta);

        // Simular hasta que termine la espera
        for (int i = 0; i < ConfiguracionSimulacion.TICKS_ESPERA_TRANSFORMACION_EASTER_EGG; i++) {
            actualizador.actualizarProceso(proceso, cuerpos, procesosActivos, Map.of(planeta, planeta), null);
        }

        assertEquals(TransformadorCuerpos.EstadoTransformacion.MIGRANDO, proceso.getEstado());
        assertEquals("Porro", planeta.getNombre());
    }
}
