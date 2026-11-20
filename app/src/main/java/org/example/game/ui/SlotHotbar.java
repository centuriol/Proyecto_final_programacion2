package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.render.ConstructorIconosSprite;

import java.util.function.Consumer;

/**
 * Componente individual que representa una casilla (slot) de la Hotbar (SRP / ISP / DIP).
 */
public class SlotHotbar extends VBox {

    private static final FabricaUI FABRICA_UI = new FabricaUI();

    private final TipoCuerpo tipo;
    private final int hotkey;
    private boolean seleccionado = false;

    public SlotHotbar(TipoCuerpo tipo, int hotkey, Consumer<TipoCuerpo> onClick) {
        this(tipo, hotkey, onClick, FABRICA_UI.crearRepositorioSprites());
    }

    public SlotHotbar(TipoCuerpo tipo, int hotkey, Consumer<TipoCuerpo> onClick, ConstructorIconosSprite constructorIconos) {
        super(4);
        this.tipo = tipo;
        this.hotkey = hotkey;
        ConstructorIconosSprite sprites = constructorIconos != null ? constructorIconos : FABRICA_UI.crearRepositorioSprites();

        setAlignment(Pos.CENTER);
        setPrefSize(100, 84);
        setPadding(new Insets(5, 8, 5, 8));
        setBackground(TemaUI.BG_SLOT_NORMAL);
        setBorder(TemaUI.BORDE_SLOT_NORMAL);
        setCursor(Cursor.HAND);

        Label lblKey = new Label("[" + hotkey + "]");
        lblKey.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
        lblKey.setTextFill(TemaUI.COLOR_TEXTO_MUTED);

        StackPane iconBox = sprites.crearContenedorSprite(tipo, 40, 40);

        Label lblNom = new Label(tipo.nombre.toUpperCase());
        lblNom.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblNom.setTextFill(Color.web(tipo.getColorHexString()));
        lblNom.setAlignment(Pos.CENTER);
        lblNom.setTextAlignment(TextAlignment.CENTER);

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
            if (onClick != null) onClick.accept(tipo);
        });
    }

    public void setSeleccionado(boolean seleccionado) {
        this.seleccionado = seleccionado;
        setBackground(seleccionado ? TemaUI.BG_PANEL : TemaUI.BG_SLOT_NORMAL);
        setBorder(seleccionado ? TemaUI.BORDE_SLOT_HOVER : TemaUI.BORDE_SLOT_NORMAL);
    }

    public void setBloqueado(boolean bloqueado) {
        setOpacity(bloqueado ? 0.45 : 1.0);
    }

    public TipoCuerpo getTipo() { return tipo; }
    public int getHotkey() { return hotkey; }
}
