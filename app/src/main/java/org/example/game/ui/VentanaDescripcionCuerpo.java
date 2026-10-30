package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.example.game.cuerpo.Civilizable;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Defensor;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.cuerpo.Transformable;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.SimulacionSolar;

import java.util.Map;

/**
 * Ventana emergente interactiva para:
 * 1. Configurar y colocar un nuevo cuerpo celeste (nombre, masa seleccionable,
 *    cálculo dinámico en tiempo real de costos y producción por tick).
 * 2. Inspeccionar la información y lore de un cuerpo celeste existente en el sistema.
 */
public class VentanaDescripcionCuerpo {

    /**
     * Abre la ventana interactiva para configurar y colocar un nuevo cuerpo celeste.
     * Permite elegir el nombre, regular la masa mediante un slider, y ver en tiempo real
     * cómo escalan los costos de materiales necesarios y la producción por tick generada.
     *
     * @param tipo Tipo de cuerpo celeste a colocar.
     * @param simulacion Instancia de la simulación activa.
     * @param owner Ventana propietaria (Stage principal).
     * @param onConfirmado Callback ejecutado tras confirmar la colocación.
     */
    public static void mostrarParaColocar(TipoCuerpo tipo, SimulacionSolar simulacion, Window owner, Runnable onConfirmado) {
        if (tipo == null) return;

        Stage stage = new Stage();
        VBox root = inicializarDialogo(stage, "Configurar y Colocar: " + tipo.nombre, owner);

        // 1. Cabecera: Icono y Nombre del Tipo
        javafx.scene.Node iconoNodo = crearNodoIcono(tipo);
        HBox header = crearHeader(iconoNodo, tipo.nombre, obtenerSubtitulo(tipo) + " • Configuración de Colocación");

        // 2. Descripción narrativa (Lore del ítem)
        VBox descBox = crearSeccionDescripcion(tipo);

        // 3. Características y propiedades físicas completas del tipo de cuerpo
        VBox caracBox = crearSeccionCaracteristicas(tipo);

        // 4. Formulario de Personalización: Nombre y Masa
        VBox formBox = new VBox(10);
        formBox.setPadding(new Insets(10));
        formBox.setStyle("-fx-background-color: #1a1c26; -fx-border-color: #2b2d3a; -fx-border-width: 1px; -fx-background-radius: 4px; -fx-border-radius: 4px;");

        // Campo de Nombre
        VBox campoNombreBox = new VBox(4);
        Label lblNombrePrompt = new Label("NOMBRE DEL CUERPO:");
        lblNombrePrompt.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblNombrePrompt.setTextFill(Color.web("#5be3ff"));

        TextField txtNombre = new TextField(obtenerNombrePorDefecto(tipo));
        txtNombre.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        txtNombre.setStyle("-fx-background-color: #0d0e15; -fx-text-fill: #ffffff; -fx-border-color: #5be3ff; -fx-border-width: 1px; -fx-border-radius: 3px; -fx-background-radius: 3px; -fx-padding: 5 8 5 8;");
        campoNombreBox.getChildren().addAll(lblNombrePrompt, txtNombre);

        // Selector de Masa
        VBox campoMasaBox = new VBox(4);
        double masaInicial = 1.0;
        if (simulacion != null) {
            masaInicial = Math.max(ConfiguracionSimulacion.MASA_FACTOR_MIN,
                    Math.min(ConfiguracionSimulacion.MASA_FACTOR_MAX, simulacion.getFactorMasaColocacion()));
        }

        double masaMax = (simulacion != null && simulacion.getProgresion() != null)
                ? simulacion.getProgresion().getLimiteMasaActual()
                : ConfiguracionSimulacion.MASA_FACTOR_MAX;
        WidgetFactorMasa widgetMasa = new WidgetFactorMasa("MASA DEL CUERPO:", masaInicial, tipo.masaBase, masaMax);
        if (tipo == TipoCuerpo.SATELITE) {
            Label lblSateliteFijo = new Label("ESTRUCTURA DE MASA FIJA: 1.0 t (No modificable)");
            lblSateliteFijo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
            lblSateliteFijo.setTextFill(Color.web("#5be3ff"));
            lblSateliteFijo.setPadding(new Insets(8));
            lblSateliteFijo.setStyle("-fx-background-color: #1a1c26; -fx-border-color: #5be3ff; -fx-border-width: 1px; -fx-background-radius: 4px;");
            campoMasaBox.getChildren().add(lblSateliteFijo);
        } else {
            campoMasaBox.getChildren().add(widgetMasa);
        }
        formBox.getChildren().addAll(campoNombreBox, campoMasaBox);

        // 4. Ficha Técnica Dinámica: Costos y Producción Escalados
        HBox econBox = new HBox(12);
        econBox.setAlignment(Pos.CENTER);

        // Columna Izquierda: Costos escalados
        VBox costoBox = new VBox(6);
        costoBox.setPadding(new Insets(10));
        costoBox.setStyle("-fx-background-color: #1a1c26; -fx-border-color: #2b2d3a; -fx-border-width: 1px; -fx-background-radius: 4px; -fx-border-radius: 4px;");
        HBox.setHgrow(costoBox, Priority.ALWAYS);

        Label lblCostoTitulo = new Label("MATERIALES PARA PONERLO");
        lblCostoTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblCostoTitulo.setTextFill(Color.web("#ffd700"));

        VBox listaCostos = new VBox(4);
        costoBox.getChildren().addAll(lblCostoTitulo, listaCostos);

        // Columna Derecha: Producción por tick escalada
        VBox prodBox = new VBox(6);
        prodBox.setPadding(new Insets(10));
        prodBox.setStyle("-fx-background-color: #1a1c26; -fx-border-color: #2b2d3a; -fx-border-width: 1px; -fx-background-radius: 4px; -fx-border-radius: 4px;");
        HBox.setHgrow(prodBox, Priority.ALWAYS);

        Label lblProdTitulo = new Label("MATERIALES QUE TE DA POR TICK");
        lblProdTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblProdTitulo.setTextFill(Color.web("#50fa7b"));

        VBox listaProd = new VBox(4);
        prodBox.getChildren().addAll(lblProdTitulo, listaProd);

        econBox.getChildren().addAll(costoBox, prodBox);

        // 5. Mensaje de Estado / Asequibilidad
        Label lblEstado = new Label();
        lblEstado.setFont(Font.font("System", FontWeight.NORMAL, 11));
        lblEstado.setWrapText(true);

        // 6. Botones de Acción (Cancelar y Colocar en Órbita)
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        btnCancelar.setStyle("-fx-background-color: #2b2d3a; -fx-text-fill: #8c92a4; -fx-border-color: #3b3d4f; -fx-border-width: 1px; -fx-cursor: hand; -fx-padding: 6 16 6 16;");
        btnCancelar.setOnAction(e -> stage.close());

        Button btnColocar = new Button("Colocar en Órbita");
        btnColocar.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        btnColocar.setStyle("-fx-background-color: #1e3a5f; -fx-text-fill: #5be3ff; -fx-border-color: #5be3ff; -fx-border-width: 1.5px; -fx-cursor: hand; -fx-padding: 6 18 6 18;");

        footer.getChildren().addAll(btnCancelar, btnColocar);

        // Lógica de Actualización Dinámica
        Runnable actualizarDatosDinamicos = () -> {
            double factorMasa = (tipo == TipoCuerpo.SATELITE) ? 1.0 : widgetMasa.getFactorMasa();

            // Actualizar lista de costos
            listaCostos.getChildren().clear();
            Map<TipoRecurso, Double> costosEscalados = InventarioJugador.getCostosEscalados(tipo, factorMasa);

            boolean puedePagarTodo = true;
            InventarioJugador inv = simulacion != null ? simulacion.getInventario() : null;

            if (costosEscalados.isEmpty()) {
                Label lblSinCosto = new Label("Sin costo de colocación");
                lblSinCosto.setFont(Font.font("System", FontWeight.NORMAL, 11));
                lblSinCosto.setTextFill(Color.web("#8c92a4"));
                listaCostos.getChildren().add(lblSinCosto);
            } else {
                for (Map.Entry<TipoRecurso, Double> e : costosEscalados.entrySet()) {
                    TipoRecurso tr = e.getKey();
                    double requerido = e.getValue();
                    double disponible = inv != null ? inv.getRecurso(tr) : 0;
                    boolean alcanza = disponible >= requerido;
                    if (!alcanza) puedePagarTodo = false;

                    Label lblItem = new Label(String.format("• %s: %.0f (Tienes: %.0f)",
                            tr.nombre, requerido, disponible));
                    lblItem.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
                    lblItem.setTextFill(Color.web(alcanza ? tr.getColorHex() : "#ff5555"));
                    listaCostos.getChildren().add(lblItem);
                }
            }

            // Actualizar lista de producción
            Map<TipoRecurso, Double> prodsEscaladas = InventarioJugador.getProduccionEscaladaPorTick(tipo, factorMasa);
            poblarListaProduccion(listaProd, prodsEscaladas);

            // Actualizar estado del botón de colocación y mensaje
            if (inv != null && !inv.puedeCrear(tipo, factorMasa)) {
                lblEstado.setText("Recursos insuficientes para este tamaño de cuerpo.");
                lblEstado.setTextFill(Color.web("#ff5555"));
                btnColocar.setDisable(true);
                btnColocar.setStyle("-fx-background-color: #2b2d3a; -fx-text-fill: #555866; -fx-border-color: #3b3d4f; -fx-border-width: 1px; -fx-padding: 6 18 6 18;");
            } else {
                lblEstado.setText("Recursos disponibles. Haz clic en 'Colocar en Órbita' y sitúa el cuerpo con el mouse.");
                lblEstado.setTextFill(Color.web("#50fa7b"));
                btnColocar.setDisable(false);
                btnColocar.setStyle("-fx-background-color: #1e3a5f; -fx-text-fill: #5be3ff; -fx-border-color: #5be3ff; -fx-border-width: 1.5px; -fx-cursor: hand; -fx-padding: 6 18 6 18;");
            }
        };

        widgetMasa.setOnFactorCambiado(val -> actualizarDatosDinamicos.run());
        actualizarDatosDinamicos.run();

        // Acción confirmar colocación
        btnColocar.setOnAction(e -> {
            String nombreFinal = txtNombre.getText() != null ? txtNombre.getText().trim() : "";
            if (nombreFinal.isBlank()) {
                nombreFinal = obtenerNombrePorDefecto(tipo);
            }
            double factorMasaFinal = (tipo == TipoCuerpo.SATELITE) ? 1.0 : widgetMasa.getFactorMasa();

            if (simulacion != null) {
                simulacion.entrarModoColocacion(tipo, factorMasaFinal, nombreFinal);
            }

            if (onConfirmado != null) {
                onConfirmado.run();
            }
            stage.close();
        });

        root.getChildren().addAll(header, new Separator(), descBox, caracBox, formBox, econBox, lblEstado, new Separator(), footer);

        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #13151f; -fx-background-color: #13151f;");

        Scene scene = new Scene(scroll, 580, 620);
        vincularCierrePorEscape(scene, stage);

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Muestra la ventana de información y descripción completa para un cuerpo celeste existente en el juego.
     * Se abre al hacer clic sobre un planeta, estrella o luna en el lienzo de simulación.
     */
    public static void mostrarParaInspeccionar(CuerpoCeleste cuerpo, Window owner) {
        mostrarParaInspeccionar(cuerpo, null, owner);
    }

    /**
     * Muestra la ventana de información y descripción interactiva de un cuerpo existente,
     * permitiendo eliminarlo y recuperar el 50% de los materiales requeridos para colocarlo.
     */
    public static void mostrarParaInspeccionar(CuerpoCeleste cuerpo, SimulacionSolar simulacion, Window owner) {
        mostrarParaInspeccionar(cuerpo, simulacion, owner, null);
    }

    public static void mostrarParaInspeccionar(CuerpoCeleste cuerpo, SimulacionSolar simulacion, Window owner, org.example.game.controlador.ControladorMouse controladorMouse) {
        if (cuerpo == null) return;
        TipoCuerpo tipo = cuerpo.getTipoCuerpo();
        if (tipo == null) return;

        Stage stage = new Stage();
        VBox root = inicializarDialogo(stage, "Inspeccionar: " + cuerpo.getNombre(), owner);

        // Cabecera: Icono y Nombre del cuerpo
        javafx.scene.Node iconoNodo = crearNodoIcono(tipo);
        HBox header = crearHeader(iconoNodo, cuerpo.getNombre(), tipo.nombre + " • " + obtenerSubtitulo(tipo));

        // Lore
        VBox descBox = crearSeccionDescripcion(tipo);

        // Datos Físicos Reales
        VBox datosFisicos = new VBox(4);
        datosFisicos.setPadding(new Insets(10));
        datosFisicos.setStyle("-fx-background-color: #1a1c26; -fx-border-color: #2b2d3a; -fx-border-width: 1px; -fx-background-radius: 4px; -fx-border-radius: 4px;");

        Label lblDatosTitulo = new Label("TELEMETRÍA Y PROPIEDADES FÍSICAS");
        lblDatosTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblDatosTitulo.setTextFill(Color.web("#ffd700"));

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

        // Producción actual del cuerpo
        VBox prodBox = new VBox(4);
        prodBox.setPadding(new Insets(10));
        prodBox.setStyle("-fx-background-color: #1a1c26; -fx-border-color: #2b2d3a; -fx-border-width: 1px; -fx-background-radius: 4px; -fx-border-radius: 4px;");

        Label lblProdTitulo = new Label("GENERACIÓN ACTUAL POR TICK");
        lblProdTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblProdTitulo.setTextFill(Color.web("#50fa7b"));
        prodBox.getChildren().add(lblProdTitulo);

        double factorMasa = tipo.masaBase > 0 ? Math.max(0.1, cuerpo.getMasa() / tipo.masaBase) : 1.0;
        Map<TipoRecurso, Double> prod = (cuerpo instanceof Transformable t && t.isTransformadoPorEvento())
                ? java.util.Collections.emptyMap()
                : InventarioJugador.getProduccionEscaladaPorTick(tipo, factorMasa);
        poblarListaProduccion(prodBox, prod);

        // Civilización si el cuerpo la alberga polimórficamente (LSP)
        if (cuerpo instanceof Civilizable civ && civ.tieneCivilizacion() && civ.getCivilizacion() != null) {
            var comp = civ.getCivilizacion();
            VBox civBox = new VBox(4);
            civBox.setPadding(new Insets(10));
            civBox.setStyle("-fx-background-color: #1a1c26; -fx-border-color: #5be3ff; -fx-border-width: 1px; -fx-background-radius: 4px; -fx-border-radius: 4px;");

            Label lblCivTitulo = new Label("CIVILIZACIÓN PLANETARIA");
            lblCivTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
            lblCivTitulo.setTextFill(Color.web("#5be3ff"));

            Label lblPob = new Label(String.format("• Población: %.0f habitantes", comp.getPoblacion()));
            lblPob.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
            lblPob.setTextFill(Color.web("#d1d5db"));

            Label lblEst = new Label("• Estado: " + comp.getEstado().name());
            lblEst.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
            lblEst.setTextFill(Color.web("#50fa7b"));

            civBox.getChildren().addAll(lblCivTitulo, lblPob, lblEst);
            datosFisicos.getChildren().add(civBox);
        }

        // Defensa por Escudo
        if (cuerpo instanceof Defensor def && def.puedeRecibirEscudo()) {
            VBox escudoBox = new VBox(6);
            escudoBox.setPadding(new Insets(10));
            escudoBox.setStyle("-fx-background-color: #1a1c26; -fx-border-color: #5be3ff; -fx-border-width: 1px; -fx-background-radius: 4px;");

            Label lblEscudoTitulo = new Label("DEFENSA PLANETARIA (ESCUDO)");
            lblEscudoTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
            lblEscudoTitulo.setTextFill(Color.web("#5be3ff"));

            String estadoEscudo = def.tieneEscudo()
                    ? "Escudo: " + def.getGolpesEscudoRestantes() + " golpes restantes"
                    : "Escudo: Inactivo";
            Label lblEstadoEscudo = new Label(estadoEscudo);
            lblEstadoEscudo.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
            lblEstadoEscudo.setTextFill(Color.web(def.tieneEscudo() ? "#50fa7b" : "#8c92a4"));

            double costoEscudo = (cuerpo instanceof org.example.game.cuerpo.Planeta p)
                    ? org.example.game.cuerpo.ServicioEscudo.calcularCostoEscudo(p)
                    : ConfiguracionSimulacion.COSTO_ACTIVACION_ESCUDO_ENERGIA;
            Button btnActivarEscudo = new Button(def.tieneEscudo()
                    ? String.format("Reactivar Escudo (%.0f ENE)", costoEscudo)
                    : String.format("Activar Escudo (%.0f ENE)", costoEscudo));
            btnActivarEscudo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
            btnActivarEscudo.setStyle("-fx-background-color: #2b2d3a; -fx-text-fill: #5be3ff; -fx-border-color: #5be3ff; -fx-border-width: 1px; -fx-cursor: hand; -fx-padding: 4 10 4 10;");
            btnActivarEscudo.setOnAction(e -> {
                if (cuerpo instanceof org.example.game.cuerpo.Planeta p) {
                    if (org.example.game.cuerpo.ServicioEscudo.activar(simulacion, p)) {
                        stage.close();
                    }
                }
            });

            escudoBox.getChildren().addAll(lblEscudoTitulo, lblEstadoEscudo, btnActivarEscudo);
            datosFisicos.getChildren().add(escudoBox);
        }

        // Misión de Colonización
        if (cuerpo instanceof org.example.game.cuerpo.Planeta p && simulacion != null && simulacion.getGestorColonizacion() != null) {
            if (simulacion.getGestorColonizacion().puedeColonizar(p, simulacion.getInventario())) {
                Button btnColonizar = new Button("Colonizar otro planeta");
                btnColonizar.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
                btnColonizar.setStyle("-fx-background-color: #2b2d3a; -fx-text-fill: #50fa7b; -fx-border-color: #50fa7b; -fx-border-width: 1px; -fx-cursor: hand; -fx-padding: 4 10 4 10;");
                btnColonizar.setOnAction(e -> {
                    stage.close();
                    if (controladorMouse != null) {
                        controladorMouse.iniciarModoColonizacion(p);
                    }
                });
                datosFisicos.getChildren().add(btnColonizar);
            }
        }

        // Sección de Reembolso / Reciclaje por Eliminación delegada al servicio de dominio (SRP / DRY)
        Map<TipoRecurso, Double> reembolso = org.example.game.jugador.ServicioReembolso.calcularReembolso(cuerpo);

        VBox refundBox = new VBox(4);
        refundBox.setPadding(new Insets(10));
        refundBox.setStyle("-fx-background-color: #1a1c26; -fx-border-color: #ff4757; -fx-border-width: 1px; -fx-background-radius: 4px; -fx-border-radius: 4px;");

        Label lblRefundTitulo = new Label("REEMBOLSO POR ELIMINAR CUERPO (50% DE MATERIALES)");
        lblRefundTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblRefundTitulo.setTextFill(Color.web("#ff6b81"));
        refundBox.getChildren().add(lblRefundTitulo);

        if (reembolso.isEmpty()) {
            Label lblSinCostos = new Label("• No reembolsa recursos.");
            lblSinCostos.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
            lblSinCostos.setTextFill(Color.web("#8c92a4"));
            refundBox.getChildren().add(lblSinCostos);
        } else {
            for (Map.Entry<TipoRecurso, Double> e : reembolso.entrySet()) {
                double cantidad = e.getValue();
                double original = cantidad / org.example.game.jugador.ServicioReembolso.FACTOR_REEMBOLSO;
                Label lblItem = new Label(String.format("• %s: +%.1f (50%% de %.1f)", e.getKey().nombre, cantidad, original));
                lblItem.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
                lblItem.setTextFill(Color.web(e.getKey().getColorHex()));
                refundBox.getChildren().add(lblItem);
            }
        }

        // Footer: Botón Eliminar (50% de reembolso) y Botón Cerrar
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_RIGHT);

        if (simulacion != null) {
            String textoEliminar = (cuerpo != null && cuerpo.permiteReembolso())
                    ? "Eliminar Cuerpo (+50% Reembolso)"
                    : "Eliminar Cuerpo (Sin Reembolso)";
            Button btnEliminar = new Button(textoEliminar);
            btnEliminar.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
            btnEliminar.setStyle("-fx-background-color: #5a1e28; -fx-text-fill: #ff6b81; -fx-border-color: #ff4757; -fx-border-width: 1.5px; -fx-cursor: hand; -fx-padding: 6 14 6 14; -fx-background-radius: 4px; -fx-border-radius: 4px;");

            btnEliminar.setOnAction(e -> {
                org.example.game.jugador.ServicioReembolso.desmantelarCuerpo(simulacion, cuerpo);
                stage.close();
            });

            footer.getChildren().add(btnEliminar);
        }

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        footer.getChildren().add(spacer);

        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        btnCerrar.setStyle("-fx-background-color: #2b2d3a; -fx-text-fill: #e8e4d8; -fx-border-color: #5be3ff; -fx-border-width: 1px; -fx-cursor: hand; -fx-padding: 6 18 6 18; -fx-background-radius: 4px; -fx-border-radius: 4px;");
        btnCerrar.setOnAction(e -> stage.close());
        footer.getChildren().add(btnCerrar);

        root.getChildren().addAll(header, new Separator(), descBox, datosFisicos, prodBox, refundBox, new Separator(), footer);

        javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(root);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #13151f; -fx-background-color: #13151f;");

        Scene scene = new Scene(scroll, 540, 580);
        vincularCierrePorEscape(scene, stage);

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Muestra la ventana en modo descripción informativa básica (compatibilidad hacia atrás).
     */
    public static void mostrar(TipoCuerpo tipo, Window owner) {
        mostrarParaColocar(tipo, null, owner, null);
    }

    public static String obtenerNombrePorDefecto(TipoCuerpo tipo) {
        return tipo != null ? tipo.getNombrePorDefecto() : "Cuerpo";
    }

    public static String obtenerIcono(TipoCuerpo tipo) {
        return tipo != null ? tipo.getIcono() : "[???]";
    }

    public static String obtenerSubtitulo(TipoCuerpo tipo) {
        return tipo != null ? tipo.getSubtitulo() : "Objeto Astronómico";
    }

    public static String obtenerLore(TipoCuerpo tipo) {
        return tipo != null ? tipo.getLore() : "Cuerpo celeste en el sistema.";
    }

    private static javafx.scene.Node crearNodoIcono(TipoCuerpo tipo) {
        if (org.example.game.render.GestorImagenes.tieneImagen(tipo)) {
            javafx.scene.image.ImageView imgView = org.example.game.render.GestorImagenes.crearImageView(tipo, 44, 44);
            javafx.scene.layout.StackPane imgBox = new javafx.scene.layout.StackPane(imgView);
            imgBox.setPrefSize(48, 48);
            imgBox.setStyle("-fx-background-color: #0d0e15; -fx-border-color: #5be3ff; -fx-border-width: 1.5px; -fx-background-radius: 4px; -fx-border-radius: 4px;");
            return imgBox;
        } else {
            Label lblIcono = new Label(obtenerIcono(tipo));
            lblIcono.setFont(Font.font("Monospace", FontWeight.BOLD, 22));
            lblIcono.setTextFill(Color.web(tipo.getColorHexString()));
            return lblIcono;
        }
    }

    private static HBox crearHeader(javafx.scene.Node iconoNodo, String titulo, String subtitulo) {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titulosBox = new VBox(2);
        Label lblTitulo = new Label(titulo.toUpperCase());
        lblTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 18));
        lblTitulo.setTextFill(Color.web("#e8e4d8"));

