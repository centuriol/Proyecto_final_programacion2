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
import org.example.game.civilizacion.CivilizacionComponent;
import org.example.game.controlador.ControladorMouse;
import org.example.game.cuerpo.*;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.ServicioReembolso;
import org.example.game.jugador.TipoRecurso;
import org.example.game.simulacion.SimulacionSolar;
import java.util.Collections;
import java.util.Map;
/**
 * Ventana modal orientada a objetos para inspeccionar un cuerpo celeste en el sistema orbital (SRP / OCP).
 */
public class VentanaInspeccion {

    private final CuerpoCeleste cuerpo;
    private final SimulacionSolar simulacion;
    private final Window owner;
    private final ProveedorInfoCuerpo seccionInfo;

    public VentanaInspeccion(CuerpoCeleste cuerpo, SimulacionSolar simulacion, Window owner, ProveedorInfoCuerpo seccionInfo) {
        this.cuerpo = cuerpo;
        this.simulacion = simulacion;
        this.owner = owner;
        this.seccionInfo = seccionInfo != null ? seccionInfo : new SeccionInfoCuerpo();
    }

    public VentanaInspeccion(CuerpoCeleste cuerpo, SimulacionSolar simulacion, Window owner) {
        this(cuerpo, simulacion, owner, new SeccionInfoCuerpo());
    }

    public static void mostrar(CuerpoCeleste cuerpo, SimulacionSolar simulacion, Window owner, ControladorMouse controladorMouse) {
        if (cuerpo == null || !cuerpo.esInteractuablePorUsuario() || cuerpo.getTipoCuerpo() == null) {
            return;
        }
        new VentanaInspeccion(cuerpo, simulacion, owner).abrir();
    }

    public void abrir() {
        Stage stage = new Stage();
        VBox root = seccionInfo.inicializarDialogo(stage, "Inspeccionar: " + cuerpo.getNombre(), owner);
        TipoCuerpo tipo = cuerpo.getTipoCuerpo();

        HBox header = seccionInfo.crearHeader(
                seccionInfo.crearNodoIcono(tipo),
                cuerpo.getNombre(),
                tipo.nombre + " • " + seccionInfo.obtenerSubtitulo(tipo)
        );

        VBox descBox = seccionInfo.crearSeccionDescripcion(tipo);
        VBox datosFisicos = construirSeccionTelemetria();
        agregarDetallesOpcionales(datosFisicos, stage);
        VBox prodBox = construirSeccionProduccion(tipo);
        HBox footer = construirFooter(stage);

        root.getChildren().addAll(header, new Separator(), descBox, datosFisicos, prodBox, new Separator(), footer);

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #13151f; -fx-background-color: #13151f;");

        Scene scene = new Scene(scroll, 540, 580);
        seccionInfo.vincularCierrePorEscape(scene, stage);
        stage.setScene(scene);
        stage.show();
    }

    private VBox construirSeccionTelemetria() {
        VBox datosFisicos = new VBox(4);
        datosFisicos.setPadding(new Insets(10));
        datosFisicos.setStyle(TemaUI.CSS_SECCION_CARD);

        Label lblDatosTitulo = new Label("TELEMETRÍA Y PROPIEDADES FÍSICAS");
        lblDatosTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblDatosTitulo.setTextFill(TemaUI.COLOR_ORO);

        Label lblMasa = new Label("• Masa: " + org.example.game.cuerpo.FormateadorMasa.formatearMasa(cuerpo.getMasa()));
        lblMasa.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        lblMasa.setTextFill(Color.web("#d1d5db"));

        Label lblVel = new Label(String.format("• Velocidad: %.2f km/s", cuerpo.getVelocidad().magnitud()));
        lblVel.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        lblVel.setTextFill(Color.web("#d1d5db"));

        Label lblPos = new Label(String.format("• Posición: (%.1f, %.1f)", cuerpo.getPosicion().x(), cuerpo.getPosicion().y()));
        lblPos.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        lblPos.setTextFill(Color.web("#d1d5db"));

        datosFisicos.getChildren().addAll(lblDatosTitulo, lblMasa, lblVel, lblPos);
        return datosFisicos;
    }

    private void agregarDetallesOpcionales(VBox datosFisicos, Stage stage) {
        org.example.game.civilizacion.SistemaCivilizaciones.getInstancia().obtenerCivilizable(cuerpo).ifPresent(civ -> {
            if (civ.tieneCivilizacion() && civ.getCivilizacion() != null) {
                datosFisicos.getChildren().add(construirTarjetaCivilizacion(civ.getCivilizacion()));
            }
        });
        if (simulacion != null && simulacion.getGestorColonizacion().puedePoblar(
                cuerpo, simulacion.getSistemaSolar().getCuerpos(), simulacion.getInventario())) {
            datosFisicos.getChildren().add(construirTarjetaPoblar(stage));
        }
        org.example.game.cuerpo.ServicioEscudo.getInstanciaPorDefecto().obtenerDefensor(cuerpo).ifPresent(def -> {
            if (def.puedeRecibirEscudo()) {
                datosFisicos.getChildren().add(construirTarjetaEscudo(def, stage));
            }
        });
    }

