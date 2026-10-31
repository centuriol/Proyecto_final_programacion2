package org.example;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.render.RenderizadorPixelArt;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.ui.BarraInventarioHotbar;
import org.example.game.ui.ResizableCanvas;
import org.example.game.ui.VentanaDescripcionCuerpo;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
class RenderizadorAnimacionYFondoTest {

    @Test
    void testRenderizadorTieneImagenFondo() {
        RenderizadorPixelArt renderizador = new RenderizadorPixelArt(1920, 1080);
        assertNotNull(renderizador.getImagenFondo(), "El renderizador debe inicializarse con la imagen de fondo");
    }

    @Test
    void testRedimensionamientoCanvasYRenderizador() {
        ResizableCanvas canvas = new ResizableCanvas(800, 600);
        assertTrue(canvas.isResizable());
        canvas.resize(1366, 768);
        assertEquals(1366, canvas.getWidth());
        assertEquals(768, canvas.getHeight());

        RenderizadorPixelArt renderizador = new RenderizadorPixelArt(800, 600);
        assertEquals(800, renderizador.getAnchoCanvas());
        assertEquals(600, renderizador.getAltoCanvas());

        renderizador.setDimensiones(1366, 768);
        assertEquals(1366, renderizador.getAnchoCanvas());
        assertEquals(768, renderizador.getAltoCanvas());
        assertNotNull(renderizador.getImagenFondo());
    }

    @Test
    void testMovimientoFondoYEscalaVelocidad() {
        RenderizadorPixelArt renderizador = new RenderizadorPixelArt(1000, 500);
        assertEquals(0.0, renderizador.getDesplazamientoFondoX(), 0.001);

        renderizador.avanzarFondo(1.0, 1.0);
        double velBase = renderizador.getVelocidadBaseFondo();
        assertEquals(velBase, renderizador.getDesplazamientoFondoX(), 0.001);

        renderizador.avanzarFondo(1.0, 2.0);
        assertEquals(velBase + 2.0 * velBase, renderizador.getDesplazamientoFondoX(), 0.001);

        renderizador.avanzarFondo(1.0, 4.0);
        assertEquals(velBase * 7.0, renderizador.getDesplazamientoFondoX(), 0.001);

        renderizador.avanzarFondo(1.0, 0.0);
        assertEquals(velBase * 7.0, renderizador.getDesplazamientoFondoX(), 0.001);
    }

    @Test
    void testRotacionAxialItemsSobreSuPropioEje() {
        RenderizadorPixelArt renderizador = new RenderizadorPixelArt(1000, 500);
        assertEquals(0.0, renderizador.getTiempoAnimacionSegundos(), 0.001);

        renderizador.avanzarAnimaciones(1.0, 1.0);
        assertEquals(1.0, renderizador.getTiempoAnimacionSegundos(), 0.001);

        renderizador.avanzarAnimaciones(1.0, 2.0);
        assertEquals(3.0, renderizador.getTiempoAnimacionSegundos(), 0.001);

        renderizador.avanzarAnimaciones(1.0, 0.0);
        assertEquals(3.0, renderizador.getTiempoAnimacionSegundos(), 0.001);
    }

    @Test
    void testNombresItemsHotbarYDescripcion() {
        assertEquals("Sol", TipoCuerpo.ESTRELLA.nombre);
        assertEquals("Planeta Porro", TipoCuerpo.PLANETA_ROCOSO.nombre);
        assertEquals("Luna", TipoCuerpo.LUNA.nombre);
        assertEquals("Planeta de Agua", TipoCuerpo.PLANETA_AGUA.nombre);
        assertEquals("Planeta Lava", TipoCuerpo.PLANETA_LAVA.nombre);

        org.example.game.ui.VisualizadorDescripcion ventana = new VentanaDescripcionCuerpo();
        assertEquals("Sol", ventana.nombrePorDefecto(TipoCuerpo.ESTRELLA));
        assertEquals("Planeta Porro", ventana.nombrePorDefecto(TipoCuerpo.PLANETA_ROCOSO));
        assertEquals("Luna", ventana.nombrePorDefecto(TipoCuerpo.LUNA));
        assertEquals("Planeta de Agua", ventana.nombrePorDefecto(TipoCuerpo.PLANETA_AGUA));
        assertEquals("Planeta Lava", ventana.nombrePorDefecto(TipoCuerpo.PLANETA_LAVA));

        assertTrue(ventana.subtitulo(TipoCuerpo.ESTRELLA).contains("Sol"));
        assertTrue(ventana.subtitulo(TipoCuerpo.PLANETA_ROCOSO).contains("Planeta Porro"));
        assertTrue(ventana.subtitulo(TipoCuerpo.LUNA).contains("Luna"));
        assertTrue(ventana.subtitulo(TipoCuerpo.PLANETA_AGUA).contains("Planeta de Agua"));
        assertTrue(ventana.subtitulo(TipoCuerpo.PLANETA_LAVA).contains("Planeta Lava"));

        SimulacionSolar sim = new SimulacionSolar(1000, 1000);
        BarraInventarioHotbar hotbar = new BarraInventarioHotbar(sim);
        assertEquals(BarraInventarioHotbar.CUERPOS_HOTBAR.length, hotbar.getChildren().size());
    }

    @Test
    void testAvanceFondoMuchasVecesElAnchoMantieneDesplazamientoEntreCeroYAncho() {
        double ancho = 1000.0;
        double alto = 600.0;
        RenderizadorPixelArt renderizador = new RenderizadorPixelArt(ancho, alto);

        // Avanzar el fondo acumulando muchas veces el ancho con distintos multiplicadores de velocidad
        for (int i = 0; i < 50; i++) {
            renderizador.avanzarFondo(100.0, 5.0);
            double desp = renderizador.getDesplazamientoFondoX();
            assertTrue(desp >= 0.0 && desp <= ancho,
                    "El desplazamiento debe quedar siempre entre 0 y el ancho (" + ancho + "), pero fue: " + desp);
        }

        // Avance masivo directo equivalente a cientos de veces el ancho
        renderizador.avanzarFondo(250000.0, 8.0);
        double despMasivo = renderizador.getDesplazamientoFondoX();
        assertTrue(despMasivo >= 0.0 && despMasivo <= ancho,
                "Tras un avance masivo, el desplazamiento debe quedar entre 0 y el ancho, pero fue: " + despMasivo);

        // Verificar tambien comportamiento con ventana redimensionada
        double nuevoAncho = 1440.0;
        renderizador.setDimensiones(nuevoAncho, 900.0);
        for (int i = 0; i < 20; i++) {
            renderizador.avanzarFondo(300.0, 4.0);
            double despRedim = renderizador.getDesplazamientoFondoX();
            assertTrue(despRedim >= 0.0 && despRedim <= nuevoAncho,
                    "Con ventana redimensionada, el desplazamiento debe quedar entre 0 y el nuevo ancho, pero fue: " + despRedim);
        }
    }
}
