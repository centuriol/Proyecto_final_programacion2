package org.example;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.HashMap;
import java.util.Map;

/**
 * SRP: esta clase solo sincroniza el modelo (SistemaSolar) con el árbol
 * visual (Pane). No calcula física, no crea la ventana.
 * DIP: recibe el RenderizadorCuerpo por constructor, no decide ella
 * misma cómo se dibuja cada cuerpo.
 */
public class VistaSistemaSolar {
    private final Pane pane = new Pane();
    private final Map<CuerpoCeleste, Node> nodos = new HashMap<>();
    private final RenderizadorCuerpo renderizador;

    public VistaSistemaSolar(RenderizadorCuerpo renderizador) {
        this.renderizador = renderizador;
    }

    public Pane getPane() {
        return pane;
    }

    public void agregarCuerpo(CuerpoCeleste cuerpo) {
        Node nodo = renderizador.crearNodo(cuerpo);
        nodos.put(cuerpo, nodo);
        pane.getChildren().add(nodo);
    }

    /** Elimina un cuerpo de la vista. */
    public void removerCuerpo(CuerpoCeleste cuerpo) {
        Node nodo = nodos.remove(cuerpo);
        if (nodo != null) {
            pane.getChildren().remove(nodo);
        }
    }

    /** Se llama en cada paso de la simulación para redibujar todo. */
    public void actualizar() {
        for (Map.Entry<CuerpoCeleste, Node> entrada : nodos.entrySet()) {
            renderizador.actualizarNodo(entrada.getValue(), entrada.getKey());
        }
    }
}
