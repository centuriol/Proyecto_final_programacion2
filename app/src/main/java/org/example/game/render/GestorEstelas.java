package org.example.game.render;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.motor.ConversorCoordenadas;
import org.example.game.motor.TransformadorCoordenadas;
import org.example.game.motor.Vector2D;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Gestiona el historial y dibujo de estelas orbitales de los cuerpos celestes.
 *
 * Principios SOLID:
 * - SRP: Responsabilidad exclusiva de almacenar puntos historicos y renderizar las colas de estela.
 */
public class GestorEstelas {

    private final Map<CuerpoCeleste, Deque<Vector2D>> estelas = new HashMap<>();
    private final ConversorCoordenadas conversor;
    private static final int MAX_PUNTOS_ESTELA = 45;

    public GestorEstelas() {
        this(new TransformadorCoordenadas());
    }

    public GestorEstelas(ConversorCoordenadas conversor) {
        this.conversor = conversor != null ? conversor : new TransformadorCoordenadas();
    }

    public void actualizar(List<CuerpoCeleste> cuerpos) {
        if (cuerpos == null) {
            estelas.clear();
            return;
        }

        // Limpiar cuerpos eliminados
        estelas.keySet().removeIf(c -> !cuerpos.contains(c));

        for (CuerpoCeleste c : cuerpos) {
            Deque<Vector2D> cola = estelas.computeIfAbsent(c, k -> new ArrayDeque<>());
            cola.addLast(c.getPosicion());
            if (cola.size() > MAX_PUNTOS_ESTELA) {
                cola.removeFirst();
            }
        }
    }

    public void dibujar(GraphicsContext gc, double anchoCanvas, double altoCanvas) {
        for (Map.Entry<CuerpoCeleste, Deque<Vector2D>> entry : estelas.entrySet()) {
            CuerpoCeleste c = entry.getKey();
            Deque<Vector2D> pts = entry.getValue();
            if (pts.size() < 2) continue;

            Color baseCol = RenderizadorPixelArt.colorWeb(c.getColorHex());
            int i = 0;
            int total = pts.size();

            Vector2D prev = null;
            for (Vector2D pt : pts) {
                if (prev != null) {
                    double progress = (double) i / total;
                    double x1 = conversor.fisicaAXJavaFX(prev.x(), anchoCanvas);
                    double y1 = conversor.fisicaAYJavaFX(prev.y(), altoCanvas);
                    double x2 = conversor.fisicaAXJavaFX(pt.x(), anchoCanvas);
                    double y2 = conversor.fisicaAYJavaFX(pt.y(), altoCanvas);

                    gc.setStroke(Color.color(baseCol.getRed(), baseCol.getGreen(), baseCol.getBlue(), progress * 0.45));
                    gc.setLineWidth(Math.max(1.0, progress * 2.5));
                    gc.strokeLine(x1, y1, x2, y2);
                }
                prev = pt;
                i++;
            }
        }
    }

    public void limpiar() {
        estelas.clear();
    }

    public Map<CuerpoCeleste, Deque<Vector2D>> getEstelas() {
        return estelas;
    }
}
