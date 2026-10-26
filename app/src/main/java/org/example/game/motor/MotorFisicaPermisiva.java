package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.simulacion.MensajeEvento;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Motor de física newtoniana asistida / permisiva para un gameplay amigable y satisfactorio.
 *
 * Principios SOLID:
 * - SRP: Responsable del cálculo de fuerzas gravitacionales, estabilidad orbital permisiva y colisiones.
 * - DIP: Implementa MotorFisica; colaboradores inyectados por constructor con valores por defecto.
 * - OCP: Nuevos tipos de interacción y comportamientos de cuerpos se manejan mediante polimorfismo.
 */
public class MotorFisicaPermisiva extends MotorFisicaBase {

    private final double G;
    private final double softening;
    private final double dt;
    private double asistenciaOrbital = 0.015; // 1.5% de corrección sutil hacia estabilidad por tick
    private double velocidadMaxima = 1200.0;
    private Consumer<String> listenerMensaje;

    // Listas internas reutilizables para optimización de memoria y GC por tick
    private final List<CuerpoCeleste> simuladosCache = new ArrayList<>();
    private final List<CuerpoCeleste> aEliminarCache = new ArrayList<>();
    private final List<CuerpoCeleste> aAgregarCache = new ArrayList<>();

    // Componentes delegados (SRP / DIP)
    private final CalculadorGravedad calculadorGravedad;
    private final AsistenteOrbital asistenteOrbital;
    private final ResolvedorColisiones resolvedorColisiones;
    private final GestorAgujeroNegro gestorAgujeroNegro;

    public MotorFisicaPermisiva(double radioMundo, CalculadorGravedad calculadorGravedad,
                                AsistenteOrbital asistenteOrbital, ResolvedorColisiones resolvedorColisiones,
                                GestorAgujeroNegro gestorAgujeroNegro) {
        super(radioMundo);
        this.G = ConstantesFisicas.G_ESCALADO;
        this.softening = 35.0;
        this.dt = 1.0;
        this.calculadorGravedad = calculadorGravedad != null ? calculadorGravedad : new CalculadorGravedad(G, softening);
        this.asistenteOrbital = asistenteOrbital != null ? asistenteOrbital : new AsistenteOrbital(G);
        this.resolvedorColisiones = resolvedorColisiones != null ? resolvedorColisiones : new ResolvedorColisiones();
        this.gestorAgujeroNegro = gestorAgujeroNegro != null ? gestorAgujeroNegro : new GestorAgujeroNegro();
    }

    public MotorFisicaPermisiva(double radioMundo, CalculadorGravedad calculadorGravedad,
                                AsistenteOrbital asistenteOrbital, ResolvedorColisiones resolvedorColisiones) {
        this(radioMundo, calculadorGravedad, asistenteOrbital, resolvedorColisiones, null);
    }

    public MotorFisicaPermisiva(double radioMundo) {
        this(radioMundo, null, null, null, null);
    }

    public MotorFisicaPermisiva() {
        this(ConstantesFisicas.RADIO_MUNDO_DEFAULT);
    }

    @Override
    public void avanzarPaso(List<CuerpoCeleste> cuerpos) {
        simuladosCache.clear();
        for (int i = 0; i < cuerpos.size(); i++) {
            CuerpoCeleste c = cuerpos.get(i);
            if (c != null && c.esSimulado()) {
                simuladosCache.add(c);
            }
        }

        if (simuladosCache.isEmpty()) return;

        // 1. Resetear fuerzas
        for (int i = 0; i < simuladosCache.size(); i++) {
            simuladosCache.get(i).resetearFuerza();
        }

        // 2. Calcular Gravedad N-Body con Softening Permisivo
        calcularGravedadNBody(simuladosCache);

        // 3. Aplicar Asistencia de Estabilidad Orbital Permisiva
        aplicarAsistenciaOrbital(simuladosCache);

        // 4. Integrar movimiento (Velocity Verlet / Symplectic Euler mejorado)
        integrarMovimiento(simuladosCache);

        // 5. Procesar entidades especiales (satélites, etc.)
        actualizarEntidadesEspeciales(simuladosCache);

        // 6. Atracción extrema y absorción en el horizonte de sucesos de agujeros negros
        aEliminarCache.clear();
        gestorAgujeroNegro.procesar(simuladosCache, aEliminarCache, listenerMensaje);
        if (!aEliminarCache.isEmpty()) {
            cuerpos.removeAll(aEliminarCache);
            for (CuerpoCeleste c : aEliminarCache) {
                notificarColision(c);
            }
        }

        // 7. Manejo de colisiones, absorciones y rebotes
        manejarColisiones(cuerpos, simuladosCache);

        // 8. Limpiar expirados o fuera del mundo
        limpiarCuerposExpirados(cuerpos);
    }

