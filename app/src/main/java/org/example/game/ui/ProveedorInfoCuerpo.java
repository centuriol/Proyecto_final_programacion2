package org.example.game.ui;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.jugador.TipoRecurso;

import java.util.Map;

/**
 * Contrato para generación de secciones visuales modales e información descriptiva (ISP / DIP).
 */
public interface ProveedorInfoCuerpo {

    VBox inicializarDialogo(Stage stage, String titulo, Window owner);

    void vincularCierrePorEscape(Scene scene, Stage stage);

    Node crearNodoIcono(TipoCuerpo tipo);

    HBox crearHeader(Node iconoNodo, String titulo, String subtitulo);

    VBox crearSeccionDescripcion(TipoCuerpo tipo);

    VBox crearSeccionCaracteristicas(TipoCuerpo tipo);

    void agregarFilaPropiedad(GridPane grid, int fila, String clave, String valor, String colorValorHex);

    String obtenerNombrePorDefecto(TipoCuerpo tipo);

    String obtenerIcono(TipoCuerpo tipo);

    String obtenerSubtitulo(TipoCuerpo tipo);

    String obtenerLore(TipoCuerpo tipo);

    void poblarListaCostos(VBox contenedor, Map<TipoRecurso, Double> costos);

    void poblarListaProduccion(VBox contenedor, Map<TipoRecurso, Double> prod);
}
