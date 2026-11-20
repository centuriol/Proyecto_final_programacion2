package org.example.game.colonizacion;

import org.example.game.cuerpo.Colonizable;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Planeta;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.MensajeEvento;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Gestor del subsistema de expediciones de colonización espacial entre planetas (SRP / DIP).
 */
public class GestorColonizacion {

    private static final GestorColonizacion INSTANCIA_GLOBAL = new GestorColonizacion();

    private static class EstadoColonizacion {
        final List<NaveColonizadora> naves = new ArrayList<>();
        final Map<CuerpoCeleste, Colonizable> colonizables = new ConcurrentHashMap<>();

        void registrar(CuerpoCeleste cuerpo, Colonizable col) {
            if (cuerpo != null && col != null) colonizables.put(cuerpo, col);
        }

        void desregistrar(CuerpoCeleste cuerpo) {
            if (cuerpo != null) colonizables.remove(cuerpo);
        }

        void limpiar() {
            naves.clear();
            colonizables.clear();
        }
    }

    private final EstadoColonizacion estado = new EstadoColonizacion();
    private final ServicioPoblacionPlaneta servicioPoblacion = new ServicioPoblacionPlaneta(this::obtenerColonizable);

    public GestorColonizacion() {}
    public static GestorColonizacion getInstanciaGlobal() { return INSTANCIA_GLOBAL; }

    public void registrar(CuerpoCeleste cuerpo, Colonizable colonizable) { estado.registrar(cuerpo, colonizable); }
    public void desregistrar(CuerpoCeleste cuerpo) { estado.desregistrar(cuerpo); }

    public Optional<Colonizable> obtenerColonizable(CuerpoCeleste cuerpo) {
        if (cuerpo == null) return Optional.empty();
        Colonizable col = estado.colonizables.get(cuerpo);
        if (col != null) return Optional.of(col);
        return this != INSTANCIA_GLOBAL ? INSTANCIA_GLOBAL.obtenerColonizable(cuerpo) : Optional.empty();
    }

    public double calcularCostoCienciaPoblar(TipoCuerpo tipo) { return servicioPoblacion.calcularCostoCienciaPoblar(tipo); }
    public double calcularCostoCienciaPoblar(TipoCuerpo tipo, InventarioJugador inv) { return servicioPoblacion.calcularCostoCienciaPoblar(tipo, inv); }
    public List<Planeta> obtenerPlanetasOrigenParaPoblar(CuerpoCeleste destino, List<CuerpoCeleste> cuerpos) { return servicioPoblacion.obtenerPlanetasOrigen(destino, cuerpos); }

    public Planeta buscarPlanetaPorroOrigen(List<CuerpoCeleste> cuerpos) {
        List<Planeta> candidatos = servicioPoblacion.obtenerPlanetasOrigen(null, cuerpos);
        return candidatos.isEmpty() ? null : candidatos.get(0);
    }

    public boolean puedePoblar(CuerpoCeleste destino, List<CuerpoCeleste> cuerpos, InventarioJugador inv) {
        return servicioPoblacion.puedePoblar(destino, cuerpos, inv);
    }

    public boolean poblar(CuerpoCeleste destino, List<CuerpoCeleste> cuerpos, InventarioJugador inv,
                          Consumer<MensajeEvento> emisorEventos, long tickActual) {
        List<Planeta> origenes = servicioPoblacion.obtenerPlanetasOrigen(destino, cuerpos);
        return servicioPoblacion.poblarDesde(origenes.isEmpty() ? null : origenes.get(0), destino, cuerpos, inv, emisorEventos, tickActual);
    }

    public boolean poblarDesde(Planeta origen, CuerpoCeleste destino, List<CuerpoCeleste> cuerpos,
                               InventarioJugador inv, Consumer<MensajeEvento> emisorEventos, long tickActual) {
        return servicioPoblacion.poblarDesde(origen, destino, cuerpos, inv, emisorEventos, tickActual);
    }

