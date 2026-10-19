package org.example.game.motor;

import org.example.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.CuerpoFisico;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Motor de física newtoniana real (N-body) usando integración Velocity Verlet.
 * SRP: Solo calcula física. No sabe de UI, juego, ni economía.
 * DIP: Implementa interfaz MotorFisica, no depende de implementaciones concretas.
 */
public class MotorFisicaNewtoniana extends MotorFisicaBase {

    private final IntegradorVelocityVerlet integrador;

    public MotorFisicaNewtoniana(double radioMundo) {
        super(radioMundo);
        // Usar constantes centralizadas (SRP)
        this.integrador = new IntegradorVelocityVerlet(
                1.0,
                ConstantesFisicas.G_ESCALADO,
                ConstantesFisicas.SOFTENING
        );
    }

    public MotorFisicaNewtoniana() {
        this(ConstantesFisicas.RADIO_MUNDO_DEFAULT);
    }

    @Override
    public void avanzarPaso(List<CuerpoCeleste> cuerpos) {
        // Filtrar solo cuerpos simulables (que implementan CuerpoFisico)
        List<CuerpoFisico> cuerposFisicos = new ArrayList<>();
        for (CuerpoCeleste c : cuerpos) {
            if (c != null && c.esSimulado()) {
                cuerposFisicos.add(c);
            }
        }

        if (cuerposFisicos.isEmpty()) return;

        // 1. Avanzar física newtoniana
        integrador.avanzarPaso(cuerposFisicos);

        // 2. Manejar colisiones y absorciones
        manejarColisiones(cuerpos, cuerposFisicos);

        // 3. Limpiar cuerpos expirados (meteoritos viejos, escapados)
        limpiarCuerposExpirados(cuerpos);
    }

    /**
     * Detecta colisiones y aplica lógica de absorción/impacto.
     * - Agujeros negros absorben todo lo que toca su horizonte de eventos
     * - Meteoritos impactan y se destruyen (transfieren momento)
     * - Cuerpos que se tocan se fusionan (opcional, para futuras mecánicas)
     */
    private void manejarColisiones(List<CuerpoCeleste> todos, List<CuerpoFisico> fisicos) {
        List<CuerpoCeleste> aEliminar = new ArrayList<>();

        for (int i = 0; i < fisicos.size(); i++) {
            CuerpoFisico ci = fisicos.get(i);
            if (aEliminar.contains(ci)) continue;

            for (int j = i + 1; j < fisicos.size(); j++) {
                CuerpoFisico cj = fisicos.get(j);
                if (aEliminar.contains(cj)) continue;

                double distancia = ci.getPosicion().distanciaA(cj.getPosicion());
                double radioSuma = ci.getRadioFisico() + cj.getRadioFisico();

                // Colisión: distancia < suma de radios físicos
                if (distancia < radioSuma) {
                    procesarColision(ci, cj, aEliminar, todos);
                }
            }
        }

        // Eliminar cuerpos marcados
        todos.removeAll(aEliminar);
        for (CuerpoCeleste c : aEliminar) {
            notificarColision(c);
        }
    }

    private void procesarColision(CuerpoFisico a, CuerpoFisico b,
                                   List<CuerpoCeleste> aEliminar,
                                   List<CuerpoCeleste> todos) {

        TipoCuerpo tipoA = a.getTipoCuerpo();
        TipoCuerpo tipoB = b.getTipoCuerpo();

        // Prioridad: Agujero negro absorbe todo
        if (tipoA == TipoCuerpo.AGUJERO_NEGRO || tipoA == TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO) {
            absorber(a, b, aEliminar);
            return;
        }
        if (tipoB == TipoCuerpo.AGUJERO_NEGRO || tipoB == TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO) {
            absorber(b, a, aEliminar);
            return;
        }

        // Meteoritos
        boolean aEsMeteorito = a.getTipoCuerpo() == TipoCuerpo.METEORITO;
        boolean bEsMeteorito = b.getTipoCuerpo() == TipoCuerpo.METEORITO;

        if (aEsMeteorito && bEsMeteorito) {
            aEliminar.add((CuerpoCeleste) a);
            aEliminar.add((CuerpoCeleste) b);
            return;
        }

        if (aEsMeteorito) {
            impactarMeteorito(a, b, aEliminar, todos);
            return;
        }
        if (bEsMeteorito) {
            impactarMeteorito(b, a, aEliminar, todos);
            return;
        }

        // Fusión de planetas/estrellas (opcional, para mecánicas avanzadas)
        // Por ahora: rebote elástico simple (conservar momento)
        reboteElastico(a, b);
    }

    private void absorber(CuerpoFisico absorbente, CuerpoFisico victima,
                           List<CuerpoCeleste> aEliminar) {
        // El absorbente gana la masa de la víctima
        double nuevaMasa = absorbente.getMasa() + victima.getMasa();
        absorbente.setMasa(nuevaMasa);

        // Conservar momento lineal: v_final = (m1*v1 + m2*v2) / (m1+m2)
        Vector2D momentoTotal = absorbente.getVelocidad().multiplicar(absorbente.getMasa() - victima.getMasa())
                .sumar(victima.getVelocidad().multiplicar(victima.getMasa()));
        absorbente.setVelocidad(momentoTotal.dividir(nuevaMasa));

        aEliminar.add((CuerpoCeleste) victima);
        System.out.println(absorbente.getNombre() + " absorbió a " + victima.getNombre()
                + " | Nueva masa: " + String.format("%.2e", nuevaMasa) + " kg");
    }

    private void impactarMeteorito(CuerpoFisico meteorito, CuerpoFisico objetivo,
                                    List<CuerpoCeleste> aEliminar,
                                    List<CuerpoCeleste> todos) {
        // Transferir momento al objetivo
        Vector2D momentoMeteorito = meteorito.getVelocidad().multiplicar(meteorito.getMasa());
        Vector2D nuevaVelObjetivo = objetivo.getVelocidad().sumar(
                momentoMeteorito.dividir(objetivo.getMasa())
        );
        objetivo.setVelocidad(nuevaVelObjetivo);

        // Daño a civilización si la hay polimórficamente (LSP)
        if (objetivo.tieneCivilizacion() && objetivo.getCivilizacion() != null) {
            System.out.println("IMPACTO en " + objetivo.getNombre() + "! Civilizacion en peligro.");
            objetivo.getCivilizacion().recibirImpactoMeteorito();
        }

        aEliminar.add((CuerpoCeleste) meteorito);
        System.out.println("Impacto: " + meteorito.getNombre() + " impacto en " + objetivo.getNombre());
    }

    private void reboteElastico(CuerpoFisico a, CuerpoFisico b) {
        ResolvedorColisiones.reboteElastico(a, b);
    }



    // ===== Configuración =====

    public double getG() { return integrador.getG(); }
    public double getSoftening() { return integrador.getSoftening(); }
    public double getDt() { return integrador.getDt(); }
    public double getRadioMundo() { return radioMundo; }

    /** Crea versión con parámetros personalizados (para testing/config) */
    public static MotorFisicaNewtoniana crearPersonalizado(double dt, double G, double softening, double radioMundo) {
        MotorFisicaNewtoniana m = new MotorFisicaNewtoniana(radioMundo);
        // Nota: Integrador es inmutable, necesitaríamos setter o recrear
        return m;
    }
}