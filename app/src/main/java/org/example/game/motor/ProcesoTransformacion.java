package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;

/**
 * SRP: Mantiene el estado mutable de un proceso de transformación planetaria en curso.
 */
public class ProcesoTransformacion {

    private record DatosProceso(
            CuerpoCeleste cuerpo,
            CuerpoCeleste estrella,
            TipoCuerpo tipoDestino,
            String nombreResultante,
            String mensajeEvento,
            double radioMinimoActivacion,
            double radioActivacion,
            boolean migrarSiguienteOrbita
    ) {}

    private static class ProgresoMigracion {
        EstadoTransformacion estado = EstadoTransformacion.ESPERANDO;
        int ticksEspera = 1;
        int ticksMigracion = 0;
        Vector2D posInicialMigracion;
        Vector2D posDestinoMigracion;
        double radioOrbitaDestino;

        void avanzarEspera() {
            ticksEspera++;
        }

        void avanzarMigracion() {
            ticksMigracion++;
        }

        void comenzarMigracion(Vector2D posInicial, Vector2D posDestino, double radioDestino) {
            estado = EstadoTransformacion.MIGRANDO;
            posInicialMigracion = posInicial;
            posDestinoMigracion = posDestino;
            radioOrbitaDestino = radioDestino;
            ticksMigracion = 0;
        }
    }

    private final DatosProceso datos;
    private final ProgresoMigracion progreso = new ProgresoMigracion();

    public ProcesoTransformacion(CuerpoCeleste cuerpo, CuerpoCeleste estrella, TipoCuerpo tipoDestino,
                                 String nombreResultante, String mensajeEvento, double radioActivacion) {
        this(cuerpo, estrella, tipoDestino, nombreResultante, mensajeEvento, 0.0, radioActivacion, true);
    }

    public ProcesoTransformacion(CuerpoCeleste cuerpo, CuerpoCeleste estrella, TipoCuerpo tipoDestino,
                                 String nombreResultante, String mensajeEvento,
                                 double radioMinimoActivacion, double radioActivacion, boolean migrarSiguienteOrbita) {
        this.datos = new DatosProceso(
                cuerpo, estrella, tipoDestino, nombreResultante, mensajeEvento,
                radioMinimoActivacion, radioActivacion, migrarSiguienteOrbita
        );
    }

    public CuerpoCeleste getCuerpo() { return datos.cuerpo(); }
    public CuerpoCeleste getEstrella() { return datos.estrella(); }
    public TipoCuerpo getTipoDestino() { return datos.tipoDestino(); }
    public EstadoTransformacion getEstado() { return progreso.estado; }
    public int getTicksEspera() { return progreso.ticksEspera; }
    public int getTicksMigracion() { return progreso.ticksMigracion; }
    public Vector2D getPosDestinoMigracion() { return progreso.posDestinoMigracion; }
    public String getNombreResultante() { return datos.nombreResultante(); }
    public String getMensajeEvento() { return datos.mensajeEvento(); }
    public double getRadioMinimoActivacion() { return datos.radioMinimoActivacion(); }
    public double getRadioActivacion() { return datos.radioActivacion(); }
    public boolean isMigrarSiguienteOrbita() { return datos.migrarSiguienteOrbita(); }
    public Vector2D getPosInicialMigracion() { return progreso.posInicialMigracion; }
    public double getRadioOrbitaDestino() { return progreso.radioOrbitaDestino; }

    public void incrementarTicksEspera() {
        progreso.avanzarEspera();
    }

    public void incrementarTicksMigracion() {
        progreso.avanzarMigracion();
    }

    public void iniciarMigracion(Vector2D posInicial, Vector2D posDestino, double radioOrbitaDestino) {
        progreso.comenzarMigracion(posInicial, posDestino, radioOrbitaDestino);
    }
}
