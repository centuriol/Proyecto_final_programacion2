package org.example;
import javafx.scene.image.Image; import javafx.scene.input.MouseButton; import javafx.scene.input.MouseEvent; import org.example.game.cuerpo.CuerpoCeleste; import org.example.game.cuerpo.TipoCuerpo; import org.example.game.jugador.InventarioJugador; import org.example.game.jugador.TipoRecurso; import org.example.game.motor.Vector2D; import org.example.game.render.GestorImagenes; import org.example.game.simulacion.ConfiguracionEconomia; import org.example.game.simulacion.ConfiguracionSimulacion; import org.example.game.simulacion.SimulacionSolar; import org.example.game.ui.CampoSatelitesMenu; import org.example.game.ui.PantallaMenuPrincipal; import org.example.game.ui.SateliteDecorativoMenu; import org.example.test.JavaFXExtension; import org.junit.jupiter.api.Test; import org.junit.jupiter.api.extension.ExtendWith; import java.util.List; import java.util.Map; import java.util.Optional; import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
class CostosProporcionalesYSatelitesMenuTest {

    @Test
    void testTablaCostosOrdenCoherenteYProporcionalALaProduccion() {
        // El Sol bajo respecto al ajuste previo (2500 / 1250) y todos los planetas, luna y satelite subieron
        assertTrue(ConfiguracionEconomia.COSTO_ESTRELLA_MINERALES < 2500.0);
        assertTrue(ConfiguracionEconomia.COSTO_ESTRELLA_ENERGIA < 1250.0);
        assertTrue(ConfiguracionEconomia.COSTO_PLANETA_AGUA_MINERALES > 200.0);
        assertTrue(ConfiguracionEconomia.COSTO_LUNA_MINERALES > 260.0);
        assertTrue(ConfiguracionEconomia.COSTO_SATELITE_MINERALES > 180.0);
        assertTrue(ConfiguracionEconomia.COSTO_PLANETA_MINERALES > 380.0);
        assertTrue(ConfiguracionEconomia.COSTO_PLANETA_LAVA_MINERALES > 650.0);
        assertTrue(ConfiguracionEconomia.COSTO_PLANETA_GASEOSO_MINERALES > 760.0);

        // Orden estricto coherente: Agua < Porro < Lava < Gaseoso < Sol (en minerales y energia)
        List<TipoCuerpo> ordenProgresivo = List.of(
                TipoCuerpo.PLANETA_AGUA,
                TipoCuerpo.PLANETA_ROCOSO,
                TipoCuerpo.PLANETA_LAVA,
                TipoCuerpo.PLANETA_GASEOSO,
                TipoCuerpo.ESTRELLA
        );
        for (int i = 0; i < ordenProgresivo.size() - 1; i++) {
            Map<TipoRecurso, Double> actual = InventarioJugador.getCostosBase(ordenProgresivo.get(i));
            Map<TipoRecurso, Double> siguiente = InventarioJugador.getCostosBase(ordenProgresivo.get(i + 1));
            assertTrue(actual.get(TipoRecurso.MINERALES) < siguiente.get(TipoRecurso.MINERALES));
            assertTrue(actual.get(TipoRecurso.ENERGIA) < siguiente.get(TipoRecurso.ENERGIA));
        }

        // Produccion planetaria estrictamente creciente y proporcional al costo (Agua < Porro < Lava < Gaseoso)
        List<TipoCuerpo> planetas = ordenProgresivo.subList(0, 4);
        for (int i = 0; i < planetas.size() - 1; i++) {
            double prodActual = sumarValores(InventarioJugador.getProduccionBasePorTick(planetas.get(i)));
            double prodSiguiente = sumarValores(InventarioJugador.getProduccionBasePorTick(planetas.get(i + 1)));
            assertTrue(prodActual < prodSiguiente, "Cada planeta mas caro debe generar mayor produccion total por tick");

            double ratioActual = sumarValores(InventarioJugador.getCostosBase(planetas.get(i))) / prodActual;
            double ratioSiguiente = sumarValores(InventarioJugador.getCostosBase(planetas.get(i + 1))) / prodSiguiente;
            assertEquals(ratioActual, ratioSiguiente, ratioActual * 0.15,
                    "La relacion costo/produccion entre planetas debe mantener logica proporcional");
        }
    }

