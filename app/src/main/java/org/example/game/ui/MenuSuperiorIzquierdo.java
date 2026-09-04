package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.render.RenderizadorPixelArt;
import org.example.game.simulacion.SimulacionSolar;

/**
 * Menú superior izquierdo estilo RimWorld UI.
 * SRP: Ofrece acceso a presets de sistemas solares, alternar capas visuales y reiniciar la simulación.
 */
public class MenuSuperiorIzquierdo extends HBox {

    private final SimulacionSolar simulacion;
    private final RenderizadorPixelArt renderizador;
    private final Runnable onResetCallback;

    public MenuSuperiorIzquierdo(SimulacionSolar simulacion, RenderizadorPixelArt renderizador, Runnable onResetCallback) {
        this.simulacion = simulacion;
        this.renderizador = renderizador;
        this.onResetCallback = onResetCallback;

        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(6, 10, 6, 10));
        setSpacing(6);
        setStyle("-fx-background-color: #1a1c26; -fx-border-color: #000000; -fx-border-width: 2px; -fx-background-radius: 4px; -fx-border-radius: 4px;");

        // Título del juego
        Label lblTitulo = new Label("ORBITA");
        lblTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        lblTitulo.setTextFill(Color.web("#5be3ff"));

        // Botón Presets
        MenuButton btnPresets = new MenuButton("Presets");
        configurarMenuButton(btnPresets);

        MenuItem itemBasico = new MenuItem("Sistema Solar Basico");
        itemBasico.setOnAction(e -> cargarPresetBasico());

        btnPresets.getItems().addAll(itemBasico);

        // Botón Grilla / Visual
        Button btnGrilla = new Button("Grilla");
        configurarBoton(btnGrilla, "Alternar Grilla (G)", renderizador::toggleGrilla);

        Button btnZonas = new Button("Órbitas");
        configurarBoton(btnZonas, "Alternar Órbitas de Referencia (H)", renderizador::toggleZonasHabitables);

        Button btnEstelas = new Button("Estelas");
        configurarBoton(btnEstelas, "Alternar Estelas Orbitales (T)", renderizador::toggleEstelas);

        // Botón Limpiar
        Button btnLimpiar = new Button("Limpiar");
        configurarBoton(btnLimpiar, "Eliminar todos los cuerpos celestes", this::confirmarLimpieza);
        btnLimpiar.setStyle("-fx-background-color: #3b2028; -fx-text-fill: #ff6b6b; -fx-border-color: #000000; -fx-border-width: 1px; -fx-cursor: hand;");

        getChildren().addAll(lblTitulo, new Separator(), btnPresets, btnGrilla, btnZonas, btnEstelas, btnLimpiar);
    }

    private void configurarBoton(Button btn, String tooltip, Runnable accion) {
        btn.setFont(Font.font("System", FontWeight.NORMAL, 10));
        btn.setStyle("-fx-background-color: #2b2d3a; -fx-text-fill: #e8e4d8; -fx-border-color: #000000; -fx-border-width: 1px; -fx-cursor: hand;");
        btn.setTooltip(new Tooltip(tooltip));
        btn.setFocusTraversable(false);
        btn.setOnAction(e -> accion.run());
    }

    private void configurarMenuButton(MenuButton btn) {
        btn.setFont(Font.font("System", FontWeight.NORMAL, 10));
        btn.setStyle("-fx-background-color: #2b2d3a; -fx-text-fill: #e8e4d8; -fx-border-color: #000000; -fx-border-width: 1px; -fx-cursor: hand;");
        btn.setFocusTraversable(false);
    }

    private void confirmarLimpieza() {
        simulacion.limpiarCuerpos();
        if (onResetCallback != null) onResetCallback.run();
    }

    private void cargarPresetBasico() {
        simulacion.cargarPresetSistemaBasico();
        if (onResetCallback != null) onResetCallback.run();
    }

    private void cargarPresetBinaria() {
        simulacion.cargarPresetEstrellaBinaria();
        if (onResetCallback != null) onResetCallback.run();
    }

    private void cargarPresetAgujero() {
        simulacion.cargarPresetAgujeroNegro();
        if (onResetCallback != null) onResetCallback.run();
    }
}
