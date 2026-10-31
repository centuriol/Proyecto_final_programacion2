package org.example.game.civilizacion;
import org.example.game.cuerpo.Planeta;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.MensajeEvento;
import org.example.game.motor.Vector2D;
import org.example.game.cuerpo.CuerpoCeleste;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
/**
 * Componente de civilización para planetas.
 * Gestiona población, estabilidad, nivel tecnológico y eventos.
 */
public class CivilizacionComponent {
    private final Planeta planeta;
    private double poblacion;
    private double poblacionMaxima;
    private int nivelTecnologico; // 0-10
    private double estabilidad; // 0.0 - 1.0
    private double felicidad; // 0.0 - 1.0
    private EstadoCivilizacion estado;
    private int ticksEfectoEventoReciente = 0;
    private Consumer<MensajeEvento> emisorEventos;

    // Historial para gráficas
    private final List<Double> historialPoblacion = new ArrayList<>();
    private final List<Double> historialEstabilidad = new ArrayList<>();
    private int contadorTicksHistorial = 0;
    private final EvaluadorHabitabilidad evaluadorHabitabilidad;

    public CivilizacionComponent(Planeta planeta) {
        this(planeta, ConfiguracionSimulacion.POBLACION_INICIAL, new EvaluadorHabitabilidad());
    }

    public CivilizacionComponent(Planeta planeta, double poblacionInicial) {
        this(planeta, poblacionInicial, new EvaluadorHabitabilidad());
    }

    public CivilizacionComponent(Planeta planeta, double poblacionInicial, EvaluadorHabitabilidad evaluadorHabitabilidad) {
        this.planeta = planeta;
        this.nivelTecnologico = 0;
        this.estabilidad = 1.0;
        this.felicidad = 1.0;
        this.estado = EstadoCivilizacion.PRÓSPERA;
        this.evaluadorHabitabilidad = evaluadorHabitabilidad != null ? evaluadorHabitabilidad : new EvaluadorHabitabilidad();
        recalcularPoblacionMaxima();
        this.poblacion = Math.min(poblacionInicial, this.poblacionMaxima);
    }

    /** Actualiza la civilización cada tick */
    public void actualizar(List<CuerpoCeleste> todosLosCuerpos) {
        if (estado == EstadoCivilizacion.EXTINGUIDA) return;

        if (ticksEfectoEventoReciente > 0) {
            ticksEfectoEventoReciente--;
        }

        CondicionesHabitabilidad condiciones = evaluadorHabitabilidad.evaluar(planeta, todosLosCuerpos);
        actualizarPoblacion(condiciones);
        actualizarEstabilidadYFelicidad(condiciones);
        estado = determinarEstado();
        procesarEventosAleatorios();
        registrarMuestraHistorial();

        if (poblacion < 10) {
            extinguir();
        }
    }

    private void actualizarPoblacion(CondicionesHabitabilidad condiciones) {
        double factorCrecimiento = calcularFactorCrecimiento(condiciones);
        if (factorCrecimiento > 0) {
            double saturacion = poblacionMaxima > 0 ? (1.0 - (poblacion / poblacionMaxima)) : 0.0;
            poblacion *= (1.0 + factorCrecimiento * Math.max(0.0, saturacion));
        } else {
            poblacion *= (1.0 + factorCrecimiento);
        }
        poblacion = Math.max(0, Math.min(poblacion, poblacionMaxima));
    }

    private void registrarMuestraHistorial() {
        contadorTicksHistorial++;
        if (contadorTicksHistorial % 10 == 0) {
            historialPoblacion.add(poblacion);
            historialEstabilidad.add(estabilidad);
            if (historialPoblacion.size() > ConfiguracionSimulacion.MAX_TICKS_HISTORIAL / 10) {
                historialPoblacion.remove(0);
                historialEstabilidad.remove(0);
            }
        }
    }

    private double calcularFactorCrecimiento(CondicionesHabitabilidad c) {
        double factor = 0;

        if (c.enZonaHabitable()) {
            // Crecimiento óptimo en centro de zona habitable
            double centroZona = (ConfiguracionSimulacion.DISTANCIA_ZONA_HABITABLE_MIN
                    + ConfiguracionSimulacion.DISTANCIA_ZONA_HABITABLE_MAX) / 2;
            double desviacion = Math.abs(c.ratioZonaHabitable() - centroZona);
            double maxDesviacion = (ConfiguracionSimulacion.DISTANCIA_ZONA_HABITABLE_MAX
                    - ConfiguracionSimulacion.DISTANCIA_ZONA_HABITABLE_MIN) / 2;
            double bonusZona = 1.0 - (desviacion / maxDesviacion);
            factor += ConfiguracionSimulacion.CRECIMIENTO_POBLACION_BASE * (0.5 + bonusZona * 0.5);
        } else {
            // Decadencia fuera de zona
            factor -= ConfiguracionSimulacion.DECADENCIA_FUERA_ZONA;
        }

        // Penalización por agujero negro
        if (c.bajoAmenazaAgujero()) {
            factor -= c.intensidadAmenaza() * 0.01;
        }

        // Penalización por inestabilidad orbital
        factor -= c.excentricidadOrbital() * 0.005;

        // Bonus por tecnología (futuro: tech tree)
        factor += nivelTecnologico * 0.0001;

        return factor;
    }

