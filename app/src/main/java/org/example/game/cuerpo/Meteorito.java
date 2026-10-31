package org.example.game.cuerpo;
import org.example.game.motor.Vector2D;
/**
 * Meteorito/asteroide: cuerpo pequeño con vida útil limitada.
 * Puede ser spawn natural (evento cósmico) o lanzado por el jugador.
 */
public class Meteorito extends CuerpoCeleste implements Actualizable, Expirable {
    private double tiempoVidaRestante; // ticks
    private final boolean lanzadoPorJugador;
    private double danoImpacto; // Para mecánicas futuras

    private CuerpoCeleste objetivo;
    private double rapidez = 0.0;
    private double factorProgresoOrigen = -1.0;

    public Meteorito(String nombre, double masa, double x, double y,
                     Vector2D velocidadInicial, double ticksVida, boolean lanzadoPorJugador) {
        super(nombre, acotarMasaMeteorito(masa), x, y, TipoCuerpo.METEORITO);
        this.setVelocidad(velocidadInicial);
        this.rapidez = velocidadInicial != null ? velocidadInicial.magnitud() : 0.0;
        this.tiempoVidaRestante = ticksVida;
        this.lanzadoPorJugador = lanzadoPorJugador;
        this.danoImpacto = calcularDanoImpacto(getMasa());
    }

    public Meteorito(String nombre, double masa, double x, double y,
                     Vector2D velocidadInicial, double ticksVida, boolean lanzadoPorJugador,
                     CuerpoCeleste objetivo) {
        this(nombre, masa, x, y, velocidadInicial, ticksVida, lanzadoPorJugador);
        this.objetivo = objetivo;
    }

    /** Constructor simplificado para spawn aleatorio inicial */
    public Meteorito(double x, double y, Vector2D velocidadInicial) {
        this("Meteorito-" + System.currentTimeMillis(),
             org.example.game.simulacion.ConfiguracionColisiones.MASA_MINIMA_METEORITO,
             x, y, velocidadInicial,
             500 + Math.random() * 500,
             false);
    }

    public void setFactorProgresoOrigen(double factorProgresoOrigen) {
        this.factorProgresoOrigen = factorProgresoOrigen;
    }

    public double getFactorProgresoOrigen() {
        return factorProgresoOrigen;
    }

    public boolean tieneProgresoExplicito() {
        return Double.isFinite(factorProgresoOrigen) && factorProgresoOrigen > 0.0;
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
    public void setMasa(double masa) {
        double masaAcotada = acotarMasaMeteorito(masa);
        super.setMasa(masaAcotada);
        this.danoImpacto = calcularDanoImpacto(masaAcotada);
    }

    @Override
    public boolean esInteractuablePorUsuario() {
        return false;
    }

    @Override
    public boolean permiteReembolso() {
        return false;
    }

    @Override
    public <R> R aceptar(VisitanteCuerpo<R> visitante) {
        return visitante.visitarMeteorito(this);
    }

    @Override
    public String describir() {
        String origen = lanzadoPorJugador ? "Jugador" : "Natural";
        return String.format("Meteorito %s (vida: %.0f ticks, daño: %.1f)", origen, tiempoVidaRestante, danoImpacto);
    }

    @Override
    public double getRadio() {
        return org.example.game.simulacion.EscaladorAmenazaMeteorito.calcularRadioDesdeMasa(getMasa());
    }

    private static double acotarMasaMeteorito(double masa) {
        if (Double.isNaN(masa) || masa <= 0.0) {
            return 0.0;
        }
        return Math.min(org.example.game.simulacion.ConfiguracionColisiones.MASA_MAXIMA_METEORITO, masa);
    }

    private static double calcularDanoImpacto(double masa) {
        return Math.sqrt(Math.max(0.0, masa)) * 10.0;
    }

    @Override
    public java.util.Optional<Actualizable> comoActualizable() {
        return java.util.Optional.of(this);
    }

    @Override
    public java.util.Optional<Expirable> comoExpirable() {
        return java.util.Optional.of(this);
    }

    @Override
    public boolean esMeteorito() {
        return true;
    }

    @Override
    public String getColorHex() {
        return lanzadoPorJugador ? "#00FF00" : "#808080"; // Verde si jugador, gris si natural
    }
}