        Label lblSubtitulo = new Label(subtitulo);
        lblSubtitulo.setFont(Font.font("System", FontWeight.NORMAL, 12));
        lblSubtitulo.setTextFill(Color.web("#8c92a4"));

        titulosBox.getChildren().addAll(lblTitulo, lblSubtitulo);
        header.getChildren().addAll(iconoNodo, titulosBox);
        return header;
    }

    private static VBox crearSeccionDescripcion(TipoCuerpo tipo) {
        VBox descBox = new VBox(4);
        Label lblDescTitulo = new Label("DESCRIPCIÓN DEL CUERPO");
        lblDescTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblDescTitulo.setTextFill(Color.web("#5be3ff"));

        Label lblDescripcion = new Label(obtenerLore(tipo));
        lblDescripcion.setFont(Font.font("System", FontWeight.NORMAL, 12));
        lblDescripcion.setTextFill(Color.web("#d1d5db"));
        lblDescripcion.setWrapText(true);
        lblDescripcion.setLineSpacing(2);

        descBox.getChildren().addAll(lblDescTitulo, lblDescripcion);
        return descBox;
    }

    public static VBox crearSeccionCaracteristicas(TipoCuerpo tipo) {
        VBox box = new VBox(6);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #1a1c26; -fx-border-color: #2b2d3a; -fx-border-width: 1px; -fx-background-radius: 4px; -fx-border-radius: 4px;");

        Label lblTitulo = new Label("CARACTERÍSTICAS Y PROPIEDADES FÍSICAS");
        lblTitulo.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        lblTitulo.setTextFill(Color.web("#ffd700"));

        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(4);

        agregarFilaPropiedad(grid, 0, "Categoría:", tipo.categoria.name(), "#5be3ff");
        agregarFilaPropiedad(grid, 1, "Masa Base:", TipoCuerpo.formatearMasa(tipo.masaBase), "#e8e4d8");
        agregarFilaPropiedad(grid, 2, "Radio Físico:", String.format(java.util.Locale.US, "%,.0f km", tipo.radioFisicoBase / 1000.0), "#e8e4d8");
        agregarFilaPropiedad(grid, 3, "Temperatura:", String.format(java.util.Locale.US, "%.0f K (%.1f °C)", tipo.temperaturaBase, tipo.temperaturaBase - 273.15), "#e8e4d8");

        String habitabilidadStr = tipo.puedeTenerCivilizacion
                ? "Apto (Alberga y prolifera civilización)"
                : "No habitable (Ambiente inhóspito)";
        String habColor = tipo.puedeTenerCivilizacion ? "#50fa7b" : "#8c92a4";
        agregarFilaPropiedad(grid, 4, "Habitabilidad:", habitabilidadStr, habColor);

        String gravStr = tipo.generaGravedadFuerte
                ? "Dominante (Atracción gravitatoria mayor)"
                : "Estándar (Interacción orbital normal)";
        agregarFilaPropiedad(grid, 5, "Gravedad:", gravStr, tipo.generaGravedadFuerte ? "#ffd700" : "#d1d5db");

        String inflStr = tipo.esMasivo ? "Cuerpo masivo principal" : "Cuerpo orbital menor";
        agregarFilaPropiedad(grid, 6, "Dinámica:", inflStr, "#5be3ff");

        box.getChildren().addAll(lblTitulo, grid);
        return box;
    }

    private static void agregarFilaPropiedad(GridPane grid, int fila, String etiqueta, String valor, String colorValor) {
        Label lblEtiqueta = new Label("• " + etiqueta);
        lblEtiqueta.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        lblEtiqueta.setTextFill(Color.web("#8c92a4"));

        Label lblValor = new Label(valor);
        lblValor.setFont(Font.font("Monospace", FontWeight.NORMAL, 11));
        lblValor.setTextFill(Color.web(colorValor));

        grid.add(lblEtiqueta, 0, fila);
        grid.add(lblValor, 1, fila);
    }

    private static VBox inicializarDialogo(Stage stage, String titulo, Window owner) {
        stage.setTitle(titulo);
        if (owner != null) {
            stage.initOwner(owner);
        }
        stage.initModality(Modality.NONE);

        VBox root = new VBox(12);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: #13151f; -fx-border-color: #5be3ff; -fx-border-width: 2px; -fx-background-radius: 6px; -fx-border-radius: 6px;");
        return root;
    }

    private static void poblarListaProduccion(VBox listaProd, Map<TipoRecurso, Double> prodsEscaladas) {
        listaProd.getChildren().clear();
        if (prodsEscaladas == null || prodsEscaladas.isEmpty()) {
            Label lblSinProd = new Label("No genera recursos pasivos");
            lblSinProd.setFont(Font.font("System", FontWeight.NORMAL, 11));
            lblSinProd.setTextFill(Color.web("#8c92a4"));
            listaProd.getChildren().add(lblSinProd);
        } else {
            for (Map.Entry<TipoRecurso, Double> e : prodsEscaladas.entrySet()) {
                Label lblItem = new Label(String.format("• %s: +%.2f / tick", e.getKey().nombre, e.getValue()));
                lblItem.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
                lblItem.setTextFill(Color.web(e.getKey().getColorHex()));
                listaProd.getChildren().add(lblItem);
            }
        }
    }

    private static void vincularCierrePorEscape(Scene scene, Stage stage) {
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                stage.close();
            }
        });
    }
}
