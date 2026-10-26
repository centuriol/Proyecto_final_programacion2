package org.example.game.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.StrokeLineCap;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.ConstantesFisicas;
import org.example.game.motor.Vector2D;

import java.util.*;

/**
 * Renderizador Pixel Art para el juego "Órbita" estilo RimWorld UI.
 * SRP: Dibuja el canvas espacial, estrellas, partículas, anillos orbitales,
 * sprites celestes procedurales de pixel art, halos y trayectorias.
 */
public class RenderizadorPixelArt {

    private double anchoCanvas;
    private double altoCanvas;

    // Configuración visual
    private boolean mostrarGrilla = false; // Desactivada por defecto ("elimina la cuadrilla, solo deja el fondo")
    private boolean mostrarEstelas = true;
    private boolean mostrarZonasHabitables = true;
    private boolean mostrarOrbitsGuia = true;

    // Componente de camara para zoom (Tarea 8)
    private final CamaraZoom camaraZoom = new CamaraZoom();

    // Componentes delegados (SRP / DIP)
    private final RenderizadorFondo renderizadorFondo;
    private final GestorEstelas gestorEstelas;
    private final Map<TipoCuerpo, RenderizadorCuerpo> renderizadoresCuerpo = new EnumMap<>(TipoCuerpo.class);
    private java.util.function.Supplier<List<org.example.game.colonizacion.NaveColonizadora>> proveedorNaves;
    private org.example.game.efectos.GestorEfectos gestorEfectos;

    /**
     * Radios base de referencia orbital alrededor de estrellas (en px a escala 1.0).
     * Compartidos entre dibujarZonasHabitables() y dibujarGhostPreview() para
     * evitar desincronizacion entre ambos metodos (Tarea 7).
     */
    private static final double[] RADIOS_REFERENCIA_ORBITAL = { 160.0, 280.0, 400.0 };

    // Cache y constantes de colores para optimizar rendimiento de renderizado por frame (Tarea 4)
    private static final Map<String, Color> CACHE_COLORES = new HashMap<>();
    private static final Map<Integer, Color> CACHE_COLORES_RGB = new HashMap<>();

    public static Color colorWeb(String hex) {
        if (hex == null || hex.isBlank()) return Color.WHITE;
        return CACHE_COLORES.computeIfAbsent(hex, Color::web);
    }

    public static Color colorRgb(int r, int g, int b) {
        int key = ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
        return CACHE_COLORES_RGB.computeIfAbsent(key, k -> Color.rgb(r, g, b));
    }

