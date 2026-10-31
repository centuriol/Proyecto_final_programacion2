package org.example.game.motor.colision;
import org.example.game.cuerpo.Civilizable;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.RegistroCapacidades;
import org.example.game.simulacion.ConfiguracionColisiones;
/**
 * Regla de colisión asimétrica donde el cuerpo menor es destruido y el mayor absorbe una fracción de su masa.
 */
public class ReglaColisionAsimetrica implements ReglaColision {

    private final UtilidadColision utilidadColision;

    public ReglaColisionAsimetrica() {
        this(new UtilidadColision());
    }

    public ReglaColisionAsimetrica(UtilidadColision utilidadColision) {
        this.utilidadColision = utilidadColision != null ? utilidadColision : new UtilidadColision();
    }

    @Override
    public boolean aplica(CuerpoCeleste a, CuerpoCeleste b) {
        return true; // Fallback por defecto para cualquier otro choque de cuerpos mayores
    }

    @Override
    public void resolver(CuerpoCeleste a, CuerpoCeleste b, ContextoColision ctx) {
        CuerpoCeleste mayor = a.getMasa() >= b.getMasa() ? a : b;
        CuerpoCeleste menor = (mayor == a) ? b : a;

        double masaGanada = menor.getMasa() * ConfiguracionColisiones.PORCENTAJE_GANANCIA_MASA_COLISION;
        mayor.setMasa(mayor.getMasa() + masaGanada);
        utilidadColision.generarEscombros(menor, ctx.aEliminar(), ctx.aAgregar());

        RegistroCapacidades.obtenerCivilizable(mayor).ifPresent(civ -> {
            if (civ.tieneCivilizacion() && civ.getCivilizacion() != null) {
                civ.getCivilizacion().recibirImpactoMeteorito();
            }
        });

        ctx.emitirMensaje("Impacto destructivo asimétrico: " + menor.getNombre() + " destruido al colisionar con " + mayor.getNombre() + ".");
    }
}
