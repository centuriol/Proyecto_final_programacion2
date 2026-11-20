package org.example.game.cuerpo;

import org.example.game.motor.Vector2D;
import org.example.game.simulacion.CalculadorAmenazaMeteorito;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/**
 * SRP: Mantiene el mapa de creadores registrados por tipo de cuerpo y delega en subfábricas especializadas.
 */
public class RegistroCreadoresCuerpo {

    private final Map<TipoCuerpo, CreadorCuerpo> registro = new EnumMap<>(TipoCuerpo.class);
    private final FabricaPlanetas fabricaPlanetas;
    private final FabricaMeteoritos fabricaMeteoritos;

    public RegistroCreadoresCuerpo(Function<String, String> generadorNombres, CalculadorAmenazaMeteorito calculadorAmenaza) {
        this.fabricaPlanetas = new FabricaPlanetas(generadorNombres);
        this.fabricaMeteoritos = new FabricaMeteoritos(generadorNombres, calculadorAmenaza);
    }

    public void registrarPorDefecto(CuerpoCelesteFactory factory) {
        registro.put(TipoCuerpo.ESTRELLA, (x, y, fm, vel) -> { Estrella e = factory.crearEstrella(x, y, fm); e.setVelocidad(vel); return e; });
        registro.put(TipoCuerpo.PLANETA_ROCOSO, (x, y, fm, vel) -> fabricaPlanetas.crearPlaneta(TipoCuerpo.PLANETA_ROCOSO, x, y, fm, vel));
        registro.put(TipoCuerpo.PLANETA_AGUA, (x, y, fm, vel) -> fabricaPlanetas.crearPlaneta(TipoCuerpo.PLANETA_AGUA, x, y, fm, vel));
        registro.put(TipoCuerpo.PLANETA_LAVA, (x, y, fm, vel) -> fabricaPlanetas.crearPlaneta(TipoCuerpo.PLANETA_LAVA, x, y, fm, vel));
        registro.put(TipoCuerpo.PLANETA_GASEOSO, (x, y, fm, vel) -> fabricaPlanetas.crearPlaneta(TipoCuerpo.PLANETA_GASEOSO, x, y, fm, vel));
        registro.put(TipoCuerpo.PLANETA_HELADO, (x, y, fm, vel) -> fabricaPlanetas.crearPlaneta(TipoCuerpo.PLANETA_HELADO, x, y, fm, vel));
        registro.put(TipoCuerpo.LUNA, factory::crearLuna);
        registro.put(TipoCuerpo.SATELITE, factory::crearSatelite);
        registro.put(TipoCuerpo.METEORITO, (x, y, fm, vel) -> fabricaMeteoritos.crearLanzadoPorJugador(x, y, vel));
        registro.put(TipoCuerpo.AGUJERO_NEGRO, (x, y, fm, vel) -> factory.crearAgujeroNegro(TipoCuerpo.AGUJERO_NEGRO, x, y, fm, vel));
        registro.put(TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO, (x, y, fm, vel) -> factory.crearAgujeroNegro(TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO, x, y, fm, vel));
    }

    public void registrar(TipoCuerpo tipo, CreadorCuerpo creador) {
        if (tipo != null && creador != null) registro.put(tipo, creador);
    }

    public CuerpoCeleste crear(TipoCuerpo tipo, double x, double y, double factorMasa, Vector2D vel) {
        CreadorCuerpo creador = registro.get(tipo);
        return creador != null ? creador.crear(x, y, factorMasa, vel) : null;
    }

    public FabricaPlanetas getFabricaPlanetas() { return fabricaPlanetas; }
    public FabricaMeteoritos getFabricaMeteoritos() { return fabricaMeteoritos; }
}
