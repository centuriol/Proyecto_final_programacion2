package org.example.game.cuerpo;

import org.example.game.civilizacion.CivilizacionComponent;
import java.util.List;

/**
 * Representa un planeta simulado con capacidades de civilización, colonización, defensa y transformación (SRP).
 */
public class Planeta extends CuerpoCeleste implements Civilizable, Colonizable, Defensor, Transformable {

    private final GestorEstadoPlaneta estado;

    public Planeta(String nombre, double masa, double x, double y, TipoCuerpo subTipo) {
        super(nombre, masa, x, y, subTipo);
        this.estado = new GestorEstadoPlaneta(this, masa, subTipo);
        if (subTipo != null && subTipo.puedeTenerCivilizacion) {
            estado.desarrollarCivilizacion();
        }
        RegistroCapacidades.registrarPlaneta(this);
    }

    public double getMasaInicial() { return estado.getMasaInicial(); }
    public double getMasaBase() { return estado.getMasaBase(); }
    @Override public boolean tieneEscudo() { return estado.tieneEscudo(); }
    @Override public int getGolpesEscudoRestantes() { return estado.getGolpesEscudoRestantes(); }
    @Override public void activarEscudo(int golpes) { estado.activarEscudo(golpes); }
    @Override public boolean absorberGolpeEscudo() { return estado.absorberGolpeEscudo(); }
    @Override public boolean puedeRecibirEscudo() { return estado.puedeRecibirEscudo(); }
    @Override public boolean puedeSerColonizado() { return !estado.tieneCivilizacion(); }
    @Override public void recibirColonos(double colonos) { estado.recibirColonos(colonos); }
    @Override public void desarrollarCivilizacion() { estado.desarrollarCivilizacion(); }
    @Override public boolean tieneCivilizacion() { return estado.tieneCivilizacion(); }
    @Override public CivilizacionComponent getCivilizacion() { return estado.getCivilizacion(); }
    @Override public void actualizarCivilizacion(List<CuerpoCeleste> cuerpos) { estado.actualizarCivilizacion(cuerpos); }
    @Override public <R> R aceptar(VisitanteCuerpo<R> visitante) { return visitante.visitarPlaneta(this); }
    public void restaurarAtributosPlaneta(boolean transformado, int escudo, boolean poseeComponenteCivilizacion) { estado.restaurarAtributos(transformado, escudo, poseeComponenteCivilizacion); }
    @Override public String describir() { return estado.describir(); }
    @Override public double getRadio() { return estado.calcularRadio(); }
    @Override public String getColorHex() { return estado.obtenerColorHex(); }
    public boolean puedeFusionarHidroMagmatica(Planeta otro) { return otro != null && estado.puedeFusionarCon(otro.estado); }
    @Override public TipoCuerpo getTipoCuerpo() { return estado.getSubTipo(); }
    @Override public boolean esTransformable() { return true; }
    @Override public void transformarA(TipoCuerpo nuevoTipo) { estado.transformarA(nuevoTipo); }
    @Override public boolean permiteReembolso() { return !estado.isTransformadoPorEvento(); }
    @Override public boolean isTransformadoPorEvento() { return estado.isTransformadoPorEvento(); }
    @Override public void setTransformadoPorEvento(boolean transformadoPorEvento) { estado.setTransformadoPorEvento(transformadoPorEvento); }
}
