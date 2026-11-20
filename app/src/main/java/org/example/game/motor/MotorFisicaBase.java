package org.example.game.motor;

import org.example.game.cuerpo.Actualizable;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Expirable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Clase base abstracta para motores de física orbital (SRP / LSP / DRY).
 */
public abstract class MotorFisicaBase implements MotorFisica, ObservableFisica {

    protected final RegistroCapacidadesFisica registroFisica;
    protected final NotificadorEventosFisica notificadorEventos = new NotificadorEventosFisica();

    public MotorFisicaBase(double radioMundo) {
        this.registroFisica = new RegistroCapacidadesFisica(radioMundo);
    }

    public void registrarActualizable(CuerpoCeleste cuerpo, Actualizable actualizable) {
        registroFisica.registrarActualizable(cuerpo, actualizable);
    }

    public void registrarExpirable(CuerpoCeleste cuerpo, Expirable expirable) {
        registroFisica.registrarExpirable(cuerpo, expirable);
    }

    public void desregistrar(CuerpoCeleste cuerpo) {
        registroFisica.desregistrar(cuerpo);
    }

    public void limpiarCapacidades() {
        registroFisica.limpiarCapacidades();
    }

    @Override
    public void agregarListenerColision(Runnable listener) {
        notificadorEventos.agregarListenerColision(listener);
    }

    @Override
    public void setListenerMensaje(Consumer<String> listener) {
        notificadorEventos.setListenerMensaje(listener);
    }

    protected void notificarColision(CuerpoCeleste cuerpo) {
        notificadorEventos.notificarColision();
    }

    protected void limpiarCuerposExpirados(List<CuerpoCeleste> cuerpos) {
        registroFisica.limpiarExpiradosYFueraDeMundo(cuerpos);
    }
}
