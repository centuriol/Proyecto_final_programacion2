package org.example;
import org.example.game.cuerpo.CuerpoCeleste; import org.example.game.cuerpo.CuerpoCelesteFactory; import org.example.game.cuerpo.Meteorito; import org.example.game.cuerpo.Planeta; import org.example.game.cuerpo.TipoCuerpo; import org.example.game.jugador.InventarioJugador; import org.example.game.jugador.TipoRecurso; import org.example.game.motor.Vector2D; import org.example.game.progresion.RamaProgresion; import org.example.game.simulacion.ConfiguracionColisiones; import org.example.game.simulacion.ConfiguracionEconomia; import org.example.game.simulacion.EscaladorAmenazaMeteorito; import org.example.game.simulacion.SimulacionSolar; import org.example.test.JavaFXExtension; import org.junit.jupiter.api.Test; import org.junit.jupiter.api.extension.ExtendWith; import java.util.List; import java.util.Map; import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
class MeteoritosProgresivosYEconomiaTest {

    // Valores de referencia del ajuste anterior, solo para verificar la direccion del cambio
    private static final double SOL_MINERALES_PREVIO = 1850.0;
    private static final double AGUA_MINERALES_PREVIO = 520.0;
    private static final double PORRO_MINERALES_PREVIO = 760.0;
    private static final double LAVA_MINERALES_PREVIO = 1100.0;
    private static final double GASEOSO_MINERALES_PREVIO = 1450.0;

    @Test
    void testSolMasBaratoYPlanetasMasCarosManteniendoProporcionConProduccion() {
        assertTrue(ConfiguracionEconomia.COSTO_ESTRELLA_MINERALES < SOL_MINERALES_PREVIO);
        assertTrue(ConfiguracionEconomia.COSTO_PLANETA_AGUA_MINERALES > AGUA_MINERALES_PREVIO);
        assertTrue(ConfiguracionEconomia.COSTO_PLANETA_MINERALES > PORRO_MINERALES_PREVIO);
        assertTrue(ConfiguracionEconomia.COSTO_PLANETA_LAVA_MINERALES > LAVA_MINERALES_PREVIO);
        assertTrue(ConfiguracionEconomia.COSTO_PLANETA_GASEOSO_MINERALES > GASEOSO_MINERALES_PREVIO);
        assertTrue(ConfiguracionEconomia.COSTO_PLANETA_GASEOSO_MINERALES < ConfiguracionEconomia.COSTO_ESTRELLA_MINERALES,
                "El Sol sigue siendo el cuerpo mas caro");

        List<TipoCuerpo> planetas = List.of(
                TipoCuerpo.PLANETA_AGUA, TipoCuerpo.PLANETA_ROCOSO, TipoCuerpo.PLANETA_LAVA, TipoCuerpo.PLANETA_GASEOSO);
        double ratioReferencia = ratioCostoProduccion(planetas.get(0));
        for (TipoCuerpo planeta : planetas) {
            assertEquals(ratioReferencia, ratioCostoProduccion(planeta), ratioReferencia * 0.10,
                    "Costo proporcional a la produccion de " + planeta.nombre);
        }
    }

    @Test
    void testEnergiaReducidaDiezPorCientoYMineralesSubenEntreCincoYDiezPorCiento() {
        assertEquals(0.085 * 0.9, ConfiguracionEconomia.PRODUCCION_ESTRELLA_ENERGIA, 1e-9);
        assertEquals(0.012 * 0.9, ConfiguracionEconomia.PRODUCCION_PLANETA_AGUA_ENERGIA, 1e-9);
        assertEquals(0.025 * 0.9, ConfiguracionEconomia.PRODUCCION_PLANETA_GASEOSO_ENERGIA, 1e-9);
        assertEquals(0.018 * 0.9, ConfiguracionEconomia.PRODUCCION_SATELITE_ENERGIA, 1e-9);

        double[][] mineralesPrevioYActual = {
                {0.038, ConfiguracionEconomia.PRODUCCION_ESTRELLA_MINERALES},
                {0.030, ConfiguracionEconomia.PRODUCCION_PLANETA_AGUA_MINERALES},
                {0.042, ConfiguracionEconomia.PRODUCCION_PLANETA_MINERALES},
                {0.072, ConfiguracionEconomia.PRODUCCION_PLANETA_LAVA_MINERALES},
                {0.085, ConfiguracionEconomia.PRODUCCION_PLANETA_GASEOSO_MINERALES},
                {0.038, ConfiguracionEconomia.PRODUCCION_LUNA_MINERALES}
        };
        for (double[] par : mineralesPrevioYActual) {
            double aumento = par[1] / par[0];
            assertTrue(aumento >= 1.05 - 1e-9 && aumento <= 1.10 + 1e-9, "Aumento de minerales fuera de rango: " + aumento);
        }
    }

