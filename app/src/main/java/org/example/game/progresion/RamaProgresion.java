package org.example.game.progresion;

/**
 * Las tres ramas de desarrollo científico del árbol tecnológico.
 *
 * Principios SOLID:
 * - SRP: Identifica y describe cada rama de especialización del árbol de tecnologías.
 */
public enum RamaProgresion {
    PLANETAS(
            "Cuerpos y Planetas",
            "Desbloquea satélites, planetas habitables, escudos protectores y mundos volcánicos."
    ),
    MASA(
            "Límite de Masa",
            "Aumenta el factor de masa máximo permitido para colocar cuerpos celestes en el simulador."
    ),
    CANTIDAD_PLANETAS(
            "Capacidad Planetaria",
            "Expande el número máximo de planetas simultáneos que pueden coexistir en el sistema solar."
    );

    public final String nombre;
    public final String descripcion;

    RamaProgresion(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }
}
