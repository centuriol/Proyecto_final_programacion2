package org.example.game.render;
/**
 * Encapsula el estado y la logica de zoom y desplazamiento (pan) de la camara de juego.
 * Girar la rueda hacia adelante (deltaY positivo) acerca hasta 1.5x;
 * girar hacia atras (deltaY negativo) aleja hasta 0.5x.
 * El arrastre traslada la vista respetando el nivel de zoom activo.
 *
 * Principios SOLID:
 * - SRP: Coordina los limites de escala y delega el desplazamiento 2D a DesplazamientoCamara.
 * - DIP: Implementa la abstraccion ControlZoom.
 */
public class CamaraZoom implements ControlZoom {

    public static final double ZOOM_MINIMO = 0.5;
    public static final double ZOOM_MAXIMO = 1.5;
    public static final double ZOOM_INICIAL = 1.0;
    public static final double INCREMENTO_POR_DEFECTO = 0.1;

    private final double incrementoZoom;
    private final DesplazamientoCamara desplazamiento = new DesplazamientoCamara();
    private double nivelZoom;

    public CamaraZoom() {
        this(ZOOM_INICIAL, INCREMENTO_POR_DEFECTO);
    }

    public CamaraZoom(double zoomInicial, double incrementoZoom) {
        this.incrementoZoom = incrementoZoom > 0 && Double.isFinite(incrementoZoom)
                ? incrementoZoom
                : INCREMENTO_POR_DEFECTO;
        this.nivelZoom = acotarZoom(zoomInicial);
    }

    public void acercar() {
        ajustarPorDelta(incrementoZoom);
    }

    public void alejar() {
        ajustarPorDelta(-incrementoZoom);
    }

    @Override
    public void aplicarRueda(double deltaY) {
        if (!Double.isFinite(deltaY) || deltaY == 0.0) {
            return;
        }
        ajustarPorDelta(deltaY > 0.0 ? incrementoZoom : -incrementoZoom);
    }

    @Override
    public void resetear() {
        this.nivelZoom = ZOOM_INICIAL;
        this.desplazamiento.centrar();
    }

    public void ajustarPorDelta(double delta) {
        if (Double.isFinite(delta)) {
            this.nivelZoom = acotarZoom(this.nivelZoom + delta);
        }
    }

    @Override
    public void setNivelZoom(double nuevoNivel) {
        if (Double.isFinite(nuevoNivel)) {
            this.nivelZoom = acotarZoom(nuevoNivel);
        }
    }

    @Override
    public double getNivelZoom() {
        return nivelZoom;
    }

    @Override
    public boolean esZoomNeutro() {
        return Math.abs(nivelZoom - ZOOM_INICIAL) < 0.001;
    }

    public boolean esVistaNeutra() {
        return esZoomNeutro() && desplazamiento.esCentrada();
    }

    @Override
    public void desplazarPorPantalla(double deltaPantallaX, double deltaPantallaY) {
        desplazamiento.desplazarPorPantalla(deltaPantallaX, deltaPantallaY, nivelZoom);
    }

    @Override
    public void enfocarPuntoFisico(double xFisica, double yFisica) {
        desplazamiento.enfocarPuntoFisico(xFisica, yFisica);
    }

    @Override
    public DesplazamientoCamara getDesplazamiento() {
        return desplazamiento;
    }

    @Override
    public double pantallaAMundo(double coordenadaPantalla, double dimensionVista) {
        if (esZoomNeutro() || dimensionVista <= 0.0) {
            return coordenadaPantalla;
        }
        double centro = dimensionVista / 2.0;
        return centro + (coordenadaPantalla - centro) / nivelZoom;
    }

    @Override
    public double mundoAPantallaX(double xFisica, double anchoVista) {
        return anchoVista / 2.0 + (xFisica + desplazamiento.getX()) * nivelZoom;
    }

    @Override
    public double mundoAPantallaY(double yFisica, double altoVista) {
        return altoVista / 2.0 + (-yFisica + desplazamiento.getY()) * nivelZoom;
    }

    public double pantallaAMundoX(double xPantalla, double anchoVista) {
        return pantallaAMundo(xPantalla, anchoVista) - desplazamiento.getX();
    }

    public double pantallaAMundoY(double yPantalla, double altoVista) {
        return pantallaAMundo(yPantalla, altoVista) - desplazamiento.getY();
    }

    public double calcularTraslacionX(double anchoVista) {
        return calcularDesplazamientoCentro(anchoVista) + desplazamiento.getX() * nivelZoom;
    }

    public double calcularTraslacionY(double altoVista) {
        return calcularDesplazamientoCentro(altoVista) + desplazamiento.getY() * nivelZoom;
    }

    public double calcularDesplazamientoCentro(double dimensionVista) {
        return (dimensionVista / 2.0) * (1.0 - nivelZoom);
    }

    private double acotarZoom(double valor) {
        double limitado = Math.max(ZOOM_MINIMO, Math.min(ZOOM_MAXIMO, valor));
        double redondeado = Math.rint(limitado * 1_000_000.0) / 1_000_000.0;
        return Math.max(ZOOM_MINIMO, Math.min(ZOOM_MAXIMO, redondeado));
    }
}