    private VBox construirTarjetaPoblar(Stage stage) {
        VBox poblarBox = new VBox(6);
        poblarBox.setPadding(new Insets(10));
        poblarBox.setStyle("-fx-background-color: #1a1c26; -fx-border-color: #50fa7b; -fx-border-width: 1px; -fx-background-radius: 4px;");

        Label lblTitulo = new Label("POBLAR PLANETA (COLONIZACIÓN)");
        lblTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblTitulo.setTextFill(TemaUI.COLOR_VERDE_EXITO);

        double costoCiencia = simulacion.getGestorColonizacion().calcularCostoCienciaPoblar(cuerpo.getTipoCuerpo());
        Label lblCosto = new Label(String.format("• Costo: %.0f Ciencia | Bonificación materiales: x1.3", costoCiencia));
        lblCosto.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        lblCosto.setTextFill(Color.web("#d1d5db"));

        java.util.List<Planeta> origenes = simulacion.getGestorColonizacion()
                .obtenerPlanetasOrigenParaPoblar(cuerpo, simulacion.getSistemaSolar().getCuerpos());
        ComboBox<Planeta> comboOrigen = crearSelectorPlanetaOrigen(origenes);

        Button btnPoblar = new Button(String.format("Poblar (%.0f Ciencia)", costoCiencia));
        btnPoblar.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        btnPoblar.setStyle(TemaUI.CSS_BTN_PRIMARIO);
        btnPoblar.setOnAction(e -> {
            Planeta origenElegido = comboOrigen.getValue() != null ? comboOrigen.getValue()
                    : (origenes.isEmpty() ? null : origenes.get(0));
            simulacion.getGestorColonizacion().poblarDesde(
                    origenElegido,
                    cuerpo,
                    simulacion.getSistemaSolar().getCuerpos(),
                    simulacion.getInventario(),
                    ev -> simulacion.notificarEvento(ev.mensaje(), ev.tipo()),
                    simulacion.getReloj().getTickActual()
            );
            stage.close();
        });

        poblarBox.getChildren().addAll(lblTitulo, lblCosto);
        if (origenes.size() > 1) {
            Label lblElegir = new Label("• Elegir planeta que poblará (-10% pob.):");
            lblElegir.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
            lblElegir.setTextFill(Color.web("#d1d5db"));
            poblarBox.getChildren().addAll(lblElegir, comboOrigen);
        } else if (origenes.size() == 1) {
            Planeta u = origenes.get(0);
            Label lblOrigen = new Label(String.format("• Origen: %s (%.0f hab.)", u.getNombre(), u.getCivilizacion().getPoblacion()));
            lblOrigen.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
            lblOrigen.setTextFill(Color.web("#d1d5db"));
            poblarBox.getChildren().add(lblOrigen);
        }
        poblarBox.getChildren().add(btnPoblar);
        return poblarBox;
    }

    private ComboBox<Planeta> crearSelectorPlanetaOrigen(java.util.List<Planeta> origenes) {
        ComboBox<Planeta> combo = new ComboBox<>();
        combo.getItems().addAll(origenes);
        if (!origenes.isEmpty()) {
            combo.getSelectionModel().selectFirst();
        }
        combo.setMaxWidth(Double.MAX_VALUE);
        javafx.util.StringConverter<Planeta> converter = new javafx.util.StringConverter<>() {
            @Override
            public String toString(Planeta p) {
                if (p == null) return "";
                int pob = p.getCivilizacion() != null ? (int) p.getCivilizacion().getPoblacion() : 0;
                return p.getNombre() + " (" + p.getTipoCuerpo().nombre + " - " + pob + " hab.)";
            }
            @Override
            public Planeta fromString(String s) { return null; }
        };
        combo.setConverter(converter);
        return combo;
    }

    private VBox construirTarjetaCivilizacion(CivilizacionComponent comp) {
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
        return civBox;
    }

    private VBox construirTarjetaEscudo(Defensor def, Stage stage) {
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

    private VBox construirSeccionProduccion(TipoCuerpo tipo) {
        VBox prodBox = new VBox(4);
        prodBox.setPadding(new Insets(10));
        prodBox.setStyle(TemaUI.CSS_SECCION_CARD);

        Label lblProdTitulo = new Label("GENERACIÓN ACTUAL POR TICK");
        lblProdTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblProdTitulo.setTextFill(TemaUI.COLOR_VERDE_EXITO);
        prodBox.getChildren().add(lblProdTitulo);

        double factorMasa = tipo.masaBase > 0 ? Math.max(0.1, cuerpo.getMasa() / tipo.masaBase) : 1.0;
        boolean transformado = RegistroCapacidades.obtenerTransformable(cuerpo)
                .map(Transformable::isTransformadoPorEvento)
                .orElse(false);
        Map<TipoRecurso, Double> prod = transformado
                ? Collections.emptyMap()
                : new java.util.EnumMap<>(InventarioJugador.getProduccionEscaladaPorTick(tipo, factorMasa));
        boolean esPoblado = RegistroCapacidades.obtenerCivilizable(cuerpo)
                .map(civ -> civ.tieneCivilizacion() && (tipo != TipoCuerpo.PLANETA_ROCOSO
                        || (civ.getCivilizacion() != null && civ.getCivilizacion().getPoblacion() >= 100.0)))
                .orElse(false);
        if (esPoblado && !prod.isEmpty()) {
            prod.replaceAll((k, v) -> v * org.example.game.simulacion.ConfiguracionEconomia.MULTIPLICADOR_PRODUCCION_PLANETA_POBLADO);
        }
        seccionInfo.poblarListaProduccion(prodBox, prod);
        return prodBox;
    }

    private HBox construirFooter(Stage stage) {
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
                simulacion.getServicioReembolso().desmantelarCuerpo(simulacion, cuerpo);
            }
            stage.close();
        });

        footer.getChildren().addAll(btnEliminar, btnCerrar);
        return footer;
    }
}