    private void actualizarEstabilidadYFelicidad(CondicionesHabitabilidad c) {
        // Estabilidad: afectada por amenazas y excentricidad
        double objetivoEstabilidad = 1.0;
        if (c.bajoAmenazaAgujero()) objetivoEstabilidad -= c.intensidadAmenaza() * 0.5;
        objetivoEstabilidad -= c.excentricidadOrbital() * 0.3;
        if (!c.enZonaHabitable()) objetivoEstabilidad -= 0.2;

        // Suavizar cambios
        estabilidad = estabilidad * 0.99 + objetivoEstabilidad * 0.01;
        estabilidad = Math.max(0, Math.min(1, estabilidad));

        // Felicidad: correlaciona con estabilidad y crecimiento
        double ratioPob = poblacionMaxima > 0 ? (poblacion / poblacionMaxima) : 0.0;
        double objetivoFelicidad = estabilidad * 0.7 + ratioPob * 0.3;
        felicidad = felicidad * 0.98 + objetivoFelicidad * 0.02;
        felicidad = Math.max(0, Math.min(1, felicidad));
    }

    private EstadoCivilizacion determinarEstado() {
        if (poblacion < 10) return EstadoCivilizacion.EXTINGUIDA;
        if (estabilidad < 0.3) return EstadoCivilizacion.EN_PELIGRO;
        if (estabilidad < 0.6 || felicidad < 0.4) return EstadoCivilizacion.LUCHANDO;
        if (estabilidad > 0.8 && felicidad > 0.7) return EstadoCivilizacion.PRÓSPERA;
        return EstadoCivilizacion.ESTABLE;
    }

    private void procesarEventosAleatorios() {
        double rand = Math.random();

        // Eventos positivos (más probables si próspera)
        if (estado == EstadoCivilizacion.PRÓSPERA && rand < 0.001) {
            // Avance tecnológico
            if (nivelTecnologico < 10) {
                nivelTecnologico++;
                recalcularPoblacionMaxima();
                emitirEvento(planeta.getNombre() + " alcanzo nivel tecnologico " + nivelTecnologico, MensajeEvento.TipoMensaje.EXITO);
            }
        }

        // Eventos negativos (más probables si en peligro)
        if (estado == EstadoCivilizacion.EN_PELIGRO && rand < 0.005) {
            // Catástrofe: pérdida poblacional y penalización temporal de producción
            poblacion *= 0.7;
            ticksEfectoEventoReciente = ConfiguracionSimulacion.DURACION_TICKS_EFECTO_EVENTO;
            emitirEvento("Catastrofe en " + planeta.getNombre() + "! Poblacion reducida a " + (int)poblacion, MensajeEvento.TipoMensaje.PELIGRO);
        }

        // Migración (futuro: entre planetas)
    }

    public void recalcularPoblacionMaxima() {
        // Basado en masa del planeta, tipo de planeta y tecnología
        double factorMasa = planeta.getMasa() / 5.97e24; // Relativo a Tierra
        double factorTipo = (planeta.getTipoCuerpo() != null)
                ? planeta.getTipoCuerpo().getFactorCapacidadPoblacion()
                : 1.0;
        poblacionMaxima = ConfiguracionSimulacion.POBLACION_MAXIMA_FACTOR * factorMasa * factorTipo * (1 + nivelTecnologico * 0.2);
    }

    public double getPorcentajeCapacidad() {
        if (poblacionMaxima <= 0) return 0.0;
        return (poblacion / poblacionMaxima) * 100.0;
    }

    public void agregarPoblacion(double cantidad) {
        if (cantidad > 0 && estado != EstadoCivilizacion.EXTINGUIDA) {
            this.poblacion = Math.min(this.poblacion + cantidad, this.poblacionMaxima);
        }
    }

    public void setPoblacion(double cantidad) {
        if (estado != EstadoCivilizacion.EXTINGUIDA) {
            this.poblacion = Math.max(0, Math.min(cantidad, this.poblacionMaxima));
        }
    }

    public void restarPoblacion(double cantidad) {
        this.poblacion = Math.max(0, this.poblacion - cantidad);
        if (this.poblacion < 10) {
            extinguir();
        }
    }

