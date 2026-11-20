package org.example.game.progresion;

import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;
import org.example.game.simulacion.ConfiguracionEconomia;
import org.example.game.simulacion.ConfiguracionSimulacion;

import java.util.*;

/**
 * Gestor del árbol de progresión tecnológica dividido en tres ramas independientes (SRP / OCP).
 */
public class SistemaProgresion {

    private static class EstadoRamas {
        final Map<RamaProgresion, Integer> nivelesPorRama = new EnumMap<>(RamaProgresion.class);
        int nivelEscudo = 0;

        void reiniciar() {
            nivelesPorRama.put(RamaProgresion.PLANETAS, 0);
            nivelesPorRama.put(RamaProgresion.MASA, 0);
            nivelesPorRama.put(RamaProgresion.CANTIDAD_PLANETAS, 0);
            nivelEscudo = 0;
        }

        int nivel(RamaProgresion rama) {
            return rama == null ? 0 : nivelesPorRama.getOrDefault(rama, 0);
        }

        void subirRama(RamaProgresion rama) {
            int nuevo = nivel(rama) + 1;
            nivelesPorRama.put(rama, nuevo);
            if (rama == RamaProgresion.PLANETAS && nuevo >= 4) {
                nivelEscudo = Math.max(nivelEscudo, 1);
            }
        }
    }

    private final EstadoRamas ramas = new EstadoRamas();
    private final RegistroMejorasSecundarias secundarias = new RegistroMejorasSecundarias();

    public SistemaProgresion() { reiniciar(); }

    public void reiniciar() {
        ramas.reiniciar();
        secundarias.limpiar();
    }

    public int getNivel(RamaProgresion rama) { return ramas.nivel(rama); }

    public int getNivelMaximo(RamaProgresion rama) {
        if (rama == null) return 0;
        return switch (rama) {
            case PLANETAS -> ElementoDesbloqueable.values().length;
            case MASA -> ConfiguracionSimulacion.ESCALONES_MASA_MAX.length - 1;
            case CANTIDAD_PLANETAS -> ConfiguracionSimulacion.ESCALONES_CANTIDAD_PLANETAS.length - 1;
        };
    }

    public boolean esNivelMaximo(RamaProgresion rama) { return getNivel(rama) >= getNivelMaximo(rama); }
    public double getCostoSiguiente(RamaProgresion rama) { return (rama == null || esNivelMaximo(rama)) ? -1.0 : ConfiguracionEconomia.costoProgresionNivel(getNivel(rama) + 1); }

    public boolean puedeInvestigar(RamaProgresion rama, InventarioJugador inv) {
        return rama != null && inv != null && !esNivelMaximo(rama) && inv.getRecurso(TipoRecurso.CIENCIA) >= getCostoSiguiente(rama);
    }

    public boolean investigar(RamaProgresion rama, InventarioJugador inv) {
        if (!puedeInvestigar(rama, inv)) return false;
        if (inv.gastarRecurso(TipoRecurso.CIENCIA, getCostoSiguiente(rama))) {
            inv.setProgresion(this);
            ramas.subirRama(rama);
            return true;
        }
        return false;
    }

    public boolean estaInvestigada(MejoraSecundaria mejora) { return secundarias.estaInvestigada(mejora); }
    public boolean estaInvestigada(String id) { return secundarias.estaInvestigada(id); }
    public boolean puedeInvestigarSecundaria(MejoraSecundaria m, InventarioJugador inv) { return secundarias.puedeInvestigar(m, inv, this::getNivel); }
    public boolean puedeInvestigarSecundaria(String id, InventarioJugador inv) { return MejoraSecundaria.porId(id).map(m -> puedeInvestigarSecundaria(m, inv)).orElse(false); }
    public boolean investigarSecundaria(MejoraSecundaria m, InventarioJugador inv) { return secundarias.investigar(m, inv, this, this::getNivel); }
    public boolean investigarSecundaria(String id, InventarioJugador inv) { return MejoraSecundaria.porId(id).map(m -> investigarSecundaria(m, inv)).orElse(false); }
    public Set<String> getMejorasSecundariasInvestigadas() { return secundarias.getInvestigadas(); }
    public double multiplicadorProduccion() { return secundarias.multiplicadorProduccion(); }
    public double multiplicadorProduccion(TipoCuerpo tipo) { return secundarias.multiplicadorProduccion(tipo, null); }
    public double multiplicadorProduccion(TipoCuerpo tipo, TipoRecurso rec) { return secundarias.multiplicadorProduccion(tipo, rec); }
    public double multiplicadorCosto(TipoCuerpo tipo) { return secundarias.multiplicadorCosto(tipo); }
    public double multiplicadorCostoEscudo() { return secundarias.multiplicadorCostoEscudo(); }
    public double multiplicadorCostoColonizacion() { return secundarias.multiplicadorCostoColonizacion(); }
    public double multiplicadorPoblacionMaxima() { return secundarias.multiplicadorPoblacionMaxima(); }
    public double multiplicadorCrecimientoPoblacion() { return secundarias.multiplicadorCrecimientoPoblacion(); }

