package org.example.game.simulacion;

import org.example.game.cuerpo.CreadorMeteoritos;
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.cuerpo.FabricaCuerpos;
import org.example.game.motor.ConversorCoordenadas;
import org.example.game.motor.TransformadorCoordenadas;
import java.util.function.BiConsumer;
import java.util.function.DoubleSupplier;

/**
 * Fábrica de composición para los colaboradores por defecto del módulo de simulación (DIP).
 */
public class FabricaSimulacion {

    public ConversorCoordenadas crearConversorCoordenadas() {
        return new TransformadorCoordenadas();
    }

    public FabricaCuerpos crearFabricaCuerpos() {
        return new CuerpoCelesteFactory();
    }

    public FabricaCuerpos crearFabricaCuerpos(CalculadorAmenazaMeteorito calculadorAmenaza) {
        return new CuerpoCelesteFactory(calculadorAmenaza);
    }

    public CreadorMeteoritos crearCreadorMeteoritos() {
        return new CuerpoCelesteFactory();
    }

    public CalculadorAmenazaMeteorito crearCalculadorAmenaza() {
        return new EscaladorAmenazaMeteorito();
    }

    public VinculadorAutoOrbita crearVinculadorAutoOrbita() {
        return new VinculadorAutoOrbita();
    }

    public ControladorColocacion crearControladorColocacion() {
        return new ControladorColocacion();
    }

    public SubsistemaSimulacion crearSubsistemaPorDefecto(
            double anchoMundo, double altoMundo,
            DoubleSupplier anchoSupplier, DoubleSupplier altoSupplier,
            BiConsumer<String, MensajeEvento.TipoMensaje> notificador) {
        return new SubsistemaSimulacion(anchoMundo, altoMundo, anchoSupplier, altoSupplier, notificador);
    }
}
