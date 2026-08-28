package org.example;
import java.util.ArrayList;
import java.util.List;

/**
 * SRP: solo administra la lista de cuerpos y dispara los pasos de física.
 * DIP: recibe MotorFisica por constructor, no crea uno propio.
 */
public class SistemaSolar {
    private final List<CuerpoCeleste> cuerpos = new ArrayList<>();
    private final MotorFisica motorFisica;

    public SistemaSolar(MotorFisica motorFisica) {
        this.motorFisica = motorFisica;
    }

    public void agregarCuerpo(CuerpoCeleste cuerpo) {
        cuerpos.add(cuerpo);
    }

    public void avanzarPaso() {
        motorFisica.avanzarPaso(cuerpos);
    }

    public List<CuerpoCeleste> getCuerpos() {
        return cuerpos;
    }
}
