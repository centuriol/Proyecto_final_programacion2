package org.example.game.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

/**
 * SRP: Métodos de utilidad y caché de colores compartidos para el renderizado visual.
 */
public final class UtilidadRender {

    private static final Map<String, Color> CACHE_COLORES = new HashMap<>();
    private static final Map<Integer, Color> CACHE_COLORES_RGB = new HashMap<>();

    private UtilidadRender() {}

    public static Color colorWeb(String hex) {
        if (hex == null || hex.isBlank()) return Color.WHITE;
        return CACHE_COLORES.computeIfAbsent(hex, Color::web);
    }

    public static Color colorRgb(int r, int g, int b) {
        int key = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
        return CACHE_COLORES_RGB.computeIfAbsent(key, k -> Color.rgb(r, g, b));
    }

    public static Color colorRgb(int r, int g, int b, double opacity) {
        int op = (int) Math.round(opacity * 255.0);
        int key = ((op & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
        return CACHE_COLORES_RGB.computeIfAbsent(key, k -> Color.rgb(r, g, b, opacity));
    }

    public static void dibujarDiscoConContorno(GraphicsContext gc, double cx, double cy, double r, Color colorInterior) {
        gc.setFill(Color.BLACK);
        gc.fillOval(cx - r - 2, cy - r - 2, (r + 2) * 2, (r + 2) * 2);
        gc.setFill(colorInterior);
        gc.fillOval(cx - r, cy - r, r * 2, r * 2);
    }
}
