package org.example;
import org.example.game.cuerpo.*;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;
import org.example.game.motor.Vector2D;
import org.example.game.progresion.ElementoDesbloqueable;
import org.example.game.progresion.RamaProgresion;
import org.example.game.progresion.SistemaProgresion;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.SimulacionSolar;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
class ProgresionYArbolTest {

    private SimulacionSolar simulacion;

    @BeforeEach
    void setUp() {
        simulacion = new SimulacionSolar(1920, 1080);
        simulacion.limpiarCuerpos();
    }

    @Test
    void testArbolTecnologicoTresRamasYCostosEscalonados() {
        SistemaProgresion prog = simulacion.getProgresion();
        InventarioJugador inv = simulacion.getInventario();

        assertEquals(0, prog.getNivel(RamaProgresion.PLANETAS));
        assertEquals(0, prog.getNivel(RamaProgresion.MASA));
        assertEquals(0, prog.getNivel(RamaProgresion.CANTIDAD_PLANETAS));

        assertEquals(0.3, prog.getLimiteMasaActual(), 1e-6);
        assertEquals(3, prog.getLimitePlanetas());

        assertTrue(prog.estaDesbloqueado(TipoCuerpo.ESTRELLA));
        assertTrue(prog.estaDesbloqueado(TipoCuerpo.PLANETA_AGUA));
        assertFalse(prog.estaDesbloqueado(TipoCuerpo.LUNA));
        assertFalse(prog.estaDesbloqueado(TipoCuerpo.PLANETA_ROCOSO));
        assertFalse(prog.estaDesbloqueado(TipoCuerpo.PLANETA_LAVA));
        assertFalse(prog.tieneEscudoDesbloqueado());

        assertEquals(ConfiguracionSimulacion.COSTO_DESBLOQUEO_LUNA, prog.getCostoSiguiente(RamaProgresion.PLANETAS));
        assertEquals(ConfiguracionSimulacion.COSTO_DESBLOQUEO_LUNA, prog.getCostoSiguiente(RamaProgresion.MASA));
        assertEquals(ConfiguracionSimulacion.COSTO_DESBLOQUEO_LUNA, prog.getCostoSiguiente(RamaProgresion.CANTIDAD_PLANETAS));

        inv.agregarRecurso(TipoRecurso.CIENCIA, ConfiguracionSimulacion.COSTO_DESBLOQUEO_LUNA);
        assertTrue(prog.investigar(RamaProgresion.MASA, inv));
        assertEquals(1, prog.getNivel(RamaProgresion.MASA));
        assertEquals(0.6, prog.getLimiteMasaActual(), 1e-6);
        assertEquals(ConfiguracionSimulacion.COSTO_DESBLOQUEO_PORRO, prog.getCostoSiguiente(RamaProgresion.MASA));

        inv.agregarRecurso(TipoRecurso.CIENCIA, ConfiguracionSimulacion.COSTO_DESBLOQUEO_LUNA);
        assertTrue(prog.investigar(RamaProgresion.CANTIDAD_PLANETAS, inv));
        assertEquals(1, prog.getNivel(RamaProgresion.CANTIDAD_PLANETAS));
        assertEquals(4, prog.getLimitePlanetas());

        inv.agregarRecurso(TipoRecurso.CIENCIA, ConfiguracionSimulacion.COSTO_DESBLOQUEO_LUNA);
        assertTrue(prog.investigar(RamaProgresion.PLANETAS, inv));
        assertTrue(prog.estaDesbloqueado(TipoCuerpo.LUNA));

        inv.agregarRecurso(TipoRecurso.CIENCIA, ConfiguracionSimulacion.COSTO_DESBLOQUEO_PORRO);
        assertTrue(prog.investigar(RamaProgresion.PLANETAS, inv));
        assertTrue(prog.estaDesbloqueado(TipoCuerpo.PLANETA_ROCOSO));

        inv.agregarRecurso(TipoRecurso.CIENCIA, ConfiguracionSimulacion.COSTO_DESBLOQUEO_SATELITE);
        assertTrue(prog.investigar(RamaProgresion.PLANETAS, inv));
        assertTrue(prog.estaDesbloqueado(TipoCuerpo.SATELITE));

        inv.agregarRecurso(TipoRecurso.CIENCIA, ConfiguracionSimulacion.COSTO_DESBLOQUEO_ESCUDO);
        assertTrue(prog.investigar(RamaProgresion.PLANETAS, inv));
        assertTrue(prog.tieneEscudoDesbloqueado());

        inv.agregarRecurso(TipoRecurso.CIENCIA, ConfiguracionSimulacion.COSTO_DESBLOQUEO_LAVA);
        assertTrue(prog.investigar(RamaProgresion.PLANETAS, inv));
        assertTrue(prog.estaDesbloqueado(TipoCuerpo.PLANETA_LAVA));
        assertTrue(prog.esNivelMaximo(RamaProgresion.PLANETAS));
    }

