package org.example;
import org.example.game.cuerpo.*;
import org.example.game.jugador.TipoRecurso;
import org.example.game.motor.MotorFisicaPermisiva;
import org.example.game.motor.Vector2D;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.SimulacionSolar;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
class CivilizacionSinergiaTest {

    private MotorFisicaPermisiva motorFisica;

    @BeforeEach
    void setUp() {
        motorFisica = new MotorFisicaPermisiva();
    }

    @Test
    void testGeneracionRecursosPorCuerpoYSlowGrowth() {
        SimulacionSolar simulacion = new SimulacionSolar(1920, 1080);
        simulacion.limpiarCuerpos();

        Estrella sol = new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0);
        simulacion.agregarCuerpo(sol);

        double eneAntes = simulacion.getInventario().getRecurso(TipoRecurso.ENERGIA);
        double minAntes = simulacion.getInventario().getRecurso(TipoRecurso.MINERALES);
        double cieAntes = simulacion.getInventario().getRecurso(TipoRecurso.CIENCIA);
        double pobAntes = simulacion.getInventario().getRecurso(TipoRecurso.POBLACION);

        simulacion.avanzarTick();

        assertEquals(eneAntes + ConfiguracionSimulacion.PRODUCCION_ESTRELLA_ENERGIA, simulacion.getInventario().getRecurso(TipoRecurso.ENERGIA), 1e-6);
        assertEquals(minAntes + ConfiguracionSimulacion.PRODUCCION_ESTRELLA_MINERALES, simulacion.getInventario().getRecurso(TipoRecurso.MINERALES), 1e-6);
        assertEquals(cieAntes + ConfiguracionSimulacion.PRODUCCION_ESTRELLA_CIENCIA, simulacion.getInventario().getRecurso(TipoRecurso.CIENCIA), 1e-6);
        assertEquals(pobAntes, simulacion.getInventario().getRecurso(TipoRecurso.POBLACION), 1e-6);

        simulacion.limpiarCuerpos();
        Planeta planeta = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(planeta);

        eneAntes = simulacion.getInventario().getRecurso(TipoRecurso.ENERGIA);
        minAntes = simulacion.getInventario().getRecurso(TipoRecurso.MINERALES);
        cieAntes = simulacion.getInventario().getRecurso(TipoRecurso.CIENCIA);
        pobAntes = simulacion.getInventario().getRecurso(TipoRecurso.POBLACION);

        simulacion.avanzarTick();

        assertEquals(eneAntes, simulacion.getInventario().getRecurso(TipoRecurso.ENERGIA), 1e-6);
        assertEquals(minAntes + ConfiguracionSimulacion.PRODUCCION_PLANETA_MINERALES, simulacion.getInventario().getRecurso(TipoRecurso.MINERALES), 1e-3);
        assertEquals(cieAntes + ConfiguracionSimulacion.PRODUCCION_PLANETA_CIENCIA, simulacion.getInventario().getRecurso(TipoRecurso.CIENCIA), 1e-3);
        assertEquals(pobAntes + ConfiguracionSimulacion.PRODUCCION_PLANETA_POBLACION, simulacion.getInventario().getRecurso(TipoRecurso.POBLACION), 1e-3);

