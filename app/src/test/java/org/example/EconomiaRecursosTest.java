package org.example;

import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.SimulacionSolar;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(JavaFXExtension.class)
class EconomiaRecursosTest {

    private InventarioJugador inventario;

    @BeforeEach
    void setUp() {
        inventario = new InventarioJugador();
    }

    @Test
    void testInventarioRecursosIniciales() {
        assertEquals(400.0, inventario.getRecurso(TipoRecurso.MINERALES));
        assertEquals(200.0, inventario.getRecurso(TipoRecurso.ENERGIA));
        assertEquals(0.0, inventario.getRecurso(TipoRecurso.CIENCIA));
        assertEquals(0.0, inventario.getRecurso(TipoRecurso.POBLACION));

        assertTrue(inventario.puedeCrear(TipoCuerpo.ESTRELLA, 1.0));
        assertTrue(inventario.puedeCrear(TipoCuerpo.PLANETA_ROCOSO, 1.0));

        assertTrue(inventario.gastarParaCrear(TipoCuerpo.ESTRELLA, 1.0));
        assertTrue(inventario.gastarParaCrear(TipoCuerpo.PLANETA_ROCOSO, 1.0));

        assertEquals(0.0, inventario.getRecurso(TipoRecurso.MINERALES));
        assertEquals(0.0, inventario.getRecurso(TipoRecurso.ENERGIA));
        assertEquals(0.0, inventario.getRecurso(TipoRecurso.CIENCIA));

        assertFalse(inventario.puedeCrear(TipoCuerpo.PLANETA_ROCOSO, 1.0));
        assertFalse(inventario.puedeCrear(TipoCuerpo.LUNA, 1.0));
        assertFalse(inventario.puedeCrear(TipoCuerpo.ESTRELLA, 1.0));
    }

    @Test
    void testEconomiaCostoEscaladoSuperlineal() {
        double costoBase = 100.0;
        assertEquals(100.0, InventarioJugador.calcularCostoEscalado(costoBase, 1.0), 1e-6);
        assertEquals(800.0, InventarioJugador.calcularCostoEscalado(costoBase, 4.0), 1e-6);

        double costo10x = InventarioJugador.calcularCostoEscalado(costoBase, 10.0);
        assertEquals(costoBase * Math.pow(10.0, 1.5), costo10x, 1e-6);

        var costosTierra1x = inventario.calcularCostoTotal(TipoCuerpo.PLANETA_ROCOSO, 1.0);
        assertEquals(ConfiguracionSimulacion.COSTO_PLANETA_MINERALES, costosTierra1x.get(TipoRecurso.MINERALES), 1e-6);
        assertEquals(ConfiguracionSimulacion.COSTO_PLANETA_ENERGIA, costosTierra1x.get(TipoRecurso.ENERGIA), 1e-6);

        var costosTierra4x = inventario.calcularCostoTotal(TipoCuerpo.PLANETA_ROCOSO, 4.0);
        assertEquals(ConfiguracionSimulacion.COSTO_PLANETA_MINERALES * 8.0, costosTierra4x.get(TipoRecurso.MINERALES), 1e-6);
        assertEquals(ConfiguracionSimulacion.COSTO_PLANETA_ENERGIA * 8.0, costosTierra4x.get(TipoRecurso.ENERGIA), 1e-6);

        double minAntes = inventario.getRecurso(TipoRecurso.MINERALES);
        double eneAntes = inventario.getRecurso(TipoRecurso.ENERGIA);
        assertTrue(inventario.puedeCrear(TipoCuerpo.PLANETA_ROCOSO, 1.0));
        assertTrue(inventario.gastarParaCrear(TipoCuerpo.PLANETA_ROCOSO, 1.0));
        assertEquals(minAntes - ConfiguracionSimulacion.COSTO_PLANETA_MINERALES, inventario.getRecurso(TipoRecurso.MINERALES), 1e-6);
        assertEquals(eneAntes - ConfiguracionSimulacion.COSTO_PLANETA_ENERGIA, inventario.getRecurso(TipoRecurso.ENERGIA), 1e-6);
    }

