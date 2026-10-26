package org.example.game.cuerpo;

import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Estrella;
import org.example.game.cuerpo.Planeta;
import org.example.game.cuerpo.AgujeroNegro;
import org.example.game.motor.Vector2D;
import org.example.game.motor.ConstantesFisicas;
import org.example.game.simulacion.ConfiguracionSimulacion;

import java.util.EnumMap;
import java.util.Map;

/**
 * Factory para creación dinámica de cuerpos celestes.
 * Centraliza la lógica de construcción con parámetros de juego.
 * OCP: Extensible mediante el registro de estrategias CreadorCuerpo sin modificar código existente.
 */
public final class CuerpoCelesteFactory {

    @FunctionalInterface
    public interface CreadorCuerpo {
        CuerpoCeleste crear(double x, double y, double factorMasa, Vector2D velocidadInicial);
    }

    private static final Map<TipoCuerpo, CreadorCuerpo> REGISTRO_CREADORES = new EnumMap<>(TipoCuerpo.class);
    private static long contadorId = 0;

    static {
        registrarCreador(TipoCuerpo.ESTRELLA, (x, y, factorMasa, vel) -> {
            Estrella e = crearEstrella(x, y, factorMasa);
            e.setVelocidad(vel);
            return e;
        });
        registrarCreador(TipoCuerpo.PLANETA_ROCOSO, CuerpoCelesteFactory::crearPlanetaRocoso);
        registrarCreador(TipoCuerpo.PLANETA_AGUA, CuerpoCelesteFactory::crearPlanetaAgua);
        registrarCreador(TipoCuerpo.PLANETA_LAVA, CuerpoCelesteFactory::crearPlanetaLava);
        registrarCreador(TipoCuerpo.PLANETA_GASEOSO, CuerpoCelesteFactory::crearPlanetaGaseoso);
        registrarCreador(TipoCuerpo.PLANETA_HELADO, CuerpoCelesteFactory::crearPlanetaHelado);
        registrarCreador(TipoCuerpo.LUNA, CuerpoCelesteFactory::crearLuna);
        registrarCreador(TipoCuerpo.SATELITE, CuerpoCelesteFactory::crearSatelite);
        registrarCreador(TipoCuerpo.METEORITO, (x, y, factorMasa, vel) -> crearMeteoritoJugadorFisica(x, y, vel));
        registrarCreador(TipoCuerpo.AGUJERO_NEGRO, (x, y, factorMasa, vel) -> {
            AgujeroNegro bh = crearAgujeroNegroEstelar(x, y, factorMasa);
            bh.setVelocidad(vel);
            return bh;
        });
        registrarCreador(TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO, (x, y, factorMasa, vel) -> {
            AgujeroNegro bh = crearAgujeroNegroSupermasivo(x, y, factorMasa);
            bh.setVelocidad(vel);
            return bh;
        });
    }

    public static void registrarCreador(TipoCuerpo tipo, CreadorCuerpo creador) {
        if (tipo != null && creador != null) {
            REGISTRO_CREADORES.put(tipo, creador);
        }
    }

    public static CuerpoCeleste crearCuerpo(TipoCuerpo tipo, double x, double y, double factorMasa, Vector2D velocidadInicial) {
        CreadorCuerpo creador = REGISTRO_CREADORES.get(tipo);
        if (creador != null) {
            return creador.crear(x, y, factorMasa, velocidadInicial);
        }
        return null;
    }

    private CuerpoCelesteFactory() {}

    private static String generarNombre(String prefijo) {
        return prefijo + "-" + (++contadorId);
    }

    // ===== Estrellas =====

    public static Estrella crearEstrella(double x, double y, double factorMasa) {
        double masa = TipoCuerpo.ESTRELLA.masaBase * factorMasa;
        return new Estrella(generarNombre("Sol"), masa, x, y);
    }

    public static Estrella crearEstrellaPersonalizada(String nombre, double masa, double x, double y) {
        return new Estrella(nombre, masa, x, y);
    }

    // ===== Planetas =====

