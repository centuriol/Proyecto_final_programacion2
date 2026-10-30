package org.example.game.cuerpo;

import org.example.game.motor.CalculadorOrbital;
import org.example.game.motor.Vector2D;
import org.example.game.simulacion.ConfiguracionSimulacion;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Fábrica instanciable y configurable para creación dinámica de cuerpos celestes.
 * Proporciona registro de estrategias CreadorCuerpo y métodos estáticos de conveniencia (OCP / DIP).
 */
public class CuerpoCelesteFactory {

    @FunctionalInterface
    public interface CreadorCuerpo {
        CuerpoCeleste crear(double x, double y, double factorMasa, Vector2D velocidadInicial);
    }

    private final Map<TipoCuerpo, CreadorCuerpo> registro = new EnumMap<>(TipoCuerpo.class);
    private final AtomicLong contadorId = new AtomicLong(0);

    private static final CuerpoCelesteFactory INSTANCIA_GLOBAL = new CuerpoCelesteFactory();

    public CuerpoCelesteFactory() {
        inicializarCreadoresPorDefecto();
    }

    public static CuerpoCelesteFactory getInstancia() {
        return INSTANCIA_GLOBAL;
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
        registro.put(TipoCuerpo.LUNA, this::crearLunaInstancia);
        registro.put(TipoCuerpo.SATELITE, this::crearSateliteInstancia);
        registro.put(TipoCuerpo.METEORITO, (x, y, factorMasa, vel) -> crearMeteoritoJugadorFisica(x, y, vel));
        registro.put(TipoCuerpo.AGUJERO_NEGRO, (x, y, factorMasa, vel) -> {
            AgujeroNegro bh = crearAgujeroNegroEstelar(x, y, factorMasa);
            bh.setVelocidad(vel);
            return bh;
        });
        registro.put(TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO, (x, y, factorMasa, vel) -> {
            AgujeroNegro bh = crearAgujeroNegroSupermasivo(x, y, factorMasa);
            bh.setVelocidad(vel);
            return bh;
        });
    }

    public void registrar(TipoCuerpo tipo, CreadorCuerpo creador) {
        if (tipo != null && creador != null) {
            registro.put(tipo, creador);
        }
    }

    public CuerpoCeleste fabricar(TipoCuerpo tipo, double x, double y, double factorMasa, Vector2D velocidadInicial) {
        CreadorCuerpo creador = registro.get(tipo);
        return creador != null ? creador.crear(x, y, factorMasa, velocidadInicial) : null;
    }

    public String generarNombre(String prefijo) {
        return prefijo + "-" + contadorId.incrementAndGet();
    }

    public void resetear() {
        contadorId.set(0);
    }

    // ===== Métodos Estáticos Delegados =====

    public static void registrarCreador(TipoCuerpo tipo, CreadorCuerpo creador) {
        INSTANCIA_GLOBAL.registrar(tipo, creador);
    }

    public static CuerpoCeleste crearCuerpo(TipoCuerpo tipo, double x, double y, double factorMasa, Vector2D velocidadInicial) {
        return INSTANCIA_GLOBAL.fabricar(tipo, x, y, factorMasa, velocidadInicial);
    }

    public static void resetearContador() {
        INSTANCIA_GLOBAL.resetear();
    }

    // ===== Planetas unificados =====

    public static Planeta crearPlaneta(TipoCuerpo tipo, double x, double y, double factorMasa, Vector2D velocidadInicial) {
        TipoCuerpo tipoEfectivo = (tipo != null && tipo.esPlaneta()) ? tipo : TipoCuerpo.PLANETA_ROCOSO;
        double masa = tipoEfectivo.masaBase * factorMasa;
        Planeta p = new Planeta(INSTANCIA_GLOBAL.generarNombre("Planeta"), masa, x, y, tipoEfectivo);
        p.setVelocidad(velocidadInicial);
        return p;
    }

    public static Planeta crearPlanetaRocoso(double x, double y, double factorMasa, Vector2D velocidadInicial) {
        return crearPlaneta(TipoCuerpo.PLANETA_ROCOSO, x, y, factorMasa, velocidadInicial);
    }

    public static Planeta crearPlanetaAgua(double x, double y, double factorMasa, Vector2D velocidadInicial) {
        return crearPlaneta(TipoCuerpo.PLANETA_AGUA, x, y, factorMasa, velocidadInicial);
    }

    public static Planeta crearPlanetaLava(double x, double y, double factorMasa, Vector2D velocidadInicial) {
        return crearPlaneta(TipoCuerpo.PLANETA_LAVA, x, y, factorMasa, velocidadInicial);
    }

