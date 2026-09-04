package org.example;

import org.example.game.cuerpo.*;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;
import org.example.game.motor.MotorFisicaPermisiva;
import org.example.game.motor.TrayectoriaPredictor;
import org.example.game.motor.Vector2D;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    private InventarioJugador inventario;
    private MotorFisicaPermisiva motorFisica;
    private TrayectoriaPredictor predictor;

    @BeforeAll
    static void initJFX() {
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {}
    }

    @BeforeEach
    void setUp() {
        inventario = new InventarioJugador();
        motorFisica = new MotorFisicaPermisiva(1000.0);
        predictor = new TrayectoriaPredictor();
    }

    @Test
    void testInventarioRecursosIniciales() {
        assertTrue(inventario.getRecurso(TipoRecurso.MINERALES) > 0);
        assertTrue(inventario.getRecurso(TipoRecurso.ENERGIA) > 0);
        assertTrue(inventario.getRecurso(TipoRecurso.POBLACION) > 0);
        assertTrue(inventario.getRecurso(TipoRecurso.CIENCIA) > 0);
        assertTrue(inventario.puedeCrear(TipoCuerpo.PLANETA_ROCOSO, 1.0));
    }

    @Test
    void testCuerpoCelesteFactory() {
        Estrella estrella = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        assertNotNull(estrella);
        assertEquals(TipoCuerpo.ESTRELLA, estrella.getTipoCuerpo());

        Vector2D pos = new Vector2D(150, 0);
        Vector2D velOrbital = CuerpoCelesteFactory.calcularVelocidadOrbitalVector(estrella, pos, false);
        assertNotNull(velOrbital);
        assertTrue(velOrbital.magnitud() > 0);

        Planeta planeta = CuerpoCelesteFactory.crearPlanetaRocoso(pos.x, pos.y, 1.0, velOrbital);
        assertNotNull(planeta);
        assertEquals(TipoCuerpo.PLANETA_ROCOSO, planeta.getTipoCuerpo());

        Luna luna = CuerpoCelesteFactory.crearLuna(170, 0, 1.0, velOrbital);
        assertEquals(TipoCuerpo.LUNA, luna.getTipoCuerpo());

        Satelite sat = CuerpoCelesteFactory.crearSatelite(140, 0, 1.0, velOrbital);
        assertEquals(TipoCuerpo.SATELITE, sat.getTipoCuerpo());

        EscudoProtector escudo = CuerpoCelesteFactory.crearEscudo(150, 0, 1.0, velOrbital);
        assertEquals(TipoCuerpo.ESCUDO_DOME, escudo.getTipoCuerpo());
    }

    @Test
    void testFisicaPermisivaAvancePaso() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Estrella estrella = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        cuerpos.add(estrella);

        Vector2D pos = new Vector2D(150, 0);
        Vector2D vel = CuerpoCelesteFactory.calcularVelocidadOrbitalVector(estrella, pos, false);
        Planeta planeta = CuerpoCelesteFactory.crearPlanetaRocoso(pos.x, pos.y, 1.0, vel);
        cuerpos.add(planeta);

        Vector2D posInicial = new Vector2D(planeta.getPosicionX(), planeta.getPosicionY());

        // Ejecutar pasos de física
        for (int i = 0; i < 20; i++) {
            motorFisica.avanzarPaso(cuerpos);
        }

        Vector2D posFinal = new Vector2D(planeta.getPosicionX(), planeta.getPosicionY());
        assertNotEquals(posInicial.y, posFinal.y);
        assertTrue(cuerpos.contains(planeta));
    }

    @Test
    void testPredictorTrayectoria() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Estrella estrella = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        cuerpos.add(estrella);

        Vector2D posInicial = new Vector2D(150, 0);
        Vector2D velInicial = CuerpoCelesteFactory.calcularVelocidadOrbitalVector(estrella, posInicial, false);

        List<Vector2D> tray = predictor.predecirTrayectoria(posInicial, velInicial, 5.97e24, cuerpos, 50, 1.0);
        assertNotNull(tray);
        assertEquals(51, tray.size()); // Posición inicial + 50 pasos
    }
    @Test
    void testLimitesFactorMasaCentralizados() {
        // Verificar límites en ConfiguracionSimulacion
        assertEquals(1.0, org.example.game.simulacion.ConfiguracionSimulacion.MASA_FACTOR_MIN, 1e-6);
        assertEquals(10.0, org.example.game.simulacion.ConfiguracionSimulacion.MASA_FACTOR_MAX, 1e-6);

        // Verificar consistencia en TipoCuerpo
        assertEquals(TipoCuerpo.PLANETA_ROCOSO.masaBase * 1.0, TipoCuerpo.PLANETA_ROCOSO.getMasaMin(), 1e-6);
        assertEquals(TipoCuerpo.PLANETA_ROCOSO.masaBase * 10.0, TipoCuerpo.PLANETA_ROCOSO.getMasaMax(), 1e-6);

        // Verificar clamp en SimulacionSolar
        org.example.game.simulacion.SimulacionSolar simulacion = new org.example.game.simulacion.SimulacionSolar(1920, 1080);
        simulacion.setFactorMasaColocacion(0.2);
        assertEquals(1.0, simulacion.getFactorMasaColocacion(), 1e-6);

        simulacion.setFactorMasaColocacion(15.0);
        assertEquals(10.0, simulacion.getFactorMasaColocacion(), 1e-6);

        simulacion.setFactorMasaColocacion(4.5);
        assertEquals(4.5, simulacion.getFactorMasaColocacion(), 1e-6);
    }

    @Test
    void testAsistenciaOrbitalAtenuadaPorMasa() {
        double asistenciaBase = motorFisica.getAsistenciaOrbital();
        double masaTierra = TipoCuerpo.PLANETA_ROCOSO.masaBase;

        // A masa base 1.0x, asistencia efectiva es igual a asistenciaBase
        double asist1x = motorFisica.calcularAsistenciaEfectiva(masaTierra);
        assertEquals(asistenciaBase, asist1x, 1e-6);

        // A masa 10x, asistencia efectiva debe ser menor
        double asist10x = motorFisica.calcularAsistenciaEfectiva(masaTierra * 10.0);
        assertTrue(asist10x < asist1x, "La asistencia para masas altas debe ser menor para mayor dificultad");
        assertEquals(asistenciaBase / 2.0, asist10x, 1e-6); // 1 + log10(10) = 2.0
    }

    @Test
    void testEconomiaCostoEscaladoSuperlineal() {
        // factor 1.0 -> costo base exacto
        double costoBase = 100.0;
        assertEquals(100.0, InventarioJugador.calcularCostoEscalado(costoBase, 1.0), 1e-6);

        // factor 4.0 -> 4^1.5 = 8 -> 800.0
        assertEquals(800.0, InventarioJugador.calcularCostoEscalado(costoBase, 4.0), 1e-6);

        // factor 10.0 -> 10^1.5 ≈ 31.62277 * 100 ≈ 3162.277
        double costo10x = InventarioJugador.calcularCostoEscalado(costoBase, 10.0);
        assertEquals(costoBase * Math.pow(10.0, 1.5), costo10x, 1e-6);

        // Desglose total de costos
        var costosTierra1x = inventario.calcularCostoTotal(TipoCuerpo.PLANETA_ROCOSO, 1.0);
        assertEquals(ConfiguracionSimulacion.COSTO_PLANETA_MINERALES, costosTierra1x.get(TipoRecurso.MINERALES), 1e-6);
        assertEquals(ConfiguracionSimulacion.COSTO_PLANETA_ENERGIA, costosTierra1x.get(TipoRecurso.ENERGIA), 1e-6);

        var costosTierra4x = inventario.calcularCostoTotal(TipoCuerpo.PLANETA_ROCOSO, 4.0);
        assertEquals(ConfiguracionSimulacion.COSTO_PLANETA_MINERALES * 8.0, costosTierra4x.get(TipoRecurso.MINERALES), 1e-6);
        assertEquals(ConfiguracionSimulacion.COSTO_PLANETA_ENERGIA * 8.0, costosTierra4x.get(TipoRecurso.ENERGIA), 1e-6);

        // Verificación de gasto
        double minAntes = inventario.getRecurso(TipoRecurso.MINERALES);
        double eneAntes = inventario.getRecurso(TipoRecurso.ENERGIA);
        assertTrue(inventario.puedeCrear(TipoCuerpo.PLANETA_ROCOSO, 1.0));
        assertTrue(inventario.gastarParaCrear(TipoCuerpo.PLANETA_ROCOSO, 1.0));
        assertEquals(minAntes - ConfiguracionSimulacion.COSTO_PLANETA_MINERALES, inventario.getRecurso(TipoRecurso.MINERALES), 1e-6);
        assertEquals(eneAntes - ConfiguracionSimulacion.COSTO_PLANETA_ENERGIA, inventario.getRecurso(TipoRecurso.ENERGIA), 1e-6);
    }

    @Test
    void testGeneracionRecursosPorCuerpoYSlowGrowth() {
        org.example.game.simulacion.SimulacionSolar simulacion = new org.example.game.simulacion.SimulacionSolar(1920, 1080);
        simulacion.limpiarCuerpos();

        // 1. Solo Estrella (Sol): debe generar Energía y Minerales
        Estrella sol = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        simulacion.agregarCuerpo(sol);

        double eneAntes = simulacion.getInventario().getRecurso(TipoRecurso.ENERGIA);
        double minAntes = simulacion.getInventario().getRecurso(TipoRecurso.MINERALES);
        double cieAntes = simulacion.getInventario().getRecurso(TipoRecurso.CIENCIA);
        double pobAntes = simulacion.getInventario().getRecurso(TipoRecurso.POBLACION);

        simulacion.avanzarTick();

        assertEquals(eneAntes + ConfiguracionSimulacion.PRODUCCION_ESTRELLA_ENERGIA, simulacion.getInventario().getRecurso(TipoRecurso.ENERGIA), 1e-6);
        assertEquals(minAntes + ConfiguracionSimulacion.PRODUCCION_ESTRELLA_MINERALES, simulacion.getInventario().getRecurso(TipoRecurso.MINERALES), 1e-6);
        assertEquals(cieAntes, simulacion.getInventario().getRecurso(TipoRecurso.CIENCIA), 1e-6, "El sol no debe generar ciencia");
        assertEquals(pobAntes, simulacion.getInventario().getRecurso(TipoRecurso.POBLACION), 1e-6, "El sol no debe generar poblacion");

        // 2. Solo Planeta: debe generar Minerales, Ciencia y Población, y NO Energía
        simulacion.limpiarCuerpos();
        Planeta planeta = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(planeta);

        eneAntes = simulacion.getInventario().getRecurso(TipoRecurso.ENERGIA);
        minAntes = simulacion.getInventario().getRecurso(TipoRecurso.MINERALES);
        cieAntes = simulacion.getInventario().getRecurso(TipoRecurso.CIENCIA);
        pobAntes = simulacion.getInventario().getRecurso(TipoRecurso.POBLACION);

        simulacion.avanzarTick();

        assertEquals(eneAntes, simulacion.getInventario().getRecurso(TipoRecurso.ENERGIA), 1e-6, "El planeta no debe generar energia");
        assertEquals(minAntes + ConfiguracionSimulacion.PRODUCCION_PLANETA_MINERALES, simulacion.getInventario().getRecurso(TipoRecurso.MINERALES), 1e-6);
        assertEquals(cieAntes + ConfiguracionSimulacion.PRODUCCION_PLANETA_CIENCIA, simulacion.getInventario().getRecurso(TipoRecurso.CIENCIA), 1e-6);
        assertEquals(pobAntes + ConfiguracionSimulacion.PRODUCCION_PLANETA_POBLACION, simulacion.getInventario().getRecurso(TipoRecurso.POBLACION), 1e-6);

        // 3. Solo Luna: debe generar Minerales exclusivamente
        simulacion.limpiarCuerpos();
        Luna luna = CuerpoCelesteFactory.crearLuna(0, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(luna);

        eneAntes = simulacion.getInventario().getRecurso(TipoRecurso.ENERGIA);
        minAntes = simulacion.getInventario().getRecurso(TipoRecurso.MINERALES);
        cieAntes = simulacion.getInventario().getRecurso(TipoRecurso.CIENCIA);
        pobAntes = simulacion.getInventario().getRecurso(TipoRecurso.POBLACION);

        simulacion.avanzarTick();

        assertEquals(eneAntes, simulacion.getInventario().getRecurso(TipoRecurso.ENERGIA), 1e-6, "La luna no debe generar energia");
        assertEquals(cieAntes, simulacion.getInventario().getRecurso(TipoRecurso.CIENCIA), 1e-6, "La luna no debe generar ciencia");
        assertEquals(pobAntes, simulacion.getInventario().getRecurso(TipoRecurso.POBLACION), 1e-6, "La luna no debe generar poblacion");
        assertEquals(minAntes + ConfiguracionSimulacion.PRODUCCION_LUNA_MINERALES, simulacion.getInventario().getRecurso(TipoRecurso.MINERALES), 1e-6);

        // 4. Verificar que el crecimiento es lento (valores por tick <= 0.05)
        assertTrue(ConfiguracionSimulacion.PRODUCCION_ESTRELLA_ENERGIA <= 0.05);
        assertTrue(ConfiguracionSimulacion.PRODUCCION_PLANETA_MINERALES <= 0.05);
        assertTrue(ConfiguracionSimulacion.PRODUCCION_PLANETA_CIENCIA <= 0.05);
        assertTrue(ConfiguracionSimulacion.PRODUCCION_PLANETA_POBLACION <= 0.05);
        assertTrue(ConfiguracionSimulacion.PRODUCCION_LUNA_MINERALES <= 0.05);
    }

    @Test
    void testAbsorcionAgujeroNegroMasa() {
        org.example.game.simulacion.SimulacionSolar simulacion = new org.example.game.simulacion.SimulacionSolar(1920, 1080);
        simulacion.limpiarCuerpos();

        AgujeroNegro bh = CuerpoCelesteFactory.crearAgujeroNegroEstelar(0, 0, 1.0);
        double masaInicialBH = bh.getMasa();
        simulacion.agregarCuerpo(bh);

        Planeta presa = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 2.0, Vector2D.cero());
        double masaPresa = presa.getMasa();
        simulacion.agregarCuerpo(presa);

        // Avanzar tick de colisión / absorción
        simulacion.avanzarTick();

        // Presa debe haber sido absorbida
        assertFalse(simulacion.getSistemaSolar().getCuerpos().contains(presa));
        assertEquals(masaInicialBH + masaPresa, bh.getMasa(), 1e-6);
    }

    @Test
    void testAbsorcionesEnCadenaAgujeroNegro() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        AgujeroNegro bh = CuerpoCelesteFactory.crearAgujeroNegroEstelar(0, 0, 1.0);
        cuerpos.add(bh);

        Planeta p1 = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        Planeta p2 = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 2.0, Vector2D.cero());
        cuerpos.add(p1);
        cuerpos.add(p2);

        double masaTotalEsperada = bh.getMasa() + p1.getMasa() + p2.getMasa();

        motorFisica.avanzarPaso(cuerpos);

        assertEquals(1, cuerpos.size());
        assertTrue(cuerpos.contains(bh));
        assertEquals(masaTotalEsperada, bh.getMasa(), 1e-6);
    }

    @Test
    void testIntegracionMovimientoMasaInvalidaProtegida() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Planeta p = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        p.setMasa(-10.0); // Caso borde masa no positiva
        cuerpos.add(p);

        assertDoesNotThrow(() -> motorFisica.avanzarPaso(cuerpos));
        assertFalse(Double.isNaN(p.getPosicionX()));
        assertFalse(Double.isNaN(p.getPosicionY()));
    }

    @Test
    void testGeneracionPasivaMinerales() {
        org.example.game.simulacion.SimulacionSolar simulacion = new org.example.game.simulacion.SimulacionSolar(1920, 1080);
        simulacion.limpiarCuerpos();

        // Sin cuerpos en el sistema: no se deben generar minerales de la nada
        double mineralesAntes = simulacion.getInventario().getRecurso(TipoRecurso.MINERALES);
        simulacion.avanzarTick();
        assertEquals(mineralesAntes, simulacion.getInventario().getRecurso(TipoRecurso.MINERALES), 1e-6);

        // Agregando una luna: genera minerales por tick
        Luna luna = CuerpoCelesteFactory.crearLuna(0, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(luna);
        simulacion.avanzarTick();
        double mineralesDespues = simulacion.getInventario().getRecurso(TipoRecurso.MINERALES);

        assertTrue(mineralesDespues > mineralesAntes, "La luna debe generar Minerales cada tick");
        assertEquals(
                mineralesAntes + org.example.game.simulacion.ConfiguracionSimulacion.PRODUCCION_LUNA_MINERALES,
                mineralesDespues,
                1e-6
        );
    }

    @Test
    void testMultiplicadorProduccionDinamicaCivilizacion() {
        Planeta p = CuerpoCelesteFactory.crearPlanetaRocoso(150, 0, 1.0, Vector2D.cero());
        p.desarrollarCivilizacion();
        var civ = p.getCivilizacion();

        // Estado inicial próspero
        double multInicial = civ.calcularMultiplicadorProduccion();
        assertTrue(multInicial >= 1.0);

        // Simulamos efecto de catástrofe reciente
        civ.setTicksEfectoEventoReciente(50);
        double multPenalizado = civ.calcularMultiplicadorProduccion();
        assertTrue(multPenalizado < multInicial, "Una catástrofe reciente debe penalizar la producción");

        // Verificamos recuperación tras transcurrir los ticks
        for (int i = 0; i < 50; i++) {
            civ.actualizar(List.of(p));
        }
        assertEquals(0, civ.getTicksEfectoEventoReciente());
    }

    @Test
    void testSinergiaCivilizacionesMultiples() {
        org.example.game.simulacion.SimulacionSolar simulacion = new org.example.game.simulacion.SimulacionSolar(1920, 1080);
        simulacion.limpiarCuerpos();

        // Crear una civilización
        Planeta p1 = CuerpoCelesteFactory.crearPlanetaRocoso(150, 0, 1.0, Vector2D.cero());
        p1.desarrollarCivilizacion();
        simulacion.agregarCuerpo(p1);

        double minAntes1 = simulacion.getInventario().getRecurso(TipoRecurso.MINERALES);
        simulacion.avanzarTick();
        double delta1 = simulacion.getInventario().getRecurso(TipoRecurso.MINERALES) - minAntes1;

        // Añadir una segunda civilización
        Planeta p2 = CuerpoCelesteFactory.crearPlanetaRocoso(250, 0, 1.0, Vector2D.cero());
        p2.desarrollarCivilizacion();
        simulacion.agregarCuerpo(p2);

        double minAntes2 = simulacion.getInventario().getRecurso(TipoRecurso.MINERALES);
        simulacion.avanzarTick();
        double delta2 = simulacion.getInventario().getRecurso(TipoRecurso.MINERALES) - minAntes2;

        // Dos civilizaciones deben producir al menos el doble que una sola
        assertTrue(delta2 >= 1.99 * delta1, "Multiples planetas deben producir recursos proporcionalmente");
    }

    @Test
    void testNotificacionesDesacopladas() {
        org.example.game.simulacion.SimulacionSolar simulacion = new org.example.game.simulacion.SimulacionSolar(1920, 1080);
        List<org.example.game.simulacion.MensajeEvento> recibidos = new ArrayList<>();

        simulacion.agregarListenerEvento(recibidos::add);

        // Notificación manual
        simulacion.notificarEvento("Prueba de alerta", org.example.game.simulacion.MensajeEvento.TipoMensaje.ADVERTENCIA);
        assertEquals(1, recibidos.size());
        assertEquals("Prueba de alerta", recibidos.get(0).mensaje());
        assertEquals(org.example.game.simulacion.MensajeEvento.TipoMensaje.ADVERTENCIA, recibidos.get(0).tipo());

        // Notificación de spawn con fondos insuficientes
        simulacion.getInventario().getRecursoObj(TipoRecurso.ENERGIA).setCantidad(0);
        simulacion.getInventario().getRecursoObj(TipoRecurso.MINERALES).setCantidad(0);
        boolean spawneado = simulacion.spawnCuerpo(TipoCuerpo.PLANETA_ROCOSO, 100, 100, 1.0, Vector2D.cero());
        assertFalse(spawneado);
        assertTrue(recibidos.size() >= 2);
        assertTrue(recibidos.get(recibidos.size() - 1).mensaje().contains("Recursos insuficientes"));
    }

    @Test
    void testIntercepcionEscudoRecursos() {
        org.example.game.simulacion.SimulacionSolar simulacion = new org.example.game.simulacion.SimulacionSolar(1920, 1080);
        simulacion.limpiarCuerpos();

        EscudoProtector escudo = CuerpoCelesteFactory.crearEscudo(0, 0, 1.0, Vector2D.cero());
        Meteorito meteorito = CuerpoCelesteFactory.crearMeteoritoJugadorFisica(10, 0, Vector2D.cero());

        simulacion.agregarCuerpo(escudo);
        simulacion.agregarCuerpo(meteorito);

        double energiaAntes = simulacion.getInventario().getRecurso(TipoRecurso.ENERGIA);
        double cienciaAntes = simulacion.getInventario().getRecurso(TipoRecurso.CIENCIA);

        simulacion.avanzarTick();

        // El meteorito debe haber sido vaporizado por el escudo
        assertFalse(simulacion.getSistemaSolar().getCuerpos().contains(meteorito));
        assertTrue(simulacion.getSistemaSolar().getCuerpos().contains(escudo));

        // Consumo de energía y recompensa de ciencia
        double energiaDespues = simulacion.getInventario().getRecurso(TipoRecurso.ENERGIA);
        double cienciaDespues = simulacion.getInventario().getRecurso(TipoRecurso.CIENCIA);

        assertTrue(cienciaDespues > cienciaAntes, "La defensa con escudo debe otorgar Ciencia");
    }

    @Test
    void testHUDRecursosTopWidgets() {
        org.example.game.simulacion.SimulacionSolar simulacion = new org.example.game.simulacion.SimulacionSolar(1920, 1080);
        org.example.game.ui.HUDRecursosTop hud = new org.example.game.ui.HUDRecursosTop(simulacion);

        var items = hud.getItemsRecursos();
        assertEquals(TipoRecurso.values().length, items.size(), "Debe haber un widget por cada TipoRecurso");

        for (TipoRecurso tipo : TipoRecurso.values()) {
            assertTrue(items.containsKey(tipo));
            var widget = items.get(tipo);
            assertEquals(tipo.nombre.toUpperCase(), widget.getNombreLabelText(), "El label debe identificarse con el nombre del TipoRecurso en mayusculas");
            assertNotNull(widget.getValorLabelText());
        }
    }

    @Test
    void testSinEmojisEnTextos() {
        for (TipoRecurso tr : TipoRecurso.values()) {
            assertFalse(tr.nombre.matches(".*[\\p{So}\\p{Cn}].*"), "El nombre de TipoRecurso no debe contener emojis: " + tr.nombre);
            assertFalse(tr.icono.matches(".*[\\p{So}\\p{Cn}].*"), "El icono de TipoRecurso no debe contener emojis: " + tr.icono);
        }
    }

    @Test
    void testGravedadProporcionalAMasa() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        // Cuerpo muy pesado (m1 = 1.90e27 kg) y cuerpo liviano (m2 = 5.97e24 kg) -> ratio ~318:1
        Planeta pesado = CuerpoCelesteFactory.crearPlanetaGaseoso(-100, 0, 1.0, Vector2D.cero());
        Planeta liviano = CuerpoCelesteFactory.crearPlanetaRocoso(100, 0, 1.0, Vector2D.cero());
        cuerpos.add(pesado);
        cuerpos.add(liviano);

        // Desactivar asistencia orbital para aislar puramente la atracción newtoniana
        motorFisica.setAsistenciaOrbital(0.0);

        double xInicialPesado = pesado.getPosicionX();
        double xInicialLiviano = liviano.getPosicionX();

        motorFisica.avanzarPaso(cuerpos);

        double dispPesado = Math.abs(pesado.getPosicionX() - xInicialPesado);
        double dispLiviano = Math.abs(liviano.getPosicionX() - xInicialLiviano);

        // La aceleración es inversamente proporcional a la masa
        assertTrue(dispLiviano > dispPesado * 50, "El cuerpo liviano debe acelerar mucho más que el pesado");
        assertTrue(pesado.getPosicionX() > xInicialPesado, "El pesado es atraído hacia el liviano");
        assertTrue(liviano.getPosicionX() < xInicialLiviano, "El liviano es atraído hacia el pesado");
    }

    @Test
    void testColisionColapsoMutuoMasasIguales() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        // Dos planetas con masas casi iguales (ratio 1.1 <= 1.3)
        Planeta p1 = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        Planeta p2 = CuerpoCelesteFactory.crearPlanetaRocoso(10, 0, 1.1, Vector2D.cero());
        cuerpos.add(p1);
        cuerpos.add(p2);

        double masaTotalOriginal = p1.getMasa() + p2.getMasa();
        motorFisica.avanzarPaso(cuerpos);

        // Ambos planetas deben destruirse mutuamente
        assertFalse(cuerpos.contains(p1));
        assertFalse(cuerpos.contains(p2));

        // Deben generarse fragmentos de escombro (3 por cada cuerpo = 6)
        assertEquals(ConfiguracionSimulacion.FRAGMENTOS_POR_COLAPSO * 2, cuerpos.size());
        for (CuerpoCeleste c : cuerpos) {
            assertTrue(c instanceof Meteorito, "Los escombros deben ser meteoritos");
        }

        // Conservación aproximada de masa: (m1 + m2) * (1 - disipación)
        double masaTotalFragmentos = 0;
        for (CuerpoCeleste c : cuerpos) {
            masaTotalFragmentos += c.getMasa();
        }
        double masaEsperada = masaTotalOriginal * (1.0 - ConfiguracionSimulacion.FRACCION_MASA_DISIPADA);
        assertEquals(masaEsperada, masaTotalFragmentos, masaEsperada * 1e-5);
    }

    @Test
    void testColisionAsimetricaMasasDistintas() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        // Dos planetas con ratio 4.0 (mayor a 1.3 y menor a 8.0)
        Planeta mayor = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 4.0, Vector2D.cero());
        Planeta menor = CuerpoCelesteFactory.crearPlanetaRocoso(10, 0, 1.0, Vector2D.cero());
        cuerpos.add(mayor);
        cuerpos.add(menor);

        double masaInicialMayor = mayor.getMasa();
        double masaInicialMenor = menor.getMasa();

        motorFisica.avanzarPaso(cuerpos);

        // El menor se destruye por completo; el mayor sobrevive
        assertFalse(cuerpos.contains(menor));
        assertTrue(cuerpos.contains(mayor));

        // El mayor pierde exactamente lo que valía el menor
        double masaEsperadaMayor = masaInicialMayor - masaInicialMenor;
        assertEquals(masaEsperadaMayor, mayor.getMasa(), 1e-6);

        // Se generan fragmentos proporcionales a la masa del menor
        assertEquals(1 + ConfiguracionSimulacion.FRAGMENTOS_POR_COLAPSO, cuerpos.size());

        double masaEscombros = 0;
        for (CuerpoCeleste c : cuerpos) {
            if (c instanceof Meteorito) {
                masaEscombros += c.getMasa();
            }
        }
        double masaEsperadaEscombros = masaInicialMenor * (1.0 - ConfiguracionSimulacion.FRACCION_MASA_DISIPADA);
        assertEquals(masaEsperadaEscombros, masaEscombros, masaEsperadaEscombros * 1e-5);
    }

    @Test
    void testColisionAbsorcionTotalProporcionExtrema() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        // Proporción >= 8.0: estrella devorando planeta
        Estrella sol = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        Planeta planeta = CuerpoCelesteFactory.crearPlanetaRocoso(10, 0, 1.0, Vector2D.cero());
        cuerpos.add(sol);
        cuerpos.add(planeta);

        double masaInicialSol = sol.getMasa();
        double masaInicialPlaneta = planeta.getMasa();

        motorFisica.avanzarPaso(cuerpos);

        // El sol absorbe al planeta y no genera escombros
        assertFalse(cuerpos.contains(planeta));
        assertTrue(cuerpos.contains(sol));
        assertEquals(1, cuerpos.size(), "La absorción total no debe generar escombros");
        assertEquals(masaInicialSol + masaInicialPlaneta, sol.getMasa(), 1e-6);
    }

    @Test
    void testColisionMeteoritosDestruccionMutua() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Meteorito m1 = CuerpoCelesteFactory.crearMeteoritoJugadorFisica(0, 0, Vector2D.cero());
        Meteorito m2 = CuerpoCelesteFactory.crearMeteoritoJugadorFisica(2, 0, Vector2D.cero());
        cuerpos.add(m1);
        cuerpos.add(m2);

        motorFisica.avanzarPaso(cuerpos);

        // Destrucción mutua simple sin nuevos fragmentos
        assertTrue(cuerpos.isEmpty(), "Dos meteoritos al colisionar deben destruirse sin generar más escombros");
    }

    @Test
    void testEscudoProtectorNoColapsaPlaneta() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        EscudoProtector escudo = CuerpoCelesteFactory.crearEscudo(0, 0, 1.0, Vector2D.cero());
        Planeta planeta = CuerpoCelesteFactory.crearPlanetaRocoso(5, 0, 1.0, Vector2D.cero());
        cuerpos.add(escudo);
        cuerpos.add(planeta);

        motorFisica.avanzarPaso(cuerpos);

        assertTrue(cuerpos.contains(escudo));
        assertTrue(cuerpos.contains(planeta));
        assertEquals(2, cuerpos.size());
    }

    @Test
    void testRadioDinamicoPorMasa() {
        Planeta p = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        double radio1x = p.getRadio();
        assertEquals(15.0, radio1x, 1e-6);

        // Aumentar masa x8 -> radio x2 (raíz cúbica)
        p.setMasa(p.getMasa() * 8.0);
        assertEquals(30.0, p.getRadio(), 1e-6);

        // Reducir masa a 1/8 -> radio / 2
        p.setMasa(TipoCuerpo.PLANETA_ROCOSO.masaBase * 0.125);
        assertEquals(7.5, p.getRadio(), 1e-6);
    }

    @Test
    void testSistemaInicialSoloEstrellaPlanetaRocosoYLuna() {
        org.example.game.simulacion.SimulacionSolar sim = new org.example.game.simulacion.SimulacionSolar(1920, 1080);
        List<CuerpoCeleste> cuerpos = sim.getSistemaSolar().getCuerpos();

        // Debe haber exactamente 3 cuerpos
        assertEquals(3, cuerpos.size(), "El sistema inicial debe contener unicamente 3 cuerpos");

        boolean tieneEstrella = false;
        boolean tienePlanetaRocoso = false;
        boolean tieneLuna = false;

        for (CuerpoCeleste c : cuerpos) {
            if (c instanceof Estrella && c.getTipoCuerpo() == TipoCuerpo.ESTRELLA) {
                tieneEstrella = true;
            } else if (c instanceof Planeta && c.getTipoCuerpo() == TipoCuerpo.PLANETA_ROCOSO) {
                tienePlanetaRocoso = true;
            } else if (c instanceof Luna && c.getTipoCuerpo() == TipoCuerpo.LUNA) {
                tieneLuna = true;
            } else {
                fail("No debe haber otros cuerpos en el sistema inicial: " + c.getTipoCuerpo());
            }
        }

        assertTrue(tieneEstrella, "Debe estar presente la estrella");
        assertTrue(tienePlanetaRocoso, "Debe estar presente el planeta rocoso");
        assertTrue(tieneLuna, "Debe estar presente la luna");
    }

    @Test
    void testRadioAtraccionJerarquiaYMenorAreaLuna() {
        Estrella estrella = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        Planeta planeta1x = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        Planeta planeta2x = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 2.0, Vector2D.cero());
        Planeta planeta5x = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 5.0, Vector2D.cero());
        Luna luna1x = CuerpoCelesteFactory.crearLuna(0, 0, 1.0, Vector2D.cero());

        // La luna debe tener un área menor que el planeta y la estrella
        assertTrue(luna1x.getRadioAtraccion() < planeta1x.getRadioAtraccion(), "La Luna debe tener un área menor que el planeta");
        assertTrue(planeta1x.getRadioAtraccion() < estrella.getRadioAtraccion(), "El planeta debe tener un área menor que la estrella");

        // El planeta 5x debe tener mayor área de atracción que el de 2x
        assertTrue(planeta5x.getRadioAtraccion() > planeta2x.getRadioAtraccion(), "Planeta 5x debe tener mayor área que 2x");
        assertTrue(planeta2x.getRadioAtraccion() > planeta1x.getRadioAtraccion(), "Planeta 2x debe tener mayor área que 1x");
    }

    @Test
    void testAtraccionColapsoPlaneta5xYPlaneta2x() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Planeta p5x = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 5.0, Vector2D.cero());
        // Colocado a 70px (dentro del radio de atracción del 5x que es > 120px)
        Planeta p2x = CuerpoCelesteFactory.crearPlanetaRocoso(70, 0, 2.0, Vector2D.cero());
        cuerpos.add(p5x);
        cuerpos.add(p2x);

        List<String> mensajes = new ArrayList<>();
        motorFisica.setListenerMensaje(mensajes::add);

        // Avanzar un paso y comprobar aceleraciones proporcionales
        motorFisica.avanzarPaso(cuerpos);

        // El planeta 2x debe acelerar hacia la izquierda (-X) con mayor intensidad que el 5x hacia la derecha (+X)
        assertTrue(p2x.getVelocidad().x < 0, "El planeta 2x debe ser atraído hacia el 5x");
        assertTrue(p5x.getVelocidad().x > 0, "El planeta 5x se mueve hacia el 2x cumpliendo acción y reacción");
        assertTrue(Math.abs(p2x.getVelocidad().x) > Math.abs(p5x.getVelocidad().x), "El 2x debe moverse más rápido que el 5x");

        // Avanzar la simulación hasta el colapso
        for (int i = 0; i < 50 && cuerpos.contains(p2x); i++) {
            motorFisica.avanzarPaso(cuerpos);
        }

        // El 2x debe haber colapsado y sido eliminado
        assertFalse(cuerpos.contains(p2x), "El planeta 2x debió colapsar al estar dentro del área de atracción del 5x");
        assertTrue(cuerpos.contains(p5x), "El planeta mayor sobrevive perdiendo masa");
    }

    @Test
    void testPlanetasADistanciaRazonableNoColapsan() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Planeta p5x = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 5.0, Vector2D.cero());
        // Distancia razonable fuera de peligro (250px > 123px)
        Planeta p2x = CuerpoCelesteFactory.crearPlanetaRocoso(250, 0, 2.0, Vector2D.cero());
        cuerpos.add(p5x);
        cuerpos.add(p2x);

        for (int i = 0; i < 40; i++) {
            motorFisica.avanzarPaso(cuerpos);
        }

        // Ninguno colapsa a distancia razonable
        assertTrue(cuerpos.contains(p5x), "El planeta 5x debe seguir existiendo");
        assertTrue(cuerpos.contains(p2x), "El planeta 2x debe seguir existiendo a distancia razonable");
    }

    @Test
    void testSolAtraeYComePlanetaCercano() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Estrella sol = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        // Planeta colocado a 45px (dentro de los 85px del radio de atracción del sol)
        Planeta planetaCercano = CuerpoCelesteFactory.crearPlanetaRocoso(45, 0, 1.0, Vector2D.cero());
        cuerpos.add(sol);
        cuerpos.add(planetaCercano);

        double masaSolInicial = sol.getMasa();
        List<String> mensajes = new ArrayList<>();
        motorFisica.setListenerMensaje(mensajes::add);

        for (int i = 0; i < 60 && cuerpos.contains(planetaCercano); i++) {
            motorFisica.avanzarPaso(cuerpos);
        }

        // El sol debe haber absorbido/comido al planeta
        assertFalse(cuerpos.contains(planetaCercano), "El planeta muy cercano al sol debe ser devorado/comido");
        assertTrue(sol.getMasa() > masaSolInicial, "El sol absorbió la masa del planeta");
        boolean mensajeAbsorcion = mensajes.stream().anyMatch(m -> m.contains("absorbió por gravedad"));
        assertTrue(mensajeAbsorcion, "Debe registrarse el mensaje de absorción por gravedad");
    }

    @Test
    void testSolNoComePlanetaADistanciaOrbital() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Estrella sol = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        Vector2D posTierra = new Vector2D(160, 0); // 160px > 85px de radio de colapso
        Vector2D velTierra = CuerpoCelesteFactory.calcularVelocidadOrbitalVector(sol, posTierra, false);
        Planeta tierra = CuerpoCelesteFactory.crearPlanetaRocoso(posTierra.x, posTierra.y, 1.0, velTierra);
        cuerpos.add(sol);
        cuerpos.add(tierra);

        for (int i = 0; i < 60; i++) {
            motorFisica.avanzarPaso(cuerpos);
        }

        assertTrue(cuerpos.contains(tierra), "La tierra a distancia orbital no debe colapsar contra el sol");
    }

    @Test
    void testLunaOrbitaPlanetaYNotificaMensaje() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Planeta planeta = CuerpoCelesteFactory.crearPlanetaPersonalizado("planeta rocoso1", TipoCuerpo.PLANETA_ROCOSO.masaBase, 0, 0, TipoCuerpo.PLANETA_ROCOSO, Vector2D.cero());
        // Luna dentro de su área de atracción (distancia 30px <= 40px)
        Luna luna = CuerpoCelesteFactory.crearLunaPersonalizada("luna1", TipoCuerpo.LUNA.masaBase, 30, 0, Vector2D.cero());
        cuerpos.add(planeta);
        cuerpos.add(luna);

        List<String> mensajes = new ArrayList<>();
        motorFisica.setListenerMensaje(mensajes::add);

        // Avanzar 1 paso: debe capturarse en órbita y notificar el mensaje exacto
        motorFisica.avanzarPaso(cuerpos);

        assertTrue(luna.estaOrbitando(planeta), "La luna debe entrar en órbita del planeta");
        assertTrue(mensajes.contains("luna1 esta orbitando planeta rocoso1"),
                "Debe mostrarse el mensaje exacto: luna1 esta orbitando planeta rocoso1. Mensajes recibidos: " + mensajes);

        // Verificar que en los siguientes pasos se mantiene en órbita sin colapsar ni ser destruida
        for (int i = 0; i < 40; i++) {
            motorFisica.avanzarPaso(cuerpos);
        }
        assertTrue(cuerpos.contains(luna), "La luna debe mantenerse en órbita estable");
        assertTrue(cuerpos.contains(planeta), "El planeta debe seguir existiendo");
    }

    @Test
    void testDosLunasCercanasColapsan() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        // Dos lunas colocadas muy cerca (20px < 40px de área de atracción)
        Luna luna1 = CuerpoCelesteFactory.crearLunaPersonalizada("luna1", TipoCuerpo.LUNA.masaBase, 0, 0, Vector2D.cero());
        Luna luna2 = CuerpoCelesteFactory.crearLunaPersonalizada("luna2", TipoCuerpo.LUNA.masaBase, 20, 0, Vector2D.cero());
        cuerpos.add(luna1);
        cuerpos.add(luna2);

        List<String> mensajes = new ArrayList<>();
        motorFisica.setListenerMensaje(mensajes::add);

        for (int i = 0; i < 40 && (cuerpos.contains(luna1) || cuerpos.contains(luna2)); i++) {
            motorFisica.avanzarPaso(cuerpos);
        }

        assertFalse(cuerpos.contains(luna1), "Luna 1 debe haber colapsado");
        assertFalse(cuerpos.contains(luna2), "Luna 2 debe haber colapsado");
        boolean colapsoMutuo = mensajes.stream().anyMatch(m -> m.contains("colapsaron en escombros"));
        assertTrue(colapsoMutuo, "Debe emitir mensaje de colisión catastrófica mutua de escombros");
    }

    @Test
    void testSoloExistenCuatroRecursos() {
        TipoRecurso[] recursos = TipoRecurso.values();
        assertEquals(4, recursos.length, "Solo deben existir exactamente 4 recursos");

        List<String> nombres = java.util.Arrays.stream(recursos).map(r -> r.nombre.toLowerCase()).toList();
        assertTrue(nombres.contains("minerales"));
        assertTrue(nombres.contains("energia"));
        assertTrue(nombres.contains("poblacion"));
        assertTrue(nombres.contains("ciencia"));
    }

    @Test
    void testEspecificacionMaterialesParaPonerYPorTick() {
        // Estrella
        var costoEstrella = InventarioJugador.getCostosBase(TipoCuerpo.ESTRELLA);
        assertTrue(costoEstrella.containsKey(TipoRecurso.MINERALES));
        assertTrue(costoEstrella.containsKey(TipoRecurso.ENERGIA));
        var prodEstrella = InventarioJugador.getProduccionBasePorTick(TipoCuerpo.ESTRELLA);
        assertTrue(prodEstrella.containsKey(TipoRecurso.ENERGIA));
        assertTrue(prodEstrella.containsKey(TipoRecurso.MINERALES));

        // Planeta Rocoso
        var costoPlaneta = InventarioJugador.getCostosBase(TipoCuerpo.PLANETA_ROCOSO);
        assertTrue(costoPlaneta.containsKey(TipoRecurso.MINERALES));
        assertTrue(costoPlaneta.containsKey(TipoRecurso.ENERGIA));
        var prodPlaneta = InventarioJugador.getProduccionBasePorTick(TipoCuerpo.PLANETA_ROCOSO);
        assertTrue(prodPlaneta.containsKey(TipoRecurso.MINERALES));
        assertTrue(prodPlaneta.containsKey(TipoRecurso.CIENCIA));
        assertTrue(prodPlaneta.containsKey(TipoRecurso.POBLACION));

        // Luna
        var costoLuna = InventarioJugador.getCostosBase(TipoCuerpo.LUNA);
        assertTrue(costoLuna.containsKey(TipoRecurso.MINERALES));
        var prodLuna = InventarioJugador.getProduccionBasePorTick(TipoCuerpo.LUNA);
        assertTrue(prodLuna.containsKey(TipoRecurso.MINERALES));
        assertEquals(1, prodLuna.size(), "La luna solo genera minerales");
    }

    @Test
    void testVentanaDescripcionCuerpoLore() {
        String loreSol = org.example.game.ui.VentanaDescripcionCuerpo.obtenerLore(TipoCuerpo.ESTRELLA);
        assertNotNull(loreSol);
        assertFalse(loreSol.isBlank());

        String lorePlaneta = org.example.game.ui.VentanaDescripcionCuerpo.obtenerLore(TipoCuerpo.PLANETA_ROCOSO);
        assertNotNull(lorePlaneta);
        assertFalse(lorePlaneta.isBlank());

        String loreLuna = org.example.game.ui.VentanaDescripcionCuerpo.obtenerLore(TipoCuerpo.LUNA);
        assertNotNull(loreLuna);
        assertFalse(loreLuna.isBlank());
    }

    @Test
    void testFormatoHotbarResumen() {
        String resumenEstrella = org.example.game.ui.BarraInventarioHotbar.formatearResumenCosto(TipoCuerpo.ESTRELLA);
        assertTrue(resumenEstrella.contains("Requiere:"));
        String prodEstrella = org.example.game.ui.BarraInventarioHotbar.formatearResumenProduccion(TipoCuerpo.ESTRELLA);
        assertTrue(prodEstrella.contains("Da/t:"));

        String tooltip = org.example.game.ui.BarraInventarioHotbar.formatearTooltipDetallado(TipoCuerpo.PLANETA_ROCOSO, 2);
        assertTrue(tooltip.contains("MATERIALES NECESARIOS PARA PONERLO"));
        assertTrue(tooltip.contains("MATERIALES QUE TE DA POR TICK"));
        assertFalse(tooltip.contains("Arrastrar para lanzar"), "No debe sugerir arrastrar para lanzar");
    }

    @Test
    void testCostosYProduccionEscaladosPorMasa() {
        // A masa 1.0x
        var costoBase = InventarioJugador.getCostosEscalados(TipoCuerpo.PLANETA_ROCOSO, 1.0);
        var prodBase = InventarioJugador.getProduccionEscaladaPorTick(TipoCuerpo.PLANETA_ROCOSO, 1.0);

        // A masa 2.0x
        var costoDoble = InventarioJugador.getCostosEscalados(TipoCuerpo.PLANETA_ROCOSO, 2.0);
        var prodDoble = InventarioJugador.getProduccionEscaladaPorTick(TipoCuerpo.PLANETA_ROCOSO, 2.0);

        // El costo a 2.0x debe ser mayor (escala superlineal 2.0^1.5 ≈ 2.82x)
        double minCostoBase = costoBase.get(TipoRecurso.MINERALES);
        double minCostoDoble = costoDoble.get(TipoRecurso.MINERALES);
        assertTrue(minCostoDoble > minCostoBase * 2.0, "El costo a 2.0x debe ser mayor que el doble debido al exponente 1.5");

        // La producción a 2.0x debe duplicar la producción base
        double prodMinBase = prodBase.get(TipoRecurso.MINERALES);
        double prodMinDoble = prodDoble.get(TipoRecurso.MINERALES);
        assertEquals(prodMinBase * 2.0, prodMinDoble, 0.001, "La producción debe escalar linealmente con el factor de masa");
    }

    @Test
    void testSimulacionColocacionConNombreYMasaPersonalizados() {
        org.example.game.simulacion.SimulacionSolar sim = new org.example.game.simulacion.SimulacionSolar(1920, 1080);
        sim.getInventario().agregarRecurso(TipoRecurso.MINERALES, 10000);
        sim.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 10000);

        String nombreEsperado = "Planeta-Alfa-77";
        double factorMasaEsperado = 2.5;

        sim.entrarModoColocacion(TipoCuerpo.PLANETA_ROCOSO, factorMasaEsperado, nombreEsperado);
        assertEquals(nombreEsperado, sim.getNombreColocacion());
        assertEquals(factorMasaEsperado, sim.getFactorMasaColocacion());

        sim.actualizarPosicionPreview(1300, 450);
        sim.confirmarColocacionAutoOrbita();

        // Verificar que el cuerpo fue creado con el nombre y masa configurados
        CuerpoCeleste cuerpoCreado = sim.getSistemaSolar().getCuerpos().stream()
                .filter(c -> nombreEsperado.equals(c.getNombre()))
                .findFirst()
                .orElse(null);

        assertNotNull(cuerpoCreado, "El cuerpo celeste debio crearse con el nombre personalizado");
        assertEquals(TipoCuerpo.PLANETA_ROCOSO, cuerpoCreado.getTipoCuerpo());
        assertEquals(TipoCuerpo.PLANETA_ROCOSO.masaBase * factorMasaEsperado, cuerpoCreado.getMasa(), 1e20,
                "La masa del cuerpo debio ser escalada por el factor elegido");
    }

    @Test
    void testNombresPorDefectoVentana() {
        String nombreSol = org.example.game.ui.VentanaDescripcionCuerpo.obtenerNombrePorDefecto(TipoCuerpo.ESTRELLA);
        assertNotNull(nombreSol);
        assertFalse(nombreSol.isBlank());

        String nombrePlaneta = org.example.game.ui.VentanaDescripcionCuerpo.obtenerNombrePorDefecto(TipoCuerpo.PLANETA_ROCOSO);
        assertNotNull(nombrePlaneta);
        assertFalse(nombrePlaneta.isBlank());

        String nombreLuna = org.example.game.ui.VentanaDescripcionCuerpo.obtenerNombrePorDefecto(TipoCuerpo.LUNA);
        assertNotNull(nombreLuna);
        assertFalse(nombreLuna.isBlank());
    }

    @Test
    void testRechazoPorRecursosInsuficientesAlAumentarMasa() {
        InventarioJugador inv = new InventarioJugador();
        // Con recursos iniciales estándar puede crear un planeta a 1.0x
        assertTrue(inv.puedeCrear(TipoCuerpo.PLANETA_ROCOSO, 1.0));

        // A masa 10.0x los costos escalan exponencialmente (10^1.5 ≈ 31.6x) superando los recursos disponibles
        assertFalse(inv.puedeCrear(TipoCuerpo.PLANETA_ROCOSO, 10.0),
                "No debe permitir crear con masa extrema si no alcanzan los recursos");
    }
}

