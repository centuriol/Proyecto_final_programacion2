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
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.simulacion.ConfiguracionSimulacion;

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
    private java.util.function.Consumer<String> listenerMensaje;
    private Runnable listenerDefensaEscudo;

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
                double distReal = r.magnitud();
                double distSegura = Math.max(0.1, distReal);
                double r2 = distSegura * distSegura + softeningCuadrado;
                double rMag = Math.sqrt(r2);

                // 1. Interacción orbital especial: determinar si ya orbitan o si se captura en órbita asistida
                CuerpoCeleste orbitando = null;
                CuerpoCeleste anfitrion = null;

                if (ci.estaOrbitando(cj)) {
                    orbitando = ci;
                    anfitrion = cj;
                } else if (cj.estaOrbitando(ci)) {
                    orbitando = cj;
                    anfitrion = ci;
                } else {
                    // Caso A: Luna y Planeta (captura automática si entra en el área de la luna)
                    Luna luna = (ci instanceof Luna) ? (Luna) ci : ((cj instanceof Luna) ? (Luna) cj : null);
                    CuerpoCeleste otroLuna = (luna == ci) ? cj : ci;
                    boolean esLunaYPlaneta = (luna != null && otroLuna != null && otroLuna.getTipoCuerpo() != null &&
                            (otroLuna.getTipoCuerpo() == TipoCuerpo.PLANETA_ROCOSO ||
                             otroLuna.getTipoCuerpo() == TipoCuerpo.PLANETA_GASEOSO ||
                             otroLuna.getTipoCuerpo() == TipoCuerpo.PLANETA_HELADO));

                    if (esLunaYPlaneta) {
                        double radioCapturaLuna = luna.getRadioAtraccion();
                        if (distReal <= radioCapturaLuna && !luna.estaOrbitando()) {
                            luna.setCuerpoOrbitado(otroLuna);
                            luna.setRadioOrbita(distReal);
                            if (listenerMensaje != null) {
                                listenerMensaje.accept(luna.getNombre() + " esta orbitando " + otroLuna.getNombre());
                            }
                            inicializarVelocidadOrbital(luna, otroLuna, distReal);
                            orbitando = luna;
                            anfitrion = otroLuna;
                        }
                    }

                    // Caso B: Planeta y Estrella / Agujero Negro (captura automática en órbitas de la estrella)
                    Planeta planeta = (ci instanceof Planeta) ? (Planeta) ci : ((cj instanceof Planeta) ? (Planeta) cj : null);
                    CuerpoCeleste estrella = (planeta == ci) ? cj : ci;
                    boolean esPlanetaYEstrella = (planeta != null && estrella != null && estrella.getTipoCuerpo() != null &&
                            (estrella.getTipoCuerpo() == TipoCuerpo.ESTRELLA ||
                             estrella.getTipoCuerpo() == TipoCuerpo.AGUJERO_NEGRO ||
                             estrella.getTipoCuerpo() == TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO));

                    if (esPlanetaYEstrella && orbitando == null) {
                        double radioCapturaPlaneta = 600.0;
                        double radioMinimoCaptura = estrella.getRadio() + planeta.getRadio() + 5.0;

                        if (!planeta.estaOrbitando() && distReal > radioMinimoCaptura && distReal <= radioCapturaPlaneta) {
                            planeta.setCuerpoOrbitado(estrella);
                            planeta.setRadioOrbita(distReal);
                            if (listenerMensaje != null) {
                                listenerMensaje.accept(planeta.getNombre() + " esta orbitando " + estrella.getNombre());
                            }
                            inicializarVelocidadOrbital(planeta, estrella, distReal);
                            orbitando = planeta;
                            anfitrion = estrella;
                        }
                    }
                }

                // 2. Si están orbitando, aplicar dinámica orbital activa estabilizada y omitir colapso
                if (orbitando != null && anfitrion != null) {
                    aplicarDinamicaOrbitalEstable(orbitando, anfitrion, distReal);
                    continue;
                }

                // 3. Gravedad newtoniana base para cuerpos no enlazados en órbita
                double fuerzaMag = G * ci.getMasa() * cj.getMasa() / r2;

                Vector2D dir = r.dividir(rMag);
                Vector2D fuerzaSobreI = dir.multiplicar(fuerzaMag);
                Vector2D fuerzaSobreJ = fuerzaSobreI.multiplicar(-1.0);

                ci.aplicarFuerza(fuerzaSobreI);
                cj.aplicarFuerza(fuerzaSobreJ);

                // 4. Atracción arcade hacia el colapso para cuerpos no orbitantes dentro del área de atracción
                CuerpoCeleste mayor = ci.getMasa() >= cj.getMasa() ? ci : cj;
                CuerpoCeleste menor = (mayor == ci) ? cj : ci;

                double radioColapso = mayor.getRadioAtraccion();
                if (distReal < radioColapso) {
                    double penetracion = Math.max(0.0, Math.min(1.0, 1.0 - (distReal / radioColapso)));
                    Vector2D dirHaciaMayor = mayor.getPosicion().restar(menor.getPosicion()).normalizar();

                    double factorMasaMayor = 1.0;
                    if (mayor.getTipoCuerpo() != null && mayor.getTipoCuerpo().masaBase > 0) {
                        factorMasaMayor = Math.max(0.1, mayor.getMasa() / mayor.getTipoCuerpo().masaBase);
                    }

                    double aMenor;
                    if (mayor.getTipoCuerpo() == TipoCuerpo.ESTRELLA || mayor.getTipoCuerpo() == TipoCuerpo.AGUJERO_NEGRO || mayor.getTipoCuerpo() == TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO) {
                        aMenor = (0.5 + 3.0 * penetracion * penetracion) * Math.sqrt(factorMasaMayor);
                    } else {
                        aMenor = (0.35 + 2.0 * penetracion * penetracion) * Math.sqrt(factorMasaMayor);
                    }

                    double fuerzaColapsoMag = menor.getMasa() * aMenor;
                    Vector2D fSobreMenor = dirHaciaMayor.multiplicar(fuerzaColapsoMag);
                    Vector2D fSobreMayor = fSobreMenor.multiplicar(-1.0);

                    menor.aplicarFuerza(fSobreMenor);
                    mayor.aplicarFuerza(fSobreMayor);
                }
            }
        }
    }

    private double calcularVelocidadOrbitalIdeal(CuerpoCeleste orbitando, CuerpoCeleste anfitrion, double rOrb) {
        double d = Math.max(10.0, rOrb);
        if (orbitando instanceof Luna && !(anfitrion.getTipoCuerpo() == TipoCuerpo.ESTRELLA ||
                anfitrion.getTipoCuerpo() == TipoCuerpo.AGUJERO_NEGRO ||
                anfitrion.getTipoCuerpo() == TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO)) {
            double factorMasa = (anfitrion.getTipoCuerpo() != null && anfitrion.getTipoCuerpo().masaBase > 0)
                    ? Math.max(0.2, anfitrion.getMasa() / anfitrion.getTipoCuerpo().masaBase)
                    : 1.0;
            // Órbita de la luna calibrada para velocidad más suave y disfrutable
            return Math.sqrt(75.0 * factorMasa / d);
        }
        return Math.sqrt(G * anfitrion.getMasa() / d);
    }

    private void inicializarVelocidadOrbital(CuerpoCeleste orbitando, CuerpoCeleste anfitrion, double distReal) {
        Vector2D r = orbitando.getPosicion().restar(anfitrion.getPosicion());
        double d = r.magnitud();
        double minOrb = anfitrion.getRadio() + orbitando.getRadio() + 5.0;
        if (d < minOrb) {
            d = minOrb;
            Vector2D norm = (r.magnitud() > 0.001) ? r.normalizar() : new Vector2D(1, 0);
            orbitando.setPosicion(anfitrion.getPosicion().sumar(norm.multiplicar(d)));
            r = norm.multiplicar(d);
        }

        orbitando.setRadioOrbita(d);

        Vector2D uRadial = r.dividir(Math.max(0.1, d));
        Vector2D vRel = orbitando.getVelocidad().restar(anfitrion.getVelocidad());
        double cross = r.x * vRel.y - r.y * vRel.x;
        boolean horario = cross < -1e-4;
        Vector2D uTangencial = horario
                ? new Vector2D(uRadial.y, -uRadial.x)
                : new Vector2D(-uRadial.y, uRadial.x);

        double vOrb = calcularVelocidadOrbitalIdeal(orbitando, anfitrion, d);
        Vector2D vFinal = anfitrion.getVelocidad().sumar(uTangencial.multiplicar(vOrb));
        orbitando.setVelocidad(vFinal);
    }

    private void aplicarDinamicaOrbitalEstable(CuerpoCeleste orbitando, CuerpoCeleste anfitrion, double distReal) {
        // Desenganche si se aleja demasiado por colisión o perturbación externa extrema
        double limiteEscape;
        if (orbitando instanceof Luna && !(anfitrion.getTipoCuerpo() == TipoCuerpo.ESTRELLA ||
                anfitrion.getTipoCuerpo() == TipoCuerpo.AGUJERO_NEGRO ||
                anfitrion.getTipoCuerpo() == TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO)) {
            limiteEscape = orbitando.getRadioAtraccion() * 3.5;
        } else {
            limiteEscape = 1500.0;
        }

        if (distReal > limiteEscape) {
            orbitando.setCuerpoOrbitado(null);
            return;
        }

        Vector2D r = orbitando.getPosicion().restar(anfitrion.getPosicion());
        double d = Math.max(0.1, r.magnitud());
        Vector2D uRadial = r.dividir(d);

        double rDeseado = orbitando.getRadioOrbita();
        if (rDeseado <= 0) {
            rDeseado = d;
            orbitando.setRadioOrbita(rDeseado);
        }

        // Proteger contra radio menor al contacto físico
        double minOrb = anfitrion.getRadio() + orbitando.getRadio() + 5.0;
        if (rDeseado < minOrb) {
            rDeseado = minOrb;
            orbitando.setRadioOrbita(rDeseado);
        }

        // Suave corrección geométrica para evitar acumulación de error por integración de Euler
        if (Math.abs(d - rDeseado) > 0.01) {
            double dAjustado = d + (rDeseado - d) * 0.15;
            orbitando.setPosicion(anfitrion.getPosicion().sumar(uRadial.multiplicar(dAjustado)));
            d = dAjustado;
        }

        Vector2D vRel = orbitando.getVelocidad().restar(anfitrion.getVelocidad());
        double cross = r.x * vRel.y - r.y * vRel.x;
        boolean horario = cross < -1e-4;
        Vector2D uTangencial = horario
                ? new Vector2D(uRadial.y, -uRadial.x)
                : new Vector2D(-uRadial.y, uRadial.x);

        double vOrbIdeal = calcularVelocidadOrbitalIdeal(orbitando, anfitrion, rDeseado);

        // Regularizar suavemente la velocidad tangencial al valor ideal de la órbita
        Vector2D vDeseada = anfitrion.getVelocidad().sumar(uTangencial.multiplicar(vOrbIdeal));
        orbitando.setVelocidad(orbitando.getVelocidad().sumar(vDeseada.restar(orbitando.getVelocidad()).multiplicar(0.15)));

        // Fuerza centrípeta física hacia el centro
        double aCentripeta = (vOrbIdeal * vOrbIdeal) / Math.max(1.0, d);
        Vector2D fCentripeta = uRadial.multiplicar(-1.0 * orbitando.getMasa() * aCentripeta);
        orbitando.aplicarFuerza(fCentripeta);

        // Compensación por aceleración inercial del anfitrión (ej. Luna acompañando planeta en movimiento)
        if (anfitrion.getMasa() > 0) {
            Vector2D aAnfitrion = anfitrion.getFuerza().dividir(anfitrion.getMasa());
            orbitando.aplicarFuerza(aAnfitrion.multiplicar(orbitando.getMasa()));
        }
    }

    /**
     * Asistencia Orbital Permisiva:
     * Si un planeta/luna/satélite está cerca de un cuerpo primario masivo (estrella o gigante),
     * aplica una sutil aceleración tangencial para suavizar la excentricidad excesiva y evitar
     * que órbitas casuales colapsen de inmediato en caos.
     * La fuerza de corrección se atenúa logarítmicamente según la masa del cuerpo (menor asistencia a mayor masa),
     * cumpliendo el diseño de juego donde cuerpos pesados resultan más caóticos y desafiantes de controlar.
     */
    private void aplicarAsistenciaOrbital(List<CuerpoCeleste> cuerpos) {
        if (asistenciaOrbital <= 0) return;

        for (CuerpoCeleste c : cuerpos) {
            // Solo asistir a cuerpos no anclas (planetas, lunas, satélites)
            if (c.getTipoCuerpo().esMasivo && c.getMasa() >= TipoCuerpo.ESTRELLA.masaBase * 0.5) {
                continue;
            }
            if (c.estaOrbitando()) {
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

                        // Asistencia efectiva dependiente de la masa relativa
                        double asistenciaEfectiva = calcularAsistenciaEfectiva(c.getMasa());

                        // Aplicar una fuerza de ajuste sutil
                        Vector2D correccion = vObjetivo.restar(vRel).multiplicar(c.getMasa() * asistenciaEfectiva);
                        c.aplicarFuerza(correccion);
                    }
                }
            }
        }
    }

    /**
     * Calcula la asistencia orbital efectiva con atenuación logarítmica proporcional a la masa del cuerpo.
     * Con masa = PLANETA_ROCOSO.masaBase (1.0x), asistenciaEfectiva == asistenciaOrbital.
     * Con masa mayor, la asistencia disminuye, exigiendo mayor precisión al jugador.
     *
     * @param masa Masa en kg del cuerpo a estabilizar.
     * @return Multiplicador de corrección efectivo.
     */
    public double calcularAsistenciaEfectiva(double masa) {
        double factorMasaCuerpo = masa / TipoCuerpo.PLANETA_ROCOSO.masaBase;
        return asistenciaOrbital / (1.0 + Math.log10(Math.max(1.0, factorMasaCuerpo)));
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
            // Protección contra masa <= 0 para prevenir división por cero o NaN
            double masaValida = Math.max(1e-3, c.getMasa());
            Vector2D aceleracion = c.getFuerza().dividir(masaValida);

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
        List<CuerpoCeleste> aAgregar = new ArrayList<>();

        for (int i = 0; i < simulados.size(); i++) {
            CuerpoCeleste a = simulados.get(i);
            if (aEliminar.contains(a)) continue;

            for (int j = i + 1; j < simulados.size(); j++) {
                CuerpoCeleste b = simulados.get(j);
                if (aEliminar.contains(b)) continue;

                // 1. Interacción con Cúpula de Escudo
                if (a instanceof EscudoProtector && b instanceof Meteorito) {
                    interactuarEscudoMeteorito((EscudoProtector) a, (Meteorito) b, aEliminar);
                    continue;
                }
                if (b instanceof EscudoProtector && a instanceof Meteorito) {
                    interactuarEscudoMeteorito((EscudoProtector) b, (Meteorito) a, aEliminar);
                    continue;
                }
                // Escudo protector no interactúa de forma destructiva con cuerpos celestes normales (no meteorito, no agujero negro)
                if ((a instanceof EscudoProtector || b instanceof EscudoProtector) &&
                        a.getTipoCuerpo() != TipoCuerpo.AGUJERO_NEGRO && a.getTipoCuerpo() != TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO &&
                        b.getTipoCuerpo() != TipoCuerpo.AGUJERO_NEGRO && b.getTipoCuerpo() != TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO) {
                    continue;
                }

                double dist = a.getPosicion().distanciaA(b.getPosicion());
                double radioContacto = a.getRadio() + b.getRadio();

                // 2. Colisión física
                if (dist < radioContacto) {
                    if (a.estaOrbitando(b)) {
                        double minOrb = b.getRadio() + a.getRadio() + 4.0;
                        Vector2D norm = dist > 0.001 ? a.getPosicion().restar(b.getPosicion()).normalizar() : new Vector2D(1, 0);
                        a.setPosicion(b.getPosicion().sumar(norm.multiplicar(minOrb)));
                        continue;
                    }
                    if (b.estaOrbitando(a)) {
                        double minOrb = a.getRadio() + b.getRadio() + 4.0;
                        Vector2D norm = dist > 0.001 ? b.getPosicion().restar(a.getPosicion()).normalizar() : new Vector2D(1, 0);
                        b.setPosicion(a.getPosicion().sumar(norm.multiplicar(minOrb)));
                        continue;
                    }

                    procesarColision(a, b, aEliminar, aAgregar);
                }
            }
        }

        todos.removeAll(aEliminar);
        todos.addAll(aAgregar);

        // Limpiar referencias a cuerpos eliminados que estaban siendo orbitados
        for (CuerpoCeleste c : todos) {
            if (c.getCuerpoOrbitado() != null && (aEliminar.contains(c.getCuerpoOrbitado()) || !todos.contains(c.getCuerpoOrbitado()))) {
                c.setCuerpoOrbitado(null);
            }
        }

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
                if (listenerDefensaEscudo != null) {
                    try { listenerDefensaEscudo.run(); } catch (Exception ignored) {}
                }
                if (listenerMensaje != null) {
                    listenerMensaje.accept("Escudo defensivo desvio y vaporizo meteorito.");
                } else {
                    System.out.println("Escudo defensivo desvio y vaporizo meteorito.");
                }
            }
        }
    }

    private void procesarColision(CuerpoCeleste a, CuerpoCeleste b, List<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar) {
        TipoCuerpo tipoA = a.getTipoCuerpo();
        TipoCuerpo tipoB = b.getTipoCuerpo();

        // 1. Agujero negro devora todo (el mayor devora al menor si ambos son agujeros negros)
        boolean aEsBH = (tipoA == TipoCuerpo.AGUJERO_NEGRO || tipoA == TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO);
        boolean bEsBH = (tipoB == TipoCuerpo.AGUJERO_NEGRO || tipoB == TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO);

        if (aEsBH && bEsBH) {
            CuerpoCeleste mayor = a.getMasa() >= b.getMasa() ? a : b;
            CuerpoCeleste menor = (mayor == a) ? b : a;
            absorber(mayor, menor, aEliminar);
            return;
        }
        if (aEsBH) {
            absorber(a, b, aEliminar);
            return;
        }
        if (bEsBH) {
            absorber(b, a, aEliminar);
            return;
        }

        // 2. Estrella devora meteoritos o satélites (caso especial)
        if (tipoA == TipoCuerpo.ESTRELLA && (b instanceof Meteorito || b instanceof Satelite)) {
            aEliminar.add(b);
            if (listenerMensaje != null) {
                listenerMensaje.accept(a.getNombre() + " desintegró a " + b.getNombre() + " en su corona.");
            }
            return;
        }
        if (tipoB == TipoCuerpo.ESTRELLA && (a instanceof Meteorito || a instanceof Satelite)) {
            aEliminar.add(a);
            if (listenerMensaje != null) {
                listenerMensaje.accept(b.getNombre() + " desintegró a " + a.getNombre() + " en su corona.");
            }
            return;
        }

        // 3. Dos meteoritos colisionando entre sí: destrucción mutua simple, sin fragmentación infinita
        if (a instanceof Meteorito && b instanceof Meteorito) {
            aEliminar.add(a);
            aEliminar.add(b);
            if (listenerMensaje != null) {
                listenerMensaje.accept("Destrucción mutua de meteoritos por impacto directo.");
            }
            return;
        }

        // 4. Meteorito impacta un cuerpo no-meteorito: comportamiento de impacto y momento
        if (a instanceof Meteorito && !(b instanceof Meteorito)) {
            impactarMeteorito((Meteorito) a, b, aEliminar);
            return;
        }
        if (b instanceof Meteorito && !(a instanceof Meteorito)) {
            impactarMeteorito((Meteorito) b, a, aEliminar);
            return;
        }

        // Caso de protección: EscudoProtector no participa en colisiones planetarias
        if (a instanceof EscudoProtector || b instanceof EscudoProtector) {
            return;
        }

        // A partir de acá, ambos son cuerpos no-meteoritos
        double masaA = Math.max(1e-9, a.getMasa());
        double masaB = Math.max(1e-9, b.getMasa());
        double masaMayor = Math.max(masaA, masaB);
        double masaMenor = Math.min(masaA, masaB);
        double ratioMasa = masaMayor / masaMenor;
        CuerpoCeleste mayor = a.getMasa() >= b.getMasa() ? a : b;
        CuerpoCeleste menor = (mayor == a) ? b : a;

        // 5. Proporción de masa extrema (>= RATIO_ABSORCION_MASA): absorción total
        if (ratioMasa >= ConfiguracionSimulacion.RATIO_ABSORCION_MASA) {
            absorber(mayor, menor, aEliminar);
            if (listenerMensaje != null) {
                listenerMensaje.accept(mayor.getNombre() + " absorbió por gravedad a " + menor.getNombre() + ".");
            }
            return;
        }

        // 6. Masas aproximadamente iguales (<= RATIO_COLISION_COLAPSO): colapso mutuo en fragmentos
        if (ratioMasa <= ConfiguracionSimulacion.RATIO_COLISION_COLAPSO) {
            generarEscombros(a, aEliminar, aAgregar);
            generarEscombros(b, aEliminar, aAgregar);
            if (listenerMensaje != null) {
                listenerMensaje.accept("Colisión catastrófica mutua: " + a.getNombre() + " y " + b.getNombre() + " colapsaron en escombros.");
            }
            return;
        }

        // 7. Cualquier otro caso (masas distintas pero no extremas): colisión destructiva asimétrica
        // El menor se destruye por completo generando escombros proporcionales a su masa
        // El mayor sobrevive perdiendo la masa equivalente del menor
        generarEscombros(menor, aEliminar, aAgregar);
        double masaBaseMayor = mayor.getTipoCuerpo() != null ? mayor.getTipoCuerpo().masaBase : 1.0;
        double pisoMinimo = Math.max(1.0, masaBaseMayor * ConfiguracionSimulacion.FRACCION_MASA_MINIMA_SOBREVIVIENTE);
        double nuevaMasaMayor = Math.max(pisoMinimo, mayor.getMasa() - menor.getMasa());
        mayor.setMasa(nuevaMasaMayor);

        if (mayor instanceof Planeta) {
            Planeta p = (Planeta) mayor;
            if (p.tieneCivilizacion()) {
                p.getCivilizacion().recibirImpactoMeteorito();
            }
        }

        if (listenerMensaje != null) {
            listenerMensaje.accept("Impacto destructivo asimétrico: " + menor.getNombre() + " destruido al colisionar con " + mayor.getNombre() + ".");
        }
    }

    private void generarEscombros(CuerpoCeleste destruido, List<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar) {
        if (!aEliminar.contains(destruido)) {
            aEliminar.add(destruido);
        }

        // No generar escombros a partir de meteoritos (evita fragmentación infinita)
        if (destruido instanceof Meteorito) {
            return;
        }

        double masaTotalFragmentos = destruido.getMasa() * (1.0 - ConfiguracionSimulacion.FRACCION_MASA_DISIPADA);
        if (masaTotalFragmentos <= 0) {
            return;
        }

        int numFragmentos = ConfiguracionSimulacion.FRAGMENTOS_POR_COLAPSO;
        // Control de límite para evitar degradación de rendimiento por O(n²)
        if (aAgregar.size() >= ConfiguracionSimulacion.MAX_METEORITOS_SIMULTANEOS) {
            numFragmentos = 1;
        }

        double masaPorFragmento = masaTotalFragmentos / numFragmentos;

        for (int i = 0; i < numFragmentos; i++) {
            Meteorito fragmento = CuerpoCelesteFactory.crearFragmentoEscombro(
                    destruido.getPosicionX(), destruido.getPosicionY(),
                    destruido.getVelocidad(), masaPorFragmento
            );
            aAgregar.add(fragmento);
        }
    }

    private void absorber(CuerpoCeleste absorbente, CuerpoCeleste victima, List<CuerpoCeleste> aEliminar) {
        double masaAnterior = absorbente.getMasa();
        double nuevaMasa = masaAnterior + victima.getMasa();
        absorbente.setMasa(nuevaMasa);

        // Conservar momento lineal
        Vector2D momento = absorbente.getVelocidad().multiplicar(masaAnterior)
                .sumar(victima.getVelocidad().multiplicar(victima.getMasa()));
        absorbente.setVelocidad(momento.dividir(nuevaMasa));

        if (!aEliminar.contains(victima)) {
            aEliminar.add(victima);
        }
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

    public void setListenerMensaje(java.util.function.Consumer<String> listener) {
        this.listenerMensaje = listener;
    }

    public void setListenerDefensaEscudo(Runnable listener) {
        this.listenerDefensaEscudo = listener;
    }

    public double getAsistenciaOrbital() {
        return asistenciaOrbital;
    }

    public void setAsistenciaOrbital(double asistencia) {
        this.asistenciaOrbital = Math.max(0, Math.min(0.1, asistencia));
    }
}