    @Test
    void testCentralizacionTotalEnConfiguracionEconomiaSinNumerosSueltos() {
        assertEquals(ConfiguracionEconomia.COSTO_PLANETA_GASEOSO_MINERALES,
                InventarioJugador.getCostosBase(TipoCuerpo.PLANETA_GASEOSO).get(TipoRecurso.MINERALES));
        assertEquals(ConfiguracionEconomia.COSTO_PLANETA_GASEOSO_ENERGIA,
                InventarioJugador.getCostosBase(TipoCuerpo.PLANETA_GASEOSO).get(TipoRecurso.ENERGIA));
        assertEquals(ConfiguracionEconomia.PRODUCCION_PLANETA_GASEOSO_MINERALES,
                InventarioJugador.getProduccionBasePorTick(TipoCuerpo.PLANETA_GASEOSO).get(TipoRecurso.MINERALES));

        assertEquals(ConfiguracionEconomia.COSTO_PLANETA_HELADO_MINERALES,
                InventarioJugador.getCostosBase(TipoCuerpo.PLANETA_HELADO).get(TipoRecurso.MINERALES));
        assertTrue(InventarioJugador.getCostosBase(TipoCuerpo.METEORITO).isEmpty());

        assertEquals(ConfiguracionEconomia.COSTO_ESTRELLA_MINERALES, ConfiguracionSimulacion.COSTO_BASE_ESTRELLA);
        assertEquals(ConfiguracionEconomia.COSTO_PLANETA_MINERALES, ConfiguracionSimulacion.COSTO_BASE_PLANETA_ROCOSO);
        assertEquals(ConfiguracionEconomia.COSTO_PLANETA_GASEOSO_MINERALES, ConfiguracionSimulacion.COSTO_BASE_PLANETA_GASEOSO);
        assertEquals(ConfiguracionEconomia.COSTO_ESCUDO_NIVEL_1, ConfiguracionSimulacion.COSTOS_NIVEL_ESCUDO[1]);
        assertEquals(ConfiguracionEconomia.COSTO_ESCUDO_NIVEL_4, ConfiguracionSimulacion.COSTOS_NIVEL_ESCUDO[4]);
    }

    @Test
    void testPresupuestoInicialAlcanzaUnicamenteParaUnSolYUnPlanetaDeAgua() {
        SimulacionSolar sim = new SimulacionSolar(1920, 1080);
        double masaInicial = ConfiguracionEconomia.FACTOR_MASA_INICIAL;

        assertFalse(sim.getInventario().puedeCrear(TipoCuerpo.SATELITE, masaInicial));

        Optional<CuerpoCeleste> sol = sim.spawnCuerpo(TipoCuerpo.ESTRELLA, 0, 0, masaInicial, Vector2D.cero());
        assertTrue(sol.isPresent(), "Debe alcanzar para un Sol inicial");

        assertFalse(sim.getInventario().puedeCrear(TipoCuerpo.ESTRELLA, masaInicial));
        assertFalse(sim.getInventario().puedeCrear(TipoCuerpo.LUNA, masaInicial));
        assertFalse(sim.getInventario().puedeCrear(TipoCuerpo.SATELITE, masaInicial));
        assertFalse(sim.getInventario().puedeCrear(TipoCuerpo.PLANETA_HELADO, masaInicial));
        assertFalse(sim.getInventario().puedeCrear(TipoCuerpo.PLANETA_ROCOSO, masaInicial));
        assertFalse(sim.getInventario().puedeCrear(TipoCuerpo.PLANETA_LAVA, masaInicial));
        assertFalse(sim.getInventario().puedeCrear(TipoCuerpo.PLANETA_GASEOSO, masaInicial));
        assertTrue(sim.getInventario().puedeCrear(TipoCuerpo.PLANETA_AGUA, masaInicial));

        Optional<CuerpoCeleste> agua = sim.spawnCuerpo(TipoCuerpo.PLANETA_AGUA, 180, 0, masaInicial, new Vector2D(0, 2.0));
        assertTrue(agua.isPresent(), "Debe alcanzar para un Planeta de Agua inicial");
        assertEquals(0.0, sim.getInventario().getRecurso(TipoRecurso.MINERALES), 1e-6);
        assertEquals(0.0, sim.getInventario().getRecurso(TipoRecurso.ENERGIA), 1e-6);
        assertFalse(sim.getInventario().puedeCrear(TipoCuerpo.PLANETA_AGUA, masaInicial));
    }

