package org.example;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.example.game.controlador.ControladorMouse;
import org.example.game.controlador.CoordinadorSesionJuego;
import org.example.game.controlador.SeleccionadorCuerpoMouse;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.cuerpo.Estrella;
import org.example.game.cuerpo.Meteorito;
import org.example.game.cuerpo.Planeta;
import org.example.game.cuerpo.Satelite;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.guardado.GestorGuardadoPartida;
import org.example.game.guardado.InfoRanuraGuardado;
import org.example.game.guardado.MapeadorEstadoSimulacion;
import org.example.game.guardado.PoliticaRanurasGuardado;
import org.example.game.guardado.RepositorioPartidas;
import org.example.game.guardado.RepositorioPartidasArchivo;
import org.example.game.guardado.TemporizadorAutoguardado;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.ServicioReembolso;
import org.example.game.jugador.TipoRecurso;
import org.example.game.motor.ConstantesFisicas;
import org.example.game.motor.MotorFisicaPermisiva;
import org.example.game.motor.Vector2D;
import org.example.game.motor.colision.UtilidadColision;
import org.example.game.progresion.RamaProgresion;
import org.example.game.render.CamaraZoom;
import org.example.game.simulacion.ConfiguracionColisiones;
import org.example.game.simulacion.ConfiguracionEconomia;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.VentanaInspeccion;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
class CorreccionesMeteoritosAutoguardadoEconomiaTest {

    @TempDir
    Path tempDir;

    @Test
    void testMeteoritoAcotaMasaYRadioNuncaSuperaAlSol() {
        Estrella solMinimo = new CuerpoCelesteFactory().crearEstrella(0, 0, ConfiguracionSimulacion.MASA_FACTOR_MIN);
        Meteorito gigante = new Meteorito("AsteroideEnorme", 1.0e35, 0, 0, Vector2D.cero(), 600, false);

        assertEquals(ConfiguracionColisiones.MASA_MAXIMA_METEORITO, gigante.getMasa(), 1e-6);
        assertTrue(gigante.getRadio() >= ConfiguracionColisiones.RADIO_MINIMO_METEORITO);
        assertTrue(gigante.getRadio() <= ConfiguracionColisiones.RADIO_MAXIMO_METEORITO);
        assertTrue(gigante.getRadio() < solMinimo.getRadio(), "Ningun meteorito puede ser mas grande que un sol");

        gigante.setMasa(9.0e40);
        assertEquals(ConfiguracionColisiones.MASA_MAXIMA_METEORITO, gigante.getMasa(), 1e-6);
        assertTrue(gigante.getRadio() <= ConfiguracionColisiones.RADIO_MAXIMO_METEORITO);

        gigante.setMasa(0.0);
        assertEquals(0.0, gigante.getMasa(), 1e-9);
        assertEquals(ConfiguracionColisiones.RADIO_MINIMO_METEORITO, gigante.getRadio(), 1e-6);
    }

    @Test
    void testEscombrosDePlanetasDestruidosRepartenMasaRespetandoTopes() {
        Planeta coloso = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 10.0, new Vector2D(2.0, -1.0));
        Set<CuerpoCeleste> aEliminar = new HashSet<>();
        List<CuerpoCeleste> escombros = new ArrayList<>();

        UtilidadColision utilidadColision = new UtilidadColision();
        utilidadColision.generarEscombros(coloso, aEliminar, escombros);

        assertTrue(aEliminar.contains(coloso));
        assertFalse(escombros.isEmpty());
        assertTrue(escombros.size() <= ConfiguracionColisiones.MAX_FRAGMENTOS_POR_COLAPSO);

        for (CuerpoCeleste frag : escombros) {
            assertInstanceOf(Meteorito.class, frag);
            assertTrue(frag.getMasa() > 0.0);
            assertTrue(frag.getMasa() <= ConfiguracionColisiones.MASA_MAXIMA_METEORITO + 1e-9);
            assertTrue(frag.getRadio() <= ConfiguracionColisiones.RADIO_MAXIMO_METEORITO + 1e-9);
        }

        // Cuerpo menor cuya masa neta cabe repartida exactamente sin alcanzar el maximo de fragmentos
        Planeta asteroideMenor = new Planeta(
                "ProtoPlaneta",
                ConfiguracionColisiones.MASA_MAXIMA_METEORITO * 2.0,
                50,
                50,
                TipoCuerpo.PLANETA_ROCOSO
        );
        Set<CuerpoCeleste> elimMenor = new HashSet<>();
        List<CuerpoCeleste> fragMenor = new ArrayList<>();
        double masaNetaEsperada = asteroideMenor.getMasa() * (1.0 - ConfiguracionColisiones.FRACCION_MASA_DISIPADA);

