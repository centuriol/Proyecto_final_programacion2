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
import org.example.game.render.GestorImagenes;
import org.example.game.render.RepositorioSprites;

import java.util.Map;

/**
 * Secciones visuales reutilizables para ventanas modales de información y configuración de cuerpos celestes (SRP / DIP).
 */
public class SeccionInfoCuerpo implements ProveedorInfoCuerpo {

    private final RepositorioSprites repositorioSprites;

    public SeccionInfoCuerpo() {
        this(new GestorImagenes());
    }

    public SeccionInfoCuerpo(RepositorioSprites repositorioSprites) {
        this.repositorioSprites = repositorioSprites != null ? repositorioSprites : new GestorImagenes();
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
            if (e.getCode() == KeyCode.ESCAPE) {
                stage.close();
            }
        });
    }

    @Override
    public Node crearNodoIcono(TipoCuerpo tipo) {
        if (repositorioSprites.tieneSprite(tipo)) {
            javafx.scene.image.ImageView imgView = repositorioSprites.crearVistaIcono(tipo, 44, 44);
            StackPane imgBox = new StackPane(imgView);
            imgBox.setPrefSize(48, 48);
            imgBox.setStyle("-fx-background-color: #0d0e15; -fx-border-color: #5be3ff; -fx-border-width: 1.5px; -fx-background-radius: 4px; -fx-border-radius: 4px;");
            return imgBox;
        } else {
            Label lblIcono = new Label(obtenerIcono(tipo));
            lblIcono.setFont(Font.font("Monospace", FontWeight.BOLD, 22));
            lblIcono.setTextFill(Color.web(tipo != null ? tipo.getColorHexString() : "#ffffff"));
            return lblIcono;
        }
    }

    @Override
    public HBox crearHeader(Node iconoNodo, String titulo, String subtitulo) {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titulosBox = new VBox(2);
        Label lblTitulo = new Label(titulo.toUpperCase());
        lblTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 18));
        lblTitulo.setTextFill(TemaUI.COLOR_TEXTO_TITULO);

        Label lblSubtitulo = new Label(subtitulo);
        lblSubtitulo.setFont(Font.font("System", FontWeight.NORMAL, 12));
        lblSubtitulo.setTextFill(TemaUI.COLOR_TEXTO_MUTED);

        titulosBox.getChildren().addAll(lblTitulo, lblSubtitulo);
        header.getChildren().addAll(iconoNodo, titulosBox);
        return header;
    }

    @Override
    public VBox crearSeccionDescripcion(TipoCuerpo tipo) {
        VBox descBox = new VBox(4);
        Label lblDescTitulo = new Label("DESCRIPCIÓN DEL CUERPO");
        lblDescTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblDescTitulo.setTextFill(TemaUI.COLOR_ACENTO_CYAN);

        Label lblDescripcion = new Label(obtenerLore(tipo));
        lblDescripcion.setFont(Font.font("System", FontWeight.NORMAL, 12));
        lblDescripcion.setTextFill(Color.web("#d1d5db"));
        lblDescripcion.setWrapText(true);
        lblDescripcion.setLineSpacing(2);

        descBox.getChildren().addAll(lblDescTitulo, lblDescripcion);
        return descBox;
    }

    @Override
    public VBox crearSeccionCaracteristicas(TipoCuerpo tipo) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(10));
        box.setStyle(TemaUI.CSS_SECCION_CARD);

        Label lblTitulo = new Label("CARACTERÍSTICAS Y PROPIEDADES FÍSICAS");
        lblTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblTitulo.setTextFill(TemaUI.COLOR_ORO);

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(4);

        agregarFilaPropiedad(grid, 0, "Categoría:", tipo.categoria.name(), "#5be3ff");
        agregarFilaPropiedad(grid, 1, "Masa Base:", TipoCuerpo.formatearMasa(tipo.masaBase), "#e8e4d8");
        agregarFilaPropiedad(grid, 2, "Radio Físico:", String.format(java.util.Locale.US, "%,.0f km", tipo.radioFisicoBase / 1000.0), "#e8e4d8");
        agregarFilaPropiedad(grid, 3, "Temperatura:", String.format(java.util.Locale.US, "%.0f K (%.1f °C)", tipo.temperaturaBase, tipo.temperaturaBase - 273.15), "#e8e4d8");

        String habitabilidadStr = tipo.puedeTenerCivilizacion
                ? "Apto (Alberga y prolifera civilización)"
                : "No habitable (Ambiente inhóspito)";
        String habColor = tipo.puedeTenerCivilizacion ? "#50fa7b" : "#8c92a4";
        agregarFilaPropiedad(grid, 4, "Habitabilidad:", habitabilidadStr, habColor);

        String gravStr = tipo.generaGravedadFuerte
                ? "Dominante (Atracción gravitatoria mayor)"
                : "Estándar (Interacción orbital normal)";
        agregarFilaPropiedad(grid, 5, "Gravedad:", gravStr, tipo.generaGravedadFuerte ? "#ffd700" : "#d1d5db");

        String inflStr = tipo.esMasivo ? "Cuerpo masivo principal" : "Cuerpo orbital menor";
        agregarFilaPropiedad(grid, 6, "Dinámica:", inflStr, "#5be3ff");

        box.getChildren().addAll(lblTitulo, grid);
        return box;
    }

    @Override
    public void agregarFilaPropiedad(GridPane grid, int fila, String clave, String valor, String colorValorHex) {
        Label lblClave = new Label(clave);
        lblClave.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        lblClave.setTextFill(TemaUI.COLOR_TEXTO_MUTED);

        Label lblValor = new Label(valor);
        lblValor.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        lblValor.setTextFill(Color.web(colorValorHex));

        grid.add(lblClave, 0, fila);
        grid.add(lblValor, 1, fila);
    }

    @Override
    public String obtenerNombrePorDefecto(TipoCuerpo tipo) {
        return tipo != null ? CatalogoDescriptoresCuerpo.obtenerDescriptor(tipo).nombrePorDefecto() : "Cuerpo";
    }

    @Override
    public String obtenerIcono(TipoCuerpo tipo) {
        return tipo != null ? CatalogoDescriptoresCuerpo.obtenerDescriptor(tipo).icono() : "[???]";
    }

    @Override
    public String obtenerSubtitulo(TipoCuerpo tipo) {
        return tipo != null ? CatalogoDescriptoresCuerpo.obtenerDescriptor(tipo).subtitulo() : "Objeto Astronomico";
    }

    @Override
    public String obtenerLore(TipoCuerpo tipo) {
        return tipo != null ? CatalogoDescriptoresCuerpo.obtenerDescriptor(tipo).lore() : "Cuerpo celeste en el sistema.";
    }

    @Override
    public void poblarListaCostos(VBox contenedor, Map<TipoRecurso, Double> costos) {
        contenedor.getChildren().clear();
        if (costos == null || costos.isEmpty()) {
            agregarMensajeEstado(contenedor, "• Gratis (Sin costo de colocación)", "#d1d5db");
        } else {
            for (var entry : costos.entrySet()) {
                agregarItemRecurso(contenedor, entry.getKey(), String.format("• %s: %.0f", entry.getKey().nombre, entry.getValue()));
            }
        }
    }

    @Override
    public void poblarListaProduccion(VBox contenedor, Map<TipoRecurso, Double> prod) {
        contenedor.getChildren().clear();
        if (prod == null || prod.isEmpty()) {
            agregarMensajeEstado(contenedor, "• No genera producción pasiva", "#8c92a4");
        } else {
            for (var entry : prod.entrySet()) {
                agregarItemRecurso(contenedor, entry.getKey(), String.format("• %s: +%.2f / tick", entry.getKey().nombre, entry.getValue()));
            }
        }
    }

    private void agregarItemRecurso(VBox contenedor, TipoRecurso tipo, String texto) {
        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        lbl.setTextFill(Color.web(tipo.getColorHex()));
        contenedor.getChildren().add(lbl);
    }

    private void agregarMensajeEstado(VBox contenedor, String mensaje, String colorHex) {
        Label lbl = new Label(mensaje);
        lbl.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        lbl.setTextFill(Color.web(colorHex));
        contenedor.getChildren().add(lbl);
    }
}
