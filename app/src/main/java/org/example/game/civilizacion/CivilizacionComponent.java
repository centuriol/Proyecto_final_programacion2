package org.example.game.civilizacion;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Planeta;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.MensajeEvento;

import java.util.List;
import java.util.function.Consumer;

/**
 * Componente de civilización para planetas: gestiona demografía, estabilidad, tecnología y eventos (SRP).
 */
public class CivilizacionComponent {

    private static class EstadoDemografico {
        final Planeta planeta;
        double poblacion;
        double poblacionMaxima;
        int nivelTecnologico = 0;
        double estabilidad = 1.0;
        double felicidad = 1.0;
        EstadoCivilizacion estado = EstadoCivilizacion.PRÓSPERA;
        int ticksEfectoEventoReciente = 0;
        Consumer<MensajeEvento> emisorEventos;
        final HistorialCivilizacion historial = new HistorialCivilizacion();

        EstadoDemografico(Planeta planeta) {
            this.planeta = planeta;
        }

        void emitir(String mensaje, MensajeEvento.TipoMensaje tipo) {
            if (emisorEventos != null) {
                emisorEventos.accept(new MensajeEvento(mensaje, tipo, 0));
            }
        }

        void extinguir() {
            estado = EstadoCivilizacion.EXTINGUIDA;
            poblacion = 0;
            emitir("Civilizacion en " + planeta.getNombre() + " se ha extinguido.", MensajeEvento.TipoMensaje.PELIGRO);
        }
    }

    private static final FabricaCivilizacion FABRICA = new FabricaCivilizacion();

    private final EstadoDemografico datos;
    private final AnalisisHabitabilidad evaluadorHabitabilidad;
    private final CalculadorDinamicaCivilizacion calculadorDinamica;

    public CivilizacionComponent(Planeta planeta, double poblacionInicial) {
        this(planeta, poblacionInicial, FABRICA.crearEvaluadorHabitabilidad(), FABRICA.crearCalculadorDinamica());
    }

    public CivilizacionComponent(Planeta planeta, double poblacionInicial,
                                 AnalisisHabitabilidad evaluadorHabitabilidad,
                                 CalculadorDinamicaCivilizacion calculadorDinamica) {
        this.datos = new EstadoDemografico(planeta);
        this.evaluadorHabitabilidad = evaluadorHabitabilidad != null ? evaluadorHabitabilidad : FABRICA.crearEvaluadorHabitabilidad();
        this.calculadorDinamica = calculadorDinamica != null ? calculadorDinamica : FABRICA.crearCalculadorDinamica();
        recalcularPoblacionMaxima();
        this.datos.poblacion = Math.min(poblacionInicial, this.datos.poblacionMaxima);
    }

    public void actualizar(List<CuerpoCeleste> todosLosCuerpos) {
        if (datos.estado == EstadoCivilizacion.EXTINGUIDA) return;
        if (datos.ticksEfectoEventoReciente > 0) datos.ticksEfectoEventoReciente--;

        CondicionesHabitabilidad condiciones = evaluadorHabitabilidad.evaluar(datos.planeta, todosLosCuerpos);
        double factorCrecimiento = calculadorDinamica.calcularFactorCrecimiento(condiciones, datos.nivelTecnologico);
        if (datos.planeta != null && datos.planeta.getTipoCuerpo() != TipoCuerpo.PLANETA_ROCOSO && factorCrecimiento > 0) {
            factorCrecimiento *= ConfiguracionSimulacion.FACTOR_CRECIMIENTO_OTROS_PLANETAS;
        }
        datos.poblacion = calculadorDinamica.calcularNuevaPoblacion(datos.poblacion, datos.poblacionMaxima, factorCrecimiento);
        datos.estabilidad = calculadorDinamica.calcularNuevaEstabilidad(datos.estabilidad, condiciones);
        datos.felicidad = calculadorDinamica.calcularNuevaFelicidad(datos.felicidad, datos.estabilidad, datos.poblacion, datos.poblacionMaxima);
        datos.estado = calculadorDinamica.determinarEstado(datos.poblacion, datos.estabilidad, datos.felicidad);

        procesarEventosAleatorios();
        datos.historial.registrarMuestra(datos.poblacion, datos.estabilidad);
        if (datos.poblacion < 10) datos.extinguir();
    }

    private void procesarEventosAleatorios() {
        double rand = Math.random();
        if (datos.estado == EstadoCivilizacion.PRÓSPERA && rand < 0.001 && datos.nivelTecnologico < 10) {
            datos.nivelTecnologico++;
            recalcularPoblacionMaxima();
            datos.emitir(datos.planeta.getNombre() + " alcanzo nivel tecnologico " + datos.nivelTecnologico, MensajeEvento.TipoMensaje.EXITO);
        }
        if (datos.estado == EstadoCivilizacion.EN_PELIGRO && rand < 0.005) {
            datos.poblacion *= 0.7;
            datos.ticksEfectoEventoReciente = ConfiguracionSimulacion.DURACION_TICKS_EFECTO_EVENTO;
            datos.emitir("Catastrofe en " + datos.planeta.getNombre() + "! Poblacion reducida a " + (int) datos.poblacion, MensajeEvento.TipoMensaje.PELIGRO);
        }
    }

