package org.example;
import javafx.scene.input.MouseButton; import javafx.scene.input.MouseEvent; import org.example.game.civilizacion.CivilizacionComponent; import org.example.game.controlador.ControladorMouse; import org.example.game.cuerpo.*; import org.example.game.jugador.Recurso; import org.example.game.jugador.ServicioReembolso; import org.example.game.jugador.TipoRecurso; import org.example.game.motor.MotorFisicaPermisiva; import org.example.game.motor.Vector2D; import org.example.game.render.CamaraZoom; import org.example.game.simulacion.SimulacionSolar; import org.example.test.JavaFXExtension; import org.junit.jupiter.api.DisplayName; import org.junit.jupiter.api.Test; import org.junit.jupiter.api.extension.ExtendWith; import java.util.List; import java.util.Optional; import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
public class Fase2BugsTest {

    @Test
    @DisplayName("Bug 1: SimulacionSolar.step() avanza exactamente 1 tick y permanece pausado")
    void testSimulacionStepPermanecePausado() {
        SimulacionSolar sim = new SimulacionSolar(1920, 1080);
        sim.pause();
        assertTrue(sim.isEnPausa());
        long tickInicial = sim.getTickActual();

        sim.step();
        assertEquals(tickInicial + 1, sim.getTickActual(), "Debe haber avanzado exactamente 1 tick");
        assertTrue(sim.isEnPausa(), "Debe permanecer pausado tras llamar a step()");

        // Un llamado a avanzarTick sin step no debe avanzar mientras esté pausado
        sim.avanzarTick();
        assertEquals(tickInicial + 1, sim.getTickActual(), "No debe seguir avanzando si no se despausó");
    }

    @Test
    @DisplayName("Bug 2: CivilizacionComponent historial avanza y protege divisiones por cero")
    void testCivilizacionComponentHistorialYDivisiones() {
        Planeta planeta = new Planeta("TestCiv", 5.97e24, 150, 0, TipoCuerpo.PLANETA_ROCOSO);
        CivilizacionComponent civ = planeta.getCivilizacion();
        assertNotNull(civ);

        // Verificar que el historial avanza con ticks (cada 10 ticks agrega un registro)
        int tamInicial = civ.getHistorialPoblacion().size();
        for (int i = 1; i <= 35; i++) {
            civ.actualizar(List.of(planeta));
        }
        assertTrue(civ.getHistorialPoblacion().size() > tamInicial, "El historial debe crecer con los ticks");

        // Proteger r=0 y vCircular=0
        Estrella estrellaEnMismaPos = new Estrella("SolZero", 1.989e30, 150, 0);
        assertDoesNotThrow(() -> civ.actualizar(List.of(planeta, estrellaEnMismaPos)));
        assertFalse(Double.isNaN(civ.getEstabilidad()));
        assertFalse(Double.isNaN(civ.getFelicidad()));

        // Proteger poblacionMaxima = 0
        planeta.setMasa(0);
        civ.recalcularPoblacionMaxima();
        assertEquals(0.0, civ.getPorcentajeCapacidad());
        assertFalse(civ.getResumen().contains("NaN"));
    }

    @Test
    @DisplayName("Bug 3: ControladorMouse aplica transformación inversa del zoom")
    void testControladorMouseAplicaTransformacionZoom() {
        SimulacionSolar sim = new SimulacionSolar(1000, 1000);
        ControladorMouse mouse = new ControladorMouse(sim, null, 1000, 1000);
        CamaraZoom zoom = new CamaraZoom();
        zoom.ajustarPorDelta(0.5); // Zoom 1.5x (centro en 500, 500)
        mouse.setCamaraZoom(zoom);

        // Punto en pantalla: (650, 500) -> Con zoom 1.5x, offset desde centro (150) / 1.5 = 100 -> xMundo = 600
        double mundoX = mouse.transformarX(650);
        double mundoY = mouse.transformarY(500);

        assertEquals(600.0, mundoX, 1e-4, "La coordenada X en mundo debe invertir la escala de zoom");
        assertEquals(500.0, mundoY, 1e-4, "La coordenada Y en mundo centrada no cambia");
    }

