package org.example.game.render;
import javafx.scene.canvas.GraphicsContext; import javafx.scene.image.Image; import javafx.scene.paint.Color; import org.example.game.cuerpo.CuerpoCeleste; import org.example.game.cuerpo.Defensor; import org.example.game.cuerpo.TipoCuerpo; import org.example.game.motor.ConstantesFisicas; import org.example.game.motor.GestorAgujeroNegro; import org.example.game.motor.Vector2D; import org.example.game.render.capa.*; import org.example.game.render.cuerpo.*; import java.util.*;
/**
 * Renderizador Pixel Art para el juego "Órbita" estilo RimWorld UI.
 * SRP: Orquesta las capas de fondo, zonas, atracción, cuerpos, trayectoria, selección y ghost.
 * OCP: Renderizado extensible de cuerpos mediante RenderizadorCuerpo y RepositorioSprites.
 */
public class RenderizadorPixelArt {

    private double anchoCanvas;
    private double altoCanvas;

    private boolean mostrarGrilla = false;
    private boolean mostrarEstelas = true;
    private boolean mostrarZonasHabitables = true;

    private final CamaraZoom camaraZoom = new CamaraZoom();
    private final RenderizadorFondo renderizadorFondo;
    private final GestorEstelas gestorEstelas;
    private final RepositorioSprites repositorioSprites;

    // Capas visuales (SRP)
    private final CapaZonasHabitables capaZonasHabitables;
    private final CapaAreaAtraccion capaAreaAtraccion;
    private final CapaSeleccion capaSeleccion;
    private final CapaTrayectorias capaTrayectorias;
    private final CapaGhostPreview capaGhostPreview;

    private final Map<TipoCuerpo, RenderizadorCuerpo> renderizadoresCuerpo = new EnumMap<>(TipoCuerpo.class);
    private java.util.function.Supplier<List<org.example.game.colonizacion.NaveColonizadora>> proveedorNaves;
    private org.example.game.efectos.GestorEfectos gestorEfectos;

    private double tiempoAnimacionSegundos = 0.0;

    public RenderizadorPixelArt(double ancho, double alto,
                                RenderizadorFondo renderizadorFondo,
                                GestorEstelas gestorEstelas,
                                RepositorioSprites repositorioSprites,
                                CapaZonasHabitables capaZonasHabitables,
                                CapaAreaAtraccion capaAreaAtraccion,
                                CapaSeleccion capaSeleccion,
                                CapaTrayectorias capaTrayectorias,
                                CapaGhostPreview capaGhostPreview) {
        this.anchoCanvas = ancho;
        this.altoCanvas = alto;
        this.renderizadorFondo = renderizadorFondo != null ? renderizadorFondo : new RenderizadorFondo(ancho, alto);
        this.gestorEstelas = gestorEstelas != null ? gestorEstelas : new GestorEstelas();
        this.repositorioSprites = repositorioSprites != null ? repositorioSprites : GestorImagenes.getInstancia();
        this.capaZonasHabitables = capaZonasHabitables != null ? capaZonasHabitables : new CapaZonasHabitables();
        this.capaAreaAtraccion = capaAreaAtraccion != null ? capaAreaAtraccion : new CapaAreaAtraccion();
        this.capaSeleccion = capaSeleccion != null ? capaSeleccion : new CapaSeleccion();
        this.capaTrayectorias = capaTrayectorias != null ? capaTrayectorias : new CapaTrayectorias();
        this.capaGhostPreview = capaGhostPreview != null ? capaGhostPreview : new CapaGhostPreview(this.repositorioSprites);

        inicializarRenderizadoresCuerpo();
    }

    public RenderizadorPixelArt(double ancho, double alto, RenderizadorFondo renderizadorFondo, GestorEstelas gestorEstelas) {
        this(ancho, alto, renderizadorFondo, gestorEstelas, null, null, null, null, null, null);
    }

    public RenderizadorPixelArt(double ancho, double alto) {
        this(ancho, alto, null, null);
    }

