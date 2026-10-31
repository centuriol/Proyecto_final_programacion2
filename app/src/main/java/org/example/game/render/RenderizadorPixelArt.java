package org.example.game.render;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Defensor;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.ConversorCoordenadas;
import org.example.game.motor.GestorAgujeroNegro;
import org.example.game.motor.TransformadorCoordenadas;
import org.example.game.motor.Vector2D;
import org.example.game.render.capa.*;
import org.example.game.render.cuerpo.*;
import java.util.*;
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
    private final ConversorCoordenadas conversor;
    private final RepositorioSprites repositorioSprites;

    // Capas visuales (SRP)
    private final CapaZonasHabitables capaZonasHabitables;
    private final CapaAreaAtraccion capaAreaAtraccion;
    private final CapaSeleccion capaSeleccion;
    private final CapaTrayectorias capaTrayectorias;
    private final CapaGhostPreview capaGhostPreview;
    private final CapaNavesColonizadoras capaNaves = new CapaNavesColonizadoras();
    private final CapaEscudoPlanetario capaEscudo = new CapaEscudoPlanetario();
    private final org.example.game.cuerpo.ServicioEscudo servicioEscudo = org.example.game.cuerpo.ServicioEscudo.getInstanciaPorDefecto();

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
        this.conversor = new TransformadorCoordenadas();
        this.repositorioSprites = repositorioSprites != null ? repositorioSprites : new GestorImagenes();
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

        capaNaves.dibujar(gc, proveedorNaves, conversor, anchoCanvas, altoCanvas);

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
        double x = Math.round(conversor.fisicaAXJavaFX(c.getPosicion().x(), anchoCanvas));
        double y = Math.round(conversor.fisicaAYJavaFX(c.getPosicion().y(), altoCanvas));
        TipoCuerpo tipo = c.getTipoCuerpo();
        double r = c.getRadio();

        double angulo = 0.0;
        if (c.getMasa() < 1e29) {
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

        if (servicioEscudo.tieneEscudo(c)) {
            capaEscudo.dibujarCupula(gc, x, y, r, tick);
        }

        gc.setFill(UtilidadRender.colorWeb("#e8e4d8"));
        gc.setGlobalAlpha(0.85);
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.fillText(c.getNombre(), x, y + c.getRadio() + 14);
        gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
        gc.setGlobalAlpha(1.0);
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
            renderizadorFondo.normalizarDesplazamiento(w);
        }
    }

    public void avanzarAnimaciones(double deltaTime, double multiplicadorVelocidad) {
        if (deltaTime <= 0 || multiplicadorVelocidad <= 0) return;
        this.tiempoAnimacionSegundos += deltaTime * multiplicadorVelocidad;
        avanzarFondo(deltaTime, multiplicadorVelocidad);
    }

    public void avanzarFondo(double deltaTime, double multiplicadorVelocidad) {
        if (deltaTime <= 0 || multiplicadorVelocidad <= 0) return;
        renderizadorFondo.avanzarDesplazamiento(deltaTime, multiplicadorVelocidad, anchoCanvas);
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
}
