package org.example.game.cuerpo;

import org.example.game.motor.CuerpoFisico;
import org.example.game.motor.Vector2D;

/**
 * SRP: Guarda datos básicos del cuerpo celeste y estado dinámico para simulación física.
 * OCP: Subclases definen su propia representación y comportamiento específico.
 *
 * Contrato público acotado a un máximo de 25 métodos (cumpliendo límites de interfaz).
 */
public abstract class CuerpoCeleste implements Masivo, CuerpoFisico {
    private String nombre;
    private double masa;
    private Vector2D posicion;
    private Vector2D velocidad = Vector2D.cero();
    private Vector2D fuerza = Vector2D.cero();
    private boolean simulado = true;

    private final TipoCuerpo tipoCuerpo;
    private CuerpoCeleste cuerpoOrbitado;
    private double radioOrbita;

    public CuerpoCeleste(String nombre, double masa, double posicionX, double posicionY, TipoCuerpo tipoCuerpo) {
        this.nombre = nombre;
        this.masa = masa;
        this.posicion = new Vector2D(posicionX, posicionY);
        this.tipoCuerpo = tipoCuerpo;
        this.radioOrbita = 0;
    }

    // 1
    @Override
    public double getMasa() {
        return masa;
    }

    // 2
    @Override
    public void setMasa(double masa) {
        this.masa = masa;
    }

    // 3
    @Override
    public String getNombre() {
        return nombre;
    }

    // 4
    public void setNombre(String nombre) {
        if (nombre != null && !nombre.isBlank()) {
            this.nombre = nombre.trim();
        }
    }

    // 5
    @Override
    public Vector2D getPosicion() {
        return posicion;
    }

    // 6
    @Override
    public void setPosicion(Vector2D pos) {
        this.posicion = pos != null ? pos : Vector2D.cero();
    }

    // 7
    @Override
    public Vector2D getVelocidad() {
        return velocidad;
    }

    // 8
    @Override
    public void setVelocidad(Vector2D velocidad) {
        this.velocidad = velocidad != null ? velocidad : Vector2D.cero();
    }

    // 9
    @Override
    public Vector2D getFuerza() {
        return fuerza;
    }

    // 10
    @Override
    public void aplicarFuerza(Vector2D fuerza) {
        if (fuerza == null) {
            this.fuerza = Vector2D.cero();
        } else {
            this.fuerza = this.fuerza.sumar(fuerza);
        }
    }

    // 11
    @Override
    public boolean esSimulado() {
        return simulado;
    }

    // 13
    @Override
    public void setSimulado(boolean simulado) {
        this.simulado = simulado;
    }

    // 14
    @Override
    public TipoCuerpo getTipoCuerpo() {
        return tipoCuerpo;
    }

    // 15
    @Override
    public abstract double getRadio();

    // 16
    @Override
    public double getRadioAtraccion() {
        TipoCuerpo tipoActual = getTipoCuerpo();
        if (tipoActual == null) return 20.0;
        double factorMasa = (tipoActual.masaBase > 0) ? Math.max(0.01, masa / tipoActual.masaBase) : 1.0;
        return Math.max(tipoActual.getRadioAtraccionMin(), tipoActual.getRadioAtraccionMax() * Math.sqrt(factorMasa));
    }

    // 17
    public abstract String getColorHex();

    // 18
    public abstract String describir();

    // 19
    public CuerpoCeleste getCuerpoOrbitado() {
        return cuerpoOrbitado;
    }

    // 20
    public void setCuerpoOrbitado(CuerpoCeleste cuerpoOrbitado) {
        this.cuerpoOrbitado = cuerpoOrbitado;
    }

    // 21
    public boolean estaOrbitando(CuerpoCeleste... objetivo) {
        if (objetivo == null || objetivo.length == 0) {
            return cuerpoOrbitado != null;
        }
        return cuerpoOrbitado != null && cuerpoOrbitado == objetivo[0];
    }

    // 22
    public double getRadioOrbita() {
        return radioOrbita;
    }

    // 23
    public void setRadioOrbita(double radioOrbita) {
        this.radioOrbita = radioOrbita;
    }

    // 23
    public boolean esInteractuablePorUsuario() {
        return true;
    }

    // 24
    public boolean permiteReembolso() {
        return true;
    }

    // 25
    public abstract <R> R aceptar(VisitanteCuerpo<R> visitante);
}
