package org.example.game.simulacion;
/**
 * Controla el tiempo, pausas, pasos discretos y velocidad de la simulación (SRP).
 */
public class RelojSimulacion {

    private long tickActual = 0;
    private boolean enPausa = false;
    private boolean pasoUnico = false;
    private double velocidadSimulacion = 1.0;

    public boolean debeAvanzar() {
        return !enPausa || pasoUnico;
    }

    public void avanzarTick() {
        tickActual++;
    }

    public void finalizarTick() {
        if (pasoUnico) {
            enPausa = true;
            pasoUnico = false;
        }
    }

    public void play() {
        enPausa = false;
    }

    public void pause() {
        enPausa = true;
    }

    public void togglePause() {
        enPausa = !enPausa;
    }

    public void step() {
        pasoUnico = true;
    }

    public void reiniciar() {
        tickActual = 0;
        enPausa = false;
        pasoUnico = false;
        velocidadSimulacion = 1.0;
    }

    public void restaurar(long tick, boolean pausa, double velocidad) {
        this.tickActual = Math.max(0L, tick);
        this.enPausa = pausa;
        this.pasoUnico = false;
        setVelocidadSimulacion(velocidad);
    }

    public long getTickActual() {
        return tickActual;
    }

    public boolean isEnPausa() {
        return enPausa;
    }

    public void setEnPausa(boolean enPausa) {
        this.enPausa = enPausa;
    }

    public double getVelocidadSimulacion() {
        return velocidadSimulacion;
    }

    public void setVelocidadSimulacion(double velocidad) {
        this.velocidadSimulacion = Math.max(0.1, Math.min(10.0, velocidad));
    }
}
