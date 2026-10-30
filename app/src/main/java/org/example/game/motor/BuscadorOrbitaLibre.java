package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.simulacion.ConfiguracionTransformacion;

import java.util.List;

/**
 * SRP: Algoritmo de exploración y búsqueda de órbitas seguras libres de solapamiento.
 */
public final class BuscadorOrbitaLibre {

    private BuscadorOrbitaLibre() {}

    public static Vector2D encontrarPosicionOrbitalLibre(CuerpoCeleste cuerpo, CuerpoCeleste estrella, double radioActivacion, List<CuerpoCeleste> cuerpos) {
        double radioBase = radioActivacion + 40.0;
        Vector2D dirActual = cuerpo.getPosicion().restar(estrella.getPosicion());
        double anguloActual = (dirActual.magnitud() > 0.001) ? Math.atan2(dirActual.y(), dirActual.x()) : 0.0;

        int pasosAngulares = 16;
        for (int intentoRadio = 0; intentoRadio <= ConfiguracionTransformacion.MAX_INTENTOS_ORBITA_LIBRE; intentoRadio++) {
            double radioTest = radioBase + (intentoRadio * ConfiguracionTransformacion.PASO_BUSQUEDA_ORBITA_PX);

            for (int i = 0; i < pasosAngulares; i++) {
                double offsetAngulo = (i == 0) ? 0.0 : ((i % 2 == 1 ? 1.0 : -1.0) * (Math.PI * 2.0 / pasosAngulares) * ((i + 1) / 2));
                double angulo = anguloActual + offsetAngulo;
                Vector2D posCandidata = estrella.getPosicion().sumar(new Vector2D(
                        Math.cos(angulo) * radioTest,
                        Math.sin(angulo) * radioTest
                ));

                if (posicionEstaLibre(posCandidata, cuerpo, cuerpos)) {
                    return posCandidata;
                }
            }
        }
        return null;
    }

    public static boolean posicionEstaLibre(Vector2D pos, CuerpoCeleste cuerpo, List<CuerpoCeleste> cuerpos) {
        double radioCuerpo = cuerpo.getRadio();
        double margen = ConfiguracionTransformacion.MARGEN_SEGURIDAD_ORBITA_LIBRE;

        for (CuerpoCeleste otro : cuerpos) {
            if (otro == cuerpo || !otro.esSimulado()) continue;
            double dist = pos.distanciaA(otro.getPosicion());
            double distMinimaSegura = radioCuerpo + otro.getRadio() + margen;
            if (dist < distMinimaSegura) {
                return false;
            }
        }
        return true;
    }
}
