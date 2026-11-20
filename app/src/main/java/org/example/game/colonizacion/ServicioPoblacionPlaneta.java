package org.example.game.colonizacion;

import org.example.game.cuerpo.Colonizable;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Planeta;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.MensajeEvento;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * SRP: Gestiona la validación y ejecución del poblamiento directo de planetas mediante Ciencia.
 */
public class ServicioPoblacionPlaneta {

    private final Function<CuerpoCeleste, Optional<Colonizable>> resolvedorColonizable;

    public ServicioPoblacionPlaneta(Function<CuerpoCeleste, Optional<Colonizable>> resolvedorColonizable) {
        this.resolvedorColonizable = resolvedorColonizable;
    }

    public double calcularCostoCienciaPoblar(TipoCuerpo tipo) {
        if (tipo == null) return ConfiguracionSimulacion.COSTO_POBLAR_OTRO_CIENCIA;
        return switch (tipo) {
            case PLANETA_AGUA -> ConfiguracionSimulacion.COSTO_POBLAR_AGUA_CIENCIA;
            case PLANETA_LAVA -> ConfiguracionSimulacion.COSTO_POBLAR_LAVA_CIENCIA;
            default -> ConfiguracionSimulacion.COSTO_POBLAR_OTRO_CIENCIA;
        };
    }

    public double calcularCostoCienciaPoblar(TipoCuerpo tipo, InventarioJugador inventario) {
        double base = calcularCostoCienciaPoblar(tipo);
        double mult = (inventario != null && inventario.getProgresion() != null)
                ? inventario.getProgresion().multiplicadorCostoColonizacion()
                : 1.0;
        return base * mult;
    }

    public List<Planeta> obtenerPlanetasOrigen(CuerpoCeleste destino, List<CuerpoCeleste> cuerpos) {
        if (cuerpos == null) return List.of();
        List<Planeta> candidatos = new ArrayList<>();
        for (CuerpoCeleste c : cuerpos) {
            if (c instanceof Planeta p && p != destino && p.esSimulado()
                    && p.tieneCivilizacion() && p.getCivilizacion() != null
                    && p.getCivilizacion().getPoblacion() >= ConfiguracionSimulacion.UMBRAL_POBLACION_PORRO_POBLAR) {
                candidatos.add(p);
            }
        }
        candidatos.sort((a, b) -> Double.compare(b.getCivilizacion().getPoblacion(), a.getCivilizacion().getPoblacion()));
        return candidatos;
    }

    public boolean puedePoblar(CuerpoCeleste destino, List<CuerpoCeleste> cuerpos, InventarioJugador inventario) {
        if (destino == null || inventario == null) return false;
        Colonizable colDestino = resolvedorColonizable.apply(destino).orElse(null);
        if (colDestino == null || !colDestino.puedeSerColonizado()) return false;
        if (obtenerPlanetasOrigen(destino, cuerpos).isEmpty()) return false;
        return inventario.getRecurso(TipoRecurso.CIENCIA) >= calcularCostoCienciaPoblar(destino.getTipoCuerpo(), inventario);
    }

    public boolean poblarDesde(Planeta origen, CuerpoCeleste destino, List<CuerpoCeleste> cuerpos,
                               InventarioJugador inventario, Consumer<MensajeEvento> emisorEventos, long tickActual) {
        List<Planeta> origenesValidos = obtenerPlanetasOrigen(destino, cuerpos);
        if (!puedePoblar(destino, cuerpos, inventario) || origen == null || !origenesValidos.contains(origen)) {
            emitir(emisorEventos, "No se cumplen los requisitos para poblar (planeta origen con +1000 hab. y Ciencia suficiente).",
                    MensajeEvento.TipoMensaje.ADVERTENCIA, tickActual);
            return false;
        }
        Colonizable colDestino = resolvedorColonizable.apply(destino).orElse(null);
        double costoCiencia = calcularCostoCienciaPoblar(destino.getTipoCuerpo(), inventario);
        inventario.gastarRecurso(TipoRecurso.CIENCIA, costoCiencia);

        double colonos = Math.max(100.0, origen.getCivilizacion().getPoblacion() * ConfiguracionSimulacion.PORCENTAJE_POBLACION_COLONIZADORA);
        origen.getCivilizacion().restarPoblacion(colonos);
        colDestino.recibirColonos(colonos);

        emitir(emisorEventos, "Planeta " + destino.getNombre() + " poblado desde " + origen.getNombre()
                + " con " + (int) colonos + " habitantes (-" + (int) costoCiencia + " Ciencia).",
                MensajeEvento.TipoMensaje.EXITO, tickActual);
        return true;
    }

    private void emitir(Consumer<MensajeEvento> emisor, String msg, MensajeEvento.TipoMensaje tipo, long tick) {
        if (emisor != null) emisor.accept(new MensajeEvento(msg, tipo, tick));
    }
}