    @Test
    @DisplayName("Bug 4: spawnCuerpo devuelve Optional y confirmarColocacionAutoOrbita lo utiliza")
    void testSpawnCuerpoDevuelveOptional() {
        SimulacionSolar sim = new SimulacionSolar(1920, 1080);
        sim.getProgresion().desbloquearTodo();
        sim.getInventario().agregarRecurso(TipoRecurso.MINERALES, 1000);
        sim.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 1000);

        Optional<CuerpoCeleste> opt = sim.spawnCuerpo(TipoCuerpo.PLANETA_ROCOSO, 100, 100, 1.0, Vector2D.cero());
        assertTrue(opt.isPresent(), "spawnCuerpo debe retornar Optional con el cuerpo creado");
        assertTrue(opt.get().getNombre().startsWith("Planeta-"));

        // Colocación asistida
        sim.entrarModoColocacion(TipoCuerpo.LUNA, 1.0, "LunaOrbital");
        sim.actualizarPosicionPreview(150, 100);
        sim.confirmarColocacionAutoOrbita();

        CuerpoCeleste luna = sim.getSistemaSolar().getCuerpos().stream()
                .filter(c -> "LunaOrbital".equals(c.getNombre()))
                .findFirst().orElse(null);
        assertNotNull(luna);
        assertEquals(opt.get(), luna.getCuerpoOrbitado(), "La luna colocada debe orbitar el planeta cercano");
    }

    @Test
    @DisplayName("Bug 5: Recurso.agregar y Recurso.gastar validan cantidades negativas")
    void testRecursoValidaNegativos() {
        Recurso recurso = new Recurso(TipoRecurso.MINERALES, 100.0);

        assertThrows(IllegalArgumentException.class, () -> recurso.agregar(-20.0),
                "agregar() con valor negativo debe lanzar IllegalArgumentException");
        assertThrows(IllegalArgumentException.class, () -> recurso.gastar(-50.0),
                "gastar() con valor negativo debe lanzar IllegalArgumentException");
        assertEquals(100.0, recurso.getCantidad(), "La cantidad no debe haber variado tras intentos inválidos");
    }

    @Test
    @DisplayName("Bug 6: ResolvedorColisiones y MotorFisica usan Set para optimizar aEliminar")
    void testMotorFisicaPermisivaUsaSetParaEliminar() {
        MotorFisicaPermisiva motor = new MotorFisicaPermisiva(1000);
        assertNotNull(motor);
        // Verificar que motor avance sin excepciones usando el Set aEliminar
        Planeta p1 = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        Planeta p2 = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        List<CuerpoCeleste> cuerpos = new java.util.ArrayList<>(List.of(p1, p2));
        assertDoesNotThrow(() -> motor.avanzarPaso(cuerpos));
    }

    @Test
    @DisplayName("Bug 7: ServicioReembolso gestiona reciclaje sin reimplementar lógica en tests")
    void testServicioReembolsoEnSimulacion() {
        SimulacionSolar sim = new SimulacionSolar(1920, 1080);
        sim.getProgresion().desbloquearTodo();
        Planeta planeta = CuerpoCelesteFactory.crearPlanetaRocoso(100, 100, 1.0, Vector2D.cero());
        sim.agregarCuerpo(planeta);

        double minAntes = sim.getInventario().getRecurso(TipoRecurso.MINERALES);
        boolean desmantelado = ServicioReembolso.desmantelarCuerpo(sim, planeta);

        assertTrue(desmantelado);
        assertFalse(sim.getSistemaSolar().getCuerpos().contains(planeta));
        assertEquals(
                minAntes + org.example.game.simulacion.ConfiguracionSimulacion.COSTO_PLANETA_MINERALES * ServicioReembolso.FACTOR_REEMBOLSO,
                sim.getInventario().getRecurso(TipoRecurso.MINERALES),
                1e-6
        );
    }
}