    public static Planeta crearPlanetaRocoso(double x, double y, double factorMasa, Vector2D velocidadInicial) {
        double masa = TipoCuerpo.PLANETA_ROCOSO.masaBase * factorMasa;
        Planeta p = new Planeta(generarNombre("Planeta"), masa, x, y, TipoCuerpo.PLANETA_ROCOSO);
        p.setVelocidad(velocidadInicial);
        return p;
    }

    public static Planeta crearPlanetaPersonalizado(String nombre, double masa, double x, double y, TipoCuerpo tipo, Vector2D velocidadInicial) {
        Planeta p = new Planeta(nombre, masa, x, y, tipo);
        p.setVelocidad(velocidadInicial);
        return p;
    }

    public static Planeta crearPlanetaGaseoso(double x, double y, double factorMasa, Vector2D velocidadInicial) {
        double masa = TipoCuerpo.PLANETA_GASEOSO.masaBase * factorMasa;
        Planeta p = new Planeta(generarNombre("Gigante"), masa, x, y, TipoCuerpo.PLANETA_GASEOSO);
        p.setVelocidad(velocidadInicial);
        return p;
    }

    public static Planeta crearPlanetaAgua(double x, double y, double factorMasa, Vector2D velocidadInicial) {
        double masa = TipoCuerpo.PLANETA_AGUA.masaBase * factorMasa;
        Planeta p = new Planeta(generarNombre("Agua"), masa, x, y, TipoCuerpo.PLANETA_AGUA);
        p.setVelocidad(velocidadInicial);
        return p;
    }

    public static Planeta crearPlanetaLava(double x, double y, double factorMasa, Vector2D velocidadInicial) {
        double masa = TipoCuerpo.PLANETA_LAVA.masaBase * factorMasa;
        Planeta p = new Planeta(generarNombre("Lava"), masa, x, y, TipoCuerpo.PLANETA_LAVA);
        p.setVelocidad(velocidadInicial);
        return p;
    }

    public static Planeta crearPlanetaHelado(double x, double y, double factorMasa, Vector2D velocidadInicial) {
        double masa = TipoCuerpo.PLANETA_HELADO.masaBase * factorMasa;
        Planeta p = new Planeta(generarNombre("Helado"), masa, x, y, TipoCuerpo.PLANETA_HELADO);
        p.setVelocidad(velocidadInicial);
        return p;
    }

    /** Crea planeta en órbita circular aproximada alrededor de un cuerpo central */
    public static Planeta crearPlanetaEnOrbita(CuerpoCeleste central, double distancia, double factorMasa, TipoCuerpo tipo) {
        // Posición inicial: a la derecha del central (eje +X en coordenadas física)
        double x = central.getPosicionX() + distancia;
        double y = central.getPosicionY();

        // Velocidad orbital circular: v = sqrt(G * M / r)
        // Usar G_ESCALADO centralizado
        double G = ConstantesFisicas.G_ESCALADO;
        double vOrbital = Math.sqrt(G * central.getMasa() / distancia);

        // En coordenadas física (Y hacia arriba), órbita contrarreloj = -Y (hacia arriba en pantalla física)
        // La conversión a JavaFX se hace en el renderizador
        Vector2D vel = new Vector2D(0, -vOrbital);

        TipoCuerpo tipoPlaneta = (tipo != null && tipo.esPlaneta()) ? tipo : TipoCuerpo.PLANETA_ROCOSO;
        CuerpoCeleste c = crearCuerpo(tipoPlaneta, x, y, factorMasa, vel);
        return (c instanceof Planeta p) ? p : crearPlanetaRocoso(x, y, factorMasa, vel);
    }

    // ===== Satélites y Lunas =====

    public static Luna crearLuna(double x, double y, double factorMasa, Vector2D velocidadInicial) {
        double masa = TipoCuerpo.LUNA.masaBase * factorMasa;
        Luna l = new Luna(generarNombre("Luna"), masa, x, y);
        l.setVelocidad(velocidadInicial);
        return l;
    }

    public static Luna crearLunaPersonalizada(String nombre, double masa, double x, double y, Vector2D velocidadInicial) {
        Luna l = new Luna(nombre, masa, x, y);
        l.setVelocidad(velocidadInicial);
        return l;
    }

