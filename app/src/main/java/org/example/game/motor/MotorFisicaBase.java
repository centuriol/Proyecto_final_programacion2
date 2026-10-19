package org.example.game.motor;

import org.example.CuerpoCeleste;
import org.example.MotorFisica;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Clase base abstracta para motores de fisica orbital y newtoniana.
 *
 * Principios SOLID:
 * - SRP: Encapsula el ciclo de vida de cuerpos celestes expirados y la gestion de observadores de colision.
 * - LSP: Proporciona comportamiento comun consistente sin alterar los contratos de MotorFisica.
 * - DRY: Centraliza metodos compartidos entre MotorFisicaPermisiva y MotorFisicaNewtoniana.
 */
public abstract class MotorFisicaBase implements MotorFisica {

    protected final double radioMundo;
    protected final List<Runnable> listenersColision = new ArrayList<>();

    public MotorFisicaBase(double radioMundo) {
        this.radioMundo = radioMundo;
    }

    public void agregarListenerColision(Runnable listener) {
        if (listener != null) {
            listenersColision.add(listener);
        }
    }

    protected void notificarColision(CuerpoCeleste cuerpo) {
        for (Runnable l : listenersColision) {
            try {
                l.run();
            } catch (Exception ignored) {}
        }
    }

    protected void limpiarCuerposExpirados(List<CuerpoCeleste> cuerpos) {
        Iterator<CuerpoCeleste> it = cuerpos.iterator();
        while (it.hasNext()) {
            CuerpoCeleste c = it.next();
            if (c.estaExpirado()) {
                it.remove();
                continue;
            }

            double dist = c.getPosicion().magnitud();
            if (dist > radioMundo * ConstantesFisicas.LIMITE_ESCAPE_FACTOR) {
                it.remove();
            }
        }
    }

    public double getRadioMundo() {
        return radioMundo;
    }
}