    @Test
    void testLimiteDePlanetasEnSimulacion() {
        simulacion.getInventario().agregarRecurso(TipoRecurso.MINERALES, 50000);
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 50000);

        assertEquals(3, simulacion.getProgresion().getLimitePlanetas());

        assertTrue(simulacion.getGestorCicloVida().spawnCuerpo(TipoCuerpo.PLANETA_AGUA, 100, 0, 0.3, Vector2D.cero()).isPresent());
        assertTrue(simulacion.getGestorCicloVida().spawnCuerpo(TipoCuerpo.PLANETA_AGUA, 200, 0, 0.3, Vector2D.cero()).isPresent());
        assertTrue(simulacion.getGestorCicloVida().spawnCuerpo(TipoCuerpo.PLANETA_AGUA, 300, 0, 0.3, Vector2D.cero()).isPresent());
        assertEquals(3, simulacion.getGestorCicloVida().getCantidadPlanetasActuales());

        assertFalse(simulacion.getGestorCicloVida().spawnCuerpo(TipoCuerpo.PLANETA_AGUA, 400, 0, 0.3, Vector2D.cero()).isPresent());
        assertEquals(3, simulacion.getGestorCicloVida().getCantidadPlanetasActuales());

        simulacion.getInventario().agregarRecurso(TipoRecurso.CIENCIA, ConfiguracionSimulacion.COSTO_DESBLOQUEO_LUNA);
        assertTrue(simulacion.getProgresion().investigar(RamaProgresion.CANTIDAD_PLANETAS, simulacion.getInventario()));
        assertEquals(4, simulacion.getProgresion().getLimitePlanetas());

