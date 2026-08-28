package org.example.game.motor;

import org.example.CuerpoCeleste;
import org.example.Estrella;
import org.example.Planeta;
import org.example.AgujeroNegro;
import org.example.game.cuerpo.EscudoProtector;
import org.example.game.cuerpo.Luna;
import org.example.game.cuerpo.Meteorito;
import org.example.game.cuerpo.Satelite;
import org.example.game.cuerpo.TipoCuerpo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Motor de física newtoniana asistida / permisiva para un gameplay amigable y satisfactorio.
 *
 * Principios SOLID:
 * - SRP: Responsable exclusivamente del cálculo de fuerzas gravitacionales, estabilidad orbital permisiva y colisiones.
 * - DIP: Implementa MotorFisica, permitiendo intercambiar el motor sin afectar a la simulación ni a la UI.
 * - OCP: Nuevos tipos de interacción y comportamientos de cuerpos se manejan limpiamente mediante polimorfismo.
 */
public class MotorFisicaPermisiva implements org.example.MotorFisica {

    private final double radioMundo;
    private final double G;
    private final double softening;
    private final double dt;
    private double asistenciaOrbital = 0.015; // 1.5% de corrección sutil hacia estabilidad por tick
    private double velocidadMaxima = 1200.0;
    private final List<Runnable> listenersColision = new ArrayList<>();

    public MotorFisicaPermisiva(double radioMundo) {
        this.radioMundo = radioMundo;
        this.G = ConstantesFisicas.G_ESCALADO;
        // Softening permisivo mayor para evitar tirones infinitos en pases cercanos
        this.softening = 35.0;
        this.dt = 1.0;
    }

    public MotorFisicaPermisiva() {
        this(ConstantesFisicas.RADIO_MUNDO_DEFAULT);
    }

    @Override
    public void avanzarPaso(List<CuerpoCeleste> cuerpos) {
        List<CuerpoCeleste> simulados = new ArrayList<>();
        for (CuerpoCeleste c : cuerpos) {
            if (c.esSimulado()) {
                simulados.add(c);
            }
        }

        if (simulados.isEmpty()) return;

        // 1. Resetear fuerzas
        for (CuerpoCeleste c : simulados) {
            c.resetearFuerza();
        }

        // 2. Calcular Gravedad N-Body con Softening Permisivo
        calcularGravedadNBody(simulados);

        // 3. Aplicar Asistencia de Estabilidad Orbital Permisiva
        aplicarAsistenciaOrbital(simulados);

        // 4. Integrar movimiento (Velocity Verlet / Symplectic Euler mejorado)
        integrarMovimiento(simulados);

        // 5. Procesar Satélites y Escudos
        actualizarEntidadesEspeciales(simulados);

        // 6. Manejo de colisiones, absorciones y rebotes
        manejarColisiones(cuerpos, simulados);

        // 7. Limpiar expirados o fuera del mundo
        limpiarCuerposExpirados(cuerpos);
    }

    private void calcularGravedadNBody(List<CuerpoCeleste> cuerpos) {
        double softeningCuadrado = softening * softening;

        for (int i = 0; i < cuerpos.size(); i++) {
            CuerpoCeleste ci = cuerpos.get(i);

            for (int j = i + 1; j < cuerpos.size(); j++) {
                CuerpoCeleste cj = cuerpos.get(j);

                Vector2D r = cj.getPosicion().restar(ci.getPosicion());
                double r2 = r.magnitudCuadrado() + softeningCuadrado;
                double rMag = Math.sqrt(r2);

                // F = G * m1 * m2 / (r² + eps²)
                double fuerzaMag = G * ci.getMasa() * cj.getMasa() / r2;

                Vector2D dir = r.dividir(rMag);
                Vector2D fuerzaSobreI = dir.multiplicar(fuerzaMag);
                Vector2D fuerzaSobreJ = fuerzaSobreI.multiplicar(-1.0);

                ci.aplicarFuerza(fuerzaSobreI);
                cj.aplicarFuerza(fuerzaSobreJ);
            }
        }
    }

