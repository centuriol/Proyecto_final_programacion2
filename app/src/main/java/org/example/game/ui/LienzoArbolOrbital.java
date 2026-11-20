package org.example.game.ui;

import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.example.game.jugador.TipoRecurso;
import org.example.game.progresion.MejoraSecundaria;
import org.example.game.progresion.RamaProgresion;
import org.example.game.simulacion.ConfiguracionEconomia;
import org.example.game.simulacion.SimulacionSolar;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Lienzo estilo neurona biológica-cósmica con soma central, 3 dendritas principales curvas
 * (PLANETAS, MASA, CANTIDAD_PLANETAS) y bifurcaciones sinápticas secundarias por nivel (SRP).
 */
public class LienzoArbolOrbital extends Pane {

    public static final PantallaArbolProgresion.NodoArbol[] NODOS_PLANETAS = new PantallaArbolProgresion.NodoArbol[] {
            new PantallaArbolProgresion.NodoArbol(1, "[LUN]", "Luna", "Satélite rocoso natural. Proporciona una extracción continua y segura de minerales sin perturbar biosferas.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_1),
            new PantallaArbolProgresion.NodoArbol(2, "[POR]", "Planeta Porro", "Mundo templado habitable con civilización, crecimiento demográfico, colonización y academias científicas.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_2),
            new PantallaArbolProgresion.NodoArbol(3, "[SAT]", "Satélite", "Sonda orbital defensiva y científica de masa fija (1.0 t). Intercepta meteoritos entrantes y genera ciencia y energía.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_3),
            new PantallaArbolProgresion.NodoArbol(4, "[ESC]", "Escudo", "Cúpula de defensa planetaria que absorbe impactos destructivos adicionales.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_4),
            new PantallaArbolProgresion.NodoArbol(5, "[LAV]", "Planeta Lava", "Mundo magmático extremo de altísima extracción mineral y datos térmicos intensivos.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_5)
    };

    public static final PantallaArbolProgresion.NodoArbol[] NODOS_MASA = new PantallaArbolProgresion.NodoArbol[] {
            new PantallaArbolProgresion.NodoArbol(1, "0.6x", "Masa x0.6", "Habilita planetas con densidad intermedia y órbitas más estables ante perturbaciones gravitacionales.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_1),
            new PantallaArbolProgresion.NodoArbol(2, "1.0x", "Masa x1.0", "Masa estándar completa (1.0x) equivalente a la Tierra y Sol base, incrementando la producción proporcional.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_2),
            new PantallaArbolProgresion.NodoArbol(3, "2.0x", "Masa x2.0", "Cuerpos de doble densidad con pozos de gravedad más profundos que retienen satélites con mayor fuerza.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_3),
            new PantallaArbolProgresion.NodoArbol(4, "5.0x", "Masa x5.0", "Colosos celestes masivos de alto rendimiento industrial y amplias zonas de influencia gravitatoria.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_4),
            new PantallaArbolProgresion.NodoArbol(5, "10.0x", "Masa x10.0", "Límite superior absoluto de masa para astros ultra-masivos capaces de deformar el espacio circundante.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_5)
    };

    public static final PantallaArbolProgresion.NodoArbol[] NODOS_CANTIDAD = new PantallaArbolProgresion.NodoArbol[] {
            new PantallaArbolProgresion.NodoArbol(1, "4 PLAN", "Capacidad (4)", "Amplía la infraestructura del sistema solar para albergar hasta 4 planetas en coexistencia orbital simultánea.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_1),
            new PantallaArbolProgresion.NodoArbol(2, "5 PLAN", "Capacidad (5)", "Permite coordinar trayectorias seguras para sostener hasta 5 planetas activos en el sistema solar.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_2),
            new PantallaArbolProgresion.NodoArbol(3, "6 PLAN", "Capacidad (6)", "Expansión orbital avanzada para alojar 6 planetas con órbitas concéntricas sin colapsos mutuos.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_3),
            new PantallaArbolProgresion.NodoArbol(4, "8 PLAN", "Capacidad (8)", "Soporte de navegación multi-planetaria para albergar hasta 8 planetas simultáneos en el mapa.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_4),
            new PantallaArbolProgresion.NodoArbol(5, "12 PLAN", "Gran Sistema (12)", "Capacidad máxima de civilización interestelar: permite hasta 12 mundos girando en armonía cósmica.", ConfiguracionEconomia.COSTO_CIENCIA_NIVEL_5)
    };

    private static final double[] Y_DENDRITA_PLANETAS = {92.0, 74.0, 96.0, 76.0, 90.0};
    private static final double[] Y_DENDRITA_MASA = {284.0, 304.0, 282.0, 302.0, 288.0};
    private static final double[] Y_DENDRITA_CANTIDAD = {488.0, 508.0, 486.0, 506.0, 492.0};
    private static final RenderizadorGeometriaArbol GEOMETRIA = new RenderizadorGeometriaArbol();

    private static class EstadoLienzoArbol {
        final SimulacionSolar simulacion;
        final BiConsumer<RamaProgresion, PantallaArbolProgresion.NodoArbol> onSeleccionarNodo;
        Consumer<MejoraSecundaria> onSeleccionarMejora;
        final Runnable onInvestigado;
        final ConstructorOrbeNodoArbol constructorOrbe;
        final Map<RamaProgresion, Button> botonesInvestigar = new EnumMap<>(RamaProgresion.class);
        final Map<RamaProgresion, Double> costosBotones = new EnumMap<>(RamaProgresion.class);
        final Map<MejoraSecundaria, Button> botonesSecundarias = new EnumMap<>(MejoraSecundaria.class);
        int ultimoHashNiveles = -1;

        EstadoLienzoArbol(SimulacionSolar sim, BiConsumer<RamaProgresion, PantallaArbolProgresion.NodoArbol> onSel,
                          Consumer<MejoraSecundaria> onSelSec, Runnable onInv, ConstructorOrbeNodoArbol orbe) {
            this.simulacion = sim;
            this.onSeleccionarNodo = onSel;
            this.onSeleccionarMejora = onSelSec;
            this.onInvestigado = onInv;
            this.constructorOrbe = orbe != null ? orbe : new ConstructorOrbeNodoArbol();
        }

        void limpiarBotones() {
            botonesInvestigar.clear();
            costosBotones.clear();
            botonesSecundarias.clear();
        }

        int calcularHashNiveles() {
            var prog = simulacion != null ? simulacion.getProgresion() : null;
            if (prog == null) return -1;
            return prog.getNivel(RamaProgresion.PLANETAS) * 10000
                    + prog.getNivel(RamaProgresion.MASA) * 1000
                    + prog.getNivel(RamaProgresion.CANTIDAD_PLANETAS) * 100
                    + prog.getNivelEscudo() * 10
                    + prog.getMejorasSecundariasInvestigadas().size();
        }
    }

    private final EstadoLienzoArbol estado;

    public LienzoArbolOrbital(SimulacionSolar simulacion,
                              BiConsumer<RamaProgresion, PantallaArbolProgresion.NodoArbol> onSeleccionarNodo,
                              Runnable onInvestigado) {
        this(simulacion, onSeleccionarNodo, null, onInvestigado, new ConstructorOrbeNodoArbol());
    }

    public LienzoArbolOrbital(SimulacionSolar simulacion,
                              BiConsumer<RamaProgresion, PantallaArbolProgresion.NodoArbol> onSeleccionarNodo,
                              Runnable onInvestigado,
                              ConstructorOrbeNodoArbol constructorOrbe) {
        this(simulacion, onSeleccionarNodo, null, onInvestigado, constructorOrbe);
    }

    public LienzoArbolOrbital(SimulacionSolar simulacion,
                              BiConsumer<RamaProgresion, PantallaArbolProgresion.NodoArbol> onSeleccionarNodo,
                              Consumer<MejoraSecundaria> onSeleccionarMejora,
                              Runnable onInvestigado,
                              ConstructorOrbeNodoArbol constructorOrbe) {
        this.estado = new EstadoLienzoArbol(simulacion, onSeleccionarNodo, onSeleccionarMejora, onInvestigado, constructorOrbe);
        setStyle("-fx-background-color: transparent;");
        setPrefSize(940, 615);
        setMinSize(940, 615);
    }

    public void setOnSeleccionarMejora(Consumer<MejoraSecundaria> onSeleccionarMejora) {
        estado.onSeleccionarMejora = onSeleccionarMejora;
    }

    public void refrescar() {
        estado.limpiarBotones();
        getChildren().clear();

        var inv = estado.simulacion.getInventario();
        var prog = estado.simulacion.getProgresion();
        double ciencia = inv != null ? inv.getRecurso(TipoRecurso.CIENCIA) : 0.0;

        getChildren().addAll(GEOMETRIA.crearAnillosSinapticosFondo());
        renderizarDendritaRama(RamaProgresion.PLANETAS, NODOS_PLANETAS, Y_DENDRITA_PLANETAS, prog.getNivel(RamaProgresion.PLANETAS), ciencia, "#5be3ff");
        renderizarDendritaRama(RamaProgresion.MASA, NODOS_MASA, Y_DENDRITA_MASA, prog.getNivel(RamaProgresion.MASA), ciencia, "#d2a8ff");
        renderizarDendritaRama(RamaProgresion.CANTIDAD_PLANETAS, NODOS_CANTIDAD, Y_DENDRITA_CANTIDAD, prog.getNivel(RamaProgresion.CANTIDAD_PLANETAS), ciencia, "#ffd700");
        getChildren().add(GEOMETRIA.crearSomaNeuronal());

        estado.ultimoHashNiveles = estado.calcularHashNiveles();
    }

    private void renderizarDendritaRama(RamaProgresion rama, PantallaArbolProgresion.NodoArbol[] nodos,
                                        double[] yCoords, int nivelActual, double ciencia, String colorRamaHex) {
        double prevCenterX = RenderizadorGeometriaArbol.SOMA_X;
        double prevCenterY = RenderizadorGeometriaArbol.SOMA_Y;

        for (int i = 0; i < nodos.length; i++) {
            PantallaArbolProgresion.NodoArbol nodo = nodos[i];
            double x = RenderizadorGeometriaArbol.START_X + i * RenderizadorGeometriaArbol.STEP_X;
            double y = yCoords[i];
            double centerX = x + 43.0;
            double centerY = y + 25.0;

            boolean activa = (i == 0) || (nodo.nivel() <= nivelActual);
            getChildren().add(GEOMETRIA.crearDendritaCurva(prevCenterX, prevCenterY, centerX, centerY, i % 2 == 0 ? -24.0 : 24.0, activa));
            renderizarBifurcacionesSecundarias(rama, nodo.nivel(), centerX, centerY, nivelActual, ciencia, colorRamaHex);

            VBox orbePrincipal = estado.constructorOrbe.construir(
                    rama, nodo, nivelActual, ciencia, estado.simulacion,
                    estado.onSeleccionarNodo, estado.onInvestigado, estado.botonesInvestigar, estado.costosBotones
            );
            orbePrincipal.setLayoutX(x);
            orbePrincipal.setLayoutY(y);
            getChildren().add(orbePrincipal);

            prevCenterX = centerX;
            prevCenterY = centerY;
        }
    }

    private void renderizarBifurcacionesSecundarias(RamaProgresion rama, int tier, double mainCenterX, double mainCenterY,
                                                    int nivelActual, double ciencia, String colorRamaHex) {
        List<MejoraSecundaria> secundarias = MejoraSecundaria.deRamaYTier(rama, tier);
        var prog = estado.simulacion.getProgresion();

        for (int idx = 0; idx < secundarias.size(); idx++) {
            MejoraSecundaria mejora = secundarias.get(idx);
            double[] offset = GEOMETRIA.calcularOffsetBifurcacion(rama, tier, idx, secundarias.size());
            double secCenterX = mainCenterX + offset[0];
            double secCenterY = mainCenterY + offset[1];

            boolean investigada = prog.estaInvestigada(mejora);
            getChildren().add(GEOMETRIA.crearEspinaBifurcacion(mainCenterX, mainCenterY, secCenterX, secCenterY, investigada, nivelActual >= tier, colorRamaHex));

            VBox orbeSec = estado.constructorOrbe.construirSecundaria(
                    mejora, nivelActual, investigada, ciencia,
                    estado.simulacion, estado.onSeleccionarMejora, estado.onInvestigado, estado.botonesSecundarias
            );
            orbeSec.setLayoutX(secCenterX - 37.0);
            orbeSec.setLayoutY(secCenterY - 17.0);
            getChildren().add(orbeSec);
        }
    }

    public void actualizarEstadosBotones(double ciencia) {
        if (estado.calcularHashNiveles() != estado.ultimoHashNiveles) {
            refrescar();
            return;
        }
        for (var entry : estado.botonesInvestigar.entrySet()) {
            Button btn = entry.getValue();
            Double costo = estado.costosBotones.get(entry.getKey());
            if (btn != null && costo != null) {
                aplicarEstiloBotonDisponible(btn, ciencia >= costo, true);
            }
        }
        for (var entry : estado.botonesSecundarias.entrySet()) {
            Button btn = entry.getValue();
            MejoraSecundaria mejora = entry.getKey();
            if (btn != null && mejora != null) {
                aplicarEstiloBotonDisponible(btn, ciencia >= mejora.costo(), false);
            }
        }
    }

    private void aplicarEstiloBotonDisponible(Button btn, boolean puedePagar, boolean principal) {
        if (btn.isDisable() == puedePagar) {
            btn.setDisable(!puedePagar);
            String pad = principal ? "2 5 2 5" : "1 3 1 3";
            btn.setStyle(puedePagar
                    ? "-fx-background-color: #1e3a47; -fx-text-fill: #5be3ff; -fx-border-color: #5be3ff; -fx-border-width: 1px; -fx-cursor: hand; -fx-padding: " + pad + "; -fx-background-radius: 3px; -fx-border-radius: 3px;"
                    : "-fx-background-color: #1a1c26; -fx-text-fill: #777777; -fx-border-color: #333333; -fx-border-width: 1px; -fx-padding: " + pad + "; -fx-background-radius: 3px; -fx-border-radius: 3px;");
        }
    }
}