    private void calcularGravedadNBody(List<CuerpoCeleste> cuerpos) {
        calculadorGravedad.calcularGravedad(cuerpos, asistenteOrbital, listenerMensaje);
    }

    private void aplicarAsistenciaOrbital(List<CuerpoCeleste> cuerpos) {
        asistenteOrbital.aplicarAsistenciaOrbital(cuerpos, asistenciaOrbital);
    }

    public double calcularAsistenciaEfectiva(double masa) {
        return asistenteOrbital.calcularAsistenciaEfectiva(masa, asistenciaOrbital);
    }

    private void integrarMovimiento(List<CuerpoCeleste> cuerpos) {
        for (CuerpoCeleste c : cuerpos) {
            double masaValida = Math.max(1e-3, c.getMasa());
            Vector2D aceleracion = c.getFuerza().dividir(masaValida);

            double aMag = aceleracion.magnitud();
            if (aMag > 200.0) {
                aceleracion = aceleracion.normalizar().multiplicar(200.0);
            }

            Vector2D nuevaVel = c.getVelocidad().sumar(aceleracion.multiplicar(dt));

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
            c.actualizarEntidad(dt);
        }
    }

    private void manejarColisiones(List<CuerpoCeleste> todos, List<CuerpoCeleste> simulados) {
        aEliminarCache.clear();
        aAgregarCache.clear();
        List<CuerpoCeleste> aEliminar = aEliminarCache;
        List<CuerpoCeleste> aAgregar = aAgregarCache;

        for (int i = 0; i < simulados.size(); i++) {
            CuerpoCeleste a = simulados.get(i);
            if (aEliminar.contains(a)) continue;

            for (int j = i + 1; j < simulados.size(); j++) {
                CuerpoCeleste b = simulados.get(j);
                if (aEliminar.contains(b)) continue;

                double dist = a.getPosicion().distanciaA(b.getPosicion());
                double radioContacto = a.getRadio() + b.getRadio();

                // Colisión física o contacto
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

                    resolvedorColisiones.resolverColision(a, b, aEliminar, aAgregar);
                }
            }
        }

        todos.removeAll(aEliminar);
        todos.addAll(aAgregar);

        // Limpiar referencias a cuerpos eliminados que estaban siendo orbitados
        // Satélites orbitando cuerpos destruidos desaparecen con ellos
        List<CuerpoCeleste> satelitesAEliminar = new ArrayList<>();
        for (CuerpoCeleste c : todos) {
            if (c.getCuerpoOrbitado() != null && (aEliminar.contains(c.getCuerpoOrbitado()) || !todos.contains(c.getCuerpoOrbitado()))) {
                if (c.getTipoCuerpo() == TipoCuerpo.SATELITE) {
                    satelitesAEliminar.add(c);
                    c.setSimulado(false);
                    c.setMasa(0);
                }
                c.setCuerpoOrbitado(null);
            }
        }
        if (!satelitesAEliminar.isEmpty()) {
            todos.removeAll(satelitesAEliminar);
            for (CuerpoCeleste s : satelitesAEliminar) {
                notificarColision(s);
            }
        }

        for (CuerpoCeleste c : aEliminar) {
            notificarColision(c);
        }
    }

    public void setListenerMensaje(Consumer<String> listener) {
        this.listenerMensaje = listener;
        this.resolvedorColisiones.setListenerMensaje(listener);
    }

    public void setListenerNotificacion(BiConsumer<String, MensajeEvento.TipoMensaje> listener) {
        this.resolvedorColisiones.setListenerNotificacion(listener);
    }

    public void setListenerExplosion(BiConsumer<Vector2D, Double> listener) {
        this.resolvedorColisiones.setListenerExplosion(listener);
    }

    public ResolvedorColisiones getResolvedorColisiones() {
        return resolvedorColisiones;
    }

    public GestorAgujeroNegro getGestorAgujeroNegro() {
        return gestorAgujeroNegro;
    }

    public double getAsistenciaOrbital() {
        return asistenciaOrbital;
    }

    public void setAsistenciaOrbital(double asistencia) {
        this.asistenciaOrbital = Math.max(0, Math.min(0.1, asistencia));
    }
}
