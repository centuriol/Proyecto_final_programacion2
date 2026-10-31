package org.example;
import javafx.scene.canvas.Canvas;
import org.example.game.cuerpo.Estrella;
import org.example.game.cuerpo.Planeta;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.render.CamaraZoom;
import org.example.game.render.EscenaFondoMenu;
import org.example.game.ui.CapaIndicadoresSol;
import org.example.game.ui.PantallaMenuPausa;
import org.example.game.ui.PantallaMenuPrincipal;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
class FondoAnimadoMenuPrincipalTest {

    @Test
    void testMenuPrincipalContieneSolALaIzquierdaYPlanetaPorroALaDerecha() {
        PantallaMenuPrincipal menu = new PantallaMenuPrincipal(() -> {}, List::of, r -> {}, () -> {});
        EscenaFondoMenu escena = menu.getEscenaFondo();

        assertNotNull(escena);
        assertNotNull(menu.getCanvasFondo());
        assertTrue(menu.getChildren().contains(menu.getCanvasFondo()));

        Estrella sol = escena.getSolIzquierdo();
        Planeta planetaPorro = escena.getPlanetaPorroDerecho();

        assertEquals(TipoCuerpo.ESTRELLA, sol.getTipoCuerpo());
        assertEquals("Sol", sol.getNombre());
        assertEquals(TipoCuerpo.PLANETA_ROCOSO, planetaPorro.getTipoCuerpo());
        assertEquals("Planeta Porro", planetaPorro.getNombre());

        double ancho = 1280.0;
        double alto = 720.0;
        double xSol = escena.calcularPosicionSolX(ancho);
        double xPlanetaPorro = escena.calcularPosicionPlanetaPorroX(ancho);
        double yAstros = escena.calcularPosicionAstroY(alto);

        assertTrue(xSol > 0.0 && xSol < ancho * 0.35, "El Sol debe ubicarse claramente a la izquierda");
        assertTrue(xPlanetaPorro > ancho * 0.65 && xPlanetaPorro < ancho, "El Planeta Porro debe ubicarse claramente a la derecha");
        assertTrue(yAstros > alto * 0.25 && yAstros < alto * 0.75);
    }

    @Test
    void testFondoDeEstrellasAnimadoAvanzaPermanentementeEnElMenuPrincipal() {
        App app = new App();
        app.inicializarInterfaz(null, 1280.0, 720.0, null);

        PantallaMenuPrincipal menu = app.getPantallaMenuPrincipal();
        EscenaFondoMenu escena = menu.getEscenaFondo();

        assertTrue(menu.isVisible(), "El menu principal inicia visible");
        assertTrue(app.getSimulacion().getReloj().isEnPausa(), "La simulacion esta en pausa mientras se muestra el menu principal");
        assertEquals(EscenaFondoMenu.TOTAL_ESTRELLAS, escena.getCantidadEstrellas());

        double tiempoInicial = escena.getTiempoAnimacion();
        double derivaInicial = escena.getDesplazamientoEstrellas();

        menu.avanzarYRenderizarFondo(0.20);
        menu.avanzarYRenderizarFondo(0.20);

        assertTrue(escena.getTiempoAnimacion() > tiempoInicial, "El tiempo de animacion del fondo debe avanzar permanentemente");
        assertTrue(escena.getDesplazamientoEstrellas() > derivaInicial, "El campo de estrellas debe desplazarse de forma continua");

        Canvas lienzoPrueba = new Canvas(800.0, 600.0);
        assertDoesNotThrow(() -> escena.dibujar(lienzoPrueba.getGraphicsContext2D(), 800.0, 600.0));
    }

    @Test
    void testComponentesNuevosSinEmojis() {
        String patronEmoji = ".*[\\p{So}\\p{Cn}].*";
        PantallaMenuPausa pausa = new PantallaMenuPausa(() -> {}, () -> true, () -> false, () -> {});
        pausa.getBtnGuardar().fire();

        assertFalse(pausa.getBtnContinuar().getText().matches(patronEmoji));
        assertFalse(pausa.getBtnGuardar().getText().matches(patronEmoji));
        assertFalse(pausa.getBtnCargar().getText().matches(patronEmoji));
        assertFalse(pausa.getBtnSalirMenu().getText().matches(patronEmoji));
        assertFalse(pausa.getLblEstado().getText().matches(patronEmoji));

        CamaraZoom camara = new CamaraZoom();
        CapaIndicadoresSol capa = new CapaIndicadoresSol(camara);
        capa.actualizar(List.of(new Estrella("Sol Lejano", TipoCuerpo.ESTRELLA.masaBase, 4000.0, 0.0)), 1280.0, 720.0);
        assertEquals(1, capa.getBotonesIndicadores().size());
        assertFalse(capa.getBotonesIndicadores().get(0).getText().matches(patronEmoji));
    }
}
