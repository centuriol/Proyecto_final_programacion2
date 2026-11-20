package org.example;

import org.example.game.controlador.comando.ComandoCargarPartida;
import org.example.game.controlador.comando.ComandoGuardarPartida;
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.cuerpo.Estrella;
import org.example.game.guardado.*;
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
class GuardadoComandosYFlujoTest {

    @TempDir
    Path tempDir;

    @Test
    void testGestorGuardadoConRepositorioEnMemoriaCumpleDIP() {
        RepositorioPartidas repoMemoria = new RepositorioMemoriaTest();
        GestorGuardadoPartida gestor = new GestorGuardadoPartida(repoMemoria);

        SimulacionSolar sim = new SimulacionSolar(1280, 720);
        sim.getPresetsSistema().cargarPresetSistemaBasico(sim);
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

        sim.getPresetsSistema().cargarPresetEstrellaBinaria(sim);
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
        sim.getPresetsSistema().cargarPresetSistemaBasico(sim);

        List<MensajeEvento> eventos = new ArrayList<>();
        sim.getBusEventos().agregarListenerEvento(eventos::add);

        assertFalse(gestor.cargarPartida("inexistente", sim, null));
        assertEquals(3, sim.getSistemaSolar().getCuerpos().size());
        assertFalse(eventos.isEmpty());
        assertEquals(MensajeEvento.TipoMensaje.ADVERTENCIA, eventos.get(eventos.size() - 1).tipo());
    }

    @Test
    void testContadorFabricaSeSincronizaTrasCargarNombresNumerados() {
        new CuerpoCelesteFactory().resetearContador();
        SimulacionSolar sim = new SimulacionSolar(1280, 720);
        sim.agregarCuerpo(new CuerpoCelesteFactory().crearEstrellaPersonalizada("Sol-42", 1.989e30, 0, 0));

        GestorGuardadoPartida gestor = new GestorGuardadoPartida(new RepositorioPartidasArchivo(tempDir.resolve("ids")));
        gestor.guardarPartidaRapida(sim, null);

        new CuerpoCelesteFactory().resetearContador();
        SimulacionSolar destino = new SimulacionSolar(1280, 720);
        gestor.cargarPartidaRapida(destino, null);

        Estrella nueva = new CuerpoCelesteFactory().crearEstrella(100, 100, 1.0);
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
        sim.getBusEventos().agregarListenerEvento(eventos::add);

        gestor.guardarPartidaRapida(sim, null);
        gestor.cargarPartidaRapida(sim, null);

        for (MensajeEvento ev : eventos) {
            assertFalse(ev.mensaje().matches(".*[\\p{So}\\p{Cn}].*"));
        }
    }

    private static class RepositorioMemoriaTest implements RepositorioPartidas {
        private final Map<String, DatosPartida> almacenamiento = new LinkedHashMap<>();

        @Override public Path guardar(String nombreSlot, DatosPartida datos) { almacenamiento.put(nombreSlot, datos); return Path.of("memoria", nombreSlot); }
        @Override public Optional<DatosPartida> cargar(String nombreSlot) { return Optional.ofNullable(almacenamiento.get(nombreSlot)); }
        @Override public List<String> listarPartidas() { return List.copyOf(almacenamiento.keySet()); }
        @Override public boolean existe(String nombreSlot) { return almacenamiento.containsKey(nombreSlot); }
        @Override public boolean eliminar(String nombreSlot) { return almacenamiento.remove(nombreSlot) != null; }
    }
}
