package org.example.game.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.StrokeLineCap;
import org.example.CuerpoCeleste;
import org.example.Estrella;
import org.example.Planeta;
import org.example.AgujeroNegro;
import org.example.game.cuerpo.EscudoProtector;
import org.example.game.cuerpo.Luna;
import org.example.game.cuerpo.Meteorito;
import org.example.game.cuerpo.Satelite;
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
    private boolean mostrarGrilla = true;
    private boolean mostrarEstelas = true;
    private boolean mostrarZonasHabitables = true;
    private boolean mostrarOrbitsGuia = true;

    // Historial de estelas por cuerpo (últimas posiciones)
    private final Map<CuerpoCeleste, Deque<Vector2D>> estelas = new HashMap<>();
    private static final int MAX_PUNTOS_ESTELA = 45;

    // Estrellas estáticas de fondo con brillo aleatorio
    private final List<EstrellaFondo> estrellasFondo = new ArrayList<>();

    private static class EstrellaFondo {
        final double x, y, size;
        final double brilloBase;
        final double velocidadParpadeo;

        EstrellaFondo(double x, double y, double size, double brilloBase, double vel) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.brilloBase = brilloBase;
            this.velocidadParpadeo = vel;
        }
    }

    public RenderizadorPixelArt(double ancho, double alto) {
        this.anchoCanvas = ancho;
        this.altoCanvas = alto;
        generarEstrellasFondo();
    }

    private void generarEstrellasFondo() {
        Random rand = new Random(42); // Semilla fija para consistencia visual
        for (int i = 0; i < 180; i++) {
            double x = rand.nextDouble() * anchoCanvas;
            double y = rand.nextDouble() * altoCanvas;
            double size = rand.nextDouble() < 0.8 ? 1.5 : (rand.nextDouble() < 0.9 ? 2.5 : 3.5);
            double brillo = 0.2 + rand.nextDouble() * 0.6;
            double vel = 0.5 + rand.nextDouble() * 2.0;
            estrellasFondo.add(new EstrellaFondo(x, y, size, brillo, vel));
        }
    }

    public void renderizarTodo(GraphicsContext gc, List<CuerpoCeleste> cuerpos, long tick,
                              Vector2D previewPosFisica, TipoCuerpo previewTipo, double previewFactorMasa,
                              Vector2D dragInicioFisica, List<Vector2D> trayectoriaPreview,
                              CuerpoCeleste cuerpoSeleccionado) {

        // 1. Fondo espacial profundo (#1a1c26 a #0c0d14)
        gc.setFill(Color.web("#0e1017"));
        gc.fillRect(0, 0, anchoCanvas, altoCanvas);

        // Nebulosas sutiles dithered
        dibujarNebulosas(gc, tick);

        // 2. Grilla Pixel Art (32px)
        if (mostrarGrilla) {
            dibujarGrilla(gc);
        }

        // 3. Estrellas de fondo parpadeantes
        dibujarEstrellasFondo(gc, tick);

        // 4. Zonas Habitables (Goldilocks) alrededor de estrellas
        if (mostrarZonasHabitables) {
            dibujarZonasHabitables(gc, cuerpos, tick);
        }

        // 5. Anillos de órbita y estelas de movimiento
        actualizarEstelas(cuerpos);
        if (mostrarEstelas) {
            dibujarEstelas(gc);
        }

        // 6. Cuerpos Celestes en Pixel Art
        for (CuerpoCeleste c : cuerpos) {
            dibujarCuerpoPixelArt(gc, c, tick);
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
            dibujarGhostPreview(gc, previewPosFisica, previewTipo, previewFactorMasa, tick);
        }
    }

    private void dibujarNebulosas(GraphicsContext gc, long tick) {
        // Nube violeta oscura en esquina
        gc.setFill(Color.rgb(40, 20, 60, 0.22));
        gc.fillOval(anchoCanvas * 0.1, altoCanvas * 0.15, 450, 300);

        // Nube cyan oscura en esquina opuesta
        gc.setFill(Color.rgb(15, 45, 65, 0.18));
        gc.fillOval(anchoCanvas * 0.65, altoCanvas * 0.55, 550, 380);
    }

    private void dibujarGrilla(GraphicsContext gc) {
        gc.setStroke(Color.web("#2b2d3a"));
        gc.setLineWidth(1.0);
        gc.setGlobalAlpha(0.25);

        int tamanoCelda = 32;
        for (double x = 0; x < anchoCanvas; x += tamanoCelda) {
            gc.strokeLine(x, 0, x, altoCanvas);
        }
        for (double y = 0; y < altoCanvas; y += tamanoCelda) {
            gc.strokeLine(0, y, anchoCanvas, y);
        }

        // Ejes centrales destacados sutilmente
        gc.setStroke(Color.web("#5be3ff"));
        gc.setGlobalAlpha(0.18);
        gc.strokeLine(anchoCanvas / 2.0, 0, anchoCanvas / 2.0, altoCanvas);
        gc.strokeLine(0, altoCanvas / 2.0, anchoCanvas, altoCanvas / 2.0);

        gc.setGlobalAlpha(1.0);
    }

    private void dibujarEstrellasFondo(GraphicsContext gc, long tick) {
        for (EstrellaFondo ef : estrellasFondo) {
            double alpha = ef.brilloBase + 0.25 * Math.sin(tick * 0.05 * ef.velocidadParpadeo);
            alpha = Math.max(0.1, Math.min(1.0, alpha));

            gc.setFill(Color.color(0.91, 0.89, 0.85, alpha)); // #e8e4d8
            gc.fillRect((int) ef.x, (int) ef.y, (int) ef.size, (int) ef.size);
        }
    }

    private void dibujarZonasHabitables(GraphicsContext gc, List<CuerpoCeleste> cuerpos, long tick) {
        for (CuerpoCeleste c : cuerpos) {
            if (c.getTipoCuerpo() == TipoCuerpo.ESTRELLA) {
                double sx = ConstantesFisicas.fisicaAXJavaFX(c.getPosicionX(), anchoCanvas);
                double sy = ConstantesFisicas.fisicaAYJavaFX(c.getPosicionY(), altoCanvas);

                // Zona habitable: 120px a 240px
                double rMin = 110.0;
                double rMax = 230.0;

                // Anillo de resplandor verde habitable
                gc.setStroke(Color.rgb(80, 250, 123, 0.12));
                gc.setLineWidth(rMax - rMin);
                double rMedio = (rMin + rMax) / 2.0;
                gc.strokeOval(sx - rMedio, sy - rMedio, rMedio * 2, rMedio * 2);

                // Bordes delimitadores
                gc.setStroke(Color.rgb(80, 250, 123, 0.35));
                gc.setLineWidth(1.0);
                gc.setLineDashes(6, 6);
                gc.strokeOval(sx - rMin, sy - rMin, rMin * 2, rMin * 2);
                gc.strokeOval(sx - rMax, sy - rMax, rMax * 2, rMax * 2);
                gc.setLineDashes(null);
            }
        }
    }

    private void actualizarEstelas(List<CuerpoCeleste> cuerpos) {
        // Limpiar cuerpos eliminados
        estelas.keySet().removeIf(c -> !cuerpos.contains(c));

        for (CuerpoCeleste c : cuerpos) {
            Deque<Vector2D> cola = estelas.computeIfAbsent(c, k -> new ArrayDeque<>());
            cola.addLast(new Vector2D(c.getPosicionX(), c.getPosicionY()));
            if (cola.size() > MAX_PUNTOS_ESTELA) {
                cola.removeFirst();
            }
        }
    }

    private void dibujarEstelas(GraphicsContext gc) {
        for (Map.Entry<CuerpoCeleste, Deque<Vector2D>> entry : estelas.entrySet()) {
            CuerpoCeleste c = entry.getKey();
            Deque<Vector2D> pts = entry.getValue();
            if (pts.size() < 2) continue;

            Color baseCol = Color.web(c.getColorHex());
            int i = 0;
            int total = pts.size();

            Vector2D prev = null;
            for (Vector2D pt : pts) {
                if (prev != null) {
                    double progress = (double) i / total;
                    double x1 = ConstantesFisicas.fisicaAXJavaFX(prev.x, anchoCanvas);
                    double y1 = ConstantesFisicas.fisicaAYJavaFX(prev.y, altoCanvas);
                    double x2 = ConstantesFisicas.fisicaAXJavaFX(pt.x, anchoCanvas);
                    double y2 = ConstantesFisicas.fisicaAYJavaFX(pt.y, altoCanvas);

                    gc.setStroke(Color.color(baseCol.getRed(), baseCol.getGreen(), baseCol.getBlue(), progress * 0.45));
                    gc.setLineWidth(Math.max(1.0, progress * 2.5));
                    gc.strokeLine(x1, y1, x2, y2);
                }
                prev = pt;
                i++;
            }
        }
    }

    // ===== Renderizado Pixel Art de Cada Cuerpo Celeste =====

    private void dibujarCuerpoPixelArt(GraphicsContext gc, CuerpoCeleste c, long tick) {
        double x = Math.round(ConstantesFisicas.fisicaAXJavaFX(c.getPosicionX(), anchoCanvas));
        double y = Math.round(ConstantesFisicas.fisicaAYJavaFX(c.getPosicionY(), altoCanvas));
        TipoCuerpo tipo = c.getTipoCuerpo();

        switch (tipo) {
            case ESTRELLA:
                dibujarEstrellaPixel(gc, x, y, c.getRadio(), tick);
                break;
            case PLANETA_ROCOSO:
                dibujarPlanetaRocosoPixel(gc, x, y, c, tick);
                break;
            case PLANETA_GASEOSO:
                dibujarGiganteGaseosoPixel(gc, x, y, c.getRadio(), tick);
                break;
            case LUNA:
                dibujarLunaPixel(gc, x, y, c.getRadio());
                break;
            case SATELITE:
                dibujarSatelitePixel(gc, x, y, tick);
                break;
            case ESCUDO_DOME:
                dibujarEscudoDomePixel(gc, x, y, (EscudoProtector) c, tick);
                break;
            case METEORITO:
                dibujarMeteoritoPixel(gc, x, y, (Meteorito) c, tick);
                break;
            case AGUJERO_NEGRO:
            case AGUJERO_NEGRO_SUPERMASIVO:
                dibujarAgujeroNegroPixel(gc, x, y, c.getRadio(), tick);
                break;
            case PLANETA_HELADO:
                dibujarPlanetaHeladoPixel(gc, x, y, c.getRadio());
                break;
            default:
                dibujarCuerpoGenerico(gc, x, y, c);
        }

        // Etiqueta compacta con nombre debajo
        gc.setFill(Color.web("#e8e4d8"));
        gc.setGlobalAlpha(0.85);
        gc.fillText(c.getNombre(), x - 20, y + c.getRadio() + 14);
        gc.setGlobalAlpha(1.0);
    }

    private void dibujarEstrellaPixel(GraphicsContext gc, double cx, double cy, double r, long tick) {
        // Resplandor corona pulsante
        double pulso = 1.0 + 0.12 * Math.sin(tick * 0.15);
        double rCorona = r * 1.5 * pulso;

        gc.setFill(Color.rgb(255, 215, 0, 0.18));
        gc.fillOval(cx - rCorona, cy - rCorona, rCorona * 2, rCorona * 2);

        // Rayos de destello pixel art (4 direcciones + diagonales)
        gc.setStroke(Color.rgb(255, 230, 100, 0.7));
        gc.setLineWidth(2.0);
        double rayoLen = r * 1.8 * pulso;
        gc.strokeLine(cx - rayoLen, cy, cx + rayoLen, cy);
        gc.strokeLine(cx, cy - rayoLen, cx, cy + rayoLen);

        // Núcleo solar (borde negro 2px + tonos dorados)
        gc.setFill(Color.BLACK);
        gc.fillOval(cx - r - 2, cy - r - 2, (r + 2) * 2, (r + 2) * 2);

        gc.setFill(Color.web("#ffaa00"));
        gc.fillOval(cx - r, cy - r, r * 2, r * 2);

        gc.setFill(Color.web("#ffd700"));
        gc.fillOval(cx - r * 0.75, cy - r * 0.75, r * 1.5, r * 1.5);

        gc.setFill(Color.web("#ffffff"));
        gc.fillOval(cx - r * 0.35, cy - r * 0.45, r * 0.7, r * 0.7);
    }

    private void dibujarPlanetaRocosoPixel(GraphicsContext gc, double cx, double cy, CuerpoCeleste c, long tick) {
        double r = c.getRadio();

        // Borde 2px negro estilo RimWorld
        gc.setFill(Color.BLACK);
        gc.fillOval(cx - r - 2, cy - r - 2, (r + 2) * 2, (r + 2) * 2);

        // Océano azul base
        gc.setFill(Color.web("#2b6cb0"));
        gc.fillOval(cx - r, cy - r, r * 2, r * 2);

        // Continentes en pixel art
        gc.setFill(Color.web("#48bb78"));
        gc.fillRect(cx - r * 0.5, cy - r * 0.3, r * 0.7, r * 0.5);
        gc.fillRect(cx + r * 0.1, cy - r * 0.6, r * 0.5, r * 0.4);
        gc.fillRect(cx - r * 0.2, cy + r * 0.2, r * 0.6, r * 0.4);

        // Sombra lateral (dithering)
        gc.setFill(Color.rgb(10, 20, 40, 0.45));
        gc.fillArc(cx - r, cy - r, r * 2, r * 2, 90, 180, javafx.scene.shape.ArcType.ROUND);

        // Si tiene civilización: luces de ciudades en el lado oscuro
        if (c instanceof Planeta && ((Planeta) c).tieneCivilizacion()) {
            gc.setFill(Color.web("#5be3ff"));
            gc.fillRect(cx - r * 0.4, cy - r * 0.1, 2, 2);
            gc.fillRect(cx - r * 0.2, cy + r * 0.3, 2, 2);
            gc.fillRect(cx + r * 0.2, cy + r * 0.1, 3, 2);
        }
    }

    private void dibujarGiganteGaseosoPixel(GraphicsContext gc, double cx, double cy, double r, long tick) {
        // Borde negro exterior
        gc.setFill(Color.BLACK);
        gc.fillOval(cx - r - 2, cy - r - 2, (r + 2) * 2, (r + 2) * 2);

        // Bandas gaseosas
        gc.setFill(Color.web("#d69e2e"));
        gc.fillOval(cx - r, cy - r, r * 2, r * 2);

        // Franjas horizontales de nubes
        gc.setFill(Color.web("#dd6b20"));
        gc.fillRect(cx - r * 0.85, cy - r * 0.5, r * 1.7, r * 0.3);
        gc.fillRect(cx - r * 0.95, cy + r * 0.1, r * 1.9, r * 0.35);

        gc.setFill(Color.web("#fbd38d"));
        gc.fillRect(cx - r * 0.9, cy - r * 0.15, r * 1.8, r * 0.2);

        // Gran Mancha Roja
        gc.setFill(Color.web("#9b2c2c"));
        gc.fillOval(cx + r * 0.2, cy + r * 0.15, r * 0.45, r * 0.3);

        // Anillos planetarios elípticos inclinados
        gc.setStroke(Color.rgb(237, 137, 54, 0.85));
        gc.setLineWidth(3.0);
        gc.strokeOval(cx - r * 1.7, cy - r * 0.45, r * 3.4, r * 0.9);

        gc.setStroke(Color.rgb(254, 235, 200, 0.9));
        gc.setLineWidth(1.5);
        gc.strokeOval(cx - r * 1.5, cy - r * 0.38, r * 3.0, r * 0.76);
    }

    private void dibujarLunaPixel(GraphicsContext gc, double cx, double cy, double r) {
        gc.setFill(Color.BLACK);
        gc.fillOval(cx - r - 2, cy - r - 2, (r + 2) * 2, (r + 2) * 2);

        gc.setFill(Color.web("#a0aec0"));
        gc.fillOval(cx - r, cy - r, r * 2, r * 2);

        // Cráteres oscuros
        gc.setFill(Color.web("#4a5568"));
        gc.fillOval(cx - r * 0.4, cy - r * 0.3, r * 0.4, r * 0.4);
        gc.fillOval(cx + r * 0.1, cy + r * 0.2, r * 0.5, r * 0.5);
        gc.fillOval(cx - r * 0.2, cy + r * 0.3, r * 0.3, r * 0.3);
    }

    private void dibujarSatelitePixel(GraphicsContext gc, double cx, double cy, long tick) {
        // Cuerpo metálico central
        gc.setFill(Color.BLACK);
        gc.fillRect(cx - 5, cy - 5, 10, 10);
        gc.setFill(Color.web("#e2e8f0"));
        gc.fillRect(cx - 4, cy - 4, 8, 8);

        // Paneles solares laterales
        gc.setFill(Color.web("#2b6cb0"));
        gc.fillRect(cx - 14, cy - 3, 8, 6);
        gc.fillRect(cx + 6, cy - 3, 8, 6);

        // Borde dorado de paneles
        gc.setStroke(Color.web("#ffd700"));
        gc.setLineWidth(1.0);
        gc.strokeRect(cx - 14, cy - 3, 8, 6);
        gc.strokeRect(cx + 6, cy - 3, 8, 6);

        // Baliza intermitente cyan
        if ((tick / 8) % 2 == 0) {
            gc.setFill(Color.web("#5be3ff"));
            gc.fillRect(cx - 1, cy - 7, 2, 2);
        }
    }

    private void dibujarEscudoDomePixel(GraphicsContext gc, double cx, double cy, EscudoProtector escudo, long tick) {
        double rEfectivo = escudo.getRadioEscudo();
        double pulso = 0.8 + 0.2 * Math.sin(escudo.getPulsoVisual());

        // Cúpula translúcida
        gc.setFill(Color.rgb(91, 227, 255, 0.15 * pulso));
        gc.fillOval(cx - rEfectivo, cy - rEfectivo, rEfectivo * 2, rEfectivo * 2);

        // Borde brillante hexagonal/circular
        gc.setStroke(Color.web("#5be3ff"));
        gc.setLineWidth(2.0);
        gc.setLineDashes(8, 4);
        gc.strokeOval(cx - rEfectivo, cy - rEfectivo, rEfectivo * 2, rEfectivo * 2);
        gc.setLineDashes(null);

        // Generador central del escudo
        gc.setFill(Color.BLACK);
        gc.fillOval(cx - 7, cy - 7, 14, 14);
        gc.setFill(Color.web("#5be3ff"));
        gc.fillOval(cx - 5, cy - 5, 10, 10);
    }

    private void dibujarMeteoritoPixel(GraphicsContext gc, double cx, double cy, Meteorito m, long tick) {
        Vector2D vel = m.getVelocidad();
        Vector2D dirOpuesta = vel.normalizar().multiplicar(-1.0);

        // Cola de fuego de partículas
        double lenCola = Math.min(40.0, vel.magnitud() * 0.04);
        double fx = cx + dirOpuesta.x * lenCola;
        double fy = cy + dirOpuesta.y * lenCola;

        gc.setStroke(Color.rgb(255, 100, 20, 0.8));
        gc.setLineWidth(4.0);
        gc.strokeLine(cx, cy, fx, fy);

        gc.setStroke(Color.rgb(255, 220, 50, 0.9));
        gc.setLineWidth(2.0);
        gc.strokeLine(cx, cy, cx + dirOpuesta.x * lenCola * 0.6, cy + dirOpuesta.y * lenCola * 0.6);

        // Roca del meteorito
        gc.setFill(Color.BLACK);
        gc.fillOval(cx - 5, cy - 5, 10, 10);
        gc.setFill(Color.web("#718096"));
        gc.fillOval(cx - 4, cy - 4, 8, 8);
    }

    private void dibujarAgujeroNegroPixel(GraphicsContext gc, double cx, double cy, double r, long tick) {
        // Disco de acreción arremolinado en violeta y cyan
        double rDisco = r * 2.2;
        double anguloGiro = tick * 2.5;

        gc.setStroke(Color.rgb(189, 147, 249, 0.7)); // Violeta
        gc.setLineWidth(3.0);
        gc.strokeArc(cx - rDisco, cy - rDisco * 0.6, rDisco * 2, rDisco * 1.2, anguloGiro, 120, javafx.scene.shape.ArcType.OPEN);

        gc.setStroke(Color.rgb(91, 227, 255, 0.85)); // Cyan
        gc.setLineWidth(2.0);
        gc.strokeArc(cx - rDisco * 0.8, cy - rDisco * 0.5, rDisco * 1.6, rDisco * 1.0, anguloGiro + 180, 140, javafx.scene.shape.ArcType.OPEN);

        // Horizonte de sucesos (negro absoluto con borde fino púrpura)
        gc.setFill(Color.BLACK);
        gc.fillOval(cx - r, cy - r, r * 2, r * 2);

        gc.setStroke(Color.web("#bd93f9"));
        gc.setLineWidth(1.5);
        gc.strokeOval(cx - r, cy - r, r * 2, r * 2);
    }

    private void dibujarPlanetaHeladoPixel(GraphicsContext gc, double cx, double cy, double r) {
        gc.setFill(Color.BLACK);
        gc.fillOval(cx - r - 2, cy - r - 2, (r + 2) * 2, (r + 2) * 2);

        gc.setFill(Color.web("#90cdf4"));
        gc.fillOval(cx - r, cy - r, r * 2, r * 2);

        // Grietas glaciares
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.0);
        gc.strokeLine(cx - r * 0.4, cy - r * 0.5, cx, cy);
        gc.strokeLine(cx, cy, cx + r * 0.5, cy + r * 0.3);
    }

    private void dibujarCuerpoGenerico(GraphicsContext gc, double cx, double cy, CuerpoCeleste c) {
        double r = c.getRadio();
        gc.setFill(Color.BLACK);
        gc.fillOval(cx - r - 2, cy - r - 2, (r + 2) * 2, (r + 2) * 2);
        gc.setFill(Color.web(c.getColorHex()));
        gc.fillOval(cx - r, cy - r, r * 2, r * 2);
    }

    // ===== Overlays de Interacción (Selección, Slingshot, Ghost Preview) =====

    private void dibujarSeleccion(GraphicsContext gc, CuerpoCeleste c, long tick) {
        double cx = ConstantesFisicas.fisicaAXJavaFX(c.getPosicionX(), anchoCanvas);
        double cy = ConstantesFisicas.fisicaAYJavaFX(c.getPosicionY(), altoCanvas);
        double r = c.getRadio() + 8.0;

        // Retícula de 4 esquinas cyan estilo RimWorld
        gc.setStroke(Color.web("#5be3ff"));
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
                gc.setStroke(Color.rgb(91, 227, 255, alpha));
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
        gc.setStroke(Color.web("#ffb86c"));
        gc.setLineWidth(2.5);
        gc.strokeLine(x0, y0, x1, y1);

        // Flecha de impulso resultante
        gc.setStroke(Color.web("#5be3ff"));
        gc.setLineWidth(3.0);
        gc.strokeLine(x0, y0, vx, vy);
        gc.fillOval(vx - 4, vy - 4, 8, 8);
    }

    private void dibujarGhostPreview(GraphicsContext gc, Vector2D posFisica, TipoCuerpo tipo, double factorMasa, long tick) {
        double x = ConstantesFisicas.fisicaAXJavaFX(posFisica.x, anchoCanvas);
        double y = ConstantesFisicas.fisicaAYJavaFX(posFisica.y, altoCanvas);
        double r = Math.max(8.0, Math.min(35.0, 15.0 * factorMasa));

        double pulso = 1.0 + 0.15 * Math.sin(tick * 0.25);
        double rPulsante = r * pulso;

        gc.setStroke(Color.web(tipo.getColorHexString()));
        gc.setLineWidth(2.0);
        gc.setLineDashes(4, 4);
        gc.strokeOval(x - rPulsante, y - rPulsante, rPulsante * 2, rPulsante * 2);
        gc.setLineDashes(null);

        // Tag informativo
        gc.setFill(Color.rgb(26, 28, 38, 0.9));
        gc.fillRect(x + r + 8, y - 20, 130, 42);
        gc.setStroke(Color.web("#5be3ff"));
        gc.setLineWidth(1.0);
        gc.strokeRect(x + r + 8, y - 20, 130, 42);

        gc.setFill(Color.web("#e8e4d8"));
        gc.fillText(tipo.nombre + " (" + String.format("%.1fx", factorMasa) + ")", x + r + 14, y - 4);
        gc.setFill(Color.web("#50fa7b"));
        gc.fillText("Click: Órbita | Drag: Lanzar", x + r + 14, y + 14);
    }

    // ===== Setters de Toggles =====

    public void toggleGrilla() { mostrarGrilla = !mostrarGrilla; }
    public void toggleEstelas() { mostrarEstelas = !mostrarEstelas; }
    public void toggleZonasHabitables() { mostrarZonasHabitables = !mostrarZonasHabitables; }
    public void setDimensiones(double w, double h) { this.anchoCanvas = w; this.altoCanvas = h; }
}
