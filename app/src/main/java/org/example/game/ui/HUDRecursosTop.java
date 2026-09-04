package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.Planeta;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;
import org.example.game.simulacion.SimulacionSolar;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Contador de recursos superior estilo interfaz de gestion espacial.
 * SRP: Muestra los recursos del jugador y la poblacion con identificadores claros.
 * OCP: Se adapta automaticamente si se agregan mas TipoRecurso en el futuro.
 */
public class HUDRecursosTop extends HBox {

    private final SimulacionSolar simulacion;
    private final InventarioJugador inventario;
    private final Map<TipoRecurso, ItemRecursoWidget> itemsRecursos = new LinkedHashMap<>();
    private final ItemPoblacionWidget itemPoblacion;

    public HUDRecursosTop(SimulacionSolar simulacion) {
        this.simulacion = simulacion;
        this.inventario = simulacion.getInventario();

        setAlignment(Pos.CENTER_RIGHT);
        setPadding(new Insets(6, 12, 6, 12));
        setSpacing(14);
        setStyle("-fx-background-color: #1a1c26; -fx-border-color: #000000; -fx-border-width: 2px; -fx-background-radius: 4px; -fx-border-radius: 4px;");

        // Generar items dinamicamente para los 4 recursos
        for (TipoRecurso tipo : TipoRecurso.values()) {
            ItemRecursoWidget item = new ItemRecursoWidget(tipo);
            itemsRecursos.put(tipo, item);
            getChildren().add(item);
        }

        this.itemPoblacion = new ItemPoblacionWidget();
        // Nota: no se agrega itemPoblacion a getChildren() para no duplicar el contador de POBLACION

        actualizar();
    }

    public void actualizar() {
        for (Map.Entry<TipoRecurso, ItemRecursoWidget> entry : itemsRecursos.entrySet()) {
            entry.getValue().actualizar(inventario.getRecurso(entry.getKey()));
        }
    }

    public Map<TipoRecurso, ItemRecursoWidget> getItemsRecursos() {
        return itemsRecursos;
    }

    private String formatPop(long n) {
        if (n >= 1_000_000_000) return String.format("%.1fB", n / 1e9);
        if (n >= 1_000_000) return String.format("%.1fM", n / 1e6);
        if (n >= 1_000) return String.format("%.1fK", n / 1e3);
        return String.valueOf(n);
    }

    /**
     * Componente reutilizable que representa el contador de un recurso individual.
     */
    public static class ItemRecursoWidget extends VBox {
        private final TipoRecurso tipo;
        private final Label lblNombre;
        private final Label lblValor;

        public ItemRecursoWidget(TipoRecurso tipo) {
            this.tipo = tipo;
            setAlignment(Pos.CENTER);
            setSpacing(1);

            lblNombre = new Label(tipo.nombre.toUpperCase());
            lblNombre.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
            lblNombre.setTextFill(Color.web("#8c92a4"));

            lblValor = new Label("0");
            lblValor.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
            lblValor.setTextFill(Color.web(tipo.getColorHex()));

            getChildren().addAll(lblNombre, lblValor);
        }

        public void actualizar(double cantidad) {
            if (cantidad >= 1_000_000_000) {
                lblValor.setText(String.format("%.1fB", cantidad / 1e9));
            } else if (cantidad >= 1_000_000) {
                lblValor.setText(String.format("%.1fM", cantidad / 1e6));
            } else if (cantidad >= 10_000) {
                lblValor.setText(String.format("%.1fK", cantidad / 1e3));
            } else if (cantidad < 100) {
                lblValor.setText(String.format("%.1f", cantidad));
            } else {
                lblValor.setText(String.format("%.0f", cantidad));
            }
        }

        public TipoRecurso getTipo() {
            return tipo;
        }

        public String getNombreLabelText() {
            return lblNombre.getText();
        }

        public String getValorLabelText() {
            return lblValor.getText();
        }
    }

    /**
     * Componente reutilizable para el contador de poblacion galactica.
     */
    public static class ItemPoblacionWidget extends VBox {
        private final Label lblNombre;
        private final Label lblValor;

        public ItemPoblacionWidget() {
            setAlignment(Pos.CENTER);
            setSpacing(1);

            lblNombre = new Label("POBLACION");
            lblNombre.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
            lblNombre.setTextFill(Color.web("#8c92a4"));

            lblValor = new Label("0");
            lblValor.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
            lblValor.setTextFill(Color.web("#50fa7b"));

            getChildren().addAll(lblNombre, lblValor);
        }

        public void actualizar(String textoPoblacion) {
            lblValor.setText(textoPoblacion);
        }
    }
}