    /**
     * Asistencia Orbital Permisiva:
     * Si un planeta/luna/satélite está cerca de un cuerpo primario masivo (estrella o gigante),
     * aplica una sutil aceleración tangencial para suavizar la excentricidad excesiva y evitar
     * que órbitas casuales colapsen de inmediato en caos.
     */
    private void aplicarAsistenciaOrbital(List<CuerpoCeleste> cuerpos) {
        if (asistenciaOrbital <= 0) return;

        for (CuerpoCeleste c : cuerpos) {
            // Solo asistir a cuerpos no anclas (planetas, lunas, satélites)
            if (c.getTipoCuerpo().esMasivo && c.getMasa() >= TipoCuerpo.ESTRELLA.masaBase * 0.5) {
                continue;
            }

            CuerpoCeleste primario = encontrarCuerpoDominante(c, cuerpos);
            if (primario != null) {
                Vector2D r = c.getPosicion().restar(primario.getPosicion());
                double dist = r.magnitud();
                if (dist > 30.0 && dist < 1200.0) {
                    // Velocidad orbital ideal v = sqrt(G * M / r)
                    double vIdeal = Math.sqrt(G * primario.getMasa() / dist);

                    // Velocidad relativa al cuerpo central
                    Vector2D vRel = c.getVelocidad().restar(primario.getVelocidad());
                    double speedRel = vRel.magnitud();

                    if (speedRel > 0.1) {
                        // Determinar sentido de giro actual (producto cruz 2D: r x v)
                        double cross = r.x * vRel.y - r.y * vRel.x;
                        boolean horario = cross < 0;

                        Vector2D unitRadial = r.dividir(dist);
                        Vector2D unitTangencial = horario
                                ? new Vector2D(unitRadial.y, -unitRadial.x)
                                : new Vector2D(-unitRadial.y, unitRadial.x);

                        Vector2D vObjetivo = unitTangencial.multiplicar(vIdeal);

                        // Aplicar una fuerza de ajuste sutil
                        Vector2D correccion = vObjetivo.restar(vRel).multiplicar(c.getMasa() * asistenciaOrbital);
                        c.aplicarFuerza(correccion);
                    }
                }
            }
        }
    }

    private CuerpoCeleste encontrarCuerpoDominante(CuerpoCeleste cuerpo, List<CuerpoCeleste> todos) {
        CuerpoCeleste dominante = null;
        double maxAtraccion = 0;

        for (CuerpoCeleste otro : todos) {
            if (otro == cuerpo) continue;
            if (!otro.getTipoCuerpo().esMasivo && otro.getMasa() < cuerpo.getMasa() * 5.0) continue;

            double dist = cuerpo.getPosicion().distanciaA(otro.getPosicion());
            if (dist < 1.0) continue;

            double atraccion = otro.getMasa() / (dist * dist + 100.0);
            if (atraccion > maxAtraccion) {
                maxAtraccion = atraccion;
                dominante = otro;
            }
        }
        return dominante;
    }

    private void integrarMovimiento(List<CuerpoCeleste> cuerpos) {
        for (CuerpoCeleste c : cuerpos) {
            Vector2D aceleracion = c.getFuerza().dividir(c.getMasa());

            // Limitar aceleración extrema para estabilidad
            double aMag = aceleracion.magnitud();
            if (aMag > 200.0) {
                aceleracion = aceleracion.normalizar().multiplicar(200.0);
            }

            Vector2D nuevaVel = c.getVelocidad().sumar(aceleracion.multiplicar(dt));

            // Limitar velocidad máxima absoluta
            double vMag = nuevaVel.magnitud();
            if (vMag > velocidadMaxima) {
                nuevaVel = nuevaVel.normalizar().multiplicar(velocidadMaxima);
            }

            Vector2D nuevaPos = c.getPosicion().sumar(nuevaVel.multiplicar(dt));
            c.confirmarPaso(nuevaPos, nuevaVel);
        }
    }

    private void actualizarEntidadesEspeciales(List<CuerpoCeleste> cuerpos) {
        for (CuerpoCeleste c : cuerpos) {
            if (c instanceof Satelite) {
                ((Satelite) c).producirRecursos(dt);
            } else if (c instanceof EscudoProtector) {
                ((EscudoProtector) c).avanzarPulso(dt * 0.05);
            }
        }
    }

    private void manejarColisiones(List<CuerpoCeleste> todos, List<CuerpoCeleste> simulados) {
        List<CuerpoCeleste> aEliminar = new ArrayList<>();

        for (int i = 0; i < simulados.size(); i++) {
            CuerpoCeleste a = simulados.get(i);
            if (aEliminar.contains(a)) continue;

            for (int j = i + 1; j < simulados.size(); j++) {
                CuerpoCeleste b = simulados.get(j);
                if (aEliminar.contains(b)) continue;

                double dist = a.getPosicion().distanciaA(b.getPosicion());
                double radioContacto = a.getRadio() + b.getRadio();

                // 1. Interacción con Cúpula de Escudo
                if (a instanceof EscudoProtector && b instanceof Meteorito) {
                    interactuarEscudoMeteorito((EscudoProtector) a, (Meteorito) b, aEliminar);
                    continue;
                }
                if (b instanceof EscudoProtector && a instanceof Meteorito) {
                    interactuarEscudoMeteorito((EscudoProtector) b, (Meteorito) a, aEliminar);
                    continue;
                }

                // 2. Colisión física
                if (dist < radioContacto) {
                    procesarColision(a, b, aEliminar);
                }
            }
        }

        todos.removeAll(aEliminar);
        for (CuerpoCeleste c : aEliminar) {
            notificarColision(c);
        }
    }

    private void interactuarEscudoMeteorito(EscudoProtector escudo, Meteorito meteorito, List<CuerpoCeleste> aEliminar) {
        double dist = escudo.getPosicion().distanciaA(meteorito.getPosicion());
        if (dist <= escudo.getRadioEscudo()) {
            if (escudo.puedeAbsorberImpacto()) {
                escudo.absorberDano(25.0);
                // Reflejar y desintegrar meteorito
                aEliminar.add(meteorito);
                System.out.println("🛡️ Escudo desvió y vaporizó meteorito!");
            }
        }
    }

