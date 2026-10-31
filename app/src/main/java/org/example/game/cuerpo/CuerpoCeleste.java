package org.example.game.cuerpo;
import org.example.game.civilizacion.CivilizacionComponent;
import org.example.game.motor.CuerpoFisico;
import org.example.game.motor.Vector2D;
import java.util.List;
/**
 * SRP: guarda datos básicos (nombre, masa, posición, velocidad, fuerza).
 * OCP: getColorHex()/getRadio() son abstractos -> cada subclase nueva
 * define su propio dibujo sin tocar esta clase ni la vista.
 *
 * Implementa CuerpoFisico para simulación física.
 */
public abstract class CuerpoCeleste implements Masivo, CuerpoFisico {
    private String nombre;
    private double masa;
    private double posicionX;
    private double posicionY;

    // Estado dinámico para física
    private Vector2D velocidad = Vector2D.cero();
    private Vector2D fuerza = Vector2D.cero();
    private boolean simulado = true;

    // Tipo de cuerpo para lógica especial
    private final TipoCuerpo tipoCuerpo;
    private CuerpoCeleste cuerpoOrbitado;
    private double radioOrbita;

    private final double masaInicial;

    public CuerpoCeleste(String nombre, double masa, double posicionX, double posicionY, TipoCuerpo tipoCuerpo) {
        this.nombre = nombre;
        this.masa = masa;
        this.masaInicial = masa;
        this.posicionX = posicionX;
        this.posicionY = posicionY;
        this.tipoCuerpo = tipoCuerpo;
        this.radioOrbita = 0;
    }

    // ===== Getters/Setters básicos =====

    @Override
    public double getMasa() {
        return masa;
    }

    public double getMasaInicial() {
        return masaInicial;
    }

