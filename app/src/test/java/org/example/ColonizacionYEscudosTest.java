package org.example;
import org.example.game.civilizacion.CivilizacionComponent;
import org.example.game.colonizacion.NaveColonizadora;
import org.example.game.cuerpo.*;
import org.example.game.jugador.TipoRecurso;
import org.example.game.motor.Vector2D;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.SimulacionSolar;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
class ColonizacionYEscudosTest {

    private SimulacionSolar simulacion;

    @BeforeEach
    void setUp() {
        simulacion = new SimulacionSolar(1920, 1080);
        simulacion.limpiarCuerpos();
    }

    @Test
    void testSoloPlanetaPorroNaceConCivilizacion() {
        Planeta porro = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        Planeta agua = CuerpoCelesteFactory.crearPlanetaAgua(100, 0, 1.0, Vector2D.cero());
        Planeta lava = CuerpoCelesteFactory.crearPlanetaLava(200, 0, 1.0, Vector2D.cero());

        assertTrue(porro.tieneCivilizacion());
        assertFalse(agua.tieneCivilizacion());
        assertFalse(lava.tieneCivilizacion());

        assertTrue(agua.puedeSerColonizado());
        assertTrue(lava.puedeSerColonizado());
    }

    @Test
    void testCrecimientoLogisticoYCapacidad() {
        Estrella sol = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        Vector2D posPorro = new Vector2D(150, 0);
        Vector2D velPorro = CuerpoCelesteFactory.calcularVelocidadOrbitalVector(sol, posPorro, false);
        Planeta porro = CuerpoCelesteFactory.crearPlanetaRocoso(posPorro.x(), posPorro.y(), 1.0, velPorro);
        CivilizacionComponent civ = porro.getCivilizacion();
        assertNotNull(civ);

        double capMaxima = civ.getPoblacionMaxima();
        assertTrue(capMaxima > 0);

        double pobInicial = civ.getPoblacion();
        for (int i = 0; i < 100; i++) {
            civ.actualizar(List.of(sol, porro));
        }

        assertTrue(civ.getPoblacion() >= pobInicial);
        assertTrue(civ.getPoblacion() <= capMaxima);
        assertTrue(civ.getPorcentajeCapacidad() >= 0.0 && civ.getPorcentajeCapacidad() <= 100.0);
    }

    @Test
    void testMecanicaColonizacionYTransferencia() {
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.MINERALES, 1000);
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 1000);

        Planeta origen = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        CivilizacionComponent civOrigen = origen.getCivilizacion();
        civOrigen.setPoblacion(civOrigen.getPoblacionMaxima() * 0.85);

        Planeta destino = CuerpoCelesteFactory.crearPlanetaAgua(500, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(origen);
        simulacion.agregarCuerpo(destino);

        double pobAntesOrigen = civOrigen.getPoblacion();
        boolean lanzada = simulacion.getGestorColonizacion().lanzar(origen, destino, simulacion.getInventario(), simulacion::notificarEvento, 0);
        assertTrue(lanzada);

        assertEquals(1, simulacion.getGestorColonizacion().getNaves().size());
        NaveColonizadora nave = simulacion.getGestorColonizacion().getNaves().get(0);
        assertEquals(pobAntesOrigen * 0.10, nave.getPoblacionColonos(), 1.0);
        assertEquals(pobAntesOrigen * 0.90, civOrigen.getPoblacion(), 1.0);

        while (!simulacion.getGestorColonizacion().getNaves().isEmpty()) {
            simulacion.avanzarTick();
        }

        assertTrue(destino.tieneCivilizacion());
        assertTrue(destino.getCivilizacion().getPoblacion() > 0);
    }

    @Test
    void testMisionColonizadoraSePierdeSiDestinoEsDestruido() {
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.MINERALES, 1000);
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 1000);

        Planeta origen = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        origen.getCivilizacion().setPoblacion(origen.getCivilizacion().getPoblacionMaxima() * 0.90);
        Planeta destino = CuerpoCelesteFactory.crearPlanetaAgua(1000, 0, 1.0, Vector2D.cero());

        simulacion.agregarCuerpo(origen);
        simulacion.agregarCuerpo(destino);

        assertTrue(simulacion.getGestorColonizacion().lanzar(origen, destino, simulacion.getInventario(), simulacion::notificarEvento, 0));

        simulacion.eliminarCuerpo(destino);
        simulacion.getGestorColonizacion().actualizar(simulacion::notificarEvento, 1);
        assertTrue(simulacion.getGestorColonizacion().getNaves().isEmpty());
    }

    @Test
    void testActivacionEscudoYSateliteOrbital() {
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 500);

        Planeta planeta = CuerpoCelesteFactory.crearPlanetaAgua(0, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(planeta);

        assertFalse(planeta.tieneEscudo());
        assertTrue(planeta.puedeRecibirEscudo());

        boolean activado = simulacion.activarEscudo(planeta);
        assertTrue(activado);
        assertTrue(planeta.tieneEscudo());
        assertEquals(5, planeta.getGolpesEscudoRestantes());

        CuerpoCeleste satelite = simulacion.getSistemaSolar().getCuerpos().stream()
                .filter(c -> c.getTipoCuerpo() == TipoCuerpo.SATELITE && c.estaOrbitando(planeta))
                .findFirst()
                .orElse(null);

        assertNotNull(satelite);
        assertEquals(ConfiguracionSimulacion.PRODUCCION_SATELITE_CIENCIA, satelite.getTipoCuerpo().getProduccionBase().get(TipoRecurso.CIENCIA), 1e-6);
        assertEquals(ConfiguracionSimulacion.PRODUCCION_SATELITE_ENERGIA, satelite.getTipoCuerpo().getProduccionBase().get(TipoRecurso.ENERGIA), 1e-6);
        assertNull(satelite.getTipoCuerpo().getProduccionBase().get(TipoRecurso.MINERALES));

        simulacion.eliminarCuerpo(planeta);
        assertFalse(simulacion.getSistemaSolar().getCuerpos().contains(satelite));
    }

    @Test
    void testEscudoAbsorbeMeteoritosYSeAgotanGolpes() {
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 500);

        Planeta planeta = CuerpoCelesteFactory.crearPlanetaAgua(0, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(planeta);
        simulacion.activarEscudo(planeta);

        double masaInicial = planeta.getMasa();
        int golpes = planeta.getGolpesEscudoRestantes();

        for (int i = 0; i < golpes; i++) {
            assertTrue(planeta.tieneEscudo());
            Meteorito met = CuerpoCelesteFactory.crearMeteoritoJugadorFisica(0, 0, Vector2D.cero());
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

        Planeta planeta = CuerpoCelesteFactory.crearPlanetaAgua(0, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(planeta);
        simulacion.activarEscudo(planeta);

        Estrella sol = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        simulacion.agregarCuerpo(sol);

        simulacion.avanzarTick();
        assertFalse(simulacion.getSistemaSolar().getCuerpos().contains(planeta));
    }

    @Test
    void testEscudoMasCaroSegunMasaDelPlaneta() {
        simulacion.getProgresion().desbloquearTodo();

        Planeta planetaChico = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 0.3, Vector2D.cero());
        Planeta planetaNormal = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        Planeta planetaGrande = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 2.0, Vector2D.cero());
        Planeta planetaColosal = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 5.0, Vector2D.cero());

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
