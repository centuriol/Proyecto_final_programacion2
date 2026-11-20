package org.example;

import org.example.game.colonizacion.NaveColonizadora;
import org.example.game.cuerpo.*;
import org.example.game.guardado.GestorGuardadoPartida;
import org.example.game.guardado.RepositorioPartidasArchivo;
import org.example.game.jugador.TipoRecurso;
import org.example.game.motor.Vector2D;
import org.example.game.progresion.MejoraSecundaria;
import org.example.game.progresion.RamaProgresion;
import org.example.game.render.CamaraZoom;
import org.example.game.simulacion.SimulacionSolar;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

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
        simOrigen.getProgresion().investigarSecundaria(MejoraSecundaria.PLANETAS_T1_MINERIA, simOrigen.getInventario());
        simOrigen.getProgresion().investigarSecundaria(MejoraSecundaria.PLANETAS_T4_ESCUDO, simOrigen.getInventario());
        simOrigen.getProgresion().investigarSecundaria(MejoraSecundaria.MASA_T1_COSTO, simOrigen.getInventario());

        Estrella sol = new CuerpoCelesteFactory().crearEstrellaPersonalizada("SolAlpha", 1.989e30, 0, 0);
        Planeta tierra = new CuerpoCelesteFactory().crearPlanetaPersonalizado(
                "Gaia", 5.97e24, 220, 0, TipoCuerpo.PLANETA_ROCOSO, new Vector2D(0, -11.0));
        tierra.setCuerpoOrbitado(sol);
        tierra.setRadioOrbita(220.0);
        tierra.activarEscudo(4);
        tierra.getCivilizacion().restaurarMetricas(9000.0, 2, 0.85, 0.90);

        Planeta oceano = new CuerpoCelesteFactory().crearPlanetaPersonalizado(
                "Poseidon", 5.97e24, 340, 0, TipoCuerpo.PLANETA_AGUA, new Vector2D(0, -8.5));
        oceano.setCuerpoOrbitado(sol);
        oceano.setRadioOrbita(340.0);

        Luna luna = new CuerpoCelesteFactory().crearLunaPersonalizada("Selene", 7.34e22, 260, 0, new Vector2D(0, -12.2));
        luna.setCuerpoOrbitado(tierra);
        luna.setRadioOrbita(40.0);

        Satelite satelite = new CuerpoCelesteFactory().crearSatelite(240, 0, 1.0, new Vector2D(0, -13.0));
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
        simOrigen.getCoordinadorPaso().getGeneradorEventosCosmicos().setTicksHastaSiguiente(1420);

        GestorGuardadoPartida gestor = new GestorGuardadoPartida(new RepositorioPartidasArchivo(tempDir.resolve("saves")));
        gestor.guardarPartida("sistema_completo", simOrigen, zoomOrigen);

        SimulacionSolar simDestino = new SimulacionSolar(1920, 1080);
        CamaraZoom zoomDestino = new CamaraZoom();
        assertTrue(gestor.cargarPartida("sistema_completo", simDestino, zoomDestino));

        assertEquals(512L, simDestino.getReloj().getTickActual());
        assertTrue(simDestino.getReloj().isEnPausa());
        assertEquals(3.0, simDestino.getReloj().getVelocidadSimulacion(), 1e-6);
        assertEquals(1.4, zoomDestino.getNivelZoom(), 1e-6);
        assertEquals(1420, simDestino.getCoordinadorPaso().getGeneradorEventosCosmicos().getTicksHastaSiguiente());
        assertEquals(777.5, simDestino.getInventario().getRecurso(TipoRecurso.MINERALES), 1e-6);
        assertEquals(444.25, simDestino.getInventario().getRecurso(TipoRecurso.ENERGIA), 1e-6);
        assertEquals(4, simDestino.getProgresion().getNivel(RamaProgresion.PLANETAS));
        assertEquals(1, simDestino.getProgresion().getNivel(RamaProgresion.MASA));
        assertTrue(simDestino.getProgresion().estaInvestigada(MejoraSecundaria.PLANETAS_T1_MINERIA));
        assertTrue(simDestino.getProgresion().estaInvestigada(MejoraSecundaria.PLANETAS_T4_ESCUDO));
        assertTrue(simDestino.getProgresion().estaInvestigada(MejoraSecundaria.MASA_T1_COSTO));
        assertFalse(simDestino.getProgresion().estaInvestigada(MejoraSecundaria.PLANETAS_T2_COSTO));

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
        Planeta inerte = new CuerpoCelesteFactory().crearPlanetaRocoso(150, 0, 1.0, Vector2D.cero());
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
    void testPlanetaTransformadoNoGeneraRecursosTrasGuardarCargarYAvanzarTick() {
        SimulacionSolar simOrigen = new SimulacionSolar(1280, 720);
        Planeta planetaLavaTransformado = new CuerpoCelesteFactory().crearPlanetaRocoso(180, 0, 1.0, Vector2D.cero());
        planetaLavaTransformado.transformarA(TipoCuerpo.PLANETA_LAVA);
        simOrigen.agregarCuerpo(planetaLavaTransformado);

        simOrigen.getInventario().getRecursoObj(TipoRecurso.MINERALES).setCantidad(500.0);
        simOrigen.getInventario().getRecursoObj(TipoRecurso.ENERGIA).setCantidad(300.0);
        simOrigen.getInventario().getRecursoObj(TipoRecurso.CIENCIA).setCantidad(100.0);

        GestorGuardadoPartida gestor = new GestorGuardadoPartida(
                new RepositorioPartidasArchivo(tempDir.resolve("transformado_sin_produccion")));
        gestor.guardarPartidaRapida(simOrigen, null);

        SimulacionSolar simCargada = new SimulacionSolar(1280, 720);
        assertTrue(gestor.cargarPartidaRapida(simCargada, null));

        assertEquals(1, simCargada.getSistemaSolar().getCuerpos().size());
        Planeta recuperado = (Planeta) simCargada.getSistemaSolar().getCuerpos().get(0);

        assertEquals(TipoCuerpo.PLANETA_LAVA, recuperado.getTipoCuerpo());
        assertTrue(recuperado.isTransformadoPorEvento());
        assertFalse(recuperado.permiteReembolso());
        assertFalse(recuperado.tieneCivilizacion());
        assertNull(recuperado.getCivilizacion());

        Optional<Transformable> transformableReg = RegistroCapacidades.obtenerTransformable(recuperado);
        assertTrue(transformableReg.isPresent());
        assertTrue(transformableReg.get().isTransformadoPorEvento());

        simCargada.getReloj().play();
        for (int i = 0; i < 20; i++) simCargada.avanzarTick();

        assertEquals(500.0, simCargada.getInventario().getRecurso(TipoRecurso.MINERALES), 1e-6);
        assertEquals(300.0, simCargada.getInventario().getRecurso(TipoRecurso.ENERGIA), 1e-6);
        assertEquals(100.0, simCargada.getInventario().getRecurso(TipoRecurso.CIENCIA), 1e-6);
    }
}
