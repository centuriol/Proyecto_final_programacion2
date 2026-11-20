package org.example;

import org.example.game.civilizacion.CivilizacionComponent;
import org.example.game.colonizacion.NaveColonizadora;
import org.example.game.cuerpo.*;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;
import org.example.game.motor.CalculadorOrbital;
import org.example.game.motor.Vector2D;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.GestorEconomia;
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
        Planeta porro = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        Planeta agua = new CuerpoCelesteFactory().crearPlanetaAgua(100, 0, 1.0, Vector2D.cero());
        Planeta lava = new CuerpoCelesteFactory().crearPlanetaLava(200, 0, 1.0, Vector2D.cero());

        assertTrue(porro.tieneCivilizacion());
        assertFalse(agua.tieneCivilizacion());
        assertFalse(lava.tieneCivilizacion());
        assertTrue(agua.puedeSerColonizado());
        assertTrue(lava.puedeSerColonizado());
    }

    @Test
    void testCrecimientoLogisticoYCapacidad() {
        Estrella sol = new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0);
        Vector2D posPorro = new Vector2D(150, 0);
        Vector2D velPorro = CalculadorOrbital.calcularVelocidadOrbitalVector(sol, posPorro, false);
        Planeta porro = new CuerpoCelesteFactory().crearPlanetaRocoso(posPorro.x(), posPorro.y(), 1.0, velPorro);
        CivilizacionComponent civ = porro.getCivilizacion();
        assertNotNull(civ);

        double capMaxima = civ.getPoblacionMaxima();
        assertTrue(capMaxima > 0);
        double pobInicial = civ.getPoblacion();
        for (int i = 0; i < 100; i++) civ.actualizar(List.of(sol, porro));

        assertTrue(civ.getPoblacion() >= pobInicial);
        assertTrue(civ.getPoblacion() <= capMaxima);
        assertTrue(civ.getPorcentajeCapacidad() >= 0.0 && civ.getPorcentajeCapacidad() <= 100.0);
    }

    @Test
    void testMecanicaColonizacionYTransferencia() {
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.MINERALES, 1000);
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 1000);

        Planeta origen = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        CivilizacionComponent civOrigen = origen.getCivilizacion();
        civOrigen.setPoblacion(civOrigen.getPoblacionMaxima() * 0.85);

        Planeta destino = new CuerpoCelesteFactory().crearPlanetaAgua(500, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(origen);
        simulacion.agregarCuerpo(destino);

        double pobAntesOrigen = civOrigen.getPoblacion();
        assertTrue(simulacion.getGestorColonizacion().lanzar(origen, destino, simulacion.getInventario(), simulacion.getBusEventos()::notificarEvento, 0));

        assertEquals(1, simulacion.getGestorColonizacion().getNaves().size());
        NaveColonizadora nave = simulacion.getGestorColonizacion().getNaves().get(0);
        assertEquals(pobAntesOrigen * 0.10, nave.getPoblacionColonos(), 1.0);
        assertEquals(pobAntesOrigen * 0.90, civOrigen.getPoblacion(), 1.0);

        while (!simulacion.getGestorColonizacion().getNaves().isEmpty()) simulacion.avanzarTick();
        assertTrue(destino.tieneCivilizacion());
        assertTrue(destino.getCivilizacion().getPoblacion() > 0);
    }

    @Test
    void testMisionColonizadoraSePierdeSiDestinoEsDestruido() {
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.MINERALES, 1000);
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 1000);

        Planeta origen = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        origen.getCivilizacion().setPoblacion(origen.getCivilizacion().getPoblacionMaxima() * 0.90);
        Planeta destino = new CuerpoCelesteFactory().crearPlanetaAgua(1000, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(origen);
        simulacion.agregarCuerpo(destino);

        assertTrue(simulacion.getGestorColonizacion().lanzar(origen, destino, simulacion.getInventario(), simulacion.getBusEventos()::notificarEvento, 0));
        simulacion.getGestorCicloVida().eliminarCuerpo(destino);
        simulacion.getGestorColonizacion().actualizar(simulacion.getBusEventos()::notificarEvento, 1);
        assertTrue(simulacion.getGestorColonizacion().getNaves().isEmpty());
    }

    @Test
    void testOpcionPoblarConPlanetaPorroMilHabitantesYCienciaAguaMasBaratoQueLava() {
        var gestorCol = simulacion.getGestorColonizacion();
        double costoAgua = gestorCol.calcularCostoCienciaPoblar(TipoCuerpo.PLANETA_AGUA);
        double costoLava = gestorCol.calcularCostoCienciaPoblar(TipoCuerpo.PLANETA_LAVA);
        assertTrue(costoAgua < costoLava);

        Planeta porro = new CuerpoCelesteFactory().crearPlanetaRocoso(160, 0, 1.0, Vector2D.cero());
        Planeta agua = new CuerpoCelesteFactory().crearPlanetaAgua(320, 0, 1.0, Vector2D.cero());
        Planeta lava = new CuerpoCelesteFactory().crearPlanetaLava(480, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(porro);
        simulacion.agregarCuerpo(agua);
        simulacion.agregarCuerpo(lava);

        porro.getCivilizacion().setPoblacion(800);
        simulacion.getInventario().agregarRecurso(TipoRecurso.CIENCIA, 2000);
        assertFalse(gestorCol.puedePoblar(agua, simulacion.getSistemaSolar().getCuerpos(), simulacion.getInventario()));

        porro.getCivilizacion().setPoblacion(1200);
        simulacion.getInventario().reiniciar();
        simulacion.getInventario().agregarRecurso(TipoRecurso.CIENCIA, costoAgua + 50.0);
        assertTrue(gestorCol.puedePoblar(agua, simulacion.getSistemaSolar().getCuerpos(), simulacion.getInventario()));
        assertFalse(gestorCol.puedePoblar(lava, simulacion.getSistemaSolar().getCuerpos(), simulacion.getInventario()));
        assertTrue(gestorCol.poblar(agua, simulacion.getSistemaSolar().getCuerpos(), simulacion.getInventario(), null, 0));
        assertTrue(agua.tieneCivilizacion());
    }

    @Test
    void testCrecimientoMasLentoEnOtrosPlanetasYBonusMaterialesX1_3AlEstarPoblado() {
        Estrella sol = new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0);
        Planeta porro = new CuerpoCelesteFactory().crearPlanetaRocoso(160, 0, 1.0,
                CalculadorOrbital.calcularVelocidadOrbitalVector(sol, new Vector2D(160, 0), false));
        Planeta agua = new CuerpoCelesteFactory().crearPlanetaAgua(320, 0, 1.0,
                CalculadorOrbital.calcularVelocidadOrbitalVector(sol, new Vector2D(320, 0), false));

        InventarioJugador invSinPoblar = new InventarioJugador();
        GestorEconomia econ = new GestorEconomia();
        double minBase = invSinPoblar.getRecurso(TipoRecurso.MINERALES);
        econ.actualizarEconomia(List.of(agua), invSinPoblar);
        double prodSinPoblar = invSinPoblar.getRecurso(TipoRecurso.MINERALES) - minBase;

        agua.recibirColonos(500.0);
        porro.getCivilizacion().setPoblacion(500.0);

        InventarioJugador invPoblado = new InventarioJugador();
        double minAntesPoblado = invPoblado.getRecurso(TipoRecurso.MINERALES);
        econ.actualizarEconomia(List.of(agua), invPoblado);
        double prodPoblado = invPoblado.getRecurso(TipoRecurso.MINERALES) - minAntesPoblado;

        assertEquals(prodSinPoblar * ConfiguracionSimulacion.MULTIPLICADOR_PRODUCCION_PLANETA_POBLADO, prodPoblado, 1e-5);

        List<CuerpoCeleste> sistema = List.of(sol, porro, agua);
        for (int i = 0; i < 100; i++) {
            porro.actualizarCivilizacion(sistema);
            agua.actualizarCivilizacion(sistema);
        }
        assertTrue(agua.getCivilizacion().getPoblacion() - 500.0 > 0);
        assertTrue(porro.getCivilizacion().getPoblacion() - 500.0 > (agua.getCivilizacion().getPoblacion() - 500.0) * 2.5);
    }

    @Test
    void testPoblarDesdeCualquierPlanetaConMilHabitantesYElegirOrigenRestandoPoblacion() {
        var gestorCol = simulacion.getGestorColonizacion();
        simulacion.getInventario().agregarRecurso(TipoRecurso.CIENCIA, 5000);

        Planeta aguaOrigen = new CuerpoCelesteFactory().crearPlanetaAgua(160, 0, 1.0, Vector2D.cero());
        Planeta lavaDestino = new CuerpoCelesteFactory().crearPlanetaLava(320, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(aguaOrigen);
        simulacion.agregarCuerpo(lavaDestino);

        aguaOrigen.recibirColonos(1500.0);
        assertTrue(gestorCol.puedePoblar(lavaDestino, simulacion.getSistemaSolar().getCuerpos(), simulacion.getInventario()));

        Planeta porroOrigen = new CuerpoCelesteFactory().crearPlanetaRocoso(480, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(porroOrigen);
        porroOrigen.getCivilizacion().setPoblacion(2000.0);

        List<Planeta> origenes = gestorCol.obtenerPlanetasOrigenParaPoblar(lavaDestino, simulacion.getSistemaSolar().getCuerpos());
        assertEquals(2, origenes.size());
        assertTrue(origenes.contains(aguaOrigen) && origenes.contains(porroOrigen));

        double pobAguaAntes = aguaOrigen.getCivilizacion().getPoblacion();
        double pobPorroAntes = porroOrigen.getCivilizacion().getPoblacion();

        assertTrue(gestorCol.poblarDesde(aguaOrigen, lavaDestino, simulacion.getSistemaSolar().getCuerpos(), simulacion.getInventario(), null, 0));
        assertTrue(lavaDestino.tieneCivilizacion());
        assertTrue(aguaOrigen.getCivilizacion().getPoblacion() < pobAguaAntes);
        assertEquals(pobPorroAntes, porroOrigen.getCivilizacion().getPoblacion(), 1e-6);
    }
}
