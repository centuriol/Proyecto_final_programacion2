package org.example.game.render;

/**
 * Encapsula el estado y la lógica de zoom y desplazamiento (pan) de la cámara de juego (SRP / DIP).
 */
public class CamaraZoom implements ControlZoom {

    public static final double ZOOM_MINIMO = 0.5;
    public static final double ZOOM_MAXIMO = 1.5;
    public static final double ZOOM_INICIAL = 1.0;
    public static final double INCREMENTO_POR_DEFECTO = 0.1;

    private static class EstadoZoom {
        final double incrementoZoom;
        double nivelZoom;

        EstadoZoom(double zoomInicial, double incremento) {
            this.incrementoZoom = incremento > 0 && Double.isFinite(incremento) ? incremento : INCREMENTO_POR_DEFECTO;
            this.nivelZoom = acotar(zoomInicial);
        }

        double acotar(double valor) {
            double limitado = Math.max(ZOOM_MINIMO, Math.min(ZOOM_MAXIMO, valor));
            double redondeado = Math.rint(limitado * 1_000_000.0) / 1_000_000.0;
            return Math.max(ZOOM_MINIMO, Math.min(ZOOM_MAXIMO, redondeado));
        }
    }

    private final EstadoZoom zoom;
    private final DesplazamientoCamara desplazamiento = new DesplazamientoCamara();

    public CamaraZoom() {
        this(ZOOM_INICIAL, INCREMENTO_POR_DEFECTO);
    }

    public CamaraZoom(double zoomInicial, double incrementoZoom) {
        this.zoom = new EstadoZoom(zoomInicial, incrementoZoom);
    }

    public void acercar() { ajustarPorDelta(zoom.incrementoZoom); }
    public void alejar() { ajustarPorDelta(-zoom.incrementoZoom); }

    @Override
    public void aplicarRueda(double deltaY) {
        if (!Double.isFinite(deltaY) || deltaY == 0.0) return;
        ajustarPorDelta(deltaY > 0.0 ? zoom.incrementoZoom : -zoom.incrementoZoom);
    }

    @Override
    public void resetear() {
        zoom.nivelZoom = ZOOM_INICIAL;
        desplazamiento.centrar();
    }

    public void ajustarPorDelta(double delta) {
        if (Double.isFinite(delta)) zoom.nivelZoom = zoom.acotar(zoom.nivelZoom + delta);
    }

    @Override
    public void setNivelZoom(double nuevoNivel) {
        if (Double.isFinite(nuevoNivel)) zoom.nivelZoom = zoom.acotar(nuevoNivel);
    }

    @Override public double getNivelZoom() { return zoom.nivelZoom; }
    @Override public boolean esZoomNeutro() { return Math.abs(zoom.nivelZoom - ZOOM_INICIAL) < 0.001; }
    public boolean esVistaNeutra() { return esZoomNeutro() && desplazamiento.esCentrada(); }

    @Override
    public void desplazarPorPantalla(double deltaPantallaX, double deltaPantallaY) {
        desplazamiento.desplazarPorPantalla(deltaPantallaX, deltaPantallaY, zoom.nivelZoom);
    }

    @Override
    public void enfocarPuntoFisico(double xFisica, double yFisica) {
        desplazamiento.enfocarPuntoFisico(xFisica, yFisica);
    }

    @Override public DesplazamientoCamara getDesplazamiento() { return desplazamiento; }

    @Override
    public double pantallaAMundo(double coordenadaPantalla, double dimensionVista) {
        if (esZoomNeutro() || dimensionVista <= 0.0) return coordenadaPantalla;
        double centro = dimensionVista / 2.0;
        return centro + (coordenadaPantalla - centro) / zoom.nivelZoom;
    }

    @Override
    public double mundoAPantallaX(double xFisica, double anchoVista) {
        return anchoVista / 2.0 + (xFisica + desplazamiento.getX()) * zoom.nivelZoom;
    }

    @Override
    public double mundoAPantallaY(double yFisica, double altoVista) {
        return altoVista / 2.0 + (-yFisica + desplazamiento.getY()) * zoom.nivelZoom;
    }

    public double pantallaAMundoX(double xPantalla, double anchoVista) {
        return pantallaAMundo(xPantalla, anchoVista) - desplazamiento.getX();
    }

    public double pantallaAMundoY(double yPantalla, double altoVista) {
        return pantallaAMundo(yPantalla, altoVista) - desplazamiento.getY();
    }

    public double calcularTraslacionX(double anchoVista) {
        return calcularDesplazamientoCentro(anchoVista) + desplazamiento.getX() * zoom.nivelZoom;
    }

    public double calcularTraslacionY(double altoVista) {
        return calcularDesplazamientoCentro(altoVista) + desplazamiento.getY() * zoom.nivelZoom;
    }

    public double calcularDesplazamientoCentro(double dimensionVista) {
        return (dimensionVista / 2.0) * (1.0 - zoom.nivelZoom);
    }
}
