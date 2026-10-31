package org.example;
import org.example.game.colonizacion.NaveColonizadora;
import org.example.game.controlador.comando.ComandoCargarPartida;
import org.example.game.controlador.comando.ComandoGuardarPartida;
import org.example.game.cuerpo.*;
import org.example.game.guardado.*;
import org.example.game.jugador.TipoRecurso;
import org.example.game.motor.Vector2D;
import org.example.game.progresion.RamaProgresion;
import org.example.game.render.CamaraZoom;
import org.example.game.simulacion.MensajeEvento;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.BarraGuardadoWidget;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
class GuardadoYCargaSimulacionTest {

    @TempDir
    Path tempDir;

    @Test
    void testGuardarYCargarRestauraSistemaSolarCompletoYRelacionesOrbitales() {
        SimulacionSolar simOrigen = new SimulacionSolar(1920, 1080);
        CamaraZoom zoomOrigen = new CamaraZoom();
        zoomOrigen.setNivelZoom(1.4);

        simOrigen.getInventario().getRecursoObj(TipoRecurso.CIENCIA).setCantidad(20000);
        simOrigen.getProgresion().investigar(RamaProgresion.PLANETAS, simOrigen.getInventario());
        simOrigen.getProgresion().investigar(RamaProgresion.PLANETAS, simOrigen.getInventario());
        simOrigen.getProgresion().investigar(RamaProgresion.PLANETAS, simOrigen.getInventario());
        simOrigen.getProgresion().investigar(RamaProgresion.PLANETAS, simOrigen.getInventario());
        simOrigen.getProgresion().investigar(RamaProgresion.MASA, simOrigen.getInventario());

        Estrella sol = CuerpoCelesteFactory.crearEstrellaPersonalizada("SolAlpha", 1.989e30, 0, 0);
        Planeta tierra = CuerpoCelesteFactory.crearPlanetaPersonalizado(
                "Gaia", 5.97e24, 220, 0, TipoCuerpo.PLANETA_ROCOSO, new Vector2D(0, -11.0));
        tierra.setCuerpoOrbitado(sol);
        tierra.setRadioOrbita(220.0);
        tierra.activarEscudo(4);
        tierra.getCivilizacion().restaurarMetricas(9000.0, 2, 0.85, 0.90);

        Planeta oceano = CuerpoCelesteFactory.crearPlanetaPersonalizado(
                "Poseidon", 5.97e24, 340, 0, TipoCuerpo.PLANETA_AGUA, new Vector2D(0, -8.5));
        oceano.setCuerpoOrbitado(sol);
        oceano.setRadioOrbita(340.0);

        Luna luna = CuerpoCelesteFactory.crearLunaPersonalizada("Selene", 7.34e22, 260, 0, new Vector2D(0, -12.2));
        luna.setCuerpoOrbitado(tierra);
        luna.setRadioOrbita(40.0);

        Satelite satelite = CuerpoCelesteFactory.crearSatelite(240, 0, 1.0, new Vector2D(0, -13.0));
        satelite.setNombre("Hubble");
        satelite.setCuerpoOrbitado(tierra);
        satelite.producirRecursos(10.0);

        Meteorito meteorito = new Meteorito("Apophis", 1e20, -300, 150, new Vector2D(3.5, -1.5), 720.0, false, tierra);

        simOrigen.agregarCuerpo(sol);
        simOrigen.agregarCuerpo(tierra);
        simOrigen.agregarCuerpo(oceano);
        simOrigen.agregarCuerpo(luna);
        simOrigen.agregarCuerpo(satelite);
        simOrigen.agregarCuerpo(meteorito);
        simOrigen.getGestorColonizacion().restaurarNave(new NaveColonizadora(new Vector2D(260, 10), oceano, 900.0));

        simOrigen.getInventario().getRecursoObj(TipoRecurso.MINERALES).setCantidad(777.5);
        simOrigen.getInventario().getRecursoObj(TipoRecurso.ENERGIA).setCantidad(444.25);
        simOrigen.getReloj().restaurar(512L, true, 3.0);
        simOrigen.getGeneradorEventosCosmicos().setTicksHastaSiguiente(1420);

        GestorGuardadoPartida gestor = new GestorGuardadoPartida(new RepositorioPartidasArchivo(tempDir.resolve("saves")));
        gestor.guardarPartida("sistema_completo", simOrigen, zoomOrigen);

        SimulacionSolar simDestino = new SimulacionSolar(1920, 1080);
        CamaraZoom zoomDestino = new CamaraZoom();
        assertTrue(gestor.cargarPartida("sistema_completo", simDestino, zoomDestino));

        assertEquals(512L, simDestino.getTickActual());
        assertTrue(simDestino.isEnPausa());
        assertEquals(3.0, simDestino.getVelocidadSimulacion(), 1e-6);
        assertEquals(1.4, zoomDestino.getNivelZoom(), 1e-6);
        assertEquals(1420, simDestino.getGeneradorEventosCosmicos().getTicksHastaSiguiente());
        assertEquals(777.5, simDestino.getInventario().getRecurso(TipoRecurso.MINERALES), 1e-6);
        assertEquals(444.25, simDestino.getInventario().getRecurso(TipoRecurso.ENERGIA), 1e-6);
        assertEquals(4, simDestino.getProgresion().getNivel(RamaProgresion.PLANETAS));
        assertEquals(1, simDestino.getProgresion().getNivel(RamaProgresion.MASA));

        List<CuerpoCeleste> cargados = simDestino.getSistemaSolar().getCuerpos();
        assertEquals(6, cargados.size());

        CuerpoCeleste solCargado = cargados.get(0);
        Planeta gaiaCargada = (Planeta) cargados.get(1);
        Planeta poseidonCargado = (Planeta) cargados.get(2);
        Luna seleneCargada = (Luna) cargados.get(3);
        Satelite hubbleCargado = (Satelite) cargados.get(4);
        Meteorito apophisCargado = (Meteorito) cargados.get(5);

        assertSame(solCargado, gaiaCargada.getCuerpoOrbitado());
        assertEquals(4, gaiaCargada.getGolpesEscudoRestantes());
        assertTrue(gaiaCargada.tieneCivilizacion());
        assertEquals(9000.0, gaiaCargada.getCivilizacion().getPoblacion(), 1e-4);
        assertEquals(2, gaiaCargada.getCivilizacion().getNivelTecnologico());

        assertFalse(poseidonCargado.tieneCivilizacion());
        assertNull(poseidonCargado.getCivilizacion());
        assertSame(gaiaCargada, seleneCargada.getCuerpoOrbitado());
        assertEquals(satelite.getCienciaGenerada(), hubbleCargado.getCienciaGenerada(), 1e-6);
        assertSame(gaiaCargada, apophisCargado.getObjetivo());

        assertEquals(1, simDestino.getGestorColonizacion().getNaves().size());
        assertSame(poseidonCargado, simDestino.getGestorColonizacion().getNaves().get(0).getDestino());
    }

