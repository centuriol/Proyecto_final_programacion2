package org.example.game.progresion;

import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.ToIntFunction;

/**
 * SRP: Gestiona las mejoras secundarias investigadas del árbol tecnológico y calcula sus multiplicadores agregados.
 */
public class RegistroMejorasSecundarias {

    private final Set<String> investigadas = new LinkedHashSet<>();

    public void limpiar() { investigadas.clear(); }

    public boolean estaInvestigada(MejoraSecundaria mejora) {
        return mejora != null && investigadas.contains(mejora.id());
    }

    public boolean estaInvestigada(String idMejora) {
        return idMejora != null && investigadas.contains(idMejora.trim());
    }

    public boolean puedeInvestigar(MejoraSecundaria mejora, InventarioJugador inventario, ToIntFunction<RamaProgresion> nivelRama) {
        if (mejora == null || inventario == null || estaInvestigada(mejora)) return false;
        if (nivelRama.applyAsInt(mejora.rama()) < mejora.tier()) return false;
        return inventario.getRecurso(TipoRecurso.CIENCIA) >= mejora.costo();
    }

    public boolean investigar(MejoraSecundaria mejora, InventarioJugador inventario,
                              SistemaProgresion progresion, ToIntFunction<RamaProgresion> nivelRama) {
        if (!puedeInvestigar(mejora, inventario, nivelRama)) return false;
        if (inventario.gastarRecurso(TipoRecurso.CIENCIA, mejora.costo())) {
            inventario.setProgresion(progresion);
            investigadas.add(mejora.id());
            return true;
        }
        return false;
    }

    public Set<String> getInvestigadas() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(investigadas));
    }

    public void desbloquearTodas() {
        for (MejoraSecundaria m : MejoraSecundaria.values()) investigadas.add(m.id());
    }

    public void restaurar(Collection<String> ids) {
        investigadas.clear();
        if (ids == null) return;
        for (String id : ids) {
            if (id != null && !id.isBlank()) {
                MejoraSecundaria.porId(id).ifPresent(m -> investigadas.add(m.id()));
            }
        }
    }

    private double sumarEfecto(MejoraSecundaria.TipoEfecto tipoEfecto) {
        double suma = 0.0;
        for (MejoraSecundaria m : MejoraSecundaria.values()) {
            if (m.efecto() == tipoEfecto && investigadas.contains(m.id())) suma += m.valor();
        }
        return suma;
    }

    public double multiplicadorProduccion() {
        return 1.0 + sumarEfecto(MejoraSecundaria.TipoEfecto.PRODUCCION_PLANETAS)
                + sumarEfecto(MejoraSecundaria.TipoEfecto.PRODUCCION_MASA);
    }

    public double multiplicadorProduccion(TipoCuerpo tipo, TipoRecurso recurso) {
        double bonus = sumarEfecto(MejoraSecundaria.TipoEfecto.PRODUCCION_MASA);
        if (tipo != null && tipo.esPlaneta()
                && (recurso == null || recurso == TipoRecurso.MINERALES || recurso == TipoRecurso.CIENCIA)) {
            bonus += sumarEfecto(MejoraSecundaria.TipoEfecto.PRODUCCION_PLANETAS);
        }
        return 1.0 + bonus;
    }

    public double multiplicadorCosto(TipoCuerpo tipo) {
        if (tipo == null) return 1.0;
        double descuento = tipo.permiteAjusteMasa() ? sumarEfecto(MejoraSecundaria.TipoEfecto.COSTO_MASA) : 0.0;
        if (tipo.esPlaneta() || tipo == TipoCuerpo.LUNA) descuento += sumarEfecto(MejoraSecundaria.TipoEfecto.COSTO_PLANETAS_LUNAS);
        else if (tipo == TipoCuerpo.SATELITE) descuento += sumarEfecto(MejoraSecundaria.TipoEfecto.COSTO_SATELITE);
        return Math.max(0.2, 1.0 - descuento);
    }

    public double multiplicadorCostoEscudo() { return Math.max(0.2, 1.0 - sumarEfecto(MejoraSecundaria.TipoEfecto.COSTO_ESCUDO)); }
    public double multiplicadorCostoColonizacion() { return Math.max(0.2, 1.0 - sumarEfecto(MejoraSecundaria.TipoEfecto.COSTO_COLONIZACION)); }
    public double multiplicadorPoblacionMaxima() { return 1.0 + sumarEfecto(MejoraSecundaria.TipoEfecto.POBLACION_MAXIMA); }
    public double multiplicadorCrecimientoPoblacion() { return 1.0 + sumarEfecto(MejoraSecundaria.TipoEfecto.CRECIMIENTO_POBLACION); }
}