    public static Color colorRgb(int r, int g, int b, double opacity) {
        int op = (int) Math.round(opacity * 255.0);
        int key = ((op & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
        return CACHE_COLORES_RGB.computeIfAbsent(key, k -> Color.rgb(r, g, b, opacity));
    }

    private static final Color COLOR_FONDO_ESPACIAL = Color.web("#0e1017");
    private static final Color COLOR_GRILLA_LINEA = Color.web("#2b2d3a");
    private static final Color COLOR_GRILLA_EJE = Color.web("#5be3ff");
    private static final Color COLOR_TEXTO_ETIQUETA = Color.web("#e8e4d8");
    private static final Color COLOR_ACENTO_CYAN = Color.web("#5be3ff");
    private static final Color COLOR_VERDE_EXITO = Color.web("#50fa7b");
    private static final Color COLOR_NARANJA_LANZAMIENTO = Color.web("#ffb86c");
    private static final Color COLOR_AREA_ESTRELLA = Color.rgb(255, 215, 0, 0.16);
    private static final Color COLOR_AREA_LUNA = Color.rgb(200, 210, 225, 0.15);
    private static final Color COLOR_AREA_PLANETA = Color.rgb(91, 227, 255, 0.14);
    private static final Color COLOR_SOMBRA_PLANETA = Color.rgb(10, 20, 40, 0.45);
    private static final Color COLOR_NEBULOSA_1 = Color.rgb(40, 20, 60, 0.22);
    private static final Color COLOR_NEBULOSA_2 = Color.rgb(15, 45, 65, 0.18);
    private static final Color COLOR_ANILLO_AGUA = Color.rgb(0, 191, 255, 0.6);
    private static final Color COLOR_ANILLO_LAVA = Color.rgb(255, 69, 0, 0.6);

    // Tiempo acumulado de animación para rotación sobre su propio eje de los ítems
    private double tiempoAnimacionSegundos = 0.0;

    public RenderizadorPixelArt(double ancho, double alto, RenderizadorFondo renderizadorFondo, GestorEstelas gestorEstelas) {
        this.anchoCanvas = ancho;
        this.altoCanvas = alto;
        this.renderizadorFondo = renderizadorFondo != null ? renderizadorFondo : new RenderizadorFondo(ancho, alto);
        this.gestorEstelas = gestorEstelas != null ? gestorEstelas : new GestorEstelas();
        inicializarRenderizadoresCuerpo();
    }

    public RenderizadorPixelArt(double ancho, double alto) {
        this(ancho, alto, null, null);
    }

    private void inicializarRenderizadoresCuerpo() {
        registrarRenderizadorCuerpo(TipoCuerpo.ESTRELLA, (gc, c, tipo, r, tick) -> dibujarEstrellaPixel(gc, 0, 0, r, tick));
        registrarRenderizadorCuerpo(TipoCuerpo.PLANETA_ROCOSO, (gc, c, tipo, r, tick) -> dibujarPlanetaRocosoPixel(gc, 0, 0, c, tick));
        registrarRenderizadorCuerpo(TipoCuerpo.PLANETA_AGUA, (gc, c, tipo, r, tick) -> dibujarPlanetaAguaPixel(gc, 0, 0, c, tick));
        registrarRenderizadorCuerpo(TipoCuerpo.PLANETA_LAVA, (gc, c, tipo, r, tick) -> dibujarPlanetaLavaPixel(gc, 0, 0, c, tick));
        registrarRenderizadorCuerpo(TipoCuerpo.PLANETA_GASEOSO, (gc, c, tipo, r, tick) -> dibujarGiganteGaseosoPixel(gc, 0, 0, r, tick));
        registrarRenderizadorCuerpo(TipoCuerpo.PLANETA_HELADO, (gc, c, tipo, r, tick) -> dibujarPlanetaHeladoPixel(gc, 0, 0, r));
        registrarRenderizadorCuerpo(TipoCuerpo.LUNA, (gc, c, tipo, r, tick) -> dibujarLunaPixel(gc, 0, 0, r));
        registrarRenderizadorCuerpo(TipoCuerpo.SATELITE, (gc, c, tipo, r, tick) -> dibujarSatelitePixel(gc, 0, 0, tick));
        registrarRenderizadorCuerpo(TipoCuerpo.METEORITO, (gc, c, tipo, r, tick) -> dibujarMeteoritoPixel(gc, 0, 0, c, tick));
        registrarRenderizadorCuerpo(TipoCuerpo.AGUJERO_NEGRO, (gc, c, tipo, r, tick) -> dibujarAgujeroNegroPixel(gc, 0, 0, r, tick));
        registrarRenderizadorCuerpo(TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO, (gc, c, tipo, r, tick) -> dibujarAgujeroNegroPixel(gc, 0, 0, r, tick));
    }

    public void setProveedorNaves(java.util.function.Supplier<List<org.example.game.colonizacion.NaveColonizadora>> proveedorNaves) {
        this.proveedorNaves = proveedorNaves;
    }

    public void setGestorEfectos(org.example.game.efectos.GestorEfectos gestorEfectos) {
        this.gestorEfectos = gestorEfectos;
    }

    public void registrarRenderizadorCuerpo(TipoCuerpo tipo, RenderizadorCuerpo renderizador) {
        if (tipo != null && renderizador != null) {
            this.renderizadoresCuerpo.put(tipo, renderizador);
        }
    }

    public void renderizarTodo(GraphicsContext gc, List<CuerpoCeleste> cuerpos, long tick,
                              Vector2D previewPosFisica, TipoCuerpo previewTipo, double previewFactorMasa,
                              Vector2D dragInicioFisica, List<Vector2D> trayectoriaPreview,
                              CuerpoCeleste cuerpoSeleccionado) {
        renderizarTodo(gc, cuerpos, tick, previewPosFisica, previewTipo, previewFactorMasa, null, dragInicioFisica, trayectoriaPreview, cuerpoSeleccionado);
    }

    public void renderizarTodo(GraphicsContext gc, List<CuerpoCeleste> cuerpos, long tick,
                              Vector2D previewPosFisica, TipoCuerpo previewTipo, double previewFactorMasa,
                              String previewNombre,
                              Vector2D dragInicioFisica, List<Vector2D> trayectoriaPreview,
                              CuerpoCeleste cuerpoSeleccionado) {

        // Aplicar zoom como transformacion de escala centrada en el canvas (Tarea 8)
        double zoom = camaraZoom.getNivelZoom();
        if (!camaraZoom.esZoomNeutro()) {
            gc.save();
            double cx = anchoCanvas / 2.0;
            double cy = altoCanvas / 2.0;
            gc.translate(cx * (1.0 - zoom), cy * (1.0 - zoom));
            gc.scale(zoom, zoom);
        }

        // 1. Fondo espacial en rotación continua hacia la derecha
        dibujarFondo(gc, tick);

        // 2. Grilla Pixel Art (32px) - Desactivada por defecto ("elimina la cuadrilla, solo deja el fondo")
        if (mostrarGrilla) {
            dibujarGrilla(gc);
        }

        // 3. Estrellas de fondo parpadeantes (solo si no hay imagen de fondo disponible)
        if (getImagenFondo() == null) {
            dibujarEstrellasFondo(gc, tick);
        }

        // 4. Zonas Habitables (Goldilocks) alrededor de estrellas
        if (mostrarZonasHabitables) {
            dibujarZonasHabitables(gc, cuerpos, tick);
        }

        // 4.1 Áreas de Atracción y Colapso Gravitacional (Líneas sutiles apenas visibles)
        dibujarAreasAtraccion(gc, cuerpos);

        // 5. Anillos de órbita y estelas de movimiento
        actualizarEstelas(cuerpos);
        if (mostrarEstelas) {
            dibujarEstelas(gc);
        }

        // 6. Cuerpos Celestes en Pixel Art
        for (CuerpoCeleste c : cuerpos) {
            dibujarCuerpoPixelArt(gc, c, cuerpos, tick);
        }

        // 6.5 Naves Colonizadoras en tránsito
        dibujarNavesColonizadoras(gc);

        // 6.6 Efectos visuales de explosiones y partículas
        if (gestorEfectos != null) {
            gestorEfectos.dibujar(gc, anchoCanvas, altoCanvas);
        }

        // 7. Cuerpo seleccionado (Retícula de inspección)
        if (cuerpoSeleccionado != null && cuerpos.contains(cuerpoSeleccionado)) {
            dibujarSeleccion(gc, cuerpoSeleccionado, tick);
        }

        // 8. Trayectoria de predicción durante colocación / lanzamiento
        if (trayectoriaPreview != null && !trayectoriaPreview.isEmpty()) {
            dibujarTrayectoriaPreview(gc, trayectoriaPreview);
        }

        // 9. Vector de lanzamiento (Slingshot Drag)
        if (dragInicioFisica != null && previewPosFisica != null) {
            dibujarVectorLanzamiento(gc, dragInicioFisica, previewPosFisica);
        }

        // 10. Ghost Preview de colocación
        if (previewTipo != null && previewPosFisica != null) {
            dibujarGhostPreview(gc, previewPosFisica, previewTipo, previewFactorMasa, previewNombre, tick);
        }

        // Restaurar transform de zoom (Tarea 8)
        if (!camaraZoom.esZoomNeutro()) {
            gc.restore();
        }
    }

    private void dibujarFondo(GraphicsContext gc, long tick) {
        renderizadorFondo.dibujarFondo(gc, anchoCanvas, altoCanvas, tick);
    }

    private void dibujarGrilla(GraphicsContext gc) {
        renderizadorFondo.dibujarGrilla(gc, anchoCanvas, altoCanvas);
    }

    private void dibujarEstrellasFondo(GraphicsContext gc, long tick) {
        renderizadorFondo.dibujarEstrellasFondo(gc, tick);
    }

    private void dibujarZonasHabitables(GraphicsContext gc, List<CuerpoCeleste> cuerpos, long tick) {
        for (CuerpoCeleste c : cuerpos) {
            if (c.getTipoCuerpo() == TipoCuerpo.ESTRELLA) {
                double sx = ConstantesFisicas.fisicaAXJavaFX(c.getPosicionX(), anchoCanvas);
                double sy = ConstantesFisicas.fisicaAYJavaFX(c.getPosicionY(), altoCanvas);

                double factorMasa = 1.0;
                if (c.getTipoCuerpo() != null && c.getTipoCuerpo().masaBase > 0) {
                    factorMasa = Math.max(0.1, c.getMasa() / c.getTipoCuerpo().masaBase);
                }
                double escala = Math.sqrt(factorMasa);

                // Líneas de referencia orbitales casi transparentes para planetas (3 órbitas de referencia)
                double[] radiosReferencia = RADIOS_REFERENCIA_ORBITAL;

                gc.setLineWidth(1.0);
                gc.setLineDashes(3, 5);

                for (double rBase : radiosReferencia) {
                    double rOrbita = rBase * escala;
                    // Línea casi transparente sutil, consistente con las áreas de atracción ("que apenas se vean")
                    gc.setStroke(COLOR_AREA_PLANETA);
                    gc.strokeOval(sx - rOrbita, sy - rOrbita, rOrbita * 2.0, rOrbita * 2.0);
                }

                gc.setLineDashes(null);
            }
        }
    }

    private void dibujarAreasAtraccion(GraphicsContext gc, List<CuerpoCeleste> cuerpos) {
        for (CuerpoCeleste c : cuerpos) {
            if (!c.esSimulado()) continue;
            if (c.getTipoCuerpo() == TipoCuerpo.SATELITE || c.getTipoCuerpo() == TipoCuerpo.METEORITO) continue;

            double rAtraccion = c.getRadioAtraccion();
            if (rAtraccion <= 0) continue;

            double sx = ConstantesFisicas.fisicaAXJavaFX(c.getPosicionX(), anchoCanvas);
            double sy = ConstantesFisicas.fisicaAYJavaFX(c.getPosicionY(), altoCanvas);

            // Color sutil según el tipo de cuerpo con muy baja opacidad ("que apenas se vean")
            Color colorArea;
            if (c.getTipoCuerpo() == TipoCuerpo.ESTRELLA) {
                colorArea = COLOR_AREA_ESTRELLA;
            } else if (c.getTipoCuerpo() == TipoCuerpo.LUNA) {
                colorArea = COLOR_AREA_LUNA;
            } else {
                colorArea = COLOR_AREA_PLANETA;
            }

            gc.setStroke(colorArea);
            gc.setLineWidth(1.0);
            gc.setLineDashes(3, 5);
            gc.strokeOval(sx - rAtraccion, sy - rAtraccion, rAtraccion * 2.0, rAtraccion * 2.0);
            gc.setLineDashes(null);
        }
    }

    private void actualizarEstelas(List<CuerpoCeleste> cuerpos) {
        gestorEstelas.actualizar(cuerpos);
    }

    private void dibujarEstelas(GraphicsContext gc) {
        gestorEstelas.dibujar(gc, anchoCanvas, altoCanvas);
    }

    // ===== Renderizado Pixel Art de Cada Cuerpo Celeste =====

    private void dibujarCuerpoPixelArt(GraphicsContext gc, CuerpoCeleste c, List<CuerpoCeleste> cuerpos, long tick) {
        double x = Math.round(ConstantesFisicas.fisicaAXJavaFX(c.getPosicionX(), anchoCanvas));
        double y = Math.round(ConstantesFisicas.fisicaAYJavaFX(c.getPosicionY(), altoCanvas));
        TipoCuerpo tipo = c.getTipoCuerpo();
        double r = c.getRadio();

        // Rotación axial sobre su propio eje: muy despacio, ~1 pixel por segundo en el perímetro (el Sol permanece fijo)
        double angulo = 0.0;
        if (tipo != TipoCuerpo.ESTRELLA) {
            double velAngular = 57.2958 / Math.max(6.0, r); // grados por segundo para 1 px/s tangencial
            double faseInicial = (c.getNombre() != null) ? Math.abs(c.getNombre().hashCode() % 360) : 0;
            angulo = (faseInicial + tiempoAnimacionSegundos * velAngular) % 360.0;
        }

        // Efecto visual de absorción por agujero negro (escala decreciente y rotación acelerada)
        double escalaBH = org.example.game.motor.GestorAgujeroNegro.calcularFactorEscala(c, cuerpos);
        double rotacionBH = org.example.game.motor.GestorAgujeroNegro.calcularRotacionExtra(c, cuerpos, tick);

        gc.save();
        gc.translate(x, y);
        if (escalaBH < 1.0) {
            gc.scale(escalaBH, escalaBH);
        }
        if (angulo != 0.0 || rotacionBH != 0.0) {
            gc.rotate(angulo + rotacionBH);
        }

        RenderizadorCuerpo renderer = renderizadoresCuerpo.get(tipo);
        if (renderer != null) {
            renderer.dibujar(gc, c, tipo, r, tick);
        } else {
            dibujarCuerpoGenerico(gc, 0, 0, c);
        }

        gc.restore();

        // Cúpula de escudo planetario
        if (c.tieneEscudo()) {
            dibujarCupulaEscudo(gc, x, y, r, tick);
        }

        // Etiqueta compacta con nombre debajo, centrada con el cuerpo
        gc.setFill(COLOR_TEXTO_ETIQUETA);
        gc.setGlobalAlpha(0.85);
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.fillText(c.getNombre(), x, y + c.getRadio() + 14);
        gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
        gc.setGlobalAlpha(1.0);
    }

    private void dibujarEstrellaPixel(GraphicsContext gc, double cx, double cy, double r, long tick) {
        javafx.scene.image.Image imgSol = GestorImagenes.getImagen(TipoCuerpo.ESTRELLA);
        if (imgSol != null) {
            // Solo la imagen del sol, sin halo ni luz externa
            double tam = Math.max(32.0, r * 2.2);
            gc.drawImage(imgSol, Math.round(cx - tam / 2.0), Math.round(cy - tam / 2.0), tam, tam);
            return;
        }

        // Resplandor corona pulsante
        double pulso = 1.0 + 0.12 * Math.sin(tick * 0.15);
        double rCorona = r * 1.5 * pulso;

        gc.setFill(colorRgb(255, 215, 0, 0.18));
        gc.fillOval(cx - rCorona, cy - rCorona, rCorona * 2, rCorona * 2);

        // Rayos de destello pixel art (4 direcciones + diagonales)
        gc.setStroke(colorRgb(255, 230, 100, 0.7));
        gc.setLineWidth(2.0);
        double rayoLen = r * 1.8 * pulso;
        gc.strokeLine(cx - rayoLen, cy, cx + rayoLen, cy);
        gc.strokeLine(cx, cy - rayoLen, cx, cy + rayoLen);

        // Núcleo solar (borde negro 2px + tonos dorados)
        dibujarDiscoConContorno(gc, cx, cy, r, colorWeb("#ffaa00"));

        gc.setFill(colorWeb("#ffd700"));
        gc.fillOval(cx - r * 0.75, cy - r * 0.75, r * 1.5, r * 1.5);

        gc.setFill(colorWeb("#ffffff"));
        gc.fillOval(cx - r * 0.35, cy - r * 0.45, r * 0.7, r * 0.7);
    }

    private boolean intentarDibujarSpritePlaneta(GraphicsContext gc, double cx, double cy, CuerpoCeleste c, TipoCuerpo tipo) {
        Image imgPlaneta = GestorImagenes.getImagen(tipo);
        if (imgPlaneta == null) return false;
        double r = c.getRadio();
        double tam = Math.max(22.0, r * 2.0);
        gc.drawImage(imgPlaneta, Math.round(cx - tam / 2.0), Math.round(cy - tam / 2.0), tam, tam);
        if (c.tieneCivilizacion()) {
            gc.setFill(colorWeb("#5be3ff"));
            gc.fillRect(cx - r * 0.3, cy - r * 0.2, 2, 2);
            gc.fillRect(cx + r * 0.2, cy + r * 0.1, 2, 2);
        }
        return true;
    }

    private void dibujarDiscoConContorno(GraphicsContext gc, double cx, double cy, double r, Color colorInterior) {
        gc.setFill(Color.BLACK);
        gc.fillOval(cx - r - 2, cy - r - 2, (r + 2) * 2, (r + 2) * 2);
        gc.setFill(colorInterior);
        gc.fillOval(cx - r, cy - r, r * 2, r * 2);
    }

    private void dibujarPlanetaRocosoPixel(GraphicsContext gc, double cx, double cy, CuerpoCeleste c, long tick) {
        if (intentarDibujarSpritePlaneta(gc, cx, cy, c, TipoCuerpo.PLANETA_ROCOSO)) return;
        double r = c.getRadio();

        // Fallback procedural planeta rocoso
        dibujarDiscoConContorno(gc, cx, cy, r, colorWeb("#2b6cb0"));

        // Continentes en pixel art
        gc.setFill(colorWeb("#48bb78"));
        gc.fillRect(cx - r * 0.5, cy - r * 0.3, r * 0.7, r * 0.5);
        gc.fillRect(cx + r * 0.1, cy - r * 0.6, r * 0.5, r * 0.4);
        gc.fillRect(cx - r * 0.2, cy + r * 0.2, r * 0.6, r * 0.4);

        // Sombra lateral (dithering)
        gc.setFill(COLOR_SOMBRA_PLANETA);
        gc.fillArc(cx - r, cy - r, r * 2, r * 2, 90, 180, javafx.scene.shape.ArcType.ROUND);

        // Si tiene civilización: luces de ciudades en el lado oscuro
        if (c.tieneCivilizacion()) {
            gc.setFill(colorWeb("#5be3ff"));
            gc.fillRect(cx - r * 0.4, cy - r * 0.1, 2, 2);
            gc.fillRect(cx - r * 0.2, cy + r * 0.3, 2, 2);
            gc.fillRect(cx + r * 0.2, cy + r * 0.1, 3, 2);
        }
    }

    private void dibujarPlanetaAguaPixel(GraphicsContext gc, double cx, double cy, CuerpoCeleste c, long tick) {
        if (intentarDibujarSpritePlaneta(gc, cx, cy, c, TipoCuerpo.PLANETA_AGUA)) return;
        double r = c.getRadio();

        dibujarDiscoConContorno(gc, cx, cy, r, colorWeb("#00bfff"));
        gc.setFill(colorWeb("#1e90ff"));
        gc.fillRect(cx - r * 0.4, cy - r * 0.2, r * 0.8, r * 0.4);
    }

    private void dibujarPlanetaLavaPixel(GraphicsContext gc, double cx, double cy, CuerpoCeleste c, long tick) {
        if (intentarDibujarSpritePlaneta(gc, cx, cy, c, TipoCuerpo.PLANETA_LAVA)) return;
        double r = c.getRadio();

        dibujarDiscoConContorno(gc, cx, cy, r, colorWeb("#ff4500"));
        gc.setFill(colorWeb("#ffd700"));
        gc.fillRect(cx - r * 0.3, cy - r * 0.2, r * 0.6, r * 0.4);
    }

    private void dibujarGiganteGaseosoPixel(GraphicsContext gc, double cx, double cy, double r, long tick) {
        // Borde negro exterior y bandas gaseosas base
        dibujarDiscoConContorno(gc, cx, cy, r, colorWeb("#d69e2e"));

        // Franjas horizontales de nubes
        gc.setFill(colorWeb("#dd6b20"));
        gc.fillRect(cx - r * 0.85, cy - r * 0.5, r * 1.7, r * 0.3);
        gc.fillRect(cx - r * 0.95, cy + r * 0.1, r * 1.9, r * 0.35);

        gc.setFill(colorWeb("#fbd38d"));
        gc.fillRect(cx - r * 0.9, cy - r * 0.15, r * 1.8, r * 0.2);

        // Gran Mancha Roja
        gc.setFill(colorWeb("#9b2c2c"));
        gc.fillOval(cx + r * 0.2, cy + r * 0.15, r * 0.45, r * 0.3);

        // Anillos planetarios elípticos inclinados
        gc.setStroke(colorRgb(237, 137, 54, 0.85));
        gc.setLineWidth(3.0);
        gc.strokeOval(cx - r * 1.7, cy - r * 0.45, r * 3.4, r * 0.9);

        gc.setStroke(colorRgb(254, 235, 200, 0.9));
        gc.setLineWidth(1.5);
        gc.strokeOval(cx - r * 1.5, cy - r * 0.38, r * 3.0, r * 0.76);
    }

    private void dibujarLunaPixel(GraphicsContext gc, double cx, double cy, double r) {
        javafx.scene.image.Image imgLuna = GestorImagenes.getImagen(TipoCuerpo.LUNA);
        if (imgLuna != null) {
            double tam = Math.max(14.0, r * 2.0);
            gc.drawImage(imgLuna, Math.round(cx - tam / 2.0), Math.round(cy - tam / 2.0), tam, tam);
            return;
        }

        dibujarDiscoConContorno(gc, cx, cy, r, colorWeb("#a0aec0"));

        // Cráteres oscuros
        gc.setFill(colorWeb("#4a5568"));
        gc.fillOval(cx - r * 0.4, cy - r * 0.3, r * 0.4, r * 0.4);
        gc.fillOval(cx + r * 0.1, cy + r * 0.2, r * 0.5, r * 0.5);
        gc.fillOval(cx - r * 0.2, cy + r * 0.3, r * 0.3, r * 0.3);
    }

    private void dibujarSatelitePixel(GraphicsContext gc, double cx, double cy, long tick) {
        // Cuerpo metálico central
        gc.setFill(Color.BLACK);
        gc.fillRect(cx - 5, cy - 5, 10, 10);
        gc.setFill(colorWeb("#e2e8f0"));
        gc.fillRect(cx - 4, cy - 4, 8, 8);

        // Paneles solares laterales
        gc.setFill(colorWeb("#2b6cb0"));
        gc.fillRect(cx - 14, cy - 3, 8, 6);
        gc.fillRect(cx + 6, cy - 3, 8, 6);

        // Borde dorado de paneles
        gc.setStroke(colorWeb("#ffd700"));
        gc.setLineWidth(1.0);
        gc.strokeRect(cx - 14, cy - 3, 8, 6);
        gc.strokeRect(cx + 6, cy - 3, 8, 6);

        // Baliza intermitente cyan
        if ((tick / 8) % 2 == 0) {
            gc.setFill(COLOR_ACENTO_CYAN);
            gc.fillRect(cx - 1, cy - 7, 2, 2);
        }
    }

    private void dibujarCupulaEscudo(GraphicsContext gc, double cx, double cy, double r, long tick) {
        double rEfectivo = r * 1.5;
        double pulso = 0.85 + 0.15 * Math.sin(tick * 0.2);

        // Cúpula translúcida
        gc.setFill(colorRgb(91, 227, 255, 0.20 * pulso));
        gc.fillOval(cx - rEfectivo, cy - rEfectivo, rEfectivo * 2, rEfectivo * 2);

        // Borde brillante hexagonal/circular segmentado
        gc.setStroke(COLOR_ACENTO_CYAN);
        gc.setLineWidth(1.8);
        gc.setLineDashes(6, 4);
        gc.strokeOval(cx - rEfectivo, cy - rEfectivo, rEfectivo * 2, rEfectivo * 2);
        gc.setLineDashes(null);
    }

    private void dibujarNavesColonizadoras(GraphicsContext gc) {
        if (proveedorNaves == null) return;
        List<org.example.game.colonizacion.NaveColonizadora> naves = proveedorNaves.get();
        if (naves == null || naves.isEmpty()) return;

        for (org.example.game.colonizacion.NaveColonizadora nave : naves) {
            if (nave == null || nave.isCompletada() || nave.isPerdida()) continue;
            Vector2D pos = nave.getPosicion();
            double sx = ConstantesFisicas.fisicaAXJavaFX(pos.x, anchoCanvas);
            double sy = ConstantesFisicas.fisicaAYJavaFX(pos.y, altoCanvas);

            // Dibujar nave ligera en tránsito (verde brillante)
            gc.save();
            gc.setFill(Color.web("#50fa7b"));
            gc.fillOval(sx - 4, sy - 4, 8, 8);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1.0);
            gc.strokeOval(sx - 5, sy - 5, 10, 10);
            gc.restore();
        }
    }

    private void dibujarMeteoritoPixel(GraphicsContext gc, double cx, double cy, CuerpoCeleste m, long tick) {
        Vector2D vel = m.getVelocidad();
        Vector2D dirOpuesta = vel.normalizar().multiplicar(-1.0);

        // Cola de fuego de partículas
        double lenCola = Math.min(40.0, vel.magnitud() * 0.04);
        double fx = cx + dirOpuesta.x * lenCola;
        double fy = cy + dirOpuesta.y * lenCola;

        gc.setStroke(colorRgb(255, 100, 20, 0.8));
        gc.setLineWidth(4.0);
        gc.strokeLine(cx, cy, fx, fy);

        gc.setStroke(colorRgb(255, 220, 50, 0.9));
        gc.setLineWidth(2.0);
        gc.strokeLine(cx, cy, cx + dirOpuesta.x * lenCola * 0.6, cy + dirOpuesta.y * lenCola * 0.6);

        // Roca del meteorito
        gc.setFill(Color.BLACK);
        gc.fillOval(cx - 5, cy - 5, 10, 10);
        gc.setFill(colorWeb("#718096"));
        gc.fillOval(cx - 4, cy - 4, 8, 8);
    }

    private void dibujarAgujeroNegroPixel(GraphicsContext gc, double cx, double cy, double r, long tick) {
        // Disco de acreción arremolinado en violeta y cyan
        double rDisco = r * 2.2;
        double anguloGiro = tick * 2.5;

        gc.setStroke(colorRgb(189, 147, 249, 0.7)); // Violeta
        gc.setLineWidth(3.0);
        gc.strokeArc(cx - rDisco, cy - rDisco * 0.6, rDisco * 2, rDisco * 1.2, anguloGiro, 120, javafx.scene.shape.ArcType.OPEN);

        gc.setStroke(colorRgb(91, 227, 255, 0.85)); // Cyan
        gc.setLineWidth(2.0);
        gc.strokeArc(cx - rDisco * 0.8, cy - rDisco * 0.5, rDisco * 1.6, rDisco * 1.0, anguloGiro + 180, 140, javafx.scene.shape.ArcType.OPEN);

        // Horizonte de sucesos (negro absoluto con borde fino púrpura)
        gc.setFill(Color.BLACK);
        gc.fillOval(cx - r, cy - r, r * 2, r * 2);

        gc.setStroke(colorWeb("#bd93f9"));
        gc.setLineWidth(1.5);
        gc.strokeOval(cx - r, cy - r, r * 2, r * 2);
    }

    private void dibujarPlanetaHeladoPixel(GraphicsContext gc, double cx, double cy, double r) {
        dibujarDiscoConContorno(gc, cx, cy, r, colorWeb("#90cdf4"));

        // Grietas glaciares
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.0);
        gc.strokeLine(cx - r * 0.4, cy - r * 0.5, cx, cy);
        gc.strokeLine(cx, cy, cx + r * 0.5, cy + r * 0.3);
    }

