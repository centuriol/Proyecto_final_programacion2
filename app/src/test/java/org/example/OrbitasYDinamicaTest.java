package org.example;
import org.example.game.cuerpo.*; import org.example.game.motor.MotorFisicaPermisiva; import org.example.game.motor.TrayectoriaPredictor; import org.example.game.motor.Vector2D; import org.example.game.simulacion.SimulacionSolar; import org.example.test.JavaFXExtension; import org.junit.jupiter.api.BeforeEach; import org.junit.jupiter.api.Test; import org.junit.jupiter.api.extension.ExtendWith; import java.util.ArrayList; import java.util.List; import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
class OrbitasYDinamicaTest {

    private MotorFisicaPermisiva motorFisica;

    @BeforeEach
    void setUp() {
        motorFisica = new MotorFisicaPermisiva();
    }

    @Test
    void testFisicaPermisivaAvancePaso() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Estrella estrella = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        cuerpos.add(estrella);

        Vector2D pos = new Vector2D(150, 0);
        Vector2D vel = CuerpoCelesteFactory.calcularVelocidadOrbitalVector(estrella, pos, false);
        Planeta planeta = CuerpoCelesteFactory.crearPlanetaRocoso(pos.x(), pos.y(), 1.0, vel);
        cuerpos.add(planeta);

        Vector2D posInicial = new Vector2D(planeta.getPosicionX(), planeta.getPosicionY());

        for (int i = 0; i < 20; i++) {
            motorFisica.avanzarPaso(cuerpos);
        }

        Vector2D posFinal = new Vector2D(planeta.getPosicionX(), planeta.getPosicionY());
        assertNotEquals(posInicial.y(), posFinal.y());
        assertTrue(cuerpos.contains(planeta));
    }

    @Test
    void testPredictorTrayectoria() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Estrella estrella = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        cuerpos.add(estrella);

        Vector2D posInicial = new Vector2D(150, 0);
        Vector2D velInicial = CuerpoCelesteFactory.calcularVelocidadOrbitalVector(estrella, posInicial, false);

        TrayectoriaPredictor predictor = new TrayectoriaPredictor();
        List<Vector2D> tray = predictor.predecirTrayectoria(posInicial, velInicial, 5.97e24, cuerpos, 50, 1.0);
        assertNotNull(tray);
        assertEquals(51, tray.size());
    }

    @Test
    void testAsistenciaOrbitalAtenuadaPorMasa() {
        double asistenciaBase = motorFisica.getAsistenciaOrbital();
        double masaTierra = TipoCuerpo.PLANETA_ROCOSO.masaBase;

        double asist1x = motorFisica.calcularAsistenciaEfectiva(masaTierra);
        assertEquals(asistenciaBase, asist1x, 1e-6);

        double asist10x = motorFisica.calcularAsistenciaEfectiva(masaTierra * 10.0);
        assertTrue(asist10x < asist1x);
        assertEquals(asistenciaBase / 2.0, asist10x, 1e-6);
    }

    @Test
    void testRadioAtraccionJerarquiaYMenorAreaLuna() {
        Estrella estrella = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        Planeta planeta1x = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        Planeta planeta2x = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 2.0, Vector2D.cero());
        Planeta planeta5x = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 5.0, Vector2D.cero());
        Luna luna1x = CuerpoCelesteFactory.crearLuna(0, 0, 1.0, Vector2D.cero());

        assertTrue(luna1x.getRadioAtraccion() < planeta1x.getRadioAtraccion());
        assertTrue(planeta1x.getRadioAtraccion() < estrella.getRadioAtraccion());
        assertTrue(planeta5x.getRadioAtraccion() > planeta2x.getRadioAtraccion());
        assertTrue(planeta2x.getRadioAtraccion() > planeta1x.getRadioAtraccion());
    }

    @Test
    void testPlanetasADistanciaRazonableNoColapsan() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Planeta p5x = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 5.0, Vector2D.cero());
        Planeta p2x = CuerpoCelesteFactory.crearPlanetaRocoso(250, 0, 2.0, Vector2D.cero());
        cuerpos.add(p5x);
        cuerpos.add(p2x);

        for (int i = 0; i < 40; i++) {
            motorFisica.avanzarPaso(cuerpos);
        }

        assertTrue(cuerpos.contains(p5x));
        assertTrue(cuerpos.contains(p2x));
    }

    @Test
    void testLunaOrbitaPlanetaYNotificaMensaje() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Planeta planeta = CuerpoCelesteFactory.crearPlanetaPersonalizado("planeta rocoso1", TipoCuerpo.PLANETA_ROCOSO.masaBase, 0, 0, TipoCuerpo.PLANETA_ROCOSO, Vector2D.cero());
        Luna luna = CuerpoCelesteFactory.crearLunaPersonalizada("luna1", TipoCuerpo.LUNA.masaBase, 30, 0, Vector2D.cero());
        cuerpos.add(planeta);
        cuerpos.add(luna);

        List<String> mensajes = new ArrayList<>();
        motorFisica.setListenerMensaje(mensajes::add);

        motorFisica.avanzarPaso(cuerpos);

        assertTrue(luna.estaOrbitando(planeta));
        assertTrue(mensajes.contains("luna1 esta orbitando planeta rocoso1"));

        for (int i = 0; i < 40; i++) {
            motorFisica.avanzarPaso(cuerpos);
        }
        assertTrue(cuerpos.contains(luna));
        assertTrue(cuerpos.contains(planeta));
    }

    @Test
    void testDosLunasCercanasColapsan() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Luna luna1 = CuerpoCelesteFactory.crearLunaPersonalizada("luna1", TipoCuerpo.LUNA.masaBase, 0, 0, Vector2D.cero());
        Luna luna2 = CuerpoCelesteFactory.crearLunaPersonalizada("luna2", TipoCuerpo.LUNA.masaBase, 20, 0, Vector2D.cero());
        cuerpos.add(luna1);
        cuerpos.add(luna2);

        List<String> mensajes = new ArrayList<>();
        motorFisica.setListenerMensaje(mensajes::add);

        for (int i = 0; i < 40 && (cuerpos.contains(luna1) || cuerpos.contains(luna2)); i++) {
            motorFisica.avanzarPaso(cuerpos);
        }

        assertFalse(cuerpos.contains(luna1));
        assertFalse(cuerpos.contains(luna2));
        assertTrue(mensajes.stream().anyMatch(m -> m.contains("colapsaron en escombros")));
    }

    @Test
    void testLunaOrbitaMasLentoYPresetActualizado() {
        SimulacionSolar sim = new SimulacionSolar(1000, 1000);
        sim.cargarPresetSistemaBasico();

        CuerpoCeleste tierra = sim.getSistemaSolar().getCuerpos().stream()
                .filter(c -> c instanceof Planeta).findFirst().orElse(null);
        CuerpoCeleste luna = sim.getSistemaSolar().getCuerpos().stream()
                .filter(c -> c instanceof Luna).findFirst().orElse(null);
        CuerpoCeleste sol = sim.getSistemaSolar().getCuerpos().stream()
                .filter(c -> c instanceof Estrella).findFirst().orElse(null);

        assertNotNull(tierra);
        assertNotNull(luna);
        assertNotNull(sol);

        assertTrue(tierra.estaOrbitando(sol));
        assertTrue(luna.estaOrbitando(tierra));

        Vector2D vRelLuna = luna.getVelocidad().restar(tierra.getVelocidad());
        assertTrue(vRelLuna.magnitud() < 2.5);
    }
}
