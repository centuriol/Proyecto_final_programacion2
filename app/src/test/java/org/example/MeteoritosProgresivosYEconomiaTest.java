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
        org.example.game.simulacion.CalculadorAmenazaMeteorito escalador = new EscaladorAmenazaMeteorito();
        assertEquals(ConfiguracionColisiones.DANO_METEORITO_INICIAL_FRACCION,
                escalador.fraccionDano(ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_INICIAL), 1e-9);
        assertEquals(0.05, ConfiguracionColisiones.DANO_METEORITO_INICIAL_FRACCION, 1e-9);

        double danoBajo = escalador.fraccionDano(ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_BAJO);
        assertEquals(ConfiguracionColisiones.DANO_METEORITO_PROGRESO_BAJO_MAX_FRACCION, danoBajo, 1e-9);
        assertTrue(danoBajo <= 0.10 + 1e-9, "Con progreso bajo (0.6x) el dano maximo es 10%");

        assertEquals(1.0, escalador.fraccionDano(ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_ALTO), 1e-9);
        assertEquals(1.0, escalador.fraccionDano(10.0), 1e-9);

        double previo = 0.0;
        for (double progreso = 0.3; progreso <= 5.0; progreso += 0.1) {
            double dano = escalador.fraccionDano(progreso);
            assertTrue(dano >= previo - 1e-12, "El dano debe crecer de forma monotona con el progreso");
            previo = dano;
        }
    }

    @Test
    void testFactorProgresoSeDerivaDelArbolODelLimiteDeMasa() {
        org.example.game.simulacion.CalculadorAmenazaMeteorito escalador = new EscaladorAmenazaMeteorito();
        SimulacionSolar sim = new SimulacionSolar(1920, 1080);
        assertEquals(ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_INICIAL,
                escalador.factorProgreso(sim.getProgresion()), 1e-9);

        sim.getProgresion().restaurarEstado(Map.of(RamaProgresion.PLANETAS, 1), 0);
        assertEquals(0.6, escalador.factorProgreso(sim.getProgresion()), 1e-9,
                "Un nivel del arbol equivale a progreso bajo (0.6x)");

        sim.getProgresion().restaurarEstado(Map.of(RamaProgresion.MASA, 4), 0);
        assertTrue(escalador.factorProgreso(sim.getProgresion())
                >= ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_ALTO);
    }

    @Test
    void testAlInicioUnMeteoritoSoloQuitaCincoPorCientoYNoDestruyeElPlaneta() {
        SimulacionSolar sim = new SimulacionSolar(1920, 1080);
        Planeta planeta = new CuerpoCelesteFactory().crearPlanetaAgua(0, 0, ConfiguracionEconomia.FACTOR_MASA_INICIAL, Vector2D.cero());
        sim.agregarCuerpo(planeta);
        double masaInicial = planeta.getMasaInicial();

        Meteorito met = new CuerpoCelesteFactory().crearMeteoritoJugadorFisica(0, 0, Vector2D.cero());
        sim.agregarCuerpo(met);
        assertEquals(masaInicial * ConfiguracionColisiones.FRACCION_MASA_METEORITO_PLANETA_MAX, met.getMasa(), masaInicial * 1e-6);
        sim.avanzarTick();

        assertTrue(sim.getSistemaSolar().getCuerpos().contains(planeta), "El planeta no debe destruirse de un solo golpe");
        assertEquals(masaInicial * (1.0 - ConfiguracionColisiones.FRACCION_MASA_METEORITO_PLANETA_MAX), planeta.getMasa(), masaInicial * 1e-6);
        assertTrue(sim.getSistemaSolar().getCuerpos().stream().noneMatch(c -> c instanceof Meteorito));
    }

    @Test
    void testProgresoBajoDanaComoMaximoDiezPorCientoYProgresoAltoDestruye() {
        SimulacionSolar sim = new SimulacionSolar(1920, 1080);
        Planeta planetaChico = new CuerpoCelesteFactory().crearPlanetaAgua(0, 0, 1.0, Vector2D.cero());
        Planeta planetaGrande = new CuerpoCelesteFactory().crearPlanetaRocoso(500, 0, 4.0, Vector2D.cero());
        sim.agregarCuerpo(planetaChico);
        sim.agregarCuerpo(planetaGrande);

        double masaAntesChico = planetaChico.getMasa();
        double masaEsperadaMeteorito = planetaGrande.getMasa() * ConfiguracionColisiones.FRACCION_MASA_METEORITO_PLANETA_MAX;

        Meteorito met = new CuerpoCelesteFactory().crearMeteoritoJugadorFisica(0, 0, Vector2D.cero());
        sim.agregarCuerpo(met);
        assertEquals(masaEsperadaMeteorito, met.getMasa(), masaEsperadaMeteorito * 1e-6);

        sim.avanzarTick();
        assertTrue(sim.getSistemaSolar().getCuerpos().contains(planetaChico));
        assertEquals(masaAntesChico - masaEsperadaMeteorito, planetaChico.getMasa(), masaAntesChico * 1e-6);
    }

    @Test
    void testTamanoYMasaDelMeteoritoEscalanConElProgreso() {
        org.example.game.simulacion.CalculadorAmenazaMeteorito escalador = new EscaladorAmenazaMeteorito();
        double radioInicial = escalador.radioPorProgreso(ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_INICIAL);
        double radioBajo = escalador.radioPorProgreso(ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_BAJO);
        double radioAlto = escalador.radioPorProgreso(ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_ALTO);
        assertTrue(radioInicial < radioBajo && radioBajo < radioAlto);

        Planeta planetaInicial = new CuerpoCelesteFactory().crearPlanetaAgua(0, 0, ConfiguracionEconomia.FACTOR_MASA_INICIAL, Vector2D.cero());
        Meteorito inicial = new CuerpoCelesteFactory().crearMeteoritoHaciaObjetivo(1920, 1080, planetaInicial,
                ConfiguracionColisiones.FACTOR_PROGRESO_METEORITO_INICIAL);
        Meteorito avanzado = new CuerpoCelesteFactory().crearMeteoritoHaciaObjetivo(1920, 1080, planetaInicial,
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