    public boolean estaDesbloqueado(TipoCuerpo tipo) { return tipo != null && getNivel(RamaProgresion.PLANETAS) >= tipo.getNivelRequerido(); }

    public boolean estaDesbloqueado(ElementoDesbloqueable elemento) {
        if (elemento == null) return false;
        return getNivel(RamaProgresion.PLANETAS) >= (elemento.ordinal() + 1);
    }

    public boolean tieneEscudoDesbloqueado() { return estaDesbloqueado(ElementoDesbloqueable.ESCUDO); }
    public double getLimiteMasaActual() { return ConfiguracionSimulacion.ESCALONES_MASA_MAX[Math.min(getNivel(RamaProgresion.MASA), ConfiguracionSimulacion.ESCALONES_MASA_MAX.length - 1)]; }
    public int getLimitePlanetas() { return ConfiguracionSimulacion.ESCALONES_CANTIDAD_PLANETAS[Math.min(getNivel(RamaProgresion.CANTIDAD_PLANETAS), ConfiguracionSimulacion.ESCALONES_CANTIDAD_PLANETAS.length - 1)]; }
    public int getNivelEscudo() { return ramas.nivelEscudo; }
    public int getGolpesEscudo() { return Math.max(1, 1 + ramas.nivelEscudo); }
    public double getCostoSiguienteNivelEscudo() { return ramas.nivelEscudo >= ConfiguracionSimulacion.NIVEL_ESCUDO_MAX ? -1.0 : ConfiguracionEconomia.costoProgresionNivel(ramas.nivelEscudo + 1); }

    public boolean puedeMejorarNivelEscudo(InventarioJugador inv) {
        return tieneEscudoDesbloqueado() && ramas.nivelEscudo < ConfiguracionSimulacion.NIVEL_ESCUDO_MAX
                && inv != null && inv.getRecurso(TipoRecurso.CIENCIA) >= getCostoSiguienteNivelEscudo();
    }

    public boolean mejorarNivelEscudo(InventarioJugador inv) {
        if (!puedeMejorarNivelEscudo(inv)) return false;
        if (inv.gastarRecurso(TipoRecurso.CIENCIA, getCostoSiguienteNivelEscudo())) {
            ramas.nivelEscudo++;
            return true;
        }
        return false;
    }

    public ElementoDesbloqueable getSiguienteDesbloqueable() {
        int nivel = getNivel(RamaProgresion.PLANETAS);
        ElementoDesbloqueable[] elems = ElementoDesbloqueable.values();
        return nivel < elems.length ? elems[nivel] : null;
    }

    public boolean puedeComprarSiguiente(InventarioJugador inv) { return puedeInvestigar(RamaProgresion.PLANETAS, inv); }
    public boolean comprarSiguienteDesbloqueable(InventarioJugador inv) { return investigar(RamaProgresion.PLANETAS, inv); }

    public void desbloquearTodo() {
        for (RamaProgresion rama : RamaProgresion.values()) ramas.nivelesPorRama.put(rama, getNivelMaximo(rama));
        ramas.nivelEscudo = ConfiguracionSimulacion.NIVEL_ESCUDO_MAX;
    }

    public void desbloquearMejorasSecundarias() { secundarias.desbloquearTodas(); }
    public void restaurarEstado(Map<RamaProgresion, Integer> niveles, int escudo) { restaurarEstado(niveles, escudo, Collections.emptySet()); }

    public void restaurarEstado(Map<RamaProgresion, Integer> niveles, int escudo, Collection<String> mejorasSecundarias) {
        reiniciar();
        if (niveles != null) {
            for (RamaProgresion rama : RamaProgresion.values()) {
                ramas.nivelesPorRama.put(rama, Math.max(0, Math.min(getNivelMaximo(rama), niveles.getOrDefault(rama, 0))));
            }
        }
        ramas.nivelEscudo = Math.max(0, Math.min(ConfiguracionSimulacion.NIVEL_ESCUDO_MAX, escudo));
        if (getNivel(RamaProgresion.PLANETAS) >= 4) ramas.nivelEscudo = Math.max(ramas.nivelEscudo, 1);
        secundarias.restaurar(mejorasSecundarias);
    }
}
