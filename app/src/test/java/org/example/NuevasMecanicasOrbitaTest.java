package org.example;

import javafx.application.Platform;
import org.example.game.cuerpo.AgujeroNegro;
import org.example.game.civilizacion.CivilizacionComponent;
import org.example.game.colonizacion.NaveColonizadora;
import org.example.game.cuerpo.*;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;
import org.example.game.motor.ConstantesFisicas;
import org.example.game.motor.Vector2D;
import org.example.game.progresion.ElementoDesbloqueable;
import org.example.game.progresion.RamaProgresion;
import org.example.game.progresion.SistemaProgresion;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.GeneradorEventosCosmicos;
import org.example.game.simulacion.SimulacionSolar;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de pruebas unitarias para todas las mecánicas nuevas de la actualización "Órbita":
 * - Árbol tecnológico en 3 ramas independientes (Planetas, Masa, Cantidad de planetas).
 * - Costos escalonados por nivel en cada rama (100, 200, 300, 400, 500 CIE).
 * - Límite inicial de 3 planetas y expansión mediante investigación.
 * - Dinámica de población logística y colonización espacial interplanetaria.
 * - Escudo protector planetario con satélite orbital y absorción de impactos.
 * - Agujero negro con atracción gravitatoria universal y extrema sin importar la distancia.
 * - Meteoritos masivos con tiempo de gracia inicial y destrucción directa de planetas iniciales indefensos.
 * - Creación de agujeros negros por colisión estelar y ciclo de Game Over.
 *
 * Principios SOLID:
 * - SRP: Pruebas desacopladas por subsistema.
 * - LSP: Verificación polimórfica sin dependencias en instanceof o switch de control.
 */
public class NuevasMecanicasOrbitaTest {

    private SimulacionSolar simulacion;

