package org.example;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.render.GestorImagenes;
import org.example.game.render.RepositorioSprites;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(JavaFXExtension.class)
class GestorSpritesYPixilTest {

    private final RepositorioSprites gestor = new GestorImagenes();

    @Test
    void testCargarImagenesItemsPorTipo() {
        Image imgSol = gestor.obtenerImagen(TipoCuerpo.ESTRELLA);
        assertNotNull(imgSol, "La imagen del Sol (ESTRELLA) debe cargarse correctamente");
        assertTrue(imgSol.getWidth() > 0, "El ancho del sprite del Sol debe ser mayor a 0");
        assertTrue(imgSol.getHeight() > 0, "El alto del sprite del Sol debe ser mayor a 0");

        Image imgLuna = gestor.obtenerImagen(TipoCuerpo.LUNA);
        assertNotNull(imgLuna, "La imagen de la Luna debe cargarse correctamente");
        assertTrue(imgLuna.getWidth() > 0, "El ancho del sprite de la Luna debe ser mayor a 0");

        Image imgPlanetaPorro = gestor.obtenerImagen(TipoCuerpo.PLANETA_ROCOSO);
        assertNotNull(imgPlanetaPorro, "La imagen del Planeta Porro debe cargarse correctamente");
        assertTrue(imgPlanetaPorro.getWidth() > 0, "El ancho del sprite debe ser mayor a 0");

        Image imgPlanetaAgua = gestor.obtenerImagen(TipoCuerpo.PLANETA_AGUA);
        assertNotNull(imgPlanetaAgua, "La imagen del Planeta de Agua debe cargarse correctamente");
        assertTrue(imgPlanetaAgua.getWidth() > 0, "El ancho del sprite debe ser mayor a 0");

        Image imgPlanetaLava = gestor.obtenerImagen(TipoCuerpo.PLANETA_LAVA);
        assertNotNull(imgPlanetaLava, "La imagen del Planeta Lava debe cargarse correctamente");
        assertTrue(imgPlanetaLava.getWidth() > 0, "El ancho del sprite debe ser mayor a 0");
    }

    @Test
    void testCargarImagenFondo() {
        Image fondo = gestor.obtenerImagenFondo();
        assertNotNull(fondo, "La imagen de fondo espacial debe cargarse");
        assertTrue(fondo.getWidth() > 0);
        assertTrue(fondo.getHeight() > 0);

        Image fondoFinalPorNombre = gestor.obtenerImagenPorNombre("fondo_final");
        assertNotNull(fondoFinalPorNombre);

        Image fondoPrimePorNombre = gestor.obtenerImagenPorNombre("fondo_prime");
        assertNotNull(fondoPrimePorNombre);

        Image fondoAnterior = gestor.obtenerImagenPorNombre("imagen_fonde");
        assertNotNull(fondoAnterior);
    }

    @Test
    void testTieneImagen() {
        assertTrue(gestor.tieneSprite(TipoCuerpo.ESTRELLA));
        assertTrue(gestor.tieneSprite(TipoCuerpo.PLANETA_ROCOSO));
        assertTrue(gestor.tieneSprite(TipoCuerpo.LUNA));
        assertTrue(gestor.tieneSprite(TipoCuerpo.PLANETA_AGUA));
        assertTrue(gestor.tieneSprite(TipoCuerpo.PLANETA_LAVA));
        assertFalse(gestor.tieneSprite(TipoCuerpo.AGUJERO_NEGRO));
    }

    @Test
    void testCrearImageView() {
        ImageView iv = gestor.crearVistaIcono(TipoCuerpo.ESTRELLA, 32, 32);
        assertNotNull(iv);
        assertEquals(32, iv.getFitWidth());
        assertEquals(32, iv.getFitHeight());
        assertFalse(iv.isSmooth(), "La imagen debe mantener smooth=false para nitidez Pixel Art");
    }

    @Test
    void testCrearContenedorIcono() {
        StackPane contenedor = gestor.crearContenedorSprite(TipoCuerpo.LUNA, 32, 32);
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
            Image img = gestor.decodificarPixil(fis);
            assertNotNull(img, "Debe decodificar la imagen embebida en sol.pixil");
            assertEquals(76, (int) img.getWidth());
            assertEquals(77, (int) img.getHeight());
        }
    }
}