    private void procesarColision(CuerpoCeleste a, CuerpoCeleste b, List<CuerpoCeleste> aEliminar) {
        TipoCuerpo tipoA = a.getTipoCuerpo();
        TipoCuerpo tipoB = b.getTipoCuerpo();

        // Agujero negro devora todo
        if (tipoA == TipoCuerpo.AGUJERO_NEGRO || tipoA == TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO) {
            absorber(a, b, aEliminar);
            return;
        }
        if (tipoB == TipoCuerpo.AGUJERO_NEGRO || tipoB == TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO) {
            absorber(b, a, aEliminar);
            return;
        }

        // Estrella devora meteoritos o satélites
        if (tipoA == TipoCuerpo.ESTRELLA && (b instanceof Meteorito || b instanceof Satelite)) {
            aEliminar.add(b);
            return;
        }
        if (tipoB == TipoCuerpo.ESTRELLA && (a instanceof Meteorito || a instanceof Satelite)) {
            aEliminar.add(a);
            return;
        }

        // Meteorito impacta planeta o luna
        if (a instanceof Meteorito && !(b instanceof Meteorito)) {
            impactarMeteorito((Meteorito) a, b, aEliminar);
            return;
        }
        if (b instanceof Meteorito && !(a instanceof Meteorito)) {
            impactarMeteorito((Meteorito) b, a, aEliminar);
            return;
        }

        // Fusión o rebote elástico suave
        reboteElastico(a, b);
    }

    private void absorber(CuerpoCeleste absorbente, CuerpoCeleste victima, List<CuerpoCeleste> aEliminar) {
        double nuevaMasa = absorbente.getMasa() + victima.getMasa();
        absorbente.setMasa(nuevaMasa);

        // Conservar momento
        Vector2D momento = absorbente.getVelocidad().multiplicar(absorbente.getMasa() - victima.getMasa())
                .sumar(victima.getVelocidad().multiplicar(victima.getMasa()));
        absorbente.setVelocidad(momento.dividir(nuevaMasa));

        aEliminar.add(victima);
    }

    private void impactarMeteorito(Meteorito meteorito, CuerpoCeleste objetivo, List<CuerpoCeleste> aEliminar) {
        Vector2D momento = meteorito.getVelocidad().multiplicar(meteorito.getMasa());
        Vector2D nuevaVel = objetivo.getVelocidad().sumar(momento.dividir(objetivo.getMasa() * 2.0));
        objetivo.setVelocidad(nuevaVel);

        if (objetivo instanceof Planeta) {
            Planeta p = (Planeta) objetivo;
            if (p.tieneCivilizacion()) {
                p.getCivilizacion().recibirImpactoMeteorito();
            }
        }
        aEliminar.add(meteorito);
    }

    private void reboteElastico(CuerpoCeleste a, CuerpoCeleste b) {
        Vector2D delta = b.getPosicion().restar(a.getPosicion());
        double dist = delta.magnitud();
        if (dist < 0.001) return;

        Vector2D normal = delta.dividir(dist);
        Vector2D relativo = a.getVelocidad().restar(b.getVelocidad());
        double velNormal = relativo.productoPunto(normal);

        if (velNormal > 0) return;

        double e = 0.4; // Coeficiente elástico suave
        double j = -(1.0 + e) * velNormal / (1.0 / a.getMasa() + 1.0 / b.getMasa());
        Vector2D impulso = normal.multiplicar(j);

        a.setVelocidad(a.getVelocidad().restar(impulso.dividir(a.getMasa())));
        b.setVelocidad(b.getVelocidad().sumar(impulso.dividir(b.getMasa())));

        // Separar
        double overlap = (a.getRadio() + b.getRadio()) - dist;
        if (overlap > 0) {
            Vector2D sep = normal.multiplicar(overlap * 0.5);
            a.mover(a.getPosicionX() - sep.x, a.getPosicionY() - sep.y);
            b.mover(b.getPosicionX() + sep.x, b.getPosicionY() + sep.y);
        }
    }

    private void limpiarCuerposExpirados(List<CuerpoCeleste> cuerpos) {
        Iterator<CuerpoCeleste> it = cuerpos.iterator();
        while (it.hasNext()) {
            CuerpoCeleste c = it.next();
            if (c instanceof Meteorito && ((Meteorito) c).estaExpirado()) {
                it.remove();
                continue;
            }

            double dist = c.getPosicion().magnitud();
            if (dist > radioMundo * ConstantesFisicas.LIMITE_ESCAPE_FACTOR) {
                it.remove();
            }
        }
    }

    public void agregarListenerColision(Runnable listener) {
        listenersColision.add(listener);
    }

    private void notificarColision(CuerpoCeleste cuerpo) {
        for (Runnable l : listenersColision) {
            try { l.run(); } catch (Exception ignored) {}
        }
    }

    public double getAsistenciaOrbital() {
        return asistenciaOrbital;
    }

    public void setAsistenciaOrbital(double asistencia) {
        this.asistenciaOrbital = Math.max(0, Math.min(0.1, asistencia));
    }
}
