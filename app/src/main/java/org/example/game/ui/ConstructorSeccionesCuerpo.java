package org.example.game.ui;

import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.jugador.TipoRecurso;

import java.util.Map;

/**
 * Capacidad segregada para construir secciones visuales descriptivas y listas de recursos de cuerpos (ISP).
 */
public interface ConstructorSeccionesCuerpo {
    Node crearNodoIcono(TipoCuerpo tipo);
    VBox crearSeccionDescripcion(TipoCuerpo tipo);
    VBox crearSeccionCaracteristicas(TipoCuerpo tipo);
    void poblarListaCostos(VBox contenedor, Map<TipoRecurso, Double> costos);
    void poblarListaProduccion(VBox contenedor, Map<TipoRecurso, Double> prod);
}
