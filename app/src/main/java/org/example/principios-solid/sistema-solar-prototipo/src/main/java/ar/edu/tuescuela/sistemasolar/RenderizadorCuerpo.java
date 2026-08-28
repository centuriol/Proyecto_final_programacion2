package org.example;

import javafx.scene.Node;

/**
 * DIP + ISP: la vista depende de esta interfaz, no de una forma concreta
 * de dibujar. Mañana se puede cambiar de círculos a imágenes (sprites)
 * implementando otra clase, sin tocar VistaSistemaSolar.
 */
public interface RenderizadorCuerpo {
    Node crearNodo(CuerpoCeleste cuerpo);
    void actualizarNodo(Node nodo, CuerpoCeleste cuerpo);
}