    @Test
    void testSoloExistenCuatroRecursos() {
        TipoRecurso[] recursos = TipoRecurso.values();
        assertEquals(4, recursos.length);

        var nombres = java.util.Arrays.stream(recursos).map(r -> r.nombre.toLowerCase()).toList();
        assertTrue(nombres.contains("minerales"));
        assertTrue(nombres.contains("energia"));
        assertTrue(nombres.contains("poblacion"));
        assertTrue(nombres.contains("ciencia"));
    }

    @Test
    void testEspecificacionMaterialesParaPonerYPorTick() {
        var costoEstrella = InventarioJugador.getCostosBase(TipoCuerpo.ESTRELLA);
        assertTrue(costoEstrella.containsKey(TipoRecurso.MINERALES));
        assertTrue(costoEstrella.containsKey(TipoRecurso.ENERGIA));
        var prodEstrella = InventarioJugador.getProduccionBasePorTick(TipoCuerpo.ESTRELLA);
        assertTrue(prodEstrella.containsKey(TipoRecurso.ENERGIA));
        assertTrue(prodEstrella.containsKey(TipoRecurso.MINERALES));

        var costoPlaneta = InventarioJugador.getCostosBase(TipoCuerpo.PLANETA_ROCOSO);
        assertTrue(costoPlaneta.containsKey(TipoRecurso.MINERALES));
        assertTrue(costoPlaneta.containsKey(TipoRecurso.ENERGIA));
        var prodPlaneta = InventarioJugador.getProduccionBasePorTick(TipoCuerpo.PLANETA_ROCOSO);
        assertTrue(prodPlaneta.containsKey(TipoRecurso.MINERALES));
        assertTrue(prodPlaneta.containsKey(TipoRecurso.CIENCIA));
        assertTrue(prodPlaneta.containsKey(TipoRecurso.POBLACION));

        var costoLuna = InventarioJugador.getCostosBase(TipoCuerpo.LUNA);
        assertTrue(costoLuna.containsKey(TipoRecurso.MINERALES));
        var prodLuna = InventarioJugador.getProduccionBasePorTick(TipoCuerpo.LUNA);
        assertTrue(prodLuna.containsKey(TipoRecurso.MINERALES));
        assertEquals(1, prodLuna.size());

        var costoAgua = InventarioJugador.getCostosBase(TipoCuerpo.PLANETA_AGUA);
        assertEquals(50.0, costoAgua.get(TipoRecurso.MINERALES));
        assertEquals(25.0, costoAgua.get(TipoRecurso.ENERGIA));
        var prodAgua = InventarioJugador.getProduccionBasePorTick(TipoCuerpo.PLANETA_AGUA);
        assertEquals(ConfiguracionSimulacion.PRODUCCION_PLANETA_AGUA_MINERALES, prodAgua.get(TipoRecurso.MINERALES), 1e-6);
        assertEquals(ConfiguracionSimulacion.PRODUCCION_PLANETA_AGUA_CIENCIA, prodAgua.get(TipoRecurso.CIENCIA), 1e-6);
        assertFalse(prodAgua.containsKey(TipoRecurso.POBLACION));

        var costoLava = InventarioJugador.getCostosBase(TipoCuerpo.PLANETA_LAVA);
        assertEquals(150.0, costoLava.get(TipoRecurso.MINERALES));
        assertEquals(75.0, costoLava.get(TipoRecurso.ENERGIA));
        var prodLava = InventarioJugador.getProduccionBasePorTick(TipoCuerpo.PLANETA_LAVA);
        assertEquals(ConfiguracionSimulacion.PRODUCCION_PLANETA_LAVA_MINERALES, prodLava.get(TipoRecurso.MINERALES), 1e-6);
        assertEquals(ConfiguracionSimulacion.PRODUCCION_PLANETA_LAVA_CIENCIA, prodLava.get(TipoRecurso.CIENCIA), 1e-6);
        assertFalse(prodLava.containsKey(TipoRecurso.POBLACION));

        assertEquals(0.10, prodEstrella.get(TipoRecurso.ENERGIA), 1e-6);
        assertEquals(0.04, prodEstrella.get(TipoRecurso.MINERALES), 1e-6);
    }

