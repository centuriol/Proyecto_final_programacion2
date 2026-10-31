package org.example.game.guardado;
import org.example.game.motor.Vector2D;
import org.example.game.render.CamaraZoom;
/**
 * Datos guardados inmutables del reloj de simulacion, eventos cosmicos y vista de camara (SRP).
 */
public record EstadoRelojGuardado( long tickActual, boolean enPausa, double velocidadSimulacion, double nivelZoom, int ticksSiguienteEvento, Vector2D desplazamientoCamara ) {
    public EstadoRelojGuardado {
        tickActual = Math.max(0L, tickActual);
        velocidadSimulacion = Double.isFinite(velocidadSimulacion)
                ? Math.max(0.1, Math.min(10.0, velocidadSimulacion))
                : 1.0;
        nivelZoom = Double.isFinite(nivelZoom)
                ? Math.max(CamaraZoom.ZOOM_MINIMO, Math.min(CamaraZoom.ZOOM_MAXIMO, nivelZoom))
                : CamaraZoom.ZOOM_INICIAL;
        ticksSiguienteEvento = Math.max(1, ticksSiguienteEvento);
        desplazamientoCamara = desplazamientoCamara != null ? desplazamientoCamara : Vector2D.cero();
    }

    public EstadoRelojGuardado(
            long tickActual,
            boolean enPausa,
            double velocidadSimulacion,
            double nivelZoom,
            int ticksSiguienteEvento
    ) {
        this(tickActual, enPausa, velocidadSimulacion, nivelZoom, ticksSiguienteEvento, Vector2D.cero());
    }

    public boolean tieneZoomPersonalizado() {
        return Math.abs(nivelZoom - CamaraZoom.ZOOM_INICIAL) >= 0.001;
    }
}
