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

        simulacion.entrarModoColocacion(TipoCuerpo.SATELITE, 1.0, "Sonda-1");
        org.example.game.motor.ConversorCoordenadas conversor = new org.example.game.motor.TransformadorCoordenadas();
        simulacion.actualizarPosicionPreview(conversor.fisicaAXJavaFX(35, 1920), conversor.fisicaAYJavaFX(0, 1080));
        simulacion.confirmarColocacionAutoOrbita();

        CuerpoCeleste sat = simulacion.getSistemaSolar().getCuerpos().stream()
                .filter(c -> c.getTipoCuerpo() == TipoCuerpo.SATELITE)
                .findFirst().orElse(null);

        assertNotNull(sat);
        assertTrue(sat.estaOrbitando(planeta));
        assertTrue(sat.getVelocidad().magnitud() > 0);
    }

    @Test
    void testLunaOrbitaMasLejosQueSatelites() {
        simulacion.cargarPresetSistemaBasico();

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

        simulacion.spawnCuerpo(TipoCuerpo.SATELITE, "Sat-1", 30, 0, 1.0, new Vector2D(0, 1.58));
        simulacion.spawnCuerpo(TipoCuerpo.SATELITE, "Sat-2", -30, 0, 1.0, new Vector2D(0, -1.58));
        simulacion.spawnCuerpo(TipoCuerpo.SATELITE, "Sat-3", 0, 35, 1.0, new Vector2D(-1.46, 0));

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
}
