package org.example;
import org.example.game.civilizacion.CivilizacionComponent;
import org.example.game.colonizacion.NaveColonizadora;
import org.example.game.cuerpo.*;
import org.example.game.jugador.TipoRecurso;
import org.example.game.motor.Vector2D;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.SimulacionSolar;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
class ColonizacionYEscudosTest {

    private SimulacionSolar simulacion;

    @BeforeEach
    void setUp() {
        simulacion = new SimulacionSolar(1920, 1080);
        simulacion.limpiarCuerpos();
    }

    @Test
    void testSoloPlanetaPorroNaceConCivilizacion() {
        Planeta porro = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        Planeta agua = new CuerpoCelesteFactory().crearPlanetaAgua(100, 0, 1.0, Vector2D.cero());
        Planeta lava = new CuerpoCelesteFactory().crearPlanetaLava(200, 0, 1.0, Vector2D.cero());

        assertTrue(porro.tieneCivilizacion());
        assertFalse(agua.tieneCivilizacion());
        assertFalse(lava.tieneCivilizacion());

        assertTrue(agua.puedeSerColonizado());
        assertTrue(lava.puedeSerColonizado());
    }

    @Test
    void testCrecimientoLogisticoYCapacidad() {
        Estrella sol = new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0);
        Vector2D posPorro = new Vector2D(150, 0);
        Vector2D velPorro = org.example.game.motor.CalculadorOrbital.calcularVelocidadOrbitalVector(sol, posPorro, false);
        Planeta porro = new CuerpoCelesteFactory().crearPlanetaRocoso(posPorro.x(), posPorro.y(), 1.0, velPorro);
        CivilizacionComponent civ = porro.getCivilizacion();
        assertNotNull(civ);

        double capMaxima = civ.getPoblacionMaxima();
        assertTrue(capMaxima > 0);

        double pobInicial = civ.getPoblacion();
        for (int i = 0; i < 100; i++) {
            civ.actualizar(List.of(sol, porro));
        }

