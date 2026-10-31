package org.example;
import org.example.game.cuerpo.*; import org.example.game.jugador.InventarioJugador; import org.example.game.jugador.ServicioReembolso; import org.example.game.jugador.TipoRecurso; import org.example.game.motor.ResolvedorColisiones; import org.example.game.motor.Vector2D; import org.example.game.simulacion.ConfiguracionSimulacion; import org.example.game.simulacion.GestorEconomia; import org.example.game.simulacion.SimulacionSolar; import org.example.test.JavaFXExtension; import org.junit.jupiter.api.BeforeEach; import org.junit.jupiter.api.Test; import org.junit.jupiter.api.extension.ExtendWith; import java.util.ArrayList; import java.util.HashSet; import java.util.List; import java.util.Set; import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
class EasterEggFusionYRadiosTest {

    private SimulacionSolar simulacion;

    @BeforeEach
    void setUp() {
        simulacion = new SimulacionSolar(1920, 1080);
    }

    @Test
    void testPlanetaTransformadoNoDesarrollaCivilizacion() {
        Planeta planeta = CuerpoCelesteFactory.crearPlanetaAgua(0, 0, 1.0, Vector2D.cero());
        planeta.transformarA(TipoCuerpo.PLANETA_ROCOSO);

        assertTrue(planeta.isTransformadoPorEvento());
        planeta.desarrollarCivilizacion();
        assertFalse(planeta.tieneCivilizacion());
        assertNull(planeta.getCivilizacion());
    }

    @Test
    void testPlanetaTransformadoNoGeneraRecursos() {
        simulacion.limpiarCuerpos();
        double minInicial = simulacion.getInventario().getRecurso(TipoRecurso.MINERALES);
        double cieInicial = simulacion.getInventario().getRecurso(TipoRecurso.CIENCIA);

        Planeta porroTransformado = CuerpoCelesteFactory.crearPlanetaRocoso(500, 500, 1.0, Vector2D.cero());
        porroTransformado.transformarA(TipoCuerpo.PLANETA_ROCOSO);
        simulacion.agregarCuerpo(porroTransformado);

        for (int i = 0; i < 20; i++) {
            simulacion.avanzarTick();
        }

        assertEquals(minInicial, simulacion.getInventario().getRecurso(TipoRecurso.MINERALES), 1e-6);
        assertEquals(cieInicial, simulacion.getInventario().getRecurso(TipoRecurso.CIENCIA), 1e-6);
    }

    @Test
    void testTresRadiosDeReferenciaOrbital() {
        List<Double> radios = simulacion.getTransformadorCuerpos().getRadiosActivacion();
        assertEquals(2, radios.size());
        assertTrue(radios.contains(ConfiguracionSimulacion.RADIO_TRANSFORMACION_AGUA_PORRO));
        assertTrue(radios.contains(ConfiguracionSimulacion.RADIO_TRANSFORMACION_PORRO_LAVA));
    }

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

        assertTrue(aAgregar.isEmpty());
        assertEquals(1, aEliminar.size());
        assertTrue(aEliminar.contains(agua));

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

        InventarioJugador inv = new InventarioJugador();
        double minAntes = inv.getRecurso(TipoRecurso.MINERALES);
        GestorEconomia economia = new GestorEconomia();
        economia.actualizarEconomia(List.of(sobreviviente), inv);
        assertEquals(minAntes, inv.getRecurso(TipoRecurso.MINERALES), 1e-6);

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

        assertTrue(aAgregar.isEmpty());
        assertEquals(1, aEliminar.size());

        CuerpoCeleste sobreviviente = aEliminar.contains(agua) ? lava : agua;
        assertEquals(TipoCuerpo.PLANETA_ROCOSO, sobreviviente.getTipoCuerpo());
        assertTrue(sobreviviente instanceof Transformable t && t.isTransformadoPorEvento());
        assertFalse(sobreviviente.permiteReembolso());
    }

    @Test
    void testEasterEggColisionAguaYLavaEnSimulacionReal() {
        simulacion.limpiarCuerpos();
        Planeta agua = CuerpoCelesteFactory.crearPlanetaAgua(100, 200, 1.0, Vector2D.cero());
        Planeta lava = CuerpoCelesteFactory.crearPlanetaLava(110, 200, 1.0, Vector2D.cero());
        simulacion.agregarCuerpo(agua);
        simulacion.agregarCuerpo(lava);

        assertEquals(2, simulacion.getSistemaSolar().getCuerpos().size());

        simulacion.avanzarTick();

        assertEquals(1, simulacion.getSistemaSolar().getCuerpos().size());
        CuerpoCeleste porro = simulacion.getSistemaSolar().getCuerpos().get(0);
        assertEquals(TipoCuerpo.PLANETA_ROCOSO, porro.getTipoCuerpo());
        assertEquals("Planeta Porro", porro.getNombre());
        assertTrue(porro instanceof Transformable t && t.isTransformadoPorEvento());
        assertFalse(porro.permiteReembolso());
    }
}
