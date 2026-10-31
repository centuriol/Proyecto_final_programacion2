package org.example;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.cuerpo.*;
import org.example.game.motor.TransformadorCuerpos;
import org.example.game.motor.Vector2D;
import org.example.game.simulacion.SimulacionSolar;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
class TransformacionCuerposTest {

    private SimulacionSolar simulacion;

    @BeforeEach
    void setUp() {
        simulacion = new SimulacionSolar(1920, 1080);
    }

    @Test
    void testEasterEggTransformacionAguaAPorroCercaDelSol() {
        simulacion.limpiarCuerpos();
        Estrella sol = new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0);
        Planeta agua = new CuerpoCelesteFactory().crearPlanetaAgua(90, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(sol);
        simulacion.agregarCuerpo(agua);

        assertEquals(TipoCuerpo.PLANETA_AGUA, agua.getTipoCuerpo());
        assertFalse(agua.isTransformadoPorEvento());

        for (int i = 0; i < ConfiguracionSimulacion.TICKS_ESPERA_TRANSFORMACION_EASTER_EGG - 1; i++) {
            simulacion.avanzarTick();
            assertEquals(TipoCuerpo.PLANETA_AGUA, agua.getTipoCuerpo());
        }

        simulacion.avanzarTick();
        assertEquals(TipoCuerpo.PLANETA_ROCOSO, agua.getTipoCuerpo());
        assertTrue(agua.isTransformadoPorEvento());
        assertEquals("Planeta Porro", agua.getNombre());

        for (int i = 0; i < ConfiguracionSimulacion.TICKS_MIGRACION_ORBITA; i++) {
            simulacion.avanzarTick();
        }
        assertTrue(agua.estaOrbitando(sol));
    }

    @Test
    void testEasterEggTransformacionPorroALavaMuyCercaDelSol() {
        simulacion.limpiarCuerpos();
        Estrella sol = new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0);
        Planeta porro = new CuerpoCelesteFactory().crearPlanetaRocoso(60, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(sol);
        simulacion.agregarCuerpo(porro);

        assertEquals(TipoCuerpo.PLANETA_ROCOSO, porro.getTipoCuerpo());

        for (int i = 0; i < ConfiguracionSimulacion.TICKS_ESPERA_TRANSFORMACION_EASTER_EGG; i++) {
            simulacion.avanzarTick();
        }

        assertEquals(TipoCuerpo.PLANETA_LAVA, porro.getTipoCuerpo());
        assertTrue(porro.isTransformadoPorEvento());
        assertEquals("Planeta Lava", porro.getNombre());
    }

    @Test
    void testEasterEggGradualEsperaYMigracionSinSaltos() {
        simulacion.limpiarCuerpos();
        Estrella sol = new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0);
        Planeta agua = new CuerpoCelesteFactory().crearPlanetaAgua(90, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(sol);
        simulacion.agregarCuerpo(agua);

        for (int i = 0; i < ConfiguracionSimulacion.TICKS_ESPERA_TRANSFORMACION_EASTER_EGG; i++) {
            simulacion.avanzarTick();
        }
        assertEquals(TipoCuerpo.PLANETA_ROCOSO, agua.getTipoCuerpo());

        Vector2D posAnterior = agua.getPosicion();
        for (int i = 0; i < ConfiguracionSimulacion.TICKS_MIGRACION_ORBITA; i++) {
            simulacion.avanzarTick();
            Vector2D posNueva = agua.getPosicion();
            double salto = posNueva.distanciaA(posAnterior);
            assertTrue(salto < 25.0, "El desplazamiento por tick en la migracion debe ser gradual");
            posAnterior = posNueva;
        }

        assertTrue(agua.estaOrbitando(sol));
        assertTrue(agua.getVelocidad().magnitud() > 0);
    }

