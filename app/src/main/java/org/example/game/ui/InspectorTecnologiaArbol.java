package org.example.game.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.game.jugador.TipoRecurso;
import org.example.game.progresion.MejoraSecundaria;
import org.example.game.progresion.RamaProgresion;
import org.example.game.simulacion.MensajeEvento;
import org.example.game.simulacion.SimulacionSolar;

import java.util.List;

/**
 * Panel lateral de inspección y acción para el Árbol Neuronal de Investigación Científica (SRP).
 * Principios SOLID:
 * - SRP: Responsable exclusivo de presentar el detalle del nodo principal o mejora secundaria seleccionada y ejecutar su desbloqueo.
 */
public class InspectorTecnologiaArbol extends VBox {

    private final SimulacionSolar simulacion;
    private final Runnable onInvestigadoCallback;

    private final Label lblInspRama;
    private final Label lblInspTitulo;
    private final StackPane inspCirclePreview;
    private final Label lblInspTagPreview;
    private final Label lblInspDesc;
    private final Label lblInspCosto;
    private final ProgressBar inspBarraCiencia;
    private final Label lblInspProgresoCiencia;
    private final Button btnInspInvestigar;

    private PantallaArbolProgresion.NodoArbol nodoSeleccionado = null;
    private RamaProgresion ramaSeleccionada = null;
    private MejoraSecundaria mejoraSecundariaSeleccionada = null;

    public InspectorTecnologiaArbol(SimulacionSolar simulacion, Runnable onInvestigadoCallback) {
        super(8);
        this.simulacion = simulacion;
        this.onInvestigadoCallback = onInvestigadoCallback;

        setPrefWidth(260);
        setMinWidth(260);
        setMaxWidth(260);
        setPadding(new Insets(12));
        setStyle("-fx-background-color: #0f121d; -fx-border-color: #2b3149; -fx-border-width: 1.5px; -fx-background-radius: 6px; -fx-border-radius: 6px;");

        lblInspRama = crearLabel("RAMA TECNOLÓGICA", FontWeight.BOLD, 9.5, "#8c92a4");
        lblInspTitulo = crearLabel("Selecciona un Nodo", FontWeight.BOLD, 14, "#e8e4d8");
        lblInspTitulo.setWrapText(true);

        inspCirclePreview = new StackPane();
        inspCirclePreview.setPrefSize(58, 58);
        inspCirclePreview.setMaxSize(58, 58);
        inspCirclePreview.setStyle("-fx-background-color: #172a38; -fx-background-radius: 50%; -fx-border-color: #5be3ff; -fx-border-width: 2px; -fx-border-radius: 50%;");

        lblInspTagPreview = crearLabel("[?]", FontWeight.BOLD, 13, "#5be3ff");
        inspCirclePreview.getChildren().add(lblInspTagPreview);

        lblInspDesc = crearLabel("Haz clic sobre cualquier nodo principal o sinapsis secundaria para inspeccionar su efecto, costo y estado.", FontWeight.NORMAL, 10, "#a0a5b5");
        lblInspDesc.setWrapText(true);
        VBox.setVgrow(lblInspDesc, Priority.ALWAYS);

        VBox costoBox = new VBox(3);
        lblInspCosto = crearLabel("Costo: -- CIE", FontWeight.BOLD, 11, "#ffd700");

        inspBarraCiencia = new ProgressBar(0.0);
        inspBarraCiencia.setMaxWidth(Double.MAX_VALUE);
        inspBarraCiencia.setPrefHeight(10);
        inspBarraCiencia.setStyle("-fx-accent: #5be3ff;");

        lblInspProgresoCiencia = crearLabel("Ciencia: -- / --", FontWeight.NORMAL, 9.5, "#8c92a4");
        costoBox.getChildren().addAll(lblInspCosto, inspBarraCiencia, lblInspProgresoCiencia);

        btnInspInvestigar = new Button("Investigar Tecnología");
        btnInspInvestigar.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        btnInspInvestigar.setMaxWidth(Double.MAX_VALUE);
        btnInspInvestigar.setDisable(true);
        btnInspInvestigar.setStyle("-fx-background-color: #1a1c26; -fx-text-fill: #777777; -fx-border-color: #333333; -fx-border-width: 1px; -fx-padding: 6 10 6 10;");
        btnInspInvestigar.setOnAction(e -> ejecutarInvestigacion());

        getChildren().addAll(lblInspRama, lblInspTitulo, inspCirclePreview, new Separator(), lblInspDesc, costoBox, btnInspInvestigar);
    }

    private Label crearLabel(String texto, FontWeight peso, double tamano, String colorHex) {
        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Monospace", peso, tamano));
        lbl.setTextFill(Color.web(colorHex));
        return lbl;
    }