    @Test
    void testFraccionDeDanoEscalaConProgresoYEsConfigurable() {
        assertEquals(ConfiguracionColisiones.DANO_METEORITO_INICIAL_FRACCION,
                EscaladorAmenazaMeteorito.calcularFraccionDano(ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_INICIAL), 1e-9);
        assertEquals(0.05, ConfiguracionColisiones.DANO_METEORITO_INICIAL_FRACCION, 1e-9);

        double danoBajo = EscaladorAmenazaMeteorito.calcularFraccionDano(ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_BAJO);
        assertEquals(ConfiguracionColisiones.DANO_METEORITO_PROGRESO_BAJO_MAX_FRACCION, danoBajo, 1e-9);
        assertTrue(danoBajo <= 0.10 + 1e-9, "Con progreso bajo (0.6x) el dano maximo es 10%");

        assertEquals(1.0, EscaladorAmenazaMeteorito.calcularFraccionDano(ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_ALTO), 1e-9);
        assertEquals(1.0, EscaladorAmenazaMeteorito.calcularFraccionDano(10.0), 1e-9);

        double previo = 0.0;
        for (double progreso = 0.3; progreso <= 5.0; progreso += 0.1) {
            double dano = EscaladorAmenazaMeteorito.calcularFraccionDano(progreso);
            assertTrue(dano >= previo - 1e-12, "El dano debe crecer de forma monotona con el progreso");
            previo = dano;
        }
    }

    @Test
    void testFactorProgresoSeDerivaDelArbolODelLimiteDeMasa() {
        SimulacionSolar sim = new SimulacionSolar(1920, 1080);
        assertEquals(ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_INICIAL,
                EscaladorAmenazaMeteorito.calcularFactorProgreso(sim.getProgresion()), 1e-9);

        sim.getProgresion().restaurarEstado(Map.of(RamaProgresion.PLANETAS, 1), 0);
        assertEquals(0.6, EscaladorAmenazaMeteorito.calcularFactorProgreso(sim.getProgresion()), 1e-9,
                "Un nivel del arbol equivale a progreso bajo (0.6x)");

        sim.getProgresion().restaurarEstado(Map.of(RamaProgresion.MASA, 4), 0);
        assertTrue(EscaladorAmenazaMeteorito.calcularFactorProgreso(sim.getProgresion())
                >= ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_ALTO);
    }

    @Test
    void testAlInicioUnMeteoritoSoloQuitaCincoPorCientoYNoDestruyeElPlaneta() {
        SimulacionSolar sim = new SimulacionSolar(1920, 1080);
        Planeta planeta = CuerpoCelesteFactory.crearPlanetaAgua(0, 0, ConfiguracionEconomia.FACTOR_MASA_INICIAL, Vector2D.cero());
        sim.agregarCuerpo(planeta);
        double masaInicial = planeta.getMasaInicial();

        sim.agregarCuerpo(CuerpoCelesteFactory.crearMeteoritoJugadorFisica(0, 0, Vector2D.cero()));
        sim.avanzarTick();

        assertTrue(sim.getSistemaSolar().getCuerpos().contains(planeta), "El planeta no debe destruirse de un solo golpe");
        assertEquals(masaInicial * (1.0 - ConfiguracionColisiones.DANO_METEORITO_INICIAL_FRACCION), planeta.getMasa(), masaInicial * 1e-6);
        assertTrue(sim.getSistemaSolar().getCuerpos().stream().noneMatch(c -> c instanceof Meteorito));
    }

