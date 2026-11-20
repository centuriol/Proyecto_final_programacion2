package org.example.game.simulacion;

import org.example.game.colonizacion.GestorColonizacion;
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.cuerpo.FabricaCuerpos;
import org.example.game.cuerpo.ServicioEscudo;
import org.example.game.cuerpo.SistemaSolar;
import org.example.game.efectos.GestorEfectos;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.ServicioReembolso;
import org.example.game.motor.MotorFisica;
import org.example.game.motor.MotorFisicaPermisiva;
import org.example.game.motor.TransformadorCuerpos;
import org.example.game.motor.TrayectoriaPredictor;
import org.example.game.progresion.SistemaProgresion;

import java.util.function.BiConsumer;
import java.util.function.DoubleSupplier;

/**
 * SRP: Ensambla y mantiene el grafo de subsistemas internos de una instancia de SimulacionSolar.
 */
public class SubsistemaSimulacion {

    final SistemaSolar sistemaSolar;
    final MotorFisica motorFisica;
    final InventarioJugador inventario = new InventarioJugador();
    final TrayectoriaPredictor predictor = new TrayectoriaPredictor();
    final TransformadorCuerpos transformadorCuerpos = new TransformadorCuerpos();
    final SistemaProgresion progresion = new SistemaProgresion();
    final ControladorEconomia gestorEconomia;
    final GeneradorEventos generadorEventosCosmicos;
    final GestorColonizacion gestorColonizacion = new GestorColonizacion();
    final GestorEfectos gestorEfectos = new GestorEfectos();
    final RelojSimulacion reloj = new RelojSimulacion();
    final BusEventos busEventos = new BusEventos();
    final ControladorColocacion controladorColocacion = new ControladorColocacion();
    final EvaluadorDerrota evaluadorCondicionDerrota = new EvaluadorCondicionDerrota();
    final ValidadorColocacion validadorColocacionCuerpos = new ValidadorColocacionCuerpos();
    final PresetsSistema presetsSistema;
    final ServicioEscudo servicioEscudo = new ServicioEscudo();
    final ServicioReembolso servicioReembolso = new ServicioReembolso();
    final CalculadorAmenazaMeteorito escaladorAmenaza = new EscaladorAmenazaMeteorito();
    final FabricaCuerpos fabricaCuerpos;
    final CoordinadorPasoSimulacion coordinadorPaso;
    final GestorCicloVidaCuerpos gestorCicloVida;
    final GestorColocacionCuerpos gestorColocacion;

    public SubsistemaSimulacion(double anchoInicial, double altoInicial,
                                DoubleSupplier anchoSupplier, DoubleSupplier altoSupplier,
                                BiConsumer<String, MensajeEvento.TipoMensaje> notificador) {
        this.motorFisica = new MotorFisicaPermisiva(Math.max(anchoInicial, altoInicial));
        this.sistemaSolar = new SistemaSolar(this.motorFisica);
        this.inventario.setProgresion(this.progresion);
        this.gestorEconomia = new GestorEconomia(this.progresion);
        this.fabricaCuerpos = new CuerpoCelesteFactory(this.escaladorAmenaza);
        this.generadorEventosCosmicos = new GeneradorEventosCosmicos(
                ConfiguracionColisiones.TICKS_GRACIA_INICIAL_METEORITOS, this.fabricaCuerpos
        );
        this.presetsSistema = new PresetsSistema(this.fabricaCuerpos);
        this.coordinadorPaso = new CoordinadorPasoSimulacion(
                motorFisica, sistemaSolar, transformadorCuerpos, gestorEconomia, inventario,
                gestorColonizacion, gestorEfectos, generadorEventosCosmicos, progresion, escaladorAmenaza
        );
        this.gestorCicloVida = new GestorCicloVidaCuerpos(
                sistemaSolar, validadorColocacionCuerpos, progresion, inventario, busEventos, fabricaCuerpos
        );
        this.gestorColocacion = new GestorColocacionCuerpos(
                controladorColocacion, gestorCicloVida, anchoSupplier, altoSupplier
        );
        conectarNotificadores(notificador);
    }

    private void conectarNotificadores(BiConsumer<String, MensajeEvento.TipoMensaje> notificador) {
        transformadorCuerpos.setListenerMensaje(msg -> notificador.accept(msg, MensajeEvento.TipoMensaje.EXITO));
        if (motorFisica != null) {
            motorFisica.setListenerMensaje(msg -> notificador.accept(msg, MensajeEvento.TipoMensaje.EXITO));
            motorFisica.agregarListenerColision(busEventos::notificarCambioEstado);
            motorFisica.setListenerNotificacion(notificador);
            motorFisica.setListenerExplosion(gestorEfectos::agregarExplosion);
            motorFisica.setProveedorProgreso(() -> escaladorAmenaza.factorProgreso(progresion));
        }
    }

    public void reiniciarTodo() {
        gestorCicloVida.limpiarCuerpos();
        gestorColonizacion.limpiar();
        gestorEfectos.limpiar();
        inventario.reiniciar();
        progresion.reiniciar();
        reloj.reiniciar();
        fabricaCuerpos.resetearContador();
        gestorColocacion.salirModoColocacion();
        busEventos.notificarListenersUI();
    }
}
