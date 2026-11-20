package org.example;
import org.example.game.cuerpo.*; import org.example.game.jugador.TipoRecurso; import org.example.game.motor.ConstantesFisicas; import org.example.game.motor.ResolvedorColisiones; import org.example.game.motor.Vector2D; import org.example.game.simulacion.GeneradorEventosCosmicos; import org.example.game.simulacion.SimulacionSolar; import org.example.test.JavaFXExtension; import org.junit.jupiter.api.BeforeEach; import org.junit.jupiter.api.Test; import org.junit.jupiter.api.extension.ExtendWith; import java.util.ArrayList; import java.util.HashSet; import java.util.List; import java.util.Set; import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
class EventosCosmicosYSatelitesTest {

    private SimulacionSolar simulacion;

    @BeforeEach
    void setUp() {
        simulacion = new SimulacionSolar(1920, 1080);
        simulacion.limpiarCuerpos();
    }

    @Test
    void testMeteoritosRestanDiezPorCientoYExtinguenCivilizacionSinEscudo() {
        simulacion.getProgresion().desbloquearTodo();
        Planeta planetaInicial = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(planetaInicial);
        double masaOriginal = planetaInicial.getMasa();
        assertTrue(planetaInicial.tieneCivilizacion());

        Meteorito met = new CuerpoCelesteFactory().crearMeteoritoJugadorFisica(0, 0, Vector2D.cero());
        simulacion.agregarCuerpo(met);
        assertEquals(masaOriginal * 0.10, met.getMasa(), masaOriginal * 1e-6);

        simulacion.avanzarTick();
        assertTrue(simulacion.getSistemaSolar().getCuerpos().contains(planetaInicial));
        assertEquals(masaOriginal * 0.90, planetaInicial.getMasa(), masaOriginal * 1e-6);
        assertFalse(planetaInicial.tieneCivilizacion(), "La civilización debe extinguirse al impactar el meteorito");
        assertEquals(0.0, planetaInicial.getCivilizacion().getPoblacion(), 1e-6);
    }

    @Test
    void testGeneradorEventosCosmicosNoSpawneaSinPlanetasYTardaEnAparecer() {
        GeneradorEventosCosmicos gen = new GeneradorEventosCosmicos();
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        List<CuerpoCeleste> agregados = new ArrayList<>();

        cuerpos.add(new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0));
        gen.actualizarEventosCosmicos(cuerpos, 1920, 1080, agregados::add, evento -> {}, 1800);
        assertTrue(agregados.isEmpty());

