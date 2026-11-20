package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.example.game.cuerpo.CatalogoDescriptoresCuerpo;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.jugador.TipoRecurso;
import org.example.game.render.RepositorioSprites;

import java.util.Map;

/**
 * Secciones visuales reutilizables para ventanas modales de información y configuración de cuerpos celestes (SRP / DIP).
 */
public class SeccionInfoCuerpo implements ProveedorInfoCuerpo {

    private static final ConstructorPanelCaracteristicas PANEL_CARACTERISTICAS = new ConstructorPanelCaracteristicas();
    private static final FabricaUI FABRICA_UI = new FabricaUI();

    private final RepositorioSprites repositorioSprites;

    public SeccionInfoCuerpo() {
        this(FABRICA_UI.crearRepositorioSprites());
    }

    public SeccionInfoCuerpo(RepositorioSprites repositorioSprites) {
        this.repositorioSprites = repositorioSprites != null ? repositorioSprites : FABRICA_UI.crearRepositorioSprites();
    }

    @Override
    public VBox inicializarDialogo(Stage stage, String titulo, Window owner) {
        stage.setTitle(titulo);
        if (owner != null) {
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
        }
        VBox root = new VBox(12);
        root.setPadding(new Insets(16));
        root.setStyle(TemaUI.CSS_MODAL_ROOT);
        return root;
    }

    @Override
    public void vincularCierrePorEscape(Scene scene, Stage stage) {
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) stage.close();
        });
    }

    @Override
    public Node crearNodoIcono(TipoCuerpo tipo) {
        if (repositorioSprites.tieneSprite(tipo)) {
            StackPane imgBox = new StackPane(repositorioSprites.crearVistaIcono(tipo, 44, 44));
            imgBox.setPrefSize(48, 48);
            imgBox.setStyle("-fx-background-color: #0d0e15; -fx-border-color: #5be3ff; -fx-border-width: 1.5px; -fx-background-radius: 4px; -fx-border-radius: 4px;");
            return imgBox;
        }
        Label lblIcono = new Label(obtenerIcono(tipo));
        lblIcono.setFont(Font.font("Monospace", FontWeight.BOLD, 22));
        lblIcono.setTextFill(Color.web(tipo != null ? tipo.getColorHexString() : "#ffffff"));
        return lblIcono;
    }

    @Override
    public HBox crearHeader(Node iconoNodo, String titulo, String subtitulo) {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label lblTitulo = new Label(titulo.toUpperCase());
        lblTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 18));
        lblTitulo.setTextFill(TemaUI.COLOR_TEXTO_TITULO);
        Label lblSubtitulo = new Label(subtitulo);
        lblSubtitulo.setFont(Font.font("System", FontWeight.NORMAL, 12));
        lblSubtitulo.setTextFill(TemaUI.COLOR_TEXTO_MUTED);
        header.getChildren().addAll(iconoNodo, new VBox(2, lblTitulo, lblSubtitulo));
        return header;
    }

    @Override
    public VBox crearSeccionDescripcion(TipoCuerpo tipo) {
        Label lblDescTitulo = new Label("DESCRIPCIÓN DEL CUERPO");
        lblDescTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblDescTitulo.setTextFill(TemaUI.COLOR_ACENTO_CYAN);
        Label lblDescripcion = new Label(obtenerLore(tipo));
        lblDescripcion.setFont(Font.font("System", FontWeight.NORMAL, 12));
        lblDescripcion.setTextFill(Color.web("#d1d5db"));
        lblDescripcion.setWrapText(true);
        lblDescripcion.setLineSpacing(2);
        return new VBox(4, lblDescTitulo, lblDescripcion);
    }

    @Override public VBox crearSeccionCaracteristicas(TipoCuerpo tipo) { return PANEL_CARACTERISTICAS.crearSeccionCaracteristicas(tipo); }
    @Override public void agregarFilaPropiedad(GridPane grid, int fila, String clave, String valor, String colorValorHex) { PANEL_CARACTERISTICAS.agregarFilaPropiedad(grid, fila, clave, valor, colorValorHex); }
    @Override public String obtenerNombrePorDefecto(TipoCuerpo tipo) { return tipo != null ? CatalogoDescriptoresCuerpo.obtenerDescriptor(tipo).nombrePorDefecto() : "Cuerpo"; }
    @Override public String obtenerIcono(TipoCuerpo tipo) { return tipo != null ? CatalogoDescriptoresCuerpo.obtenerDescriptor(tipo).icono() : "[???]"; }
    @Override public String obtenerSubtitulo(TipoCuerpo tipo) { return tipo != null ? CatalogoDescriptoresCuerpo.obtenerDescriptor(tipo).subtitulo() : "Objeto Astronomico"; }
    @Override public String obtenerLore(TipoCuerpo tipo) { return tipo != null ? CatalogoDescriptoresCuerpo.obtenerDescriptor(tipo).lore() : "Cuerpo celeste en el sistema."; }
    @Override public void poblarListaCostos(VBox contenedor, Map<TipoRecurso, Double> costos) { PANEL_CARACTERISTICAS.poblarListaCostos(contenedor, costos); }
    @Override public void poblarListaProduccion(VBox contenedor, Map<TipoRecurso, Double> prod) { PANEL_CARACTERISTICAS.poblarListaProduccion(contenedor, prod); }
}
