package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.example.game.civilizacion.CivilizacionComponent;
import org.example.game.cuerpo.*;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;
import org.example.game.simulacion.ConfiguracionEconomia;
import org.example.game.simulacion.SimulacionSolar;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * SRP: Construye las tarjetas de telemetría, civilización, colonización, escudo y producción de la ventana de inspección.
 */
public class ConstructorTarjetasInspeccion {

    public VBox construirSeccionTelemetria(CuerpoCeleste cuerpo) {
        VBox datosFisicos = new VBox(4);
        datosFisicos.setPadding(new Insets(10));
        datosFisicos.setStyle(TemaUI.CSS_SECCION_CARD);

        Label lblDatosTitulo = crearTitulo("TELEMETRÍA Y PROPIEDADES FÍSICAS", TemaUI.COLOR_ORO);
        Label lblMasa = crearDato("• Masa: " + FormateadorMasa.formatearMasa(cuerpo.getMasa()));
        Label lblVel = crearDato(String.format("• Velocidad: %.2f km/s", cuerpo.getVelocidad().magnitud()));
        Label lblPos = crearDato(String.format("• Posición: (%.1f, %.1f)", cuerpo.getPosicion().x(), cuerpo.getPosicion().y()));

        datosFisicos.getChildren().addAll(lblDatosTitulo, lblMasa, lblVel, lblPos);
        return datosFisicos;
    }

    public VBox construirTarjetaCivilizacion(CivilizacionComponent comp) {
        VBox civBox = new VBox(4);
        civBox.setPadding(new Insets(10));
        civBox.setStyle("-fx-background-color: #1a1c26; -fx-border-color: #5be3ff; -fx-border-width: 1px; -fx-background-radius: 4px; -fx-border-radius: 4px;");

        Label lblEst = new Label("• Estado: " + comp.getEstado().name());
        lblEst.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        lblEst.setTextFill(TemaUI.COLOR_VERDE_EXITO);

        civBox.getChildren().addAll(
                crearTitulo("CIVILIZACIÓN PLANETARIA", TemaUI.COLOR_ACENTO_CYAN),
                crearDato(String.format("• Población: %.0f habitantes", comp.getPoblacion())),
                lblEst
        );
        return civBox;
    }

    public VBox construirTarjetaPoblar(CuerpoCeleste cuerpo, SimulacionSolar simulacion, Stage stage) {
        VBox poblarBox = new VBox(6);
        poblarBox.setPadding(new Insets(10));
        poblarBox.setStyle("-fx-background-color: #1a1c26; -fx-border-color: #50fa7b; -fx-border-width: 1px; -fx-background-radius: 4px;");

        double costoCiencia = simulacion.getGestorColonizacion().calcularCostoCienciaPoblar(cuerpo.getTipoCuerpo());
        List<Planeta> origenes = simulacion.getGestorColonizacion()
                .obtenerPlanetasOrigenParaPoblar(cuerpo, simulacion.getSistemaSolar().getCuerpos());
        ComboBox<Planeta> comboOrigen = crearSelectorPlanetaOrigen(origenes);

        Button btnPoblar = new Button(String.format("Poblar (%.0f Ciencia)", costoCiencia));
        btnPoblar.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        btnPoblar.setStyle(TemaUI.CSS_BTN_PRIMARIO);
        btnPoblar.setOnAction(e -> {
            Planeta origenElegido = comboOrigen.getValue() != null ? comboOrigen.getValue()
                    : (origenes.isEmpty() ? null : origenes.get(0));
            simulacion.getGestorColonizacion().poblarDesde(
                    origenElegido, cuerpo, simulacion.getSistemaSolar().getCuerpos(), simulacion.getInventario(),
                    ev -> simulacion.notificarEvento(ev.mensaje(), ev.tipo()), simulacion.getReloj().getTickActual()
            );
            stage.close();
        });

        poblarBox.getChildren().addAll(
                crearTitulo("POBLAR PLANETA (COLONIZACIÓN)", TemaUI.COLOR_VERDE_EXITO),
                crearDato(String.format("• Costo: %.0f Ciencia | Bonificación materiales: x1.3", costoCiencia))
        );
        if (origenes.size() > 1) {
            poblarBox.getChildren().addAll(crearDato("• Elegir planeta que poblará (-10% pob.):"), comboOrigen);
        } else if (origenes.size() == 1) {
            Planeta u = origenes.get(0);
            poblarBox.getChildren().add(crearDato(String.format("• Origen: %s (%.0f hab.)", u.getNombre(), u.getCivilizacion().getPoblacion())));
        }
        poblarBox.getChildren().add(btnPoblar);
        return poblarBox;
    }

