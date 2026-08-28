package org.example.game.cuerpo;

import org.example.CuerpoCeleste;
import org.example.game.motor.Vector2D;

/**
 * Meteorito/asteroide: cuerpo pequeño con vida útil limitada.
 * Puede ser spawn natural (evento cósmico) o lanzado por el jugador.
 */
public class Meteorito extends CuerpoCeleste {
    private double tiempoVidaRestante; // ticks
    private final boolean lanzadoPorJugador;
    private double danoImpacto; // Para mecánicas futuras

    public Meteorito(String nombre, double masa, double x, double y,
                     Vector2D velocidadInicial, double ticksVida, boolean lanzadoPorJugador) {
        super(nombre, masa, x, y, TipoCuerpo.METEORITO);
        this.setVelocidad(velocidadInicial);
        this.tiempoVidaRestante = ticksVida;
        this.lanzadoPorJugador = lanzadoPorJugador;
        this.danoImpacto = Math.sqrt(masa) * 10; // Heurística simple
    }

    /** Constructor simplificado para spawn aleatorio */
    public Meteorito(double x, double y, Vector2D velocidadInicial) {
        this("Meteorito-" + System.currentTimeMillis(),
             TipoCuerpo.METEORITO.masaBase * (0.5 + Math.random()),
             x, y, velocidadInicial,
             500 + Math.random() * 500, // 500-1000 ticks de vida
             false);
    }

    public void reducirVida(double dt) {
        this.tiempoVidaRestante -= dt;
    }

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