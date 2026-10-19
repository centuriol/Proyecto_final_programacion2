package org.example;

import org.example.game.motor.Vector2D;
import org.example.game.cuerpo.TipoCuerpo;

/**
 * SRP: guarda datos básicos (nombre, masa, posición, velocidad, fuerza).
 * OCP: getColorHex()/getRadio() son abstractos -> cada subclase nueva
 * define su propio dibujo sin tocar esta clase ni la vista.
 *
 * Ahora implementa CuerpoFisico para simulación newtoniana real.
 */
public abstract class CuerpoCeleste implements Masivo, org.example.game.motor.CuerpoFisico {
    private String nombre;
    private double masa;
    private double posicionX;
    private double posicionY;

    // Estado dinámico para física newtoniana
    private Vector2D velocidad = Vector2D.cero();
    private Vector2D fuerza = Vector2D.cero();
    private Vector2D velocidadMedioPaso = Vector2D.cero();
    private Vector2D nuevaPosicion = Vector2D.cero();
    private boolean simulado = true;

    // Tipo de cuerpo para lógica especial
    private final TipoCuerpo tipoCuerpo;
    private CuerpoCeleste cuerpoOrbitado;

    // Datos legacy para compatibilidad (órbitas circulares simples)
    @Deprecated
    private double centroX;
    @Deprecated
    private double centroY;
    @Deprecated
    private double radioOrbita;
    @Deprecated
    private double anguloActual;
    @Deprecated
    private double velocidadAngular = 0.3;

    public CuerpoCeleste(String nombre, double masa, double posicionX, double posicionY, TipoCuerpo tipoCuerpo) {
        this.nombre = nombre;
        this.masa = masa;
        this.posicionX = posicionX;
        this.posicionY = posicionY;
        this.tipoCuerpo = tipoCuerpo;
        this.centroX = posicionX;
        this.centroY = posicionY;
        this.radioOrbita = 0;
        this.anguloActual = 0;
    }

    // ===== Métodos legacy (deprecated, mantener para compatibilidad) =====

    /** @deprecated Usar velocidad/posición vectorial en su lugar */
    @Deprecated
    public void configurarOrbita(double centroX, double centroY, double velocidadAngular) {
        this.centroX = centroX;
        this.centroY = centroY;
        this.radioOrbita = Math.hypot(posicionX - centroX, posicionY - centroY);
        this.velocidadAngular = velocidadAngular;

        // Convertir a velocidad orbital inicial para nueva física
        if (radioOrbita > 0) {
            double vOrbital = velocidadAngular * radioOrbita;
            // Dirección tangencial (perpendicular al radio)
            double dx = posicionX - centroX;
            double dy = posicionY - centroY;
            double dist = Math.hypot(dx, dy);
            this.velocidad = new Vector2D(-dy / dist * vOrbital, dx / dist * vOrbital);
        }
    }

    /** @deprecated Usar MotorFisicaNewtoniana en su lugar */
    @Deprecated
    public void orbitar() {
        if (radioOrbita == 0) return;
        anguloActual += velocidadAngular;
        posicionX = centroX + radioOrbita * Math.cos(anguloActual);
        posicionY = centroY + radioOrbita * Math.sin(anguloActual);
    }

    // ===== Getters/Setters básicos =====

    @Override
    public double getMasa() {
        return masa;
    }

