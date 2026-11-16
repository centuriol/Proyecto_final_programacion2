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
    void testMeteoritosDestruyenPlanetasInicialesSinEscudo() {
        simulacion.getProgresion().desbloquearTodo();
        Planeta planetaInicial = new CuerpoCelesteFactory().crearPlanetaAgua(0, 0, 0.3, Vector2D.cero());
        simulacion.agregarCuerpo(planetaInicial);

        Meteorito met = new CuerpoCelesteFactory().crearMeteoritoJugadorFisica(0, 0, Vector2D.cero());
        simulacion.agregarCuerpo(met);

        simulacion.avanzarTick();
        assertFalse(simulacion.getSistemaSolar().getCuerpos().contains(planetaInicial));
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
    void testVariosSatelitesChocandoseEnMismoPlanetaConLunaDurante200TicksMantienenOrbitaPegada() {
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.MINERALES, 10000);
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 10000);

        Estrella sol = new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0);
        Planeta planeta = new CuerpoCelesteFactory().crearPlanetaRocoso(260, 0, 1.0, new Vector2D(0, -10.5));
        planeta.setCuerpoOrbitado(sol);
        planeta.setRadioOrbita(260.0);

        Luna luna = new CuerpoCelesteFactory().crearLuna(260 + 65, 0, 1.0, new Vector2D(0, -12.0));
        luna.setCuerpoOrbitado(planeta);
        luna.setRadioOrbita(65.0);

        simulacion.agregarCuerpo(sol);
        simulacion.agregarCuerpo(planeta);
        simulacion.agregarCuerpo(luna);

        // 4 satelites en la misma banda orbital del planeta (incluyendo dos superpuestos y en sentidos opuestos para forzar cruces/choques continuos)
        Satelite sat1 = new CuerpoCelesteFactory().crearSatelite(260 + 28, 0, 1.0, new Vector2D(0, -9.0));
        Satelite sat2 = new CuerpoCelesteFactory().crearSatelite(260 + 28, 1, 1.0, new Vector2D(0, -12.0));
        Satelite sat3 = new CuerpoCelesteFactory().crearSatelite(260 - 28, 0, 1.0, new Vector2D(0, -9.0));
        Satelite sat4 = new CuerpoCelesteFactory().crearSatelite(260, 22, 1.0, new Vector2D(1.5, -10.5));
        List<Satelite> satelites = List.of(sat1, sat2, sat3, sat4);
        for (Satelite s : satelites) {
            simulacion.agregarCuerpo(s);
        }

        for (int tick = 1; tick <= 200; tick++) {
            simulacion.avanzarTick();

            List<CuerpoCeleste> actuales = simulacion.getSistemaSolar().getCuerpos();
            assertTrue(actuales.contains(planeta), "El planeta debe seguir existiendo en tick " + tick);
            assertTrue(actuales.contains(luna), "La luna debe seguir existiendo sin chocar en tick " + tick);
            assertSame(planeta, luna.getCuerpoOrbitado(), "La luna debe conservar su planeta anfitrion en tick " + tick);

            double distLuna = luna.getPosicion().distanciaA(planeta.getPosicion());
            assertTrue(distLuna >= 55.0, "La luna debe orbitar mas lejos que los satelites en tick " + tick + ", dist=" + distLuna);

            for (Satelite sat : satelites) {
                assertTrue(actuales.contains(sat), "El satelite " + sat.getNombre() + " no debe ser eliminado en tick " + tick);
                assertTrue(sat.esSimulado(), "El satelite debe seguir simulado en tick " + tick);
                assertSame(planeta, sat.getCuerpoOrbitado(),
                        "El satelite debe conservar su cuerpoOrbitado sin perder su orbita en tick " + tick);

                double distPlaneta = sat.getPosicion().distanciaA(planeta.getPosicion());
                double radioContactoPlaneta = planeta.getRadio() + sat.getRadio();
                assertTrue(distPlaneta > radioContactoPlaneta,
                        "El satelite no debe chocar con el planeta en tick " + tick + " (dist=" + distPlaneta + " <= " + radioContactoPlaneta + ")");
                assertTrue(distPlaneta < distLuna - luna.getRadio() - sat.getRadio() + 4.0,
                        "El satelite debe quedar mas pegado al planeta que la luna en tick " + tick + " (distSat=" + distPlaneta + ", distLuna=" + distLuna + ")");
            }
        }
    }
}