    public boolean puedeColonizar(Planeta origen, InventarioJugador inventario) {
        if (origen == null || !origen.tieneCivilizacion() || origen.getCivilizacion() == null || inventario == null) return false;
        if (origen.getCivilizacion().getPorcentajeCapacidad() < ConfiguracionSimulacion.UMBRAL_COLONIZAR_PORCENTAJE) return false;
        double mult = inventario.getProgresion() != null ? inventario.getProgresion().multiplicadorCostoColonizacion() : 1.0;
        return inventario.getRecurso(TipoRecurso.ENERGIA) >= ConfiguracionSimulacion.COSTO_COLONIZAR_ENERGIA * mult
                && inventario.getRecurso(TipoRecurso.MINERALES) >= ConfiguracionSimulacion.COSTO_COLONIZAR_MINERALES * mult;
    }

    public boolean lanzar(Planeta origen, CuerpoCeleste destino, InventarioJugador inventario, Consumer<MensajeEvento> emisorEventos, long tickActual) {
        if (!puedeColonizar(origen, inventario)) {
            emitir(emisorEventos, "No se cumplen los requisitos para colonizar (80% capacidad y recursos).", MensajeEvento.TipoMensaje.ADVERTENCIA, tickActual);
            return false;
        }
        Colonizable colDestino = obtenerColonizable(destino).orElse(null);
        if (colDestino == null || destino == origen || !colDestino.puedeSerColonizado()) {
            emitir(emisorEventos, "El destino seleccionado no puede ser colonizado o ya tiene civilización.", MensajeEvento.TipoMensaje.ADVERTENCIA, tickActual);
            return false;
        }
        double mult = inventario.getProgresion() != null ? inventario.getProgresion().multiplicadorCostoColonizacion() : 1.0;
        inventario.gastarRecurso(TipoRecurso.ENERGIA, ConfiguracionSimulacion.COSTO_COLONIZAR_ENERGIA * mult);
        inventario.gastarRecurso(TipoRecurso.MINERALES, ConfiguracionSimulacion.COSTO_COLONIZAR_MINERALES * mult);

        double colonos = origen.getCivilizacion().getPoblacion() * ConfiguracionSimulacion.PORCENTAJE_POBLACION_COLONIZADORA;
        origen.getCivilizacion().restarPoblacion(colonos);
        estado.naves.add(new NaveColonizadora(origen.getPosicion(), destino, colDestino, colonos));
        emitir(emisorEventos, "Misión de colonización lanzada desde " + origen.getNombre() + " hacia " + destino.getNombre() + " con " + (int) colonos + " colonos.", MensajeEvento.TipoMensaje.INFO, tickActual);
        return true;
    }

    public void actualizar(Consumer<MensajeEvento> emisorEventos, long tickActual) {
        Iterator<NaveColonizadora> it = estado.naves.iterator();
        while (it.hasNext()) {
            NaveColonizadora nave = it.next();
            nave.actualizar();
            if (nave.isCompletada()) {
                it.remove();
                emitir(emisorEventos, "Colonización exitosa: " + nave.getDestino().getNombre() + " ha fundado una nueva colonia con " + (int) nave.getPoblacionColonos() + " habitantes.", MensajeEvento.TipoMensaje.EXITO, tickActual);
            } else if (nave.isPerdida()) {
                it.remove();
                emitir(emisorEventos, "Misión de colonización hacia " + nave.getDestino().getNombre() + " perdida en el espacio.", MensajeEvento.TipoMensaje.PELIGRO, tickActual);
            }
        }
    }

    private void emitir(Consumer<MensajeEvento> emisor, String msg, MensajeEvento.TipoMensaje tipo, long tick) {
        if (emisor != null) emisor.accept(new MensajeEvento(msg, tipo, tick));
    }

    public void agregarNave(NaveColonizadora nave) { if (nave != null) estado.naves.add(nave); }
    public void restaurarNave(NaveColonizadora nave) { if (nave != null) estado.naves.add(nave); }
    public List<NaveColonizadora> getNaves() { return Collections.unmodifiableList(estado.naves); }
    public void limpiar() { estado.limpiar(); }
}
