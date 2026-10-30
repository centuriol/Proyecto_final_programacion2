package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Expirable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Clase base abstracta para motores de fisica orbital.
 *
 * Principios SOLID:
 * - SRP: Encapsula el ciclo de vida de cuerpos celestes expirados y la gestion de observadores.
 * - LSP: Proporciona comportamiento comun consistente sin alterar los contratos de MotorFisica.
 * - DRY: Centraliza metodos compartidos.
 */
public abstract class MotorFisicaBase implements MotorFisica, ObservableFisica {

    protected final double radioMundo;
    protected final List<Runnable> listenersColision = new ArrayList<>();
    protected Consumer<String> listenerMensaje;

    public MotorFisicaBase(double radioMundo) {
        this.radioMundo = radioMundo;
    }

    @Override
    public void agregarListenerColision(Runnable listener) {
        if (listener != null) {
            listenersColision.add(listener);
        }
    }

    @Override
    public void setListenerMensaje(Consumer<String> listener) {
        this.listenerMensaje = listener;
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
            if (c instanceof Expirable exp && exp.estaExpirado()) {
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
