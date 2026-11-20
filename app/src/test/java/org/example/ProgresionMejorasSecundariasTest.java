package org.example;

import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.cuerpo.Estrella;
import org.example.game.cuerpo.Planeta;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;
import org.example.game.motor.Vector2D;
import org.example.game.progresion.MejoraSecundaria;
import org.example.game.progresion.RamaProgresion;
import org.example.game.progresion.SistemaProgresion;
import org.example.game.simulacion.ConfiguracionEconomia;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.GestorEconomia;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.PantallaArbolProgresion;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(JavaFXExtension.class)
class ProgresionMejorasSecundariasTest {

    private SimulacionSolar simulacion;

    @BeforeEach
    void setUp() {
        simulacion = new SimulacionSolar(1920, 1080);
        simulacion.limpiarCuerpos();
    }

    @Test
    void testMejorasSecundariasRequierenNodoPrincipalYNoBloqueanCaminoPrincipal() {
        SistemaProgresion prog = simulacion.getProgresion();
        InventarioJugador inv = simulacion.getInventario();

        assertEquals(3, RamaProgresion.values().length);
        for (MejoraSecundaria m : MejoraSecundaria.values()) {
            assertNotNull(m.rama());
            assertTrue(m.tier() >= 1 && m.tier() <= 5);
            double costoPrincipal = ConfiguracionEconomia.costoProgresionNivel(m.tier());
            assertTrue(m.costo() < costoPrincipal, "La mejora secundaria " + m.id() + " debe costar menos que el nodo principal");
        }

        inv.agregarRecurso(TipoRecurso.CIENCIA, 10000);
        assertFalse(prog.puedeInvestigarSecundaria(MejoraSecundaria.PLANETAS_T1_MINERIA, inv));
        assertFalse(prog.investigarSecundaria(MejoraSecundaria.PLANETAS_T1_MINERIA, inv));

        assertTrue(prog.investigar(RamaProgresion.PLANETAS, inv));
        assertTrue(prog.investigar(RamaProgresion.PLANETAS, inv));
        assertEquals(2, prog.getNivel(RamaProgresion.PLANETAS));
        assertFalse(prog.estaInvestigada(MejoraSecundaria.PLANETAS_T1_MINERIA));

        assertTrue(prog.puedeInvestigarSecundaria(MejoraSecundaria.PLANETAS_T1_MINERIA, inv));
        assertTrue(prog.puedeInvestigarSecundaria(MejoraSecundaria.PLANETAS_T2_COSTO, inv));
        assertFalse(prog.puedeInvestigarSecundaria(MejoraSecundaria.PLANETAS_T3_PRODUCCION, inv));

        assertTrue(prog.investigarSecundaria(MejoraSecundaria.PLANETAS_T1_MINERIA, inv));
        assertTrue(prog.estaInvestigada(MejoraSecundaria.PLANETAS_T1_MINERIA));
        assertFalse(prog.investigarSecundaria(MejoraSecundaria.PLANETAS_T1_MINERIA, inv));
    }

    @Test
    void testMultiplicadoresMejorasSecundariasEnEconomiaEInventario() {
        SistemaProgresion prog = simulacion.getProgresion();
        InventarioJugador inv = simulacion.getInventario();

        Estrella sol = new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0);
        Planeta tierra = new CuerpoCelesteFactory().crearPlanetaAgua(200, 0, 1.0, new Vector2D(0, -10));
        tierra.setCuerpoOrbitado(sol);
        simulacion.agregarCuerpo(sol);
        simulacion.agregarCuerpo(tierra);

        double costoAguaBaseMin = inv.calcularCostoTotal(TipoCuerpo.PLANETA_AGUA, 1.0).get(TipoRecurso.MINERALES);
        double costoSatBaseMin = inv.calcularCostoTotal(TipoCuerpo.SATELITE, 1.0).get(TipoRecurso.MINERALES);

        GestorEconomia gestorEconomia = new GestorEconomia(prog);
        inv.getRecursoObj(TipoRecurso.MINERALES).setCantidad(0);
        gestorEconomia.actualizarEconomia(simulacion.getSistemaSolar().getCuerpos(), inv);
        double prodMinBase = inv.getRecurso(TipoRecurso.MINERALES);
        assertTrue(prodMinBase > 0);

        inv.agregarRecurso(TipoRecurso.CIENCIA, 100000);
        for (int i = 0; i < 5; i++) {
            prog.investigar(RamaProgresion.PLANETAS, inv);
            prog.investigar(RamaProgresion.MASA, inv);
            prog.investigar(RamaProgresion.CANTIDAD_PLANETAS, inv);
        }

