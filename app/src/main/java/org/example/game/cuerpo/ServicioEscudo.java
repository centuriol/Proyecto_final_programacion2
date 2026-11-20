package org.example.game.cuerpo;

import org.example.game.jugador.TipoRecurso;
import org.example.game.progresion.ElementoDesbloqueable;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.MensajeEvento;
import org.example.game.simulacion.SimulacionSolar;

import java.util.Optional;

/**
 * Servicio de dominio para la activación y gestión de escudos planetarios (SRP / DIP).
 */
public class ServicioEscudo {

    private static final ServicioEscudo INSTANCIA_POR_DEFECTO = new ServicioEscudo();

    private final RegistroDefensores registro = new RegistroDefensores();

    public ServicioEscudo() {
    }

    public ServicioEscudo(FabricaCuerpos fabricaCuerpos) {
    }

    public static ServicioEscudo getInstanciaPorDefecto() { return INSTANCIA_POR_DEFECTO; }
    public void registrar(CuerpoCeleste cuerpo, Defensor defensor) { registro.registrar(cuerpo, defensor); }
    public void desregistrar(CuerpoCeleste cuerpo) { registro.desregistrar(cuerpo); }
    public Optional<Defensor> obtenerDefensor(CuerpoCeleste cuerpo) { return registro.obtenerDefensor(cuerpo); }
    public boolean tieneEscudo(CuerpoCeleste cuerpo) { return registro.tieneEscudo(cuerpo); }
    public void limpiar() { registro.limpiar(); }

    public double calcularCostoEscudo(Defensor defensor) {
        if (defensor == null) return ConfiguracionSimulacion.COSTO_ACTIVACION_ESCUDO_ENERGIA;
        double masaBase = defensor.getMasaBase();
        double factorMasa = masaBase > 0 ? Math.max(0.3, defensor.getMasa() / masaBase) : 1.0;
        return Math.round(ConfiguracionSimulacion.COSTO_ACTIVACION_ESCUDO_ENERGIA * factorMasa);
    }

    public double calcularCostoEscudo(Planeta planeta) {
        return calcularCostoEscudo((Defensor) planeta);
    }

    public boolean activar(SimulacionSolar simulacion, Planeta planeta) {
        return activar(simulacion, (Defensor) planeta);
    }

    public boolean activar(SimulacionSolar simulacion, Defensor planeta) {
        if (simulacion == null || planeta == null || !planeta.puedeRecibirEscudo()) return false;

        var progresion = simulacion.getProgresion();
        if (progresion == null || !progresion.estaDesbloqueado(ElementoDesbloqueable.ESCUDO)) {
            simulacion.notificarEvento("El Escudo aún no ha sido desbloqueado con Ciencia.", MensajeEvento.TipoMensaje.ADVERTENCIA);
            return false;
        }

        var inv = simulacion.getInventario();
        double costoEnergia = Math.round(calcularCostoEscudo(planeta) * progresion.multiplicadorCostoEscudo());
        if (inv == null || inv.getRecurso(TipoRecurso.ENERGIA) < costoEnergia) {
            simulacion.notificarEvento("Energía insuficiente para activar el escudo (" + (int) costoEnergia + " ENE requeridos).", MensajeEvento.TipoMensaje.ADVERTENCIA);
            return false;
        }

        inv.gastarRecurso(TipoRecurso.ENERGIA, costoEnergia);
        int golpes = progresion.getGolpesEscudo();
        planeta.activarEscudo(golpes);

        simulacion.notificarEvento("Escudo activado en " + planeta.getNombre() + " (" + golpes + " golpes).", MensajeEvento.TipoMensaje.EXITO);
        return true;
    }
}