    @Test
    void testPlanetaInerteTransformadoPreservaNoReembolsableYSinCivilizacion() {
        SimulacionSolar sim = new SimulacionSolar(1280, 720);
        Planeta inerte = CuerpoCelesteFactory.crearPlanetaRocoso(150, 0, 1.0, Vector2D.cero());
        inerte.transformarA(TipoCuerpo.PLANETA_ROCOSO);
        sim.agregarCuerpo(inerte);

        GestorGuardadoPartida gestor = new GestorGuardadoPartida(new RepositorioPartidasArchivo(tempDir.resolve("inerte")));
        gestor.guardarPartidaRapida(sim, null);

        SimulacionSolar cargada = new SimulacionSolar(1280, 720);
        assertTrue(gestor.cargarPartidaRapida(cargada, null));

        CuerpoCeleste recuperado = cargada.getSistemaSolar().getCuerpos().get(0);
        assertFalse(recuperado.permiteReembolso());
        assertFalse(((Planeta) recuperado).tieneCivilizacion());
    }

    @Test
    void testGestorGuardadoConRepositorioEnMemoriaCumpleDIP() {
        RepositorioPartidas repoMemoria = new RepositorioMemoriaTest();
        GestorGuardadoPartida gestor = new GestorGuardadoPartida(repoMemoria);

        SimulacionSolar sim = new SimulacionSolar(1280, 720);
        sim.cargarPresetSistemaBasico();
        gestor.guardarPartida("memoria_slot", sim, null);

        assertTrue(repoMemoria.existe("memoria_slot"));

        SimulacionSolar destino = new SimulacionSolar(1280, 720);
        assertTrue(gestor.cargarPartida("memoria_slot", destino, null));
        assertEquals(3, destino.getSistemaSolar().getCuerpos().size());
    }

