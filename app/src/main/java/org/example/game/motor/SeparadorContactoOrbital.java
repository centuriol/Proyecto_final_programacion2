package org.example.game.motor;

import org.example.game.cuerpo.CuerpoCeleste;

/**
 * SRP: Separa contactos físicos entre un cuerpo orbitante y su cuerpo anfitrión manteniendo órbita estable.
 */
public class SeparadorContactoOrbital {

    private static final FabricaMotorFisica FABRICA = new FabricaMotorFisica();
    private final AsistenciaOrbital asistenteOrbital;

    public SeparadorContactoOrbital() {
        this(FABRICA.crearAsistenciaOrbitalPorDefecto());
    }

    public SeparadorContactoOrbital(AsistenciaOrbital asistenteOrbital) {
        this.asistenteOrbital = asistenteOrbital != null ? asistenteOrbital : FABRICA.crearAsistenciaOrbitalPorDefecto();
    }

    public void separar(CuerpoCeleste orbitante, CuerpoCeleste orbitado) {
        double minOrb = Math.max(
                orbitado.getRadio() + orbitante.getRadio() + 4.0,
                AsistenteOrbital.calcularRadioOrbitalMinimo(orbitante, orbitado)
        );
        Vector2D diff = orbitante.getPosicion().restar(orbitado.getPosicion());
        double dActual = diff.magnitud();
        Vector2D norm = dActual > 0.001 ? diff.dividir(dActual) : new Vector2D(1, 0);
        orbitante.setPosicion(orbitado.getPosicion().sumar(norm.multiplicar(minOrb)));

        if (orbitante.estaOrbitando(orbitado)) {
            ajustarVelocidadOrbital(orbitante, orbitado, diff, norm, minOrb);
        }
    }

    private void ajustarVelocidadOrbital(CuerpoCeleste orbitante, CuerpoCeleste orbitado,
                                         Vector2D diff, Vector2D norm, double minOrb) {
        double rAcotado = AsistenteOrbital.acotarRadioOrbital(orbitante, orbitado, Math.max(orbitante.getRadioOrbita(), minOrb));
        orbitante.setRadioOrbita(rAcotado);
        Vector2D vRel = orbitante.getVelocidad().restar(orbitado.getVelocidad());
        double cross = diff.x() * vRel.y() - diff.y() * vRel.x();
        Vector2D uTangencial = (cross < -1e-4)
                ? new Vector2D(norm.y(), -norm.x())
                : new Vector2D(-norm.y(), norm.x());
        double vIdeal = asistenteOrbital.calcularVelocidadOrbitalIdeal(orbitante, orbitado, rAcotado);
        orbitante.setVelocidad(orbitado.getVelocidad().sumar(uTangencial.multiplicar(vIdeal)));
    }

    public boolean estaOrbitandoPlaneta(CuerpoCeleste menor) {
        CuerpoCeleste host = menor.getCuerpoOrbitado();
        return menor.estaOrbitando() && host != null && host.esSimulado()
                && host.getTipoCuerpo() != null && host.getTipoCuerpo().esPlaneta();
    }

    public boolean estaOrbitandoOtroPlaneta(CuerpoCeleste menor, CuerpoCeleste planeta) {
        return estaOrbitandoPlaneta(menor) && menor.getCuerpoOrbitado() != planeta;
    }
}
