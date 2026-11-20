package org.example.game.cuerpo;

import org.example.game.motor.Vector2D;

/**
 * SRP: Encapsula el estado cinemático, de identidad y vínculo orbital de un cuerpo celeste.
 */
public class EstadoCinematicoCuerpo {

    private static class DatosCinematicos {
        String nombre;
        double masa;
        Vector2D posicion;
        Vector2D velocidad = Vector2D.cero();
        Vector2D fuerza = Vector2D.cero();
        boolean simulado = true;

        DatosCinematicos(String nombre, double masa, double x, double y) {
            this.nombre = nombre;
            this.masa = masa;
            this.posicion = new Vector2D(x, y);
        }

        void aplicarFuerza(Vector2D delta) {
            this.fuerza = (delta == null) ? Vector2D.cero() : this.fuerza.sumar(delta);
        }

        double calcularRadioAtraccion(TipoCuerpo tipoActual) {
            if (tipoActual == null) return 20.0;
            double factorMasa = (tipoActual.masaBase > 0) ? Math.max(0.01, masa / tipoActual.masaBase) : 1.0;
            return Math.max(tipoActual.getRadioAtraccionMin(), tipoActual.getRadioAtraccionMax() * Math.sqrt(factorMasa));
        }
    }

    private static class VinculoOrbital {
        CuerpoCeleste cuerpoOrbitado;
        double radioOrbita = 0.0;

        boolean estaOrbitando(CuerpoCeleste... objetivo) {
            if (objetivo == null || objetivo.length == 0) {
                return cuerpoOrbitado != null;
            }
            return cuerpoOrbitado != null && cuerpoOrbitado == objetivo[0];
        }
    }

    private final DatosCinematicos datos;
    private final VinculoOrbital orbita = new VinculoOrbital();

    public EstadoCinematicoCuerpo(String nombre, double masa, double posicionX, double posicionY) {
        this.datos = new DatosCinematicos(nombre, masa, posicionX, posicionY);
    }

    public double getMasa() { return datos.masa; }
    public void setMasa(double masa) { datos.masa = masa; }
    public String getNombre() { return datos.nombre; }
    public void setNombre(String nombre) { if (nombre != null && !nombre.isBlank()) datos.nombre = nombre.trim(); }
    public Vector2D getPosicion() { return datos.posicion; }
    public void setPosicion(Vector2D pos) { datos.posicion = pos != null ? pos : Vector2D.cero(); }
    public Vector2D getVelocidad() { return datos.velocidad; }
    public void setVelocidad(Vector2D vel) { datos.velocidad = vel != null ? vel : Vector2D.cero(); }
    public Vector2D getFuerza() { return datos.fuerza; }
    public boolean isSimulado() { return datos.simulado; }
    public void setSimulado(boolean simulado) { datos.simulado = simulado; }
    public CuerpoCeleste getCuerpoOrbitado() { return orbita.cuerpoOrbitado; }
    public void setCuerpoOrbitado(CuerpoCeleste orbitado) { orbita.cuerpoOrbitado = orbitado; }
    public double getRadioOrbita() { return orbita.radioOrbita; }
    public void setRadioOrbita(double radioOrbita) { orbita.radioOrbita = radioOrbita; }

    public void aplicarFuerza(Vector2D delta) { datos.aplicarFuerza(delta); }
    public boolean estaOrbitando(CuerpoCeleste... objetivo) { return orbita.estaOrbitando(objetivo); }
    public double calcularRadioAtraccion(TipoCuerpo tipoActual) { return datos.calcularRadioAtraccion(tipoActual); }
}