    public void seleccionar(RamaProgresion rama, PantallaArbolProgresion.NodoArbol nodo) {
        this.mejoraSecundariaSeleccionada = null;
        this.ramaSeleccionada = rama;
        this.nodoSeleccionado = nodo;
        actualizar();
    }

    public void seleccionarSecundaria(MejoraSecundaria mejora) {
        this.mejoraSecundariaSeleccionada = mejora;
        this.ramaSeleccionada = mejora != null ? mejora.rama() : null;
        this.nodoSeleccionado = null;
        actualizar();
    }

    public void actualizar() {
        if (mejoraSecundariaSeleccionada != null) {
            actualizarVistaSecundaria();
            return;
        }
        if (nodoSeleccionado == null || ramaSeleccionada == null) return;
        actualizarVistaPrincipal();
    }

    private void actualizarVistaSecundaria() {
        var prog = simulacion.getProgresion();
        var inv = simulacion.getInventario();
        double ciencia = inv != null ? inv.getRecurso(TipoRecurso.CIENCIA) : 0.0;
        int nivelActual = prog.getNivel(mejoraSecundariaSeleccionada.rama());

        lblInspRama.setText("SINAPSIS SECUNDARIA · " + mejoraSecundariaSeleccionada.rama().name() + " (T" + mejoraSecundariaSeleccionada.tier() + ")");
        lblInspTitulo.setText(mejoraSecundariaSeleccionada.nombre());
        lblInspTagPreview.setText(mejoraSecundariaSeleccionada.tag());
        lblInspDesc.setText(mejoraSecundariaSeleccionada.descripcion() + "\n\nEfecto: " + mejoraSecundariaSeleccionada.formatearEfecto());

        if (prog.estaInvestigada(mejoraSecundariaSeleccionada)) {
            aplicarEstadoInvestigado("Mejora secundaria activa en el sistema.", "Mejora Desbloqueada");
        } else if (nivelActual >= mejoraSecundariaSeleccionada.tier()) {
            aplicarEstadoDisponible(ciencia, mejoraSecundariaSeleccionada.costo(), "INVESTIGAR MEJORA");
        } else {
            aplicarEstadoBloqueado(
                    String.format("Bloqueado (Requiere Nodo T%d)", mejoraSecundariaSeleccionada.tier()),
                    "Investiga primero el nodo principal T" + mejoraSecundariaSeleccionada.tier() + " de esta dendrita."
            );
        }
    }

    private void actualizarVistaPrincipal() {
        var prog = simulacion.getProgresion();
        var inv = simulacion.getInventario();
        double ciencia = inv != null ? inv.getRecurso(TipoRecurso.CIENCIA) : 0.0;
        int nivelActual = prog.getNivel(ramaSeleccionada);

        lblInspRama.setText("DENDRITA: " + ramaSeleccionada.name());
        lblInspTitulo.setText(nodoSeleccionado.nombre());
        lblInspTagPreview.setText(nodoSeleccionado.tag());
        lblInspDesc.setText(construirDescripcionConSecundarias(prog));

        if (nodoSeleccionado.nivel() <= nivelActual) {
            aplicarEstadoInvestigado("Tecnología activa en el sistema.", "Tecnología Desbloqueada");
        } else if (nodoSeleccionado.nivel() == nivelActual + 1) {
            aplicarEstadoDisponible(ciencia, nodoSeleccionado.costo(), "INVESTIGAR AHORA");
        } else {
            aplicarEstadoBloqueado(
                    String.format("Bloqueado (Requiere Nivel %d)", nodoSeleccionado.nivel() - 1),
                    "Investiga primero la tecnología previa."
            );
        }
    }

    private String construirDescripcionConSecundarias(org.example.game.progresion.SistemaProgresion prog) {
        StringBuilder sb = new StringBuilder(nodoSeleccionado.desc());
        List<MejoraSecundaria> secundarias = MejoraSecundaria.deRamaYTier(ramaSeleccionada, nodoSeleccionado.nivel());
        if (!secundarias.isEmpty()) {
            sb.append("\n\nBifurcaciones T").append(nodoSeleccionado.nivel()).append(":");
            for (MejoraSecundaria m : secundarias) {
                String estado = prog.estaInvestigada(m)
                        ? "INVESTIGADA"
                        : (prog.getNivel(ramaSeleccionada) >= m.tier() ? "DISPONIBLE" : "BLOQUEADA");
                sb.append("\n• ").append(m.nombre())
                        .append(" (").append(m.formatearEfecto())
                        .append(" | ").append(String.format("%.0f CIE", m.costo()))
                        .append(" | ").append(estado).append(")");
            }
        }
        return sb.toString();
    }

