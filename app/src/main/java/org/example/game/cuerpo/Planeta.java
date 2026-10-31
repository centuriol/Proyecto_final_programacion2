package org.example.game.cuerpo;

import org.example.game.civilizacion.CivilizacionComponent;
import org.example.game.simulacion.ConfiguracionSimulacion;
import java.util.List;

public class Planeta extends CuerpoCeleste implements Civilizable, Colonizable, Defensor, Transformable {
    private boolean tieneCivilizacion = false;
    private TipoCuerpo subTipo;
    private boolean transformadoPorEvento = false;
    private CivilizacionComponent civilizacion;
    private int golpesEscudo = 0;
    private final double masaInicial;
    private ReaccionTermicaPlaneta reaccionTermica;

    public Planeta(String nombre, double masa, double x, double y, TipoCuerpo subTipo) {
        super(nombre, masa, x, y, subTipo);
        this.subTipo = subTipo;
        this.masaInicial = masa;
        this.reaccionTermica = (subTipo != null) ? subTipo.getReaccionTermica() : ReaccionTermicaPlaneta.INERTE;
        if (subTipo != null && subTipo.puedeTenerCivilizacion) {
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
        if (tieneEscudo()) {
            golpesEscudo--;
            return true;
        }
        return false;
    }

    @Override
    public boolean puedeRecibirEscudo() {
        return !tieneCivilizacion() || (civilizacion != null && civilizacion.estaViva());
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
            civilizacion = new CivilizacionComponent(this, ConfiguracionSimulacion.POBLACION_INICIAL);
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
                this.civilizacion = new CivilizacionComponent(this, ConfiguracionSimulacion.POBLACION_INICIAL);
            }
            this.tieneCivilizacion = this.civilizacion.estaViva();
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
        double radioBase = (subTipo != null) ? subTipo.getRadioVisualBase() : 15.0;
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
        return otro != null && this.reaccionTermica.interactuar(otro.reaccionTermica);
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
            this.reaccionTermica = nuevoTipo.getReaccionTermica();
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
}
