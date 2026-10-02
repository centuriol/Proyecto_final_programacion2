package org.example;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.render.GestorImagenes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.jupiter.api.Assertions.*;

class GestorImagenesTest {

    @BeforeAll
    static void initJFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {}
    }

    @Test
    void testCargarImagenesItemsPorTipo() {
        Image imgSol = GestorImagenes.getImagen(TipoCuerpo.ESTRELLA);
        assertNotNull(imgSol, "La imagen del Sol (ESTRELLA) debe cargarse correctamente");
        assertTrue(imgSol.getWidth() > 0, "El ancho del sprite del Sol debe ser mayor a 0");
        assertTrue(imgSol.getHeight() > 0, "El alto del sprite del Sol debe ser mayor a 0");

        Image imgLuna = GestorImagenes.getImagen(TipoCuerpo.LUNA);
        assertNotNull(imgLuna, "La imagen de la Luna debe cargarse correctamente");
        assertTrue(imgLuna.getWidth() > 0, "El ancho del sprite de la Luna debe ser mayor a 0");

        Image imgPlanetaPorro = GestorImagenes.getImagen(TipoCuerpo.PLANETA_ROCOSO);
        assertNotNull(imgPlanetaPorro, "La imagen del Planeta Porro (PLANETA_ROCOSO) debe cargarse correctamente");
        assertTrue(imgPlanetaPorro.getWidth() > 0, "El ancho del sprite del Planeta Porro debe ser mayor a 0");

        Image imgPlanetaAgua = GestorImagenes.getImagen(TipoCuerpo.PLANETA_AGUA);
        assertNotNull(imgPlanetaAgua, "La imagen del Planeta de Agua (PLANETA_AGUA) debe cargarse correctamente");
        assertTrue(imgPlanetaAgua.getWidth() > 0, "El ancho del sprite de Planeta de Agua debe ser mayor a 0");

        Image imgPlanetaLava = GestorImagenes.getImagen(TipoCuerpo.PLANETA_LAVA);
        assertNotNull(imgPlanetaLava, "La imagen del Planeta Lava (PLANETA_LAVA) debe cargarse correctamente");
        assertTrue(imgPlanetaLava.getWidth() > 0, "El ancho del sprite de Planeta Lava debe ser mayor a 0");
    }

    @Test
    void testCargarImagenFondo() {
        Image fondo = GestorImagenes.getImagenFondo();
        assertNotNull(fondo, "La imagen de fondo espacial (fondo_final) debe cargarse correctamente");
        assertTrue(fondo.getWidth() > 0, "El ancho del fondo debe ser mayor a 0");
        assertTrue(fondo.getHeight() > 0, "El alto del fondo debe ser mayor a 0");

        Image fondoFinalPorNombre = GestorImagenes.getImagenPorNombre("fondo_final");
        assertNotNull(fondoFinalPorNombre, "Debe cargarse la imagen por el nombre fondo_final");

        Image fondoPrimePorNombre = GestorImagenes.getImagenPorNombre("fondo_prime");
        assertNotNull(fondoPrimePorNombre, "Debe cargarse la imagen por el nombre fondo_prime");

        Image fondoAnterior = GestorImagenes.getImagenPorNombre("imagen_fonde");
        assertNotNull(fondoAnterior, "Debe cargarse la imagen anterior por el nombre imagen_fonde");
    }

    @Test
    void testRenderizadorTieneImagenFondo() {
        org.example.game.render.RenderizadorPixelArt renderizador = new org.example.game.render.RenderizadorPixelArt(1920, 1080);
        assertNotNull(renderizador.getImagenFondo(), "El renderizador debe inicializarse con la imagen de fondo");
    }

    @Test
    void testRedimensionamientoCanvasYRenderizador() {
        org.example.game.ui.ResizableCanvas canvas = new org.example.game.ui.ResizableCanvas(800, 600);
        assertTrue(canvas.isResizable());
        canvas.resize(1366, 768);
        assertEquals(1366, canvas.getWidth());
        assertEquals(768, canvas.getHeight());

        org.example.game.render.RenderizadorPixelArt renderizador = new org.example.game.render.RenderizadorPixelArt(800, 600);
        assertEquals(800, renderizador.getAnchoCanvas());
        assertEquals(600, renderizador.getAltoCanvas());

        renderizador.setDimensiones(1366, 768);
        assertEquals(1366, renderizador.getAnchoCanvas());
        assertEquals(768, renderizador.getAltoCanvas());
        assertNotNull(renderizador.getImagenFondo());
    }

    @Test
    void testMovimientoFondoYEscalaVelocidad() {
        org.example.game.render.RenderizadorPixelArt renderizador = new org.example.game.render.RenderizadorPixelArt(1000, 500);
        assertEquals(0.0, renderizador.getDesplazamientoFondoX(), 0.001);

        // A velocidad 1x (1 segundo)
        renderizador.avanzarFondo(1.0, 1.0);
        double velBase = renderizador.getVelocidadBaseFondo();
        assertEquals(velBase, renderizador.getDesplazamientoFondoX(), 0.001);

        // A velocidad 2x (1 segundo), debe avanzar el doble
        renderizador.avanzarFondo(1.0, 2.0);
        assertEquals(velBase + 2.0 * velBase, renderizador.getDesplazamientoFondoX(), 0.001);

        // A velocidad 4x (1 segundo), debe avanzar el cuádruple
        renderizador.avanzarFondo(1.0, 4.0);
        assertEquals(velBase * 7.0, renderizador.getDesplazamientoFondoX(), 0.001);

        // Si se pausa (multiplicador 0), no debe avanzar
        renderizador.avanzarFondo(1.0, 0.0);
        assertEquals(velBase * 7.0, renderizador.getDesplazamientoFondoX(), 0.001);
    }

    @Test
    void testRotacionAxialItemsSobreSuPropioEje() {
        org.example.game.render.RenderizadorPixelArt renderizador = new org.example.game.render.RenderizadorPixelArt(1000, 500);
        assertEquals(0.0, renderizador.getTiempoAnimacionSegundos(), 0.001);

        // A velocidad 1x (1 segundo)
        renderizador.avanzarAnimaciones(1.0, 1.0);
        assertEquals(1.0, renderizador.getTiempoAnimacionSegundos(), 0.001);

        // A velocidad 2x (1 segundo), suma 2 segundos de tiempo animado
        renderizador.avanzarAnimaciones(1.0, 2.0);
        assertEquals(3.0, renderizador.getTiempoAnimacionSegundos(), 0.001);

        // Pausa (0.0), no debe avanzar
        renderizador.avanzarAnimaciones(1.0, 0.0);
        assertEquals(3.0, renderizador.getTiempoAnimacionSegundos(), 0.001);
    }

    @Test
    void testTieneImagen() {
        assertTrue(GestorImagenes.tieneImagen(TipoCuerpo.ESTRELLA));
        assertTrue(GestorImagenes.tieneImagen(TipoCuerpo.PLANETA_ROCOSO));
        assertTrue(GestorImagenes.tieneImagen(TipoCuerpo.LUNA));
        assertTrue(GestorImagenes.tieneImagen(TipoCuerpo.PLANETA_AGUA));
        assertTrue(GestorImagenes.tieneImagen(TipoCuerpo.PLANETA_LAVA));
        assertFalse(GestorImagenes.tieneImagen(TipoCuerpo.AGUJERO_NEGRO));
    }

    @Test
    void testCrearImageView() {
        ImageView iv = GestorImagenes.crearImageView(TipoCuerpo.ESTRELLA, 32, 32);
        assertNotNull(iv);
        assertEquals(32, iv.getFitWidth());
        assertEquals(32, iv.getFitHeight());
        assertFalse(iv.isSmooth(), "La imagen debe mantener smooth=false para nitidez Pixel Art");
    }

    @Test
    void testCrearContenedorIcono() {
        StackPane contenedor = GestorImagenes.crearContenedorIcono(TipoCuerpo.LUNA, 32, 32);
        assertNotNull(contenedor);
        assertEquals(1, contenedor.getChildren().size());
        assertTrue(contenedor.getChildren().get(0) instanceof ImageView);
    }

    @Test
    void testDecodificarPixilDirecto() throws Exception {
        File pixilFile = new File("src/main/java/org/example/game/Imagenes/sol.pixil");
        if (!pixilFile.exists()) {
            pixilFile = new File("app/src/main/java/org/example/game/Imagenes/sol.pixil");
        }
        assertTrue(pixilFile.exists(), "El archivo sol.pixil debe existir");
        try (FileInputStream fis = new FileInputStream(pixilFile)) {
            Image img = GestorImagenes.decodificarPixil(fis);
            assertNotNull(img, "Debe decodificar la imagen embebida en sol.pixil");
            assertEquals(76, (int) img.getWidth());
            assertEquals(77, (int) img.getHeight());
        }
    }

    @Test
    void testNombresItemsHotbarYDescripcion() {
        assertEquals("Sol", TipoCuerpo.ESTRELLA.nombre);
        assertEquals("Planeta Porro", TipoCuerpo.PLANETA_ROCOSO.nombre);
        assertEquals("Luna", TipoCuerpo.LUNA.nombre);
        assertEquals("Planeta de Agua", TipoCuerpo.PLANETA_AGUA.nombre);
        assertEquals("Planeta Lava", TipoCuerpo.PLANETA_LAVA.nombre);

        assertEquals("Sol", org.example.game.ui.VentanaDescripcionCuerpo.obtenerNombrePorDefecto(TipoCuerpo.ESTRELLA));
        assertEquals("Planeta Porro", org.example.game.ui.VentanaDescripcionCuerpo.obtenerNombrePorDefecto(TipoCuerpo.PLANETA_ROCOSO));
        assertEquals("Luna", org.example.game.ui.VentanaDescripcionCuerpo.obtenerNombrePorDefecto(TipoCuerpo.LUNA));
        assertEquals("Planeta de Agua", org.example.game.ui.VentanaDescripcionCuerpo.obtenerNombrePorDefecto(TipoCuerpo.PLANETA_AGUA));
        assertEquals("Planeta Lava", org.example.game.ui.VentanaDescripcionCuerpo.obtenerNombrePorDefecto(TipoCuerpo.PLANETA_LAVA));

        assertTrue(org.example.game.ui.VentanaDescripcionCuerpo.obtenerSubtitulo(TipoCuerpo.ESTRELLA).contains("Sol"));
        assertTrue(org.example.game.ui.VentanaDescripcionCuerpo.obtenerSubtitulo(TipoCuerpo.PLANETA_ROCOSO).contains("Planeta Porro"));
        assertTrue(org.example.game.ui.VentanaDescripcionCuerpo.obtenerSubtitulo(TipoCuerpo.LUNA).contains("Luna"));
        assertTrue(org.example.game.ui.VentanaDescripcionCuerpo.obtenerSubtitulo(TipoCuerpo.PLANETA_AGUA).contains("Planeta de Agua"));
        assertTrue(org.example.game.ui.VentanaDescripcionCuerpo.obtenerSubtitulo(TipoCuerpo.PLANETA_LAVA).contains("Planeta Lava"));

        org.example.game.simulacion.SimulacionSolar sim = new org.example.game.simulacion.SimulacionSolar(1000, 1000);
        org.example.game.ui.BarraInventarioHotbar hotbar = new org.example.game.ui.BarraInventarioHotbar(sim);
        assertEquals(5, hotbar.getChildren().size());
    }
}