    private void inicializarRenderizadoresCuerpo() {
        RenderizadorEstrella estrellaRenderer = new RenderizadorEstrella(repositorioSprites);
        RenderizadorPlaneta planetaRenderer = new RenderizadorPlaneta(repositorioSprites);
        RenderizadorLuna lunaRenderer = new RenderizadorLuna(repositorioSprites);
        RenderizadorSatelite sateliteRenderer = new RenderizadorSatelite();
        RenderizadorMeteorito meteoritoRenderer = new RenderizadorMeteorito();
        RenderizadorAgujeroNegro agujeroRenderer = new RenderizadorAgujeroNegro();

        registrarRenderizadorCuerpo(TipoCuerpo.ESTRELLA, estrellaRenderer);
        registrarRenderizadorCuerpo(TipoCuerpo.PLANETA_ROCOSO, planetaRenderer);
        registrarRenderizadorCuerpo(TipoCuerpo.PLANETA_AGUA, planetaRenderer);
        registrarRenderizadorCuerpo(TipoCuerpo.PLANETA_LAVA, planetaRenderer);
        registrarRenderizadorCuerpo(TipoCuerpo.PLANETA_GASEOSO, planetaRenderer);
        registrarRenderizadorCuerpo(TipoCuerpo.PLANETA_HELADO, planetaRenderer);
        registrarRenderizadorCuerpo(TipoCuerpo.LUNA, lunaRenderer);
        registrarRenderizadorCuerpo(TipoCuerpo.SATELITE, sateliteRenderer);
        registrarRenderizadorCuerpo(TipoCuerpo.METEORITO, meteoritoRenderer);
        registrarRenderizadorCuerpo(TipoCuerpo.AGUJERO_NEGRO, agujeroRenderer);
        registrarRenderizadorCuerpo(TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO, agujeroRenderer);
    }

    public void registrarRenderizadorCuerpo(TipoCuerpo tipo, RenderizadorCuerpo renderizador) {
        if (tipo != null && renderizador != null) {
            renderizadoresCuerpo.put(tipo, renderizador);
        }
    }

    public void setProveedorNaves(java.util.function.Supplier<List<org.example.game.colonizacion.NaveColonizadora>> proveedorNaves) {
        this.proveedorNaves = proveedorNaves;
    }

    public void setGestorEfectos(org.example.game.efectos.GestorEfectos gestorEfectos) {
        this.gestorEfectos = gestorEfectos;
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

        renderizadorFondo.dibujarFondo(gc, anchoCanvas, altoCanvas, tick);

        if (repositorioSprites.obtenerImagenFondo() == null) {
            renderizadorFondo.dibujarEstrellasFondo(gc, tick);
        }

        double zoom = camaraZoom.getNivelZoom();
        boolean vistaTransformada = !camaraZoom.esVistaNeutra();
        if (vistaTransformada) {
            gc.save();
            gc.translate(
                    camaraZoom.calcularTraslacionX(anchoCanvas),
                    camaraZoom.calcularTraslacionY(altoCanvas)
            );
            gc.scale(zoom, zoom);
        }

        if (mostrarGrilla) {
            renderizadorFondo.dibujarGrilla(gc, anchoCanvas, altoCanvas, camaraZoom);
        }

        if (mostrarZonasHabitables) {
            capaZonasHabitables.dibujar(gc, cuerpos, anchoCanvas, altoCanvas);
        }

        capaAreaAtraccion.dibujar(gc, cuerpos, anchoCanvas, altoCanvas);

        if (mostrarEstelas) {
            gestorEstelas.dibujar(gc, anchoCanvas, altoCanvas);
        }

        for (CuerpoCeleste c : cuerpos) {
            dibujarCuerpoPixelArt(gc, c, cuerpos, tick);
        }

        dibujarNavesColonizadoras(gc);

        if (gestorEfectos != null) {
            gestorEfectos.dibujar(gc, anchoCanvas, altoCanvas);
        }

        if (cuerpoSeleccionado != null && cuerpos.contains(cuerpoSeleccionado)) {
            capaSeleccion.dibujar(gc, cuerpoSeleccionado, anchoCanvas, altoCanvas);
        }

        if (trayectoriaPreview != null && !trayectoriaPreview.isEmpty()) {
            capaTrayectorias.dibujar(gc, trayectoriaPreview, anchoCanvas, altoCanvas);
        }

        if (previewTipo != null && previewPosFisica != null) {
            capaGhostPreview.dibujar(gc, previewPosFisica, previewTipo, previewFactorMasa, previewNombre, tick,
                    tiempoAnimacionSegundos, anchoCanvas, altoCanvas);
        }

        if (vistaTransformada) {
            gc.restore();
        }
    }

