package org.example;

import org.example.game.cuerpo.TipoCuerpo;

public class AgujeroNegro extends CuerpoCeleste {
    private final TipoCuerpo subTipo;

    public AgujeroNegro(String nombre, double masa, double x, double y) {
        this(nombre, masa, x, y, TipoCuerpo.AGUJERO_NEGRO);
    }

    public AgujeroNegro(String nombre, double masa, double x, double y, TipoCuerpo subTipo) {
        super(nombre, masa, x, y, subTipo);
        this.subTipo = subTipo;
    }

    public void absorber(CuerpoCeleste victima) {
        System.out.println(getNombre() + " absorbió a " + victima.getNombre() + "!");
    }

    @Override
    public String describir() {
        return "Agujero Negro " + getNombre() + " [" + subTipo.nombre + "] (masa: " + String.format("%.2e", getMasa()) + " kg)";
    }

    @Override
    public double getRadio() {
        return 25;
    }

    @Override
    public String getColorHex() {
        return subTipo.getColorHexString();
    }

    public TipoCuerpo getSubTipo() {
        return subTipo;
    }
}
