package org.example.game.civilizacion;

/**
 * Estados de prosperidad y supervivencia de una civilización planetaria.
 */
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
        return String.format("#%06X", (0xFFFFFF & color));
    }
}
