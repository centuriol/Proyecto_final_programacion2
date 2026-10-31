package org.example;
import javafx.scene.canvas.Canvas;
import org.example.game.controlador.BucleJuego;
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.guardado.*;
import org.example.game.render.CamaraZoom;
import org.example.game.render.RenderizadorPixelArt;
import org.example.game.simulacion.SimulacionSolar;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
class RanurasYAutoguardadoTest {

    @TempDir
    Path tempDir;

    @Test
    void testTresRanurasInicialmenteVaciasYLlenadoProgresivo() {
        GestorGuardadoPartida gestor = crearGestorConRelojControlado("progresivo", new AtomicLong(1000L));
        SimulacionSolar sim = new SimulacionSolar(1280, 720);

        List<InfoRanuraGuardado> iniciales = gestor.obtenerRanuras();
        assertEquals(3, iniciales.size());
        assertTrue(iniciales.stream().noneMatch(InfoRanuraGuardado::estaOcupada));

        sim.getReloj().restaurar(10L, false, 1.0);
        gestor.guardarPartidaRapida(sim, null);
        sim.getReloj().restaurar(20L, false, 1.0);
        gestor.guardarPartidaRapida(sim, null);
        sim.getReloj().restaurar(30L, false, 1.0);
        gestor.guardarPartidaRapida(sim, null);

        List<InfoRanuraGuardado> ocupadas = gestor.obtenerRanuras();
        assertTrue(ocupadas.stream().allMatch(InfoRanuraGuardado::estaOcupada));
        assertEquals(10L, ocupadas.get(0).datos().reloj().tickActual());
        assertEquals(20L, ocupadas.get(1).datos().reloj().tickActual());
        assertEquals(30L, ocupadas.get(2).datos().reloj().tickActual());
    }

    @Test
    void testCuandoTresRanurasEstanOcupadasReemplazaLaMasAntigua() {
        AtomicLong reloj = new AtomicLong(10_000L);
        GestorGuardadoPartida gestor = crearGestorConRelojControlado("rotacion", reloj);
        SimulacionSolar sim = new SimulacionSolar(1280, 720);

        // Guardados 1, 2 y 3 llenan Ranura 1 (t=10000), Ranura 2 (t=20000), Ranura 3 (t=30000)
        for (int i = 1; i <= 3; i++) {
            reloj.set(i * 10_000L);
            sim.getReloj().restaurar(i * 100L, false, 1.0);
            gestor.guardarPartidaRapida(sim, null);
        }

        // Guardado 4 (t=40000, tick=400) debe reemplazar Ranura 1 (la mas antigua, t=10000)
        reloj.set(40_000L);
        sim.getReloj().restaurar(400L, false, 1.0);
        gestor.guardarPartidaRapida(sim, null);

        List<InfoRanuraGuardado> trasCuarto = gestor.obtenerRanuras();
        assertEquals(400L, trasCuarto.get(0).datos().reloj().tickActual(), "Ranura 1 era la mas antigua y debe reemplazarse");
        assertEquals(200L, trasCuarto.get(1).datos().reloj().tickActual());
        assertEquals(300L, trasCuarto.get(2).datos().reloj().tickActual());

        // Guardado 5 (t=50000, tick=500) debe reemplazar Ranura 2 (ahora la mas antigua, t=20000)
        reloj.set(50_000L);
        sim.getReloj().restaurar(500L, false, 1.0);
        gestor.guardarPartidaRapida(sim, null);

        List<InfoRanuraGuardado> trasQuinto = gestor.obtenerRanuras();
        assertEquals(400L, trasQuinto.get(0).datos().reloj().tickActual());
        assertEquals(500L, trasQuinto.get(1).datos().reloj().tickActual(), "Ranura 2 era la mas antigua y debe reemplazarse");
        assertEquals(300L, trasQuinto.get(2).datos().reloj().tickActual());
    }

    @Test
    void testCargarUltimoGuardadoRestauraElMasRecienteYPosicionCamara() {
        AtomicLong reloj = new AtomicLong(5_000L);
        GestorGuardadoPartida gestor = crearGestorConRelojControlado("ultimo", reloj);
        SimulacionSolar sim = new SimulacionSolar(1280, 720);
        CamaraZoom camara = new CamaraZoom();

        for (int i = 1; i <= 4; i++) {
            reloj.set(i * 5_000L);
            sim.getReloj().restaurar(i * 50L, false, 1.0);
            camara.setNivelZoom(1.2);
            camara.getDesplazamiento().setPosicion(i * 150.0, -i * 80.0);
            gestor.guardarPartidaRapida(sim, camara);
        }

        SimulacionSolar destino = new SimulacionSolar(1280, 720);
        CamaraZoom camaraDestino = new CamaraZoom();
        assertTrue(gestor.cargarUltimoGuardado(destino, camaraDestino));

        assertEquals(200L, destino.getTickActual(), "Debe cargar el 4to guardado (el mas reciente, alojado en Ranura 1)");
        assertEquals(1.2, camaraDestino.getNivelZoom(), 1e-6);
        assertEquals(600.0, camaraDestino.getDesplazamiento().getX(), 1e-6);
        assertEquals(-320.0, camaraDestino.getDesplazamiento().getY(), 1e-6);
    }