    private void dibujarCuerpoGenerico(GraphicsContext gc, double cx, double cy, CuerpoCeleste c) {
        dibujarDiscoConContorno(gc, cx, cy, c.getRadio(), colorWeb(c.getColorHex()));
    }

    // ===== Overlays de Interacción (Selección, Slingshot, Ghost Preview) =====

    private void dibujarSeleccion(GraphicsContext gc, CuerpoCeleste c, long tick) {
        double cx = ConstantesFisicas.fisicaAXJavaFX(c.getPosicionX(), anchoCanvas);
        double cy = ConstantesFisicas.fisicaAYJavaFX(c.getPosicionY(), altoCanvas);
        double r = c.getRadio() + 8.0;

        // Retícula de 4 esquinas cyan estilo RimWorld
        gc.setStroke(COLOR_ACENTO_CYAN);
        gc.setLineWidth(2.0);

        double len = 6.0;
        // Top-left
        gc.strokeLine(cx - r, cy - r + len, cx - r, cy - r);
        gc.strokeLine(cx - r, cy - r, cx - r + len, cy - r);
        // Top-right
        gc.strokeLine(cx + r - len, cy - r, cx + r, cy - r);
        gc.strokeLine(cx + r, cy - r, cx + r, cy - r + len);
        // Bottom-left
        gc.strokeLine(cx - r, cy + r - len, cx - r, cy + r);
        gc.strokeLine(cx - r, cy + r, cx - r + len, cy + r);
        // Bottom-right
        gc.strokeLine(cx + r - len, cy + r, cx + r, cy + r);
        gc.strokeLine(cx + r, cy + r, cx + r, cy + r - len);
    }

