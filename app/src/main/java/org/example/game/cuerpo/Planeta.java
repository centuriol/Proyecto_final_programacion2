package org.example.game.cuerpo;
import org.example.game.civilizacion.CivilizacionComponent;
import java.util.List;
public class Planeta extends CuerpoCeleste implements Civilizable, Colonizable, Defensor, Transformable {
    private boolean tieneCivilizacion = false;
    private TipoCuerpo subTipo;
    private boolean transformadoPorEvento = false;
    private CivilizacionComponent civilizacion;
    private int golpesEscudo = 0;
    private final double masaInicial;

    public Planeta(String nombre, double masa, double x, double y) {
        this(nombre, masa, x, y, TipoCuerpo.PLANETA_ROCOSO);
    }

    public Planeta(String nombre, double masa, double x, double y, TipoCuerpo subTipo) {
        super(nombre, masa, x, y, subTipo);
        this.subTipo = subTipo;
        this.masaInicial = masa;
        if (subTipo == TipoCuerpo.PLANETA_ROCOSO) {
            desarrollarCivilizacion();
        }
        RegistroCapacidades.registrarPlaneta(this);
    }

    public double getMasaInicial() {
        return masaInicial;
    }

    public double getMasaBase() {
        return subTipo != null ? subTipo.masaBase : 1.0;
    }

    @Override
    public boolean tieneEscudo() {
        return golpesEscudo > 0;
    }

    @Override
    public int getGolpesEscudoRestantes() {
        return golpesEscudo;
    }

    @Override
    public void activarEscudo(int golpes) {
        this.golpesEscudo = Math.max(this.golpesEscudo, golpes);
    }

    @Override
    public boolean absorberGolpeEscudo() {
        if (golpesEscudo > 0) {
            golpesEscudo--;
            return true;
        }
        return false;
    }

    @Override
    public boolean puedeRecibirEscudo() {
        return true;
    }

    @Override
    public double getRadioEscudo() {
        return 65.0;
    }

    @Override
    public double getPulsoVisual() {
        return 0.0;
    }

    @Override
    public boolean puedeSerColonizado() {
        return !tieneCivilizacion();
    }

    @Override
    public void recibirColonos(double colonos) {
        if (!tieneCivilizacion()) {
            this.tieneCivilizacion = true;
            this.civilizacion = new CivilizacionComponent(this, colonos);
        } else if (civilizacion != null) {
            civilizacion.agregarPoblacion(colonos);
        }
    }

    @Override
    public void desarrollarCivilizacion() {
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

    @Override
    public CivilizacionComponent getCivilizacion() {
        return civilizacion;
    }

    @Override
    public void actualizarCivilizacion(List<CuerpoCeleste> cuerpos) {
        if (civilizacion != null) {
            civilizacion.actualizar(cuerpos);
            if (!civilizacion.estaViva()) {
                this.tieneCivilizacion = false;
            }
        }
    }

    @Override
    public <R> R aceptar(VisitanteCuerpo<R> visitante) {
        return visitante.visitarPlaneta(this);
    }

    public void restaurarAtributosPlaneta(boolean transformado, int escudo, boolean poseeComponenteCivilizacion) {
        this.transformadoPorEvento = transformado;
        this.golpesEscudo = Math.max(0, escudo);
        if (!poseeComponenteCivilizacion) {
            this.civilizacion = null;
            this.tieneCivilizacion = false;
        } else {
            if (this.civilizacion == null) {
                this.civilizacion = new CivilizacionComponent(this);
            }
            this.tieneCivilizacion = this.civilizacion.estaViva();
        }
    }

    public void sincronizarFlagCivilizacion() {
        this.tieneCivilizacion = this.civilizacion != null && this.civilizacion.estaViva();
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

    public boolean puedeFusionarHidroMagmatica(Planeta otro) {
        if (otro == null) return false;
        boolean yoAgua = this.subTipo == TipoCuerpo.PLANETA_AGUA;
        boolean yoLava = this.subTipo == TipoCuerpo.PLANETA_LAVA;
        boolean otroAgua = otro.subTipo == TipoCuerpo.PLANETA_AGUA;
        boolean otroLava = otro.subTipo == TipoCuerpo.PLANETA_LAVA;
        return (yoAgua && otroLava) || (yoLava && otroAgua);
    }

    public TipoCuerpo getSubTipo() {
        return subTipo;
    }

    @Override
    public TipoCuerpo getTipoCuerpo() {
        return subTipo;
    }

    @Override
    public boolean esTransformable() {
        return true;
    }

    @Override
    public void transformarA(TipoCuerpo nuevoTipo) {
        if (nuevoTipo != null) {
            this.subTipo = nuevoTipo;
            this.transformadoPorEvento = true;
            if (civilizacion != null) {
                civilizacion.destruir();
                this.tieneCivilizacion = false;
            }
        }
    }

    @Override
    public boolean permiteReembolso() {
        return !transformadoPorEvento;
    }

    @Override
    public boolean isTransformadoPorEvento() {
        return transformadoPorEvento;
    }

    @Override
    public void setTransformadoPorEvento(boolean transformadoPorEvento) {
        this.transformadoPorEvento = transformadoPorEvento;
    }

    public double getFactorCapacidadPoblacion() {
        return subTipo != null ? subTipo.getFactorCapacidadPoblacion() : 0.0;
    }
}
