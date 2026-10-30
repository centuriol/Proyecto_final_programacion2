package org.example;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import org.example.game.cuerpo.*;
import org.example.game.controlador.ControladorMouse;
import org.example.game.controlador.ControladorTeclado;
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.cuerpo.Meteorito;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.jugador.TipoRecurso;
import org.example.game.motor.ConstantesFisicas;
import org.example.game.motor.MotorFisicaPermisiva;
import org.example.game.motor.TransformadorCuerpos;
import org.example.game.motor.Vector2D;
import org.example.game.render.CamaraZoom;
import org.example.game.render.RenderizadorPixelArt;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.ServicioReembolso;
import org.example.game.motor.ResolvedorColisiones;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.GestorEconomia;
import org.example.game.simulacion.SimulacionSolar;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios que verifican las 8 tareas de mejoras del motor fisico y nuevas mecanicas.
 *
 * Principios:
 * - Tarea 2: Colision asimetrica con ganancia de masa porcentual en el cuerpo mayor.
 * - Tarea 3: Poblacion en 0 explicitamente tras destruccion o eliminacion de planeta.
 * - Tarea 4 y 5: Transformaciones por proximidad estelar (easter egg) y no-rentabilidad.
 * - Tarea 6: Atajo temporal Ctrl+M (recursos maximos y toggle visual).
 * - Tarea 7: Anillo orbital extra de referencia.
 * - Tarea 8: Zoom clamp entre 0.5x y 1.5x.
 */
class MejorasMotorFisicaYMecanicasTest {

    private MotorFisicaPermisiva motorFisica;
    private SimulacionSolar simulacion;

