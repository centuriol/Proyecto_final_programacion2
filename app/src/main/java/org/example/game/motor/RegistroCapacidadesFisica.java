package org.example.game.motor;

import org.example.game.cuerpo.Actualizable;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Expirable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SRP: Administra el registro de entidades actualizables y expirables del motor físico.
 */
public class RegistroCapacidadesFisica {

    private final Map<CuerpoCeleste, Actualizable> actualizables = new ConcurrentHashMap<>();
    private final Map<CuerpoCeleste, Expirable> expirables = new ConcurrentHashMap<>();
    private final double limiteEscape;

    public RegistroCapacidadesFisica(double radioMundo) {
        this.limiteEscape = radioMundo * ConstantesFisicas.LIMITE_ESCAPE_FACTOR;
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

    public Actualizable obtenerActualizable(CuerpoCeleste c) {
        return c != null ? actualizables.get(c) : null;
    }

    public boolean esActualizableRegistrado(CuerpoCeleste c) {
        return c != null && actualizables.containsKey(c);
    }

    public void limpiarExpiradosYFueraDeMundo(List<CuerpoCeleste> cuerpos) {
        Iterator<CuerpoCeleste> it = cuerpos.iterator();
        while (it.hasNext()) {
            CuerpoCeleste c = it.next();
            Expirable exp = expirables.get(c);
            boolean expirado = exp != null && exp.estaExpirado();
            boolean fuera = c.getPosicion().magnitud() > limiteEscape;
            if (expirado || fuera) {
                desregistrar(c);
                it.remove();
            }
        }
    }
}