    @Test
    void testAutoguardadoPeriodicoEscribeSoloEnRanuraSeleccionadaSinRotar() {
        AtomicLong reloj = new AtomicLong(1_000L);
        GestorGuardadoPartida gestor = crearGestorConRelojControlado("auto", reloj);
        SimulacionSolar sim = new SimulacionSolar(1280, 720);
        sim.cargarPresetSistemaBasico();
        CamaraZoom camara = new CamaraZoom();

        gestor.setRanuraSeleccionada(2);
        TemporizadorAutoguardado auto = new TemporizadorAutoguardado(gestor, sim, camara, 10.0);
        auto.activar();

        assertFalse(auto.actualizar(4.0), "Antes del intervalo no debe guardar");

        for (int ciclo = 1; ciclo <= 4; ciclo++) {
            reloj.set(ciclo * 10_000L);
            sim.getReloj().restaurar(ciclo * 25L, false, 1.0);
            assertTrue(auto.actualizar(10.0), "Al cumplir el intervalo debe autoguardar");
        }

        List<InfoRanuraGuardado> ranuras = gestor.obtenerRanuras();
        assertFalse(ranuras.get(0).estaOcupada(), "Ranura 1 no debe ser tocada por el autoguardado");
        assertTrue(ranuras.get(1).estaOcupada(), "Ranura 2 seleccionada debe recibir el autoguardado");
        assertEquals(100L, ranuras.get(1).datos().reloj().tickActual(), "Ranura 2 debe tener el ultimo autoguardado");
        assertFalse(ranuras.get(2).estaOcupada(), "Ranura 3 no debe ser tocada por el autoguardado");
    }

    @Test
    void testAutoguardadoNoEjecutaEnMenuEnPausaNiEnDerrota() {
        GestorGuardadoPartida gestor = crearGestorConRelojControlado("guardas", new AtomicLong(1000L));
        SimulacionSolar sim = new SimulacionSolar(1280, 720);
        sim.cargarPresetSistemaBasico();
        TemporizadorAutoguardado auto = new TemporizadorAutoguardado(gestor, sim, null, 5.0);

        // Inactivo (como en el menu principal)
        assertFalse(auto.actualizar(10.0));

        // En pausa
        auto.activar();
        sim.pause();
        assertFalse(auto.actualizar(10.0));

        // En derrota (agujero negro sin planetas ni lunas)
        sim.play();
        sim.limpiarCuerpos();
        sim.agregarCuerpo(new CuerpoCelesteFactory().crearAgujeroNegroEstelar(0, 0, 1.0));
        assertTrue(sim.estaPerdida());
        assertFalse(auto.actualizar(10.0));
        assertTrue(gestor.obtenerRanuras().stream().noneMatch(InfoRanuraGuardado::estaOcupada));
    }

    @Test
    void testCargarRanuraEspecificaEIndiceInvalido() {
        GestorGuardadoPartida gestor = crearGestorConRelojControlado("especifica", new AtomicLong(2000L));
        SimulacionSolar sim = new SimulacionSolar(1280, 720);
        sim.getReloj().restaurar(77L, false, 2.0);
        gestor.guardarPartidaRapida(sim, null);
        sim.getReloj().restaurar(99L, false, 3.0);
        gestor.guardarPartidaRapida(sim, null);

        SimulacionSolar destino = new SimulacionSolar(1280, 720);
        assertTrue(gestor.cargarRanura(1, destino, null));
        assertEquals(77L, destino.getTickActual());

        assertThrows(ExcepcionPersistenciaPartida.class, () -> gestor.cargarRanura(0, destino, null));
        assertThrows(ExcepcionPersistenciaPartida.class, () -> gestor.cargarRanura(4, destino, null));
    }

    @Test
    void testBucleJuegoIntegraTemporizadorAutoguardadoPorFrame() {
        GestorGuardadoPartida gestor = crearGestorConRelojControlado("bucle", new AtomicLong(3000L));
        SimulacionSolar sim = new SimulacionSolar(800, 600);
        sim.cargarPresetSistemaBasico();
        RenderizadorPixelArt renderizador = new RenderizadorPixelArt(800, 600);
        Canvas canvas = new Canvas(800, 600);

        TemporizadorAutoguardado auto = new TemporizadorAutoguardado(gestor, sim, renderizador.getCamaraZoom(), 0.5);
        auto.activar();

        BucleJuego bucle = new BucleJuego(sim, renderizador, canvas, null, auto);
        long t0 = 1_000_000_000L;
        bucle.handle(t0);
        bucle.handle(t0 + 600_000_000L); // +0.6s >= 0.5s

        assertTrue(gestor.obtenerRanuras().get(0).estaOcupada());
    }

    private GestorGuardadoPartida crearGestorConRelojControlado(String subcarpeta, AtomicLong reloj) {
        RepositorioPartidas repo = new RepositorioPartidasArchivo(tempDir.resolve(subcarpeta));
        PoliticaRanurasGuardado politica = new PoliticaRanurasGuardado(reloj::get);
        return new GestorGuardadoPartida(repo, new MapeadorEstadoSimulacion(), politica);
    }
}
