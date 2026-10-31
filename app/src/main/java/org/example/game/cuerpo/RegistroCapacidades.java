package org.example.game.cuerpo;

import org.example.game.civilizacion.SistemaCivilizaciones;
import org.example.game.colonizacion.GestorColonizacion;
import org.example.game.motor.MotorFisicaBase;
import org.example.game.motor.MotorFisicaPermisiva;
import org.example.game.motor.TransformadorCuerpos;

import java.util.Optional;

/**
 * SRP: Hub de despacho para el registro y desregistro de capacidades polimórficas
 * (Civilizable, Defensor, Colonizable, Transformable, Actualizable, Expirable)
 * sin requerir casteo explícito de los tipos concretos creados por la fábrica.
 */
public class RegistroCapacidades {

    public static void registrarPlaneta(Planeta planeta) {
        if (planeta == null) return;
        SistemaCivilizaciones.getInstancia().registrar(planeta, planeta);
        ServicioEscudo.getInstanciaPorDefecto().registrar(planeta, planeta);
        GestorColonizacion.getInstanciaGlobal().registrar(planeta, planeta);
        TransformadorCuerpos.getInstanciaGlobal().registrarTransformable(planeta, planeta);
    }

    public static void registrarSatelite(Satelite satelite) {
        if (satelite == null) return;
        MotorFisicaPermisiva.getInstanciaGlobal().registrarActualizable(satelite, satelite);
    }

    public static void registrarMeteorito(Meteorito meteorito) {
        if (meteorito == null) return;
        MotorFisicaPermisiva.getInstanciaGlobal().registrarActualizable(meteorito, meteorito);
        MotorFisicaPermisiva.getInstanciaGlobal().registrarExpirable(meteorito, meteorito);
    }

    public static void registrarEstrella(Estrella estrella) {
        if (estrella == null) return;
        TransformadorCuerpos.getInstanciaGlobal().registrarEstrella(estrella);
    }

    public static void registrarAgujeroNegro(AgujeroNegro agujeroNegro) {
        if (agujeroNegro == null) return;
        org.example.game.motor.GestorAgujeroNegro.registrar(agujeroNegro);
    }

    public static void registrarExpirable(CuerpoCeleste cuerpo, Expirable expirable) {
        if (cuerpo == null || expirable == null) return;
        MotorFisicaPermisiva.getInstanciaGlobal().registrarExpirable(cuerpo, expirable);
    }

    public static void desregistrar(CuerpoCeleste cuerpo) {
        if (cuerpo == null) return;
        SistemaCivilizaciones.getInstancia().desregistrar(cuerpo);
        ServicioEscudo.getInstanciaPorDefecto().desregistrar(cuerpo);
        GestorColonizacion.getInstanciaGlobal().desregistrar(cuerpo);
        TransformadorCuerpos.getInstanciaGlobal().desregistrar(cuerpo);
        MotorFisicaPermisiva.getInstanciaGlobal().desregistrar(cuerpo);
        org.example.game.motor.GestorAgujeroNegro.desregistrar(cuerpo);
    }

    public static Optional<Civilizable> obtenerCivilizable(CuerpoCeleste cuerpo) {
        return SistemaCivilizaciones.getInstancia().obtenerCivilizable(cuerpo);
    }

    public static Optional<Defensor> obtenerDefensor(CuerpoCeleste cuerpo) {
        return ServicioEscudo.getInstanciaPorDefecto().obtenerDefensor(cuerpo);
    }

    public static Optional<Colonizable> obtenerColonizable(CuerpoCeleste cuerpo) {
        return GestorColonizacion.getInstanciaGlobal().obtenerColonizable(cuerpo);
    }

    public static Optional<Transformable> obtenerTransformable(CuerpoCeleste cuerpo) {
        return TransformadorCuerpos.getInstanciaGlobal().obtenerTransformable(cuerpo);
    }

    public static void limpiar() {
        SistemaCivilizaciones.getInstancia().limpiar();
        ServicioEscudo.getInstanciaPorDefecto().limpiar();
        GestorColonizacion.getInstanciaGlobal().limpiar();
        TransformadorCuerpos.getInstanciaGlobal().limpiar();
        MotorFisicaPermisiva.getInstanciaGlobal().limpiarCapacidades();
    }
}
