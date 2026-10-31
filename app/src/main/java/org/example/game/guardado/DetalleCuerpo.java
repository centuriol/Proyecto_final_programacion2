package org.example.game.guardado;
/**
 * Atributos especificos de subtipo capturados para la persistencia de un cuerpo celeste (SRP).
 */
public record DetalleCuerpo( boolean transformadoPorEvento, int golpesEscudo, DatosCivilizacion civilizacion, double cienciaGenerada, double energiaGenerada, double tiempoVidaRestante, boolean lanzadoPorJugador, int indiceObjetivo ) {
    public DetalleCuerpo {
        golpesEscudo = Math.max(0, golpesEscudo);
        cienciaGenerada = Double.isFinite(cienciaGenerada) ? Math.max(0.0, cienciaGenerada) : 0.0;
        energiaGenerada = Double.isFinite(energiaGenerada) ? Math.max(0.0, energiaGenerada) : 0.0;
        tiempoVidaRestante = Double.isFinite(tiempoVidaRestante) ? tiempoVidaRestante : 0.0;
        indiceObjetivo = Math.max(-1, indiceObjetivo);
    }

    public static DetalleCuerpo basico() {
        return new DetalleCuerpo(false, 0, null, 0.0, 0.0, 0.0, false, -1);
    }

    public static DetalleCuerpo dePlaneta(boolean transformado, int golpesEscudo, DatosCivilizacion civilizacion) {
        return new DetalleCuerpo(transformado, golpesEscudo, civilizacion, 0.0, 0.0, 0.0, false, -1);
    }

    public static DetalleCuerpo deSatelite(double cienciaGenerada, double energiaGenerada) {
        return new DetalleCuerpo(false, 0, null, cienciaGenerada, energiaGenerada, 0.0, false, -1);
    }

    public static DetalleCuerpo deMeteorito(double vidaRestante, boolean lanzadoJugador, int indiceObjetivo) {
        return new DetalleCuerpo(false, 0, null, 0.0, 0.0, vidaRestante, lanzadoJugador, indiceObjetivo);
    }

    public boolean tieneCivilizacionRegistrada() {
        return civilizacion != null;
    }
}
