package org.example;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.example.game.motor.ConstantesFisicas;

/**
 * SRP: esta clase solo sabe transformar un CuerpoCeleste en un Circle
 * y mantenerlo actualizado. No sabe nada de física ni de Timeline.
 * Usa ConstantesFisicas para conversión de coordenadas física -> JavaFX.
 */
public class RenderizadorCircular implements RenderizadorCuerpo {
    private double anchoMundo;
    private double altoMundo;

    public RenderizadorCircular() {
        this(800, 600);
    }

    public RenderizadorCircular(double anchoMundo, double altoMundo) {
        this.anchoMundo = anchoMundo;
        this.altoMundo = altoMundo;
    }

    public void setDimensionesMundo(double anchoMundo, double altoMundo) {
        this.anchoMundo = anchoMundo;
        this.altoMundo = altoMundo;
    }

    @Override
    public Node crearNodo(CuerpoCeleste cuerpo) {
        Circle circulo = new Circle(cuerpo.getRadio(), Color.web(cuerpo.getColorHex()));
        double xJavaFX = ConstantesFisicas.fisicaAXJavaFX(cuerpo.getPosicionX(), anchoMundo);
        double yJavaFX = ConstantesFisicas.fisicaAYJavaFX(cuerpo.getPosicionY(), altoMundo);
        circulo.setCenterX(xJavaFX);
        circulo.setCenterY(yJavaFX);
        return circulo;
    }

    @Override
    public void actualizarNodo(Node nodo, CuerpoCeleste cuerpo) {
        Circle circulo = (Circle) nodo;
        double xJavaFX = ConstantesFisicas.fisicaAXJavaFX(cuerpo.getPosicionX(), anchoMundo);
        double yJavaFX = ConstantesFisicas.fisicaAYJavaFX(cuerpo.getPosicionY(), altoMundo);
        circulo.setCenterX(xJavaFX);
        circulo.setCenterY(yJavaFX);
        circulo.setFill(Color.web(cuerpo.getColorHex())); // por si cambia (ej: civilización)
    }
}
