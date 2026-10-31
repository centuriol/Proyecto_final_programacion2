package org.example.game.cuerpo;
import java.util.Locale;
/**
 * SRP: Formatea cantidades de masa física astronómica en unidades legibles (MT).
 */
public class FormateadorMasa {

    private static final FormateadorMasa INSTANCIA_POR_DEFECTO = new FormateadorMasa();

    public FormateadorMasa() {}

    public static FormateadorMasa getInstanciaPorDefecto() {
        return INSTANCIA_POR_DEFECTO;
    }

    public String formatear(double masaKg) {
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

    public static String formatearMasa(double masaKg) {
        return INSTANCIA_POR_DEFECTO.formatear(masaKg);
    }
}
