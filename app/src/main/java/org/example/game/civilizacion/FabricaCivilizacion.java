package org.example.game.civilizacion;

/**
 * Fábrica de composición para los colaboradores por defecto de CivilizacionComponent (DIP).
 */
public class FabricaCivilizacion {

    public AnalisisHabitabilidad crearEvaluadorHabitabilidad() {
        return new EvaluadorHabitabilidad();
    }

    public CalculadorDinamicaCivilizacion crearCalculadorDinamica() {
        return new CalculadorDinamicaCivilizacion();
    }
}
