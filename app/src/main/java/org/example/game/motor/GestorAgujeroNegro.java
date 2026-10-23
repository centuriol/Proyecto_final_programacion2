package org.example.game.motor;

import org.example.AgujeroNegro;
import org.example.CuerpoCeleste;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Gestor del campo gravitacional extremo y absorción en el horizonte de sucesos de los agujeros negros.
 *
 * Principios SOLID:
 * - SRP: Modela exclusivamente la atracción creciente y la absorción en el horizonte de sucesos.
 * - LSP: Aplica atracción y absorción polimórficamente sin discriminar planetas protegidos por escudo.
 * - DIP: Desacoplado y configurable mediante callbacks.
 */
public class GestorAgujeroNegro {

    public static final double FACTOR_RADIO_INFLUENCIA = 4.0;

    public void procesar(List<CuerpoCeleste> cuerpos, List<CuerpoCeleste> aEliminar, Consumer<String> emisorMensajes) {
        if (cuerpos == null || cuerpos.isEmpty()) return;

        List<AgujeroNegro> agujeros = new ArrayList<>();
        for (CuerpoCeleste c : cuerpos) {
            if (c != null && c.esSimulado() && c.getTipoCuerpo() != null && c.getTipoCuerpo().esAgujeroNegro()) {
                if (c instanceof AgujeroNegro an) {
                    agujeros.add(an);
                }
            }
        }

        if (agujeros.isEmpty()) return;

        for (AgujeroNegro bh : agujeros) {
            double rHorizonte = bh.getRadio();
            double rInfluencia = rHorizonte * FACTOR_RADIO_INFLUENCIA;

            for (CuerpoCeleste c : cuerpos) {
                if (c == bh || c == null || !c.esSimulado() || (c.getTipoCuerpo() != null && c.getTipoCuerpo().esAgujeroNegro())) {
                    continue;
                }

                double dist = bh.getPosicion().distanciaA(c.getPosicion());
                if (dist <= rHorizonte) {
                    // Absorción en el horizonte de sucesos (el escudo NO protege)
                    bh.absorber(c);
                    if (aEliminar != null && !aEliminar.contains(c)) {
                        aEliminar.add(c);
                    }
                    if (emisorMensajes != null) {
                        emisorMensajes.accept(bh.getNombre() + " devoró completamente a " + c.getNombre() + " en su horizonte de eventos.");
                    }
                } else {
                    // Atracción universal implacable sin importar la distancia (alcance total y fuerza extrema)
                    Vector2D dir = bh.getPosicion().restar(c.getPosicion()).normalizar();
                    double distSegura = Math.max(10.0, dist);
                    double fuerzaArrastre = Math.max(12.0, (120000.0 / (distSegura * distSegura)) + (1200.0 / distSegura));
                    c.setVelocidad(c.getVelocidad().sumar(dir.multiplicar(fuerzaArrastre)));
                }
            }
        }
    }

    private static double calcularProximidadNormalizada(CuerpoCeleste c, List<CuerpoCeleste> todos) {
        if (c == null || todos == null || (c.getTipoCuerpo() != null && c.getTipoCuerpo().esAgujeroNegro())) {
            return 1.0;
        }
        double minT = 1.0;
        for (CuerpoCeleste otro : todos) {
            if (otro != null && otro.esSimulado() && otro.getTipoCuerpo() != null && otro.getTipoCuerpo().esAgujeroNegro()) {
                double rHorizonte = otro.getRadio();
                double rInfluencia = rHorizonte * FACTOR_RADIO_INFLUENCIA;
                double dist = otro.getPosicion().distanciaA(c.getPosicion());
                if (dist < rInfluencia) {
                    double t = Math.max(0.0, (dist - rHorizonte) / (rInfluencia - rHorizonte));
                    if (t < minT) minT = t;
                }
            }
        }
        return minT;
    }

    public static double calcularFactorEscala(CuerpoCeleste c, List<CuerpoCeleste> todos) {
        double t = calcularProximidadNormalizada(c, todos);
        return 0.2 + 0.8 * t;
    }

    public static double calcularRotacionExtra(CuerpoCeleste c, List<CuerpoCeleste> todos, long tick) {
        double t = calcularProximidadNormalizada(c, todos);
        if (t >= 1.0) return 0.0;
        double cercania = 1.0 - t;
        return tick * cercania * 15.0;
    }
}