    public static Planeta crearPlanetaGaseoso(double x, double y, double factorMasa, Vector2D velocidadInicial) {
        return crearPlaneta(TipoCuerpo.PLANETA_GASEOSO, x, y, factorMasa, velocidadInicial);
    }

    public static Planeta crearPlanetaHelado(double x, double y, double factorMasa, Vector2D velocidadInicial) {
        return crearPlaneta(TipoCuerpo.PLANETA_HELADO, x, y, factorMasa, velocidadInicial);
    }

    public static Planeta crearPlanetaPersonalizado(String nombre, double masa, double x, double y, TipoCuerpo tipo, Vector2D velocidadInicial) {
        Planeta p = new Planeta(nombre, masa, x, y, tipo);
        p.setVelocidad(velocidadInicial);
        return p;
    }

    public static Planeta crearPlanetaEnOrbita(CuerpoCeleste central, double distancia, double factorMasa, TipoCuerpo tipo) {
        double x = central.getPosicionX() + distancia;
        double y = central.getPosicionY();
        double vOrbital = CalculadorOrbital.calcularRapidezOrbital(central.getMasa(), distancia);
        Vector2D vel = new Vector2D(0, -vOrbital);
        TipoCuerpo tipoPlaneta = (tipo != null && tipo.esPlaneta()) ? tipo : TipoCuerpo.PLANETA_ROCOSO;
        CuerpoCeleste c = crearCuerpo(tipoPlaneta, x, y, factorMasa, vel);
        return (c instanceof Planeta p) ? p : crearPlanetaRocoso(x, y, factorMasa, vel);
    }

    // ===== Estrellas =====

    public static Estrella crearEstrella(double x, double y, double factorMasa) {
        double masa = TipoCuerpo.ESTRELLA.masaBase * factorMasa;
        return new Estrella(INSTANCIA_GLOBAL.generarNombre("Sol"), masa, x, y);
    }

    public static Estrella crearEstrellaPersonalizada(String nombre, double masa, double x, double y) {
        return new Estrella(nombre, masa, x, y);
    }

    // ===== Satélites y Lunas =====

    private Luna crearLunaInstancia(double x, double y, double factorMasa, Vector2D velocidadInicial) {
        double masa = TipoCuerpo.LUNA.masaBase * factorMasa;
        Luna l = new Luna(generarNombre("Luna"), masa, x, y);
        l.setVelocidad(velocidadInicial);
        return l;
    }

    public static Luna crearLuna(double x, double y, double factorMasa, Vector2D velocidadInicial) {
        return INSTANCIA_GLOBAL.crearLunaInstancia(x, y, factorMasa, velocidadInicial);
    }

    public static Luna crearLunaPersonalizada(String nombre, double masa, double x, double y, Vector2D velocidadInicial) {
        Luna l = new Luna(nombre, masa, x, y);
        l.setVelocidad(velocidadInicial);
        return l;
    }

    private Satelite crearSateliteInstancia(double x, double y, double factorMasa, Vector2D velocidadInicial) {
        double masa = TipoCuerpo.SATELITE.masaBase * factorMasa;
        Satelite s = new Satelite(generarNombre("Sat"), masa, x, y);
        s.setVelocidad(velocidadInicial);
        return s;
    }

    public static Satelite crearSatelite(double x, double y, double factorMasa, Vector2D velocidadInicial) {
        return INSTANCIA_GLOBAL.crearSateliteInstancia(x, y, factorMasa, velocidadInicial);
    }

    // ===== Agujeros Negros =====

    public static AgujeroNegro crearAgujeroNegroPersonalizado(double x, double y, double masa, Vector2D velocidadInicial) {
        AgujeroNegro bh = new AgujeroNegro(INSTANCIA_GLOBAL.generarNombre("AgujeroNegro"), masa, x, y, TipoCuerpo.AGUJERO_NEGRO);
        bh.setVelocidad(velocidadInicial);
        return bh;
    }

    public static AgujeroNegro crearAgujeroNegroEstelar(double x, double y, double factorMasa) {
        double masa = TipoCuerpo.AGUJERO_NEGRO.masaBase * factorMasa;
        return new AgujeroNegro(INSTANCIA_GLOBAL.generarNombre("BH"), masa, x, y, TipoCuerpo.AGUJERO_NEGRO);
    }

    public static AgujeroNegro crearAgujeroNegroSupermasivo(double x, double y, double factorMasa) {
        double masa = TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO.masaBase * factorMasa;
        return new AgujeroNegro(INSTANCIA_GLOBAL.generarNombre("SMBH"), masa, x, y, TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO);
    }