    @Test
    void testProgresoBajoDanaComoMaximoDiezPorCientoYProgresoAltoDestruye() {
        SimulacionSolar simBaja = new SimulacionSolar(1920, 1080);
        simBaja.getProgresion().restaurarEstado(Map.of(RamaProgresion.PLANETAS, 1), 0);
        Planeta planetaBajo = CuerpoCelesteFactory.crearPlanetaAgua(0, 0, 0.6, Vector2D.cero());
        simBaja.agregarCuerpo(planetaBajo);
        double masaBaja = planetaBajo.getMasaInicial();
        simBaja.agregarCuerpo(CuerpoCelesteFactory.crearMeteoritoJugadorFisica(0, 0, Vector2D.cero()));
        simBaja.avanzarTick();
        assertTrue(simBaja.getSistemaSolar().getCuerpos().contains(planetaBajo));
        assertTrue(planetaBajo.getMasa() >= masaBaja * (1.0 - ConfiguracionColisiones.DANO_METEORITO_PROGRESO_BAJO_MAX_FRACCION) - masaBaja * 1e-6);

        SimulacionSolar simAlta = new SimulacionSolar(1920, 1080);
        simAlta.getProgresion().restaurarEstado(Map.of(RamaProgresion.MASA, 4), 0);
        Planeta planetaAlto = CuerpoCelesteFactory.crearPlanetaAgua(0, 0, 1.0, Vector2D.cero());
        simAlta.agregarCuerpo(planetaAlto);
        simAlta.agregarCuerpo(CuerpoCelesteFactory.crearMeteoritoJugadorFisica(0, 0, Vector2D.cero()));
        simAlta.avanzarTick();
        assertFalse(simAlta.getSistemaSolar().getCuerpos().contains(planetaAlto), "Con progreso alto el dano es completo");
    }

    @Test
    void testTamanoYMasaDelMeteoritoEscalanConElProgreso() {
        double radioInicial = EscaladorAmenazaMeteorito.calcularRadioPorProgreso(ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_INICIAL);
        double radioBajo = EscaladorAmenazaMeteorito.calcularRadioPorProgreso(ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_BAJO);
        double radioAlto = EscaladorAmenazaMeteorito.calcularRadioPorProgreso(ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_ALTO);
        assertTrue(radioInicial < radioBajo && radioBajo < radioAlto);

        Planeta planetaInicial = CuerpoCelesteFactory.crearPlanetaAgua(0, 0, ConfiguracionEconomia.FACTOR_MASA_INICIAL, Vector2D.cero());
        Meteorito inicial = CuerpoCelesteFactory.crearMeteoritoHaciaObjetivo(1920, 1080, planetaInicial,
                ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_INICIAL);
        Meteorito avanzado = CuerpoCelesteFactory.crearMeteoritoHaciaObjetivo(1920, 1080, planetaInicial,
                ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_ALTO);

        assertEquals(ConfiguracionColisiones.MASA_MINIMA_METEORITO, inicial.getMasa(), ConfiguracionColisiones.MASA_MINIMA_METEORITO * 1e-6);
        assertEquals(radioInicial, inicial.getRadio(), 1e-6);
        assertTrue(avanzado.getMasa() > inicial.getMasa());
        assertTrue(avanzado.getRadio() > inicial.getRadio());
        assertTrue(avanzado.getRadio() <= ConfiguracionColisiones.RADIO_MAXIMO_METEORITO + 1e-9);
        assertTrue(inicial.getRadio() < planetaInicial.getRadio() * 0.25, "Al inicio el meteorito es mucho mas chico que el planeta");
    }

    @Test
    void testPresupuestoInicialSigueAlcanzandoSoloParaSolYPlanetaDeAgua() {
        InventarioJugador inventario = new InventarioJugador();
        double masa = ConfiguracionEconomia.FACTOR_MASA_INICIAL;
        assertTrue(inventario.gastarParaCrear(TipoCuerpo.ESTRELLA, masa));
        assertFalse(inventario.puedeCrear(TipoCuerpo.LUNA, masa));
        assertFalse(inventario.puedeCrear(TipoCuerpo.PLANETA_ROCOSO, masa));
        assertTrue(inventario.gastarParaCrear(TipoCuerpo.PLANETA_AGUA, masa));
        assertEquals(0.0, inventario.getRecurso(TipoRecurso.MINERALES), 1e-6);
        assertEquals(0.0, inventario.getRecurso(TipoRecurso.ENERGIA), 1e-6);
    }

    private static double ratioCostoProduccion(TipoCuerpo tipo) {
        double costo = InventarioJugador.getCostosBase(tipo).values().stream().mapToDouble(Double::doubleValue).sum();
        double produccion = InventarioJugador.getProduccionBasePorTick(tipo).values().stream().mapToDouble(Double::doubleValue).sum();
        return costo / produccion;
    }
}
