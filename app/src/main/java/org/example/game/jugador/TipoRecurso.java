package org.example.game.jugador;

/**
 * Tipos de recursos que el jugador acumula y gasta para crear cuerpos.
 */
public enum TipoRecurso {
    MASA_ESTELAR("Masa Estelar", "⭐", "Para crear estrellas", 0xFFD700),
    MATERIA_PLANETARIA("Materia Planetaria", "🪨", "Para crear planetas", 0x4169E1),
    MATERIA_OSCURA("Materia Oscura", "🕳", "Para crear agujeros negros", 0x800080),
    MINERALES("Minerales", "⛏", "Para crear meteoritos/naves", 0xB87333),
    ENERGIA("Energía", "⚡", "Mantenimiento y tecnología", 0x00FFFF),
    CIENCIA("Ciencia", "🔬", "Investigación y tech tree", 0xFF69B4);

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