    @Test
    void testCancelacionAMitadPorSalidaDeRadio() {
        simulacion.limpiarCuerpos();
        Estrella sol = new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0);
        Planeta agua = new CuerpoCelesteFactory().crearPlanetaAgua(90, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(sol);
        simulacion.agregarCuerpo(agua);

        for (int i = 0; i < ConfiguracionSimulacion.TICKS_ESPERA_TRANSFORMACION_EASTER_EGG / 2; i++) {
            simulacion.avanzarTick();
        }

        assertTrue(simulacion.getTransformadorCuerpos().tieneProcesoActivo(agua));

        agua.setPosicion(new Vector2D(500, 500));
        simulacion.avanzarTick();

        assertFalse(simulacion.getTransformadorCuerpos().tieneProcesoActivo(agua));
        assertEquals(TipoCuerpo.PLANETA_AGUA, agua.getTipoCuerpo());
        assertFalse(agua.isTransformadoPorEvento());
    }

    @Test
    void testPlanetaEliminadoDuranteEsperaNoDejaEstadoColgado() {
        simulacion.limpiarCuerpos();
        Estrella sol = new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0);
        Planeta agua = new CuerpoCelesteFactory().crearPlanetaAgua(90, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(sol);
        simulacion.agregarCuerpo(agua);

        simulacion.avanzarTick();
        assertTrue(simulacion.getTransformadorCuerpos().tieneProcesoActivo(agua));

        simulacion.getGestorCicloVida().eliminarCuerpo(agua);
        simulacion.avanzarTick();

        assertFalse(simulacion.getTransformadorCuerpos().tieneProcesoActivo(agua));
        assertEquals(0, simulacion.getTransformadorCuerpos().getCantidadProcesosActivos());
    }

    @Test
    void testDestinoLibreEligeOrbitaEstableYMigra() {
        TransformadorCuerpos transformador = new TransformadorCuerpos();
        Estrella sol = new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0);
        Planeta agua = new CuerpoCelesteFactory().crearPlanetaAgua(90, 0, 1.0, Vector2D.cero());

        List<CuerpoCeleste> cuerpos = new ArrayList<>(List.of(sol, agua));
        Vector2D destino = transformador.encontrarPosicionOrbitalLibre(agua, sol, ConfiguracionSimulacion.RADIO_TRANSFORMACION_AGUA_PORRO, cuerpos);

        assertNotNull(destino);
        double distSol = destino.distanciaA(sol.getPosicion());
        assertTrue(distSol >= ConfiguracionSimulacion.RADIO_TRANSFORMACION_AGUA_PORRO);
    }

    @Test
    void testDestinoOcupadoBuscaOrbitaLibreOAbortaSinColision() {
        TransformadorCuerpos transformador = new TransformadorCuerpos();
        Estrella sol = new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0);
        Planeta agua = new CuerpoCelesteFactory().crearPlanetaAgua(90, 0, 1.0, Vector2D.cero());

        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        cuerpos.add(sol);
        cuerpos.add(agua);

        double radioBase = ConfiguracionSimulacion.RADIO_TRANSFORMACION_AGUA_PORRO + 40.0;
        for (int intento = 0; intento < 10; intento++) {
            double radio = radioBase + intento * ConfiguracionSimulacion.PASO_BUSQUEDA_ORBITA_PX;
            for (int ang = 0; ang < 16; ang++) {
                double rad = Math.toRadians(ang * 22.5);
                double x = Math.cos(rad) * radio;
                double y = Math.sin(rad) * radio;
                Planeta obstaculo = new CuerpoCelesteFactory().crearPlanetaRocoso(x, y, 1.0, Vector2D.cero());
                cuerpos.add(obstaculo);
            }
        }

        Vector2D destino = transformador.encontrarPosicionOrbitalLibre(agua, sol, ConfiguracionSimulacion.RADIO_TRANSFORMACION_AGUA_PORRO, cuerpos);

        if (destino != null) {
            for (CuerpoCeleste c : cuerpos) {
                if (c == agua || c == sol) continue;
                double minDist = agua.getRadio() + c.getRadio() + ConfiguracionSimulacion.MARGEN_SEGURIDAD_ORBITA_LIBRE;
                assertTrue(destino.distanciaA(c.getPosicion()) >= minDist);
            }
        } else {
            assertNull(destino);
        }
    }
}
