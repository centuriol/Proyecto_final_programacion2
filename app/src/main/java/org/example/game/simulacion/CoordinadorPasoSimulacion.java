package org.example.game.simulacion;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.SistemaSolar;
import org.example.game.motor.MotorFisica;
import org.example.game.motor.TransformadorCuerpos;
import org.example.game.colonizacion.GestorColonizacion;
import org.example.game.efectos.GestorEfectos;
import org.example.game.jugador.InventarioJugador;
import org.example.game.progresion.SistemaProgresion;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * SRP: Coordina la ejecucion ordenada del pipeline de avance de paso (tick) de la simulacion.
 * DIP: Depende de abstracciones de servicios y gestores delegados.
 */
public class CoordinadorPasoSimulacion {

    private final MotorFisica motorFisica;
    private final SistemaSolar sistemaSolar;
    private final TransformadorCuerpos transformadorCuerpos;
    private final GestorEconomia gestorEconomia;
    private final InventarioJugador inventario;
    private final GestorColonizacion gestorColonizacion;
    private final GestorEfectos gestorEfectos;
    private final GeneradorEventosCosmicos generadorEventosCosmicos;
    private final SistemaProgresion progresion;
    private final CalculadorAmenazaMeteorito escaladorAmenaza;

    public CoordinadorPasoSimulacion(MotorFisica motorFisica,
                                     SistemaSolar sistemaSolar,
                                     TransformadorCuerpos transformadorCuerpos,
                                     GestorEconomia gestorEconomia,
                                     InventarioJugador inventario,
                                     GestorColonizacion gestorColonizacion,
                                     GestorEfectos gestorEfectos,
                                     GeneradorEventosCosmicos generadorEventosCosmicos,
                                     SistemaProgresion progresion,
                                     CalculadorAmenazaMeteorito escaladorAmenaza) {
        this.motorFisica = motorFisica;
        this.sistemaSolar = sistemaSolar;
        this.transformadorCuerpos = transformadorCuerpos;
        this.gestorEconomia = gestorEconomia;
        this.inventario = inventario;
        this.gestorColonizacion = gestorColonizacion;
        this.gestorEfectos = gestorEfectos;
        this.generadorEventosCosmicos = generadorEventosCosmicos;
        this.progresion = progresion;
        this.escaladorAmenaza = escaladorAmenaza != null ? escaladorAmenaza : new EscaladorAmenazaMeteorito();
    }

    public void ejecutarPaso(long tickActual, double anchoMundo, double altoMundo,
                             Consumer<MensajeEvento> emisorEventos,
                             Consumer<CuerpoCeleste> agregadorCuerpos) {
        // 1. Fisica permisiva con asistencia orbital
        motorFisica.avanzarPaso(sistemaSolar.getCuerpos());

        // 2. Actualizar civilizaciones
        actualizarCivilizaciones(emisorEventos);

        // 2.5 Transformaciones de cuerpos por proximidad
        transformadorCuerpos.procesarTransformaciones(sistemaSolar.getCuerpos());

        // 3. Economia pasiva y satelites
        gestorEconomia.actualizarEconomia(sistemaSolar.getCuerpos(), inventario);

        // 4. Misiones de colonizacion en curso
        gestorColonizacion.actualizar(emisorEventos, tickActual);

        // 5. Efectos visuales
        gestorEfectos.actualizar();

        // 6. Eventos cosmicos escalados segun el progreso tecnologico / masa desbloqueada
        double factorProgreso = escaladorAmenaza.factorProgreso(progresion);
        generadorEventosCosmicos.actualizarEventosCosmicos(
                sistemaSolar.getCuerpos(), anchoMundo, altoMundo, agregadorCuerpos, emisorEventos, tickActual, factorProgreso
        );
    }

    public void actualizarCivilizaciones(Consumer<MensajeEvento> emisorEventos) {
        List<CuerpoCeleste> cuerpos = new ArrayList<>(sistemaSolar.getCuerpos());
        org.example.game.civilizacion.SistemaCivilizaciones.getInstancia().actualizarCivilizaciones(cuerpos, emisorEventos);
    }

    public GeneradorEventosCosmicos getGeneradorEventosCosmicos() {
        return generadorEventosCosmicos;
    }
}
