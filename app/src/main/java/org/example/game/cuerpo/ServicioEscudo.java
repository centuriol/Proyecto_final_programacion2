package org.example.game.cuerpo;
import org.example.game.jugador.TipoRecurso;
import org.example.game.motor.AsistenteOrbital;
import org.example.game.motor.Vector2D;
import org.example.game.progresion.ElementoDesbloqueable;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.MensajeEvento;
import org.example.game.simulacion.SimulacionSolar;
/**
 * Servicio de dominio para la activación y gestión de escudos planetarios y satélites orbitales.
 *
 * Principios SOLID:
 * - SRP: Responsabilidad exclusiva de activar el escudo en un planeta y crear su satélite orbital asociado.
 * - DIP: Trabaja sobre abstracciones de simulación y modelo celeste.
 */
public class ServicioEscudo {

    private static final ServicioEscudo INSTANCIA_POR_DEFECTO = new ServicioEscudo();

    public ServicioEscudo() {}

    public static ServicioEscudo getInstanciaPorDefecto() {
        return INSTANCIA_POR_DEFECTO;
    }

    /**
     * Calcula el costo de energía para activar el escudo, escalando con la masa del planeta.
     */
    public double calcularCostoEscudo(Planeta planeta) {
        if (planeta == null) return ConfiguracionSimulacion.COSTO_ACTIVACION_ESCUDO_ENERGIA;
        double factorMasa = 1.0;
        if (planeta.getTipoCuerpo() != null && planeta.getTipoCuerpo().masaBase > 0) {
            factorMasa = Math.max(0.3, planeta.getMasa() / planeta.getTipoCuerpo().masaBase);
        }
        return Math.round(ConfiguracionSimulacion.COSTO_ACTIVACION_ESCUDO_ENERGIA * factorMasa);
    }

    /**
     * Activa el escudo sobre el planeta indicado si está desbloqueado y hay energía suficiente.
     * Cobra energía (escalada por masa), aplica golpesEscudo = progresion.getGolpesEscudo(), y crea un único Satélite
     * orbitando el planeta si aún no posee uno.
     */
    public boolean activar(SimulacionSolar simulacion, Planeta planeta) {
        if (simulacion == null || planeta == null || !planeta.puedeRecibirEscudo()) {
            return false;
        }

        var progresion = simulacion.getProgresion();
        if (progresion == null || !progresion.estaDesbloqueado(ElementoDesbloqueable.ESCUDO)) {
            simulacion.notificarEvento("El Escudo aún no ha sido desbloqueado con Ciencia.", MensajeEvento.TipoMensaje.ADVERTENCIA);
            return false;
        }

        var inv = simulacion.getInventario();
        double costoEnergia = calcularCostoEscudo(planeta);
        if (inv == null || inv.getRecurso(TipoRecurso.ENERGIA) < costoEnergia) {
            simulacion.notificarEvento("Energía insuficiente para activar el escudo (" + (int)costoEnergia + " ENE requeridos).", MensajeEvento.TipoMensaje.ADVERTENCIA);
            return false;
        }

        // Cobrar energía
        inv.gastarRecurso(TipoRecurso.ENERGIA, costoEnergia);

        // Activar escudo en el planeta con los golpes correspondientes al nivel de progresión
        int golpes = progresion.getGolpesEscudo();
        planeta.activarEscudo(golpes);

        // Verificar si ya tiene un satélite orbitándolo
        boolean yaTieneSatelite = false;
        for (CuerpoCeleste c : simulacion.getSistemaSolar().getCuerpos()) {
            if (c.getTipoCuerpo() == TipoCuerpo.SATELITE && c.estaOrbitando(planeta)) {
                yaTieneSatelite = true;
                break;
            }
        }

        if (!yaTieneSatelite) {
            double radioOrbita = planeta.getRadio() + 25.0;
            Vector2D posSatelite = planeta.getPosicion().sumar(new Vector2D(radioOrbita, 0));
            Satelite sat = CuerpoCelesteFactory.crearSatelite(posSatelite.x(), posSatelite.y(), 1.0, Vector2D.cero());
            sat.setCuerpoOrbitado(planeta);
            sat.setRadioOrbita(radioOrbita);

            AsistenteOrbital asistente = new AsistenteOrbital();
            asistente.inicializarVelocidadOrbital(sat, planeta, radioOrbita);

            simulacion.agregarCuerpo(sat);
        }

        simulacion.notificarEvento("Escudo activado en " + planeta.getNombre() + " (" + golpes + " golpes). Satélite desplegado.", MensajeEvento.TipoMensaje.EXITO);
        return true;
    }
}