    private void dibujarTrayectoriaPreview(GraphicsContext gc, List<Vector2D> trayectoria) {
        gc.setLineWidth(2.0);
        gc.setLineCap(StrokeLineCap.ROUND);

        Vector2D prev = null;
        int i = 0;
        int total = trayectoria.size();

        for (Vector2D ptFisica : trayectoria) {
            double x = ConstantesFisicas.fisicaAXJavaFX(ptFisica.x, anchoCanvas);
            double y = ConstantesFisicas.fisicaAYJavaFX(ptFisica.y, altoCanvas);

            if (prev != null) {
                double x0 = ConstantesFisicas.fisicaAXJavaFX(prev.x, anchoCanvas);
                double y0 = ConstantesFisicas.fisicaAYJavaFX(prev.y, altoCanvas);

                double alpha = 1.0 - ((double) i / total) * 0.75;
                gc.setStroke(colorRgb(91, 227, 255, alpha));
                if (i % 2 == 0) {
                    gc.strokeLine(x0, y0, x, y);
                }
            }
            prev = ptFisica;
            i++;
        }
    }

    private void dibujarVectorLanzamiento(GraphicsContext gc, Vector2D inicioFisica, Vector2D actualFisica) {
        double x0 = ConstantesFisicas.fisicaAXJavaFX(inicioFisica.x, anchoCanvas);
        double y0 = ConstantesFisicas.fisicaAYJavaFX(inicioFisica.y, altoCanvas);
        double x1 = ConstantesFisicas.fisicaAXJavaFX(actualFisica.x, anchoCanvas);
        double y1 = ConstantesFisicas.fisicaAYJavaFX(actualFisica.y, altoCanvas);

        // Vector slingshot invertido (arrastre hacia atrás impulsa hacia adelante)
        double vx = x0 - (x1 - x0);
        double vy = y0 - (y1 - y0);

        // Línea elástica
        gc.setStroke(COLOR_NARANJA_LANZAMIENTO);
        gc.setLineWidth(2.5);
        gc.strokeLine(x0, y0, x1, y1);

        // Flecha de impulso resultante
        gc.setStroke(COLOR_ACENTO_CYAN);
        gc.setLineWidth(3.0);
        gc.strokeLine(x0, y0, vx, vy);
        gc.fillOval(vx - 4, vy - 4, 8, 8);
    }