    @Test
    void testSpritePixelArtSateliteExisteYCargaSinError() {
        Image sprite = GestorImagenes.getImagenPorNombre(CampoSatelitesMenu.NOMBRE_SPRITE_SATELITE);
        assertNotNull(sprite, "El sprite pixel art satelite.png debe existir y cargarse");
        assertFalse(sprite.isError(), "El sprite satelite.png no debe presentar errores de decodificacion");
        assertTrue(sprite.getWidth() > 0 && sprite.getHeight() > 0);
    }

    @Test
    void testMenuPrincipalCincoSatelitesIzquierdaYCincoDerechaGirandoSobreSuPropioEje() {
        PantallaMenuPrincipal menu = new PantallaMenuPrincipal(() -> {}, List::of, r -> {}, () -> {});
        CampoSatelitesMenu campo = menu.getCampoSatelites();
        double ancho = menu.getCanvasFondo().getWidth();
        double alto = menu.getCanvasFondo().getHeight();

        List<SateliteDecorativoMenu> izquierda = campo.obtenerSatelites(SateliteDecorativoMenu.Lado.IZQUIERDA);
        List<SateliteDecorativoMenu> derecha = campo.obtenerSatelites(SateliteDecorativoMenu.Lado.DERECHA);
        List<SateliteDecorativoMenu> todos = campo.obtenerSatelites(null);

        assertEquals(5, izquierda.size(), "Deben existir exactamente 5 satelites a la izquierda");
        assertEquals(5, derecha.size(), "Deben existir exactamente 5 satelites a la derecha");
        assertEquals(10, todos.size());

        for (SateliteDecorativoMenu izq : izquierda) {
            assertTrue(izq.calcularCentro(ancho, alto).x() < ancho * 0.30, "Satelite izquierdo en franja izquierda");
        }
        for (SateliteDecorativoMenu der : derecha) {
            assertTrue(der.calcularCentro(ancho, alto).x() > ancho * 0.70, "Satelite derecho en franja derecha");
        }

        List<Double> angulosAntes = todos.stream().map(s -> s.getEstado().anguloGrados()).toList();
        List<Double> centrosYAntes = todos.stream().map(s -> s.calcularCentro(ancho, alto).y()).toList();

        menu.avanzarYRenderizarFondo(0.5);

        for (int i = 0; i < todos.size(); i++) {
            SateliteDecorativoMenu sat = todos.get(i);
            assertNotEquals(angulosAntes.get(i), sat.getEstado().anguloGrados(), 1e-6, "Debe girar sobre su propio eje");
            assertEquals(centrosYAntes.get(i), sat.calcularCentro(ancho, alto).y(), 1e-6, "Sin clic su posicion Y no cae");
        }
    }

