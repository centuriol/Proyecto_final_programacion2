package org.example.game.controlador;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.Objects;

/**
 * Representa una combinacion inmutable de tecla y modificadores (Ctrl, Shift, Alt).
 *
 * Principios SOLID:
 * - SRP: Encapsula la identidad, extraccion y comparacion de combinaciones de teclas.
 * - OCP: Permite registrar cualquier combinacion arbitraria sin modificar la estructura del controlador.
 */
public final class CombinacionTeclas {
    private final KeyCode keyCode;
    private final boolean control;
    private final boolean shift;
    private final boolean alt;

    public CombinacionTeclas(KeyCode keyCode, boolean control, boolean shift, boolean alt) {
        this.keyCode = Objects.requireNonNull(keyCode, "KeyCode no puede ser null");
        this.control = control;
        this.shift = shift;
        this.alt = alt;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CombinacionTeclas that = (CombinacionTeclas) o;
        return control == that.control && shift == that.shift && alt == that.alt && keyCode == that.keyCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyCode, control, shift, alt);
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
