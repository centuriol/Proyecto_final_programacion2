package org.example.game.civilizacion;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Planeta;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.MensajeEvento;

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
    private final CalculadorDinamicaCivilizacion calculadorDinamica;

    public CivilizacionComponent(Planeta planeta, double poblacionInicial) {
        this(planeta, poblacionInicial, new EvaluadorHabitabilidad(), new CalculadorDinamicaCivilizacion());
    }

    CivilizacionComponent(Planeta planeta,
                          double poblacionInicial,
                          EvaluadorHabitabilidad evaluadorHabitabilidad,
                          CalculadorDinamicaCivilizacion calculadorDinamica) {
        this.planeta = planeta;
        this.nivelTecnologico = 0;
        this.estabilidad = 1.0;
        this.felicidad = 1.0;
        this.estado = EstadoCivilizacion.PRÓSPERA;
        this.evaluadorHabitabilidad = evaluadorHabitabilidad != null ? evaluadorHabitabilidad : new EvaluadorHabitabilidad();
        this.calculadorDinamica = calculadorDinamica != null ? calculadorDinamica : new CalculadorDinamicaCivilizacion();
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
        double factorCrecimiento = calculadorDinamica.calcularFactorCrecimiento(condiciones, nivelTecnologico);
        poblacion = calculadorDinamica.calcularNuevaPoblacion(poblacion, poblacionMaxima, factorCrecimiento);
        estabilidad = calculadorDinamica.calcularNuevaEstabilidad(estabilidad, condiciones);
        felicidad = calculadorDinamica.calcularNuevaFelicidad(felicidad, estabilidad, poblacion, poblacionMaxima);
        estado = calculadorDinamica.determinarEstado(poblacion, estabilidad, felicidad);

        procesarEventosAleatorios();
        registrarMuestraHistorial();

        if (poblacion < 10) {
            extinguir();
        }
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

    private void procesarEventosAleatorios() {
        double rand = Math.random();

        if (estado == EstadoCivilizacion.PRÓSPERA && rand < 0.001) {
            if (nivelTecnologico < 10) {
                nivelTecnologico++;
                recalcularPoblacionMaxima();
                emitirEvento(planeta.getNombre() + " alcanzo nivel tecnologico " + nivelTecnologico, MensajeEvento.TipoMensaje.EXITO);
            }
        }

        if (estado == EstadoCivilizacion.EN_PELIGRO && rand < 0.005) {
            poblacion *= 0.7;
            ticksEfectoEventoReciente = ConfiguracionSimulacion.DURACION_TICKS_EFECTO_EVENTO;
            emitirEvento("Catastrofe en " + planeta.getNombre() + "! Poblacion reducida a " + (int)poblacion, MensajeEvento.TipoMensaje.PELIGRO);
        }
    }

    public void recalcularPoblacionMaxima() {
        double factorTipo = (planeta != null && planeta.getTipoCuerpo() != null)
                ? planeta.getTipoCuerpo().getFactorCapacidadPoblacion() : 0.0;
        poblacionMaxima = calculadorDinamica.calcularPoblacionMaxima(
                planeta != null ? planeta.getMasa() : 0.0, factorTipo, nivelTecnologico
        );
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
        this.poblacion *= 0.75;
        this.estabilidad = Math.max(0.1, this.estabilidad - 0.3);
        this.felicidad = Math.max(0.1, this.felicidad - 0.4);
        this.ticksEfectoEventoReciente = ConfiguracionSimulacion.DURACION_TICKS_EFECTO_EVENTO;
        this.estado = calculadorDinamica.determinarEstado(poblacion, estabilidad, felicidad);
        emitirEvento("Catastrofe meteorica en " + planeta.getNombre() + "! Poblacion restante: " + (int)poblacion, MensajeEvento.TipoMensaje.PELIGRO);
    }

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
        }
    }

    public double calcularMultiplicadorProduccion() {
        return calculadorDinamica.calcularMultiplicadorProduccion(
                estado, estabilidad, felicidad, nivelTecnologico, ticksEfectoEventoReciente
        );
    }

    // ===== Getters y Setters =====

    public void setEmisorEventos(Consumer<MensajeEvento> emisorEventos) {
        this.emisorEventos = emisorEventos;
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
        this.estado = estadoGuardado != null ? estadoGuardado : calculadorDinamica.determinarEstado(poblacion, estabilidad, felicidad);
        this.ticksEfectoEventoReciente = Math.max(0, ticksEvento);
        if (this.estado == EstadoCivilizacion.EXTINGUIDA) {
            this.poblacion = 0.0;
        }
    }

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
