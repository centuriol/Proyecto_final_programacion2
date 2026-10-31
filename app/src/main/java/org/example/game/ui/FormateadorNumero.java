package org.example.game.ui;
import java.util.Locale;
/**
 * Utilidad reutilizable para dar formato a numeros grandes en la interfaz de usuario.
 *
 * Principios SOLID:
 * - SRP: Responsabilidad exclusiva de formatear cantidades numericas a formato legible (K, M, B).
 * - DRY: Elimina la duplicacion de formateo entre HUDRecursosTop y HUDSimulacion.
 */
public class FormateadorNumero {

    private static final FormateadorNumero INSTANCIA_POR_DEFECTO = new FormateadorNumero();

    public FormateadorNumero() {}

    public static FormateadorNumero getInstanciaPorDefecto() {
        return INSTANCIA_POR_DEFECTO;
    }

    public String formato(long n) {
        if (n >= 1_000_000_000) return String.format(Locale.US, "%.1fB", n / 1e9);
        if (n >= 1_000_000) return String.format(Locale.US, "%.1fM", n / 1e6);
        if (n >= 1_000) return String.format(Locale.US, "%.1fK", n / 1e3);
        return String.valueOf(n);
    }

    public String formato(double n) {
        return formato((long) n);
    }

    public String formatoColorHex(int color) {
        return String.format("#%06X", color);
    }

    public static String formatear(long n) {
        return INSTANCIA_POR_DEFECTO.formato(n);
    }

    public static String formatear(double n) {
        return INSTANCIA_POR_DEFECTO.formato(n);
    }

    public static String formatearColorHex(int color) {
        return INSTANCIA_POR_DEFECTO.formatoColorHex(color);
    }
}
