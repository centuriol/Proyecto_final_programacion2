package org.example.game.cuerpo;

import org.example.game.motor.Vector2D;
import org.example.game.simulacion.CalculadorAmenazaMeteorito;
import org.example.game.simulacion.EscaladorAmenazaMeteorito;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Fábrica instanciable e inyectable para la creación desacoplada de cuerpos celestes (SRP / OCP / DIP).
 */
public class CuerpoCelesteFactory implements FabricaCuerpos {

    private final Map<TipoCuerpo, CreadorCuerpo> registro = new EnumMap<>(TipoCuerpo.class);
    private static final AtomicLong contadorId = new AtomicLong(0);
    private final FabricaPlanetas fabricaPlanetas;
    private final FabricaMeteoritos fabricaMeteoritos;

    public CuerpoCelesteFactory() {
        this(new EscaladorAmenazaMeteorito());
    }

    public CuerpoCelesteFactory(CalculadorAmenazaMeteorito calculadorAmenaza) {
        this.fabricaPlanetas = new FabricaPlanetas(this::generarNombre);
        this.fabricaMeteoritos = new FabricaMeteoritos(this::generarNombre, calculadorAmenaza);
        inicializarCreadoresPorDefecto();
    }

    private void inicializarCreadoresPorDefecto() {
        registro.put(TipoCuerpo.ESTRELLA, (x, y, factorMasa, vel) -> {
            Estrella e = crearEstrella(x, y, factorMasa);
            e.setVelocidad(vel);
            return e;
        });
        registro.put(TipoCuerpo.PLANETA_ROCOSO, (x, y, factorMasa, vel) -> crearPlaneta(TipoCuerpo.PLANETA_ROCOSO, x, y, factorMasa, vel));
        registro.put(TipoCuerpo.PLANETA_AGUA, (x, y, factorMasa, vel) -> crearPlaneta(TipoCuerpo.PLANETA_AGUA, x, y, factorMasa, vel));
        registro.put(TipoCuerpo.PLANETA_LAVA, (x, y, factorMasa, vel) -> crearPlaneta(TipoCuerpo.PLANETA_LAVA, x, y, factorMasa, vel));
        registro.put(TipoCuerpo.PLANETA_GASEOSO, (x, y, factorMasa, vel) -> crearPlaneta(TipoCuerpo.PLANETA_GASEOSO, x, y, factorMasa, vel));
        registro.put(TipoCuerpo.PLANETA_HELADO, (x, y, factorMasa, vel) -> crearPlaneta(TipoCuerpo.PLANETA_HELADO, x, y, factorMasa, vel));
        registro.put(TipoCuerpo.LUNA, this::crearLuna);
        registro.put(TipoCuerpo.SATELITE, this::crearSatelite);
        registro.put(TipoCuerpo.METEORITO, (x, y, factorMasa, vel) -> crearMeteoritoJugadorFisica(x, y, vel));
        registro.put(TipoCuerpo.AGUJERO_NEGRO, (x, y, factorMasa, vel) -> crearAgujeroNegro(TipoCuerpo.AGUJERO_NEGRO, x, y, factorMasa, vel));
        registro.put(TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO, (x, y, factorMasa, vel) -> crearAgujeroNegro(TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO, x, y, factorMasa, vel));
    }

    @Override
    public void registrarCreador(TipoCuerpo tipo, CreadorCuerpo creador) {
        if (tipo != null && creador != null) {
            registro.put(tipo, creador);
        }
    }

    @Override
    public CuerpoCeleste crearCuerpo(TipoCuerpo tipo, double x, double y, double factorMasa, Vector2D velocidadInicial) {
        CreadorCuerpo creador = registro.get(tipo);
        return creador != null ? creador.crear(x, y, factorMasa, velocidadInicial) : null;
    }

    @Override
    public String generarNombre(String prefijo) {
        return prefijo + "-" + contadorId.incrementAndGet();
    }

    @Override
    public void resetearContador() {
        contadorId.set(0);
    }

    @Override
    public void asegurarContadorMinimo(long minimo) {
        if (minimo > 0) {
            contadorId.updateAndGet(actual -> Math.max(actual, minimo));
        }
    }

    @Override
    public Planeta crearPlaneta(TipoCuerpo tipo, double x, double y, double factorMasa, Vector2D velocidadInicial) {
        return fabricaPlanetas.crearPlaneta(tipo, x, y, factorMasa, velocidadInicial);
    }

    public Planeta crearPlanetaRocoso(double x, double y, double factorMasa, Vector2D velocidadInicial) {
        return crearPlaneta(TipoCuerpo.PLANETA_ROCOSO, x, y, factorMasa, velocidadInicial);
    }

