package org.example.game.cuerpo;
/**
 * Contrato de doble despacho polimorfico (patron Visitor) sobre la jerarquia CuerpoCeleste.
 * Permite agregar operaciones externas (como extraccion de datos de persistencia)
 * sin utilizar cadenas de instanceof ni acoplar el dominio a formatos externos (OCP / DIP).
 *
 * @param <R> Tipo del resultado producido por la visita.
 */
public interface VisitanteCuerpo<R> {

    R visitarEstrella(Estrella estrella);

    R visitarPlaneta(Planeta planeta);

    R visitarLuna(Luna luna);

    R visitarSatelite(Satelite satelite);

    R visitarMeteorito(Meteorito meteorito);

    R visitarAgujeroNegro(AgujeroNegro agujeroNegro);
}
