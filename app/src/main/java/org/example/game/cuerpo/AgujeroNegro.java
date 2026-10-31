package org.example.game.cuerpo;
public class AgujeroNegro extends CuerpoCeleste {
    private final TipoCuerpo subTipo;

    public AgujeroNegro(String nombre, double masa, double x, double y) {
        this(nombre, masa, x, y, TipoCuerpo.AGUJERO_NEGRO);
    }

    public AgujeroNegro(String nombre, double masa, double x, double y, TipoCuerpo subTipo) {
        super(nombre, masa, x, y, subTipo);
        this.subTipo = subTipo;
        RegistroCapacidades.registrarAgujeroNegro(this);
    }

    public void absorber(CuerpoCeleste victima) {
        if (victima == null) return;
        setMasa(getMasa() + victima.getMasa());
        RegistroCapacidades.obtenerCivilizable(victima).ifPresent(civ -> {
            if (civ.tieneCivilizacion() && civ.getCivilizacion() != null) {
                civ.getCivilizacion().destruir();
            }
        });
        victima.setSimulado(false);
        victima.setMasa(0);
        System.out.println(getNombre() + " absorbió a " + victima.getNombre() + "!");
    }

    @Override
    public <R> R aceptar(VisitanteCuerpo<R> visitante) {
        return visitante.visitarAgujeroNegro(this);
    }

    @Override
    public String describir() {
        return "Agujero Negro " + getNombre() + " [" + subTipo.nombre + "] (masa: " + FormateadorMasa.formatearMasa(getMasa()) + ")";
    }

    @Override
    public double getRadio() {
        double radioBase = (subTipo != null) ? subTipo.getRadioVisualBase() : 25.0;
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