    public static Satelite crearSatelite(double x, double y, double factorMasa, Vector2D velocidadInicial) {
        double masa = TipoCuerpo.SATELITE.masaBase * factorMasa;
        Satelite s = new Satelite(generarNombre("Sat"), masa, x, y);
        s.setVelocidad(velocidadInicial);
        return s;
    }

    public static AgujeroNegro crearAgujeroNegroPersonalizado(double x, double y, double masa, Vector2D velocidadInicial) {
        AgujeroNegro bh = new AgujeroNegro(generarNombre("AgujeroNegro"), masa, x, y, TipoCuerpo.AGUJERO_NEGRO);
        bh.setVelocidad(velocidadInicial);
        return bh;
    }

    // ===== Agujeros Negros =====

    public static AgujeroNegro crearAgujeroNegroEstelar(double x, double y, double factorMasa) {
        double masa = TipoCuerpo.AGUJERO_NEGRO.masaBase * factorMasa;
        return new AgujeroNegro(generarNombre("BH"), masa, x, y, TipoCuerpo.AGUJERO_NEGRO);
    }

    public static AgujeroNegro crearAgujeroNegroSupermasivo(double x, double y, double factorMasa) {
        double masa = TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO.masaBase * factorMasa;
        return new AgujeroNegro(generarNombre("SMBH"), masa, x, y, TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO);
    }

    // ===== Meteoritos =====

    public static org.example.game.cuerpo.Meteorito crearMeteoritoAleatorio(double anchoMundo, double altoMundo) {
        return crearMeteoritoHaciaObjetivo(anchoMundo, altoMundo, null);
    }

    public static org.example.game.cuerpo.Meteorito crearMeteoritoHaciaObjetivo(double anchoMundo, double altoMundo, CuerpoCeleste objetivo) {
        double semiAncho = Math.max(400.0, anchoMundo / 2.0);
        double semiAlto = Math.max(300.0, altoMundo / 2.0);

        // Borde visible en coordenadas físicas (centro en (0,0))
        double lado = Math.random() * 4.0;
        double xFisica, yFisica;

        if (lado < 1.0) { // Izquierda
            xFisica = -semiAncho - 50.0;
            yFisica = (Math.random() * 2.0 - 1.0) * semiAlto;
        } else if (lado < 2.0) { // Derecha
            xFisica = semiAncho + 50.0;
            yFisica = (Math.random() * 2.0 - 1.0) * semiAlto;
        } else if (lado < 3.0) { // Arriba
            xFisica = (Math.random() * 2.0 - 1.0) * semiAncho;
            yFisica = semiAlto + 50.0;
        } else { // Abajo
            xFisica = (Math.random() * 2.0 - 1.0) * semiAncho;
            yFisica = -semiAlto - 50.0;
        }

        Vector2D posInicio = new Vector2D(xFisica, yFisica);
        Vector2D dir;
        if (objetivo != null) {
            dir = objetivo.getPosicion().restar(posInicio);
        } else {
            dir = Vector2D.cero().restar(posInicio);
        }

        double velMag = ConfiguracionSimulacion.VELOCIDAD_METEORITO_PX_TICK;
        Vector2D vel = (dir.magnitud() > 1e-3 ? dir.normalizar() : new Vector2D(1, 0)).multiplicar(velMag);

        // Vida suficiente para cruzar el mapa holgadamente (~ semiAncho * 3 / velMag)
        double ticksVida = Math.max(1200.0, (semiAncho + semiAlto) * 3.0 / Math.max(1.0, velMag));

        return new org.example.game.cuerpo.Meteorito(
                generarNombre("Meteorito"),
                TipoCuerpo.METEORITO.masaBase * (0.8 + Math.random() * 0.4),
                xFisica, yFisica,
                vel, ticksVida, false, objetivo
        );
    }

