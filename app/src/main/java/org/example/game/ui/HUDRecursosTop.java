package org.example.game.ui;
import javafx.geometry.Insets; import javafx.geometry.Pos; import javafx.scene.control.Label; import javafx.scene.layout.HBox; import javafx.scene.layout.VBox; import javafx.scene.paint.Color; import javafx.scene.text.Font; import javafx.scene.text.FontWeight; import org.example.game.jugador.InventarioJugador; import org.example.game.jugador.TipoRecurso; import org.example.game.simulacion.SimulacionSolar; import java.util.LinkedHashMap; import java.util.Map;
/**
 * Contador de recursos superior estilo interfaz de gestion espacial.
 * SRP: Muestra los recursos del jugador y la poblacion con identificadores claros.
 * Composición: Los widgets de recursos son componentes directos sin jerarquías de herencia profundas.
 */
public class HUDRecursosTop extends HBox {

    private final InventarioJugador inventario;
    private final Map<TipoRecurso, ItemRecursoWidget> itemsRecursos = new LinkedHashMap<>();

    public HUDRecursosTop(SimulacionSolar simulacion) {
        this.inventario = simulacion != null ? simulacion.getInventario() : new InventarioJugador();

        setAlignment(Pos.CENTER_RIGHT);
        setPadding(new Insets(6, 12, 6, 12));
        setSpacing(14);
        setStyle("-fx-background-color: #1a1c26; -fx-border-color: #000000; -fx-border-width: 2px; -fx-background-radius: 4px; -fx-border-radius: 4px;");

        for (TipoRecurso tipo : TipoRecurso.values()) {
            ItemRecursoWidget item = new ItemRecursoWidget(tipo);
            itemsRecursos.put(tipo, item);
            getChildren().add(item);
        }

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

    /**
     * Componente visual para un recurso individual (hereda directamente de VBox, sin clases intermedias).
     */
    public static class ItemRecursoWidget extends VBox {
        private final TipoRecurso tipo;
        private final Label lblNombre;
        private final Label lblValor;

        public ItemRecursoWidget(TipoRecurso tipo) {
            super(1);
            this.tipo = tipo;
            setAlignment(Pos.CENTER);

            lblNombre = new Label(tipo.nombre.toUpperCase());
            lblNombre.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
            lblNombre.setTextFill(Color.web("#8c92a4"));

            lblValor = new Label("0");
            lblValor.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
            lblValor.setTextFill(Color.web(tipo.getColorHex()));

            getChildren().addAll(lblNombre, lblValor);
        }

        public void actualizar(double cantidad) {
            lblValor.setText(FormateadorNumero.formatear(cantidad));
        }

        public String getNombreLabelText() {
            return lblNombre.getText();
        }

        public String getValorLabelText() {
            return lblValor.getText();
        }

        public TipoRecurso getTipo() {
            return tipo;
        }
    }
}
