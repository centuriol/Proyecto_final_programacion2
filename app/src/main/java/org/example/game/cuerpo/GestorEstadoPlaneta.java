package org.example.game.cuerpo;

import org.example.game.civilizacion.CivilizacionComponent;
import org.example.game.simulacion.ConfiguracionSimulacion;
import java.util.List;

/**
 * SRP: Encapsula el estado evolutivo del planeta (civilización, escudo, reacción térmica y transformaciones).
 */
public class GestorEstadoPlaneta {

    private static class DatosPlaneta {
        boolean tieneCivilizacion = false;
        TipoCuerpo subTipo;
        boolean transformadoPorEvento = false;
        CivilizacionComponent civilizacion;
        int golpesEscudo = 0;
        final double masaInicial;
        ReaccionTermicaPlaneta reaccionTermica;

        DatosPlaneta(double masaInicial, TipoCuerpo subTipo) {
            this.masaInicial = masaInicial;
            this.subTipo = subTipo;
            this.reaccionTermica = (subTipo != null) ? subTipo.getReaccionTermica() : ReaccionTermicaPlaneta.INERTE;
        }

        void destruirCivilizacion() {
            if (civilizacion != null) {
                civilizacion.destruir();
                civilizacion = null;
            }
            tieneCivilizacion = false;
        }
    }

    private final Planeta planeta;
    private final DatosPlaneta datos;

    public GestorEstadoPlaneta(Planeta planeta, double masaInicial, TipoCuerpo subTipo) {
        this.planeta = planeta;
        this.datos = new DatosPlaneta(masaInicial, subTipo);
    }

    public double getMasaInicial() { return datos.masaInicial; }
    public double getMasaBase() { return datos.subTipo != null ? datos.subTipo.masaBase : 1.0; }
    public TipoCuerpo getSubTipo() { return datos.subTipo; }
    public boolean tieneEscudo() { return datos.golpesEscudo > 0; }
    public int getGolpesEscudoRestantes() { return datos.golpesEscudo; }
    public void activarEscudo(int golpes) { datos.golpesEscudo = Math.max(datos.golpesEscudo, golpes); }
    public boolean isTransformadoPorEvento() { return datos.transformadoPorEvento; }
    public CivilizacionComponent getCivilizacion() { return datos.civilizacion; }

    public boolean absorberGolpeEscudo() {
        if (!tieneEscudo()) return false;
        datos.golpesEscudo--;
        return true;
    }

    public boolean tieneCivilizacion() {
        return datos.tieneCivilizacion && datos.civilizacion != null && datos.civilizacion.estaViva();
    }

    public boolean puedeRecibirEscudo() {
        return !tieneCivilizacion() || (datos.civilizacion != null && datos.civilizacion.estaViva());
    }

    public void recibirColonos(double colonos) {
        if (!tieneCivilizacion()) {
            datos.tieneCivilizacion = true;
            datos.civilizacion = new CivilizacionComponent(planeta, colonos);
        } else if (datos.civilizacion != null) {
            datos.civilizacion.agregarPoblacion(colonos);
        }
    }

    public void desarrollarCivilizacion() {
        if (datos.transformadoPorEvento) return;
        datos.tieneCivilizacion = true;
        if (datos.civilizacion == null) {
            datos.civilizacion = new CivilizacionComponent(planeta, ConfiguracionSimulacion.POBLACION_INICIAL);
        }
    }

    public void actualizarCivilizacion(List<CuerpoCeleste> cuerpos) {
        if (datos.civilizacion != null) {
            datos.civilizacion.actualizar(cuerpos);
            if (!datos.civilizacion.estaViva()) datos.tieneCivilizacion = false;
        }
    }

    public void restaurarAtributos(boolean transformado, int escudo, boolean poseeComponenteCivilizacion) {
        datos.transformadoPorEvento = transformado;
        datos.golpesEscudo = Math.max(0, escudo);
        if (transformado || !poseeComponenteCivilizacion) {
            datos.destruirCivilizacion();
        } else {
            if (datos.civilizacion == null) {
                datos.civilizacion = new CivilizacionComponent(planeta, ConfiguracionSimulacion.POBLACION_INICIAL);
            }
            datos.tieneCivilizacion = datos.civilizacion.estaViva();
        }
        RegistroCapacidades.registrarPlaneta(planeta);
        RegistroCapacidades.obtenerTransformable(planeta).ifPresent(t -> t.setTransformadoPorEvento(transformado));
    }

    public void transformarA(TipoCuerpo nuevoTipo) {
        if (nuevoTipo == null) return;
        datos.subTipo = nuevoTipo;
        datos.reaccionTermica = nuevoTipo.getReaccionTermica();
        datos.transformadoPorEvento = true;
        datos.destruirCivilizacion();
        RegistroCapacidades.registrarPlaneta(planeta);
    }

    public void setTransformadoPorEvento(boolean transformado) {
        datos.transformadoPorEvento = transformado;
        if (transformado) datos.destruirCivilizacion();
        RegistroCapacidades.registrarPlaneta(planeta);
    }

    public boolean puedeFusionarCon(GestorEstadoPlaneta otro) {
        return otro != null && datos.reaccionTermica.interactuar(otro.datos.reaccionTermica);
    }

    public String describir() {
        String civInfo = datos.civilizacion != null
                ? " | " + datos.civilizacion.getEstado().nombre + " (Pop: " + (int) datos.civilizacion.getPoblacion() + ")"
                : "";
        return "Planeta " + planeta.getNombre() + " [" + datos.subTipo.nombre + "] (civ: " + datos.tieneCivilizacion + ")" + civInfo;
    }

    public double calcularRadio() {
        double radioBase = datos.subTipo != null ? datos.subTipo.getRadioVisualBase() : 15.0;
        double factor = planeta.getMasa() / (datos.subTipo != null ? datos.subTipo.masaBase : TipoCuerpo.PLANETA_ROCOSO.masaBase);
        return factor <= 0 ? 3.0 : Math.max(3.0, radioBase * Math.cbrt(factor));
    }

    public String obtenerColorHex() {
        if (datos.civilizacion != null && datos.civilizacion.estaViva()) {
            return datos.civilizacion.getEstado().getColorHex();
        }
        return datos.subTipo.getColorHexString();
    }
}