    @Test
    void testCostosYProduccionEscaladosPorMasa() {
        var costoBase = InventarioJugador.getCostosEscalados(TipoCuerpo.PLANETA_ROCOSO, 1.0);
        var prodBase = InventarioJugador.getProduccionEscaladaPorTick(TipoCuerpo.PLANETA_ROCOSO, 1.0);

        var costoDoble = InventarioJugador.getCostosEscalados(TipoCuerpo.PLANETA_ROCOSO, 2.0);
        var prodDoble = InventarioJugador.getProduccionEscaladaPorTick(TipoCuerpo.PLANETA_ROCOSO, 2.0);

        double minCostoBase = costoBase.get(TipoRecurso.MINERALES);
        double minCostoDoble = costoDoble.get(TipoRecurso.MINERALES);
        assertTrue(minCostoDoble > minCostoBase * 2.0);

        double prodMinBase = prodBase.get(TipoRecurso.MINERALES);
        double prodMinDoble = prodDoble.get(TipoRecurso.MINERALES);
        assertEquals(prodMinBase * 2.0, prodMinDoble, 0.001);
    }

    @Test
    void testRechazoPorRecursosInsuficientesAlAumentarMasa() {
        InventarioJugador inv = new InventarioJugador();
        assertTrue(inv.puedeCrear(TipoCuerpo.PLANETA_ROCOSO, 1.0));
        assertFalse(inv.puedeCrear(TipoCuerpo.PLANETA_ROCOSO, 10.0));
    }

    @Test
    void testPresupuestoInicialAlcanzaSoloParaUnSolYUnPlaneta() {
        SimulacionSolar sim = new SimulacionSolar(1920, 1080);
        sim.getProgresion().desbloquearTodo();
        assertTrue(sim.getSistemaSolar().getCuerpos().isEmpty());

        sim.entrarModoColocacion(TipoCuerpo.ESTRELLA, 1.0, "SolInicial");
        sim.actualizarPosicionPreview(960, 540);
        sim.confirmarColocacionAutoOrbita();

        assertEquals(1, sim.getSistemaSolar().getCuerpos().size());
        assertEquals(100.0, sim.getInventario().getRecurso(TipoRecurso.MINERALES));
        assertEquals(50.0, sim.getInventario().getRecurso(TipoRecurso.ENERGIA));
        assertEquals(0.0, sim.getInventario().getRecurso(TipoRecurso.CIENCIA));

        assertFalse(sim.getInventario().puedeCrear(TipoCuerpo.ESTRELLA, 1.0));

        sim.entrarModoColocacion(TipoCuerpo.PLANETA_ROCOSO, 1.0, "PlanetaInicial");
        sim.actualizarPosicionPreview(1120, 540);
        sim.confirmarColocacionAutoOrbita();

        assertEquals(2, sim.getSistemaSolar().getCuerpos().size());
        assertEquals(0.0, sim.getInventario().getRecurso(TipoRecurso.MINERALES));
        assertEquals(0.0, sim.getInventario().getRecurso(TipoRecurso.ENERGIA));
        assertEquals(0.0, sim.getInventario().getRecurso(TipoRecurso.CIENCIA));

        assertFalse(sim.getInventario().puedeCrear(TipoCuerpo.PLANETA_ROCOSO, 1.0));
        assertFalse(sim.getInventario().puedeCrear(TipoCuerpo.LUNA, 1.0));
        assertFalse(sim.getInventario().puedeCrear(TipoCuerpo.ESTRELLA, 1.0));
    }
}
