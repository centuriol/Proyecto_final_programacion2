package org.example.game.guardado;
import org.example.game.render.ControlZoom;
import org.example.game.simulacion.SimulacionSolar;
import java.util.Objects;
/**
 * Ejecuta el autoguardado periodico de la partida activa sobre las tres ranuras rotativas.
 * Solo avanza cuando la sesion esta activa, sin pausa y sin derrota (SRP / DIP).
 */
public class TemporizadorAutoguardado {

    public static final double INTERVALO_DEFECTO_SEGUNDOS = 20.0;

    private final GestorGuardadoPartida gestorGuardado;
    private final SimulacionSolar simulacion;
    private final ControlZoom controlZoom;
    private final double intervaloSegundos;

    private double tiempoAcumuladoSegundos;
    private boolean activo;

    public TemporizadorAutoguardado(GestorGuardadoPartida gestorGuardado,
                                    SimulacionSolar simulacion,
                                    ControlZoom controlZoom) {
        this(gestorGuardado, simulacion, controlZoom, INTERVALO_DEFECTO_SEGUNDOS);
    }

    public TemporizadorAutoguardado(GestorGuardadoPartida gestorGuardado,
                                    SimulacionSolar simulacion,
                                    ControlZoom controlZoom,
                                    double intervaloSegundos) {
        this.gestorGuardado = Objects.requireNonNull(gestorGuardado, "El gestor de guardado no puede ser null");
        this.simulacion = Objects.requireNonNull(simulacion, "La simulacion no puede ser null");
        this.controlZoom = controlZoom;
        this.intervaloSegundos = (Double.isFinite(intervaloSegundos) && intervaloSegundos > 0.0)
                ? intervaloSegundos
                : INTERVALO_DEFECTO_SEGUNDOS;
    }

    public void activar() {
        this.activo = true;
        this.tiempoAcumuladoSegundos = 0.0;
    }

    public void desactivar() {
        this.activo = false;
        this.tiempoAcumuladoSegundos = 0.0;
    }

    public boolean actualizar(double deltaSegundos) {
        if (!activo || !Double.isFinite(deltaSegundos) || deltaSegundos <= 0.0) {
            return false;
        }
        if (simulacion.getReloj().isEnPausa() || simulacion.estaPerdida()) {
            return false;
        }
        tiempoAcumuladoSegundos += deltaSegundos;
        if (tiempoAcumuladoSegundos < intervaloSegundos) {
            return false;
        }
        tiempoAcumuladoSegundos %= intervaloSegundos;
        try {
            gestorGuardado.guardarAutoguardado(simulacion, controlZoom);
            return true;
        } catch (ExcepcionPersistenciaPartida ignored) {
            return false;
        }
    }

    public boolean estaActivo() {
        return activo;
    }

    public double getIntervaloSegundos() {
        return intervaloSegundos;
    }

    public double getTiempoAcumuladoSegundos() {
        return tiempoAcumuladoSegundos;
    }
}
