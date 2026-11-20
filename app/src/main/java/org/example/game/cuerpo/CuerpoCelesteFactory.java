package org.example.game.cuerpo;

import org.example.game.motor.Vector2D;
import org.example.game.simulacion.CalculadorAmenazaMeteorito;
import org.example.game.simulacion.FabricaSimulacion;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Fábrica instanciable e inyectable para la creación desacoplada de cuerpos celestes (SRP / OCP / DIP).
 */
public class CuerpoCelesteFactory implements FabricaCuerpos {

    private static final AtomicLong CONTADOR_ID = new AtomicLong(0);
    private static final FabricaSimulacion FABRICA_SIM = new FabricaSimulacion();
    private final RegistroCreadoresCuerpo registroCreadores;

    public CuerpoCelesteFactory() {
        this(FABRICA_SIM.crearCalculadorAmenaza());
    }

    public CuerpoCelesteFactory(CalculadorAmenazaMeteorito calculadorAmenaza) {
        this.registroCreadores = new RegistroCreadoresCuerpo(this::generarNombre, calculadorAmenaza);
        this.registroCreadores.registrarPorDefecto(this);
    }

    @Override public void registrarCreador(TipoCuerpo tipo, CreadorCuerpo creador) { registroCreadores.registrar(tipo, creador); }
    @Override public CuerpoCeleste crearCuerpo(TipoCuerpo tipo, double x, double y, double factorMasa, Vector2D vel) { return registroCreadores.crear(tipo, x, y, factorMasa, vel); }
    @Override public String generarNombre(String prefijo) { return prefijo + "-" + CONTADOR_ID.incrementAndGet(); }
    @Override public void resetearContador() { CONTADOR_ID.set(0); }
    @Override public void asegurarContadorMinimo(long minimo) { if (minimo > 0) CONTADOR_ID.updateAndGet(actual -> Math.max(actual, minimo)); }

    @Override public Planeta crearPlaneta(TipoCuerpo tipo, double x, double y, double fm, Vector2D vel) { return registroCreadores.getFabricaPlanetas().crearPlaneta(tipo, x, y, fm, vel); }
    public Planeta crearPlanetaRocoso(double x, double y, double fm, Vector2D vel) { return crearPlaneta(TipoCuerpo.PLANETA_ROCOSO, x, y, fm, vel); }
    public Planeta crearPlanetaAgua(double x, double y, double fm, Vector2D vel) { return crearPlaneta(TipoCuerpo.PLANETA_AGUA, x, y, fm, vel); }
    public Planeta crearPlanetaLava(double x, double y, double fm, Vector2D vel) { return crearPlaneta(TipoCuerpo.PLANETA_LAVA, x, y, fm, vel); }
    @Override public Planeta crearPlanetaPersonalizado(String nombre, double masa, double x, double y, TipoCuerpo tipo, Vector2D vel) { return registroCreadores.getFabricaPlanetas().crearPlanetaPersonalizado(nombre, masa, x, y, tipo, vel); }
    @Override public Planeta crearPlanetaEnOrbita(CuerpoCeleste central, double dist, double fm, TipoCuerpo tipo) { return registroCreadores.getFabricaPlanetas().crearPlanetaEnOrbita(central, dist, fm, tipo); }

    @Override public Estrella crearEstrella(double x, double y, double fm) { return new Estrella(generarNombre("Sol"), TipoCuerpo.ESTRELLA.masaBase * fm, x, y); }
    @Override public Estrella crearEstrellaPersonalizada(String nombre, double masa, double x, double y) { return new Estrella(nombre, masa, x, y); }

    @Override
    public Luna crearLuna(double x, double y, double fm, Vector2D vel) {
        Luna l = new Luna(generarNombre("Luna"), TipoCuerpo.LUNA.masaBase * fm, x, y);
        l.setVelocidad(vel);
        return l;
    }

    @Override
    public Luna crearLunaPersonalizada(String nombre, double masa, double x, double y, Vector2D vel) {
        Luna l = new Luna(nombre, masa, x, y);
        l.setVelocidad(vel);
        return l;
    }

    @Override
    public Satelite crearSatelite(double x, double y, double fm, Vector2D vel) {
        Satelite s = new Satelite(generarNombre("Sat"), TipoCuerpo.SATELITE.masaBase * fm, x, y);
        s.setVelocidad(vel);
        return s;
    }

    @Override
    public AgujeroNegro crearAgujeroNegro(TipoCuerpo tipo, double x, double y, double fm, Vector2D vel) {
        TipoCuerpo t = tipo != null ? tipo : TipoCuerpo.AGUJERO_NEGRO;
        AgujeroNegro bh = new AgujeroNegro(generarNombre("BH"), t.masaBase * fm, x, y, t);
        bh.setVelocidad(vel);
        return bh;
    }

    @Override
    public AgujeroNegro crearAgujeroNegroPersonalizado(double x, double y, double masa, Vector2D vel) {
        AgujeroNegro bh = new AgujeroNegro(generarNombre("AgujeroNegro"), masa, x, y, TipoCuerpo.AGUJERO_NEGRO);
        bh.setVelocidad(vel);
        return bh;
    }

    @Override public AgujeroNegro crearAgujeroNegroEstelar(double x, double y, double fm) { return new AgujeroNegro(generarNombre("BH"), TipoCuerpo.AGUJERO_NEGRO.masaBase * fm, x, y, TipoCuerpo.AGUJERO_NEGRO); }
    @Override public AgujeroNegro crearAgujeroNegroSupermasivo(double x, double y, double fm) { return new AgujeroNegro(generarNombre("SMBH"), TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO.masaBase * fm, x, y, TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO); }

    @Override public Meteorito crearMeteoritoHaciaObjetivo(double w, double h, CuerpoCeleste obj, double fp) { return registroCreadores.getFabricaMeteoritos().crearHaciaObjetivo(w, h, obj, fp); }
    @Override public Meteorito crearMeteoritoJugadorFisica(double x, double y, Vector2D vel) { return registroCreadores.getFabricaMeteoritos().crearLanzadoPorJugador(x, y, vel); }
    @Override public Meteorito crearFragmentoColision(double x, double y, Vector2D velBase, double masaFrag) { return registroCreadores.getFabricaMeteoritos().crearFragmento(x, y, velBase, masaFrag); }
}