        assertTrue(simulacion.getGestorCicloVida().spawnCuerpo(TipoCuerpo.PLANETA_AGUA, 400, 0, 0.3, Vector2D.cero()).isPresent());
        assertEquals(4, simulacion.getGestorCicloVida().getCantidadPlanetasActuales());
    }

    @Test
    void testSpawnCuerpoRechazaBloqueados() {
        simulacion.getInventario().agregarRecurso(TipoRecurso.MINERALES, 5000);
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 5000);

        assertFalse(simulacion.getGestorCicloVida().spawnCuerpo(TipoCuerpo.LUNA, 100, 100, 0.3, Vector2D.cero()).isPresent());
        assertTrue(simulacion.getGestorCicloVida().spawnCuerpo(TipoCuerpo.PLANETA_AGUA, 100, 100, 0.3, Vector2D.cero()).isPresent());
    }

    @Test
    void testSolProduceCienciaInicial() {
        simulacion.getProgresion().desbloquearTodo();
        Estrella sol = new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0);
        simulacion.agregarCuerpo(sol);

        assertEquals(0.0, simulacion.getInventario().getRecurso(TipoRecurso.CIENCIA));
        for (int i = 0; i < 50; i++) {
            simulacion.avanzarTick();
        }
        assertTrue(simulacion.getInventario().getRecurso(TipoRecurso.CIENCIA) > 0.0);
    }

    @Test
    void testGameOverYReinicioCompleto() {
        simulacion.getProgresion().desbloquearTodo();
        AgujeroNegro bh = new CuerpoCelesteFactory().crearAgujeroNegroEstelar(0, 0, 1.0);
        simulacion.agregarCuerpo(bh);

        assertTrue(simulacion.estaPerdida());

        simulacion.reiniciar();
        assertFalse(simulacion.estaPerdida());
        assertTrue(simulacion.getSistemaSolar().getCuerpos().isEmpty());
        InventarioJugador base = new InventarioJugador();
        assertEquals(base.getRecurso(TipoRecurso.MINERALES), simulacion.getInventario().getRecurso(TipoRecurso.MINERALES), 1e-6);
        assertEquals(base.getRecurso(TipoRecurso.ENERGIA), simulacion.getInventario().getRecurso(TipoRecurso.ENERGIA), 1e-6);
        assertEquals(0.0, simulacion.getInventario().getRecurso(TipoRecurso.CIENCIA));
    }

    @Test
    void testItemSateliteDesbloqueoAntesDelEscudoYProduccionFija() {
        SistemaProgresion prog = simulacion.getProgresion();
        InventarioJugador inv = simulacion.getInventario();

        assertFalse(prog.estaDesbloqueado(TipoCuerpo.SATELITE));
        assertFalse(prog.estaDesbloqueado(ElementoDesbloqueable.SATELITE));

        inv.agregarRecurso(TipoRecurso.CIENCIA,
                ConfiguracionSimulacion.COSTO_DESBLOQUEO_LUNA + ConfiguracionSimulacion.COSTO_DESBLOQUEO_PORRO);
        assertTrue(prog.investigar(RamaProgresion.PLANETAS, inv));
        assertTrue(prog.investigar(RamaProgresion.PLANETAS, inv));

        inv.agregarRecurso(TipoRecurso.CIENCIA, ConfiguracionSimulacion.COSTO_DESBLOQUEO_SATELITE);
        assertTrue(prog.investigar(RamaProgresion.PLANETAS, inv));
        assertTrue(prog.estaDesbloqueado(TipoCuerpo.SATELITE));
        assertTrue(prog.estaDesbloqueado(ElementoDesbloqueable.SATELITE));
        assertFalse(prog.tieneEscudoDesbloqueado());

        assertEquals(ConfiguracionSimulacion.PRODUCCION_SATELITE_CIENCIA,
                TipoCuerpo.SATELITE.getProduccionBase().get(TipoRecurso.CIENCIA), 1e-6);
        assertEquals(ConfiguracionSimulacion.PRODUCCION_SATELITE_ENERGIA,
                TipoCuerpo.SATELITE.getProduccionBase().get(TipoRecurso.ENERGIA), 1e-6);
        assertNull(TipoCuerpo.SATELITE.getProduccionBase().get(TipoRecurso.MINERALES));

        assertTrue(ConfiguracionSimulacion.COSTO_SATELITE_MINERALES < ConfiguracionSimulacion.COSTO_PLANETA_MINERALES);
        assertTrue(ConfiguracionSimulacion.COSTO_SATELITE_ENERGIA < ConfiguracionSimulacion.COSTO_ACTIVACION_ESCUDO_ENERGIA);

        inv.agregarRecurso(TipoRecurso.MINERALES, 1000);
        inv.agregarRecurso(TipoRecurso.ENERGIA, 1000);
        assertTrue(simulacion.getGestorCicloVida().spawnCuerpo(TipoCuerpo.SATELITE, "SondaAlpha", 100, 100, 5.0, Vector2D.cero()).isPresent());
        CuerpoCeleste satSpawneado = simulacion.getSistemaSolar().getCuerpos().stream()
                .filter(c -> c.getTipoCuerpo() == TipoCuerpo.SATELITE)
                .findFirst().orElse(null);
        assertNotNull(satSpawneado);
        assertEquals(TipoCuerpo.SATELITE.masaBase, satSpawneado.getMasa(), 1e-6);
    }

    @Test
    void testPlanetaAguaDaMasRecursosAlInicio() {
        var prodAgua = TipoCuerpo.PLANETA_AGUA.getProduccionBase();
        assertTrue(prodAgua.containsKey(TipoRecurso.MINERALES));
        assertTrue(prodAgua.containsKey(TipoRecurso.CIENCIA));
        assertTrue(prodAgua.containsKey(TipoRecurso.ENERGIA));
        assertEquals(ConfiguracionSimulacion.PRODUCCION_PLANETA_AGUA_MINERALES, prodAgua.get(TipoRecurso.MINERALES), 1e-6);
        assertEquals(ConfiguracionSimulacion.PRODUCCION_PLANETA_AGUA_CIENCIA, prodAgua.get(TipoRecurso.CIENCIA), 1e-6);
        assertEquals(ConfiguracionSimulacion.PRODUCCION_PLANETA_AGUA_ENERGIA, prodAgua.get(TipoRecurso.ENERGIA), 1e-6);
    }
}