    public Planeta crearPlanetaAgua(double x, double y, double factorMasa, Vector2D velocidadInicial) {
        return crearPlaneta(TipoCuerpo.PLANETA_AGUA, x, y, factorMasa, velocidadInicial);
    }

    public Planeta crearPlanetaLava(double x, double y, double factorMasa, Vector2D velocidadInicial) {
        return crearPlaneta(TipoCuerpo.PLANETA_LAVA, x, y, factorMasa, velocidadInicial);
    }

    @Override
    public Planeta crearPlanetaPersonalizado(String nombre, double masa, double x, double y, TipoCuerpo tipo, Vector2D velocidadInicial) {
        return fabricaPlanetas.crearPlanetaPersonalizado(nombre, masa, x, y, tipo, velocidadInicial);
    }

    @Override
    public Planeta crearPlanetaEnOrbita(CuerpoCeleste central, double distancia, double factorMasa, TipoCuerpo tipo) {
        return fabricaPlanetas.crearPlanetaEnOrbita(central, distancia, factorMasa, tipo);
    }

    @Override
    public Estrella crearEstrella(double x, double y, double factorMasa) {
        double masa = TipoCuerpo.ESTRELLA.masaBase * factorMasa;
        return new Estrella(generarNombre("Sol"), masa, x, y);
    }

    @Override
    public Estrella crearEstrellaPersonalizada(String nombre, double masa, double x, double y) {
        return new Estrella(nombre, masa, x, y);
    }

    @Override
    public Luna crearLuna(double x, double y, double factorMasa, Vector2D velocidadInicial) {
        double masa = TipoCuerpo.LUNA.masaBase * factorMasa;
        Luna l = new Luna(generarNombre("Luna"), masa, x, y);
        l.setVelocidad(velocidadInicial);
        return l;
    }

    @Override
    public Luna crearLunaPersonalizada(String nombre, double masa, double x, double y, Vector2D velocidadInicial) {
        Luna l = new Luna(nombre, masa, x, y);
        l.setVelocidad(velocidadInicial);
        return l;
    }

    @Override
    public Satelite crearSatelite(double x, double y, double factorMasa, Vector2D velocidadInicial) {
        double masa = TipoCuerpo.SATELITE.masaBase * factorMasa;
        Satelite s = new Satelite(generarNombre("Sat"), masa, x, y);
        s.setVelocidad(velocidadInicial);
        return s;
    }

    @Override
    public AgujeroNegro crearAgujeroNegro(TipoCuerpo tipo, double x, double y, double factorMasa, Vector2D velocidadInicial) {
        TipoCuerpo t = tipo != null ? tipo : TipoCuerpo.AGUJERO_NEGRO;
        AgujeroNegro bh = new AgujeroNegro(generarNombre("BH"), t.masaBase * factorMasa, x, y, t);
        bh.setVelocidad(velocidadInicial);
        return bh;
    }

    @Override
    public AgujeroNegro crearAgujeroNegroPersonalizado(double x, double y, double masa, Vector2D velocidadInicial) {
        AgujeroNegro bh = new AgujeroNegro(generarNombre("AgujeroNegro"), masa, x, y, TipoCuerpo.AGUJERO_NEGRO);
        bh.setVelocidad(velocidadInicial);
        return bh;
    }

    @Override
    public AgujeroNegro crearAgujeroNegroEstelar(double x, double y, double factorMasa) {
        double masa = TipoCuerpo.AGUJERO_NEGRO.masaBase * factorMasa;
        return new AgujeroNegro(generarNombre("BH"), masa, x, y, TipoCuerpo.AGUJERO_NEGRO);
    }

    @Override
    public AgujeroNegro crearAgujeroNegroSupermasivo(double x, double y, double factorMasa) {
        double masa = TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO.masaBase * factorMasa;
        return new AgujeroNegro(generarNombre("SMBH"), masa, x, y, TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO);
    }

    @Override
    public Meteorito crearMeteoritoHaciaObjetivo(double anchoMundo, double altoMundo, CuerpoCeleste objetivo, double factorProgreso) {
        return fabricaMeteoritos.crearHaciaObjetivo(anchoMundo, altoMundo, objetivo, factorProgreso);
    }

    @Override
    public Meteorito crearMeteoritoJugadorFisica(double x, double y, Vector2D vel) {
        return fabricaMeteoritos.crearLanzadoPorJugador(x, y, vel);
    }

    @Override
    public Meteorito crearFragmentoColision(double x, double y, Vector2D velocidadBase, double masaFragmento) {
        return fabricaMeteoritos.crearFragmento(x, y, velocidadBase, masaFragmento);
    }
}
