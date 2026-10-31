package org.example.game.cuerpo;

import org.example.game.motor.Vector2D;

/**
 * Contrato de fábrica para creación desacoplada de cuerpos celestes (ISP / DIP).
 */
public interface FabricaCuerpos {

    CuerpoCeleste crearCuerpo(TipoCuerpo tipo, double x, double y, double factorMasa, Vector2D velocidadInicial);

    Planeta crearPlaneta(TipoCuerpo tipo, double x, double y, double factorMasa, Vector2D velocidadInicial);

    Planeta crearPlanetaPersonalizado(String nombre, double masa, double x, double y, TipoCuerpo tipo, Vector2D velocidadInicial);

    Planeta crearPlanetaEnOrbita(CuerpoCeleste central, double distancia, double factorMasa, TipoCuerpo tipo);

    Estrella crearEstrella(double x, double y, double factorMasa);

    Estrella crearEstrellaPersonalizada(String nombre, double masa, double x, double y);

    Luna crearLuna(double x, double y, double factorMasa, Vector2D velocidadInicial);

    Luna crearLunaPersonalizada(String nombre, double masa, double x, double y, Vector2D velocidadInicial);

    Satelite crearSatelite(double x, double y, double factorMasa, Vector2D velocidadInicial);

    AgujeroNegro crearAgujeroNegro(TipoCuerpo tipo, double x, double y, double factorMasa, Vector2D velocidadInicial);

    AgujeroNegro crearAgujeroNegroPersonalizado(double x, double y, double masa, Vector2D velocidadInicial);

    AgujeroNegro crearAgujeroNegroEstelar(double x, double y, double factorMasa);

    AgujeroNegro crearAgujeroNegroSupermasivo(double x, double y, double factorMasa);

    Meteorito crearMeteoritoHaciaObjetivo(double anchoMundo, double altoMundo, CuerpoCeleste objetivo, double factorProgreso);

    Meteorito crearMeteoritoJugadorFisica(double x, double y, Vector2D vel);

    Meteorito crearFragmentoColision(double x, double y, Vector2D velocidadBase, double masaFragmento);

    String generarNombre(String prefijo);

    void resetearContador();

    void asegurarContadorMinimo(long minimo);

    void registrarCreador(TipoCuerpo tipo, CreadorCuerpo creador);
}
