package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.cuerpo.FormateadorMasa;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.jugador.TipoRecurso;

import java.util.Locale;
import java.util.Map;

/**
 * SRP: Construye paneles de características físicas y listas de recursos económicos para modales de cuerpos.
 */
public class ConstructorPanelCaracteristicas {

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
        agregarFilaPropiedad(grid, 1, "Masa Base:", FormateadorMasa.formatearMasa(tipo.masaBase), "#e8e4d8");
        agregarFilaPropiedad(grid, 2, "Radio Físico:", String.format(Locale.US, "%,.0f km", tipo.radioFisicoBase / 1000.0), "#e8e4d8");
        agregarFilaPropiedad(grid, 3, "Temperatura:", String.format(Locale.US, "%.0f K (%.1f °C)", tipo.temperaturaBase, tipo.temperaturaBase - 273.15), "#e8e4d8");
        agregarFilaPropiedad(grid, 4, "Habitabilidad:",
                tipo.puedeTenerCivilizacion ? "Apto (Alberga y prolifera civilización)" : "No habitable (Ambiente inhóspito)",
                tipo.puedeTenerCivilizacion ? "#50fa7b" : "#8c92a4");
        agregarFilaPropiedad(grid, 5, "Gravedad:",
                tipo.generaGravedadFuerte ? "Dominante (Atracción gravitatoria mayor)" : "Estándar (Interacción orbital normal)",
                tipo.generaGravedadFuerte ? "#ffd700" : "#d1d5db");
        agregarFilaPropiedad(grid, 6, "Dinámica:",
                tipo.esMasivo ? "Cuerpo masivo principal" : "Cuerpo orbital menor", "#5be3ff");

        box.getChildren().addAll(lblTitulo, grid);
        return box;
    }

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

    public void poblarListaCostos(VBox contenedor, Map<TipoRecurso, Double> costos) {
        contenedor.getChildren().clear();
        if (costos == null || costos.isEmpty()) {
            agregarEtiqueta(contenedor, "• Gratis (Sin costo de colocación)", "#d1d5db");
        } else {
            for (var entry : costos.entrySet()) {
                agregarEtiqueta(contenedor, String.format("• %s: %.0f", entry.getKey().nombre, entry.getValue()), entry.getKey().getColorHex());
            }
        }
    }

    public void poblarListaProduccion(VBox contenedor, Map<TipoRecurso, Double> prod) {
        contenedor.getChildren().clear();
        if (prod == null || prod.isEmpty()) {
            agregarEtiqueta(contenedor, "• No genera producción pasiva", "#8c92a4");
        } else {
            for (var entry : prod.entrySet()) {
                agregarEtiqueta(contenedor, String.format("• %s: +%.2f / tick", entry.getKey().nombre, entry.getValue()), entry.getKey().getColorHex());
            }
        }
    }

    private void agregarEtiqueta(VBox contenedor, String texto, String colorHex) {
        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        lbl.setTextFill(Color.web(colorHex));
        contenedor.getChildren().add(lbl);
    }
}
