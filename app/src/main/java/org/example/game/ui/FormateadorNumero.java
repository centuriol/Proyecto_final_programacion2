package org.example.game.ui;
import java.util.Locale;
/**
 * Utilidad reutilizable para dar formato a numeros grandes en la interfaz de usuario.
 *
 * Principios SOLID:
 * - SRP: Responsabilidad exclusiva de formatear cantidades numericas a formato legible (K, M, B).
 * - DRY: Elimina la duplicacion de formateo entre HUDRecursosTop y HUDSimulacion.
 */
public final class FormateadorNumero {

    private FormateadorNumero() {}

    public static String formatear(long n) {
        if (n >= 1_000_000_000) return String.format(Locale.US, "%.1fB", n / 1e9);
        if (n >= 1_000_000) return String.format(Locale.US, "%.1fM", n / 1e6);
        if (n >= 1_000) return String.format(Locale.US, "%.1fK", n / 1e3);
        return String.valueOf(n);
    }

    public static String formatear(double n) {
        return formatear((long) n);
    }

    public static String formatearColorHex(int color) {
        return String.format("#%06X", color);
    }
}
