package org.example.game.cuerpo;

import org.example.game.motor.Vector2D;
import org.example.game.simulacion.CalculadorAmenazaMeteorito;
import org.example.game.simulacion.ConfiguracionColisiones;

/**
 * Meteorito/asteroide: cuerpo pequeño con vida útil limitada (SRP).
 */
public class Meteorito extends CuerpoCeleste implements Actualizable, Expirable {

    private final CicloVidaMeteorito cicloVida;

    public Meteorito(String nombre, double masa, double x, double y,
                     Vector2D velocidadInicial, double ticksVida, boolean lanzadoPorJugador) {
        this(nombre, masa, x, y, velocidadInicial, ticksVida, lanzadoPorJugador, null);
    }

    public Meteorito(String nombre, double masa, double x, double y,
                     Vector2D velocidadInicial, double ticksVida, boolean lanzadoPorJugador,
                     CuerpoCeleste objetivo) {
        super(nombre, CicloVidaMeteorito.acotarMasa(masa), x, y, TipoCuerpo.METEORITO);
        this.setVelocidad(velocidadInicial);
        this.cicloVida = new CicloVidaMeteorito(getMasa(), velocidadInicial, ticksVida, lanzadoPorJugador, objetivo);
        RegistroCapacidades.registrarMeteorito(this);
    }

    public Meteorito(double x, double y, Vector2D velocidadInicial) {
        this("Meteorito-" + System.currentTimeMillis(),
                ConfiguracionColisiones.MASA_MINIMA_METEORITO,
                x, y, velocidadInicial,
                500 + Math.random() * 500,
                false);
    }

    public void setCalculadorAmenaza(CalculadorAmenazaMeteorito calc) { cicloVida.setCalculadorAmenaza(calc); }
    public void setFactorProgresoOrigen(double factor) { cicloVida.setFactorProgresoOrigen(factor); }
    public double getFactorProgresoOrigen() { return cicloVida.getFactorProgresoOrigen(); }
    public boolean tieneProgresoExplicito() { return cicloVida.tieneProgresoExplicito(); }
    public void setObjetivo(CuerpoCeleste objetivo) { cicloVida.setObjetivo(objetivo); }
    public CuerpoCeleste getObjetivo() { return cicloVida.getObjetivo(); }
    public void reducirVida(double dt) { cicloVida.reducirVida(dt); }
    @Override public void actualizarEntidad(double dt) { cicloVida.actualizarGuiado(this, dt); }
    @Override public boolean estaExpirado() { return cicloVida.estaExpirado(); }
    public boolean isLanzadoPorJugador() { return cicloVida.isLanzadoPorJugador(); }
    public double getDanoImpacto() { return cicloVida.getDanoImpacto(); }
    public double getTiempoVidaRestante() { return cicloVida.getTiempoVidaRestante(); }

    @Override
    public void setMasa(double masa) {
        double masaAcotada = CicloVidaMeteorito.acotarMasa(masa);
        super.setMasa(masaAcotada);
        if (cicloVida != null) cicloVida.actualizarDanoDesdeMasa(masaAcotada);
    }

    @Override public boolean esInteractuablePorUsuario() { return false; }
    @Override public boolean permiteReembolso() { return false; }
    @Override public <R> R aceptar(VisitanteCuerpo<R> visitante) { return visitante.visitarMeteorito(this); }
    @Override public String describir() { return cicloVida.describir(); }
    @Override public double getRadio() { return cicloVida.calcularRadio(getMasa()); }
    @Override public String getColorHex() { return cicloVida.isLanzadoPorJugador() ? "#00FF00" : "#808080"; }
}
