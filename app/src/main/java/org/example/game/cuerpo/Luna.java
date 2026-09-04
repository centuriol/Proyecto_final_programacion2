package org.example.game.cuerpo;

import org.example.CuerpoCeleste;

/**
 * Luna craterizada que puede orbitar planetas o estrellas.
 * SRP: Representa las propiedades y comportamiento de un satélite natural.
 */
public class Luna extends CuerpoCeleste {

    private CuerpoCeleste cuerpoOrbitado;

    public Luna(String nombre, double masa, double x, double y) {
        super(nombre, masa, x, y, TipoCuerpo.LUNA);
    }

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

    @Override
    public String describir() {
        return "Luna " + getNombre() + " (masa: " + String.format("%.2e", getMasa()) + " kg)";
    }

    @Override
    public double getRadio() {
        double factor = getMasa() / TipoCuerpo.LUNA.masaBase;
        if (factor <= 0) return 2.0;
        return Math.max(2.0, 9.0 * Math.cbrt(factor));
    }

    @Override
    public String getColorHex() {
        return TipoCuerpo.LUNA.getColorHexString();
    }
}