        GeneradorEventosCosmicos genPlaneta = new GeneradorEventosCosmicos(1);
        Planeta planeta = new CuerpoCelesteFactory().crearPlanetaAgua(100, 0, 1.0, Vector2D.cero());
        cuerpos.add(planeta);
        genPlaneta.actualizarEventosCosmicos(cuerpos, 1920, 1080, agregados::add, evento -> {}, 1800);
        assertEquals(1, agregados.size());
        assertTrue(agregados.get(0) instanceof Meteorito);
    }

    @Test
    void testAgujeroNegroAtraeTodoSinImportarDistanciaYConFuerza() {
        simulacion.getProgresion().desbloquearTodo();
        AgujeroNegro bh = new CuerpoCelesteFactory().crearAgujeroNegroEstelar(0, 0, 1.0);
        simulacion.agregarCuerpo(bh);

        Planeta planetaLejano = new CuerpoCelesteFactory().crearPlanetaAgua(3000, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(planetaLejano);

        assertEquals(0.0, planetaLejano.getVelocidad().magnitud());

        simulacion.avanzarTick();

        Vector2D velFinal = planetaLejano.getVelocidad();
        assertTrue(velFinal.x() < -10.0);
        assertTrue(velFinal.magnitud() >= 12.0);
    }

    @Test
    void testFusionEstrellasCreaAgujeroNegro() {
        simulacion.getProgresion().desbloquearTodo();
        Estrella sol1 = new CuerpoCelesteFactory().crearEstrella(-10, 0, 1.0);
        sol1.setVelocidad(new Vector2D(10, 0));
        Estrella sol2 = new CuerpoCelesteFactory().crearEstrella(10, 0, 1.0);
        sol2.setVelocidad(new Vector2D(-10, 0));

        simulacion.agregarCuerpo(sol1);
        simulacion.agregarCuerpo(sol2);

        simulacion.avanzarTick();

        boolean hayAgujeroNegro = simulacion.getSistemaSolar().getCuerpos().stream()
                .anyMatch(c -> c.getTipoCuerpo() == TipoCuerpo.AGUJERO_NEGRO);
        assertTrue(hayAgujeroNegro);
    }

    @Test
    void testSateliteOrbitaPlanetasComoLaLuna() {
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.MINERALES, 1000);
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 1000);

        Planeta planeta = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(planeta);

        simulacion.getGestorColocacion().entrarModoColocacion(TipoCuerpo.SATELITE, 1.0, "Sonda-1");
        org.example.game.motor.ConversorCoordenadas conversor = new org.example.game.motor.TransformadorCoordenadas();
        simulacion.getGestorColocacion().actualizarPosicionPreview(conversor.fisicaAXJavaFX(35, 1920), conversor.fisicaAYJavaFX(0, 1080));
        simulacion.getGestorColocacion().confirmarColocacionAutoOrbita(simulacion);

        CuerpoCeleste sat = simulacion.getSistemaSolar().getCuerpos().stream()
                .filter(c -> c.getTipoCuerpo() == TipoCuerpo.SATELITE)
                .findFirst().orElse(null);

        assertNotNull(sat);
        assertTrue(sat.estaOrbitando(planeta));
        assertTrue(sat.getVelocidad().magnitud() > 0);
    }

    @Test
    void testLunaOrbitaMasLejosQueSatelites() {
        simulacion.getPresetsSistema().cargarPresetSistemaBasico(simulacion);

        CuerpoCeleste tierra = simulacion.getSistemaSolar().getCuerpos().stream()
                .filter(c -> c instanceof Planeta).findFirst().orElse(null);
        CuerpoCeleste luna = simulacion.getSistemaSolar().getCuerpos().stream()
                .filter(c -> c instanceof Luna).findFirst().orElse(null);

        assertNotNull(tierra);
        assertNotNull(luna);

        double distLuna = luna.getPosicion().distanciaA(tierra.getPosicion());
        assertTrue(distLuna >= 60.0);
    }

    @Test
    void testMultiplesSatelitesEnUnPlanetaNoColapsanEntreSi() {
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.MINERALES, 5000);
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 5000);

        Planeta planeta = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(planeta);

        simulacion.getGestorCicloVida().spawnCuerpo(TipoCuerpo.SATELITE, "Sat-1", 30, 0, 1.0, new Vector2D(0, 1.58));
        simulacion.getGestorCicloVida().spawnCuerpo(TipoCuerpo.SATELITE, "Sat-2", -30, 0, 1.0, new Vector2D(0, -1.58));
        simulacion.getGestorCicloVida().spawnCuerpo(TipoCuerpo.SATELITE, "Sat-3", 0, 35, 1.0, new Vector2D(-1.46, 0));

        long countSats = simulacion.getSistemaSolar().getCuerpos().stream()
                .filter(c -> c.getTipoCuerpo() == TipoCuerpo.SATELITE).count();
        assertEquals(3, countSats);

        simulacion.avanzarTicks(50);

        long countSatsDespues = simulacion.getSistemaSolar().getCuerpos().stream()
                .filter(c -> c.getTipoCuerpo() == TipoCuerpo.SATELITE).count();
        assertEquals(3, countSatsDespues);

        ResolvedorColisiones resolvedor = new ResolvedorColisiones();
        Satelite sA = (Satelite) new CuerpoCelesteFactory().crearSatelite(10, 10, 1.0, Vector2D.cero());
        Satelite sB = (Satelite) new CuerpoCelesteFactory().crearSatelite(10, 10, 1.0, Vector2D.cero());
        Set<CuerpoCeleste> aEliminar = new HashSet<>();
        List<CuerpoCeleste> aAgregar = new ArrayList<>();

        resolvedor.resolverColision(sA, sB, aEliminar, aAgregar);
        assertTrue(aEliminar.isEmpty());
        assertTrue(aAgregar.isEmpty());
    }

    @Test
    void testVariosSatelitesEnMismoPlanetaChocandoseDurante200TicksMantienenOrbita() {
        Planeta planeta = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        Luna luna = new CuerpoCelesteFactory().crearLuna(65, 0, 0.8, new Vector2D(0, -1.07));
        luna.setCuerpoOrbitado(planeta);
        List<Satelite> satelites = List.of(
                new CuerpoCelesteFactory().crearSatelite(28, 0, 1.0, new Vector2D(0, 1.6)),
                new CuerpoCelesteFactory().crearSatelite(28, 2, 1.0, new Vector2D(0, -1.6)),
                new CuerpoCelesteFactory().crearSatelite(12, 0, 1.0, new Vector2D(0, 1.6)),
                new CuerpoCelesteFactory().crearSatelite(58, 0, 1.0, new Vector2D(0, -1.5))
        );
        simulacion.agregarCuerpo(planeta);
        simulacion.agregarCuerpo(luna);
        satelites.forEach(simulacion::agregarCuerpo);

        for (int tick = 0; tick < 200; tick++) {
            assertDoesNotThrow(() -> simulacion.avanzarTick());
        }

        double distLuna = luna.getPosicion().distanciaA(planeta.getPosicion());
        assertTrue(simulacion.getSistemaSolar().getCuerpos().contains(luna) && luna.estaOrbitando(planeta));
        assertTrue(distLuna >= 60.0);
        for (Satelite sat : satelites) {
            double distSat = sat.getPosicion().distanciaA(planeta.getPosicion());
            assertTrue(simulacion.getSistemaSolar().getCuerpos().contains(sat) && sat.esSimulado());
            assertSame(planeta, sat.getCuerpoOrbitado());
            assertTrue(distSat > planeta.getRadio() + sat.getRadio());
            assertTrue(distSat + sat.getRadio() < distLuna - luna.getRadio());
            assertTrue(sat.getVelocidad().magnitud() < 10.0);
        }
    }

    @Test
    void testLunaNoEsInterrumpidaPorSatelitesAlEntrarEnOrbitaNiAlOrbitarPlanetaEnMovimiento() {
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.MINERALES, 50000);
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 50000);

        Estrella sol = new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0);
        Vector2D posPlaneta = new Vector2D(175, 0);
        Vector2D velPlaneta = org.example.game.motor.CalculadorOrbital.calcularVelocidadOrbitalVector(sol, posPlaneta, false);
        Planeta planeta = new CuerpoCelesteFactory().crearPlanetaRocoso(posPlaneta.x(), posPlaneta.y(), 1.0, velPlaneta);
        planeta.setCuerpoOrbitado(sol);
        planeta.setRadioOrbita(175.0);

        Vector2D posPlaneta2 = new Vector2D(290, 0);
        Vector2D velPlaneta2 = org.example.game.motor.CalculadorOrbital.calcularVelocidadOrbitalVector(sol, posPlaneta2, false);
        Planeta planeta2 = new CuerpoCelesteFactory().crearPlanetaAgua(posPlaneta2.x(), posPlaneta2.y(), 2.0, velPlaneta2);
        planeta2.setCuerpoOrbitado(sol);
        planeta2.setRadioOrbita(290.0);

        simulacion.agregarCuerpo(sol);
        simulacion.agregarCuerpo(planeta);
        simulacion.agregarCuerpo(planeta2);

        // Satélites ya orbitando el planeta
        List<Satelite> satelites = List.of(
                new CuerpoCelesteFactory().crearSatelite(posPlaneta.x() + 28, posPlaneta.y(), 1.0, velPlaneta),
                new CuerpoCelesteFactory().crearSatelite(posPlaneta.x() - 28, posPlaneta.y(), 1.0, velPlaneta),
                new CuerpoCelesteFactory().crearSatelite(posPlaneta.x(), posPlaneta.y() + 28, 1.0, velPlaneta)
        );
        satelites.forEach(simulacion::agregarCuerpo);
        simulacion.avanzarTicks(5);

        // Colocar Luna justo sobre la zona de los satélites (dist=28px) para verificar que entra en órbita sin ser interrumpida
        Luna luna = new CuerpoCelesteFactory().crearLuna(
                planeta.getPosicion().x() + 28, planeta.getPosicion().y(), 1.0, planeta.getVelocidad());
        simulacion.agregarCuerpo(luna);

        simulacion.avanzarTick();
        assertTrue(luna.estaOrbitando(planeta), "La luna debe entrar en órbita del planeta sin ser bloqueada por los satélites");

        Vector2D rRelPrev = luna.getPosicion().restar(planeta.getPosicion());
        double anguloPrev = Math.atan2(rRelPrev.y(), rRelPrev.x());
        double avanceAngularTotal = 0.0;

        for (int tick = 0; tick < 300; tick++) {
            simulacion.avanzarTick();
            assertTrue(luna.esSimulado() && luna.estaOrbitando(planeta), "La luna no debe perder su órbita en el tick " + tick);

            Vector2D rRel = luna.getPosicion().restar(planeta.getPosicion());
            double distLuna = rRel.magnitud();
            assertTrue(distLuna >= 60.0 - 1e-3, "La luna debe mantenerse en su carril exterior en el tick " + tick);

            double anguloActual = Math.atan2(rRel.y(), rRel.x());
            double deltaAngulo = anguloActual - anguloPrev;
            while (deltaAngulo <= -Math.PI) deltaAngulo += 2 * Math.PI;
            while (deltaAngulo > Math.PI) deltaAngulo -= 2 * Math.PI;
            assertTrue(Math.abs(deltaAngulo) > 1e-4, "La luna debe avanzar continuamente sin quedarse trabada");
            avanceAngularTotal += Math.abs(deltaAngulo);
            anguloPrev = anguloActual;

            for (Satelite sat : satelites) {
                assertTrue(sat.esSimulado() && sat.estaOrbitando(planeta));
                double distSat = sat.getPosicion().distanciaA(planeta.getPosicion());
                assertTrue(distSat + sat.getRadio() < distLuna - luna.getRadio());
            }
        }
        assertTrue(avanceAngularTotal > Math.PI, "La luna debe completar un recorrido orbital continuo alrededor del planeta");
    }
}
