package org.example.game.cuerpo;
/**
 * Contrato de doble despacho polimorfico (patron Visitor) sobre la jerarquia CuerpoCeleste.
 * Permite agregar operaciones externas (como extraccion de datos de persistencia)
 * sin utilizar cadenas de instanceof ni acoplar el dominio a formatos externos (OCP / DIP).
 *
 * @param <R> Tipo del resultado producido por la visita.
 */
public interface VisitanteCuerpo<R> {

    default R visitarEstrella(Estrella estrella) { return null; }

    default R visitarPlaneta(Planeta planeta) { return null; }

    default R visitarLuna(Luna luna) { return null; }

    default R visitarSatelite(Satelite satelite) { return null; }

    default R visitarMeteorito(Meteorito meteorito) { return null; }

    default R visitarAgujeroNegro(AgujeroNegro agujeroNegro) { return null; }
}
