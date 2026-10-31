package org.example.game.render;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.HashMap;
import java.util.Map;
/**
 * SRP: Métodos de utilidad y caché de colores compartidos para el renderizado visual.
 */
public class UtilidadRender {

    private static final UtilidadRender INSTANCIA_POR_DEFECTO = new UtilidadRender();
    private final Map<String, Color> cacheColores = new HashMap<>();
    private final Map<Integer, Color> cacheColoresRgb = new HashMap<>();

    public UtilidadRender() {}

    public static UtilidadRender getInstanciaPorDefecto() {
        return INSTANCIA_POR_DEFECTO;
    }

    public Color obtenerColorWeb(String hex) {
        if (hex == null || hex.isBlank()) return Color.WHITE;
        return cacheColores.computeIfAbsent(hex, Color::web);
    }

    public Color obtenerColorRgb(int r, int g, int b) {
        int key = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
        return cacheColoresRgb.computeIfAbsent(key, k -> Color.rgb(r, g, b));
    }

    public Color obtenerColorRgb(int r, int g, int b, double opacity) {
        int op = (int) Math.round(opacity * 255.0);
        int key = ((op & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
        return cacheColoresRgb.computeIfAbsent(key, k -> Color.rgb(r, g, b, opacity));
    }

    public void discoConContorno(GraphicsContext gc, double cx, double cy, double r, Color colorInterior) {
        gc.setFill(Color.BLACK);
        gc.fillOval(cx - r - 2, cy - r - 2, (r + 2) * 2, (r + 2) * 2);
        gc.setFill(colorInterior);
        gc.fillOval(cx - r, cy - r, r * 2, r * 2);
    }

    public static Color colorWeb(String hex) {
        return INSTANCIA_POR_DEFECTO.obtenerColorWeb(hex);
    }

    public static Color colorRgb(int r, int g, int b) {
        return INSTANCIA_POR_DEFECTO.obtenerColorRgb(r, g, b);
    }

    public static Color colorRgb(int r, int g, int b, double opacity) {
        return INSTANCIA_POR_DEFECTO.obtenerColorRgb(r, g, b, opacity);
    }

    public static void dibujarDiscoConContorno(GraphicsContext gc, double cx, double cy, double r, Color colorInterior) {
        INSTANCIA_POR_DEFECTO.discoConContorno(gc, cx, cy, r, colorInterior);
    }
}
