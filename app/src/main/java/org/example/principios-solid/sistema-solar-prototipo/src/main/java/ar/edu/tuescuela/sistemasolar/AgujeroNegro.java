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
        return "Agujero Negro " + getNombre() + " [" + subTipo.nombre + "] (masa: " + TipoCuerpo.formatearMasa(getMasa()) + ")";
    }

    @Override
    public double getRadio() {
        double radioBase = (subTipo == TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO) ? 45.0 : 25.0;
        double factor = getMasa() / (subTipo != null ? subTipo.masaBase : TipoCuerpo.AGUJERO_NEGRO.masaBase);
        if (factor <= 0) return 5.0;
        return Math.max(5.0, radioBase * Math.cbrt(factor));
    }

    @Override
    public String getColorHex() {
        return subTipo.getColorHexString();
    }

    public TipoCuerpo getSubTipo() {
        return subTipo;
    }
}
