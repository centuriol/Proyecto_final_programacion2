package org.example.game.guardado;

import java.util.Set;

/**
 * Abstracción para codificación y decodificación de entidades de partida en diccionarios de propiedades (DIP / ISP).
 */
public interface CodificadorEntidades {
    void escribirCuerpo(DiccionarioPropiedades dic, int indice, DatosCuerpo cuerpo);
    DatosCuerpo leerCuerpo(DiccionarioPropiedades dic, int indice);
    void escribirNave(DiccionarioPropiedades dic, int indice, DatosNaveColonizadora nave);
    DatosNaveColonizadora leerNave(DiccionarioPropiedades dic, int indice);
    void escribirMejorasSecundarias(DiccionarioPropiedades dic, String clave, Set<String> mejoras);
    Set<String> leerMejorasSecundarias(DiccionarioPropiedades dic, String clave);
}
