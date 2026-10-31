package org.example;
import org.example.game.cuerpo.*; import org.example.game.jugador.InventarioJugador; import org.example.game.jugador.ServicioReembolso; import org.example.game.jugador.TipoRecurso; import org.example.game.motor.Vector2D; import org.example.game.render.GestorImagenes; import org.example.game.simulacion.ConfiguracionSimulacion; import org.example.game.simulacion.SimulacionSolar; import org.example.test.JavaFXExtension; import org.junit.jupiter.api.Test; import org.junit.jupiter.api.extension.ExtendWith; import java.util.List; import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
class ReembolsoYColocacionTest {

    @Test
    void testCuerpoCelesteFactory() {
        Estrella estrella = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        assertNotNull(estrella);
        assertEquals(TipoCuerpo.ESTRELLA, estrella.getTipoCuerpo());

        Vector2D pos = new Vector2D(150, 0);
        Vector2D velOrbital = CuerpoCelesteFactory.calcularVelocidadOrbitalVector(estrella, pos, false);
        assertNotNull(velOrbital);
        assertTrue(velOrbital.magnitud() > 0);

        Planeta planeta = CuerpoCelesteFactory.crearPlanetaRocoso(pos.x(), pos.y(), 1.0, velOrbital);
        assertNotNull(planeta);
        assertEquals(TipoCuerpo.PLANETA_ROCOSO, planeta.getTipoCuerpo());

        Luna luna = CuerpoCelesteFactory.crearLuna(170, 0, 1.0, velOrbital);
        assertEquals(TipoCuerpo.LUNA, luna.getTipoCuerpo());

        Satelite sat = CuerpoCelesteFactory.crearSatelite(140, 0, 1.0, velOrbital);
        assertEquals(TipoCuerpo.SATELITE, sat.getTipoCuerpo());
    }

    @Test
    void testLimitesFactorMasaCentralizados() {
        assertEquals(0.3, ConfiguracionSimulacion.MASA_FACTOR_MIN, 1e-6);
        assertEquals(10.0, ConfiguracionSimulacion.MASA_FACTOR_MAX, 1e-6);

        assertEquals(TipoCuerpo.PLANETA_ROCOSO.masaBase * 0.3, TipoCuerpo.PLANETA_ROCOSO.getMasaMin(), 1e-6);
        assertEquals(TipoCuerpo.PLANETA_ROCOSO.masaBase * 10.0, TipoCuerpo.PLANETA_ROCOSO.getMasaMax(), 1e-6);

        SimulacionSolar simulacion = new SimulacionSolar(1920, 1080);
        simulacion.getProgresion().desbloquearTodo();
        simulacion.setFactorMasaColocacion(0.2);
        assertEquals(0.3, simulacion.getFactorMasaColocacion(), 1e-6);

        simulacion.setFactorMasaColocacion(15.0);
        assertEquals(10.0, simulacion.getFactorMasaColocacion(), 1e-6);

        simulacion.setFactorMasaColocacion(4.5);
        assertEquals(4.5, simulacion.getFactorMasaColocacion(), 1e-6);
    }

    @Test
    void testSistemaInicialVacioYPresetOpcional() {
        SimulacionSolar sim = new SimulacionSolar(1920, 1080);
        List<CuerpoCeleste> cuerpos = sim.getSistemaSolar().getCuerpos();

        assertTrue(cuerpos.isEmpty());

        sim.cargarPresetSistemaBasico();
        assertEquals(3, sim.getSistemaSolar().getCuerpos().size());
    }

    @Test
    void testEliminarCuerpoReembolsaMitadDeMateriales() {
        SimulacionSolar sim = new SimulacionSolar(1920, 1080);
        sim.getProgresion().desbloquearTodo();
        sim.getInventario().agregarRecurso(TipoRecurso.MINERALES, 1000.0);
        sim.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 1000.0);
        double minAntes = sim.getInventario().getRecurso(TipoRecurso.MINERALES);
        double eneAntes = sim.getInventario().getRecurso(TipoRecurso.ENERGIA);

        sim.entrarModoColocacion(TipoCuerpo.PLANETA_ROCOSO, 1.0, "TierraTest");
        sim.actualizarPosicionPreview(1120, 540);
        sim.confirmarColocacionAutoOrbita();