        utilidadColision.generarEscombros(asteroideMenor, elimMenor, fragMenor);

        double sumaMasas = fragMenor.stream().mapToDouble(CuerpoCeleste::getMasa).sum();
        assertEquals(masaNetaEsperada, sumaMasas, masaNetaEsperada * 1e-6);
        for (CuerpoCeleste f : fragMenor) {
            assertTrue(f.getMasa() <= ConfiguracionColisiones.MASA_MAXIMA_METEORITO + 1e-9);
        }
    }

    @Test
    void testMeteoritoNoSePuedeSeleccionarInspeccionarNiEliminarConClic() {
        SimulacionSolar sim = new SimulacionSolar(1280, 720);
        sim.limpiarCuerpos();

        Meteorito meteorito = new CuerpoCelesteFactory().crearMeteoritoJugadorFisica(0, 0, Vector2D.cero());
        sim.agregarCuerpo(meteorito);
        assertFalse(meteorito.esInteractuablePorUsuario());
        assertFalse(meteorito.permiteReembolso());

        SeleccionadorCuerpoMouse seleccionador = new SeleccionadorCuerpoMouse();
        org.example.game.motor.ConversorCoordenadas conversor = new org.example.game.motor.TransformadorCoordenadas();
        double centroX = conversor.fisicaAXJavaFX(0, 1280);
        double centroY = conversor.fisicaAYJavaFX(0, 720);
        assertNull(seleccionador.encontrarCuerpo(centroX, centroY, 1280, 720, sim.getSistemaSolar().getCuerpos()));

        AtomicBoolean inspeccionDisparada = new AtomicBoolean(false);
        ControladorMouse mouse = new ControladorMouse(sim, null, c -> inspeccionDisparada.set(true), 1280, 720);

        MouseEvent pressIzq = crearEventoMouse(MouseEvent.MOUSE_PRESSED, centroX, centroY, MouseButton.PRIMARY);
        MouseEvent releaseIzq = crearEventoMouse(MouseEvent.MOUSE_RELEASED, centroX, centroY, MouseButton.PRIMARY);
        MouseEvent pressDer = crearEventoMouse(MouseEvent.MOUSE_PRESSED, centroX, centroY, MouseButton.SECONDARY);

        mouse.manejarMousePresionado(pressIzq);
        mouse.manejarMouseLiberado(releaseIzq);
        mouse.manejarMousePresionado(pressDer);

        assertNull(mouse.getCuerpoSeleccionado());
        assertFalse(inspeccionDisparada.get(), "Hacer clic sobre un meteorito no debe abrir inspeccion");

        mouse.setCuerpoSeleccionado(meteorito);
        assertNull(mouse.getCuerpoSeleccionado());

        assertDoesNotThrow(() -> VentanaInspeccion.mostrar(meteorito, sim, null, mouse));
        assertFalse(sim.desmantelarCuerpo(meteorito), "No se puede eliminar un meteorito manualmente");
        assertTrue(sim.getSistemaSolar().getCuerpos().contains(meteorito));
    }

    @Test
    void testSateliteInterceptaYDetieneMeteoritoSinDestruirseNiPerderOrbita() {
        MotorFisicaPermisiva motor = new MotorFisicaPermisiva(2000);
        Planeta planeta = new CuerpoCelesteFactory().crearPlanetaAgua(0, 0, 0.3, Vector2D.cero());
        Satelite satelite = new CuerpoCelesteFactory().crearSatelite(28, 0, 1.0, new Vector2D(0, 1.5));
        satelite.setCuerpoOrbitado(planeta);

        // Meteorito acercandose al planeta dentro del rango defensivo del satelite
        Meteorito amenaza = new Meteorito("Amenaza-1", ConfiguracionColisiones.MASA_MAXIMA_METEORITO, -18, 0, new Vector2D(3.0, 0), 500, false, planeta);

        List<CuerpoCeleste> cuerpos = new ArrayList<>(List.of(planeta, amenaza, satelite));
        double velInicialSat = satelite.getVelocidad().magnitud();

        motor.avanzarPaso(cuerpos);

        assertFalse(cuerpos.contains(amenaza), "El satelite debe interceptar y eliminar al meteorito");
        assertTrue(cuerpos.contains(planeta), "El planeta protegido debe sobrevivir intacto");
        assertTrue(cuerpos.contains(satelite), "El satelite debe sobrevivir tras interceptar al meteorito");
        assertTrue(satelite.getVelocidad().magnitud() < velInicialSat * 5.0 + 10.0,
                "El satelite no debe salir disparado por transferencia balistica extrema");
    }

    @Test
    void testAutoguardadoAlCargarRanuraEscribeSiempreEnEsaRanuraSinBorrarOtras() {
        AtomicLong reloj = new AtomicLong(1_000L);
        RepositorioPartidas repo = new RepositorioPartidasArchivo(tempDir.resolve("autoguardado_fijo"));
        GestorGuardadoPartida gestor = new GestorGuardadoPartida(repo, new MapeadorEstadoSimulacion(), new PoliticaRanurasGuardado(reloj::get));
        SimulacionSolar sim = new SimulacionSolar(1280, 720);
        CamaraZoom camara = new CamaraZoom();

        // Poblar las tres ranuras con tres partidas distintas (ticks 111, 222, 333)
        long[] ticksIniciales = {111L, 222L, 333L};
        for (int i = 0; i < 3; i++) {
            reloj.set((i + 1) * 5_000L);
            sim.getReloj().restaurar(ticksIniciales[i], false, 1.0);
            gestor.guardarPartidaRapida(sim, camara);
        }

        // Usuario carga explicitamente la Ranura 2 desde el menu
        CoordinadorSesionJuego coordinador = new CoordinadorSesionJuego(sim, camara, gestor, null);
        assertTrue(coordinador.cargarRanura(2));
        assertEquals(2, gestor.getRanuraSeleccionada());

        // Ejecutar 6 ciclos consecutivos de autoguardado
        TemporizadorAutoguardado auto = coordinador.getAutoguardado();
        for (int ciclo = 1; ciclo <= 6; ciclo++) {
            reloj.set(50_000L + ciclo * 10_000L);
            sim.getReloj().restaurar(222L + ciclo * 10L, false, 1.0);
            assertTrue(auto.actualizar(TemporizadorAutoguardado.INTERVALO_DEFECTO_SEGUNDOS));
        }

        List<InfoRanuraGuardado> ranuras = gestor.obtenerRanuras();
        assertEquals(111L, ranuras.get(0).datos().reloj().tickActual(), "Ranura 1 debe permanecer intacta sin borrarse ni rotarse");
        assertEquals(282L, ranuras.get(1).datos().reloj().tickActual(), "Ranura 2 seleccionada debe actualizarse con el ultimo autoguardado");
        assertEquals(333L, ranuras.get(2).datos().reloj().tickActual(), "Ranura 3 debe permanecer intacta sin borrarse ni rotarse");
    }

    @Test
    void testNuevaPartidaFijaUnaSolaRanuraParaTodosSusAutoguardados() {
        AtomicLong reloj = new AtomicLong(2_000L);
        RepositorioPartidas repo = new RepositorioPartidasArchivo(tempDir.resolve("nueva_partida_fija"));
        GestorGuardadoPartida gestor = new GestorGuardadoPartida(repo, new MapeadorEstadoSimulacion(), new PoliticaRanurasGuardado(reloj::get));
        SimulacionSolar sim = new SimulacionSolar(1280, 720);
        CamaraZoom camara = new CamaraZoom();

        // Ranura 1 ya ocupada por una partida previa
        sim.getReloj().restaurar(999L, false, 1.0);
        gestor.guardarPartidaRapida(sim, camara);

        // Iniciar nueva partida selecciona Ranura 2 (primera libre) y se queda fija alli
        CoordinadorSesionJuego coordinador = new CoordinadorSesionJuego(sim, camara, gestor, null);
        coordinador.iniciarNuevaPartida();
        assertEquals(2, gestor.getRanuraSeleccionada());

        for (int i = 1; i <= 5; i++) {
            reloj.set(10_000L + i * 5_000L);
            sim.avanzarTicks(5);
            assertTrue(coordinador.getAutoguardado().actualizar(TemporizadorAutoguardado.INTERVALO_DEFECTO_SEGUNDOS));
        }

        List<InfoRanuraGuardado> ranuras = gestor.obtenerRanuras();
        assertEquals(999L, ranuras.get(0).datos().reloj().tickActual(), "Ranura 1 previa no debe sobrescribirse");
        assertTrue(ranuras.get(1).estaOcupada(), "Ranura 2 debe contener el autoguardado de la nueva partida");
        assertEquals(25L, ranuras.get(1).datos().reloj().tickActual());
        assertFalse(ranuras.get(2).estaOcupada(), "Ranura 3 debe seguir libre sin rotacion");
    }

    @Test
    void testRebalanceoEconomiaSolMuchoMasCaroYInicioSoloAlcanzaSolYPlanetaAgua() {
        assertTrue(ConfiguracionEconomia.COSTO_ESTRELLA_MINERALES > ConfiguracionEconomia.COSTO_PLANETA_GASEOSO_MINERALES);
        assertTrue(ConfiguracionEconomia.COSTO_PLANETA_GASEOSO_MINERALES > ConfiguracionEconomia.COSTO_PLANETA_LAVA_MINERALES);
        assertTrue(ConfiguracionEconomia.COSTO_PLANETA_LAVA_MINERALES > ConfiguracionEconomia.COSTO_PLANETA_MINERALES);
        assertTrue(ConfiguracionEconomia.COSTO_PLANETA_MINERALES > ConfiguracionEconomia.COSTO_PLANETA_AGUA_MINERALES);

        SimulacionSolar sim = new SimulacionSolar(1920, 1080);
        double masaInicial = ConfiguracionSimulacion.MASA_FACTOR_MIN;

        // No alcanza para 2 soles
        Optional<CuerpoCeleste> sol1 = sim.spawnCuerpo(TipoCuerpo.ESTRELLA, 0, 0, masaInicial, Vector2D.cero());
        assertTrue(sol1.isPresent(), "Al inicio debe alcanzar para colocar 1 Sol");
        assertFalse(sim.getInventario().puedeCrear(TipoCuerpo.ESTRELLA, masaInicial), "No debe alcanzar para un segundo Sol");
        assertFalse(sim.getInventario().puedeCrear(TipoCuerpo.PLANETA_ROCOSO, masaInicial), "No debe alcanzar para un Planeta Porro");
        assertFalse(sim.getInventario().puedeCrear(TipoCuerpo.LUNA, masaInicial), "No debe alcanzar para una Luna");

        // Alcanza exactamente para 1 Planeta de Agua y nada mas
        assertTrue(sim.getInventario().puedeCrear(TipoCuerpo.PLANETA_AGUA, masaInicial));
        Optional<CuerpoCeleste> agua1 = sim.spawnCuerpo(TipoCuerpo.PLANETA_AGUA, 180, 0, masaInicial, new Vector2D(0, 2.0));
        assertTrue(agua1.isPresent(), "Al inicio debe alcanzar para colocar 1 Planeta de Agua junto al Sol");

        assertEquals(0.0, sim.getInventario().getRecurso(TipoRecurso.MINERALES), 1e-6);
        assertEquals(0.0, sim.getInventario().getRecurso(TipoRecurso.ENERGIA), 1e-6);
        assertFalse(sim.getInventario().puedeCrear(TipoCuerpo.PLANETA_AGUA, masaInicial), "No debe alcanzar para un segundo Planeta de Agua");
    }

    @Test
    void testProgresionMasLentaYReguladaEnConfiguracionEconomia() {
        SimulacionSolar sim = new SimulacionSolar(1920, 1080);
        double masaInicial = ConfiguracionSimulacion.MASA_FACTOR_MIN;
        sim.spawnCuerpo(TipoCuerpo.ESTRELLA, 0, 0, masaInicial, Vector2D.cero());
        sim.spawnCuerpo(TipoCuerpo.PLANETA_AGUA, 200, 0, masaInicial, new Vector2D(0, 2.2));

        // Tras 1000 ticks (~50 segundos reales a 1x), aun no se desbloquea el primer escalon ni sobra para otro Sol
        sim.avanzarTicks(1000);
        assertFalse(sim.getProgresion().puedeInvestigar(RamaProgresion.PLANETAS, sim.getInventario()),
                "La progresion inicial debe ser mas lenta y regulada");
        assertFalse(sim.getInventario().puedeCrear(TipoCuerpo.ESTRELLA, masaInicial),
                "En los primeros 1000 ticks no debe acumularse suficiente para otro Sol");

        // Costos de investigacion estrictamente crecientes y sincronizados con ConfiguracionEconomia
        double costoPrevio = 0.0;
        for (int nivel = 1; nivel <= 5; nivel++) {
            double costoNivel = ConfiguracionEconomia.costoProgresionNivel(nivel);
            assertTrue(costoNivel > costoPrevio, "Cada nivel tecnologico debe costar mas ciencia que el anterior");
            costoPrevio = costoNivel;
        }
    }

    private MouseEvent crearEventoMouse(javafx.event.EventType<? extends MouseEvent> tipo,
                                        double x,
                                        double y,
                                        MouseButton boton) {
        return new MouseEvent(
                tipo, x, y, x, y, boton, 1,
                false, false, false, false,
                boton == MouseButton.PRIMARY, false, boton == MouseButton.SECONDARY,
                true, false, true, null
        );
    }
}
