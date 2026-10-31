package org.example;
import org.example.game.cuerpo.*;
import org.example.game.motor.MotorFisicaPermisiva;
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
class AgujeroNegroFisicaTest {

    private MotorFisicaPermisiva motorFisica;

    @BeforeEach
    void setUp() {
        motorFisica = new MotorFisicaPermisiva();
    }

    @Test
    void testAbsorcionAgujeroNegroMasa() {
        SimulacionSolar simulacion = new SimulacionSolar(1920, 1080);
        simulacion.limpiarCuerpos();

        AgujeroNegro bh = new CuerpoCelesteFactory().crearAgujeroNegroEstelar(0, 0, 1.0);
        double masaInicialBH = bh.getMasa();
        simulacion.agregarCuerpo(bh);

        Planeta presa = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 2.0, Vector2D.cero());
        double masaPresa = presa.getMasa();
        simulacion.agregarCuerpo(presa);

        simulacion.avanzarTick();

        assertFalse(simulacion.getSistemaSolar().getCuerpos().contains(presa));
        assertEquals(masaInicialBH + masaPresa, bh.getMasa(), 1e-6);
    }

    @Test
    void testAbsorcionesEnCadenaAgujeroNegro() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        AgujeroNegro bh = new CuerpoCelesteFactory().crearAgujeroNegroEstelar(0, 0, 1.0);
        cuerpos.add(bh);

        Planeta p1 = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        Planeta p2 = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 2.0, Vector2D.cero());
        cuerpos.add(p1);
        cuerpos.add(p2);

        double masaTotalEsperada = bh.getMasa() + p1.getMasa() + p2.getMasa();
        motorFisica.avanzarPaso(cuerpos);

        assertEquals(1, cuerpos.size());
        assertTrue(cuerpos.contains(bh));
        assertEquals(masaTotalEsperada, bh.getMasa(), 1e-6);
    }

    @Test
    void testIntegracionMovimientoMasaInvalidaProtegida() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Planeta p = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        p.setMasa(-10.0);
        cuerpos.add(p);

        assertDoesNotThrow(() -> motorFisica.avanzarPaso(cuerpos));
        assertFalse(Double.isNaN(p.getPosicion().x()));
        assertFalse(Double.isNaN(p.getPosicion().y()));
    }

    @Test
    void testGravedadProporcionalAMasa() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Planeta pesado = new CuerpoCelesteFactory().crearPlanetaGaseoso(-100, 0, 1.0, Vector2D.cero());
        Planeta liviano = new CuerpoCelesteFactory().crearPlanetaRocoso(100, 0, 1.0, Vector2D.cero());
        cuerpos.add(pesado);
        cuerpos.add(liviano);

        motorFisica.setAsistenciaOrbital(0.0);

        double xInicialPesado = pesado.getPosicion().x();
        double xInicialLiviano = liviano.getPosicion().x();

        motorFisica.avanzarPaso(cuerpos);

        double dispPesado = Math.abs(pesado.getPosicion().x() - xInicialPesado);
        double dispLiviano = Math.abs(liviano.getPosicion().x() - xInicialLiviano);

        assertTrue(dispLiviano > dispPesado * 50);
        assertTrue(pesado.getPosicion().x() > xInicialPesado);
        assertTrue(liviano.getPosicion().x() < xInicialLiviano);
    }

    @Test
    void testSolAtraeYComePlanetaCercano() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Estrella sol = new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0);
        Planeta planetaCercano = new CuerpoCelesteFactory().crearPlanetaRocoso(45, 0, 1.0, Vector2D.cero());
        cuerpos.add(sol);
        cuerpos.add(planetaCercano);

        double masaSolInicial = sol.getMasa();
        List<String> mensajes = new ArrayList<>();
        motorFisica.setListenerMensaje(mensajes::add);

        for (int i = 0; i < 60 && cuerpos.contains(planetaCercano); i++) {
            motorFisica.avanzarPaso(cuerpos);
        }

        assertFalse(cuerpos.contains(planetaCercano));
        assertTrue(sol.getMasa() > masaSolInicial);
        assertTrue(mensajes.stream().anyMatch(m -> m.contains("absorbió por gravedad")));
    }

    @Test
    void testSolNoComePlanetaADistanciaOrbital() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Estrella sol = new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0);
        Vector2D posTierra = new Vector2D(160, 0);
        Vector2D velTierra = new CuerpoCelesteFactory().calcularVelocidadOrbitalVector(sol, posTierra, false);
        Planeta tierra = new CuerpoCelesteFactory().crearPlanetaRocoso(posTierra.x(), posTierra.y(), 1.0, velTierra);
        cuerpos.add(sol);
        cuerpos.add(tierra);

        for (int i = 0; i < 60; i++) {
            motorFisica.avanzarPaso(cuerpos);
        }

        assertTrue(cuerpos.contains(tierra));
    }
}
