package org.example.game.civilizacion;

import org.example.Planeta;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.motor.Vector2D;
import org.example.CuerpoCeleste;
import org.example.Planeta;

import java.util.ArrayList;
import java.util.List;

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

    // Historial para gráficas
    private final List<Double> historialPoblacion = new ArrayList<>();
    private final List<Double> historialEstabilidad = new ArrayList<>();

    public enum EstadoCivilizacion {
        PRÓSPERA("Próspera", 0x32CD32),      // Verde
        ESTABLE("Estable", 0x4169E1),        // Azul
        LUCHANDO("Luchando", 0xFFA500),      // Naranja
        EN_PELIGRO("En Peligro", 0xFF4500),  // Rojo naranja
        EXTINGUIDA("Extinguida", 0x808080);  // Gris

        public final String nombre;
        public final int color;

        EstadoCivilizacion(String nombre, int color) {
            this.nombre = nombre;
            this.color = color;
        }

        public String getColorHex() {
            return String.format("#%06X", color);
        }
    }

    public CivilizacionComponent(Planeta planeta) {
        this.planeta = planeta;
        this.poblacion = ConfiguracionSimulacion.POBLACION_INICIAL;
        this.nivelTecnologico = 0;
        this.estabilidad = 1.0;
        this.felicidad = 1.0;
        this.estado = EstadoCivilizacion.PRÓSPERA;
        recalcularPoblacionMaxima();
    }

    /** Actualiza la civilización cada tick */
    public void actualizar(List<CuerpoCeleste> todosLosCuerpos) {
        if (estado == EstadoCivilizacion.EXTINGUIDA) return;

        // 1. Calcular condiciones de habitabilidad
        CondicionesHabitabilidad condiciones = evaluarHabitabilidad(todosLosCuerpos);

        // 2. Calcular crecimiento/decadencia de población
        double factorCrecimiento = calcularFactorCrecimiento(condiciones);
        poblacion *= (1.0 + factorCrecimiento);
        poblacion = Math.max(0, Math.min(poblacion, poblacionMaxima));

        // 3. Actualizar estabilidad y felicidad
        actualizarEstabilidadYFelicidad(condiciones);

        // 4. Determinar nuevo estado
        estado = determinarEstado();

        // 5. Eventos aleatorios basados en estado
        procesarEventosAleatorios();

        // 6. Registrar historial (cada 10 ticks para no saturar)
        if (historialPoblacion.size() % 10 == 0) {
            historialPoblacion.add(poblacion);
            historialEstabilidad.add(estabilidad);
            if (historialPoblacion.size() > ConfiguracionSimulacion.MAX_TICKS_HISTORIAL / 10) {
                historialPoblacion.remove(0);
                historialEstabilidad.remove(0);
            }
        }

        // 7. Verificar extinción
        if (poblacion < 1000) {
            extinguir();
        }
    }

    private CondicionesHabitabilidad evaluarHabitabilidad(List<CuerpoCeleste> cuerpos) {
        CondicionesHabitabilidad c = new CondicionesHabitabilidad();

        // Encontrar estrella más cercana
        CuerpoCeleste estrellaMasCercana = null;
        double distEstrellaMin = Double.MAX_VALUE;
        double masaEstrellaTotal = 0;

        // Encontrar agujero negro más cercano
        CuerpoCeleste agujeroMasCercano = null;
        double distAgujeroMin = Double.MAX_VALUE;

        for (CuerpoCeleste otro : cuerpos) {
            if (otro == planeta) continue;

            double dist = planeta.getPosicion().distanciaA(otro.getPosicion());

            if (otro.getTipoCuerpo() == org.example.game.cuerpo.TipoCuerpo.ESTRELLA) {
                masaEstrellaTotal += otro.getMasa();
                if (dist < distEstrellaMin) {
                    distEstrellaMin = dist;
                    estrellaMasCercana = otro;
                }
            } else if (otro.getTipoCuerpo() == org.example.game.cuerpo.TipoCuerpo.AGUJERO_NEGRO ||
                       otro.getTipoCuerpo() == org.example.game.cuerpo.TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO) {
                if (dist < distAgujeroMin) {
                    distAgujeroMin = dist;
                    agujeroMasCercano = otro;
                }
            }
        }

        // Zona habitable: basada en flujo estelar (luminosidad ~ masa^3.5 aprox)
        // Distancia ideal ~ sqrt(luminosidad) ~ masa^1.75
        if (estrellaMasCercana != null) {
            double masaSol = 1.989e30;
            double factorLuminosidad = Math.pow(estrellaMasCercana.getMasa() / masaSol, 3.5);
            double distanciaIdeal = Math.sqrt(factorLuminosidad) * 150; // 150px = 1 UA escalado
            c.distanciaAEstrella = distEstrellaMin;
            c.distanciaIdeal = distanciaIdeal;
            c.ratioZonaHabitable = distEstrellaMin / distanciaIdeal;
            c.enZonaHabitable = c.ratioZonaHabitable >= ConfiguracionSimulacion.DISTANCIA_ZONA_HABITABLE_MIN
                    && c.ratioZonaHabitable <= ConfiguracionSimulacion.DISTANCIA_ZONA_HABITABLE_MAX;
            c.flujoEnergetico = factorLuminosidad / (distEstrellaMin * distEstrellaMin);
        }

        // Amenaza de agujero negro
        if (agujeroMasCercano != null) {
            c.distanciaAgujeroNegro = distAgujeroMin;
            c.bajoAmenazaAgujero = distAgujeroMin < ConfiguracionSimulacion.AMENAZA_AGUJERO_NEGRO_RADIO;
            c.intensidadAmenaza = 1.0 - (distAgujeroMin / ConfiguracionSimulacion.AMENAZA_AGUJERO_NEGRO_RADIO);
        }

        // Estabilidad orbital: excentricidad aproximada
        // (simplificado: variación de distancia a estrella en últimos ticks)
        c.excentricidadOrbital = calcularExcentricidadAproximada(estrellaMasCercana);

        return c;
    }

    private double calcularExcentricidadAproximada(CuerpoCeleste estrella) {
        // Simplificado: usar velocidad actual vs velocidad circular
        if (estrella == null) return 0;
        double r = planeta.getPosicion().distanciaA(estrella.getPosicion());
        double vCircular = Math.sqrt(ConfiguracionSimulacion.G_ESCALADO * estrella.getMasa() / r);
        double vActual = planeta.getVelocidad().magnitud();
        return Math.abs(vActual - vCircular) / vCircular;
    }

    private double calcularFactorCrecimiento(CondicionesHabitabilidad c) {
        double factor = 0;

        if (c.enZonaHabitable) {
            // Crecimiento óptimo en centro de zona habitable
            double centroZona = (ConfiguracionSimulacion.DISTANCIA_ZONA_HABITABLE_MIN
                    + ConfiguracionSimulacion.DISTANCIA_ZONA_HABITABLE_MAX) / 2;
            double desviacion = Math.abs(c.ratioZonaHabitable - centroZona);
            double maxDesviacion = (ConfiguracionSimulacion.DISTANCIA_ZONA_HABITABLE_MAX
                    - ConfiguracionSimulacion.DISTANCIA_ZONA_HABITABLE_MIN) / 2;
            double bonusZona = 1.0 - (desviacion / maxDesviacion);
            factor += ConfiguracionSimulacion.CRECIMIENTO_POBLACION_BASE * (0.5 + bonusZona * 0.5);
        } else {
            // Decadencia fuera de zona
            factor -= ConfiguracionSimulacion.DECADENCIA_FUERA_ZONA;
        }

        // Penalización por agujero negro
        if (c.bajoAmenazaAgujero) {
            factor -= c.intensidadAmenaza * 0.01;
        }

        // Penalización por inestabilidad orbital
        factor -= c.excentricidadOrbital * 0.005;

        // Bonus por tecnología (futuro: tech tree)
        factor += nivelTecnologico * 0.0001;

        return factor;
    }

    private void actualizarEstabilidadYFelicidad(CondicionesHabitabilidad c) {
        // Estabilidad: afectada por amenazas y excentricidad
        double objetivoEstabilidad = 1.0;
        if (c.bajoAmenazaAgujero) objetivoEstabilidad -= c.intensidadAmenaza * 0.5;
        objetivoEstabilidad -= c.excentricidadOrbital * 0.3;
        if (!c.enZonaHabitable) objetivoEstabilidad -= 0.2;

        // Suavizar cambios
        estabilidad = estabilidad * 0.99 + objetivoEstabilidad * 0.01;
        estabilidad = Math.max(0, Math.min(1, estabilidad));

        // Felicidad: correlaciona con estabilidad y crecimiento
        double objetivoFelicidad = estabilidad * 0.7 + (poblacion / poblacionMaxima) * 0.3;
        felicidad = felicidad * 0.98 + objetivoFelicidad * 0.02;
        felicidad = Math.max(0, Math.min(1, felicidad));
    }

    private EstadoCivilizacion determinarEstado() {
        if (poblacion < 1000) return EstadoCivilizacion.EXTINGUIDA;
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
            if (nivelTecnologico < 10) nivelTecnologico++;
            System.out.println("🔬 " + planeta.getNombre() + " alcanzó nivel tecnológico " + nivelTecnologico);
        }

        // Eventos negativos (más probables si en peligro)
        if (estado == EstadoCivilizacion.EN_PELIGRO && rand < 0.005) {
            // Catástrofe: pérdida poblacional
            poblacion *= 0.7;
            System.out.println("☠ Catástrofe en " + planeta.getNombre() + "! Población: " + (int)poblacion);
        }

        // Migración (futuro: entre planetas)
    }

    private void recalcularPoblacionMaxima() {
        // Basado en masa del planeta y tecnología
        double factorMasa = planeta.getMasa() / 5.97e24; // Relativo a Tierra
        poblacionMaxima = ConfiguracionSimulacion.POBLACION_MAXIMA_FACTOR * factorMasa * (1 + nivelTecnologico * 0.2);
    }

    public void recibirImpactoMeteorito() {
        if (estado == EstadoCivilizacion.EXTINGUIDA) return;
        this.poblacion *= 0.75; // Pérdida del 25% de población
        this.estabilidad = Math.max(0.1, this.estabilidad - 0.3);
        this.felicidad = Math.max(0.1, this.felicidad - 0.4);
        this.estado = determinarEstado();
        System.out.println("💥 ¡Catástrofe meteórica en " + planeta.getNombre() + "! Población restante: " + (int)poblacion);
    }

    private void extinguir() {
        estado = EstadoCivilizacion.EXTINGUIDA;
        poblacion = 0;
        planeta.setSimulado(false); // El planeta sigue ahí pero sin civ
        System.out.println("💀 Civilización en " + planeta.getNombre() + " se ha extinguido.");
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
        return String.format("%s: Pop %.0f/%.0f (%.1f%%) | Tech %d | Est %.0f%% | %s",
                planeta.getNombre(), poblacion, poblacionMaxima, poblacion/poblacionMaxima*100,
                nivelTecnologico, estabilidad*100, estado.nombre);
    }

    // ===== Clase interna para condiciones =====

    public static class CondicionesHabitabilidad {
        public double distanciaAEstrella = -1;
        public double distanciaIdeal = -1;
        public double ratioZonaHabitable = 0;
        public boolean enZonaHabitable = false;
        public double flujoEnergetico = 0;

        public double distanciaAgujeroNegro = -1;
        public boolean bajoAmenazaAgujero = false;
        public double intensidadAmenaza = 0;

        public double excentricidadOrbital = 0;
    }
}