    private void aplicarEstadoInvestigado(String subtexto, String textoBoton) {
        lblInspCosto.setText("Estado: ¡INVESTIGADO!");
        lblInspCosto.setTextFill(Color.web("#50fa7b"));
        inspBarraCiencia.setProgress(1.0);
        lblInspProgresoCiencia.setText(subtexto);
        btnInspInvestigar.setDisable(true);
        btnInspInvestigar.setText(textoBoton);
        btnInspInvestigar.setStyle("-fx-background-color: #102619; -fx-text-fill: #50fa7b; -fx-border-color: #50fa7b; -fx-border-width: 1.2px; -fx-padding: 6 10 6 10;");
        inspCirclePreview.setStyle("-fx-background-color: #102619; -fx-background-radius: 50%; -fx-border-color: #50fa7b; -fx-border-width: 2.5px; -fx-border-radius: 50%;");
        lblInspTagPreview.setTextFill(Color.web("#50fa7b"));
    }

    private void aplicarEstadoDisponible(double ciencia, double costo, String textoBoton) {
        lblInspCosto.setText(String.format("Costo: %.0f CIE", costo));
        lblInspCosto.setTextFill(Color.web("#ffd700"));

        double progreso = costo > 0 ? Math.min(1.0, ciencia / costo) : 1.0;
        inspBarraCiencia.setProgress(progreso);
        lblInspProgresoCiencia.setText(String.format("Ciencia: %.1f / %.0f CIE", ciencia, costo));

        boolean puedePagar = ciencia >= costo;
        btnInspInvestigar.setDisable(!puedePagar);
        btnInspInvestigar.setText(puedePagar ? textoBoton : "Ciencia Insuficiente");
        btnInspInvestigar.setStyle(puedePagar
                ? "-fx-background-color: #1e3a5f; -fx-text-fill: #5be3ff; -fx-border-color: #5be3ff; -fx-border-width: 1.5px; -fx-cursor: hand; -fx-padding: 6 10 6 10; -fx-background-radius: 4px; -fx-border-radius: 4px;"
                : "-fx-background-color: #1a1c26; -fx-text-fill: #777777; -fx-border-color: #333333; -fx-border-width: 1px; -fx-padding: 6 10 6 10; -fx-background-radius: 4px; -fx-border-radius: 4px;");
        inspCirclePreview.setStyle("-fx-background-color: #1a233a; -fx-background-radius: 50%; -fx-border-color: #ffd700; -fx-border-width: 2.5px; -fx-border-radius: 50%;");
        lblInspTagPreview.setTextFill(Color.web("#ffd700"));
    }

    private void aplicarEstadoBloqueado(String textoCosto, String subtexto) {
        lblInspCosto.setText(textoCosto);
        lblInspCosto.setTextFill(Color.web("#777777"));
        inspBarraCiencia.setProgress(0.0);
        lblInspProgresoCiencia.setText(subtexto);
        btnInspInvestigar.setDisable(true);
        btnInspInvestigar.setText("Tecnología Bloqueada");
        btnInspInvestigar.setStyle("-fx-background-color: #141620; -fx-text-fill: #555555; -fx-border-color: #2b2d3a; -fx-border-width: 1px; -fx-padding: 6 10 6 10;");
        inspCirclePreview.setStyle("-fx-background-color: #10121a; -fx-background-radius: 50%; -fx-border-color: #2b2d3a; -fx-border-width: 1.5px; -fx-border-radius: 50%;");
        lblInspTagPreview.setTextFill(Color.web("#555555"));
    }

    private void ejecutarInvestigacion() {
        var inv = simulacion.getInventario();
        var prog = simulacion.getProgresion();
        if (mejoraSecundariaSeleccionada != null) {
            if (prog.investigarSecundaria(mejoraSecundariaSeleccionada, inv)) {
                simulacion.notificarEvento("Mejora dendrítica completada: " + mejoraSecundariaSeleccionada.nombre(), MensajeEvento.TipoMensaje.EXITO);
                if (onInvestigadoCallback != null) {
                    onInvestigadoCallback.run();
                }
                actualizar();
            }
            return;
        }
        if (nodoSeleccionado != null && ramaSeleccionada != null) {
            if (prog.investigar(ramaSeleccionada, inv)) {
                simulacion.notificarEvento("Investigación completada: " + nodoSeleccionado.nombre(), MensajeEvento.TipoMensaje.EXITO);
                if (onInvestigadoCallback != null) {
                    onInvestigadoCallback.run();
                }
                actualizar();
            }
        }
    }

    public PantallaArbolProgresion.NodoArbol getNodoSeleccionado() {
        return nodoSeleccionado;
    }

    public RamaProgresion getRamaSeleccionada() {
        return ramaSeleccionada;
    }

    public MejoraSecundaria getMejoraSecundariaSeleccionada() {
        return mejoraSecundariaSeleccionada;
    }

    public Button getBtnInspInvestigar() {
        return btnInspInvestigar;
    }

    public Label getLblInspTitulo() {
        return lblInspTitulo;
    }

    public Label getLblInspDesc() {
        return lblInspDesc;
    }

    public Label getLblInspCosto() {
        return lblInspCosto;
    }
}
