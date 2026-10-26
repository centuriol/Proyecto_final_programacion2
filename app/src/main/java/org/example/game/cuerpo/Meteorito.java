package org.example.game.cuerpo;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.motor.Vector2D;

/**
 * Meteorito/asteroide: cuerpo pequeño con vida útil limitada.
 * Puede ser spawn natural (evento cósmico) o lanzado por el jugador.
 */
public class Meteorito extends CuerpoCeleste {
    private double tiempoVidaRestante; // ticks
    private final boolean lanzadoPorJugador;
    private double danoImpacto; // Para mecánicas futuras

    private CuerpoCeleste objetivo;
    private double rapidez = 0.0;

    public Meteorito(String nombre, double masa, double x, double y,
                     Vector2D velocidadInicial, double ticksVida, boolean lanzadoPorJugador) {
        super(nombre, masa, x, y, TipoCuerpo.METEORITO);
        this.setVelocidad(velocidadInicial);
        this.rapidez = velocidadInicial != null ? velocidadInicial.magnitud() : 0.0;
        this.tiempoVidaRestante = ticksVida;
        this.lanzadoPorJugador = lanzadoPorJugador;
        this.danoImpacto = Math.sqrt(masa) * 10; // Heurística simple
    }

    public Meteorito(String nombre, double masa, double x, double y,
                     Vector2D velocidadInicial, double ticksVida, boolean lanzadoPorJugador,
                     CuerpoCeleste objetivo) {
        this(nombre, masa, x, y, velocidadInicial, ticksVida, lanzadoPorJugador);
        this.objetivo = objetivo;
    }

    /** Constructor simplificado para spawn aleatorio */
    public Meteorito(double x, double y, Vector2D velocidadInicial) {
        this("Meteorito-" + System.currentTimeMillis(),
             TipoCuerpo.METEORITO.masaBase * (0.5 + Math.random()),
             x, y, velocidadInicial,
             500 + Math.random() * 500, // 500-1000 ticks de vida
             false);
    }

    public void setObjetivo(CuerpoCeleste objetivo) {
        this.objetivo = objetivo;
    }

    public CuerpoCeleste getObjetivo() {
        return objetivo;
    }

    public void reducirVida(double dt) {
        this.tiempoVidaRestante -= dt;
    }

    @Override
    public void actualizarEntidad(double dt) {
        reducirVida(dt);

        // Guiado suave hacia el objetivo
        if (objetivo != null && objetivo.esSimulado()) {
            Vector2D dirDeseada = objetivo.getPosicion().restar(getPosicion());
            if (dirDeseada.magnitud() > 1.0) {
                Vector2D vDeseada = dirDeseada.normalizar().multiplicar(rapidez > 0 ? rapidez : getVelocidad().magnitud());
                double guiado = org.example.game.simulacion.ConfiguracionSimulacion.GUIADO_METEORITO;
                Vector2D nuevaVel = getVelocidad().sumar(vDeseada.restar(getVelocidad()).multiplicar(guiado));
                if (rapidez > 0) {
                    nuevaVel = nuevaVel.normalizar().multiplicar(rapidez);
                }
                setVelocidad(nuevaVel);
            }
        }
    }

    @Override
    public boolean estaExpirado() {
        return tiempoVidaRestante <= 0;
    }

    public boolean isLanzadoPorJugador() {
        return lanzadoPorJugador;
    }

    public double getDanoImpacto() {
        return danoImpacto;
    }

    public double getTiempoVidaRestante() {
        return tiempoVidaRestante;
    }

    @Override
    public String describir() {
        String origen = lanzadoPorJugador ? "Jugador" : "Natural";
        return String.format("Meteorito %s (vida: %.0f ticks, daño: %.1f)", origen, tiempoVidaRestante, danoImpacto);
    }

    @Override
    public double getRadio() {
        // Radio visual basado en masa (escala logarítmica para que se vean)
        return Math.max(3, Math.log10(getMasa() / 1e10) * 5);
    }

    @Override
    public String getColorHex() {
        return lanzadoPorJugador ? "#00FF00" : "#808080"; // Verde si jugador, gris si natural
    }
}