package org.example;

import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.civilizacion.CivilizacionComponent;

public class Planeta extends CuerpoCeleste implements Civilizable {
    private boolean tieneCivilizacion = false;
    private TipoCuerpo subTipo;
    private boolean transformadoPorEvento = false;
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
        // Tarea 5: Un planeta transformado por evento no puede desarrollar civilizacion
        if (transformadoPorEvento) return;
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

    @Override
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

    @Override
    public TipoCuerpo getTipoCuerpo() {
        return subTipo;
    }

    /**
     * Transforma este planeta a un nuevo subtipo preservando posicion, velocidad y estado fisico.
     * Marca el planeta como transformado por evento para que no genere recursos,
     * no desarrolle civilizacion y no de reembolso al eliminarlo (Tarea 5).
     *
     * @param nuevoTipo El nuevo subtipo de planeta.
     */
    @Override
    public boolean esTransformable() {
        return true;
    }

    @Override
    public void transformarA(TipoCuerpo nuevoTipo) {
        if (nuevoTipo != null) {
            this.subTipo = nuevoTipo;
            this.transformadoPorEvento = true;
            // Destruir civilizacion existente: un planeta transformado no debe tenerla
            if (civilizacion != null) {
                civilizacion.destruir();
                this.tieneCivilizacion = false;
            }
        }
    }

    @Override
    public boolean isTransformadoPorEvento() {
        return transformadoPorEvento;
    }

    @Override
    public void setTransformadoPorEvento(boolean transformadoPorEvento) {
        this.transformadoPorEvento = transformadoPorEvento;
    }
}