    public static org.example.game.cuerpo.Meteorito crearMeteoritoJugador(double x, double y, Vector2D direccion, double potencia) {
        Vector2D vel = direccion.normalizar().multiplicar(potencia);
        vel = new Vector2D(vel.x, -vel.y);
        double xFisica = ConstantesFisicas.javaFXAFisicaX(x, 1920);
        double yFisica = ConstantesFisicas.javaFXAFisica(y, 1080);

        return new org.example.game.cuerpo.Meteorito(
            generarNombre("Proyectil"),
            TipoCuerpo.METEORITO.masaBase * 0.1,
            xFisica, yFisica, vel, 200, true
        );
    }

    public static org.example.game.cuerpo.Meteorito crearMeteoritoJugadorFisica(double xFisica, double yFisica, Vector2D velocidadFisica) {
        return new org.example.game.cuerpo.Meteorito(
            generarNombre("Meteorito"),
            TipoCuerpo.METEORITO.masaBase * 0.1,
            xFisica, yFisica, velocidadFisica, 300, true
        );
    }

    /**
     * Crea un fragmento de escombro (meteorito) producto de una colisión destructiva.
     * No pasa por el inventario ni cuesta recursos: es una consecuencia física, no una compra.
     */
    public static Meteorito crearFragmentoEscombro(double xFisica, double yFisica,
                                                    Vector2D velocidadBase, double masaFragmento) {
        // Dispersión angular aleatoria alrededor de la velocidad base del cuerpo original
        double anguloExtra = (Math.random() - 0.5) * Math.PI; // +/- 90°
        double velBase = velocidadBase != null ? velocidadBase.magnitud() : 0.0;
        double velEyeccion = ConfiguracionSimulacion.VELOCIDAD_EYECCION_FRAGMENTOS
                * (0.5 + Math.random());

        double anguloFinal;
        if (velBase > 1e-3 && velocidadBase != null) {
            double anguloBase = Math.atan2(velocidadBase.y, velocidadBase.x);
            anguloFinal = anguloBase + anguloExtra;
        } else {
            anguloFinal = Math.random() * 2.0 * Math.PI;
        }

        Vector2D velFragmento = new Vector2D(
                Math.cos(anguloFinal) * (velBase * 0.3 + velEyeccion),
                Math.sin(anguloFinal) * (velBase * 0.3 + velEyeccion)
        );

        return new Meteorito(
                generarNombre("Escombro"),
                masaFragmento,
                xFisica, yFisica,
                velFragmento,
                250, // ticksVida — desaparecen solos, no acumulan objetos para siempre
                false
        );
    }

    // ===== Utilidades Orbitales Inteligentes =====

    /** Calcula la velocidad orbital tangencial exacta en cualquier posición 2D arbitraria respecto al centro */
    public static Vector2D calcularVelocidadOrbitalVector(CuerpoCeleste central, Vector2D posicion, boolean horario) {
        Vector2D r = posicion.restar(central.getPosicion());
        double distancia = r.magnitud();
        if (distancia < 1.0) return Vector2D.cero();

        double G = ConstantesFisicas.G_ESCALADO;
        double vOrbital = Math.sqrt(G * central.getMasa() / distancia);

        // Vector perpendicular unitario
        // En coordenadas física: (-ry, rx) gira antihorario; (ry, -rx) gira horario
        Vector2D unitarioRadial = r.dividir(distancia);
        Vector2D unitarioTangencial = horario
                ? new Vector2D(unitarioRadial.y, -unitarioRadial.x)
                : new Vector2D(-unitarioRadial.y, unitarioRadial.x);

        // Sumar velocidad propia del cuerpo central para orbitas relativas (ej. Luna orbitando Planeta que orbita Estrella)
        return central.getVelocidad().sumar(unitarioTangencial.multiplicar(vOrbital));
    }

    /** Calcula velocidad orbital circular simple para una distancia */
    public static Vector2D calcularVelocidadOrbital(CuerpoCeleste central, double distancia, boolean horario) {
        double G = ConstantesFisicas.G_ESCALADO;
        double v = Math.sqrt(G * central.getMasa() / distancia);
        return horario ? new Vector2D(0, v) : new Vector2D(0, -v);
    }

    public static void resetearContador() {
        contadorId = 0;
    }
}