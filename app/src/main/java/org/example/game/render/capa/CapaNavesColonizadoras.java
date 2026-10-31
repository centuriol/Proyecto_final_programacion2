package org.example.game.render.capa;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.game.colonizacion.NaveColonizadora;
import org.example.game.motor.ConversorCoordenadas;
import org.example.game.motor.Vector2D;
import org.example.game.render.UtilidadRender;

import java.util.List;
import java.util.function.Supplier;

/**
 * Capa responsable de renderizar las naves colonizadoras activas en el lienzo (SRP).
 */
public class CapaNavesColonizadoras {

    public void dibujar(GraphicsContext gc,
                        Supplier<List<NaveColonizadora>> proveedorNaves,
                        ConversorCoordenadas conversor,
                        double anchoCanvas,
                        double altoCanvas) {
        if (gc == null || proveedorNaves == null || conversor == null) return;
        List<NaveColonizadora> naves = proveedorNaves.get();
        if (naves == null || naves.isEmpty()) return;

        for (NaveColonizadora nave : naves) {
            if (nave == null || nave.isCompletada() || nave.isPerdida()) continue;
            Vector2D pos = nave.getPosicion();
            double sx = conversor.fisicaAXJavaFX(pos.x(), anchoCanvas);
            double sy = conversor.fisicaAYJavaFX(pos.y(), altoCanvas);

            gc.save();
            gc.setFill(UtilidadRender.colorWeb("#50fa7b"));
            gc.fillOval(sx - 4, sy - 4, 8, 8);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1.0);
            gc.strokeOval(sx - 5, sy - 5, 10, 10);
            gc.restore();
        }
    }
}
