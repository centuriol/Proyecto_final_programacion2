package org.example.game.cuerpo;

import org.example.game.motor.Vector2D;
import org.example.game.simulacion.CalculadorAmenazaMeteorito;
import org.example.game.simulacion.ConfiguracionColisiones;
import org.example.game.simulacion.FabricaSimulacion;

/**
 * SRP / DIP: Gestiona el ciclo de vida, daño de impacto y guiado hacia objetivo de un meteorito.
 */
public class CicloVidaMeteorito {

    private static final FabricaSimulacion FABRICA_SIM = new FabricaSimulacion();

    private static class EstadoMeteorito {
        double tiempoVidaRestante;
        final boolean lanzadoPorJugador;
        double danoImpacto;
        CuerpoCeleste objetivo;
        final double rapidez;
        double factorProgresoOrigen = -1.0;

        EstadoMeteorito(double masaAcotada, Vector2D velocidadInicial, double ticksVida,
                        boolean lanzadoPorJugador, CuerpoCeleste objetivo) {
            this.rapidez = velocidadInicial != null ? velocidadInicial.magnitud() : 0.0;
            this.tiempoVidaRestante = ticksVida;
            this.lanzadoPorJugador = lanzadoPorJugador;
            this.danoImpacto = calcularDanoImpacto(masaAcotada);
            this.objetivo = objetivo;
        }

        void descontarVida(double dt) { tiempoVidaRestante -= dt; }
        boolean expirado() { return tiempoVidaRestante <= 0; }
    }

    private final EstadoMeteorito estado;
    private CalculadorAmenazaMeteorito calculadorAmenaza;

    public CicloVidaMeteorito(double masaAcotada, Vector2D velocidadInicial, double ticksVida,
                              boolean lanzadoPorJugador, CuerpoCeleste objetivo) {
        this(masaAcotada, velocidadInicial, ticksVida, lanzadoPorJugador, objetivo, FABRICA_SIM.crearCalculadorAmenaza());
    }

    public CicloVidaMeteorito(double masaAcotada, Vector2D velocidadInicial, double ticksVida,
                              boolean lanzadoPorJugador, CuerpoCeleste objetivo,
                              CalculadorAmenazaMeteorito calculadorAmenaza) {
        this.estado = new EstadoMeteorito(masaAcotada, velocidadInicial, ticksVida, lanzadoPorJugador, objetivo);
        this.calculadorAmenaza = calculadorAmenaza != null ? calculadorAmenaza : FABRICA_SIM.crearCalculadorAmenaza();
    }

    public static double acotarMasa(double masa) {
        if (Double.isNaN(masa) || masa <= 0.0) return 0.0;
        return Math.min(ConfiguracionColisiones.MASA_MAXIMA_METEORITO, masa);
    }

    public static double calcularDanoImpacto(double masa) {
        return Math.sqrt(Math.max(0.0, masa)) * 10.0;
    }

    public void actualizarGuiado(Meteorito meteorito, double dt) {
        estado.descontarVida(dt);
        if (estado.objetivo != null && estado.objetivo.esSimulado()) {
            Vector2D dirDeseada = estado.objetivo.getPosicion().restar(meteorito.getPosicion());
            if (dirDeseada.magnitud() > 1.0) {
                double mag = estado.rapidez > 0 ? estado.rapidez : Math.max(1.0, meteorito.getVelocidad().magnitud());
                meteorito.setVelocidad(dirDeseada.normalizar().multiplicar(mag));
            }
        }
    }

    public void actualizarDanoDesdeMasa(double masaAcotada) { estado.danoImpacto = calcularDanoImpacto(masaAcotada); }
    public void reducirVida(double dt) { estado.descontarVida(dt); }
    public boolean estaExpirado() { return estado.expirado(); }
    public boolean tieneProgresoExplicito() { return Double.isFinite(estado.factorProgresoOrigen) && estado.factorProgresoOrigen > 0.0; }
    public double calcularRadio(double masa) { return calculadorAmenaza.radioDesdeMasa(masa); }
    public String describir() {
        return String.format("Meteorito %s (vida: %.0f ticks, daño: %.1f)",
                estado.lanzadoPorJugador ? "Jugador" : "Natural", estado.tiempoVidaRestante, estado.danoImpacto);
    }

    public void setCalculadorAmenaza(CalculadorAmenazaMeteorito calc) { if (calc != null) this.calculadorAmenaza = calc; }
    public void setFactorProgresoOrigen(double factor) { estado.factorProgresoOrigen = factor; }
    public double getFactorProgresoOrigen() { return estado.factorProgresoOrigen; }
    public void setObjetivo(CuerpoCeleste objetivo) { estado.objetivo = objetivo; }
    public CuerpoCeleste getObjetivo() { return estado.objetivo; }
    public boolean isLanzadoPorJugador() { return estado.lanzadoPorJugador; }
    public double getDanoImpacto() { return estado.danoImpacto; }
    public double getTiempoVidaRestante() { return estado.tiempoVidaRestante; }
}
