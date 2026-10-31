package org.example.game.motor.colision;
import org.example.game.cuerpo.Civilizable;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.cuerpo.Meteorito;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.CuerpoFisico;
import org.example.game.motor.Vector2D;
import org.example.game.simulacion.ConfiguracionColisiones;
import java.util.List;
import java.util.Set;
/**
 * SRP: Métodos cinemáticos y destructivos compartidos para la resolución de colisiones.
 */
public final class UtilidadColision {

    private UtilidadColision() {}

    public static double calcularRatioMasas(CuerpoCeleste a, CuerpoCeleste b) {
        double masaA = Math.max(1e-9, a.getMasa());
        double masaB = Math.max(1e-9, b.getMasa());
        return Math.max(masaA, masaB) / Math.min(masaA, masaB);
    }

    public static void desactivarYEliminar(CuerpoCeleste victima, Set<CuerpoCeleste> aEliminar) {
        if (victima == null) return;
        victima.setSimulado(false);
        victima.setMasa(0);
        destruirCivilizacionSiExiste(victima);
        if (!aEliminar.contains(victima)) {
            aEliminar.add(victima);
        }
    }

    public static void destruirCivilizacionSiExiste(CuerpoCeleste objetivo) {
        if (objetivo != null && objetivo.tieneCivilizacion() && objetivo.getCivilizacion() != null) {
            objetivo.getCivilizacion().destruir();
        }
    }

    public static void absorber(CuerpoCeleste absorbente, CuerpoCeleste victima, Set<CuerpoCeleste> aEliminar) {
        double masaTotal = absorbente.getMasa() + victima.getMasa();
        if (masaTotal > 0) {
            Vector2D momentoAbsorbente = absorbente.getVelocidad().multiplicar(absorbente.getMasa());
            Vector2D momentoVictima = victima.getVelocidad().multiplicar(victima.getMasa());
            Vector2D nuevaVelocidad = momentoAbsorbente.sumar(momentoVictima).dividir(masaTotal);
            absorbente.setVelocidad(nuevaVelocidad);
        }
        absorbente.setMasa(masaTotal);
        desactivarYEliminar(victima, aEliminar);
    }

    public static void generarEscombros(CuerpoCeleste destruido, Set<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar) {
        if (destruido.getTipoCuerpo() == TipoCuerpo.METEORITO) {
            desactivarYEliminar(destruido, aEliminar);
            return;
        }

        double masaNeta = destruido.getMasa() * (1.0 - ConfiguracionColisiones.FRACCION_MASA_DISIPADA);
        desactivarYEliminar(destruido, aEliminar);

        if (masaNeta <= 0) return;

        double topeMasaFragmento = ConfiguracionColisiones.MASA_MAXIMA_METEORITO;
        int requeridosPorTope = (int) Math.ceil(masaNeta / topeMasaFragmento);
        int numFragmentos = Math.max(
                ConfiguracionColisiones.FRAGMENTOS_POR_COLAPSO,
                Math.min(ConfiguracionColisiones.MAX_FRAGMENTOS_POR_COLAPSO, requeridosPorTope)
        );
        if (aAgregar.size() >= 24) {
            numFragmentos = 1;
        }

        double masaRepartible = Math.min(masaNeta, numFragmentos * topeMasaFragmento);
        double masaPorFragmento = Math.min(topeMasaFragmento, masaRepartible / numFragmentos);
        for (int i = 0; i < numFragmentos; i++) {
            Meteorito fragmento = CuerpoCelesteFactory.crearFragmentoEscombro(
                    destruido.getPosicionX(), destruido.getPosicionY(),
                    destruido.getVelocidad(), masaPorFragmento
            );
            aAgregar.add(fragmento);
        }
    }

    public static void reboteElastico(CuerpoFisico a, CuerpoFisico b) {
        Vector2D normal = b.getPosicion().restar(a.getPosicion()).normalizar();
        Vector2D relativo = a.getVelocidad().restar(b.getVelocidad());
        double velocidadNormal = relativo.productoPunto(normal);

        if (velocidadNormal > 0) return;

        double e = 0.5;
        double masaTotalInv = (1.0 / Math.max(1e-9, a.getMasa())) + (1.0 / Math.max(1e-9, b.getMasa()));
        double j = -(1 + e) * velocidadNormal / masaTotalInv;

        Vector2D impulso = normal.multiplicar(j);
        a.setVelocidad(a.getVelocidad().restar(impulso.dividir(a.getMasa())));
        b.setVelocidad(b.getVelocidad().sumar(impulso.dividir(b.getMasa())));

        double overlap = (a.getRadioFisico() + b.getRadioFisico()) - a.getPosicion().distanciaA(b.getPosicion());
        if (overlap > 0) {
            Vector2D separacion = normal.multiplicar(overlap * 0.5);
            a.setPosicion(a.getPosicion().restar(separacion));
            b.setPosicion(b.getPosicion().sumar(separacion));
        }
    }
}
