package org.example.game.controlador;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.Vector2D;
import org.example.game.simulacion.SimulacionSolar;
import java.util.ArrayList;
import java.util.List;
/**
 * Responsable de la predicción y cálculo throttled de trayectorias preview para el cursor del mouse (SRP).
 */
public class PredictorPreviewMouse {

    private List<Vector2D> trayectoriaPreview = new ArrayList<>();
    private double ultimoPreviewMouseX = Double.NaN;
    private double ultimoPreviewMouseY = Double.NaN;
    private long ultimoTiempoPrediccionMs = 0;

    public void actualizarConThrottle(double mouseX, double mouseY, SimulacionSolar simulacion) {
        long ahora = System.currentTimeMillis();
        double dx = mouseX - ultimoPreviewMouseX;
        double dy = mouseY - ultimoPreviewMouseY;

        if (Double.isNaN(ultimoPreviewMouseX) || (dx * dx + dy * dy >= 4.0 && ahora - ultimoTiempoPrediccionMs >= 16)) {
            ultimoPreviewMouseX = mouseX;
            ultimoPreviewMouseY = mouseY;
            ultimoTiempoPrediccionMs = ahora;
            actualizarPrediccion(simulacion);
        }
    }

    public void actualizarPrediccion(SimulacionSolar simulacion) {
        if (simulacion == null) return;
        Vector2D pos = simulacion.getGestorColocacion().getPosicionPreview();
        if (pos == null) return;

        Vector2D velOrbital = simulacion.getGestorColocacion().calcularVelocidadOrbitalAsistida(pos, simulacion.getSistemaSolar().getCuerpos());
        TipoCuerpo tipo = simulacion.getGestorColocacion().getTipoColocacion();
        double masa = tipo != null ? tipo.masaBase * simulacion.getGestorColocacion().getFactorMasaColocacion() : 1e24;

        trayectoriaPreview = simulacion.getPredictor().predecirTrayectoria(
                pos, velOrbital, masa,
                simulacion.getSistemaSolar().getCuerpos(), 75, 1.0
        );
    }

    public void limpiar() {
        trayectoriaPreview.clear();
        ultimoPreviewMouseX = Double.NaN;
        ultimoPreviewMouseY = Double.NaN;
        ultimoTiempoPrediccionMs = 0;
    }

    public List<Vector2D> getTrayectoriaPreview() {
        return trayectoriaPreview;
    }
}