        assertEquals(minAntes - ConfiguracionSimulacion.COSTO_PLANETA_MINERALES, sim.getInventario().getRecurso(TipoRecurso.MINERALES), 1e-6);
        assertEquals(eneAntes - ConfiguracionSimulacion.COSTO_PLANETA_ENERGIA, sim.getInventario().getRecurso(TipoRecurso.ENERGIA), 1e-6);

        CuerpoCeleste planeta = sim.getSistemaSolar().getCuerpos().get(0);
        assertNotNull(planeta);

        ServicioReembolso.desmantelarCuerpo(sim, planeta);

        assertFalse(sim.getSistemaSolar().getCuerpos().contains(planeta));
        assertEquals(minAntes - ConfiguracionSimulacion.COSTO_PLANETA_MINERALES * 0.5, sim.getInventario().getRecurso(TipoRecurso.MINERALES), 1e-6);
        assertEquals(eneAntes - ConfiguracionSimulacion.COSTO_PLANETA_ENERGIA * 0.5, sim.getInventario().getRecurso(TipoRecurso.ENERGIA), 1e-6);
    }

    @Test
    void testSimulacionColocacionConNombreYMasaPersonalizados() {
        SimulacionSolar sim = new SimulacionSolar(1920, 1080);
        sim.getProgresion().desbloquearTodo();
        sim.getInventario().agregarRecurso(TipoRecurso.MINERALES, 10000);
        sim.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 10000);

        String nombreEsperado = "Planeta-Alfa-77";
        double factorMasaEsperado = 2.5;

        sim.entrarModoColocacion(TipoCuerpo.PLANETA_ROCOSO, factorMasaEsperado, nombreEsperado);
        assertEquals(nombreEsperado, sim.getNombreColocacion());
        assertEquals(factorMasaEsperado, sim.getFactorMasaColocacion());

        sim.actualizarPosicionPreview(1300, 450);
        sim.confirmarColocacionAutoOrbita();

        CuerpoCeleste cuerpoCreado = sim.getSistemaSolar().getCuerpos().stream()
                .filter(c -> nombreEsperado.equals(c.getNombre()))
                .findFirst()
                .orElse(null);

        assertNotNull(cuerpoCreado);
        assertEquals(TipoCuerpo.PLANETA_ROCOSO, cuerpoCreado.getTipoCuerpo());
        assertEquals(TipoCuerpo.PLANETA_ROCOSO.masaBase * factorMasaEsperado, cuerpoCreado.getMasa(), 1e20);
    }

    @Test
    void testEliminarEstrellaReembolsa50PorCiento() {
        SimulacionSolar sim = new SimulacionSolar(1920, 1080);
        sim.getProgresion().desbloquearTodo();
        sim.getInventario().agregarRecurso(TipoRecurso.MINERALES, ConfiguracionSimulacion.COSTO_ESTRELLA_MINERALES);
        sim.getInventario().agregarRecurso(TipoRecurso.ENERGIA, ConfiguracionSimulacion.COSTO_ESTRELLA_ENERGIA);
        double minAntes = sim.getInventario().getRecurso(TipoRecurso.MINERALES);
        double eneAntes = sim.getInventario().getRecurso(TipoRecurso.ENERGIA);

        sim.entrarModoColocacion(TipoCuerpo.ESTRELLA, 1.0, "SolTest");
        sim.actualizarPosicionPreview(960, 540);
        sim.confirmarColocacionAutoOrbita();

        CuerpoCeleste estrella = sim.getSistemaSolar().getCuerpos().get(0);

        ServicioReembolso.desmantelarCuerpo(sim, estrella);

        assertFalse(sim.getSistemaSolar().getCuerpos().contains(estrella));
        assertEquals(minAntes - ConfiguracionSimulacion.COSTO_ESTRELLA_MINERALES * 0.5, sim.getInventario().getRecurso(TipoRecurso.MINERALES), 1e-6);
        assertEquals(eneAntes - ConfiguracionSimulacion.COSTO_ESTRELLA_ENERGIA * 0.5, sim.getInventario().getRecurso(TipoRecurso.ENERGIA), 1e-6);
        assertEquals(0.0, sim.getInventario().getRecurso(TipoRecurso.CIENCIA));
    }

    @Test
    void testNuevosItemsPlanetaAguaYPlanetaLavaYSolX2() {
        SimulacionSolar sim = new SimulacionSolar(1920, 1080);
        sim.limpiarCuerpos();

        var costoAgua = InventarioJugador.getCostosBase(TipoCuerpo.PLANETA_AGUA);
        assertEquals(ConfiguracionSimulacion.COSTO_PLANETA_AGUA_MINERALES, costoAgua.get(TipoRecurso.MINERALES));
        assertEquals(ConfiguracionSimulacion.COSTO_PLANETA_AGUA_ENERGIA, costoAgua.get(TipoRecurso.ENERGIA));
        assertNull(costoAgua.get(TipoRecurso.CIENCIA));

        var costoLava = InventarioJugador.getCostosBase(TipoCuerpo.PLANETA_LAVA);
        assertEquals(ConfiguracionSimulacion.COSTO_PLANETA_LAVA_MINERALES, costoLava.get(TipoRecurso.MINERALES));
        assertEquals(ConfiguracionSimulacion.COSTO_PLANETA_LAVA_ENERGIA, costoLava.get(TipoRecurso.ENERGIA));
        assertNull(costoLava.get(TipoRecurso.CIENCIA));

        var prodAgua = InventarioJugador.getProduccionBasePorTick(TipoCuerpo.PLANETA_AGUA);
        assertEquals(ConfiguracionSimulacion.PRODUCCION_PLANETA_AGUA_MINERALES, prodAgua.get(TipoRecurso.MINERALES), 1e-6);
        assertEquals(ConfiguracionSimulacion.PRODUCCION_PLANETA_AGUA_CIENCIA, prodAgua.get(TipoRecurso.CIENCIA), 1e-6);
        assertNull(prodAgua.get(TipoRecurso.POBLACION));

        var prodLava = InventarioJugador.getProduccionBasePorTick(TipoCuerpo.PLANETA_LAVA);
        assertEquals(ConfiguracionSimulacion.PRODUCCION_PLANETA_LAVA_MINERALES, prodLava.get(TipoRecurso.MINERALES), 1e-6);
        assertEquals(ConfiguracionSimulacion.PRODUCCION_PLANETA_LAVA_CIENCIA, prodLava.get(TipoRecurso.CIENCIA), 1e-6);
        assertNull(prodLava.get(TipoRecurso.POBLACION));

        var prodSol = InventarioJugador.getProduccionBasePorTick(TipoCuerpo.ESTRELLA);
        assertEquals(ConfiguracionSimulacion.PRODUCCION_ESTRELLA_ENERGIA, prodSol.get(TipoRecurso.ENERGIA), 1e-6);
        assertEquals(ConfiguracionSimulacion.PRODUCCION_ESTRELLA_MINERALES, prodSol.get(TipoRecurso.MINERALES), 1e-6);

        Planeta agua = CuerpoCelesteFactory.crearPlanetaAgua(100, 100, 1.0, Vector2D.cero());
        sim.agregarCuerpo(agua);

        double minAntes = sim.getInventario().getRecurso(TipoRecurso.MINERALES);
        double cieAntes = sim.getInventario().getRecurso(TipoRecurso.CIENCIA);
        double pobAntes = sim.getInventario().getRecurso(TipoRecurso.POBLACION);
        double eneAntes = sim.getInventario().getRecurso(TipoRecurso.ENERGIA);

        sim.avanzarTick();

        assertEquals(minAntes + ConfiguracionSimulacion.PRODUCCION_PLANETA_AGUA_MINERALES, sim.getInventario().getRecurso(TipoRecurso.MINERALES), 1e-6);
        assertEquals(cieAntes + ConfiguracionSimulacion.PRODUCCION_PLANETA_AGUA_CIENCIA, sim.getInventario().getRecurso(TipoRecurso.CIENCIA), 1e-6);
        assertEquals(pobAntes, sim.getInventario().getRecurso(TipoRecurso.POBLACION), 1e-6);
        assertEquals(eneAntes + ConfiguracionSimulacion.PRODUCCION_PLANETA_AGUA_ENERGIA, sim.getInventario().getRecurso(TipoRecurso.ENERGIA), 1e-6);

        assertTrue(GestorImagenes.tieneImagen(TipoCuerpo.PLANETA_AGUA));
        assertTrue(GestorImagenes.tieneImagen(TipoCuerpo.PLANETA_LAVA));
    }
}
