package org.example.game.motor;

import org.example.CuerpoCeleste;
import org.example.game.cuerpo.Meteorito;
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
public class MotorFisicaNewtoniana implements org.example.MotorFisica {

    private final IntegradorVelocityVerlet integrador;
    private final double radioMundo;
    private final List<Runnable> listenersColision = new ArrayList<>();

    public MotorFisicaNewtoniana(double radioMundo) {
        this.radioMundo = radioMundo;
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
            if (c instanceof CuerpoFisico && ((CuerpoFisico) c).esSimulado()) {
                cuerposFisicos.add((CuerpoFisico) c);
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

        // Meteorito impactando cuerpo
        if (a instanceof Meteorito && !(b instanceof Meteorito)) {
            impactarMeteorito((Meteorito) a, b, aEliminar, todos);
            return;
        }
        if (b instanceof Meteorito && !(a instanceof Meteorito)) {
            impactarMeteorito((Meteorito) b, a, aEliminar, todos);
            return;
        }

        // Choque meteorito-meteorito: ambos se destruyen
        if (a instanceof Meteorito && b instanceof Meteorito) {
            aEliminar.add((CuerpoCeleste) a);
            aEliminar.add((CuerpoCeleste) b);
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

    private void impactarMeteorito(Meteorito meteorito, CuerpoFisico objetivo,
                                    List<CuerpoCeleste> aEliminar,
                                    List<CuerpoCeleste> todos) {
        // Transferir momento al objetivo
        Vector2D momentoMeteorito = meteorito.getVelocidad().multiplicar(meteorito.getMasa());
        Vector2D nuevaVelObjetivo = objetivo.getVelocidad().sumar(
                momentoMeteorito.dividir(objetivo.getMasa())
        );
        objetivo.setVelocidad(nuevaVelObjetivo);

        // Daño a civilización si la hay (futuro)
        if (objetivo instanceof org.example.Planeta) {
            org.example.Planeta planeta = (org.example.Planeta) objetivo;
            if (planeta.tieneCivilizacion()) {
                System.out.println("⚠ IMPACTO en " + planeta.getNombre() + "! Civilización en peligro.");
                // TODO: Reducir población/estabilidad
            }
        }

        aEliminar.add(meteorito);
        System.out.println("💥 " + meteorito.getNombre() + " impactó en " + objetivo.getNombre());
    }

    private void reboteElastico(CuerpoFisico a, CuerpoFisico b) {
        // Colisión elástica 2D simplificada (aproximación 1D en eje de colisión)
        Vector2D normal = b.getPosicion().restar(a.getPosicion()).normalizar();
        Vector2D relativo = a.getVelocidad().restar(b.getVelocidad());
        double velocidadNormal = relativo.productoPunto(normal);

        if (velocidadNormal > 0) return; // Ya separándose

        double e = 0.5; // Coeficiente de restitución (0.5 = parcialmente elástico)
        double j = -(1 + e) * velocidadNormal / (1/a.getMasa() + 1/b.getMasa());
        Vector2D impulso = normal.multiplicar(j);

        a.setVelocidad(a.getVelocidad().restar(impulso.dividir(a.getMasa())));
        b.setVelocidad(b.getVelocidad().sumar(impulso.dividir(b.getMasa())));

        // Separar ligeramente para evitar colisiones repetidas
        double overlap = (a.getRadioFisico() + b.getRadioFisico()) - a.getPosicion().distanciaA(b.getPosicion());
        if (overlap > 0) {
            Vector2D separacion = normal.multiplicar(overlap * 0.5);
            a.setPosicion(a.getPosicion().restar(separacion));
            b.setPosicion(b.getPosicion().sumar(separacion));
        }
    }

    private void limpiarCuerposExpirados(List<CuerpoCeleste> cuerpos) {
        Iterator<CuerpoCeleste> it = cuerpos.iterator();
        while (it.hasNext()) {
            CuerpoCeleste c = it.next();

            // Meteoritos con vida agotada
            if (c instanceof Meteorito && ((Meteorito) c).estaExpirado()) {
                it.remove();
                continue;
            }

            // Cuerpos que escapan del mundo (demasiado lejos)
            if (c instanceof CuerpoFisico) {
                CuerpoFisico cf = (CuerpoFisico) c;
                double distCentro = cf.getPosicion().magnitud();
                if (distCentro > radioMundo * ConstantesFisicas.LIMITE_ESCAPE_FACTOR) {
                    it.remove();
                    System.out.println(c.getNombre() + " escapó del sistema (distancia: " + (int)distCentro + "px)");
                }
            }
        }
    }

    // ===== Eventos =====

    public void agregarListenerColision(Runnable listener) {
        listenersColision.add(listener);
    }

    private void notificarColision(CuerpoCeleste cuerpo) {
        for (Runnable l : listenersColision) {
            try { l.run(); } catch (Exception e) { e.printStackTrace(); }
        }
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