    @BeforeAll
    static void initJavaFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {}
    }

    @BeforeEach
    void setUp() {
        simulacion = new SimulacionSolar(1920, 1080);
        simulacion.limpiarCuerpos();
    }

    // =========================================================================
    // 1. ÁRBOL TECNOLÓGICO: 3 RAMAS INDEPENDIENTES Y COSTOS ESCALONADOS
    // =========================================================================

    @Test
    void testArbolTecnologicoTresRamasYCostosEscalonados() {
        SistemaProgresion prog = simulacion.getProgresion();
        InventarioJugador inv = simulacion.getInventario();

        // 1. Estado inicial
        assertEquals(0, prog.getNivel(RamaProgresion.PLANETAS));
        assertEquals(0, prog.getNivel(RamaProgresion.MASA));
        assertEquals(0, prog.getNivel(RamaProgresion.CANTIDAD_PLANETAS));

        assertEquals(0.3, prog.getLimiteMasaActual(), 1e-6);
        assertEquals(3, prog.getLimitePlanetas());

        assertTrue(prog.estaDesbloqueado(TipoCuerpo.ESTRELLA));
        assertTrue(prog.estaDesbloqueado(TipoCuerpo.PLANETA_AGUA));
        assertFalse(prog.estaDesbloqueado(TipoCuerpo.LUNA));
        assertFalse(prog.estaDesbloqueado(TipoCuerpo.PLANETA_ROCOSO));
        assertFalse(prog.estaDesbloqueado(TipoCuerpo.PLANETA_LAVA));
        assertFalse(prog.tieneEscudoDesbloqueado());

        // Cada rama cuesta 100 CIE en su primer nivel
        assertEquals(100.0, prog.getCostoSiguiente(RamaProgresion.PLANETAS));
        assertEquals(100.0, prog.getCostoSiguiente(RamaProgresion.MASA));
        assertEquals(100.0, prog.getCostoSiguiente(RamaProgresion.CANTIDAD_PLANETAS));

        // 2. El jugador puede mejorar lo que quiera en el orden que desee
        // Primero mejora MASA por 100 CIE
        inv.agregarRecurso(TipoRecurso.CIENCIA, 100.0);
        assertTrue(prog.investigar(RamaProgresion.MASA, inv));
        assertEquals(1, prog.getNivel(RamaProgresion.MASA));
        assertEquals(0.6, prog.getLimiteMasaActual(), 1e-6);

        // El siguiente nivel de MASA ahora cuesta 500 CIE (escalado mayor)
        assertEquals(500.0, prog.getCostoSiguiente(RamaProgresion.MASA));

        // Las otras dos ramas siguen costando 100 CIE en su nivel 1
        assertEquals(100.0, prog.getCostoSiguiente(RamaProgresion.PLANETAS));
        assertEquals(100.0, prog.getCostoSiguiente(RamaProgresion.CANTIDAD_PLANETAS));

        // 3. Mejorar CANTIDAD_PLANETAS por 100 CIE
        inv.agregarRecurso(TipoRecurso.CIENCIA, 100.0);
        assertTrue(prog.investigar(RamaProgresion.CANTIDAD_PLANETAS, inv));
        assertEquals(1, prog.getNivel(RamaProgresion.CANTIDAD_PLANETAS));
        assertEquals(4, prog.getLimitePlanetas());
        assertEquals(500.0, prog.getCostoSiguiente(RamaProgresion.CANTIDAD_PLANETAS));

        // 4. Mejorar PLANETAS: Nivel 1 (Luna, 100 CIE)
        inv.agregarRecurso(TipoRecurso.CIENCIA, 100.0);
        assertTrue(prog.investigar(RamaProgresion.PLANETAS, inv));
        assertTrue(prog.estaDesbloqueado(TipoCuerpo.LUNA));
        assertEquals(500.0, prog.getCostoSiguiente(RamaProgresion.PLANETAS));

        // Nivel 2: Planeta Porro (500 CIE)
        inv.agregarRecurso(TipoRecurso.CIENCIA, 500.0);
        assertTrue(prog.investigar(RamaProgresion.PLANETAS, inv));
        assertTrue(prog.estaDesbloqueado(TipoCuerpo.PLANETA_ROCOSO));
        assertEquals(1500.0, prog.getCostoSiguiente(RamaProgresion.PLANETAS));

        // Nivel 3: Satélite (1500 CIE)
        inv.agregarRecurso(TipoRecurso.CIENCIA, 1500.0);
        assertTrue(prog.investigar(RamaProgresion.PLANETAS, inv));
        assertTrue(prog.estaDesbloqueado(TipoCuerpo.SATELITE));
        assertEquals(3500.0, prog.getCostoSiguiente(RamaProgresion.PLANETAS));

        // Nivel 4: Escudo (3500 CIE)
        inv.agregarRecurso(TipoRecurso.CIENCIA, 3500.0);
        assertTrue(prog.investigar(RamaProgresion.PLANETAS, inv));
        assertTrue(prog.tieneEscudoDesbloqueado());
        assertEquals(8000.0, prog.getCostoSiguiente(RamaProgresion.PLANETAS));

        // Nivel 5: Planeta Lava (8000 CIE)
        inv.agregarRecurso(TipoRecurso.CIENCIA, 8000.0);
        assertTrue(prog.investigar(RamaProgresion.PLANETAS, inv));
        assertTrue(prog.estaDesbloqueado(TipoCuerpo.PLANETA_LAVA));
        assertTrue(prog.esNivelMaximo(RamaProgresion.PLANETAS));
    }

    @Test
    void testLimiteDePlanetasEnSimulacion() {
        simulacion.getInventario().agregarRecurso(TipoRecurso.MINERALES, 50000);
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 50000);

        // Inicialmente límite es 3 planetas
        assertEquals(3, simulacion.getProgresion().getLimitePlanetas());

        // Spawnear 3 planetas de agua (desbloqueados al inicio)
        assertTrue(simulacion.spawnCuerpo(TipoCuerpo.PLANETA_AGUA, 100, 0, 0.3, Vector2D.cero()));
        assertTrue(simulacion.spawnCuerpo(TipoCuerpo.PLANETA_AGUA, 200, 0, 0.3, Vector2D.cero()));
        assertTrue(simulacion.spawnCuerpo(TipoCuerpo.PLANETA_AGUA, 300, 0, 0.3, Vector2D.cero()));
        assertEquals(3, simulacion.getCantidadPlanetasActuales());

        // Intentar colocar un 4to planeta debe ser rechazado por límite
        boolean cuartoSpawneado = simulacion.spawnCuerpo(TipoCuerpo.PLANETA_AGUA, 400, 0, 0.3, Vector2D.cero());
        assertFalse(cuartoSpawneado, "No debe permitir superar el límite de planetas simultáneos");
        assertEquals(3, simulacion.getCantidadPlanetasActuales());

        // Investigar la rama de CANTIDAD_PLANETAS (100 CIE) expande el límite a 4
        simulacion.getInventario().agregarRecurso(TipoRecurso.CIENCIA, 100.0);
        assertTrue(simulacion.getProgresion().investigar(RamaProgresion.CANTIDAD_PLANETAS, simulacion.getInventario()));
        assertEquals(4, simulacion.getProgresion().getLimitePlanetas());

        // Ahora el 4to planeta sí se puede spawnear
        assertTrue(simulacion.spawnCuerpo(TipoCuerpo.PLANETA_AGUA, 400, 0, 0.3, Vector2D.cero()));
        assertEquals(4, simulacion.getCantidadPlanetasActuales());
    }

    @Test
    void testSpawnCuerpoRechazaBloqueados() {
        simulacion.getInventario().agregarRecurso(TipoRecurso.MINERALES, 5000);
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 5000);

        // Luna bloqueada al inicio
        boolean exitoLuna = simulacion.spawnCuerpo(TipoCuerpo.LUNA, 100, 100, 0.3, Vector2D.cero());
        assertFalse(exitoLuna, "No debe permitir spawnear cuerpos bloqueados");

        // Planeta de Agua permitido al inicio
        boolean exitoAgua = simulacion.spawnCuerpo(TipoCuerpo.PLANETA_AGUA, 100, 100, 0.3, Vector2D.cero());
        assertTrue(exitoAgua, "Debe permitir spawnear Planeta de Agua desde el inicio");
    }

    @Test
    void testSolProduceCienciaInicial() {
        simulacion.getProgresion().desbloquearTodo();
        Estrella sol = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        simulacion.agregarCuerpo(sol);

        assertEquals(0.0, simulacion.getInventario().getRecurso(TipoRecurso.CIENCIA));
        for (int i = 0; i < 50; i++) {
            simulacion.avanzarTick();
        }
        assertTrue(simulacion.getInventario().getRecurso(TipoRecurso.CIENCIA) > 0.0,
                "El Sol debe generar ciencia pasiva");
    }

    // =========================================================================
    // 2. POBLACIÓN Y COLONIZACIÓN
    // =========================================================================

    @Test
    void testSoloPlanetaPorroNaceConCivilizacion() {
        Planeta porro = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        Planeta agua = CuerpoCelesteFactory.crearPlanetaAgua(100, 0, 1.0, Vector2D.cero());
        Planeta lava = CuerpoCelesteFactory.crearPlanetaLava(200, 0, 1.0, Vector2D.cero());

        assertTrue(porro.tieneCivilizacion(), "Planeta Porro debe nacer con civilización");
        assertFalse(agua.tieneCivilizacion(), "Planeta Agua debe nacer sin civilización");
        assertFalse(lava.tieneCivilizacion(), "Planeta Lava debe nacer sin civilización");

        assertTrue(agua.puedeSerColonizado(), "Planeta Agua debe poder ser colonizado");
        assertTrue(lava.puedeSerColonizado(), "Planeta Lava debe poder ser colonizado");
    }

    @Test
    void testCrecimientoLogisticoYCapacidad() {
        Estrella sol = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        Vector2D posPorro = new Vector2D(150, 0);
        Vector2D velPorro = CuerpoCelesteFactory.calcularVelocidadOrbitalVector(sol, posPorro, false);
        Planeta porro = CuerpoCelesteFactory.crearPlanetaRocoso(posPorro.x, posPorro.y, 1.0, velPorro);
        CivilizacionComponent civ = porro.getCivilizacion();
        assertNotNull(civ);

        double capMaxima = civ.getPoblacionMaxima();
        assertTrue(capMaxima > 0);

        double pobInicial = civ.getPoblacion();
        for (int i = 0; i < 100; i++) {
            civ.actualizar(List.of(sol, porro));
        }

        assertTrue(civ.getPoblacion() >= pobInicial, "La población debe crecer según el modelo logístico");
        assertTrue(civ.getPoblacion() <= capMaxima, "La población no debe superar la capacidad de carga");
        assertTrue(civ.getPorcentajeCapacidad() >= 0.0 && civ.getPorcentajeCapacidad() <= 100.0);
    }

    @Test
    void testMecanicaColonizacionYTransferencia() {
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.MINERALES, 1000);
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 1000);

        Planeta origen = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        CivilizacionComponent civOrigen = origen.getCivilizacion();
        civOrigen.setPoblacion(civOrigen.getPoblacionMaxima() * 0.85);

        Planeta destino = CuerpoCelesteFactory.crearPlanetaAgua(500, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(origen);
        simulacion.agregarCuerpo(destino);

        double pobAntesOrigen = civOrigen.getPoblacion();
        boolean lanzada = simulacion.getGestorColonizacion().lanzar(origen, destino, simulacion.getInventario(), simulacion::notificarEvento, 0);
        assertTrue(lanzada, "La misión colonizadora debe lanzarse");

        assertEquals(1, simulacion.getGestorColonizacion().getNaves().size());
        NaveColonizadora nave = simulacion.getGestorColonizacion().getNaves().get(0);
        assertEquals(pobAntesOrigen * 0.10, nave.getPoblacionColonos(), 1.0, "La nave debe llevar el 10% de la población");
        assertEquals(pobAntesOrigen * 0.90, civOrigen.getPoblacion(), 1.0, "El origen debe ceder el 10%");

        // Avanzar la simulación hasta que la nave alcance al destino
        while (!simulacion.getGestorColonizacion().getNaves().isEmpty()) {
            simulacion.avanzarTick();
        }

        assertTrue(destino.tieneCivilizacion(), "El planeta destino debe haberse colonizado");
        assertTrue(destino.getCivilizacion().getPoblacion() > 0, "El destino debe tener la población transferida");
    }

    @Test
    void testMisionColonizadoraSePierdeSiDestinoEsDestruido() {
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.MINERALES, 1000);
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 1000);

        Planeta origen = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        origen.getCivilizacion().setPoblacion(origen.getCivilizacion().getPoblacionMaxima() * 0.90);
        Planeta destino = CuerpoCelesteFactory.crearPlanetaAgua(1000, 0, 1.0, Vector2D.cero());

        simulacion.agregarCuerpo(origen);
        simulacion.agregarCuerpo(destino);

        assertTrue(simulacion.getGestorColonizacion().lanzar(origen, destino, simulacion.getInventario(), simulacion::notificarEvento, 0));

        // Destruir el destino en tránsito
        simulacion.eliminarCuerpo(destino);

        // Avanzar el gestor
        simulacion.getGestorColonizacion().actualizar(simulacion::notificarEvento, 1);
        assertTrue(simulacion.getGestorColonizacion().getNaves().isEmpty(),
                "La nave debe perderse al no encontrar su destino");
    }

    // =========================================================================
    // 3. ESCUDO PLANETARIO Y SATÉLITE DE CIENCIA
    // =========================================================================

    @Test
    void testActivacionEscudoYSateliteOrbital() {
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 500);

        Planeta planeta = CuerpoCelesteFactory.crearPlanetaAgua(0, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(planeta);

        assertFalse(planeta.tieneEscudo());
        assertTrue(planeta.puedeRecibirEscudo());

        boolean activado = ServicioEscudo.activar(simulacion, planeta);
        assertTrue(activado);
        assertTrue(planeta.tieneEscudo());
        assertEquals(5, planeta.getGolpesEscudoRestantes());

        // Verificar que se desplegó el satélite de ciencia orbitándolo
        CuerpoCeleste satelite = simulacion.getSistemaSolar().getCuerpos().stream()
                .filter(c -> c.getTipoCuerpo() == TipoCuerpo.SATELITE && c.estaOrbitando(planeta))
                .findFirst()
                .orElse(null);

        assertNotNull(satelite);
        assertEquals(ConfiguracionSimulacion.PRODUCCION_SATELITE_CIENCIA, satelite.getTipoCuerpo().getProduccionBase().get(TipoRecurso.CIENCIA), 1e-6);
        assertEquals(ConfiguracionSimulacion.PRODUCCION_SATELITE_ENERGIA, satelite.getTipoCuerpo().getProduccionBase().get(TipoRecurso.ENERGIA), 1e-6);
        assertNull(satelite.getTipoCuerpo().getProduccionBase().get(TipoRecurso.MINERALES));

        // Al destruir el planeta, el satélite también debe desaparecer
        simulacion.eliminarCuerpo(planeta);
        assertFalse(simulacion.getSistemaSolar().getCuerpos().contains(satelite),
                "El satélite debe desaparecer cuando el planeta orbitado es destruido");
    }

    @Test
    void testEscudoAbsorbeMeteoritosYSeAgotanGolpes() {
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 500);

        Planeta planeta = CuerpoCelesteFactory.crearPlanetaAgua(0, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(planeta);
        ServicioEscudo.activar(simulacion, planeta);

        double masaInicial = planeta.getMasa();
        int golpes = planeta.getGolpesEscudoRestantes();

        for (int i = 0; i < golpes; i++) {
            assertTrue(planeta.tieneEscudo());
            Meteorito met = CuerpoCelesteFactory.crearMeteoritoJugadorFisica(0, 0, Vector2D.cero());
            simulacion.agregarCuerpo(met);
            simulacion.avanzarTick();
            assertEquals(masaInicial, planeta.getMasa(), 1e-3, "El escudo debe absorber sin daño de masa");
        }

        // Una vez consumidos los golpes, el escudo se desactiva
        assertFalse(planeta.tieneEscudo(), "El escudo debe desactivarse tras absorber todos los golpes");
    }

    @Test
    void testEscudoNoProtegeAnteEstrellasNiAgujeroNegro() {
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 500);

        Planeta planeta = CuerpoCelesteFactory.crearPlanetaAgua(0, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(planeta);
        ServicioEscudo.activar(simulacion, planeta);

        Estrella sol = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        simulacion.agregarCuerpo(sol);

        simulacion.avanzarTick();
        assertFalse(simulacion.getSistemaSolar().getCuerpos().contains(planeta),
                "El escudo no protege ante la absorción o choque con una Estrella gigante");
    }

    // =========================================================================
    // 4. METEORITOS GUIADOS, DAÑO INICIAL Y TIEMPO DE GRACIA
    // =========================================================================

    @Test
    void testMeteoritosDestruyenPlanetasInicialesSinEscudo() {
        simulacion.getProgresion().desbloquearTodo();
        // Planeta inicial colocado con masa reducida (0.3x) y sin escudo
        Planeta planetaInicial = CuerpoCelesteFactory.crearPlanetaAgua(0, 0, 0.3, Vector2D.cero());
        simulacion.agregarCuerpo(planetaInicial);

        Meteorito met = CuerpoCelesteFactory.crearMeteoritoJugadorFisica(0, 0, Vector2D.cero());
        simulacion.agregarCuerpo(met);

        // Impacto directo de meteorito masivo destruye el planeta inicial indefenso
        simulacion.avanzarTick();
        assertFalse(simulacion.getSistemaSolar().getCuerpos().contains(planetaInicial),
                "El meteorito masivo debe destruir por completo a los planetas indefensos del inicio");
    }

    @Test
    void testGeneradorEventosCosmicosNoSpawneaSinPlanetasYTardaEnAparecer() {
        GeneradorEventosCosmicos gen = new GeneradorEventosCosmicos();
        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        List<CuerpoCeleste> agregados = new ArrayList<>();

        // Solo con una estrella, no debe generar meteoritos
        cuerpos.add(CuerpoCelesteFactory.crearEstrella(0, 0, 1.0));
        gen.actualizarEventosCosmicos(cuerpos, 1920, 1080, agregados::add, evento -> {}, 1800);
        assertTrue(agregados.isEmpty(), "No deben spawnear meteoritos si no hay planetas");

        // Al agregar un planeta y estar el contador en 1, sí debe permitirse generar
        GeneradorEventosCosmicos genPlaneta = new GeneradorEventosCosmicos(1);
        Planeta planeta = CuerpoCelesteFactory.crearPlanetaAgua(100, 0, 1.0, Vector2D.cero());
        cuerpos.add(planeta);
        genPlaneta.actualizarEventosCosmicos(cuerpos, 1920, 1080, agregados::add, evento -> {}, 1800);
        assertEquals(1, agregados.size(), "Debe spawnear un meteorito al haber planetas");
        assertTrue(agregados.get(0) instanceof Meteorito);
    }

    // =========================================================================
    // 5. AGUJERO NEGRO: ATRACCIÓN UNIVERSAL FUERTE Y GAME OVER
    // =========================================================================

    @Test
    void testAgujeroNegroAtraeTodoSinImportarDistanciaYConFuerza() {
        simulacion.getProgresion().desbloquearTodo();
        AgujeroNegro bh = CuerpoCelesteFactory.crearAgujeroNegroEstelar(0, 0, 1.0);
        simulacion.agregarCuerpo(bh);

        // Planeta a gran distancia (3000 px del agujero negro) con velocidad cero
        Planeta planetaLejano = CuerpoCelesteFactory.crearPlanetaAgua(3000, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(planetaLejano);

        assertEquals(0.0, planetaLejano.getVelocidad().magnitud());

        // Avanzar un tick
        simulacion.avanzarTick();

        // Debe haber sido atraído fuertemente hacia la izquierda (dirección hacia x=0)
        Vector2D velFinal = planetaLejano.getVelocidad();
        assertTrue(velFinal.x < -10.0, "El agujero negro debe atraer fuertemente sin importar la gran distancia (vel x < -10)");
        assertTrue(velFinal.magnitud() >= 12.0, "La fuerza de atracción debe ser masiva y perceptible");
    }

    @Test
    void testFusionEstrellasCreaAgujeroNegro() {
        simulacion.getProgresion().desbloquearTodo();
        Estrella sol1 = CuerpoCelesteFactory.crearEstrella(-10, 0, 1.0);
        sol1.setVelocidad(new Vector2D(10, 0));
        Estrella sol2 = CuerpoCelesteFactory.crearEstrella(10, 0, 1.0);
        sol2.setVelocidad(new Vector2D(-10, 0));

        simulacion.agregarCuerpo(sol1);
        simulacion.agregarCuerpo(sol2);

        simulacion.avanzarTick();

        boolean hayAgujeroNegro = simulacion.getSistemaSolar().getCuerpos().stream()
                .anyMatch(c -> c.getTipoCuerpo() == TipoCuerpo.AGUJERO_NEGRO);
        assertTrue(hayAgujeroNegro, "La colisión de 2 estrellas debe crear un Agujero Negro");
    }

    @Test
    void testGameOverYReinicioCompleto() {
        simulacion.getProgresion().desbloquearTodo();
        AgujeroNegro bh = CuerpoCelesteFactory.crearAgujeroNegroEstelar(0, 0, 1.0);
        simulacion.agregarCuerpo(bh);

        // Sin planetas ni lunas en presencia de un agujero negro -> Game Over
        assertTrue(simulacion.estaPerdida(), "Debe marcar partida perdida si hay Agujero Negro sin planetas/lunas");

        // Al reiniciar la simulación
        simulacion.reiniciar();
        assertFalse(simulacion.estaPerdida());
        assertTrue(simulacion.getSistemaSolar().getCuerpos().isEmpty());
        assertEquals(400.0, simulacion.getInventario().getRecurso(TipoRecurso.MINERALES));
        assertEquals(200.0, simulacion.getInventario().getRecurso(TipoRecurso.ENERGIA));
        assertEquals(0.0, simulacion.getInventario().getRecurso(TipoRecurso.CIENCIA));
    }

    @Test
    void testItemSateliteDesbloqueoAntesDelEscudoYProduccionFija() {
        SistemaProgresion prog = simulacion.getProgresion();
        InventarioJugador inv = simulacion.getInventario();

        // 1. Inicialmente bloqueado
        assertFalse(prog.estaDesbloqueado(TipoCuerpo.SATELITE));
        assertFalse(prog.estaDesbloqueado(ElementoDesbloqueable.SATELITE));

        // Nivel 1 (Luna, 100 CIE) y Nivel 2 (Porro, 500 CIE)
        inv.agregarRecurso(TipoRecurso.CIENCIA, 600.0);
        assertTrue(prog.investigar(RamaProgresion.PLANETAS, inv));
        assertTrue(prog.investigar(RamaProgresion.PLANETAS, inv));

        // Nivel 3: Satélite se desbloquea ANTES del Escudo (1500 CIE)
        inv.agregarRecurso(TipoRecurso.CIENCIA, 1500.0);
        assertTrue(prog.investigar(RamaProgresion.PLANETAS, inv));
        assertTrue(prog.estaDesbloqueado(TipoCuerpo.SATELITE));
        assertTrue(prog.estaDesbloqueado(ElementoDesbloqueable.SATELITE));
        // Escudo sigue bloqueado hasta el Nivel 4
        assertFalse(prog.tieneEscudoDesbloqueado());

        // 2. Producción y costos: solo genera ciencia y un poco de energía
        assertEquals(ConfiguracionSimulacion.PRODUCCION_SATELITE_CIENCIA,
                TipoCuerpo.SATELITE.getProduccionBase().get(TipoRecurso.CIENCIA), 1e-6);
        assertEquals(ConfiguracionSimulacion.PRODUCCION_SATELITE_ENERGIA,
                TipoCuerpo.SATELITE.getProduccionBase().get(TipoRecurso.ENERGIA), 1e-6);
        assertNull(TipoCuerpo.SATELITE.getProduccionBase().get(TipoRecurso.MINERALES));
        assertNull(TipoCuerpo.SATELITE.getProduccionBase().get(TipoRecurso.POBLACION));

        // Es más barato que el escudo y que los planetas
        assertTrue(ConfiguracionSimulacion.COSTO_SATELITE_MINERALES < ConfiguracionSimulacion.COSTO_PLANETA_MINERALES);
        assertTrue(ConfiguracionSimulacion.COSTO_SATELITE_ENERGIA < ConfiguracionSimulacion.COSTO_ACTIVACION_ESCUDO_ENERGIA);

        // 3. No se puede medir en masa / no modificable en masa
        var costoBase = InventarioJugador.getCostosEscalados(TipoCuerpo.SATELITE, 1.0);
        var costoX5 = InventarioJugador.getCostosEscalados(TipoCuerpo.SATELITE, 5.0);
        assertEquals(costoBase.get(TipoRecurso.MINERALES), costoX5.get(TipoRecurso.MINERALES), 1e-6);

        var prodBase = InventarioJugador.getProduccionEscaladaPorTick(TipoCuerpo.SATELITE, 1.0);
        var prodX10 = InventarioJugador.getProduccionEscaladaPorTick(TipoCuerpo.SATELITE, 10.0);
        assertEquals(prodBase.get(TipoRecurso.CIENCIA), prodX10.get(TipoRecurso.CIENCIA), 1e-6);

        // Al spawnearlo, la masa siempre es fija (1.0e3)
        inv.agregarRecurso(TipoRecurso.MINERALES, 1000);
        inv.agregarRecurso(TipoRecurso.ENERGIA, 1000);
        assertTrue(simulacion.spawnCuerpo(TipoCuerpo.SATELITE, "SondaAlpha", 100, 100, 5.0, Vector2D.cero()));
        CuerpoCeleste satSpawneado = simulacion.getSistemaSolar().getCuerpos().stream()
                .filter(c -> c.getTipoCuerpo() == TipoCuerpo.SATELITE)
                .findFirst().orElse(null);
        assertNotNull(satSpawneado);
        assertEquals(TipoCuerpo.SATELITE.masaBase, satSpawneado.getMasa(), 1e-6);
    }

    @Test
    void testEscudoMasCaroSegunMasaDelPlaneta() {
        simulacion.getProgresion().desbloquearTodo();

        Planeta planetaChico = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 0.3, Vector2D.cero());
        Planeta planetaNormal = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        Planeta planetaGrande = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 2.0, Vector2D.cero());
        Planeta planetaColosal = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 5.0, Vector2D.cero());

        double costoChico = ServicioEscudo.calcularCostoEscudo(planetaChico);
        double costoNormal = ServicioEscudo.calcularCostoEscudo(planetaNormal);
        double costoGrande = ServicioEscudo.calcularCostoEscudo(planetaGrande);
        double costoColosal = ServicioEscudo.calcularCostoEscudo(planetaColosal);

        // El costo debe escalar con la masa
        assertTrue(costoChico < costoNormal, "Planeta más liviano debe pagar menos energía");
        assertEquals(50.0, costoNormal, 1e-6, "Masa normal 1.0x paga el costo base");
        assertTrue(costoGrande > costoNormal, "Planeta 2.0x debe pagar más energía");
        assertTrue(costoColosal > costoGrande, "Planeta 5.0x debe pagar aún más energía");
        assertEquals(250.0, costoColosal, 1e-6);
    }

    @Test
    void testPlanetaAguaDaMasRecursosAlInicio() {
        var prodAgua = TipoCuerpo.PLANETA_AGUA.getProduccionBase();
        assertTrue(prodAgua.containsKey(TipoRecurso.MINERALES));
        assertTrue(prodAgua.containsKey(TipoRecurso.CIENCIA));
        assertTrue(prodAgua.containsKey(TipoRecurso.ENERGIA));
        assertTrue(prodAgua.get(TipoRecurso.MINERALES) >= 0.035);
        assertTrue(prodAgua.get(TipoRecurso.CIENCIA) >= 0.025);
        assertTrue(prodAgua.get(TipoRecurso.ENERGIA) >= 0.02);
    }

    @Test
    void testSateliteOrbitaPlanetasComoLaLuna() {
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.MINERALES, 1000);
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 1000);

        // Colocar un Planeta central
        Planeta planeta = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(planeta);

        // Colocar un Satélite a 35px del planeta en modo auto-órbita
        simulacion.entrarModoColocacion(TipoCuerpo.SATELITE, 1.0, "Sonda-1");
        simulacion.actualizarPosicionPreview(ConstantesFisicas.fisicaAXJavaFX(35, 1920), ConstantesFisicas.fisicaAYJavaFX(0, 1080));
        simulacion.confirmarColocacionAutoOrbita();

        CuerpoCeleste sat = simulacion.getSistemaSolar().getCuerpos().stream()
                .filter(c -> c.getTipoCuerpo() == TipoCuerpo.SATELITE)
                .findFirst().orElse(null);

        assertNotNull(sat, "El satélite debe haber sido creado");
        assertTrue(sat.estaOrbitando(planeta), "El satélite debe orbitar al planeta anfitrión igual que una Luna");
        assertTrue(sat.getVelocidad().magnitud() > 0, "El satélite debe poseer velocidad orbital asistida");
    }

    @Test
    void testLunaOrbitaMasLejosQueSatelites() {
        simulacion.cargarPresetSistemaBasico();

        CuerpoCeleste tierra = simulacion.getSistemaSolar().getCuerpos().stream()
                .filter(c -> c instanceof Planeta).findFirst().orElse(null);
        CuerpoCeleste luna = simulacion.getSistemaSolar().getCuerpos().stream()
                .filter(c -> c instanceof Luna).findFirst().orElse(null);

        assertNotNull(tierra);
        assertNotNull(luna);

        double distLuna = luna.getPosicion().distanciaA(tierra.getPosicion());
        assertTrue(distLuna >= 60.0, "La Luna debe orbitar a una distancia más lejana (>= 60px) para no interferir con satélites");
    }

    @Test
    void testMultiplesSatelitesEnUnPlanetaNoColapsanEntreSi() {
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.MINERALES, 5000);
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 5000);

        Planeta planeta = CuerpoCelesteFactory.crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(planeta);

        // Colocar 3 satélites alrededor del mismo planeta
        simulacion.spawnCuerpo(TipoCuerpo.SATELITE, "Sat-1", 30, 0, 1.0, new Vector2D(0, 1.58));
        simulacion.spawnCuerpo(TipoCuerpo.SATELITE, "Sat-2", -30, 0, 1.0, new Vector2D(0, -1.58));
        simulacion.spawnCuerpo(TipoCuerpo.SATELITE, "Sat-3", 0, 35, 1.0, new Vector2D(-1.46, 0));

        long countSats = simulacion.getSistemaSolar().getCuerpos().stream()
                .filter(c -> c.getTipoCuerpo() == TipoCuerpo.SATELITE).count();
        assertEquals(3, countSats, "Debe ser posible tener múltiples satélites en un solo planeta");

        // Simular 50 ticks
        simulacion.avanzarTicks(50);

        long countSatsDespues = simulacion.getSistemaSolar().getCuerpos().stream()
                .filter(c -> c.getTipoCuerpo() == TipoCuerpo.SATELITE).count();
        assertEquals(3, countSatsDespues, "Los satélites no deben colapsar entre sí durante su órbita simultánea");

        // Verificación directa en ResolvedorColisiones: dos satélites no se destruyen ni generan escombros
        org.example.game.motor.ResolvedorColisiones resolvedor = new org.example.game.motor.ResolvedorColisiones();
        Satelite sA = (Satelite) CuerpoCelesteFactory.crearSatelite(10, 10, 1.0, Vector2D.cero());
        Satelite sB = (Satelite) CuerpoCelesteFactory.crearSatelite(10, 10, 1.0, Vector2D.cero());
        List<CuerpoCeleste> aEliminar = new ArrayList<>();
        List<CuerpoCeleste> aAgregar = new ArrayList<>();

        resolvedor.resolverColision(sA, sB, aEliminar, aAgregar);
        assertTrue(aEliminar.isEmpty(), "Los satélites no deben eliminarse al colisionar entre sí");
        assertTrue(aAgregar.isEmpty(), "Los satélites no deben generar escombros");
    }
}
