package org.example.game.cuerpo;

/**
 * Cuerpos celestes susceptibles de metamorfosis astrofísica (ISP / OCP).
 */
public interface Transformable {
    boolean esTransformable();
    void transformarA(TipoCuerpo nuevoTipo);
    boolean isTransformadoPorEvento();
    void setTransformadoPorEvento(boolean transformado);
}
