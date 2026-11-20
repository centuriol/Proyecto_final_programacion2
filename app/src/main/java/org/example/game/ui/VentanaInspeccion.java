package org.example.game.ui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.example.game.civilizacion.SistemaCivilizaciones;
import org.example.game.controlador.ControladorMouse;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.ServicioEscudo;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.simulacion.SimulacionSolar;

/**
 * Ventana modal orientada a objetos para inspeccionar un cuerpo celeste en el sistema orbital (SRP / OCP).
 */
public class VentanaInspeccion {

    private static final ConstructorTarjetasInspeccion CONSTRUCTOR_TARJETAS = new ConstructorTarjetasInspeccion();
    private static final FabricaUI FABRICA_UI = new FabricaUI();

    private final CuerpoCeleste cuerpo;
    private final SimulacionSolar simulacion;
    private final Window owner;
    private final ProveedorInfoCuerpo seccionInfo;

    public VentanaInspeccion(CuerpoCeleste cuerpo, SimulacionSolar simulacion, Window owner, ProveedorInfoCuerpo seccionInfo) {
        this.cuerpo = cuerpo;
        this.simulacion = simulacion;
        this.owner = owner;
        this.seccionInfo = seccionInfo != null ? seccionInfo : FABRICA_UI.crearProveedorInfoCuerpo();
    }

    public VentanaInspeccion(CuerpoCeleste cuerpo, SimulacionSolar simulacion, Window owner) {
        this(cuerpo, simulacion, owner, FABRICA_UI.crearProveedorInfoCuerpo());
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
        VBox datosFisicos = CONSTRUCTOR_TARJETAS.construirSeccionTelemetria(cuerpo);
        agregarDetallesOpcionales(datosFisicos, stage);
        VBox prodBox = CONSTRUCTOR_TARJETAS.construirSeccionProduccion(cuerpo, tipo, seccionInfo);
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

    private void agregarDetallesOpcionales(VBox datosFisicos, Stage stage) {
        SistemaCivilizaciones.getInstancia().obtenerCivilizable(cuerpo).ifPresent(civ -> {
            if (civ.tieneCivilizacion() && civ.getCivilizacion() != null) {
                datosFisicos.getChildren().add(CONSTRUCTOR_TARJETAS.construirTarjetaCivilizacion(civ.getCivilizacion()));
            }
        });
        if (simulacion != null && simulacion.getGestorColonizacion().puedePoblar(
                cuerpo, simulacion.getSistemaSolar().getCuerpos(), simulacion.getInventario())) {
            datosFisicos.getChildren().add(CONSTRUCTOR_TARJETAS.construirTarjetaPoblar(cuerpo, simulacion, stage));
        }
        ServicioEscudo.getInstanciaPorDefecto().obtenerDefensor(cuerpo).ifPresent(def -> {
            if (def.puedeRecibirEscudo()) {
                datosFisicos.getChildren().add(CONSTRUCTOR_TARJETAS.construirTarjetaEscudo(def, simulacion, stage));
            }
        });
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
