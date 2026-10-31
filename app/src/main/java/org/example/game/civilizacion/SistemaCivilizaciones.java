package org.example.game.civilizacion;

import org.example.game.cuerpo.Civilizable;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.simulacion.MensajeEvento;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * SRP: Administra la colección activa de entidades Civilizable y coordina su ciclo de vida y avance temporal.
 * DIP: Trabaja exclusivamente sobre la abstracción Civilizable sin acoplarse a clases concretas.
 */
public class SistemaCivilizaciones {

    private final List<Civilizable> civilizables = new CopyOnWriteArrayList<>();
    private final Map<CuerpoCeleste, Civilizable> porCuerpo = new ConcurrentHashMap<>();

    private static final SistemaCivilizaciones INSTANCIA_GLOBAL = new SistemaCivilizaciones();

    public static SistemaCivilizaciones getInstancia() {
        return INSTANCIA_GLOBAL;
    }

    public void registrar(CuerpoCeleste cuerpo, Civilizable civilizable) {
        if (cuerpo != null && civilizable != null) {
            porCuerpo.put(cuerpo, civilizable);
            if (!civilizables.contains(civilizable)) {
                civilizables.add(civilizable);
            }
        }
    }

    public void desregistrar(CuerpoCeleste cuerpo) {
        if (cuerpo == null) return;
        Civilizable civ = porCuerpo.remove(cuerpo);
        if (civ != null) {
            civilizables.remove(civ);
            if (civ.getCivilizacion() != null) {
                civ.getCivilizacion().destruir();
            }
        }
    }

    public Optional<Civilizable> obtenerCivilizable(CuerpoCeleste cuerpo) {
        if (cuerpo == null) return Optional.empty();
        return Optional.ofNullable(porCuerpo.get(cuerpo));
    }

    public List<Civilizable> getCivilizables() {
        return Collections.unmodifiableList(civilizables);
    }

    public int getCantidadCivilizablesActivos() {
        return civilizables.size();
    }

    public void actualizarCivilizaciones(List<CuerpoCeleste> cuerpos, Consumer<MensajeEvento> emisorEventos) {
        for (Civilizable civ : civilizables) {
            if (civ.tieneCivilizacion()) {
                if (civ.getCivilizacion() != null) {
                    civ.getCivilizacion().setEmisorEventos(emisorEventos);
                }
                civ.actualizarCivilizacion(cuerpos);
            }
        }
    }

    public void limpiar() {
        civilizables.clear();
        porCuerpo.clear();
    }
}
