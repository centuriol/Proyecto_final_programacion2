package org.example.game.cuerpo;
import java.util.Locale;
/**
 * SRP: Formatea cantidades de masa física astronómica en unidades legibles (MT).
 */
public final class FormateadorMasa {

    private FormateadorMasa() {}

    public static String formatearMasa(double masaKg) {
        double mt = masaKg / 1e24;
        if (mt >= 1_000_000_000.0) {
            return String.format(Locale.US, "%.2fB MT", mt / 1e9);
        } else if (mt >= 1_000_000.0) {
            return String.format(Locale.US, "%.2fM MT", mt / 1e6);
        } else if (mt >= 1_000.0) {
            return String.format(Locale.US, "%.2fk MT", mt / 1e3);
        } else if (mt >= 0.01) {
            return String.format(Locale.US, "%.2f MT", mt);
        } else if (mt >= 0.0001) {
            return String.format(Locale.US, "%.4f MT", mt);
        } else {
            return String.format(Locale.US, "%.6f MT", mt);
        }
    }
}