    private void dibujarGhostPreview(GraphicsContext gc, Vector2D posFisica, TipoCuerpo tipo, double factorMasa, long tick) {
        dibujarGhostPreview(gc, posFisica, tipo, factorMasa, null, tick);
    }

    private void dibujarGhostPreview(GraphicsContext gc, Vector2D posFisica, TipoCuerpo tipo, double factorMasa, String previewNombre, long tick) {
        double x = ConstantesFisicas.fisicaAXJavaFX(posFisica.x, anchoCanvas);
        double y = ConstantesFisicas.fisicaAYJavaFX(posFisica.y, altoCanvas);
        double r = Math.max(8.0, Math.min(35.0, 15.0 * factorMasa));

        double pulso = 1.0 + 0.15 * Math.sin(tick * 0.25);
        double rPulsante = r * pulso;

        gc.setStroke(colorWeb(tipo.getColorHexString()));
        gc.setLineWidth(2.0);
        gc.setLineDashes(4, 4);
        gc.strokeOval(x - rPulsante, y - rPulsante, rPulsante * 2, rPulsante * 2);
        gc.setLineDashes(null);

        // Si el ítem tiene imagen de sprite, dibujarlo como ghost translúcido (el Sol no gira)
        javafx.scene.image.Image imgGhost = GestorImagenes.getImagen(tipo);
        if (imgGhost != null) {
            gc.setGlobalAlpha(0.65);
            double tamGhost = (tipo == TipoCuerpo.ESTRELLA) ? Math.max(32.0, r * 2.2) : Math.max(16.0, r * 2.0);
            double angulo = 0.0;
            if (tipo != TipoCuerpo.ESTRELLA) {
                double velAngular = 57.2958 / Math.max(6.0, r);
                angulo = (tiempoAnimacionSegundos * velAngular) % 360.0;
            }

            gc.save();
            gc.translate(x, y);
            if (angulo != 0.0) {
                gc.rotate(angulo);
            }
            gc.drawImage(imgGhost, Math.round(-tamGhost / 2.0), Math.round(-tamGhost / 2.0), tamGhost, tamGhost);
            gc.restore();
            gc.setGlobalAlpha(1.0);
        }

        // Radio de atracción / colapso del cuerpo a colocar (guía de distancia segura)
        double rAtraccion;
        if (tipo == TipoCuerpo.ESTRELLA) {
            rAtraccion = Math.max(65.0, 85.0 * Math.sqrt(factorMasa));
        } else if (tipo == TipoCuerpo.PLANETA_ROCOSO || tipo == TipoCuerpo.PLANETA_GASEOSO
                || tipo == TipoCuerpo.PLANETA_HELADO || tipo == TipoCuerpo.PLANETA_AGUA
                || tipo == TipoCuerpo.PLANETA_LAVA) {
            rAtraccion = Math.max(35.0, 55.0 * Math.sqrt(factorMasa));
        } else if (tipo == TipoCuerpo.LUNA) {
            rAtraccion = Math.max(25.0, 40.0 * Math.sqrt(factorMasa));
        } else if (tipo == TipoCuerpo.AGUJERO_NEGRO || tipo == TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO) {
            rAtraccion = Math.max(90.0, 120.0 * Math.sqrt(factorMasa));
        } else {
            rAtraccion = Math.max(20.0, 35.0 * Math.sqrt(factorMasa));
        }

        // Círculo sutil ("que apenas se vea") de área de colapso para preview
        gc.setStroke(colorRgb(255, 255, 255, 0.20));
        gc.setLineWidth(1.0);
        gc.setLineDashes(3, 5);
        gc.strokeOval(x - rAtraccion, y - rAtraccion, rAtraccion * 2.0, rAtraccion * 2.0);

        // Si se está colocando una Estrella, mostrar también las líneas de referencia orbitales casi transparentes
        if (tipo == TipoCuerpo.ESTRELLA) {
            double escala = Math.sqrt(Math.max(0.1, factorMasa));
            double[] radiosReferencia = RADIOS_REFERENCIA_ORBITAL;
            gc.setStroke(colorRgb(91, 227, 255, 0.12));
            for (double rBase : radiosReferencia) {
                double rOrbita = rBase * escala;
                gc.strokeOval(x - rOrbita, y - rOrbita, rOrbita * 2.0, rOrbita * 2.0);
            }
        }

        gc.setLineDashes(null);

        // Tag informativo con nombre personalizado si existe
        String textoNombre = (previewNombre != null && !previewNombre.isBlank()) ? previewNombre : tipo.nombre;
        String linea1 = textoNombre + " (" + String.format("%.1fx", factorMasa) + ")";
        double tagWidth = Math.max(130.0, linea1.length() * 8.0 + 20.0);

        gc.setFill(colorRgb(26, 28, 38, 0.9));
        gc.fillRect(x + r + 8, y - 20, tagWidth, 42);
        gc.setStroke(COLOR_ACENTO_CYAN);
        gc.setLineWidth(1.0);
        gc.strokeRect(x + r + 8, y - 20, tagWidth, 42);

        gc.setFill(COLOR_TEXTO_ETIQUETA);
        gc.fillText(linea1, x + r + 14, y - 4);
        gc.setFill(COLOR_VERDE_EXITO);
        gc.fillText("Click para colocar", x + r + 14, y + 14);
    }

