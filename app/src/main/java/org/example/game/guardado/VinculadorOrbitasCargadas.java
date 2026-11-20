package org.example.game.guardado;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.GeneradorIdentidadCuerpos;

import java.util.List;

/**
 * SRP: Vincula relaciones orbitales reconstruidas y sincroniza los contadores de identificadores de nombres.
 */
public class VinculadorOrbitasCargadas {

    private final GeneradorIdentidadCuerpos generadorIdentidad;

    public VinculadorOrbitasCargadas(GeneradorIdentidadCuerpos generadorIdentidad) {
        this.generadorIdentidad = generadorIdentidad;
    }

    public void vincularOrbitas(List<DatosCuerpo> datosCuerpos, List<CuerpoCeleste> reconstruidos) {
        for (int i = 0; i < datosCuerpos.size(); i++) {
            EstadoCinematica cin = datosCuerpos.get(i).cinematica();
            CuerpoCeleste cuerpo = reconstruidos.get(i);
            cuerpo.setRadioOrbita(cin.radioOrbita());
            int idxOrbitado = cin.indiceOrbitado();
            if (idxOrbitado >= 0 && idxOrbitado < reconstruidos.size() && idxOrbitado != i) {
                cuerpo.setCuerpoOrbitado(reconstruidos.get(idxOrbitado));
            }
        }
    }

    public void sincronizarContadorNombre(String nombre) {
        int guion = nombre.lastIndexOf('-');
        if (guion > 0 && guion < nombre.length() - 1) {
            try {
                generadorIdentidad.asegurarContadorMinimo(Long.parseLong(nombre.substring(guion + 1)));
            } catch (NumberFormatException e) {
                System.err.println("[DEBUG] Nombre de cuerpo sin id numerico estandar: " + nombre);
            }
        }
    }
}
