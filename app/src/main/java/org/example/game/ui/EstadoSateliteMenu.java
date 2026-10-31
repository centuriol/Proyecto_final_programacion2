package org.example.game.ui;
import org.example.game.motor.Vector2D;
/**
 * SRP: Instantanea inmutable del estado visual y cinematico de un satelite decorativo del menu.
 */
public record EstadoSateliteMenu( SateliteDecorativoMenu.Lado lado, double anguloGrados, boolean cayendo, Vector2D cinematicaCaida ) {
    public double desplazamientoCaidaY() {
        return cinematicaCaida.x();
    }

    public double velocidadCaidaY() {
        return cinematicaCaida.y();
    }
}
