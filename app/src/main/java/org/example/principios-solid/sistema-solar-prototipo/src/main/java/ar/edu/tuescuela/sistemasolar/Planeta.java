package org.example;

import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.civilizacion.CivilizacionComponent;

public class Planeta extends CuerpoCeleste implements Civilizable {
    private boolean tieneCivilizacion = false;
    private final TipoCuerpo subTipo;
    private CivilizacionComponent civilizacion;

    public Planeta(String nombre, double masa, double x, double y) {
        this(nombre, masa, x, y, TipoCuerpo.PLANETA_ROCOSO);
    }

    public Planeta(String nombre, double masa, double x, double y, TipoCuerpo subTipo) {
        super(nombre, masa, x, y, subTipo);
        this.subTipo = subTipo;
    }

    @Override
    public void desarrollarCivilizacion() {
        this.tieneCivilizacion = true;
        if (civilizacion == null) {
            civilizacion = new CivilizacionComponent(this);
        }
    }

    @Override
    public boolean tieneCivilizacion() {
        return tieneCivilizacion && civilizacion != null && civilizacion.estaViva();
    }

    public CivilizacionComponent getCivilizacion() {
        return civilizacion;
    }

    public void actualizarCivilizacion(java.util.List<org.example.CuerpoCeleste> cuerpos) {
        if (civilizacion != null) {
            civilizacion.actualizar(cuerpos);
            // Sincronizar estado
            if (!civilizacion.estaViva()) {
                this.tieneCivilizacion = false;
            }
        }
    }

    @Override
    public String describir() {
        String civInfo = "";
        if (civilizacion != null) {
            civInfo = " | " + civilizacion.getEstado().nombre + " (Pop: " + (int)civilizacion.getPoblacion() + ")";
        }
        return "Planeta " + getNombre() + " [" + subTipo.nombre + "] (civ: " + tieneCivilizacion + ")" + civInfo;
    }

    @Override
    public double getRadio() {
        double radioBase;
        if (subTipo == TipoCuerpo.PLANETA_GASEOSO) {
            radioBase = 22.0;
        } else if (subTipo == TipoCuerpo.PLANETA_HELADO) {
            radioBase = 12.0;
        } else {
            radioBase = 15.0;
        }
        double factor = getMasa() / (subTipo != null ? subTipo.masaBase : TipoCuerpo.PLANETA_ROCOSO.masaBase);
        if (factor <= 0) return 3.0;
        return Math.max(3.0, radioBase * Math.cbrt(factor));
    }

    @Override
    public String getColorHex() {
        if (civilizacion != null && civilizacion.estaViva()) {
            return civilizacion.getEstado().getColorHex();
        }
        return subTipo.getColorHexString();
    }

    public TipoCuerpo getSubTipo() {
        return subTipo;
    }
}