        assertTrue(civ.getPoblacion() >= pobInicial);
        assertTrue(civ.getPoblacion() <= capMaxima);
        assertTrue(civ.getPorcentajeCapacidad() >= 0.0 && civ.getPorcentajeCapacidad() <= 100.0);
    }

    @Test
    void testMecanicaColonizacionYTransferencia() {
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.MINERALES, 1000);
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 1000);

        Planeta origen = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        CivilizacionComponent civOrigen = origen.getCivilizacion();
        civOrigen.setPoblacion(civOrigen.getPoblacionMaxima() * 0.85);

        Planeta destino = new CuerpoCelesteFactory().crearPlanetaAgua(500, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(origen);
        simulacion.agregarCuerpo(destino);

        double pobAntesOrigen = civOrigen.getPoblacion();
        boolean lanzada = simulacion.getGestorColonizacion().lanzar(origen, destino, simulacion.getInventario(), simulacion.getBusEventos()::notificarEvento, 0);
        assertTrue(lanzada);

        assertEquals(1, simulacion.getGestorColonizacion().getNaves().size());
        NaveColonizadora nave = simulacion.getGestorColonizacion().getNaves().get(0);
        assertEquals(pobAntesOrigen * 0.10, nave.getPoblacionColonos(), 1.0);
        assertEquals(pobAntesOrigen * 0.90, civOrigen.getPoblacion(), 1.0);

        while (!simulacion.getGestorColonizacion().getNaves().isEmpty()) {
            simulacion.avanzarTick();
        }

        assertTrue(destino.tieneCivilizacion());
        assertTrue(destino.getCivilizacion().getPoblacion() > 0);
    }

    @Test
    void testMisionColonizadoraSePierdeSiDestinoEsDestruido() {
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.MINERALES, 1000);
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 1000);

        Planeta origen = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        origen.getCivilizacion().setPoblacion(origen.getCivilizacion().getPoblacionMaxima() * 0.90);
        Planeta destino = new CuerpoCelesteFactory().crearPlanetaAgua(1000, 0, 1.0, Vector2D.cero());

        simulacion.agregarCuerpo(origen);
        simulacion.agregarCuerpo(destino);

        assertTrue(simulacion.getGestorColonizacion().lanzar(origen, destino, simulacion.getInventario(), simulacion.getBusEventos()::notificarEvento, 0));

        simulacion.getGestorCicloVida().eliminarCuerpo(destino);
        simulacion.getGestorColonizacion().actualizar(simulacion.getBusEventos()::notificarEvento, 1);
        assertTrue(simulacion.getGestorColonizacion().getNaves().isEmpty());
    }

    @Test
    void testActivacionEscudoYSateliteOrbital() {
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 500);

        Planeta planeta = new CuerpoCelesteFactory().crearPlanetaAgua(0, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(planeta);

        assertFalse(planeta.tieneEscudo());
        assertTrue(planeta.puedeRecibirEscudo());

        boolean activado = simulacion.getServicioEscudo().activar(simulacion, planeta);
        assertTrue(activado);
        assertTrue(planeta.tieneEscudo());
        assertEquals(5, planeta.getGolpesEscudoRestantes());

        boolean creoSateliteAutomatico = simulacion.getSistemaSolar().getCuerpos().stream()
                .anyMatch(c -> c.getTipoCuerpo() == TipoCuerpo.SATELITE);
        assertFalse(creoSateliteAutomatico, "Activar el escudo no debe desplegar un satélite automáticamente");

        Satelite satelite = new CuerpoCelesteFactory().crearSatelite(25, 0, 1.0, Vector2D.cero());
        satelite.setCuerpoOrbitado(planeta);
        simulacion.agregarCuerpo(satelite);

        assertEquals(ConfiguracionSimulacion.PRODUCCION_SATELITE_CIENCIA, satelite.getTipoCuerpo().getProduccionBase().get(TipoRecurso.CIENCIA), 1e-6);
        assertEquals(ConfiguracionSimulacion.PRODUCCION_SATELITE_ENERGIA, satelite.getTipoCuerpo().getProduccionBase().get(TipoRecurso.ENERGIA), 1e-6);
        assertNull(satelite.getTipoCuerpo().getProduccionBase().get(TipoRecurso.MINERALES));

        simulacion.getGestorCicloVida().eliminarCuerpo(planeta);
        assertFalse(simulacion.getSistemaSolar().getCuerpos().contains(satelite));
    }

    @Test
    void testEscudoAbsorbeMeteoritosYSeAgotanGolpes() {
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 500);

        Planeta planeta = new CuerpoCelesteFactory().crearPlanetaAgua(0, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(planeta);
        simulacion.getServicioEscudo().activar(simulacion, planeta);

        double masaInicial = planeta.getMasa();
        int golpes = planeta.getGolpesEscudoRestantes();

        for (int i = 0; i < golpes; i++) {
            assertTrue(planeta.tieneEscudo());
            Meteorito met = new CuerpoCelesteFactory().crearMeteoritoJugadorFisica(0, 0, Vector2D.cero());
            simulacion.agregarCuerpo(met);
            simulacion.avanzarTick();
            assertEquals(masaInicial, planeta.getMasa(), 1e-3);
        }

        assertFalse(planeta.tieneEscudo());
    }

    @Test
    void testEscudoNoProtegeAnteEstrellasNiAgujeroNegro() {
        simulacion.getProgresion().desbloquearTodo();
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 500);

        Planeta planeta = new CuerpoCelesteFactory().crearPlanetaAgua(0, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(planeta);
        simulacion.getServicioEscudo().activar(simulacion, planeta);

        Estrella sol = new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0);
        simulacion.agregarCuerpo(sol);

        simulacion.avanzarTick();
        assertFalse(simulacion.getSistemaSolar().getCuerpos().contains(planeta));
    }

    @Test
    void testEscudoMasCaroSegunMasaDelPlaneta() {
        simulacion.getProgresion().desbloquearTodo();

        Planeta planetaChico = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 0.3, Vector2D.cero());
        Planeta planetaNormal = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 1.0, Vector2D.cero());
        Planeta planetaGrande = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 2.0, Vector2D.cero());
        Planeta planetaColosal = new CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 5.0, Vector2D.cero());

        ServicioEscudo servicioEscudo = new ServicioEscudo();
        double costoChico = servicioEscudo.calcularCostoEscudo(planetaChico);
        double costoNormal = servicioEscudo.calcularCostoEscudo(planetaNormal);
        double costoGrande = servicioEscudo.calcularCostoEscudo(planetaGrande);
        double costoColosal = servicioEscudo.calcularCostoEscudo(planetaColosal);

        assertTrue(costoChico < costoNormal);
        assertEquals(ConfiguracionSimulacion.COSTO_ACTIVACION_ESCUDO_ENERGIA, costoNormal, 1e-6);
        assertTrue(costoGrande > costoNormal);
        assertTrue(costoColosal > costoGrande);
        assertEquals(ConfiguracionSimulacion.COSTO_ACTIVACION_ESCUDO_ENERGIA * 5.0, costoColosal, 1e-6);
    }

    @Test
    void testOpcionPoblarConPlanetaPorroMilHabitantesYCienciaAguaMasBaratoQueLava() {
        var gestorCol = simulacion.getGestorColonizacion();
        double costoAgua = gestorCol.calcularCostoCienciaPoblar(TipoCuerpo.PLANETA_AGUA);
        double costoLava = gestorCol.calcularCostoCienciaPoblar(TipoCuerpo.PLANETA_LAVA);
        assertTrue(costoAgua < costoLava, "Poblar un Planeta de Agua debe ser mas barato en Ciencia que poblar un Planeta Lava");

        Planeta porro = new CuerpoCelesteFactory().crearPlanetaRocoso(160, 0, 1.0, Vector2D.cero());
        Planeta agua = new CuerpoCelesteFactory().crearPlanetaAgua(320, 0, 1.0, Vector2D.cero());
        Planeta lava = new CuerpoCelesteFactory().crearPlanetaLava(480, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(porro);
        simulacion.agregarCuerpo(agua);
        simulacion.agregarCuerpo(lava);

        // Con menos de 1000 habitantes en Planeta Porro, no se puede poblar aunque sobre ciencia
        porro.getCivilizacion().setPoblacion(800);
        simulacion.getInventario().agregarRecurso(TipoRecurso.CIENCIA, 2000);
        assertFalse(gestorCol.puedePoblar(agua, simulacion.getSistemaSolar().getCuerpos(), simulacion.getInventario()));

        // Con >= 1000 habitantes y ciencia suficiente para Agua (300) pero no para Lava (750)
        porro.getCivilizacion().setPoblacion(1200);
        simulacion.getInventario().reiniciar();
        simulacion.getInventario().agregarRecurso(TipoRecurso.CIENCIA, costoAgua + 50.0);
        assertTrue(gestorCol.puedePoblar(agua, simulacion.getSistemaSolar().getCuerpos(), simulacion.getInventario()));
        assertFalse(gestorCol.puedePoblar(lava, simulacion.getSistemaSolar().getCuerpos(), simulacion.getInventario()));

        assertTrue(gestorCol.poblar(agua, simulacion.getSistemaSolar().getCuerpos(), simulacion.getInventario(), null, 0));
        assertTrue(agua.tieneCivilizacion(), "El Planeta de Agua debe quedar poblado tras ejecutar poblar()");
    }

    @Test
    void testCrecimientoMasLentoEnOtrosPlanetasYBonusMaterialesX1_3AlEstarPoblado() {
        Estrella sol = new CuerpoCelesteFactory().crearEstrella(0, 0, 1.0);
        Planeta porro = new CuerpoCelesteFactory().crearPlanetaRocoso(160, 0, 1.0,
                org.example.game.motor.CalculadorOrbital.calcularVelocidadOrbitalVector(sol, new Vector2D(160, 0), false));
        Planeta agua = new CuerpoCelesteFactory().crearPlanetaAgua(320, 0, 1.0,
                org.example.game.motor.CalculadorOrbital.calcularVelocidadOrbitalVector(sol, new Vector2D(320, 0), false));

        // Comparar producción antes y después de poblar Planeta de Agua
        org.example.game.jugador.InventarioJugador invSinPoblar = new org.example.game.jugador.InventarioJugador();
        org.example.game.simulacion.GestorEconomia econ = new org.example.game.simulacion.GestorEconomia();
        double minBase = invSinPoblar.getRecurso(TipoRecurso.MINERALES);
        econ.actualizarEconomia(List.of(agua), invSinPoblar);
        double prodSinPoblar = invSinPoblar.getRecurso(TipoRecurso.MINERALES) - minBase;

        agua.recibirColonos(500.0);
        porro.getCivilizacion().setPoblacion(500.0);

        org.example.game.jugador.InventarioJugador invPoblado = new org.example.game.jugador.InventarioJugador();
        double minAntesPoblado = invPoblado.getRecurso(TipoRecurso.MINERALES);
        econ.actualizarEconomia(List.of(agua), invPoblado);
        double prodPoblado = invPoblado.getRecurso(TipoRecurso.MINERALES) - minAntesPoblado;

        assertEquals(prodSinPoblar * ConfiguracionSimulacion.MULTIPLICADOR_PRODUCCION_PLANETA_POBLADO, prodPoblado, 1e-5,
                "Un planeta poblado debe otorgar x1.3 mas materiales");

        List<CuerpoCeleste> sistema = List.of(sol, porro, agua);
        for (int i = 0; i < 100; i++) {
            porro.actualizarCivilizacion(sistema);
            agua.actualizarCivilizacion(sistema);
        }

        double aumentoPorro = porro.getCivilizacion().getPoblacion() - 500.0;
        double aumentoAgua = agua.getCivilizacion().getPoblacion() - 500.0;
        assertTrue(aumentoAgua > 0, "La poblacion en Planeta de Agua debe crecer");
        assertTrue(aumentoPorro > aumentoAgua * 2.5, "La poblacion en otros planetas debe crecer mucho mas despacio que en Planeta Porro");
    }

    @Test
    void testPoblarDesdeCualquierPlanetaConMilHabitantesYElegirOrigenRestandoPoblacion() {
        var gestorCol = simulacion.getGestorColonizacion();
        simulacion.getInventario().agregarRecurso(TipoRecurso.CIENCIA, 5000);

        // Sin Planeta Porro: un Planeta de Agua poblado con >= 1000 habitantes puede poblar un Planeta Lava
        Planeta aguaOrigen = new CuerpoCelesteFactory().crearPlanetaAgua(160, 0, 1.0, Vector2D.cero());
        Planeta lavaDestino = new CuerpoCelesteFactory().crearPlanetaLava(320, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(aguaOrigen);
        simulacion.agregarCuerpo(lavaDestino);

        aguaOrigen.recibirColonos(1500.0);
        assertTrue(gestorCol.puedePoblar(lavaDestino, simulacion.getSistemaSolar().getCuerpos(), simulacion.getInventario()),
                "Un Planeta de Agua con +1000 habitantes debe poder poblar otros planetas aunque no haya Planeta Porro");

        // Si agregamos otro planeta con +1000 habitantes, ambos aparecen como opciones de origen y se resta población al elegido
        Planeta porroOrigen = new CuerpoCelesteFactory().crearPlanetaRocoso(480, 0, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(porroOrigen);
        porroOrigen.getCivilizacion().setPoblacion(2000.0);

        List<Planeta> origenes = gestorCol.obtenerPlanetasOrigenParaPoblar(lavaDestino, simulacion.getSistemaSolar().getCuerpos());
        assertEquals(2, origenes.size(), "Deben listarse ambos planetas con +1000 habitantes para elegir cual poblara al destino");
        assertTrue(origenes.contains(aguaOrigen) && origenes.contains(porroOrigen));

        double pobAguaAntes = aguaOrigen.getCivilizacion().getPoblacion();
        double pobPorroAntes = porroOrigen.getCivilizacion().getPoblacion();

        // Elegimos poblar desde aguaOrigen específicamente
        assertTrue(gestorCol.poblarDesde(aguaOrigen, lavaDestino, simulacion.getSistemaSolar().getCuerpos(), simulacion.getInventario(), null, 0));
        assertTrue(lavaDestino.tieneCivilizacion(), "El planeta destino debe quedar poblado");
        assertTrue(aguaOrigen.getCivilizacion().getPoblacion() < pobAguaAntes,
                "Se debe restar la poblacion del planeta elegido que puebla al otro");
        assertEquals(pobPorroAntes, porroOrigen.getCivilizacion().getPoblacion(), 1e-6,
                "El planeta que no fue elegido no debe perder poblacion");
    }
}