    @Test
    void testComandosGuardarYCargarYBarraGuardadoWidget() {
        GestorGuardadoPartida gestor = new GestorGuardadoPartida(new RepositorioPartidasArchivo(tempDir.resolve("ui_saves")));
        SimulacionSolar sim = new SimulacionSolar(1280, 720);
        CamaraZoom zoom = new CamaraZoom();
        AtomicBoolean postCargaInvocado = new AtomicBoolean(false);
        gestor.agregarListenerPostCarga(() -> postCargaInvocado.set(true));

        ComandoGuardarPartida cmdGuardar = new ComandoGuardarPartida(gestor, sim, zoom);
        ComandoCargarPartida cmdCargar = new ComandoCargarPartida(gestor, sim, zoom);
        BarraGuardadoWidget barra = new BarraGuardadoWidget(cmdGuardar, cmdCargar);

        sim.cargarPresetEstrellaBinaria();
        zoom.setNivelZoom(0.7);
        barra.getBtnGuardar().fire();

        sim.limpiarCuerpos();
        zoom.resetear();
        assertEquals(0, sim.getSistemaSolar().getCuerpos().size());

        barra.getBtnCargar().fire();
        assertTrue(postCargaInvocado.get());
        assertEquals(3, sim.getSistemaSolar().getCuerpos().size());
        assertEquals(0.7, zoom.getNivelZoom(), 1e-6);
    }

    @Test
    void testCargarPartidaInexistenteNotificaAdvertenciaSinBorrarEstadoActual() {
        GestorGuardadoPartida gestor = new GestorGuardadoPartida(new RepositorioPartidasArchivo(tempDir.resolve("vacio")));
        SimulacionSolar sim = new SimulacionSolar(1280, 720);
        sim.cargarPresetSistemaBasico();

        List<MensajeEvento> eventos = new ArrayList<>();
        sim.agregarListenerEvento(eventos::add);

        boolean exito = gestor.cargarPartida("inexistente", sim, null);
        assertFalse(exito);
        assertEquals(3, sim.getSistemaSolar().getCuerpos().size());
        assertFalse(eventos.isEmpty());
        assertEquals(MensajeEvento.TipoMensaje.ADVERTENCIA, eventos.get(eventos.size() - 1).tipo());
    }

    @Test
    void testContadorFabricaSeSincronizaTrasCargarNombresNumerados() {
        CuerpoCelesteFactory.resetearContador();
        SimulacionSolar sim = new SimulacionSolar(1280, 720);
        sim.agregarCuerpo(CuerpoCelesteFactory.crearEstrellaPersonalizada("Sol-42", 1.989e30, 0, 0));

        GestorGuardadoPartida gestor = new GestorGuardadoPartida(new RepositorioPartidasArchivo(tempDir.resolve("ids")));
        gestor.guardarPartidaRapida(sim, null);

        CuerpoCelesteFactory.resetearContador();
        SimulacionSolar destino = new SimulacionSolar(1280, 720);
        gestor.cargarPartidaRapida(destino, null);

        Estrella nueva = CuerpoCelesteFactory.crearEstrella(100, 100, 1.0);
        assertEquals("Sol-43", nueva.getNombre());
    }

    @Test
    void testTextosYNotificacionesGuardadoSinEmojis() {
        BarraGuardadoWidget barra = new BarraGuardadoWidget(() -> {}, () -> {});
        assertFalse(barra.getBtnGuardar().getText().matches(".*[\\p{So}\\p{Cn}].*"));
        assertFalse(barra.getBtnCargar().getText().matches(".*[\\p{So}\\p{Cn}].*"));

        GestorGuardadoPartida gestor = new GestorGuardadoPartida(new RepositorioMemoriaTest());
        SimulacionSolar sim = new SimulacionSolar(1280, 720);
        List<MensajeEvento> eventos = new ArrayList<>();
        sim.agregarListenerEvento(eventos::add);

        gestor.guardarPartidaRapida(sim, null);
        gestor.cargarPartidaRapida(sim, null);

        for (MensajeEvento ev : eventos) {
            assertFalse(ev.mensaje().matches(".*[\\p{So}\\p{Cn}].*"));
        }
    }

    private static class RepositorioMemoriaTest implements RepositorioPartidas {
        private final Map<String, DatosPartida> almacenamiento = new LinkedHashMap<>();

        @Override
        public Path guardar(String nombreSlot, DatosPartida datos) {
            almacenamiento.put(nombreSlot, datos);
            return Path.of("memoria", nombreSlot);
        }

        @Override
        public Optional<DatosPartida> cargar(String nombreSlot) {
            return Optional.ofNullable(almacenamiento.get(nombreSlot));
        }

        @Override
        public List<String> listarPartidas() {
            return List.copyOf(almacenamiento.keySet());
        }

        @Override
        public boolean existe(String nombreSlot) {
            return almacenamiento.containsKey(nombreSlot);
        }

        @Override
        public boolean eliminar(String nombreSlot) {
            return almacenamiento.remove(nombreSlot) != null;
        }
    }
}