    private ComboBox<Planeta> crearSelectorPlanetaOrigen(List<Planeta> origenes) {
        ComboBox<Planeta> combo = new ComboBox<>();
        combo.getItems().addAll(origenes);
        if (!origenes.isEmpty()) combo.getSelectionModel().selectFirst();
        combo.setMaxWidth(Double.MAX_VALUE);
        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Planeta p) {
                if (p == null) return "";
                int pob = p.getCivilizacion() != null ? (int) p.getCivilizacion().getPoblacion() : 0;
                return p.getNombre() + " (" + p.getTipoCuerpo().nombre + " - " + pob + " hab.)";
            }
            @Override
            public Planeta fromString(String s) { return null; }
        });
        return combo;
    }

    public VBox construirTarjetaEscudo(Defensor def, SimulacionSolar simulacion, Stage stage) {
        VBox escudoBox = new VBox(6);
        escudoBox.setPadding(new Insets(10));
        escudoBox.setStyle("-fx-background-color: #1a1c26; -fx-border-color: #5be3ff; -fx-border-width: 1px; -fx-background-radius: 4px;");

        String estadoEscudo = def.tieneEscudo()
                ? "Escudo: " + def.getGolpesEscudoRestantes() + " golpes restantes"
                : "Escudo: Inactivo";
        escudoBox.getChildren().addAll(crearTitulo("DEFENSA PLANETARIA (ESCUDO)", TemaUI.COLOR_ACENTO_CYAN), crearDato(estadoEscudo));

        if (simulacion != null && def.puedeRecibirEscudo()) {
            Button btnActivarEscudo = new Button("Activar Escudo");
            btnActivarEscudo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
            btnActivarEscudo.setStyle(TemaUI.CSS_BTN_PRIMARIO);
            btnActivarEscudo.setOnAction(e -> {
                simulacion.getServicioEscudo().activar(simulacion, def);
                stage.close();
            });
            escudoBox.getChildren().add(btnActivarEscudo);
        }
        return escudoBox;
    }

    public VBox construirSeccionProduccion(CuerpoCeleste cuerpo, TipoCuerpo tipo, ProveedorInfoCuerpo seccionInfo) {
        VBox prodBox = new VBox(4);
        prodBox.setPadding(new Insets(10));
        prodBox.setStyle(TemaUI.CSS_SECCION_CARD);
        prodBox.getChildren().add(crearTitulo("GENERACIÓN ACTUAL POR TICK", TemaUI.COLOR_VERDE_EXITO));

        double factorMasa = tipo.masaBase > 0 ? Math.max(0.1, cuerpo.getMasa() / tipo.masaBase) : 1.0;
        boolean transformado = RegistroCapacidades.obtenerTransformable(cuerpo)
                .map(Transformable::isTransformadoPorEvento).orElse(false);
        Map<TipoRecurso, Double> prod = transformado
                ? Collections.emptyMap()
                : new EnumMap<>(InventarioJugador.getProduccionEscaladaPorTick(tipo, factorMasa));
        boolean esPoblado = RegistroCapacidades.obtenerCivilizable(cuerpo)
                .map(civ -> civ.tieneCivilizacion() && (tipo != TipoCuerpo.PLANETA_ROCOSO
                        || (civ.getCivilizacion() != null && civ.getCivilizacion().getPoblacion() >= 100.0)))
                .orElse(false);
        if (esPoblado && !prod.isEmpty()) {
            prod.replaceAll((k, v) -> v * ConfiguracionEconomia.MULTIPLICADOR_PRODUCCION_PLANETA_POBLADO);
        }
        seccionInfo.poblarListaProduccion(prodBox, prod);
        return prodBox;
    }

    private Label crearTitulo(String texto, Color color) {
        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lbl.setTextFill(color);
        return lbl;
    }

    private Label crearDato(String texto) {
        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        lbl.setTextFill(Color.web("#d1d5db"));
        return lbl;
    }
}