    private void dibujarCuerpoPixelArt(GraphicsContext gc, CuerpoCeleste c, List<CuerpoCeleste> cuerpos, long tick) {
        double x = Math.round(ConstantesFisicas.fisicaAXJavaFX(c.getPosicionX(), anchoCanvas));
        double y = Math.round(ConstantesFisicas.fisicaAYJavaFX(c.getPosicionY(), altoCanvas));
        TipoCuerpo tipo = c.getTipoCuerpo();
        double r = c.getRadio();

        double angulo = 0.0;
        if (tipo != TipoCuerpo.ESTRELLA) {
            double velAngular = 57.2958 / Math.max(6.0, r);
            double faseInicial = (c.getNombre() != null) ? Math.abs(c.getNombre().hashCode() % 360) : 0;
            angulo = (faseInicial + tiempoAnimacionSegundos * velAngular) % 360.0;
        }

        double escalaBH = GestorAgujeroNegro.calcularFactorEscala(c, cuerpos);
        double rotacionBH = GestorAgujeroNegro.calcularRotacionExtra(c, cuerpos, tick);

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
            UtilidadRender.dibujarDiscoConContorno(gc, 0, 0, r, UtilidadRender.colorWeb(c.getColorHex()));
        }

        gc.restore();

        if (c.tieneEscudo()) {
            dibujarCupulaEscudo(gc, x, y, r, tick);
        }

        gc.setFill(UtilidadRender.colorWeb("#e8e4d8"));
        gc.setGlobalAlpha(0.85);
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.fillText(c.getNombre(), x, y + c.getRadio() + 14);
        gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
        gc.setGlobalAlpha(1.0);
    }

    private void dibujarCupulaEscudo(GraphicsContext gc, double cx, double cy, double r, long tick) {
        double rEfectivo = r * 1.5;
        double pulso = 0.85 + 0.15 * Math.sin(tick * 0.2);

        gc.setFill(UtilidadRender.colorRgb(91, 227, 255, 0.20 * pulso));
        gc.fillOval(cx - rEfectivo, cy - rEfectivo, rEfectivo * 2, rEfectivo * 2);

        gc.setStroke(UtilidadRender.colorWeb("#5be3ff"));
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
            double sx = ConstantesFisicas.fisicaAXJavaFX(pos.x(), anchoCanvas);
            double sy = ConstantesFisicas.fisicaAYJavaFX(pos.y(), altoCanvas);

            gc.save();
            gc.setFill(UtilidadRender.colorWeb("#50fa7b"));
            gc.fillOval(sx - 4, sy - 4, 8, 8);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1.0);
            gc.strokeOval(sx - 5, sy - 5, 10, 10);
            gc.restore();
        }
    }

    public void actualizarEstelas(List<CuerpoCeleste> cuerpos) {
        gestorEstelas.actualizar(cuerpos);
    }

    public void limpiarEstelas() {
        gestorEstelas.limpiar();
    }

    public void toggleGrilla() { mostrarGrilla = !mostrarGrilla; }
    public void toggleEstelas() { mostrarEstelas = !mostrarEstelas; }
    public void toggleZonasHabitables() { mostrarZonasHabitables = !mostrarZonasHabitables; }

    public CamaraZoom getCamaraZoom() { return camaraZoom; }
    public double getAnchoCanvas() { return anchoCanvas; }
    public double getAltoCanvas() { return altoCanvas; }

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
        this.tiempoAnimacionSegundos += deltaTime * multiplicadorVelocidad;
        avanzarFondo(deltaTime, multiplicadorVelocidad);
    }

    public void avanzarFondo(double deltaTime, double multiplicadorVelocidad) {
        if (deltaTime <= 0 || multiplicadorVelocidad <= 0) return;
        double desplazamientoPx = renderizadorFondo.getVelocidadBaseFondo() * multiplicadorVelocidad * deltaTime;
        renderizadorFondo.setDesplazamientoFondoX(renderizadorFondo.getDesplazamientoFondoX() + desplazamientoPx);
    }

    public Image getImagenFondo() {
        return repositorioSprites.obtenerImagenFondo();
    }

    public double getDesplazamientoFondoX() {
        return renderizadorFondo.getDesplazamientoFondoX();
    }

    public double getVelocidadBaseFondo() {
        return renderizadorFondo.getVelocidadBaseFondo();
    }

    public double getTiempoAnimacionSegundos() {
        return tiempoAnimacionSegundos;
    }

    public static Color colorWeb(String hex) {
        return UtilidadRender.colorWeb(hex);
    }

    public static Color colorRgb(int r, int g, int b) {
        return UtilidadRender.colorRgb(r, g, b);
    }

    public static Color colorRgb(int r, int g, int b, double opacity) {
        return UtilidadRender.colorRgb(r, g, b, opacity);
    }
}
