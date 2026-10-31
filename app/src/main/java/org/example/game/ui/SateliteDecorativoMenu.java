package org.example.game.ui;
import org.example.game.motor.Vector2D;
/**
 * SRP: Modelo exclusivamente estetico de un satelite decorativo para la pantalla del menu principal.
 * Gira de forma continua sobre su propio eje y, al recibir clic, cae verticalmente con aceleracion
 * gravitatoria promedio hasta salir por debajo de la pantalla, sin relacion alguna con la simulacion.
 */
public class SateliteDecorativoMenu {

    public enum Lado {
        IZQUIERDA,
        DERECHA
    }

    public static final double GRAVEDAD_TIERRA_MS2 = 9.81;
    public static final double ESCALA_PIXELES_POR_METRO = 55.0;
    public static final double GRAVEDAD_PROMEDIO_PX_SEG2 = GRAVEDAD_TIERRA_MS2 * ESCALA_PIXELES_POR_METRO;
    public static final double TAMANO_VISUAL_PX = 42.0;

    private static final double GRADOS_CIRCUNFERENCIA = 360.0;
    private static final double FACTOR_MARGEN_SALIDA = 0.80;
    private static final double FACTOR_RADIO_CLIC = 0.62;

    private final Lado lado;
    private final double fraccionX;
    private final double fraccionYInicial;
    private final double velocidadGiroGradosPorSegundo;
    private final double anguloInicialGrados;

    private double anguloGrados;
    private boolean cayendo = false;
    private double velocidadCaidaY = 0.0;
    private double desplazamientoCaidaY = 0.0;

    public SateliteDecorativoMenu(Lado lado,
                                  double fraccionX,
                                  double fraccionYInicial,
                                  double velocidadGiroGradosPorSegundo) {
        this.lado = lado != null ? lado : Lado.IZQUIERDA;
        this.fraccionX = fraccionX;
        this.fraccionYInicial = fraccionYInicial;
        this.velocidadGiroGradosPorSegundo = velocidadGiroGradosPorSegundo;
        double base = (fraccionYInicial * GRADOS_CIRCUNFERENCIA) % GRADOS_CIRCUNFERENCIA;
        this.anguloInicialGrados = base < 0.0 ? base + GRADOS_CIRCUNFERENCIA : base;
        this.anguloGrados = this.anguloInicialGrados;
    }

    public void avanzar(double deltaSegundos) {
        if (!Double.isFinite(deltaSegundos) || deltaSegundos <= 0.0) {
            return;
        }
        double siguiente = (anguloGrados + velocidadGiroGradosPorSegundo * deltaSegundos) % GRADOS_CIRCUNFERENCIA;
        anguloGrados = siguiente < 0.0 ? siguiente + GRADOS_CIRCUNFERENCIA : siguiente;
        if (cayendo) {
            desplazamientoCaidaY += velocidadCaidaY * deltaSegundos
                    + 0.5 * GRAVEDAD_PROMEDIO_PX_SEG2 * deltaSegundos * deltaSegundos;
            velocidadCaidaY += GRAVEDAD_PROMEDIO_PX_SEG2 * deltaSegundos;
        }
    }

    public void iniciarCaida() {
        this.cayendo = true;
    }

    public void reiniciar() {
        this.anguloGrados = this.anguloInicialGrados;
        this.cayendo = false;
        this.velocidadCaidaY = 0.0;
        this.desplazamientoCaidaY = 0.0;
    }

    public Vector2D calcularCentro(double anchoCanvas, double altoCanvas) {
        return new Vector2D(anchoCanvas * fraccionX, altoCanvas * fraccionYInicial + desplazamientoCaidaY);
    }

    public boolean estaFueraDePantalla(double altoCanvas) {
        double centroY = altoCanvas * fraccionYInicial + desplazamientoCaidaY;
        return altoCanvas > 0.0 && (centroY - TAMANO_VISUAL_PX * FACTOR_MARGEN_SALIDA) > altoCanvas;
    }

    public boolean contienePunto(double x, double y, double anchoCanvas, double altoCanvas) {
        if (anchoCanvas <= 0.0 || altoCanvas <= 0.0 || estaFueraDePantalla(altoCanvas)) {
            return false;
        }
        Vector2D centro = calcularCentro(anchoCanvas, altoCanvas);
        double dx = x - centro.x();
        double dy = y - centro.y();
        double radioClic = TAMANO_VISUAL_PX * FACTOR_RADIO_CLIC;
        return (dx * dx + dy * dy) <= (radioClic * radioClic);
    }

    public EstadoSateliteMenu getEstado() {
        return new EstadoSateliteMenu(lado, anguloGrados, cayendo, new Vector2D(desplazamientoCaidaY, velocidadCaidaY));
    }
}
