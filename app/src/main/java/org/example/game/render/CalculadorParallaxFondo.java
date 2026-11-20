package org.example.game.render;

/**
 * SRP: Calcula y normaliza el desplazamiento automático del fondo espacial y su efecto parallax con la cámara.
 */
public class CalculadorParallaxFondo {

    private static class EstadoParallax {
        double desplazamientoFondoX = 0.0;
        double desplazamientoFondoY = 0.0;
        double velocidadBaseFondo = 1.0;
        double factorParallax = RenderizadorFondo.FACTOR_PARALLAX_POR_DEFECTO;
        double anchoCanvas;
        double altoCanvas;
        ControlZoom camaraZoom;
        double desplazamientoCamaraX = 0.0;
        double desplazamientoCamaraY = 0.0;
        double nivelZoomCamara = CamaraZoom.ZOOM_INICIAL;

        boolean usaCamaraActiva() {
            return camaraZoom != null
                    && camaraZoom.getDesplazamiento() != null
                    && !camaraZoom.getDesplazamiento().esCentrada();
        }

        double obtenerZoomEfectivo() {
            if (usaCamaraActiva()) return camaraZoom.getNivelZoom();
            if (desplazamientoCamaraX != 0.0 || desplazamientoCamaraY != 0.0) return nivelZoomCamara;
            return camaraZoom != null ? camaraZoom.getNivelZoom() : nivelZoomCamara;
        }
    }

    private final EstadoParallax estado = new EstadoParallax();

    public CalculadorParallaxFondo(double ancho, double alto) {
        estado.anchoCanvas = Math.max(0.0, ancho);
        estado.altoCanvas = Math.max(0.0, alto);
    }

    public void actualizarDimensiones(double ancho, double alto) {
        if (ancho > 0) estado.anchoCanvas = ancho;
        if (alto > 0) estado.altoCanvas = alto;
    }

    public void avanzarDesplazamiento(double deltaTime, double multiplicadorVelocidad, double anchoCanvas) {
        if (anchoCanvas <= 0 || deltaTime <= 0 || multiplicadorVelocidad <= 0) return;
        estado.anchoCanvas = anchoCanvas;
        estado.desplazamientoFondoX += estado.velocidadBaseFondo * multiplicadorVelocidad * deltaTime;
        normalizar(anchoCanvas);
    }

    public void actualizarDesplazamientoCamara(double camaraX, double camaraY, double nivelZoom) {
        estado.desplazamientoCamaraX = Double.isFinite(camaraX) ? camaraX : 0.0;
        estado.desplazamientoCamaraY = Double.isFinite(camaraY) ? camaraY : 0.0;
        estado.nivelZoomCamara = (Double.isFinite(nivelZoom) && nivelZoom > 0.0) ? nivelZoom : CamaraZoom.ZOOM_INICIAL;
    }

    public double calcularOffsetParallaxX() {
        double camX = estado.usaCamaraActiva() ? estado.camaraZoom.getDesplazamiento().getX() : estado.desplazamientoCamaraX;
        return camX * estado.obtenerZoomEfectivo() * estado.factorParallax;
    }

    public double calcularOffsetParallaxY() {
        double camY = estado.usaCamaraActiva() ? estado.camaraZoom.getDesplazamiento().getY() : estado.desplazamientoCamaraY;
        return camY * estado.obtenerZoomEfectivo() * estado.factorParallax;
    }

    public void normalizar(double ancho, double alto) {
        normalizar(ancho);
        if (alto > 0) {
            estado.altoCanvas = alto;
            estado.desplazamientoFondoY = normalizarEnRango(estado.desplazamientoFondoY, alto);
        }
    }

    public void normalizar(double ancho) {
        if (ancho > 0) {
            estado.anchoCanvas = ancho;
            estado.desplazamientoFondoX = normalizarEnRango(estado.desplazamientoFondoX, ancho);
        }
    }

    public void normalizar() {
        normalizar(estado.anchoCanvas, estado.altoCanvas);
    }

    public double normalizarEnRango(double valor, double dimension) {
        if (!Double.isFinite(valor)) return 0.0;
        if (!Double.isFinite(dimension) || dimension <= 0.0) return valor;
        double normalizado = (valor % dimension + dimension) % dimension;
        return (normalizado <= 0.0 || normalizado >= dimension) ? 0.0 : normalizado;
    }

    public double getDesplazamientoFondoX() {
        return normalizarEnRango(estado.desplazamientoFondoX + calcularOffsetParallaxX(), estado.anchoCanvas);
    }

    public void setDesplazamientoFondoX(double desplazamiento) {
        estado.desplazamientoFondoX = desplazamiento;
        if (estado.anchoCanvas > 0) normalizar(estado.anchoCanvas);
    }

    public double getDesplazamientoFondoY() {
        return normalizarEnRango(estado.desplazamientoFondoY + calcularOffsetParallaxY(), estado.altoCanvas);
    }

    public void setDesplazamientoFondoY(double desplazamiento) {
        estado.desplazamientoFondoY = desplazamiento;
        if (estado.altoCanvas > 0) normalizar(estado.anchoCanvas, estado.altoCanvas);
    }

    public double getFactorParallax() { return estado.factorParallax; }
    public void setFactorParallax(double factor) {
        if (Double.isFinite(factor) && factor >= 0.0) estado.factorParallax = factor;
    }
    public ControlZoom getCamaraZoom() { return estado.camaraZoom; }
    public void setCamaraZoom(ControlZoom camaraZoom) {
        estado.camaraZoom = camaraZoom;
        estado.desplazamientoCamaraX = 0.0;
        estado.desplazamientoCamaraY = 0.0;
    }
    public double getVelocidadBaseFondo() { return estado.velocidadBaseFondo; }
    public void setVelocidadBaseFondo(double vel) { estado.velocidadBaseFondo = vel; }
    public double getAnchoCanvas() { return estado.anchoCanvas; }
    public double getAltoCanvas() { return estado.altoCanvas; }
}
