package org.example.game.cuerpo;
/**
 * Satélite artificial que recopila datos científicos y transmite energía.
 * SRP: Representa un artefacto tecnológico creado por el jugador o civilizaciones.
 */
public class Satelite extends CuerpoCeleste implements Actualizable {

    private double cienciaGenerada = 0;
    private double energiaGenerada = 0;

    public Satelite(String nombre, double masa, double x, double y) {
        super(nombre, masa, x, y, TipoCuerpo.SATELITE);
    }

    @Override
    public void actualizarEntidad(double dt) {
        producirRecursos(dt);
    }

    /** Genera recursos pasivos periódicamente (ciencia y energía) */
    public void producirRecursos(double dt) {
        cienciaGenerada += org.example.game.simulacion.ConfiguracionSimulacion.PRODUCCION_SATELITE_CIENCIA * dt;
        energiaGenerada += org.example.game.simulacion.ConfiguracionSimulacion.PRODUCCION_SATELITE_ENERGIA * dt;
    }

    public double getCienciaGenerada() {
        return cienciaGenerada;
    }

    public double getEnergiaGenerada() {
        return energiaGenerada;
    }

    public void restaurarProduccionAcumulada(double ciencia, double energia) {
        this.cienciaGenerada = Math.max(0.0, ciencia);
        this.energiaGenerada = Math.max(0.0, energia);
    }

    @Override
    public java.util.Optional<Actualizable> comoActualizable() {
        return java.util.Optional.of(this);
    }

    @Override
    public boolean esSatelite() {
        return true;
    }

    @Override
    public boolean esCuerpoMenor() {
        return true;
    }

    @Override
    public <R> R aceptar(VisitanteCuerpo<R> visitante) {
        return visitante.visitarSatelite(this);
    }

    @Override
    public String describir() {
        return "Satélite " + getNombre() + " (Ciencia: +" + String.format("%.1f", cienciaGenerada) + ", Energía: +" + String.format("%.1f", energiaGenerada) + ")";
    }

    @Override
    public double getRadio() {
        return 7.0;
    }

    @Override
    public String getColorHex() {
        return TipoCuerpo.SATELITE.getColorHexString();
    }
}
