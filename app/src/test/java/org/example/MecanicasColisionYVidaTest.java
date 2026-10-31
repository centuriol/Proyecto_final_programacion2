package org.example;
import javafx.scene.input.ScrollEvent; import org.example.game.civilizacion.EstadoCivilizacion; import org.example.game.simulacion.ConfiguracionSimulacion; import org.example.game.controlador.ControladorMouse; import org.example.game.cuerpo.*; import org.example.game.motor.MotorFisicaPermisiva; import org.example.game.motor.Vector2D; import org.example.game.render.CamaraZoom; import org.example.game.simulacion.SimulacionSolar; import org.example.test.JavaFXExtension; import org.junit.jupiter.api.BeforeEach; import org.junit.jupiter.api.Test; import org.junit.jupiter.api.extension.ExtendWith; import java.util.ArrayList; import java.util.List; import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
class MecanicasColisionYVidaTest {

    private MotorFisicaPermisiva motorFisica;
    private SimulacionSolar simulacion;

    @BeforeEach
    void setUp() {
        motorFisica = new MotorFisicaPermisiva();
        simulacion = new SimulacionSolar(1920, 1080);
    }

    @Test
    void testRadioCapturaEscalaConRadioAtraccionDeEstrella() {
        Estrella estrella1 = new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0);
        Estrella estrellaGrande = new CuerpoCelesteFactory().crearEstrella(0, 0, 4.0);

        assertTrue(estrellaGrande.getRadioAtraccion() > estrella1.getRadioAtraccion());

        Planeta planeta = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 300, 1.0, Vector2D.cero());
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        cuerpos.add(estrella1);
        cuerpos.add(planeta);

        motorFisica.avanzarPaso(cuerpos);

        assertTrue(planeta.estaOrbitando());
        assertEquals(estrella1, planeta.getCuerpoOrbitado());
        assertTrue(planeta.getVelocidad().magnitud() > 0);
    }

    @Test
    void testColisionAsimetricaMayorGanaPorcentajeDeMasa() {
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

        double masaGanadaEsperada = masaInicialMenor * ConfiguracionSimulacion.PORCENTAJE_GANANCIA_MASA_COLISION;
        double masaEsperadaMayor = masaInicialMayor + masaGanadaEsperada;
        assertEquals(masaEsperadaMayor, mayor.getMasa(), 1e-6);

        long countMeteoritos = cuerpos.stream().filter(c -> c instanceof Meteorito).count();
        assertTrue(countMeteoritos >= ConfiguracionSimulacion.FRAGMENTOS_POR_COLAPSO);
        assertTrue(countMeteoritos <= ConfiguracionSimulacion.MAX_FRAGMENTOS_POR_COLAPSO);
    }

    @Test
    void testColisionSimetricaAmbosColapsan() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Planeta p1 = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        Planeta p2 = new CuerpoCelesteFactory().crearPlanetaRocoso(10, 0, 1.0, Vector2D.cero());
        cuerpos.add(p1);
        cuerpos.add(p2);

        motorFisica.avanzarPaso(cuerpos);

        assertFalse(cuerpos.contains(p1));
        assertFalse(cuerpos.contains(p2));
        assertTrue(cuerpos.size() >= ConfiguracionSimulacion.FRAGMENTOS_POR_COLAPSO * 2);
        assertTrue(cuerpos.size() <= ConfiguracionSimulacion.MAX_FRAGMENTOS_POR_COLAPSO * 2);
    }

    @Test
    void testPoblacionEnCeroTrasEliminacionManual() {
        Planeta planeta = new CuerpoCelesteFactory().crearPlanetaRocoso(100, 100, 1.0, Vector2D.cero());
        planeta.desarrollarCivilizacion();
        assertTrue(planeta.tieneCivilizacion());
        assertTrue(planeta.getCivilizacion().getPoblacion() > 0);

        simulacion.agregarCuerpo(planeta);
        simulacion.eliminarCuerpo(planeta);

        assertEquals(0.0, planeta.getCivilizacion().getPoblacion(), 1e-6);
        assertEquals(EstadoCivilizacion.EXTINGUIDA, planeta.getCivilizacion().getEstado());
        assertFalse(planeta.tieneCivilizacion());
    }

    @Test
    void testPoblacionEnCeroTrasColisionAsimetrica() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Planeta mayor = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 5.0, Vector2D.cero());
        Planeta menor = new CuerpoCelesteFactory().crearPlanetaRocoso(5, 0, 1.0, Vector2D.cero());
        menor.desarrollarCivilizacion();
        assertTrue(menor.tieneCivilizacion());

        cuerpos.add(mayor);
        cuerpos.add(menor);

        motorFisica.avanzarPaso(cuerpos);

        assertEquals(0.0, menor.getCivilizacion().getPoblacion(), 1e-6);
        assertFalse(menor.tieneCivilizacion());
    }

    @Test
    void testPoblacionEnCeroTrasAbsorcionPorAgujeroNegro() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        AgujeroNegro bh = new CuerpoCelesteFactory().crearAgujeroNegroEstelar(0, 0, 2.0);
        Planeta victima = new CuerpoCelesteFactory().crearPlanetaRocoso(5, 0, 1.0, Vector2D.cero());
        victima.desarrollarCivilizacion();
        assertTrue(victima.tieneCivilizacion());

        cuerpos.add(bh);
        cuerpos.add(victima);

        motorFisica.avanzarPaso(cuerpos);

        assertEquals(0.0, victima.getCivilizacion().getPoblacion(), 1e-6);
        assertFalse(victima.tieneCivilizacion());
    }

    @Test
    void testCamaraZoomLimitesYReseteo() {
        CamaraZoom zoom = new CamaraZoom();
        assertEquals(1.0, zoom.getNivelZoom(), 1e-6);
        assertTrue(zoom.esZoomNeutro());

        for (int i = 0; i < 20; i++) {
            zoom.alejar();
        }
        assertEquals(0.5, zoom.getNivelZoom(), 1e-6);

        for (int i = 0; i < 30; i++) {
            zoom.acercar();
        }
        assertEquals(1.5, zoom.getNivelZoom(), 1e-6);

        zoom.resetear();
        assertEquals(1.0, zoom.getNivelZoom(), 1e-6);
        assertTrue(zoom.esZoomNeutro());
    }

    @Test
    void testControladorMouseCtrlScrollAjustaZoom() {
        ControladorMouse mouse = new ControladorMouse(simulacion, null, null, 1920, 1080);
        CamaraZoom zoom = new CamaraZoom();
        mouse.setCamaraZoom(zoom);

        double zoomInicial = zoom.getNivelZoom();

        ScrollEvent ctrlScrollUp = new ScrollEvent(
                ScrollEvent.SCROLL, 0, 0, 0, 0, false, true, false, false, false, false,
                0, 10.0, 0, 10.0, ScrollEvent.HorizontalTextScrollUnits.NONE, 0,
                ScrollEvent.VerticalTextScrollUnits.NONE, 0, 0, null
        );
        mouse.manejarScroll(ctrlScrollUp);
        assertTrue(zoom.getNivelZoom() > zoomInicial);

        ScrollEvent ctrlScrollDown = new ScrollEvent(
                ScrollEvent.SCROLL, 0, 0, 0, 0, false, true, false, false, false, false,
                0, -10.0, 0, -10.0, ScrollEvent.HorizontalTextScrollUnits.NONE, 0,
                ScrollEvent.VerticalTextScrollUnits.NONE, 0, 0, null
        );
        mouse.manejarScroll(ctrlScrollDown);
        assertEquals(zoomInicial, zoom.getNivelZoom(), 1e-4);
    }
}