    public void recalcularPoblacionMaxima() {
        double factorTipo = (datos.planeta != null && datos.planeta.getTipoCuerpo() != null)
                ? datos.planeta.getTipoCuerpo().getFactorCapacidadPoblacion() : 0.0;
        datos.poblacionMaxima = calculadorDinamica.calcularPoblacionMaxima(
                datos.planeta != null ? datos.planeta.getMasa() : 0.0, factorTipo, datos.nivelTecnologico
        );
    }

    public double getPorcentajeCapacidad() { return datos.poblacionMaxima <= 0 ? 0.0 : (datos.poblacion / datos.poblacionMaxima) * 100.0; }

    public void agregarPoblacion(double cantidad) {
        if (cantidad > 0 && datos.estado != EstadoCivilizacion.EXTINGUIDA) {
            datos.poblacion = Math.min(datos.poblacion + cantidad, datos.poblacionMaxima);
        }
    }

    public void setPoblacion(double cantidad) {
        if (datos.estado != EstadoCivilizacion.EXTINGUIDA) {
            datos.poblacion = Math.max(0, Math.min(cantidad, datos.poblacionMaxima));
        }
    }

    public void restarPoblacion(double cantidad) {
        datos.poblacion = Math.max(0, datos.poblacion - cantidad);
        if (datos.poblacion < 10) datos.extinguir();
    }

    public void recibirImpactoMeteorito() {
        if (datos.estado == EstadoCivilizacion.EXTINGUIDA) return;
        datos.estabilidad = 0.0;
        datos.felicidad = 0.0;
        datos.ticksEfectoEventoReciente = ConfiguracionSimulacion.DURACION_TICKS_EFECTO_EVENTO;
        datos.extinguir();
    }

    public void destruir() {
        datos.poblacion = 0;
        datos.estado = EstadoCivilizacion.EXTINGUIDA;
    }

    public double calcularMultiplicadorProduccion() {
        return calculadorDinamica.calcularMultiplicadorProduccion(
                datos.estado, datos.estabilidad, datos.felicidad, datos.nivelTecnologico, datos.ticksEfectoEventoReciente
        );
    }

    public void restaurarMetricas(double poblacionGuardada, int nivelTech, double estabilidadGuardada, double felicidadGuardada) {
        datos.nivelTecnologico = Math.max(0, Math.min(10, nivelTech));
        recalcularPoblacionMaxima();
        datos.estabilidad = Math.max(0.0, Math.min(1.0, estabilidadGuardada));
        datos.felicidad = Math.max(0.0, Math.min(1.0, felicidadGuardada));
        datos.poblacion = Math.max(0.0, Math.min(poblacionGuardada, datos.poblacionMaxima));
    }

    public void restaurarEstadoGeneral(EstadoCivilizacion estadoGuardado, int ticksEvento) {
        datos.estado = estadoGuardado != null ? estadoGuardado : calculadorDinamica.determinarEstado(datos.poblacion, datos.estabilidad, datos.felicidad);
        datos.ticksEfectoEventoReciente = Math.max(0, ticksEvento);
        if (datos.estado == EstadoCivilizacion.EXTINGUIDA) datos.poblacion = 0.0;
    }

    public void setEmisorEventos(Consumer<MensajeEvento> emisor) { datos.emisorEventos = emisor; }
    public int getTicksEfectoEventoReciente() { return datos.ticksEfectoEventoReciente; }
    public void setTicksEfectoEventoReciente(int ticks) { datos.ticksEfectoEventoReciente = Math.max(0, ticks); }
    public double getPoblacion() { return datos.poblacion; }
    public double getPoblacionMaxima() { return datos.poblacionMaxima; }
    public int getNivelTecnologico() { return datos.nivelTecnologico; }
    public double getEstabilidad() { return datos.estabilidad; }
    public double getFelicidad() { return datos.felicidad; }
    public EstadoCivilizacion getEstado() { return datos.estado; }
    public List<Double> getHistorialPoblacion() { return datos.historial.getHistorialPoblacion(); }
    public List<Double> getHistorialEstabilidad() { return datos.historial.getHistorialEstabilidad(); }
    public boolean estaViva() { return datos.estado != EstadoCivilizacion.EXTINGUIDA; }

    public String getResumen() {
        double pct = datos.poblacionMaxima > 0 ? (datos.poblacion / datos.poblacionMaxima * 100) : 0.0;
        return String.format("%s: Pop %.0f/%.0f (%.1f%%) | Tech %d | Est %.0f%% | %s",
                datos.planeta.getNombre(), datos.poblacion, datos.poblacionMaxima, pct,
                datos.nivelTecnologico, datos.estabilidad * 100, datos.estado.nombre);
    }
}