    @Override
    public void setMasa(double masa) {
        this.masa = masa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre != null && !nombre.isBlank()) {
            this.nombre = nombre.trim();
        }
    }

    public double getPosicionX() {
        return posicionX;
    }

    public double getPosicionY() {
        return posicionY;
    }

    public void mover(double nuevaX, double nuevaY) {
        this.posicionX = nuevaX;
        this.posicionY = nuevaY;
    }

    // ===== Implementación CuerpoFisico =====

    @Override
    public Vector2D getVelocidad() {
        return velocidad;
    }

    @Override
    public void setVelocidad(Vector2D velocidad) {
        this.velocidad = velocidad;
    }

    @Override
    public Vector2D getFuerza() {
        return fuerza;
    }

    @Override
    public void setFuerza(Vector2D fuerza) {
        this.fuerza = fuerza;
    }

    @Override
    public void aplicarFuerza(Vector2D fuerza) {
        this.fuerza = this.fuerza.sumar(fuerza);
    }

    @Override
    public void resetearFuerza() {
        this.fuerza = Vector2D.cero();
    }

    @Override
    public void confirmarPaso(Vector2D nuevaPosicion, Vector2D nuevaVelocidad) {
        this.posicionX = nuevaPosicion.x();
        this.posicionY = nuevaPosicion.y();
        this.velocidad = nuevaVelocidad;
    }

    @Override
    public boolean esSimulado() {
        return simulado;
    }

    @Override
    public void setSimulado(boolean simulado) {
        this.simulado = simulado;
    }

    @Override
    public TipoCuerpo getTipoCuerpo() {
        return tipoCuerpo;
    }

    @Override
    public double getRadioFisico() {
        TipoCuerpo tipoActual = getTipoCuerpo();
        return tipoActual != null ? tipoActual.radioFisicoBase : 6.37e6;
    }

    @Override
    public double getRadioAtraccion() {
        TipoCuerpo tipoActual = getTipoCuerpo();
        if (tipoActual == null) return 20.0;
        double factorMasa = (tipoActual.masaBase > 0) ? Math.max(0.01, masa / tipoActual.masaBase) : 1.0;
        return Math.max(tipoActual.getRadioAtraccionMin(), tipoActual.getRadioAtraccionMax() * Math.sqrt(factorMasa));
    }

    /**
     * Indica si el cuerpo fue transformado por un evento cosmico.
     */
    public boolean isTransformadoPorEvento() {
        return false;
    }

    /**
     * Indica si el cuerpo tiene capacidad para transformarse bajo condiciones extremas.
     */
    public boolean esTransformable() {
        return false;
    }

    /**
     * Marca o desmarca este cuerpo como transformado por un evento cosmico.
     */
    public void setTransformadoPorEvento(boolean transformado) {
        // Implementado por subclases transformables
    }

    /**
     * Transforma polimorficamente este cuerpo hacia otro tipo celeste si es transformable.
     */
    public void transformarA(TipoCuerpo nuevoTipo) {
        // Implementado por subclases transformables
    }

    /**
     * Absorbe a otro cuerpo celeste en este cuerpo (por ejemplo horizonte de sucesos).
     */
    public void absorber(CuerpoCeleste victima) {
        // Implementado por subclases masivas absorbentes
    }

    /**
     * Indica si el jugador puede recibir un reembolso de recursos al desmantelar este cuerpo.
     */
    public boolean permiteReembolso() {
        return !isTransformadoPorEvento();
    }

    /**
     * Indica si el cuerpo puede ser seleccionado, inspeccionado o eliminado por interaccion directa del usuario.
     */
    public boolean esInteractuablePorUsuario() {
        return true;
    }

    /**
     * Indica si el cuerpo posee una civilizacion activa.
     */
    public boolean tieneCivilizacion() {
        return false;
    }

    /**
     * Obtiene el componente de civilizacion asociado, o null si no posee.
     */
    public org.example.game.civilizacion.CivilizacionComponent getCivilizacion() {
        return null;
    }

    /**
     * Actualiza el estado de la civilizacion planetaria si corresponde.
     */
    public void actualizarCivilizacion(java.util.List<CuerpoCeleste> cuerpos) {
        // Implementado por subclases habitadas
    }

    /**
     * Indica si el cuerpo es apto para recibir escudos defensivos.
     */
    public boolean puedeRecibirEscudo() {
        return false;
    }

    /**
     * Indica polimorficamente si el cuerpo mantiene un escudo defensivo activo.
     */
    public boolean tieneEscudo() {
        return false;
    }

    /**
     * Cantidad de impactos de meteorito que el escudo puede soportar antes de disiparse.
     */
    public int getGolpesEscudoRestantes() {
        return 0;
    }

    /**
     * Absorbe un impacto en el escudo si se encuentra activo. Retorna true si fue absorbido.
     */
    public boolean absorberGolpeEscudo() {
        return false;
    }

    /**
     * Indica si el cuerpo es apto para ser colonizado.
     */
    public boolean puedeSerColonizado() {
        return false;
    }

    /**
     * Recibe una expedicion de colonos en este cuerpo celeste si es apto.
     */
    public void recibirColonos(double colonos) {
        // Implementado por subclases colonizables
    }

    /**
     * Indica si el cuerpo ha expirado y debe retirarse de la simulacion.
     */
    public boolean estaExpirado() {
        return false;
    }

    /**
     * Actualiza el estado autonomo de la entidad por tick de simulacion.
     */
    public void actualizarEntidad(double dt) {
        // Implementado por entidades autonomas
    }

    /**
     * Actualiza estado interno por paso de tiempo fisico.
     */
    public void actualizarEstado(double dt) {
        // Implementado por subclases dinamicas
    }

    /**
     * Factor de progreso asignado al origen balistico del cuerpo celeste.
     */
    public double getFactorProgresoOrigen() {
        return -1.0;
    }

    /**
     * Indica si el cuerpo posee un progreso de amenaza explicito.
     */
    public boolean tieneProgresoExplicito() {
        return false;
    }

    // ===== Métodos abstractos para vista y despacho polimórfico =====

    public abstract <R> R aceptar(VisitanteCuerpo<R> visitante);

    public abstract String describir();

    /** Radio en píxeles para dibujarlo. Cada subclase decide el suyo. */
    public abstract double getRadio();

    /** Color en formato CSS (ej: "yellow", "#3355ff"). */
    public abstract String getColorHex();

    // ===== Utilidades vectoriales =====

    @Override
    public Vector2D getPosicion() {
        return new Vector2D(posicionX, posicionY);
    }

    @Override
    public void setPosicion(Vector2D pos) {
        this.posicionX = pos.x();
        this.posicionY = pos.y();
    }

    // ===== Estado Orbital =====

    public CuerpoCeleste getCuerpoOrbitado() {
        return cuerpoOrbitado;
    }

    public void setCuerpoOrbitado(CuerpoCeleste cuerpoOrbitado) {
        this.cuerpoOrbitado = cuerpoOrbitado;
    }

    public boolean estaOrbitando() {
        return cuerpoOrbitado != null;
    }

    public boolean estaOrbitando(CuerpoCeleste cuerpo) {
        return cuerpoOrbitado != null && cuerpoOrbitado == cuerpo;
    }

    public double getRadioOrbita() {
        return radioOrbita;
    }

    public void setRadioOrbita(double radioOrbita) {
        this.radioOrbita = radioOrbita;
    }
}
