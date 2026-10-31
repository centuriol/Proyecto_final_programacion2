package org.example;
import org.example.game.cuerpo.*; import org.example.game.jugador.TipoRecurso; import org.example.game.motor.MotorFisicaPermisiva; import org.example.game.motor.Vector2D; import org.example.game.simulacion.ConfiguracionSimulacion; import org.example.game.simulacion.SimulacionSolar; import org.example.test.JavaFXExtension; import org.junit.jupiter.api.BeforeEach; import org.junit.jupiter.api.Test; import org.junit.jupiter.api.extension.ExtendWith; import java.util.ArrayList; import java.util.List; import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
class ColisionesFisicaTest {

    private MotorFisicaPermisiva motorFisica;

    @BeforeEach
    void setUp() {
        motorFisica = new MotorFisicaPermisiva();
    }

    @Test
    void testColisionColapsoMutuoMasasIguales() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Planeta p1 = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        Planeta p2 = new CuerpoCelesteFactory().crearPlanetaRocoso(10, 0, 1.1, Vector2D.cero());
        cuerpos.add(p1);
        cuerpos.add(p2);

        double masaTotalOriginal = p1.getMasa() + p2.getMasa();
        motorFisica.avanzarPaso(cuerpos);

        assertFalse(cuerpos.contains(p1));
        assertFalse(cuerpos.contains(p2));

        assertTrue(cuerpos.size() >= ConfiguracionSimulacion.FRAGMENTOS_POR_COLAPSO * 2);
        assertTrue(cuerpos.size() <= ConfiguracionSimulacion.MAX_FRAGMENTOS_POR_COLAPSO * 2);
        for (CuerpoCeleste c : cuerpos) {
            assertTrue(c instanceof Meteorito);
            assertTrue(c.getMasa() <= ConfiguracionSimulacion.MASA_MAXIMA_METEORITO + 1e-9);
            assertTrue(c.getRadio() <= ConfiguracionSimulacion.RADIO_MAXIMO_METEORITO + 1e-9);
        }
    }

    @Test
    void testColisionAsimetricaMasasDistintas() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Planeta mayor = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 4.0, Vector2D.cero());
        Planeta menor = new CuerpoCelesteFactory().crearPlanetaRocoso(10, 0, 1.0, Vector2D.cero());
        cuerpos.add(mayor);
        cuerpos.add(menor);

        double masaInicialMayor = mayor.getMasa();
        double masaInicialMenor = menor.getMasa();

        motorFisica.avanzarPaso(cuerpos);

        assertFalse(cuerpos.contains(menor));
        assertTrue(cuerpos.contains(mayor));

        double masaEsperadaMayor = masaInicialMayor + (masaInicialMenor * ConfiguracionSimulacion.PORCENTAJE_GANANCIA_MASA_COLISION);
        assertEquals(masaEsperadaMayor, mayor.getMasa(), 1e-6);

        long cantidadEscombros = cuerpos.stream().filter(c -> c instanceof Meteorito).count();
        assertTrue(cantidadEscombros >= ConfiguracionSimulacion.FRAGMENTOS_POR_COLAPSO);
        assertTrue(cantidadEscombros <= ConfiguracionSimulacion.MAX_FRAGMENTOS_POR_COLAPSO);
        for (CuerpoCeleste c : cuerpos) {
            if (c instanceof Meteorito) {
                assertTrue(c.getMasa() <= ConfiguracionSimulacion.MASA_MAXIMA_METEORITO + 1e-9);
                assertTrue(c.getRadio() <= ConfiguracionSimulacion.RADIO_MAXIMO_METEORITO + 1e-9);
            }
        }
    }

    @Test
    void testColisionAbsorcionTotalProporcionExtrema() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Estrella sol = new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0);
        Planeta planeta = new CuerpoCelesteFactory().crearPlanetaRocoso(10, 0, 1.0, Vector2D.cero());
        cuerpos.add(sol);
        cuerpos.add(planeta);

        double masaInicialSol = sol.getMasa();
        double masaInicialPlaneta = planeta.getMasa();

        motorFisica.avanzarPaso(cuerpos);

        assertFalse(cuerpos.contains(planeta));
        assertTrue(cuerpos.contains(sol));
        assertEquals(1, cuerpos.size());
        assertEquals(masaInicialSol + masaInicialPlaneta, sol.getMasa(), 1e-6);
    }

    @Test
    void testColisionMeteoritosDestruccionMutua() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Meteorito m1 = new CuerpoCelesteFactory().crearMeteoritoJugadorFisica(0, 0, Vector2D.cero());
        Meteorito m2 = new CuerpoCelesteFactory().crearMeteoritoJugadorFisica(2, 0, Vector2D.cero());
        cuerpos.add(m1);
        cuerpos.add(m2);

        motorFisica.avanzarPaso(cuerpos);

        assertTrue(cuerpos.isEmpty());
    }

    @Test
    void testEscudoPlanetarioNoColapsaPlaneta() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Planeta p1 = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        p1.activarEscudo(1);
        Planeta p2 = new CuerpoCelesteFactory().crearPlanetaRocoso(5, 0, 1.0, Vector2D.cero());
        cuerpos.add(p1);
        cuerpos.add(p2);

        motorFisica.avanzarPaso(cuerpos);

        assertTrue(cuerpos.contains(p1));
        assertTrue(cuerpos.contains(p2));
        assertEquals(2, cuerpos.size());
    }

    @Test
    void testIntercepcionEscudoRecursos() {
        SimulacionSolar simulacion = new SimulacionSolar(1920, 1080);
        simulacion.limpiarCuerpos();
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 200.0);

        Planeta planeta = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(planeta);
        simulacion.getServicioEscudo().activar(simulacion, planeta);
        assertTrue(planeta.tieneEscudo());

        Meteorito meteorito = new CuerpoCelesteFactory().crearMeteoritoJugadorFisica(10, 0, Vector2D.cero());
        simulacion.agregarCuerpo(meteorito);

        simulacion.avanzarTick();

        assertFalse(simulacion.getSistemaSolar().getCuerpos().contains(meteorito));
        assertTrue(simulacion.getSistemaSolar().getCuerpos().contains(planeta));
    }

    @Test
    void testAtraccionColapsoPlaneta5xYPlaneta2x() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Planeta p5x = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 5.0, Vector2D.cero());
        Planeta p2x = new CuerpoCelesteFactory().crearPlanetaRocoso(70, 0, 2.0, Vector2D.cero());
        cuerpos.add(p5x);
        cuerpos.add(p2x);

        List<String> mensajes = new ArrayList<>();
        motorFisica.setListenerMensaje(mensajes::add);

        motorFisica.avanzarPaso(cuerpos);

        assertTrue(p2x.getVelocidad().x() < 0);
        assertTrue(p5x.getVelocidad().x() > 0);
        assertTrue(Math.abs(p2x.getVelocidad().x()) > Math.abs(p5x.getVelocidad().x()));

        for (int i = 0; i < 50 && cuerpos.contains(p2x); i++) {
            motorFisica.avanzarPaso(cuerpos);
        }

        assertFalse(cuerpos.contains(p2x));
        assertTrue(cuerpos.contains(p5x));
    }
}
