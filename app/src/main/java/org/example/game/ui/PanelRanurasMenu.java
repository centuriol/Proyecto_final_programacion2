package org.example.game.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.guardado.InfoRanuraGuardado;
import org.example.game.guardado.PoliticaRanurasGuardado;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * SRP: Gestiona la vista de selección de ranuras de guardado del menú principal.
 */
public class PanelRanurasMenu extends VBox {

    public static final String ESTILO_BOTON_MENU =
            "-fx-background-color: #172a38; -fx-text-fill: #5be3ff; -fx-border-color: #5be3ff; "
            + "-fx-border-width: 1.5px; -fx-background-radius: 4px; -fx-border-radius: 4px; "
            + "-fx-padding: 12 24 12 24; -fx-cursor: hand;";
    private static final String ESTILO_RANURA_VACIA =
            "-fx-background-color: #14161f; -fx-text-fill: #6c7284; -fx-border-color: #2b2d3a; "
            + "-fx-border-width: 1px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-padding: 10 18 10 18;";

    private final Supplier<List<InfoRanuraGuardado>> proveedorRanuras;
    private final List<Button> botonesRanuras = new ArrayList<>(PoliticaRanurasGuardado.TOTAL_RANURAS);
    private final Button btnVolver;

    public PanelRanurasMenu(Supplier<List<InfoRanuraGuardado>> proveedorRanuras,
                            IntConsumer onCargarRanura,
                            Runnable onVolver) {
        super(12);
        this.proveedorRanuras = proveedorRanuras != null ? proveedorRanuras : List::of;
        setAlignment(Pos.CENTER);

        Label encabezado = new Label("SELECCIONAR RANURA DE GUARDADO");
        encabezado.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
        encabezado.setTextFill(TemaUI.COLOR_TEXTO_TITULO);
        getChildren().add(encabezado);

        for (int i = 1; i <= PoliticaRanurasGuardado.TOTAL_RANURAS; i++) {
            final int numeroRanura = i;
            Button btnRanura = crearBotonAccion("Ranura " + numeroRanura, () -> {
                if (onCargarRanura != null) onCargarRanura.accept(numeroRanura);
            });
            botonesRanuras.add(btnRanura);
            getChildren().add(btnRanura);
        }

        btnVolver = crearBotonAccion("Volver", onVolver != null ? onVolver : () -> {});
        btnVolver.setStyle(TemaUI.CSS_BTN_SECUNDARIO + " -fx-padding: 10 20 10 20;");
        getChildren().add(btnVolver);
    }

    public static Button crearBotonAccion(String texto, Runnable accion) {
        Button btn = new Button(texto);
        btn.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(ESTILO_BOTON_MENU);
        btn.setOnAction(e -> accion.run());
        return btn;
    }

    public void refrescar() {
        List<InfoRanuraGuardado> ranuras = proveedorRanuras.get();
        for (int i = 0; i < botonesRanuras.size(); i++) {
            Button btn = botonesRanuras.get(i);
            InfoRanuraGuardado info = (ranuras != null && i < ranuras.size())
                    ? ranuras.get(i)
                    : new InfoRanuraGuardado(i + 1, "ranura_" + (i + 1), null);
            btn.setText(info.tituloRanura() + " - " + info.formatearResumen());
            btn.setDisable(!info.estaOcupada());
            btn.setStyle(info.estaOcupada() ? ESTILO_BOTON_MENU : ESTILO_RANURA_VACIA);
        }
    }

    public Button getBtnVolver() { return btnVolver; }
    public List<Button> getBotonesRanuras() { return Collections.unmodifiableList(botonesRanuras); }
}
