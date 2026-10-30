package org.example;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.render.GestorImagenes;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(JavaFXExtension.class)
class GestorSpritesYPixilTest {

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
        assertNotNull(imgPlanetaPorro, "La imagen del Planeta Porro debe cargarse correctamente");
        assertTrue(imgPlanetaPorro.getWidth() > 0, "El ancho del sprite debe ser mayor a 0");

        Image imgPlanetaAgua = GestorImagenes.getImagen(TipoCuerpo.PLANETA_AGUA);
        assertNotNull(imgPlanetaAgua, "La imagen del Planeta de Agua debe cargarse correctamente");
        assertTrue(imgPlanetaAgua.getWidth() > 0, "El ancho del sprite debe ser mayor a 0");

        Image imgPlanetaLava = GestorImagenes.getImagen(TipoCuerpo.PLANETA_LAVA);
        assertNotNull(imgPlanetaLava, "La imagen del Planeta Lava debe cargarse correctamente");
        assertTrue(imgPlanetaLava.getWidth() > 0, "El ancho del sprite debe ser mayor a 0");
    }

    @Test
    void testCargarImagenFondo() {
        Image fondo = GestorImagenes.getImagenFondo();
        assertNotNull(fondo, "La imagen de fondo espacial debe cargarse");
        assertTrue(fondo.getWidth() > 0);
        assertTrue(fondo.getHeight() > 0);

        Image fondoFinalPorNombre = GestorImagenes.getImagenPorNombre("fondo_final");
        assertNotNull(fondoFinalPorNombre);

        Image fondoPrimePorNombre = GestorImagenes.getImagenPorNombre("fondo_prime");
        assertNotNull(fondoPrimePorNombre);

        Image fondoAnterior = GestorImagenes.getImagenPorNombre("imagen_fonde");
        assertNotNull(fondoAnterior);
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
        File pixilFile = new File("src/main/resources/org/example/game/Imagenes/sol.pixil");
        if (!pixilFile.exists()) {
            pixilFile = new File("app/src/main/resources/org/example/game/Imagenes/sol.pixil");
        }
        assertTrue(pixilFile.exists(), "El archivo sol.pixil debe existir");
        try (FileInputStream fis = new FileInputStream(pixilFile)) {
            Image img = GestorImagenes.decodificarPixil(fis);
            assertNotNull(img, "Debe decodificar la imagen embebida en sol.pixil");
            assertEquals(76, (int) img.getWidth());
            assertEquals(77, (int) img.getHeight());
        }
    }
}
