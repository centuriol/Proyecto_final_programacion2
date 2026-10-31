package org.example.game.ui;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.render.GestorImagenes;
import java.util.function.Consumer;
/**
 * Componente individual que representa una casilla (slot) de la Hotbar (SRP).
 */
public class SlotHotbar extends VBox {

    private final TipoCuerpo tipo;
    private final int hotkey;
    private final Label lblKey;
    private final StackPane iconBox;
    private final Label lblNom;
    private boolean seleccionado = false;
    private boolean bloqueado = false;

    public SlotHotbar(TipoCuerpo tipo, int hotkey, Consumer<TipoCuerpo> onClick) {
        super(4);
        this.tipo = tipo;
        this.hotkey = hotkey;

        setAlignment(Pos.CENTER);
        setPrefSize(100, 84);
        setPadding(new Insets(5, 8, 5, 8));
        setBackground(TemaUI.BG_SLOT_NORMAL);
        setBorder(TemaUI.BORDE_SLOT_NORMAL);
        setCursor(Cursor.HAND);

        lblKey = new Label("[" + hotkey + "]");
        lblKey.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
        lblKey.setTextFill(TemaUI.COLOR_TEXTO_MUTED);

        iconBox = GestorImagenes.crearContenedorIcono(tipo, 40, 40);

        lblNom = new Label(tipo.nombre.toUpperCase());
        lblNom.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblNom.setTextFill(Color.web(tipo.getColorHexString()));
        lblNom.setAlignment(Pos.CENTER);
        lblNom.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        getChildren().addAll(lblKey, iconBox, lblNom);

        setOnMouseEntered(e -> {
            if (!seleccionado) {
                setBackground(TemaUI.BG_SLOT_HOVER);
                setBorder(TemaUI.BORDE_SLOT_HOVER);
            }
        });

        setOnMouseExited(e -> {
            if (!seleccionado) {
                setBackground(TemaUI.BG_SLOT_NORMAL);
                setBorder(TemaUI.BORDE_SLOT_NORMAL);
            }
        });

        setOnMouseClicked(e -> {
            if (onClick != null) {
                onClick.accept(tipo);
            }
        });
    }

    public void setSeleccionado(boolean seleccionado) {
        this.seleccionado = seleccionado;
        if (seleccionado) {
            setBackground(TemaUI.BG_PANEL);
            setBorder(TemaUI.BORDE_SLOT_HOVER);
        } else {
            setBackground(TemaUI.BG_SLOT_NORMAL);
            setBorder(TemaUI.BORDE_SLOT_NORMAL);
        }
    }

    public void setBloqueado(boolean bloqueado) {
        this.bloqueado = bloqueado;
        setOpacity(bloqueado ? 0.45 : 1.0);
    }

    public TipoCuerpo getTipo() {
        return tipo;
    }

    public int getHotkey() {
        return hotkey;
    }
}