    // ===== Setters de Toggles =====

    public void toggleGrilla() { mostrarGrilla = !mostrarGrilla; }
    public void toggleEstelas() { mostrarEstelas = !mostrarEstelas; }
    public void toggleZonasHabitables() { mostrarZonasHabitables = !mostrarZonasHabitables; }

    public CamaraZoom getCamaraZoom() { return camaraZoom; }

    public void setDimensiones(double w, double h) {
        if (w > 0 && h > 0 && (w != this.anchoCanvas || h != this.altoCanvas)) {
            this.anchoCanvas = w;
            this.altoCanvas = h;
            renderizadorFondo.generarEstrellasFondo(w, h);
            if (w > 0) {
                renderizadorFondo.setDesplazamientoFondoX(renderizadorFondo.getDesplazamientoFondoX() % w);
            }
        }
    }

    public void avanzarAnimaciones(double deltaTime, double multiplicadorVelocidad) {
        if (deltaTime <= 0 || multiplicadorVelocidad <= 0) return;

        // 1. Avanzar tiempo de rotación sobre su propio eje de los ítems
        tiempoAnimacionSegundos += deltaTime * multiplicadorVelocidad;

        // 2. Avanzar desplazamiento continuo del fondo hacia la derecha
        renderizadorFondo.avanzarDesplazamiento(deltaTime, multiplicadorVelocidad, anchoCanvas);
    }

    public void avanzarFondo(double deltaTime, double multiplicadorVelocidad) {
        avanzarAnimaciones(deltaTime, multiplicadorVelocidad);
    }

    public double getTiempoAnimacionSegundos() {
        return tiempoAnimacionSegundos;
    }

    public void setTiempoAnimacionSegundos(double t) {
        this.tiempoAnimacionSegundos = t;
    }

    public double getDesplazamientoFondoX() {
        return renderizadorFondo.getDesplazamientoFondoX();
    }

    public void setDesplazamientoFondoX(double desplazamiento) {
        renderizadorFondo.setDesplazamientoFondoX(desplazamiento);
    }

    public double getVelocidadBaseFondo() {
        return renderizadorFondo.getVelocidadBaseFondo();
    }

    public void setVelocidadBaseFondo(double vel) {
        renderizadorFondo.setVelocidadBaseFondo(vel);
    }

    public double getAnchoCanvas() {
        return anchoCanvas;
    }

    public double getAltoCanvas() {
        return altoCanvas;
    }

    public Image getImagenFondo() {
        return renderizadorFondo.getImagenFondo();
    }

    public void setImagenFondo(Image imagenFondo) {
        renderizadorFondo.setImagenFondo(imagenFondo);
    }
}