        simulacion.limpiarCuerpos();
        Luna luna = new CuerpoCelesteFactory().crearLuna(0, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(luna);

        eneAntes = simulacion.getInventario().getRecurso(TipoRecurso.ENERGIA);
        minAntes = simulacion.getInventario().getRecurso(TipoRecurso.MINERALES);
        cieAntes = simulacion.getInventario().getRecurso(TipoRecurso.CIENCIA);
        pobAntes = simulacion.getInventario().getRecurso(TipoRecurso.POBLACION);

        simulacion.avanzarTick();

        assertEquals(eneAntes, simulacion.getInventario().getRecurso(TipoRecurso.ENERGIA), 1e-6);
        assertEquals(cieAntes, simulacion.getInventario().getRecurso(TipoRecurso.CIENCIA), 1e-6);
        assertEquals(pobAntes, simulacion.getInventario().getRecurso(TipoRecurso.POBLACION), 1e-6);
        assertEquals(minAntes + ConfiguracionSimulacion.PRODUCCION_LUNA_MINERALES, simulacion.getInventario().getRecurso(TipoRecurso.MINERALES), 1e-6);

        assertTrue(ConfiguracionSimulacion.PRODUCCION_ESTRELLA_ENERGIA <= 0.10);
        assertTrue(ConfiguracionSimulacion.PRODUCCION_PLANETA_MINERALES <= 0.05);
        assertTrue(ConfiguracionSimulacion.PRODUCCION_PLANETA_CIENCIA <= 0.05);
        assertTrue(ConfiguracionSimulacion.PRODUCCION_PLANETA_POBLACION <= 0.05);
        assertTrue(ConfiguracionSimulacion.PRODUCCION_LUNA_MINERALES <= 0.05);
    }

    @Test
    void testGeneracionPasivaMinerales() {
        SimulacionSolar simulacion = new SimulacionSolar(1920, 1080);
        simulacion.limpiarCuerpos();

        double mineralesAntes = simulacion.getInventario().getRecurso(TipoRecurso.MINERALES);
        simulacion.avanzarTick();
        assertEquals(mineralesAntes, simulacion.getInventario().getRecurso(TipoRecurso.MINERALES), 1e-6);

        Luna luna = new CuerpoCelesteFactory().crearLuna(0, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(luna);
        simulacion.avanzarTick();
        double mineralesDespues = simulacion.getInventario().getRecurso(TipoRecurso.MINERALES);

        assertTrue(mineralesDespues > mineralesAntes);
        assertEquals(
                mineralesAntes + ConfiguracionSimulacion.PRODUCCION_LUNA_MINERALES,
                mineralesDespues,
                1e-6
        );
    }

    @Test
    void testMultiplicadorProduccionDinamicaCivilizacion() {
        Planeta p = new CuerpoCelesteFactory().crearPlanetaRocoso(150, 0, 1.0, Vector2D.cero());
        p.desarrollarCivilizacion();
        var civ = p.getCivilizacion();

        double multInicial = civ.calcularMultiplicadorProduccion();
        assertTrue(multInicial >= 1.0);

        civ.setTicksEfectoEventoReciente(50);
        double multPenalizado = civ.calcularMultiplicadorProduccion();
        assertTrue(multPenalizado < multInicial);

        for (int i = 0; i < 50; i++) {
            civ.actualizar(List.of(p));
        }
        assertEquals(0, civ.getTicksEfectoEventoReciente());
    }

    @Test
    void testSinergiaCivilizacionesMultiples() {
        SimulacionSolar simulacion = new SimulacionSolar(1920, 1080);
        simulacion.limpiarCuerpos();

        Planeta p1 = new CuerpoCelesteFactory().crearPlanetaRocoso(150, 0, 1.0, Vector2D.cero());
        p1.desarrollarCivilizacion();
        simulacion.agregarCuerpo(p1);

        double minAntes1 = simulacion.getInventario().getRecurso(TipoRecurso.MINERALES);
        simulacion.avanzarTick();
        double delta1 = simulacion.getInventario().getRecurso(TipoRecurso.MINERALES) - minAntes1;

        Planeta p2 = new CuerpoCelesteFactory().crearPlanetaRocoso(250, 0, 1.0, Vector2D.cero());
        p2.desarrollarCivilizacion();
        simulacion.agregarCuerpo(p2);

        double minAntes2 = simulacion.getInventario().getRecurso(TipoRecurso.MINERALES);
        simulacion.avanzarTick();
        double delta2 = simulacion.getInventario().getRecurso(TipoRecurso.MINERALES) - minAntes2;

        assertTrue(delta2 >= 1.99 * delta1);
    }

    @Test
    void testPlanetaOrbitaSolComoLaLuna() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Estrella sol = new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0);
        Planeta planeta = new CuerpoCelesteFactory().crearPlanetaPersonalizado("tierra1", TipoCuerpo.PLANETA_ROCOSO.masaBase, 160, 0, TipoCuerpo.PLANETA_ROCOSO, Vector2D.cero());
        cuerpos.add(sol);
        cuerpos.add(planeta);