    @Test
    void testClicEnSateliteDelMenuIniciaCaidaConGravedadPromedioHastaSalirDePantalla() {
        PantallaMenuPrincipal menu = new PantallaMenuPrincipal(() -> {}, List::of, r -> {}, () -> {});
        double ancho = menu.getCanvasFondo().getWidth();
        double alto = menu.getCanvasFondo().getHeight();

        SateliteDecorativoMenu satIzq = menu.getCampoSatelites().obtenerSatelites(SateliteDecorativoMenu.Lado.IZQUIERDA).get(0);
        SateliteDecorativoMenu satDer = menu.getCampoSatelites().obtenerSatelites(SateliteDecorativoMenu.Lado.DERECHA).get(2);
        Vector2D posIzq = satIzq.calcularCentro(ancho, alto);
        Vector2D posDer = satDer.calcularCentro(ancho, alto);

        MouseEvent clicIzq = new MouseEvent(
                MouseEvent.MOUSE_PRESSED, posIzq.x(), posIzq.y(), posIzq.x(), posIzq.y(),
                MouseButton.PRIMARY, 1, false, false, false, false,
                true, false, false, true, false, true, null
        );
        menu.getCanvasFondo().getOnMousePressed().handle(clicIzq);
        assertTrue(menu.manejarClicSatelite(posDer.x(), posDer.y()));

        assertTrue(satIzq.getEstado().cayendo());
        assertTrue(satDer.getEstado().cayendo());

        menu.avanzarYRenderizarFondo(0.4);
        double caidaEsperada = 0.5 * SateliteDecorativoMenu.GRAVEDAD_PROMEDIO_PX_SEG2 * 0.4 * 0.4;
        assertEquals(caidaEsperada, satIzq.getEstado().desplazamientoCaidaY(), 1e-4);
        assertTrue(satIzq.getEstado().velocidadCaidaY() > 0.0);
        assertTrue(satIzq.calcularCentro(ancho, alto).y() > posIzq.y());

        for (int paso = 0; paso < 20; paso++) {
            menu.avanzarYRenderizarFondo(0.15);
        }
        assertTrue(satIzq.estaFueraDePantalla(alto), "El satelite clicado debe salir por debajo de la pantalla");
        assertTrue(satDer.estaFueraDePantalla(alto), "El satelite derecho clicado debe salir de pantalla");
        assertFalse(menu.manejarClicSatelite(posIzq.x(), satIzq.calcularCentro(ancho, alto).y()));

        long sinCaer = menu.getCampoSatelites().obtenerSatelites(null).stream()
                .filter(s -> !s.getEstado().cayendo())
                .count();
        assertEquals(8, sinCaer, "Los satelites no clicados deben permanecer girando en su lugar");

        menu.mostrarMenuPrincipal();
        assertTrue(menu.getCampoSatelites().obtenerSatelites(null).stream().noneMatch(s -> s.getEstado().cayendo()));
    }

    @Test
    void testSatelitesDelMenuSonSoloEsteticosSinEfectoEnSimulacionNiPartida() {
        SimulacionSolar simulacion = new SimulacionSolar(1280, 720);
        double minInicial = simulacion.getInventario().getRecurso(TipoRecurso.MINERALES);
        double eneInicial = simulacion.getInventario().getRecurso(TipoRecurso.ENERGIA);

        PantallaMenuPrincipal menu = new PantallaMenuPrincipal(simulacion::reiniciar, List::of, r -> {}, () -> {});
        double ancho = menu.getCanvasFondo().getWidth();
        double alto = menu.getCanvasFondo().getHeight();

        for (SateliteDecorativoMenu sat : menu.getCampoSatelites().obtenerSatelites(null)) {
            Vector2D centro = sat.calcularCentro(ancho, alto);
            menu.manejarClicSatelite(centro.x(), centro.y());
        }
        menu.avanzarYRenderizarFondo(2.5);
        menu.ocultar();

        assertTrue(simulacion.getSistemaSolar().getCuerpos().isEmpty(), "La simulacion no debe recibir ningun cuerpo del menu");
        assertEquals(0L, simulacion.getReloj().getTickActual());
        assertEquals(minInicial, simulacion.getInventario().getRecurso(TipoRecurso.MINERALES), 1e-9);
        assertEquals(eneInicial, simulacion.getInventario().getRecurso(TipoRecurso.ENERGIA), 1e-9);
        assertFalse(menu.isVisible());
    }

    private static double sumarValores(Map<TipoRecurso, Double> mapa) {
        return mapa.values().stream().mapToDouble(Double::doubleValue).sum();
    }
}
