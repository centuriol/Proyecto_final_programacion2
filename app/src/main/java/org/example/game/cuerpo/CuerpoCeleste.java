package org.example.game.cuerpo;

import org.example.game.motor.CuerpoFisico;
import org.example.game.motor.Vector2D;

/**
 * SRP: Guarda datos básicos del cuerpo celeste delegando estado cinemático y orbital a EstadoCinematicoCuerpo.
 * OCP: Subclases definen su propia representación y comportamiento específico.
 *
 * Contrato público acotado a un máximo de 25 métodos (cumpliendo límites de interfaz).
 */
public abstract class CuerpoCeleste implements Masivo, CuerpoFisico {

    private final TipoCuerpo tipoCuerpo;
    private final EstadoCinematicoCuerpo estado;

    public CuerpoCeleste(String nombre, double masa, double posicionX, double posicionY, TipoCuerpo tipoCuerpo) {
        this.tipoCuerpo = tipoCuerpo;
        this.estado = new EstadoCinematicoCuerpo(nombre, masa, posicionX, posicionY);
    }

    // 1
    @Override public double getMasa() { return estado.getMasa(); }
    // 2
    @Override public void setMasa(double masa) { estado.setMasa(masa); }
    // 3
    @Override public String getNombre() { return estado.getNombre(); }
    // 4
    public void setNombre(String nombre) { estado.setNombre(nombre); }
    // 5
    @Override public Vector2D getPosicion() { return estado.getPosicion(); }
    // 6
    @Override public void setPosicion(Vector2D pos) { estado.setPosicion(pos); }
    // 7
    @Override public Vector2D getVelocidad() { return estado.getVelocidad(); }
    // 8
    @Override public void setVelocidad(Vector2D velocidad) { estado.setVelocidad(velocidad); }
    // 9
    @Override public Vector2D getFuerza() { return estado.getFuerza(); }
    // 10
    @Override public void aplicarFuerza(Vector2D fuerza) { estado.aplicarFuerza(fuerza); }
    // 11
    @Override public boolean esSimulado() { return estado.isSimulado(); }
    // 12
    @Override public void setSimulado(boolean simulado) { estado.setSimulado(simulado); }
    // 13
    @Override public TipoCuerpo getTipoCuerpo() { return tipoCuerpo; }
    // 14
    @Override public abstract double getRadio();
    // 15
    @Override public double getRadioAtraccion() { return estado.calcularRadioAtraccion(getTipoCuerpo()); }
    // 16
    public abstract String getColorHex();
    // 17
    public abstract String describir();
    // 18
    public CuerpoCeleste getCuerpoOrbitado() { return estado.getCuerpoOrbitado(); }
    // 19
    public void setCuerpoOrbitado(CuerpoCeleste cuerpoOrbitado) { estado.setCuerpoOrbitado(cuerpoOrbitado); }
    // 20
    public boolean estaOrbitando(CuerpoCeleste... objetivo) { return estado.estaOrbitando(objetivo); }
    // 21
    public double getRadioOrbita() { return estado.getRadioOrbita(); }
    // 22
    public void setRadioOrbita(double radioOrbita) { estado.setRadioOrbita(radioOrbita); }
    // 23
    public boolean esInteractuablePorUsuario() { return true; }
    // 24
    public boolean permiteReembolso() { return true; }
    // 25
    public abstract <R> R aceptar(VisitanteCuerpo<R> visitante);
}
