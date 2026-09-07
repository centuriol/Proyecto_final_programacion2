package org.example.game.jugador;

/**
 * Tipos de recursos que el jugador acumula y gasta para crear cuerpos.
 */
public enum TipoRecurso {
    MINERALES("Minerales", "[MIN]", "Material basico de construccion y estructuras", 0xB87333),
    ENERGIA("Energia", "[ENE]", "Energia estelar y tecnologica", 0x00FFFF),
    POBLACION("Poblacion", "[POB]", "Habitantes y fuerza de trabajo", 0x50FA7B),
    CIENCIA("Ciencia", "[CIE]", "Desarrollo cientifico y tecnologico", 0xFF69B4);

    public final String nombre;
    public final String icono;
    public final String descripcion;
    public final int color;

    TipoRecurso(String nombre, String icono, String descripcion, int color) {
        this.nombre = nombre;
        this.icono = icono;
        this.descripcion = descripcion;
        this.color = color;
    }

    public String getColorHex() {
        return String.format("#%06X", color);
    }
}