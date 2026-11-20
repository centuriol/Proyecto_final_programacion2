package org.example.game.cuerpo;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SRP: Mantiene el registro de cuerpos que implementan la capacidad Defensor y sus escudos activos.
 */
public class RegistroDefensores {

    private final Map<CuerpoCeleste, Defensor> defensores = new ConcurrentHashMap<>();
    private final Map<Defensor, CuerpoCeleste> cuerposPorDefensor = new ConcurrentHashMap<>();

    public void registrar(CuerpoCeleste cuerpo, Defensor defensor) {
        if (cuerpo != null && defensor != null) {
            defensores.put(cuerpo, defensor);
            cuerposPorDefensor.put(defensor, cuerpo);
        }
    }

    public void desregistrar(CuerpoCeleste cuerpo) {
        if (cuerpo != null) {
            Defensor def = defensores.remove(cuerpo);
            if (def != null) cuerposPorDefensor.remove(def);
        }
    }

    public Optional<Defensor> obtenerDefensor(CuerpoCeleste cuerpo) {
        return cuerpo == null ? Optional.empty() : Optional.ofNullable(defensores.get(cuerpo));
    }

    public boolean tieneEscudo(CuerpoCeleste cuerpo) {
        if (cuerpo == null) return false;
        Defensor def = defensores.get(cuerpo);
        return def != null && def.tieneEscudo();
    }

    public void limpiar() {
        defensores.clear();
        cuerposPorDefensor.clear();
    }
}
