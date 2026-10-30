package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.example.game.controlador.ControladorMouse;
import org.example.game.cuerpo.*;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.ServicioReembolso;
import org.example.game.jugador.TipoRecurso;
import org.example.game.simulacion.SimulacionSolar;

import java.util.Map;

/**
 * Ventana modal para inspeccionar un cuerpo celeste existente en el sistema orbital (SRP).
 */
public class VentanaInspeccion {

    public static void mostrar(CuerpoCeleste cuerpo, SimulacionSolar simulacion, Window owner, ControladorMouse controladorMouse) {
        if (cuerpo == null) return;
        TipoCuerpo tipo = cuerpo.getTipoCuerpo();
        if (tipo == null) return;

        Stage stage = new Stage();
        VBox root = SeccionInfoCuerpo.inicializarDialogo(stage, "Inspeccionar: " + cuerpo.getNombre(), owner);

        HBox header = SeccionInfoCuerpo.crearHeader(
                SeccionInfoCuerpo.crearNodoIcono(tipo),
                cuerpo.getNombre(),
                tipo.nombre + " • " + SeccionInfoCuerpo.obtenerSubtitulo(tipo)
        );

        VBox descBox = SeccionInfoCuerpo.crearSeccionDescripcion(tipo);

        // Telemetría
        VBox datosFisicos = new VBox(4);
        datosFisicos.setPadding(new Insets(10));
        datosFisicos.setStyle(TemaUI.CSS_SECCION_CARD);

        Label lblDatosTitulo = new Label("TELEMETRÍA Y PROPIEDADES FÍSICAS");
        lblDatosTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblDatosTitulo.setTextFill(TemaUI.COLOR_ORO);

        Label lblMasa = new Label("• Masa: " + TipoCuerpo.formatearMasa(cuerpo.getMasa()));
        lblMasa.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        lblMasa.setTextFill(Color.web("#d1d5db"));

        Label lblVel = new Label(String.format("• Velocidad: %.2f km/s", cuerpo.getVelocidad().magnitud()));
        lblVel.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        lblVel.setTextFill(Color.web("#d1d5db"));

        Label lblPos = new Label(String.format("• Posición: (%.1f, %.1f)", cuerpo.getPosicionX(), cuerpo.getPosicionY()));
        lblPos.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        lblPos.setTextFill(Color.web("#d1d5db"));

        datosFisicos.getChildren().addAll(lblDatosTitulo, lblMasa, lblVel, lblPos);

        // Producción actual
        VBox prodBox = new VBox(4);
        prodBox.setPadding(new Insets(10));
        prodBox.setStyle(TemaUI.CSS_SECCION_CARD);

        Label lblProdTitulo = new Label("GENERACIÓN ACTUAL POR TICK");
        lblProdTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblProdTitulo.setTextFill(TemaUI.COLOR_VERDE_EXITO);
        prodBox.getChildren().add(lblProdTitulo);

        double factorMasa = tipo.masaBase > 0 ? Math.max(0.1, cuerpo.getMasa() / tipo.masaBase) : 1.0;
        Map<TipoRecurso, Double> prod = (cuerpo instanceof Transformable t && t.isTransformadoPorEvento())
                ? java.util.Collections.emptyMap()
                : InventarioJugador.getProduccionEscaladaPorTick(tipo, factorMasa);
        SeccionInfoCuerpo.poblarListaProduccion(prodBox, prod);

        // Civilización
        if (cuerpo instanceof Civilizable civ && civ.tieneCivilizacion() && civ.getCivilizacion() != null) {
            var comp = civ.getCivilizacion();
            VBox civBox = new VBox(4);
            civBox.setPadding(new Insets(10));
            civBox.setStyle("-fx-background-color: #1a1c26; -fx-border-color: #5be3ff; -fx-border-width: 1px; -fx-background-radius: 4px; -fx-border-radius: 4px;");

            Label lblCivTitulo = new Label("CIVILIZACIÓN PLANETARIA");
            lblCivTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
            lblCivTitulo.setTextFill(TemaUI.COLOR_ACENTO_CYAN);

            Label lblPob = new Label(String.format("• Población: %.0f habitantes", comp.getPoblacion()));
            lblPob.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
            lblPob.setTextFill(Color.web("#d1d5db"));

            Label lblEst = new Label("• Estado: " + comp.getEstado().name());
            lblEst.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
            lblEst.setTextFill(TemaUI.COLOR_VERDE_EXITO);

            civBox.getChildren().addAll(lblCivTitulo, lblPob, lblEst);
            datosFisicos.getChildren().add(civBox);
        }

        // Escudo
        if (cuerpo instanceof Defensor def && def.puedeRecibirEscudo()) {
            VBox escudoBox = new VBox(6);
            escudoBox.setPadding(new Insets(10));
            escudoBox.setStyle("-fx-background-color: #1a1c26; -fx-border-color: #5be3ff; -fx-border-width: 1px; -fx-background-radius: 4px;");

            Label lblEscudoTitulo = new Label("DEFENSA PLANETARIA (ESCUDO)");
            lblEscudoTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
            lblEscudoTitulo.setTextFill(TemaUI.COLOR_ACENTO_CYAN);

            String estadoEscudo = def.tieneEscudo()
                    ? "Escudo: " + def.getGolpesEscudoRestantes() + " golpes restantes"
                    : "Escudo: Inactivo";
            Label lblEstadoEscudo = new Label(estadoEscudo);
            lblEstadoEscudo.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
            lblEstadoEscudo.setTextFill(Color.web("#d1d5db"));

            escudoBox.getChildren().addAll(lblEscudoTitulo, lblEstadoEscudo);
            datosFisicos.getChildren().add(escudoBox);
        }

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        btnCerrar.setStyle(TemaUI.CSS_BTN_SECUNDARIO);
        btnCerrar.setOnAction(e -> stage.close());

        Button btnEliminar = new Button("Eliminar Cuerpo");
        btnEliminar.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        btnEliminar.setStyle(TemaUI.CSS_BTN_PELIGRO);
        btnEliminar.setOnAction(e -> {
            if (simulacion != null) {
                ServicioReembolso.reembolsarYEliminar(cuerpo, simulacion);
            }
            stage.close();
        });

        footer.getChildren().addAll(btnEliminar, btnCerrar);

        root.getChildren().addAll(header, new Separator(), descBox, datosFisicos, prodBox, new Separator(), footer);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #13151f; -fx-background-color: #13151f;");

        Scene scene = new Scene(scroll, 540, 580);
        SeccionInfoCuerpo.vincularCierrePorEscape(scene, stage);

        stage.setScene(scene);
        stage.show();
    }
}
