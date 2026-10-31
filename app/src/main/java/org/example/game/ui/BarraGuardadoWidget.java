package org.example.game.ui;
import javafx.geometry.Pos; import javafx.scene.control.Button; import javafx.scene.control.Tooltip; import javafx.scene.layout.HBox; import javafx.scene.text.Font; import javafx.scene.text.FontWeight; import org.example.game.controlador.comando.Comando;
/**
 * Barra compacta de controles para guardar y cargar partida en la interfaz superior (SRP / DIP).
 */
public class BarraGuardadoWidget extends HBox {

    private final Button btnGuardar;
    private final Button btnCargar;

    public BarraGuardadoWidget(Comando comandoGuardar, Comando comandoCargar) {
        super(6);
        setAlignment(Pos.CENTER_LEFT);
        setPickOnBounds(true);

        this.btnGuardar = crearBotonAccion("[GUARDAR (F5)]", "Guardar partida actual (F5 / Ctrl+S)", comandoGuardar);
        this.btnCargar = crearBotonAccion("[CARGAR (F9)]", "Cargar partida guardada (F9 / Ctrl+L)", comandoCargar);

        getChildren().addAll(btnGuardar, btnCargar);
    }

    private Button crearBotonAccion(String texto, String descripcionTooltip, Comando comando) {
        Button boton = new Button(texto);
        boton.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        boton.setStyle(TemaUI.CSS_BTN_PRIMARIO);
        boton.setFocusTraversable(false);
        boton.setTooltip(new Tooltip(descripcionTooltip));
        boton.setOnAction(e -> {
            if (comando != null) {
                comando.ejecutar();
            }
        });
        return boton;
    }

    public Button getBtnGuardar() {
        return btnGuardar;
    }

    public Button getBtnCargar() {
        return btnCargar;
    }
}