        assertTrue(prog.investigarSecundaria(MejoraSecundaria.PLANETAS_T1_MINERIA, inv));
        assertTrue(prog.investigarSecundaria(MejoraSecundaria.PLANETAS_T2_COSTO, inv));
        assertTrue(prog.investigarSecundaria(MejoraSecundaria.PLANETAS_T4_ESCUDO, inv));
        assertTrue(prog.investigarSecundaria(MejoraSecundaria.MASA_T1_COSTO, inv));
        assertTrue(prog.investigarSecundaria(MejoraSecundaria.MASA_T2_PRODUCCION, inv));
        assertTrue(prog.investigarSecundaria(MejoraSecundaria.MASA_T3_SATELITE, inv));
        assertTrue(prog.investigarSecundaria(MejoraSecundaria.CANTIDAD_T1_POBLACION, inv));
        assertTrue(prog.investigarSecundaria(MejoraSecundaria.CANTIDAD_T2_CRECIMIENTO, inv));
        assertTrue(prog.investigarSecundaria(MejoraSecundaria.CANTIDAD_T3_COLONIZAR, inv));

        assertTrue(prog.multiplicadorProduccion(TipoCuerpo.PLANETA_AGUA, TipoRecurso.MINERALES) > 1.0);
        assertTrue(prog.multiplicadorCosto(TipoCuerpo.PLANETA_AGUA) < 1.0);
        assertTrue(prog.multiplicadorCosto(TipoCuerpo.SATELITE) < 1.0);
        assertTrue(prog.multiplicadorCostoEscudo() < 1.0);
        assertTrue(prog.multiplicadorPoblacionMaxima() > 1.0);
        assertTrue(prog.multiplicadorCrecimientoPoblacion() > 1.0);
        assertTrue(prog.multiplicadorCostoColonizacion() < 1.0);

        double costoAguaConMejoras = inv.calcularCostoTotal(TipoCuerpo.PLANETA_AGUA, 1.0).get(TipoRecurso.MINERALES);
        double costoSatConMejoras = inv.calcularCostoTotal(TipoCuerpo.SATELITE, 1.0).get(TipoRecurso.MINERALES);
        assertTrue(costoAguaConMejoras < costoAguaBaseMin);
        assertTrue(costoSatConMejoras < costoSatBaseMin);

        inv.getRecursoObj(TipoRecurso.MINERALES).setCantidad(0);
        gestorEconomia.actualizarEconomia(simulacion.getSistemaSolar().getCuerpos(), inv);
        assertTrue(inv.getRecurso(TipoRecurso.MINERALES) > prodMinBase);
    }

    @Test
    void testPantallaArbolNeuronaEInspectorSecundarias() {
        PantallaArbolProgresion pantalla = new PantallaArbolProgresion(simulacion, () -> {});
        pantalla.alternar();
        assertTrue(pantalla.isVisible());

        simulacion.getInventario().agregarRecurso(TipoRecurso.CIENCIA, 5000);
        simulacion.getProgresion().investigar(RamaProgresion.PLANETAS, simulacion.getInventario());
        pantalla.refrescar();
        pantalla.getInspectorPanel().seleccionarSecundaria(MejoraSecundaria.PLANETAS_T1_MINERIA);
        assertEquals(MejoraSecundaria.PLANETAS_T1_MINERIA, pantalla.getInspectorPanel().getMejoraSecundariaSeleccionada());

        assertFalse(MejoraSecundaria.deRamaYTier(RamaProgresion.PLANETAS, 1).isEmpty());
        assertEquals(2, MejoraSecundaria.deRamaYTier(RamaProgresion.PLANETAS, 5).size());
    }

    @Test
    void testColocarSolRespetaProgresionDeMasaYNoPierdeConMuchaMasa() {
        simulacion.getInventario().agregarRecurso(TipoRecurso.MINERALES, 500_000);
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 500_000);

        assertEquals(0, simulacion.getProgresion().getNivel(RamaProgresion.MASA));
        var solExcedidoOpt = simulacion.getGestorCicloVida().spawnCuerpo(
                TipoCuerpo.ESTRELLA, "SolBloqueado", 0, 0, ConfiguracionSimulacion.MASA_FACTOR_MAX, Vector2D.cero()
        );
        assertTrue(solExcedidoOpt.isEmpty());

        simulacion.getProgresion().desbloquearTodo();
        var solMasivoOpt = simulacion.getGestorCicloVida().spawnCuerpo(
                TipoCuerpo.ESTRELLA, "SolGigante", 0, 0, ConfiguracionSimulacion.MASA_FACTOR_MAX, Vector2D.cero()
        );
        assertTrue(solMasivoOpt.isPresent());
        assertTrue(solMasivoOpt.get().getMasa() >= 1e31);

        for (int i = 0; i < 30; i++) simulacion.avanzarTick();
        assertFalse(simulacion.estaPerdida());
    }
}
