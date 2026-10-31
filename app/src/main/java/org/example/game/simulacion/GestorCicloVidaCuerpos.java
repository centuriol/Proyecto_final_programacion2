package org.example.game.simulacion;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.cuerpo.SistemaSolar;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.jugador.InventarioJugador;
import org.example.game.motor.Vector2D;
import org.example.game.progresion.SistemaProgresion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * SRP: Administra el ciclo de vida (instanciacion, validacion de spawn, registro,
 * eliminacion en cascada y conteo) de los cuerpos celestes en el sistema solar.
 */
public class GestorCicloVidaCuerpos {

    private final SistemaSolar sistemaSolar;
    private final ValidadorColocacionCuerpos validador;
    private final SistemaProgresion progresion;
    private final InventarioJugador inventario;
    private final BusEventos busEventos;
    private final org.example.game.cuerpo.FabricaCuerpos fabricaCuerpos;

    public GestorCicloVidaCuerpos(SistemaSolar sistemaSolar,
                                  ValidadorColocacionCuerpos validador,
                                  SistemaProgresion progresion,
                                  InventarioJugador inventario,
                                  BusEventos busEventos) {
        this(sistemaSolar, validador, progresion, inventario, busEventos, new org.example.game.cuerpo.CuerpoCelesteFactory());
    }

    public GestorCicloVidaCuerpos(SistemaSolar sistemaSolar,
                                  ValidadorColocacionCuerpos validador,
                                  SistemaProgresion progresion,
                                  InventarioJugador inventario,
                                  BusEventos busEventos,
                                  org.example.game.cuerpo.FabricaCuerpos fabricaCuerpos) {
        this.sistemaSolar = sistemaSolar;
        this.validador = validador;
        this.progresion = progresion;
        this.inventario = inventario;
        this.busEventos = busEventos;
        this.fabricaCuerpos = fabricaCuerpos != null ? fabricaCuerpos : new org.example.game.cuerpo.CuerpoCelesteFactory();
    }

    public int getCantidadPlanetasActuales() {
        int count = 0;
        for (CuerpoCeleste c : sistemaSolar.getCuerpos()) {
            if (c != null && c.esSimulado() && org.example.game.cuerpo.RegistroCapacidades.obtenerCivilizable(c).isPresent()) {
                count++;
            }
        }
        return count;
    }

    public Optional<CuerpoCeleste> spawnCuerpo(TipoCuerpo tipo, String nombrePersonalizado,
                                               double x, double y, double factorMasa,
                                               Vector2D velocidadInicial,
                                               java.util.function.BiConsumer<String, MensajeEvento.TipoMensaje> notificador,
                                               Runnable alCompletarColocacion) {
        double factorEfectivo = (tipo == TipoCuerpo.SATELITE) ? 1.0 : factorMasa;
        if (!validarRequisitosColocacion(tipo, factorEfectivo, notificador)) {
            return Optional.empty();
        }

        CuerpoCeleste cuerpo = fabricaCuerpos.crearCuerpo(tipo, x, y, factorEfectivo, velocidadInicial);
        if (cuerpo == null) return Optional.empty();

        if (nombrePersonalizado != null && !nombrePersonalizado.isBlank()) {
            cuerpo.setNombre(nombrePersonalizado.trim());
        }
        if (inventario.gastarParaCrear(tipo, factorEfectivo)) {
            agregarCuerpo(cuerpo);
            if (alCompletarColocacion != null) {
                alCompletarColocacion.run();
            }
            if (notificador != null) {
                notificador.accept("Cuerpo colocado: " + cuerpo.getNombre() + " (" + tipo.nombre + ")", MensajeEvento.TipoMensaje.INFO);
            }
            return Optional.of(cuerpo);
        }
        return Optional.empty();
    }

    public boolean validarRequisitosColocacion(TipoCuerpo tipo, double factorMasa, java.util.function.BiConsumer<String, MensajeEvento.TipoMensaje> notificador) {
        return validador.validar(
                tipo,
                factorMasa,
                getCantidadPlanetasActuales(),
                progresion,
                inventario,
                notificador
        );
    }

    public void agregarCuerpo(CuerpoCeleste cuerpo) {
        sistemaSolar.agregarCuerpo(cuerpo);
        busEventos.notificarCambioEstado();
    }

    public void eliminarCuerpo(CuerpoCeleste cuerpo) {
        if (cuerpo == null) return;
        cuerpo.setSimulado(false);
        cuerpo.setMasa(0);
        org.example.game.cuerpo.RegistroCapacidades.desregistrar(cuerpo);
        sistemaSolar.getCuerpos().remove(cuerpo);

        List<CuerpoCeleste> satelitesAEliminar = new ArrayList<>();
        for (CuerpoCeleste c : sistemaSolar.getCuerpos()) {
            if (c.getCuerpoOrbitado() == cuerpo) {
                if (c.getMasa() <= 10000.0) {
                    satelitesAEliminar.add(c);
                }
                c.setCuerpoOrbitado(null);
            }
        }
        for (CuerpoCeleste s : satelitesAEliminar) {
            eliminarCuerpo(s);
        }

        busEventos.notificarCambioEstado();
    }

    public void limpiarCuerpos() {
        sistemaSolar.getCuerpos().clear();
        org.example.game.cuerpo.RegistroCapacidades.limpiar();
        busEventos.notificarCambioEstado();
    }
}
