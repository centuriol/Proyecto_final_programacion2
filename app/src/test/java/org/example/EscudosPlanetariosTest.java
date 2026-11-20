package org.example;

import org.example.game.cuerpo.*;
import org.example.game.jugador.TipoRecurso;
import org.example.game.motor.Vector2D;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.SimulacionSolar;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(JavaFXExtension.class)
class EscudosPlanetariosTest {

    private SimulacionSolar simulacion;

    @BeforeEach
    void setUp() {
        simulacion = new SimulacionSolar(1920, 1080);
        simulacion.limpiarCuerpos();
    }

    @Test
    void testActivacionEscudoYSateliteOrbital() {
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 500);

        Planeta planeta = new CuerpoCelesteFactory().crearPlanetaAgua(0, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(planeta);

        assertFalse(planeta.tieneEscudo());
        assertTrue(planeta.puedeRecibirEscudo());

        boolean activado = simulacion.getServicioEscudo().activar(simulacion, planeta);
        assertTrue(activado);
        assertTrue(planeta.tieneEscudo());
        assertEquals(5, planeta.getGolpesEscudoRestantes());

        boolean creoSateliteAutomatico = simulacion.getSistemaSolar().getCuerpos().stream()
                .anyMatch(c -> c.getTipoCuerpo() == TipoCuerpo.SATELITE);
        assertFalse(creoSateliteAutomatico, "Activar el escudo no debe desplegar un satélite automáticamente");

        Satelite satelite = new CuerpoCelesteFactory().crearSatelite(25, 0, 1.0, Vector2D.cero());
        satelite.setCuerpoOrbitado(planeta);
        simulacion.agregarCuerpo(satelite);

        assertEquals(ConfiguracionSimulacion.PRODUCCION_SATELITE_CIENCIA, satelite.getTipoCuerpo().getProduccionBase().get(TipoRecurso.CIENCIA), 1e-6);
        assertEquals(ConfiguracionSimulacion.PRODUCCION_SATELITE_ENERGIA, satelite.getTipoCuerpo().getProduccionBase().get(TipoRecurso.ENERGIA), 1e-6);
        assertNull(satelite.getTipoCuerpo().getProduccionBase().get(TipoRecurso.MINERALES));

        simulacion.getGestorCicloVida().eliminarCuerpo(planeta);
        assertFalse(simulacion.getSistemaSolar().getCuerpos().contains(satelite));
    }

    @Test
    void testEscudoAbsorbeMeteoritosYSeAgotanGolpes() {
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 500);

        Planeta planeta = new CuerpoCelesteFactory().crearPlanetaAgua(0, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(planeta);
        simulacion.getServicioEscudo().activar(simulacion, planeta);

        double masaInicial = planeta.getMasa();
        int golpes = planeta.getGolpesEscudoRestantes();

        for (int i = 0; i < golpes; i++) {
            assertTrue(planeta.tieneEscudo());
            Meteorito met = new CuerpoCelesteFactory().crearMeteoritoJugadorFisica(0, 0, Vector2D.cero());
            simulacion.agregarCuerpo(met);
            simulacion.avanzarTick();
            assertEquals(masaInicial, planeta.getMasa(), 1e-3);
        }

        assertFalse(planeta.tieneEscudo());
    }

    @Test
    void testEscudoNoProtegeAnteEstrellasNiAgujeroNegro() {
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 500);

        Planeta planeta = new CuerpoCelesteFactory().crearPlanetaAgua(0, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(planeta);
        simulacion.getServicioEscudo().activar(simulacion, planeta);

        Estrella sol = new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0);
        simulacion.agregarCuerpo(sol);

        simulacion.avanzarTick();
        assertFalse(simulacion.getSistemaSolar().getCuerpos().contains(planeta));
    }

    @Test
    void testEscudoMasCaroSegunMasaDelPlaneta() {
        simulacion.getProgresion().desbloquearTodo();

        Planeta planetaChico = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 0.3, Vector2D.cero());
        Planeta planetaNormal = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        Planeta planetaGrande = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 2.0, Vector2D.cero());
        Planeta planetaColosal = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 5.0, Vector2D.cero());

        ServicioEscudo servicioEscudo = new ServicioEscudo();
        double costoChico = servicioEscudo.calcularCostoEscudo(planetaChico);
        double costoNormal = servicioEscudo.calcularCostoEscudo(planetaNormal);
        double costoGrande = servicioEscudo.calcularCostoEscudo(planetaGrande);
        double costoColosal = servicioEscudo.calcularCostoEscudo(planetaColosal);

        assertTrue(costoChico < costoNormal);
        assertEquals(ConfiguracionSimulacion.COSTO_ACTIVACION_ESCUDO_ENERGIA, costoNormal, 1e-6);
        assertTrue(costoGrande > costoNormal);
        assertTrue(costoColosal > costoGrande);
        assertEquals(ConfiguracionSimulacion.COSTO_ACTIVACION_ESCUDO_ENERGIA * 5.0, costoColosal, 1e-6);
    }
}
