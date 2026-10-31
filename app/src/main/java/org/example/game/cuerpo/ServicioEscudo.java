package org.example.game.cuerpo;

import org.example.game.jugador.TipoRecurso;
import org.example.game.motor.AsistenteOrbital;
import org.example.game.motor.Vector2D;
import org.example.game.progresion.ElementoDesbloqueable;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.MensajeEvento;
import org.example.game.simulacion.SimulacionSolar;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de dominio para la activación y gestión de escudos planetarios y satélites orbitales.
 *
 * Principios SOLID:
 * - SRP: Responsabilidad exclusiva de activar el escudo en un planeta y crear su satélite orbital asociado.
 * - DIP: Trabaja sobre abstracciones de simulación y modelo celeste.
 */
public class ServicioEscudo {

    private static final ServicioEscudo INSTANCIA_POR_DEFECTO = new ServicioEscudo();

    private final Map<CuerpoCeleste, Defensor> defensores = new ConcurrentHashMap<>();
    private final Map<Defensor, CuerpoCeleste> cuerposPorDefensor = new ConcurrentHashMap<>();
    private final Map<CuerpoCeleste, Satelite> satelitesEscudo = new ConcurrentHashMap<>();
    private final FabricaCuerpos fabricaCuerpos;

    public ServicioEscudo() {
        this(new CuerpoCelesteFactory());
    }

    public ServicioEscudo(FabricaCuerpos fabricaCuerpos) {
        this.fabricaCuerpos = fabricaCuerpos != null ? fabricaCuerpos : new CuerpoCelesteFactory();
    }

    public static ServicioEscudo getInstanciaPorDefecto() {
        return INSTANCIA_POR_DEFECTO;
    }

    public void registrar(CuerpoCeleste cuerpo, Defensor defensor) {
        if (cuerpo != null && defensor != null) {
            defensores.put(cuerpo, defensor);
            cuerposPorDefensor.put(defensor, cuerpo);
        }
    }

    public void desregistrar(CuerpoCeleste cuerpo) {
        if (cuerpo != null) {
            Defensor def = defensores.remove(cuerpo);
            if (def != null) {
                cuerposPorDefensor.remove(def);
            }
            satelitesEscudo.remove(cuerpo);
        }
    }

    public Optional<Defensor> obtenerDefensor(CuerpoCeleste cuerpo) {
        if (cuerpo == null) return Optional.empty();
        return Optional.ofNullable(defensores.get(cuerpo));
    }

    public boolean tieneEscudo(CuerpoCeleste cuerpo) {
        if (cuerpo == null) return false;
        Defensor def = defensores.get(cuerpo);
        return def != null && def.tieneEscudo();
    }

    public void limpiar() {
        defensores.clear();
        satelitesEscudo.clear();
    }

    /**
     * Calcula el costo de energía para activar el escudo, escalando con la masa del planeta.
     */
    public double calcularCostoEscudo(Defensor defensor) {
        if (defensor == null) return ConfiguracionSimulacion.COSTO_ACTIVACION_ESCUDO_ENERGIA;
        double factorMasa = 1.0;
        double masaBase = defensor.getMasaBase();
        if (masaBase > 0) {
            factorMasa = Math.max(0.3, defensor.getMasa() / masaBase);
        }
        return Math.round(ConfiguracionSimulacion.COSTO_ACTIVACION_ESCUDO_ENERGIA * factorMasa);
    }

    public double calcularCostoEscudo(Planeta planeta) {
        return calcularCostoEscudo((Defensor) planeta);
    }

    public boolean activar(SimulacionSolar simulacion, Planeta planeta) {
        return activar(simulacion, (Defensor) planeta);
    }

    /**
     * Activa el escudo sobre el planeta indicado si está desbloqueado y hay energía suficiente.
     */
    public boolean activar(SimulacionSolar simulacion, Defensor planeta) {
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
        CuerpoCeleste cuerpoPlaneta = cuerposPorDefensor.get(planeta);
        if (cuerpoPlaneta == null && planeta instanceof CuerpoCeleste) {
            cuerpoPlaneta = (CuerpoCeleste) planeta;
        }

        Satelite satExistente = cuerpoPlaneta != null ? satelitesEscudo.get(cuerpoPlaneta) : null;
        if (satExistente != null && simulacion.getSistemaSolar().getCuerpos().contains(satExistente)) {
            yaTieneSatelite = true;
        } else if (cuerpoPlaneta != null) {
            for (CuerpoCeleste c : simulacion.getSistemaSolar().getCuerpos()) {
                if (c.estaOrbitando(cuerpoPlaneta) && c.getMasa() <= 10000.0) {
                    yaTieneSatelite = true;
                    break;
                }
            }
        }

        if (!yaTieneSatelite && cuerpoPlaneta != null) {
            double radioOrbita = planeta.getRadio() + 25.0;
            Vector2D posSatelite = planeta.getPosicion().sumar(new Vector2D(radioOrbita, 0));
            Satelite sat = fabricaCuerpos.crearSatelite(posSatelite.x(), posSatelite.y(), 1.0, Vector2D.cero());
            sat.setCuerpoOrbitado(cuerpoPlaneta);
            sat.setRadioOrbita(radioOrbita);
            satelitesEscudo.put(cuerpoPlaneta, sat);

            AsistenteOrbital asistente = new AsistenteOrbital();
            asistente.inicializarVelocidadOrbital(sat, cuerpoPlaneta, radioOrbita);

            simulacion.agregarCuerpo(sat);
        }

        simulacion.notificarEvento("Escudo activado en " + planeta.getNombre() + " (" + golpes + " golpes). Satélite desplegado.", MensajeEvento.TipoMensaje.EXITO);
        return true;
    }
}
