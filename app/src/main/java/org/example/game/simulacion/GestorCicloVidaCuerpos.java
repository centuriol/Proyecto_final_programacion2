package org.example.game.simulacion;

import org.example.game.cuerpo.*;
import org.example.game.jugador.InventarioJugador;
import org.example.game.motor.Vector2D;
import org.example.game.progresion.SistemaProgresion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * SRP: Administra el ciclo de vida (instanciación, validación de spawn, registro,
 * eliminación en cascada y conteo) de los cuerpos celestes en el sistema solar.
 */
public class GestorCicloVidaCuerpos {

    private static final FabricaSimulacion FABRICA_SIM = new FabricaSimulacion();

    private static class ServiciosSpawn {
        final ValidadorColocacion validador;
        final SistemaProgresion progresion;
        final InventarioJugador inventario;
        final BusEventos busEventos;
        final FabricaCuerpos fabricaCuerpos;

        ServiciosSpawn(ValidadorColocacion v, SistemaProgresion p, InventarioJugador i, BusEventos b, FabricaCuerpos f) {
            this.validador = v;
            this.progresion = p;
            this.inventario = i;
            this.busEventos = b;
            this.fabricaCuerpos = f != null ? f : FABRICA_SIM.crearFabricaCuerpos();
        }

        boolean validar(TipoCuerpo tipo, double factorMasa, int planetasActuales, BiConsumer<String, MensajeEvento.TipoMensaje> notificador) {
            return validador.validar(tipo, factorMasa, planetasActuales, progresion, inventario, notificador);
        }

        void notificarCambio() {
            busEventos.notificarCambioEstado();
        }
    }

    private final SistemaSolar sistemaSolar;
    private final ServiciosSpawn servicios;

    public GestorCicloVidaCuerpos(SistemaSolar sistemaSolar, ValidadorColocacion validador,
                                  SistemaProgresion progresion, InventarioJugador inventario, BusEventos busEventos) {
        this(sistemaSolar, validador, progresion, inventario, busEventos, FABRICA_SIM.crearFabricaCuerpos());
    }

    public GestorCicloVidaCuerpos(SistemaSolar sistemaSolar, ValidadorColocacion validador,
                                  SistemaProgresion progresion, InventarioJugador inventario,
                                  BusEventos busEventos, FabricaCuerpos fabricaCuerpos) {
        this.sistemaSolar = sistemaSolar;
        this.servicios = new ServiciosSpawn(validador, progresion, inventario, busEventos, fabricaCuerpos);
    }

    public int getCantidadPlanetasActuales() {
        int count = 0;
        for (CuerpoCeleste c : sistemaSolar.getCuerpos()) {
            if (c != null && c.esSimulado() && RegistroCapacidades.obtenerCivilizable(c).isPresent()) {
                count++;
            }
        }
        return count;
    }

    public Optional<CuerpoCeleste> spawnCuerpo(TipoCuerpo tipo, double x, double y, double factorMasa, Vector2D velocidadInicial) {
        return spawnCuerpo(tipo, null, x, y, factorMasa, velocidadInicial);
    }

    public Optional<CuerpoCeleste> spawnCuerpo(TipoCuerpo tipo, String nombrePersonalizado, double x, double y, double factorMasa, Vector2D velocidadInicial) {
        return spawnCuerpo(tipo, nombrePersonalizado, x, y, factorMasa, velocidadInicial,
                (msg, tipoMsg) -> { if (servicios.busEventos != null) servicios.busEventos.notificarEvento(msg, tipoMsg, 0L); }, () -> {});
    }

    public Optional<CuerpoCeleste> spawnCuerpo(TipoCuerpo tipo, String nombrePersonalizado,
                                               double x, double y, double factorMasa, Vector2D velocidadInicial,
                                               BiConsumer<String, MensajeEvento.TipoMensaje> notificador,
                                               Runnable alCompletarColocacion) {
        double factorEfectivo = tipo != null ? tipo.ajustarFactorMasaEfectivo(factorMasa) : factorMasa;
        if (!validarRequisitosColocacion(tipo, factorEfectivo, notificador)) return Optional.empty();

        CuerpoCeleste cuerpo = servicios.fabricaCuerpos.crearCuerpo(tipo, x, y, factorEfectivo, velocidadInicial);
        if (cuerpo == null) return Optional.empty();

        if (nombrePersonalizado != null && !nombrePersonalizado.isBlank()) cuerpo.setNombre(nombrePersonalizado.trim());
        if (servicios.inventario.gastarParaCrear(tipo, factorEfectivo)) {
            agregarCuerpo(cuerpo);
            if (alCompletarColocacion != null) alCompletarColocacion.run();
            if (notificador != null) {
                notificador.accept("Cuerpo colocado: " + cuerpo.getNombre() + " (" + tipo.nombre + ")", MensajeEvento.TipoMensaje.INFO);
            }
            return Optional.of(cuerpo);
        }
        return Optional.empty();
    }

    public boolean validarRequisitosColocacion(TipoCuerpo tipo, double factorMasa, BiConsumer<String, MensajeEvento.TipoMensaje> notificador) {
        return servicios.validar(tipo, factorMasa, getCantidadPlanetasActuales(), notificador);
    }

    public void agregarCuerpo(CuerpoCeleste cuerpo) {
        if (cuerpo != null && cuerpo.getTipoCuerpo() == TipoCuerpo.METEORITO
                && (cuerpo.getNombre() == null || !cuerpo.getNombre().startsWith("Escombro"))) {
            double masaMaxPlaneta = obtenerMasaMaximaPlaneta();
            if (masaMaxPlaneta > 0.0) {
                cuerpo.setMasa(masaMaxPlaneta * ConfiguracionColisiones.FRACCION_MASA_METEORITO_PLANETA_MAX);
            }
        }
        sistemaSolar.agregarCuerpo(cuerpo);
        servicios.notificarCambio();
    }

    private double obtenerMasaMaximaPlaneta() {
        double max = 0.0;
        for (CuerpoCeleste c : sistemaSolar.getCuerpos()) {
            if (c != null && c.esSimulado() && c.getTipoCuerpo() != null && c.getTipoCuerpo().esPlaneta()) {
                max = Math.max(max, c.getMasa());
            }
        }
        return max;
    }

    public void eliminarCuerpo(CuerpoCeleste cuerpo) {
        if (cuerpo == null) return;
        cuerpo.setSimulado(false);
        cuerpo.setMasa(0);
        RegistroCapacidades.desregistrar(cuerpo);
        sistemaSolar.getCuerpos().remove(cuerpo);

        List<CuerpoCeleste> satelitesAEliminar = new ArrayList<>();
        for (CuerpoCeleste c : sistemaSolar.getCuerpos()) {
            if (c.getCuerpoOrbitado() == cuerpo) {
                if (c.getMasa() <= 10000.0) satelitesAEliminar.add(c);
                c.setCuerpoOrbitado(null);
            }
        }
        for (CuerpoCeleste s : satelitesAEliminar) eliminarCuerpo(s);
        servicios.notificarCambio();
    }

    public List<CuerpoCeleste> getCuerpos() { return sistemaSolar.getCuerpos(); }

    public void limpiarCuerpos() {
        sistemaSolar.getCuerpos().clear();
        RegistroCapacidades.limpiar();
        servicios.notificarCambio();
    }
}
