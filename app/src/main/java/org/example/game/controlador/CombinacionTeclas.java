package org.example.game.controlador;
import javafx.scene.input.KeyCode; import javafx.scene.input.KeyEvent; import java.util.Objects;
/**
 * Representa una combinación inmutable de tecla y modificadores (Ctrl, Shift, Alt).
 * Implementado como Java 17 record.
 */
public record CombinacionTeclas(KeyCode keyCode, boolean control, boolean shift, boolean alt) {

    public CombinacionTeclas {
        Objects.requireNonNull(keyCode, "KeyCode no puede ser null");
    }

    public static CombinacionTeclas de(KeyCode keyCode) {
        return new CombinacionTeclas(keyCode, false, false, false);
    }

    public static CombinacionTeclas ctrl(KeyCode keyCode) {
        return new CombinacionTeclas(keyCode, true, false, false);
    }

    public static CombinacionTeclas shift(KeyCode keyCode) {
        return new CombinacionTeclas(keyCode, false, true, false);
    }

    public static CombinacionTeclas alt(KeyCode keyCode) {
        return new CombinacionTeclas(keyCode, false, false, true);
    }

    public static CombinacionTeclas de(KeyEvent event) {
        if (event == null || event.getCode() == null) return null;
        return new CombinacionTeclas(
                event.getCode(),
                event.isControlDown() || event.isMetaDown(),
                event.isShiftDown(),
                event.isAltDown()
        );
    }

    public KeyCode getKeyCode() {
        return keyCode;
    }

    public boolean isControl() {
        return control;
    }

    public boolean isShift() {
        return shift;
    }

    public boolean isAlt() {
        return alt;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (control) sb.append("Ctrl+");
        if (alt) sb.append("Alt+");
        if (shift) sb.append("Shift+");
        sb.append(keyCode);
        return sb.toString();
    }
}
