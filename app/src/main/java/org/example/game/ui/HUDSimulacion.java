package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.simulacion.SimulacionSolar;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.Planeta;
import org.example.game.jugador.TipoRecurso;

/**
 * HUD superior con información de estado de la simulación.
 * Elementos clickeables para interacción rápida.
 */
public class HUDSimulacion extends HBox {
    private final SimulacionSolar simulacion;

    private final Button btnTick = new Button("Tick: 0");
    private final Label lblPoblacion = new Label("👥 Pop: 0");
    private final Label lblCuerpos = new Label("🪐 Cuerpos: 0");
    private final Button btnEstado = new Button("⏸ PAUSADO");
    private final Label lblVelocidad = new Label("⚡ 1.0x");
    private final Label lblAlertas = new Label("");

    public HUDSimulacion(SimulacionSolar simulacion) {
        this.simulacion = simulacion;

        setPrefHeight(ConfiguracionSimulacion.HUD_ALTO);
        setPadding(new Insets(5, 15, 5, 15));
        setSpacing(20);
        setAlignment(Pos.CENTER_LEFT);
        setStyle("-fx-background-color: rgba(26, 26, 46, 0.9); -fx-border-color: #0f3460; -fx-border-width: 0 0 2 0;");

        // Estilo común
        Font fontMono = Font.font("Monospace", FontWeight.BOLD, 12);
        Font fontNormal = Font.font("System", FontWeight.NORMAL, 11);

        // Botón Tick - click para mostrar info detallada
        btnTick.setFont(fontMono);
        btnTick.setTextFill(Color.web("#00ffff"));
        btnTick.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-cursor: hand;");
        btnTick.setFocusTraversable(false);
        btnTick.setOnAction(e -> mostrarInfoTick());
        btnTick.setTooltip(new Tooltip("Click para ver detalles del tick"));

        lblPoblacion.setFont(fontMono);
        lblPoblacion.setTextFill(Color.web("#32cd32"));

        lblCuerpos.setFont(fontMono);
        lblCuerpos.setTextFill(Color.web("#ffd700"));

        // Botón Estado - click para play/pause
        btnEstado.setFont(fontNormal);
        btnEstado.setTextFill(Color.web("#ffa500"));
        btnEstado.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-cursor: hand;");
        btnEstado.setFocusTraversable(false);
        btnEstado.setOnAction(e -> simulacion.togglePause());
        btnEstado.setTooltip(new Tooltip("Click para Play/Pause (Espacio)"));

        lblVelocidad.setFont(fontMono);
        lblVelocidad.setTextFill(Color.web("#ff69b4"));

        lblAlertas.setFont(Font.font("System", FontWeight.BOLD, 11));
        lblAlertas.setTextFill(Color.web("#ff4444"));

        getChildren().addAll(btnTick, lblPoblacion, lblCuerpos, btnEstado, lblVelocidad, lblAlertas);

        // Callback de actualización
        simulacion.setOnTickCallback(this::actualizar);
        actualizar();
    }

    private void actualizar() {
        // Tick
        btnTick.setText("Tick: " + simulacion.getTickActual());

        // Población total
        long popTotal = simulacion.getSistemaSolar().getCuerpos().stream()
                .filter(c -> c instanceof org.example.Planeta)
                .map(c -> (org.example.Planeta) c)
                .filter(Planeta::tieneCivilizacion)
                .mapToLong(p -> (long) p.getCivilizacion().getPoblacion())
                .sum();
        lblPoblacion.setText("👥 Pop: " + formatNumber(popTotal));

        // Cuenta de cuerpos por tipo
        long estrellas = simulacion.getSistemaSolar().getCuerpos().stream()
                .filter(c -> c.getTipoCuerpo() == org.example.game.cuerpo.TipoCuerpo.ESTRELLA).count();
        long planetas = simulacion.getSistemaSolar().getCuerpos().stream()
                .filter(c -> c instanceof org.example.Planeta).count();
        long agujeros = simulacion.getSistemaSolar().getCuerpos().stream()
                .filter(c -> c.getTipoCuerpo() == org.example.game.cuerpo.TipoCuerpo.AGUJERO_NEGRO
                        || c.getTipoCuerpo() == org.example.game.cuerpo.TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO).count();
        long meteoritos = simulacion.getSistemaSolar().getCuerpos().stream()
                .filter(c -> c instanceof org.example.game.cuerpo.Meteorito).count();

        lblCuerpos.setText(String.format("🪐 ★%d 🪨%d 🕳%d ☄%d", estrellas, planetas, agujeros, meteoritos));

        // Estado simulación
        btnEstado.setText(simulacion.isEnPausa() ? "⏸ PAUSADO" : "▶ EJECUTANDO");
        btnEstado.setTextFill(simulacion.isEnPausa() ? Color.web("#ffa500") : Color.web("#32cd32"));

        // Velocidad
        lblVelocidad.setText(String.format("⚡ %.1fx", simulacion.getVelocidadSimulacion()));

        // Alertas
        StringBuilder alertas = new StringBuilder();
        for (org.example.CuerpoCeleste c : simulacion.getSistemaSolar().getCuerpos()) {
            if (c instanceof org.example.Planeta) {
                org.example.Planeta p = (org.example.Planeta) c;
                if (p.getCivilizacion() != null) {
                    var estado = p.getCivilizacion().getEstado();
                    if (estado == org.example.game.civilizacion.CivilizacionComponent.EstadoCivilizacion.EN_PELIGRO
                            || estado == org.example.game.civilizacion.CivilizacionComponent.EstadoCivilizacion.LUCHANDO) {
                        alertas.append("⚠ ").append(p.getNombre()).append(": ").append(estado.nombre).append("  ");
                    }
                }
            }
        }
        lblAlertas.setText(alertas.toString());
    }

    private String formatNumber(long n) {
        if (n >= 1_000_000_000) return String.format("%.1fB", n / 1e9);
        if (n >= 1_000_000) return String.format("%.1fM", n / 1e6);
        if (n >= 1_000) return String.format("%.1fK", n / 1e3);
        return String.valueOf(n);
    }

    private void mostrarInfoTick() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Info Tick");
        alert.setHeaderText("Detalles del Tick " + simulacion.getTickActual());
        alert.setContentText(
            "Tick actual: " + simulacion.getTickActual() + "\n" +
            "Estado: " + (simulacion.isEnPausa() ? "Pausado" : "Ejecutando") + "\n" +
            "Velocidad: " + String.format("%.1fx", simulacion.getVelocidadSimulacion()) + "\n" +
            "Cuerpos activos: " + simulacion.getSistemaSolar().getCuerpos().size() + "\n" +
            "Modo colocación: " + simulacion.getModoColocacion().name()
        );
        alert.showAndWait();
    }
}