    public void recibirImpactoMeteorito() {
        if (estado == EstadoCivilizacion.EXTINGUIDA) return;
        this.poblacion *= 0.75; // Pérdida del 25% de población
        this.estabilidad = Math.max(0.1, this.estabilidad - 0.3);
        this.felicidad = Math.max(0.1, this.felicidad - 0.4);
        this.ticksEfectoEventoReciente = ConfiguracionSimulacion.DURACION_TICKS_EFECTO_EVENTO;
        this.estado = determinarEstado();
        emitirEvento("Catastrofe meteorica en " + planeta.getNombre() + "! Poblacion restante: " + (int)poblacion, MensajeEvento.TipoMensaje.PELIGRO);
    }

    /**
     * Destruye la civilizacion de forma explicita, poniendo la poblacion en 0
     * y marcando el estado como EXTINGUIDA.
     * Invocado al eliminar o destruir el planeta por cualquier via
     * (borrado manual, absorcion, colision, meteorito destructivo).
     */
    public void destruir() {
        this.poblacion = 0;
        this.estado = EstadoCivilizacion.EXTINGUIDA;
    }

    private void extinguir() {
        estado = EstadoCivilizacion.EXTINGUIDA;
        poblacion = 0;
        emitirEvento("Civilizacion en " + planeta.getNombre() + " se ha extinguido.", MensajeEvento.TipoMensaje.PELIGRO);
    }

    private void emitirEvento(String mensaje, MensajeEvento.TipoMensaje tipo) {
        if (emisorEventos != null) {
            emisorEventos.accept(new MensajeEvento(mensaje, tipo, 0));
        } else {
            System.out.println("[" + tipo.name() + "] " + mensaje);
        }
    }

    /**
     * Calcula el multiplicador dinamico de produccion de recursos para la civilizacion.
     * Incorpora estabilidad, felicidad, nivel tecnologico y penalizaciones de eventos recientes.
     *
     * @return Factor multiplicador de produccion.
     */
    public double calcularMultiplicadorProduccion() {
        if (estado == EstadoCivilizacion.EXTINGUIDA) return 0.0;
        double base = (estabilidad * 0.6) + (felicidad * 0.4);
        double techBonus = 1.0 + (nivelTecnologico * ConfiguracionSimulacion.MULTIPLICADOR_TECNOLOGIA_CIV);
        double factor = base * techBonus;
        if (ticksEfectoEventoReciente > 0) {
            factor *= (1.0 - ConfiguracionSimulacion.PENALIZACION_EVENTO_CATASTROFE);
        }
        return Math.max(0.1, factor);
    }

    // ===== Getters y Setters =====

    public void setEmisorEventos(Consumer<MensajeEvento> emisorEventos) {
        this.emisorEventos = emisorEventos;
    }

    public Consumer<MensajeEvento> getEmisorEventos() {
        return emisorEventos;
    }

    public int getTicksEfectoEventoReciente() {
        return ticksEfectoEventoReciente;
    }

    public void setTicksEfectoEventoReciente(int ticks) {
        this.ticksEfectoEventoReciente = Math.max(0, ticks);
    }

    public void restaurarMetricas(double poblacionGuardada, int nivelTech, double estabilidadGuardada, double felicidadGuardada) {
        this.nivelTecnologico = Math.max(0, Math.min(10, nivelTech));
        recalcularPoblacionMaxima();
        this.estabilidad = Math.max(0.0, Math.min(1.0, estabilidadGuardada));
        this.felicidad = Math.max(0.0, Math.min(1.0, felicidadGuardada));
        this.poblacion = Math.max(0.0, Math.min(poblacionGuardada, this.poblacionMaxima));
    }

    public void restaurarEstadoGeneral(EstadoCivilizacion estadoGuardado, int ticksEvento) {
        this.estado = estadoGuardado != null ? estadoGuardado : determinarEstado();
        this.ticksEfectoEventoReciente = Math.max(0, ticksEvento);
        if (this.estado == EstadoCivilizacion.EXTINGUIDA) {
            this.poblacion = 0.0;
        }
    }

    // ===== Getters =====

    public double getPoblacion() { return poblacion; }
    public double getPoblacionMaxima() { return poblacionMaxima; }
    public int getNivelTecnologico() { return nivelTecnologico; }
    public double getEstabilidad() { return estabilidad; }
    public double getFelicidad() { return felicidad; }
    public EstadoCivilizacion getEstado() { return estado; }
    public List<Double> getHistorialPoblacion() { return List.copyOf(historialPoblacion); }
    public List<Double> getHistorialEstabilidad() { return List.copyOf(historialEstabilidad); }

    public boolean estaViva() { return estado != EstadoCivilizacion.EXTINGUIDA; }

    public String getResumen() {
        double pct = poblacionMaxima > 0 ? (poblacion / poblacionMaxima * 100) : 0.0;
        return String.format("%s: Pop %.0f/%.0f (%.1f%%) | Tech %d | Est %.0f%% | %s",
                planeta.getNombre(), poblacion, poblacionMaxima, pct,
                nivelTecnologico, estabilidad*100, estado.nombre);
    }
}