    // ===== Meteoritos =====

    public static Meteorito crearMeteoritoAleatorio(double anchoMundo, double altoMundo) {
        return crearMeteoritoHaciaObjetivo(anchoMundo, altoMundo, null);
    }

    public static Meteorito crearMeteoritoHaciaObjetivo(double anchoMundo, double altoMundo, CuerpoCeleste objetivo) {
        double semiAncho = Math.max(400.0, anchoMundo / 2.0);
        double semiAlto = Math.max(300.0, altoMundo / 2.0);

        double lado = Math.random() * 4.0;
        double xFisica, yFisica;

        if (lado < 1.0) {
            xFisica = -semiAncho - 50.0;
            yFisica = (Math.random() * 2.0 - 1.0) * semiAlto;
        } else if (lado < 2.0) {
            xFisica = semiAncho + 50.0;
            yFisica = (Math.random() * 2.0 - 1.0) * semiAlto;
        } else if (lado < 3.0) {
            xFisica = (Math.random() * 2.0 - 1.0) * semiAncho;
            yFisica = semiAlto + 50.0;
        } else {
            xFisica = (Math.random() * 2.0 - 1.0) * semiAncho;
            yFisica = -semiAlto - 50.0;
        }

        Vector2D posInicio = new Vector2D(xFisica, yFisica);
        Vector2D dir = (objetivo != null)
                ? objetivo.getPosicion().restar(posInicio)
                : Vector2D.cero().restar(posInicio);

        double velMag = ConfiguracionSimulacion.VELOCIDAD_METEORITO_PX_TICK;
        Vector2D vel = (dir.magnitud() > 1e-3 ? dir.normalizar() : new Vector2D(1, 0)).multiplicar(velMag);
        double ticksVida = Math.max(1200.0, (semiAncho + semiAlto) * 3.0 / Math.max(1.0, velMag));

        return new Meteorito(
                INSTANCIA_GLOBAL.generarNombre("Meteorito"),
                TipoCuerpo.METEORITO.masaBase * (0.8 + Math.random() * 0.4),
                posInicio.x(), posInicio.y(),
                vel, ticksVida, false, objetivo
        );
    }

    public static Meteorito crearMeteoritoJugadorFisica(double x, double y, Vector2D vel) {
        vel = new Vector2D(vel.x(), -vel.y());
        return new Meteorito(
                INSTANCIA_GLOBAL.generarNombre("Meteorito"),
                TipoCuerpo.METEORITO.masaBase * 0.1,
                x, y, vel, 2000, true
        );
    }

    public static Meteorito crearFragmentoColision(double x, double y, Vector2D velocidadBase, double masaFragmento) {
        return crearFragmentoEscombro(x, y, velocidadBase, masaFragmento);
    }

    public static Meteorito crearFragmentoEscombro(double xFisica, double yFisica,
                                                    Vector2D velocidadBase, double masaFragmento) {
        double anguloExtra = (Math.random() - 0.5) * Math.PI;
        double velBase = velocidadBase != null ? velocidadBase.magnitud() : 0.0;
        double velEyeccion = ConfiguracionSimulacion.VELOCIDAD_EYECCION_FRAGMENTOS * (0.5 + Math.random());

        double anguloFinal;
        if (velBase > 1e-3 && velocidadBase != null) {
            double anguloBase = Math.atan2(velocidadBase.y(), velocidadBase.x());
            anguloFinal = anguloBase + anguloExtra;
        } else {
            anguloFinal = Math.random() * 2.0 * Math.PI;
        }

        Vector2D velFragmento = new Vector2D(
                Math.cos(anguloFinal) * (velBase * 0.3 + velEyeccion),
                Math.sin(anguloFinal) * (velBase * 0.3 + velEyeccion)
        );

        return new Meteorito(
                INSTANCIA_GLOBAL.generarNombre("Escombro"),
                masaFragmento,
                xFisica, yFisica,
                velFragmento,
                250,
                false
        );
    }

    // ===== Utilidades Orbitales =====

    public static Vector2D calcularVelocidadOrbitalVector(CuerpoCeleste central, Vector2D posicion, boolean horario) {
        return CalculadorOrbital.calcularVelocidadOrbitalVector(central, posicion, horario);
    }

    public static Vector2D calcularVelocidadOrbital(CuerpoCeleste central, double distancia, boolean horario) {
        double v = CalculadorOrbital.calcularRapidezOrbital(central.getMasa(), distancia);
        return horario ? new Vector2D(0, v) : new Vector2D(0, -v);
    }
}
