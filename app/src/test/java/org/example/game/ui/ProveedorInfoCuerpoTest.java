package org.example.game.ui;

import org.example.game.cuerpo.TipoCuerpo;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(JavaFXExtension.class)
class ProveedorInfoCuerpoTest {

    private final ProveedorInfoCuerpo proveedor = new SeccionInfoCuerpo();

    @Test
    @DisplayName("Obtiene descriptores de texto correctos por tipo")
    void testDescriptores() {
        assertEquals("Sol", proveedor.obtenerNombrePorDefecto(TipoCuerpo.ESTRELLA));
        assertEquals("Planeta Porro", proveedor.obtenerNombrePorDefecto(TipoCuerpo.PLANETA_ROCOSO));
        assertNotNull(proveedor.obtenerLore(TipoCuerpo.PLANETA_AGUA));
        assertNotNull(proveedor.obtenerSubtitulo(TipoCuerpo.LUNA));
        assertNotNull(proveedor.obtenerIcono(TipoCuerpo.METEORITO));
    }

    @Test
    @DisplayName("Crea sección de características para tipos conocidos")
    void testSeccionCaracteristicas() {
        var box = proveedor.crearSeccionCaracteristicas(TipoCuerpo.PLANETA_LAVA);
        assertNotNull(box);
        assertFalse(box.getChildren().isEmpty());
    }
}