        List<String> mensajes = new ArrayList<>();
        motorFisica.setListenerMensaje(mensajes::add);

        motorFisica.avanzarPaso(cuerpos);

        assertTrue(planeta.estaOrbitando(sol));
        assertTrue(mensajes.contains("tierra1 esta orbitando " + sol.getNombre()));

        for (int i = 0; i < 40; i++) {
            motorFisica.avanzarPaso(cuerpos);
        }
        assertTrue(cuerpos.contains(planeta));
        assertTrue(cuerpos.contains(sol));
    }

    @Test
    void testPlanetaEnOrbitaDelSolNoDecaeNiEsAbsorbidoALargoPlazo() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Estrella sol = new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0);
        Planeta tierra = new CuerpoCelesteFactory().crearPlanetaPersonalizado("tierra", TipoCuerpo.PLANETA_ROCOSO.masaBase, 160, 0, TipoCuerpo.PLANETA_ROCOSO, Vector2D.cero());
        Planeta marte = new CuerpoCelesteFactory().crearPlanetaPersonalizado("marte", TipoCuerpo.PLANETA_ROCOSO.masaBase, 280, 0, TipoCuerpo.PLANETA_ROCOSO, Vector2D.cero());
        cuerpos.add(sol);
        cuerpos.add(tierra);
        cuerpos.add(marte);

        for (int i = 0; i < 400; i++) {
            motorFisica.avanzarPaso(cuerpos);
        }

        assertTrue(cuerpos.contains(tierra));
        assertTrue(cuerpos.contains(marte));
        assertTrue(cuerpos.contains(sol));

        assertTrue(tierra.estaOrbitando(sol));
        assertTrue(marte.estaOrbitando(sol));

        double distFinalTierra = tierra.getPosicion().distanciaA(sol.getPosicion());
        double distFinalMarte = marte.getPosicion().distanciaA(sol.getPosicion());

        assertEquals(160.0, distFinalTierra, 5.0);
        assertEquals(280.0, distFinalMarte, 5.0);
    }

    @Test
    void testColocacionPlanetaEnOrbitasDelSolAutoOrbitaYEstabilidad() {
        SimulacionSolar sim = new SimulacionSolar(1920, 1080);
        sim.getProgresion().desbloquearTodo();
        sim.limpiarCuerpos();

        Estrella sol = new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0);
        sim.agregarCuerpo(sol);

        sim.getInventario().agregarRecurso(TipoRecurso.MINERALES, 5000);
        sim.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 5000);

        sim.getGestorColocacion().entrarModoColocacion(TipoCuerpo.PLANETA_ROCOSO, 1.0, "NuevoPlaneta");
        sim.getGestorColocacion().actualizarPosicionPreview(1120, 540);
        sim.getGestorColocacion().confirmarColocacionAutoOrbita(sim);

        CuerpoCeleste nuevoPlaneta = sim.getSistemaSolar().getCuerpos().stream()
                .filter(c -> "NuevoPlaneta".equals(c.getNombre()))
                .findFirst()
                .orElse(null);

        assertNotNull(nuevoPlaneta);
        assertTrue(nuevoPlaneta.estaOrbitando(sol));
        assertEquals(160.0, nuevoPlaneta.getRadioOrbita(), 1.0);

        for (int i = 0; i < 200; i++) {
            sim.avanzarTick();
        }

        assertTrue(sim.getSistemaSolar().getCuerpos().contains(nuevoPlaneta));
        double distFinal = nuevoPlaneta.getPosicion().distanciaA(sol.getPosicion());
        assertEquals(160.0, distFinal, 5.0);
    }
}