    public void setMasa(double masa) {
        this.masa = masa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre != null && !nombre.isBlank()) {
            this.nombre = nombre.trim();
        }
    }

    public double getPosicionX() {
        return posicionX;
    }

    public double getPosicionY() {
        return posicionY;
    }

    public void mover(double nuevaX, double nuevaY) {
        this.posicionX = nuevaX;
        this.posicionY = nuevaY;
    }

    // ===== Implementación CuerpoFisico (Nueva física) =====

    @Override
    public Vector2D getVelocidad() {
        return velocidad;
    }

    @Override
    public void setVelocidad(Vector2D velocidad) {
        this.velocidad = velocidad;
    }

    @Override
    public Vector2D getFuerza() {
        return fuerza;
    }

    @Override
    public void setFuerza(Vector2D fuerza) {
        this.fuerza = fuerza;
    }

    @Override
    public void aplicarFuerza(Vector2D fuerza) {
        this.fuerza = this.fuerza.sumar(fuerza);
    }

    @Override
    public void resetearFuerza() {
        this.fuerza = Vector2D.cero();
    }

    @Override
    public Vector2D getVelocidadMedioPaso() {
        return velocidadMedioPaso;
    }

    @Override
    public void setVelocidadMedioPaso(Vector2D v) {
        this.velocidadMedioPaso = v;
    }

    @Override
    public Vector2D getNuevaPosicion() {
        return nuevaPosicion;
    }

    @Override
    public void setNuevaPosicion(Vector2D pos) {
        this.nuevaPosicion = pos;
    }

    @Override
    public void confirmarPaso(Vector2D nuevaPosicion, Vector2D nuevaVelocidad) {
        this.posicionX = nuevaPosicion.x;
        this.posicionY = nuevaPosicion.y;
        this.velocidad = nuevaVelocidad;
    }

    @Override
    public boolean esSimulado() {
        return simulado;
    }

    @Override
    public void setSimulado(boolean simulado) {
        this.simulado = simulado;
    }

    @Override
    public TipoCuerpo getTipoCuerpo() {
        return tipoCuerpo;
    }

    @Override
    public double getRadioFisico() {
        TipoCuerpo tipoActual = getTipoCuerpo();
        return tipoActual != null ? tipoActual.radioFisicoBase : 6.37e6;
    }

    @Override
    public double getRadioAtraccion() {
        TipoCuerpo tipoActual = getTipoCuerpo();
        if (tipoActual == null) return 20.0;
        double factorMasa = (tipoActual.masaBase > 0) ? Math.max(0.01, masa / tipoActual.masaBase) : 1.0;
        return Math.max(tipoActual.getRadioAtraccionMin(), tipoActual.getRadioAtraccionMax() * Math.sqrt(factorMasa));
    }

    /**
     * Actualiza la entidad dinámicamente por tick polimórficamente (LSP / OCP).
     * Sobreescrito por entidades con comportamientos pasivos (satélites, escudos, meteoritos).
     */
    public void actualizarEntidad(double dt) {
        // No-op por defecto
    }

    /**
     * Indica si este cuerpo cumplió su tiempo de vida y debe ser removido (LSP).
     */
    public boolean estaExpirado() {
        return false;
    }

    /**
     * Indica si este cuerpo celeste alberga una civilización activa (LSP).
     */
    public boolean tieneCivilizacion() {
        return false;
    }

    /**
     * Obtiene el componente de civilización si lo tiene, o null (LSP).
     */
    public org.example.game.civilizacion.CivilizacionComponent getCivilizacion() {
        return null;
    }

    /**
     * Actualiza la civilización asociada si el cuerpo es civilizable (LSP/OCP).
     */
    public void actualizarCivilizacion(java.util.List<CuerpoCeleste> cuerpos) {
        // No-op por defecto
    }

    /**
     * Indica si este cuerpo fue transformado por un evento del sistema (LSP).
     */
    public boolean isTransformadoPorEvento() {
        return false;
    }

    /**
     * Establece el estado de transformación por evento (LSP).
     */
    public void setTransformadoPorEvento(boolean transformado) {
        // No-op por defecto
    }

    /**
     * Indica si el jugador puede recibir un reembolso de recursos al desmantelar este cuerpo.
     */
    public boolean permiteReembolso() {
        return !isTransformadoPorEvento();
    }

    /**
     * Radio efectivo de defensa de estructuras con escudo (LSP).
     */
    public double getRadioEscudo() {
        return 65.0;
    }

    /**
     * Fase de animación de pulso visual de estructuras defensivas (LSP).
     */
    public double getPulsoVisual() {
        return 0.0;
    }

    /**
     * Indica si este cuerpo puede transformarse por condiciones del entorno (LSP / OCP).
     */
    public boolean esTransformable() {
        return false;
    }

    /**
     * Transforma polimórficamente el cuerpo a un nuevo tipo astronómico (LSP / OCP).
     */
    public void transformarA(TipoCuerpo nuevoTipo) {
        // No-op por defecto
    }


    // ===== Métodos abstractos para vista =====

    public abstract String describir();

    /** Radio en píxeles para dibujarlo. Cada subclase decide el suyo. */
    public abstract double getRadio();

    /** Color en formato CSS (ej: "yellow", "#3355ff"). */
    public abstract String getColorHex();

    // ===== Utilidades vectoriales =====

    public Vector2D getPosicion() {
        return new Vector2D(posicionX, posicionY);
    }

    public void setPosicion(Vector2D pos) {
        this.posicionX = pos.x;
        this.posicionY = pos.y;
    }

    // ===== Estado Orbital =====

    public CuerpoCeleste getCuerpoOrbitado() {
        return cuerpoOrbitado;
    }

    public void setCuerpoOrbitado(CuerpoCeleste cuerpoOrbitado) {
        this.cuerpoOrbitado = cuerpoOrbitado;
    }

    public boolean estaOrbitando() {
        return cuerpoOrbitado != null;
    }

    public boolean estaOrbitando(CuerpoCeleste cuerpo) {
        return cuerpoOrbitado != null && cuerpoOrbitado == cuerpo;
    }

    public double getRadioOrbita() {
        return radioOrbita;
    }

    public void setRadioOrbita(double radioOrbita) {
        this.radioOrbita = radioOrbita;
    }
}
