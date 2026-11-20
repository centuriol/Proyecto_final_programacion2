package org.example.game.render;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.cuerpo.CatalogoDescriptoresCuerpo;
import org.example.game.cuerpo.TipoCuerpo;

/**
 * SRP: Construye nodos visuales JavaFX (ImageView y StackPane con fallback) para sprites de cuerpos celestes.
 */
public class ConstructorVistaSprite {

    public ImageView crearVistaIcono(Image img, double ancho, double alto) {
        if (img == null) return null;
        ImageView iv = new ImageView(img);
        iv.setFitWidth(ancho);
        iv.setFitHeight(alto);
        iv.setPreserveRatio(true);
        iv.setSmooth(false);
        return iv;
    }

    public StackPane crearContenedorSprite(Image img, TipoCuerpo tipo, double ancho, double alto) {
        ImageView iv = crearVistaIcono(img, ancho, alto);
        StackPane contenedor = new StackPane();
        contenedor.setAlignment(Pos.CENTER);
        contenedor.setPrefSize(ancho + 4, alto + 4);
        contenedor.setStyle("-fx-background-color: #1a1c26; -fx-border-color: #000000; -fx-border-width: 1px; -fx-background-radius: 3px; -fx-border-radius: 3px;");
        if (iv != null) {
            contenedor.getChildren().add(iv);
        } else if (tipo != null) {
            Label lbl = new Label(CatalogoDescriptoresCuerpo.obtenerDescriptor(tipo).icono());
            lbl.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
            lbl.setTextFill(Color.web(tipo.getColorHexString()));
            contenedor.getChildren().add(lbl);
        }
        return contenedor;
    }
}
