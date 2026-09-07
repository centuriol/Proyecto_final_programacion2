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
        return tipoCuerpo.radioFisicoBase;
    }

    @Override
    public double getRadioAtraccion() {
        double factorMasa = 1.0;
        if (tipoCuerpo != null && tipoCuerpo.masaBase > 0) {
            factorMasa = Math.max(0.01, masa / tipoCuerpo.masaBase);
        }
        if (tipoCuerpo == TipoCuerpo.ESTRELLA) {
            return Math.max(65.0, 85.0 * Math.sqrt(factorMasa));
        } else if (tipoCuerpo == TipoCuerpo.PLANETA_ROCOSO || tipoCuerpo == TipoCuerpo.PLANETA_GASEOSO || tipoCuerpo == TipoCuerpo.PLANETA_HELADO) {
            return Math.max(35.0, 55.0 * Math.sqrt(factorMasa));
        } else if (tipoCuerpo == TipoCuerpo.LUNA) {
            return Math.max(25.0, 40.0 * Math.sqrt(factorMasa));
        } else if (tipoCuerpo == TipoCuerpo.AGUJERO_NEGRO || tipoCuerpo == TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO) {
            return Math.max(90.0, 120.0 * Math.sqrt(factorMasa));
        } else {
            return Math.max(20.0, 35.0 * Math.sqrt(factorMasa));
        }
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
