package org.example.game.motor;

import org.example.game.cuerpo.Actualizable;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Expirable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    protected final Map<CuerpoCeleste, Actualizable> actualizables = new ConcurrentHashMap<>();
    protected final Map<CuerpoCeleste, Expirable> expirables = new ConcurrentHashMap<>();
    protected Consumer<String> listenerMensaje;

    public MotorFisicaBase(double radioMundo) {
        this.radioMundo = radioMundo;
    }

    public void registrarActualizable(CuerpoCeleste cuerpo, Actualizable actualizable) {
        if (cuerpo != null && actualizable != null) {
            actualizables.put(cuerpo, actualizable);
        }
    }

    public void registrarExpirable(CuerpoCeleste cuerpo, Expirable expirable) {
        if (cuerpo != null && expirable != null) {
            expirables.put(cuerpo, expirable);
        }
    }

    public void desregistrar(CuerpoCeleste cuerpo) {
        if (cuerpo != null) {
            actualizables.remove(cuerpo);
            expirables.remove(cuerpo);
        }
    }

    public void limpiarCapacidades() {
        actualizables.clear();
        expirables.clear();
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
            } catch (Exception ex) {
                System.err.println("[WARN] Error al ejecutar listener de colision: " + ex.getMessage());
            }
        }
    }

    protected void limpiarCuerposExpirados(List<CuerpoCeleste> cuerpos) {
        Iterator<CuerpoCeleste> it = cuerpos.iterator();
        while (it.hasNext()) {
            CuerpoCeleste c = it.next();
            Expirable exp = expirables.get(c);
            if (exp != null && exp.estaExpirado()) {
                expirables.remove(c);
                actualizables.remove(c);
                it.remove();
                continue;
            }

            double dist = c.getPosicion().magnitud();
            if (dist > radioMundo * ConstantesFisicas.LIMITE_ESCAPE_FACTOR) {
                expirables.remove(c);
                actualizables.remove(c);
                it.remove();
            }
        }
    }
}
