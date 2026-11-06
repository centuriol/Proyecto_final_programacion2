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

    @Test
    void testCargarSpritesPersonasYEscudo() {
        Image imgEscudo = gestor.obtenerImagenPorNombre(org.example.game.render.capa.CapaEscudoPlanetario.NOMBRE_SPRITE_ESCUDO);
        assertNotNull(imgEscudo, "El sprite del escudo planetario debe cargarse correctamente");
        assertTrue(imgEscudo.getWidth() > 0 && imgEscudo.getHeight() > 0);

        for (String nombrePersona : org.example.game.render.cuerpo.RenderizadorPlaneta.SPRITES_PERSONAS) {
            Image imgPersona = gestor.obtenerImagenPorNombre(nombrePersona);
            assertNotNull(imgPersona, "El sprite de persona '" + nombrePersona + "' debe cargarse correctamente");
            assertEquals(8, (int) imgPersona.getWidth());
            assertEquals(8, (int) imgPersona.getHeight());
        }
    }

    @Test
    void testCargarSpritesExplosionYAnimacionGifEnColision() {
        assertEquals(9, org.example.game.efectos.GestorEfectos.SPRITES_EXPLOSION.size());
        for (String nombreExplosion : org.example.game.efectos.GestorEfectos.SPRITES_EXPLOSION) {
            Image imgExp = gestor.obtenerImagenPorNombre(nombreExplosion);
            assertNotNull(imgExp, "El frame de explosion '" + nombreExplosion + "' debe cargarse correctamente");
            assertTrue(imgExp.getWidth() > 0 && imgExp.getHeight() > 0);
        }

        org.example.game.simulacion.SimulacionSolar sim = new org.example.game.simulacion.SimulacionSolar(1280, 720);
        sim.limpiarCuerpos();
        org.example.game.cuerpo.Planeta planeta = new org.example.game.cuerpo.CuerpoCelesteFactory().crearPlanetaRocoso(0, 0, 1.0, org.example.game.motor.Vector2D.cero());
        org.example.game.cuerpo.Meteorito met = new org.example.game.cuerpo.Meteorito("Met-Impacto", planeta.getMasa() * 0.1, 5, 0, new org.example.game.motor.Vector2D(-5, 0), 200, false, planeta);
        sim.agregarCuerpo(planeta);
        sim.agregarCuerpo(met);

        sim.avanzarTick();
        assertEquals(1, sim.getGestorEfectos().getCantidadExplosionesActivas(), "Debe activarse la animacion GIF de explosion al colisionar");

        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(400, 400);
        for (int i = 0; i < 36; i++) {
            assertDoesNotThrow(() -> sim.getGestorEfectos().dibujar(canvas.getGraphicsContext2D(), 400, 400));
            sim.getGestorEfectos().actualizar();
        }
        assertEquals(0, sim.getGestorEfectos().getCantidadExplosionesActivas(), "La explosion GIF debe finalizar tras recorrer sus 9 frames");
    }
}
