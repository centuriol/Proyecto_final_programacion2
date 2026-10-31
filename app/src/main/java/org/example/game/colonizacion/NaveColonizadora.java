package org.example.game.colonizacion;

import org.example.game.cuerpo.Colonizable;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.motor.Vector2D;
import org.example.game.simulacion.ConfiguracionSimulacion;

/**
 * Entidad ligera que representa una nave colonizadora en tránsito.
 * NO es un CuerpoCeleste, no entra al motor físico gravitatorio.
 *
 * Principios SOLID:
 * - SRP: Modela exclusivamente la posición, trayectoria y carga poblacional de la nave.
 */
public class NaveColonizadora {

    private Vector2D posicion;
    private final CuerpoCeleste destino;
    private final Colonizable colonizableDestino;
    private final double poblacionColonos;
    private boolean completada = false;
    private boolean perdida = false;

    public NaveColonizadora(Vector2D origen, CuerpoCeleste destino, Colonizable colonizableDestino, double poblacionColonos) {
        this.posicion = origen != null ? origen : Vector2D.cero();
        this.destino = destino;
        this.colonizableDestino = colonizableDestino;
        this.poblacionColonos = poblacionColonos;
    }

    public NaveColonizadora(Vector2D origen, CuerpoCeleste destino, double poblacionColonos) {
        this(origen, destino, GestorColonizacion.getInstanciaGlobal().obtenerColonizable(destino).orElse(null), poblacionColonos);
    }

    public void actualizar() {
        if (completada || perdida) return;

        if (destino == null || !destino.esSimulado()) {
            this.perdida = true;
            return;
        }

        Vector2D posDestino = destino.getPosicion();
        Vector2D dir = posDestino.restar(posicion);
        double distancia = dir.magnitud();

        double vel = ConfiguracionSimulacion.VELOCIDAD_NAVE_COLONIZADORA;
        if (distancia <= vel + destino.getRadio()) {
            // Llegó al destino
            this.posicion = posDestino;
            this.completada = true;
            Colonizable col = colonizableDestino != null ? colonizableDestino : GestorColonizacion.getInstanciaGlobal().obtenerColonizable(destino).orElse(null);
            if (col != null) {
                col.recibirColonos(poblacionColonos);
            }
        } else {
            Vector2D desplazamiento = dir.normalizar().multiplicar(vel);
            this.posicion = this.posicion.sumar(desplazamiento);
        }
    }

    public Vector2D getPosicion() {
        return posicion;
    }

    public CuerpoCeleste getDestino() {
        return destino;
    }

    public double getPoblacionColonos() {
        return poblacionColonos;
    }

    public boolean isCompletada() {
        return completada;
    }

    public boolean isPerdida() {
        return perdida;
    }
}