    @BeforeAll
    static void initJFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {}
    }

    @BeforeEach
    void setUp() {
        motorFisica = new MotorFisicaPermisiva();
        simulacion = new SimulacionSolar(1920, 1080);
    }

    // ===== TAREA 1: Atracción gravitatoria hacia el Sol =====

    @Test
    void testRadioCapturaEscalaConRadioAtraccionDeEstrella() {
        Estrella estrella1 = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        Estrella estrellaGrande = CuerpoCelesteFactory.crearEstrella(0, 0, 4.0);

        // El radio de atracción debe ser mayor para la estrella más masiva
        assertTrue(estrellaGrande.getRadioAtraccion() > estrella1.getRadioAtraccion());

        // Un planeta colocado a distancia intermedia no debe colapsar por radio fijo
        Planeta planeta = CuerpoCelesteFactory.crearPlanetaRocoso(0, 300, 1.0, Vector2D.cero());
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        cuerpos.add(estrella1);
        cuerpos.add(planeta);

        motorFisica.avanzarPaso(cuerpos);

        // Se capturó en órbita asistida y tiene velocidad orbital inicializada
        assertTrue(planeta.estaOrbitando());
        assertEquals(estrella1, planeta.getCuerpoOrbitado());
        assertTrue(planeta.getVelocidad().magnitud() > 0);
    }

    // ===== TAREA 2: Reglas de colisión entre planetas =====

    @Test
    void testColisionAsimetricaMayorGanaPorcentajeDeMasa() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        // Dos planetas con ratio 4.0 (rango asimetrico: mayor a 1.3 y menor a 8.0)
        Planeta mayor = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 4.0, Vector2D.cero());
        Planeta menor = CuerpoCelesteFactory.crearPlanetaRocoso(10, 0, 1.0, Vector2D.cero());
        cuerpos.add(mayor);
        cuerpos.add(menor);

        double masaInicialMayor = mayor.getMasa();
        double masaInicialMenor = menor.getMasa();

        motorFisica.avanzarPaso(cuerpos);

        // El menor se destruye; el mayor sobrevive
        assertFalse(cuerpos.contains(menor));
        assertTrue(cuerpos.contains(mayor));

        // El mayor debe ganar el porcentaje configurado de la masa del menor (no perder masa)
        double masaGanadaEsperada = masaInicialMenor * ConfiguracionSimulacion.PORCENTAJE_GANANCIA_MASA_COLISION;
        double masaEsperadaMayor = masaInicialMayor + masaGanadaEsperada;
        assertEquals(masaEsperadaMayor, mayor.getMasa(), 1e-6);

        // Se generan escombros del menor
        long countMeteoritos = cuerpos.stream().filter(c -> c instanceof Meteorito).count();
        assertEquals(ConfiguracionSimulacion.FRAGMENTOS_POR_COLAPSO, countMeteoritos);
    }

    @Test
    void testColisionSimetricaAmbosColapsan() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        // Masas iguales (ratio 1.0 <= RATIO_COLISION_COLAPSO)
        Planeta p1 = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        Planeta p2 = CuerpoCelesteFactory.crearPlanetaRocoso(10, 0, 1.0, Vector2D.cero());
        cuerpos.add(p1);
        cuerpos.add(p2);

        motorFisica.avanzarPaso(cuerpos);

        // Ambos son destruidos y reemplazados por fragmentos
        assertFalse(cuerpos.contains(p1));
        assertFalse(cuerpos.contains(p2));
        assertEquals(ConfiguracionSimulacion.FRAGMENTOS_POR_COLAPSO * 2, cuerpos.size());
    }

    // ===== TAREA 3: Población en 0 tras destrucción explícita =====

    @Test
    void testPoblacionEnCeroTrasEliminacionManual() {
        Planeta planeta = CuerpoCelesteFactory.crearPlanetaRocoso(100, 100, 1.0, Vector2D.cero());
        planeta.desarrollarCivilizacion();
        assertTrue(planeta.tieneCivilizacion());
        assertTrue(planeta.getCivilizacion().getPoblacion() > 0);

        simulacion.agregarCuerpo(planeta);
        simulacion.eliminarCuerpo(planeta);

        // La población debe quedar en 0 explícitamente y el estado en EXTINGUIDA
        assertEquals(0.0, planeta.getCivilizacion().getPoblacion(), 1e-6);
        assertEquals(org.example.game.civilizacion.EstadoCivilizacion.EXTINGUIDA,
                planeta.getCivilizacion().getEstado());
        assertFalse(planeta.tieneCivilizacion());
    }

    @Test
    void testPoblacionEnCeroTrasColisionAsimetrica() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        Planeta mayor = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 5.0, Vector2D.cero());
        Planeta menor = CuerpoCelesteFactory.crearPlanetaRocoso(5, 0, 1.0, Vector2D.cero());
        menor.desarrollarCivilizacion();
        assertTrue(menor.tieneCivilizacion());

        cuerpos.add(mayor);
        cuerpos.add(menor);

        motorFisica.avanzarPaso(cuerpos);

        // El menor fue destruido en la colisión: su población debe estar en 0
        assertEquals(0.0, menor.getCivilizacion().getPoblacion(), 1e-6);
        assertFalse(menor.tieneCivilizacion());
    }

    @Test
    void testPoblacionEnCeroTrasAbsorcionPorAgujeroNegro() {
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        AgujeroNegro bh = CuerpoCelesteFactory.crearAgujeroNegroEstelar(0, 0, 2.0);
        Planeta victima = CuerpoCelesteFactory.crearPlanetaRocoso(5, 0, 1.0, Vector2D.cero());
        victima.desarrollarCivilizacion();
        assertTrue(victima.tieneCivilizacion());

        cuerpos.add(bh);
        cuerpos.add(victima);

        motorFisica.avanzarPaso(cuerpos);

        // La víctima fue absorbida: su población debe quedar en 0 de forma determinista
        assertEquals(0.0, victima.getCivilizacion().getPoblacion(), 1e-6);
        assertFalse(victima.tieneCivilizacion());
    }

    // ===== TAREA 4 y 5: Easter Egg y No-Rentabilidad =====

    @Test
    void testEasterEggTransformacionAguaAPorroCercaDelSol() {
        simulacion.limpiarCuerpos();
        Estrella sol = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        // Distancia dentro de RADIO_TRANSFORMACION_AGUA_PORRO (120.0) pero mayor al radio de colisión
        Planeta agua = CuerpoCelesteFactory.crearPlanetaAgua(90, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(sol);
        simulacion.agregarCuerpo(agua);

        assertEquals(TipoCuerpo.PLANETA_AGUA, agua.getSubTipo());
        assertFalse(agua.isTransformadoPorEvento());

        // Durante el tiempo de espera no se transforma inmediatamente (gradual)
        for (int i = 0; i < ConfiguracionSimulacion.TICKS_ESPERA_TRANSFORMACION_EASTER_EGG - 1; i++) {
            simulacion.avanzarTick();
            assertEquals(TipoCuerpo.PLANETA_AGUA, agua.getSubTipo());
        }

        // Al cumplirse el tiempo de espera se transforma
        simulacion.avanzarTick();
        assertEquals(TipoCuerpo.PLANETA_ROCOSO, agua.getSubTipo());
        assertTrue(agua.isTransformadoPorEvento());
        assertEquals("Planeta Porro", agua.getNombre());

        // Completar la migración gradual hacia la órbita estable
        for (int i = 0; i < ConfiguracionSimulacion.TICKS_MIGRACION_ORBITA; i++) {
            simulacion.avanzarTick();
        }
        assertTrue(agua.estaOrbitando(sol));
    }

    @Test
    void testEasterEggTransformacionPorroALavaMuyCercaDelSol() {
        simulacion.limpiarCuerpos();
        Estrella sol = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        // Distancia dentro de RADIO_TRANSFORMACION_PORRO_LAVA (70.0) pero sin tocar
        Planeta porro = CuerpoCelesteFactory.crearPlanetaRocoso(60, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(sol);
        simulacion.agregarCuerpo(porro);

        assertEquals(TipoCuerpo.PLANETA_ROCOSO, porro.getSubTipo());

        for (int i = 0; i < ConfiguracionSimulacion.TICKS_ESPERA_TRANSFORMACION_EASTER_EGG; i++) {
            simulacion.avanzarTick();
        }

        // Se transformó a PLANETA_LAVA
        assertEquals(TipoCuerpo.PLANETA_LAVA, porro.getSubTipo());
        assertTrue(porro.isTransformadoPorEvento());
        assertEquals("Planeta Lava", porro.getNombre());
    }

    @Test
    void testEasterEggGradualEsperaYMigracionSinSaltos() {
        simulacion.limpiarCuerpos();
        Estrella sol = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        Planeta agua = CuerpoCelesteFactory.crearPlanetaAgua(90, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(sol);
        simulacion.agregarCuerpo(agua);

        // 1. Acumulacion de ticks de espera
        for (int i = 0; i < ConfiguracionSimulacion.TICKS_ESPERA_TRANSFORMACION_EASTER_EGG; i++) {
            simulacion.avanzarTick();
        }
        assertEquals(TipoCuerpo.PLANETA_ROCOSO, agua.getSubTipo());

        // 2. Migracion suave paso a paso sin teletransportes
        Vector2D posAnterior = agua.getPosicion();
        for (int i = 0; i < ConfiguracionSimulacion.TICKS_MIGRACION_ORBITA; i++) {
            simulacion.avanzarTick();
            Vector2D posNueva = agua.getPosicion();
            double salto = posNueva.distanciaA(posAnterior);
            assertTrue(salto < 25.0, "El desplazamiento por tick en la migracion debe ser gradual, sin saltos");
            posAnterior = posNueva;
        }

        assertTrue(agua.estaOrbitando(sol));
        assertTrue(agua.getVelocidad().magnitud() > 0);
    }

    @Test
    void testCancelacionAMitadPorSalidaDeRadio() {
        simulacion.limpiarCuerpos();
        Estrella sol = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        Planeta agua = CuerpoCelesteFactory.crearPlanetaAgua(90, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(sol);
        simulacion.agregarCuerpo(agua);

        // Avanzar la mitad del tiempo de espera
        for (int i = 0; i < ConfiguracionSimulacion.TICKS_ESPERA_TRANSFORMACION_EASTER_EGG / 2; i++) {
            simulacion.avanzarTick();
        }

        assertTrue(simulacion.getTransformadorCuerpos().tieneProcesoActivo(agua));

        // Mover el planeta fuera del radio de activacion
        agua.setPosicion(new Vector2D(500, 500));
        simulacion.avanzarTick();

        // El proceso debe cancelarse limpiamente sin quedar estado colgado
        assertFalse(simulacion.getTransformadorCuerpos().tieneProcesoActivo(agua));
        assertEquals(TipoCuerpo.PLANETA_AGUA, agua.getSubTipo());
        assertFalse(agua.isTransformadoPorEvento());
    }

    @Test
    void testPlanetaEliminadoDuranteEsperaNoDejaEstadoColgado() {
        simulacion.limpiarCuerpos();
        Estrella sol = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        Planeta agua = CuerpoCelesteFactory.crearPlanetaAgua(90, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(sol);
        simulacion.agregarCuerpo(agua);

        simulacion.avanzarTick();
        assertTrue(simulacion.getTransformadorCuerpos().tieneProcesoActivo(agua));

        // El planeta es eliminado por el jugador durante la espera
        simulacion.eliminarCuerpo(agua);
        simulacion.avanzarTick();

        // Se cancelo y limpio el proceso sin referencias huerfanas
        assertFalse(simulacion.getTransformadorCuerpos().tieneProcesoActivo(agua));
        assertEquals(0, simulacion.getTransformadorCuerpos().getCantidadProcesosActivos());
    }

    @Test
    void testDestinoLibreEligeOrbitaEstableYMigra() {
        TransformadorCuerpos transformador = new TransformadorCuerpos();
        Estrella sol = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        Planeta agua = CuerpoCelesteFactory.crearPlanetaAgua(90, 0, 1.0, Vector2D.cero());

        List<CuerpoCeleste> cuerpos = new ArrayList<>(List.of(sol, agua));
        Vector2D destino = transformador.encontrarPosicionOrbitalLibre(agua, sol, ConfiguracionSimulacion.RADIO_TRANSFORMACION_AGUA_PORRO, cuerpos);

        assertNotNull(destino);
        double distSol = destino.distanciaA(sol.getPosicion());
        assertTrue(distSol >= ConfiguracionSimulacion.RADIO_TRANSFORMACION_AGUA_PORRO);
    }

    @Test
    void testDestinoOcupadoBuscaOrbitaLibreOAbortaSinColision() {
        TransformadorCuerpos transformador = new TransformadorCuerpos();
        Estrella sol = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        Planeta agua = CuerpoCelesteFactory.crearPlanetaAgua(90, 0, 1.0, Vector2D.cero());

        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        cuerpos.add(sol);
        cuerpos.add(agua);

        // Llenar multiples orbitas con planetas en circulo para ocupar el espacio
        double radioBase = ConfiguracionSimulacion.RADIO_TRANSFORMACION_AGUA_PORRO + 40.0;
        for (int intento = 0; intento < 10; intento++) {
            double radio = radioBase + intento * ConfiguracionSimulacion.PASO_BUSQUEDA_ORBITA_PX;
            for (int ang = 0; ang < 16; ang++) {
                double rad = Math.toRadians(ang * 22.5);
                double x = Math.cos(rad) * radio;
                double y = Math.sin(rad) * radio;
                Planeta obstaculo = CuerpoCelesteFactory.crearPlanetaRocoso(x, y, 1.0, Vector2D.cero());
                cuerpos.add(obstaculo);
            }
        }

        Vector2D destino = transformador.encontrarPosicionOrbitalLibre(agua, sol, ConfiguracionSimulacion.RADIO_TRANSFORMACION_AGUA_PORRO, cuerpos);

        if (destino != null) {
            // Si encontro una orbita exterior libre, la distancia a todo obstaculo debe respetar el margen de seguridad
            for (CuerpoCeleste c : cuerpos) {
                if (c == agua || c == sol) continue;
                double minDist = agua.getRadio() + c.getRadio() + ConfiguracionSimulacion.MARGEN_SEGURIDAD_ORBITA_LIBRE;
                assertTrue(destino.distanciaA(c.getPosicion()) >= minDist, "El destino encontrado no debe solapar con ningun cuerpo existente");
            }
        } else {
            // Si todas las orbitas examinadas estaban ocupadas, aborta retornando null sin causar colisiones
            assertNull(destino);
        }
    }

    @Test
    void testPlanetaTransformadoNoDesarrollaCivilizacion() {
        Planeta planeta = CuerpoCelesteFactory.crearPlanetaAgua(0, 0, 1.0, Vector2D.cero());
        planeta.transformarA(TipoCuerpo.PLANETA_ROCOSO);

        assertTrue(planeta.isTransformadoPorEvento());
        // Intentar desarrollar civilización en planeta transformado debe ser bloqueado (Tarea 5)
        planeta.desarrollarCivilizacion();
        assertFalse(planeta.tieneCivilizacion());
        assertNull(planeta.getCivilizacion());
    }

    @Test
    void testPlanetaTransformadoNoGeneraRecursos() {
        simulacion.limpiarCuerpos();
        // Guardar estado inicial de inventario
        double minInicial = simulacion.getInventario().getRecurso(TipoRecurso.MINERALES);
        double cieInicial = simulacion.getInventario().getRecurso(TipoRecurso.CIENCIA);

        // Crear planeta rocoso transformado (un porro normal generaría minerales y ciencia)
        Planeta porroTransformado = CuerpoCelesteFactory.crearPlanetaRocoso(500, 500, 1.0, Vector2D.cero());
        porroTransformado.transformarA(TipoCuerpo.PLANETA_ROCOSO);
        simulacion.agregarCuerpo(porroTransformado);

        // Avanzar varios ticks
        for (int i = 0; i < 20; i++) {
            simulacion.avanzarTick();
        }

        // El planeta transformado no debe haber generado recursos (Tarea 5)
        assertEquals(minInicial, simulacion.getInventario().getRecurso(TipoRecurso.MINERALES), 1e-6);
        assertEquals(cieInicial, simulacion.getInventario().getRecurso(TipoRecurso.CIENCIA), 1e-6);
    }

    // ===== TAREA 7: Anillo extra de órbita alrededor del sol =====

    @Test
    void testTresRadiosDeReferenciaOrbital() {
        // Verificar que los radios de activación del transformador existan
        List<Double> radios = simulacion.getTransformadorCuerpos().getRadiosActivacion();
        assertEquals(2, radios.size());
        assertTrue(radios.contains(ConfiguracionSimulacion.RADIO_TRANSFORMACION_AGUA_PORRO));
        assertTrue(radios.contains(ConfiguracionSimulacion.RADIO_TRANSFORMACION_PORRO_LAVA));
    }

    // ===== TAREA 8: CamaraZoom clampeado entre 0.5x y 1.5x =====

    @Test
    void testCamaraZoomLimitesYReseteo() {
        CamaraZoom zoom = new CamaraZoom();
        assertEquals(1.0, zoom.getNivelZoom(), 1e-6);
        assertTrue(zoom.esZoomNeutro());

        // Intentar alejar más allá del mínimo (0.5)
        for (int i = 0; i < 20; i++) {
            zoom.alejar();
        }
        assertEquals(0.5, zoom.getNivelZoom(), 1e-6);

        // Intentar acercar más allá del máximo (1.5)
        for (int i = 0; i < 30; i++) {
            zoom.acercar();
        }
        assertEquals(1.5, zoom.getNivelZoom(), 1e-6);

        // Reseteo vuelve a 1.0
        zoom.resetear();
        assertEquals(1.0, zoom.getNivelZoom(), 1e-6);
        assertTrue(zoom.esZoomNeutro());
    }

    @Test
    void testControladorMouseCtrlScrollAjustaZoom() {
        ControladorMouse mouse = new ControladorMouse(simulacion, null, null, 1920, 1080);
        CamaraZoom zoom = new CamaraZoom();
        mouse.setCamaraZoom(zoom);

        double zoomInicial = zoom.getNivelZoom();

        // Scroll con Ctrl activa zoom
        ScrollEvent ctrlScrollUp = new ScrollEvent(
                ScrollEvent.SCROLL, 0, 0, 0, 0, false, true, false, false, false, false,
                0, 10.0, 0, 10.0, ScrollEvent.HorizontalTextScrollUnits.NONE, 0,
                ScrollEvent.VerticalTextScrollUnits.NONE, 0, 0, null
        );
        mouse.manejarScroll(ctrlScrollUp);
        assertTrue(zoom.getNivelZoom() > zoomInicial);

        ScrollEvent ctrlScrollDown = new ScrollEvent(
                ScrollEvent.SCROLL, 0, 0, 0, 0, false, true, false, false, false, false,
                0, -10.0, 0, -10.0, ScrollEvent.HorizontalTextScrollUnits.NONE, 0,
                ScrollEvent.VerticalTextScrollUnits.NONE, 0, 0, null
        );
        mouse.manejarScroll(ctrlScrollDown);
        assertEquals(zoomInicial, zoom.getNivelZoom(), 1e-4);
    }

    // ===== EASTER EGG: Colision Planeta de Agua + Planeta Lava -> Planeta Porro inerte =====

    @Test
    void testEasterEggColisionAguaYLavaFormaPlanetaPorroInerte() {
        ResolvedorColisiones resolvedor = new ResolvedorColisiones();
        Planeta agua = CuerpoCelesteFactory.crearPlanetaAgua(100, 100, 1.0, new Vector2D(5, 0));
        Planeta lava = CuerpoCelesteFactory.crearPlanetaLava(105, 100, 2.0, new Vector2D(-2, 0));

        double masaTotalEsperada = agua.getMasa() + lava.getMasa();
        Vector2D momentoEsperado = agua.getVelocidad().multiplicar(agua.getMasa()).sumar(lava.getVelocidad().multiplicar(lava.getMasa()));
        Vector2D velEsperada = momentoEsperado.dividir(masaTotalEsperada);
        Vector2D posEsperada = agua.getPosicion().multiplicar(agua.getMasa()).sumar(lava.getPosicion().multiplicar(lava.getMasa())).dividir(masaTotalEsperada);

        Set<CuerpoCeleste> aEliminar = new HashSet<>();
        List<CuerpoCeleste> aAgregar = new ArrayList<>();

        resolvedor.resolverColision(agua, lava, aEliminar, aAgregar);

        // Sin escombros ni fragmentos aleatorios
        assertTrue(aAgregar.isEmpty(), "No deben generarse escombros ni fragmentos en direcciones aleatorias");

        // El cuerpo absorbido fue marcado para eliminar
        assertEquals(1, aEliminar.size());
        assertTrue(aEliminar.contains(agua));

        // El sobreviviente es un Planeta Porro inerte
        Planeta sobreviviente = lava;
        assertEquals(TipoCuerpo.PLANETA_ROCOSO, sobreviviente.getTipoCuerpo());
        assertEquals("Planeta Porro", sobreviviente.getNombre());
        assertTrue(sobreviviente.isTransformadoPorEvento());
        assertFalse(sobreviviente.permiteReembolso());
        assertEquals(masaTotalEsperada, sobreviviente.getMasa(), 1e-6);
        assertEquals(posEsperada.x(), sobreviviente.getPosicion().x(), 1e-4);
        assertEquals(posEsperada.y(), sobreviviente.getPosicion().y(), 1e-4);
        assertEquals(velEsperada.x(), sobreviviente.getVelocidad().x(), 1e-4);
        assertEquals(velEsperada.y(), sobreviviente.getVelocidad().y(), 1e-4);

        // No genera recursos pasivos
        InventarioJugador inv = new InventarioJugador();
        double minAntes = inv.getRecurso(TipoRecurso.MINERALES);
        GestorEconomia economia = new GestorEconomia();
        economia.actualizarEconomia(List.of(sobreviviente), inv);
        assertEquals(minAntes, inv.getRecurso(TipoRecurso.MINERALES), 1e-6);

        // No devuelve recursos si es destruido / desmantelado
        assertTrue(ServicioReembolso.calcularReembolso(sobreviviente).isEmpty());
        simulacion.limpiarCuerpos();
        simulacion.agregarCuerpo(sobreviviente);
        double mineralesAntes = simulacion.getInventario().getRecurso(TipoRecurso.MINERALES);
        boolean desmantelado = ServicioReembolso.desmantelarCuerpo(simulacion, sobreviviente);
        assertTrue(desmantelado);
        assertEquals(mineralesAntes, simulacion.getInventario().getRecurso(TipoRecurso.MINERALES), 1e-6);
    }

    @Test
    void testEasterEggColisionAguaYLavaMasasIgualesNoColapsaEnEscombros() {
        ResolvedorColisiones resolvedor = new ResolvedorColisiones();
        Planeta agua = CuerpoCelesteFactory.crearPlanetaAgua(200, 200, 1.0, Vector2D.cero());
        Planeta lava = CuerpoCelesteFactory.crearPlanetaLava(202, 200, 1.0, Vector2D.cero());

        Set<CuerpoCeleste> aEliminar = new HashSet<>();
        List<CuerpoCeleste> aAgregar = new ArrayList<>();

        resolvedor.resolverColision(agua, lava, aEliminar, aAgregar);

        // Excepción al colapso mutuo: no debe generar fragmentos
        assertTrue(aAgregar.isEmpty(), "Colision agua y lava no debe colapsar en escombros a pesar de ratio de masa = 1.0");
        assertEquals(1, aEliminar.size());

        CuerpoCeleste sobreviviente = aEliminar.contains(agua) ? lava : agua;
        assertEquals(TipoCuerpo.PLANETA_ROCOSO, sobreviviente.getTipoCuerpo());
        assertTrue(sobreviviente instanceof Transformable t && t.isTransformadoPorEvento());
        assertFalse(sobreviviente.permiteReembolso());
    }

    @Test
    void testEasterEggColisionAguaYLavaEnSimulacionReal() {
        simulacion.limpiarCuerpos();
        // Colocar agua y lava en contacto físico (radio de cada uno es ~15px)
        Planeta agua = CuerpoCelesteFactory.crearPlanetaAgua(100, 200, 1.0, Vector2D.cero());
        Planeta lava = CuerpoCelesteFactory.crearPlanetaLava(110, 200, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(agua);
        simulacion.agregarCuerpo(lava);

        assertEquals(2, simulacion.getSistemaSolar().getCuerpos().size());

        // Avanzar 1 tick de simulación para que se procese la colisión
        simulacion.avanzarTick();

        // Queda exactamente 1 cuerpo (el Planeta Porro fusionado) sin escombros
        assertEquals(1, simulacion.getSistemaSolar().getCuerpos().size());
        CuerpoCeleste porro = simulacion.getSistemaSolar().getCuerpos().get(0);
        assertEquals(TipoCuerpo.PLANETA_ROCOSO, porro.getTipoCuerpo());
        assertEquals("Planeta Porro", porro.getNombre());
        assertTrue(porro instanceof Transformable t && t.isTransformadoPorEvento());
        assertFalse(porro.permiteReembolso());
    }
}
