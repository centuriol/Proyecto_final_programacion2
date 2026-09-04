package org.example.game.cuerpo;

import org.example.CuerpoCeleste;
import org.example.Estrella;
import org.example.Planeta;
import org.example.AgujeroNegro;
import org.example.game.motor.Vector2D;
import org.example.game.motor.ConstantesFisicas;
import org.example.game.simulacion.ConfiguracionSimulacion;

/**
 * Factory para creación dinámica de cuerpos celestes.
 * Centraliza la lógica de construcción con parámetros de juego.
 */
public final class CuerpoCelesteFactory {

    private static long contadorId = 0;

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

        Planeta p;
        switch (tipo) {
            case PLANETA_GASEOSO:
                p = crearPlanetaGaseoso(x, y, factorMasa, vel);
                break;
            case PLANETA_HELADO:
                p = crearPlanetaHelado(x, y, factorMasa, vel);
                break;
            default:
                p = crearPlanetaRocoso(x, y, factorMasa, vel);
        }
        return p;
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

    public static EscudoProtector crearEscudo(double x, double y, double factorMasa, Vector2D velocidadInicial) {
        double masa = TipoCuerpo.ESCUDO_DOME.masaBase * factorMasa;
        EscudoProtector e = new EscudoProtector(generarNombre("Escudo"), masa, x, y);
        e.setVelocidad(velocidadInicial);
        return e;
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
        double lado = Math.random() * 4;
        double x, y;
        Vector2D vel;

        if (lado < 1) { // Izquierda
            x = -100; y = Math.random() * altoMundo;
            vel = new Vector2D(1000 + Math.random() * 5000, (Math.random() - 0.5) * 2000);
        } else if (lado < 2) { // Derecha
            x = anchoMundo + 100; y = Math.random() * altoMundo;
            vel = new Vector2D(-1000 - Math.random() * 5000, (Math.random() - 0.5) * 2000);
        } else if (lado < 3) { // Arriba
            x = Math.random() * anchoMundo; y = -100;
            vel = new Vector2D((Math.random() - 0.5) * 2000, 1000 + Math.random() * 5000);
        } else { // Abajo
            x = Math.random() * anchoMundo; y = altoMundo + 100;
            vel = new Vector2D((Math.random() - 0.5) * 2000, -1000 - Math.random() * 5000);
        }

        double xFisica = ConstantesFisicas.javaFXAFisicaX(x, anchoMundo);
        double yFisica = ConstantesFisicas.javaFXAFisica(y, altoMundo);
        vel = new Vector2D(vel.x, -vel.y);

        return new org.example.game.cuerpo.Meteorito(xFisica, yFisica, vel);
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