package org.example.game.motor;

import java.util.List;

/**
 * Integrador Velocity Verlet para simulaciones N-body.
 *
 * Ventajas sobre Euler:
 * - Conserva energía a largo plazo (simétrico en el tiempo)
 * - Estable para pasos de tiempo mayores
 * - Reversible (importante para órbitas estables)
 *
 * Algoritmo:
 * v(t + Δt/2) = v(t) + a(t) * Δt/2
 * x(t + Δt) = x(t) + v(t + Δt/2) * Δt
 * a(t + Δt) = F(x(t + Δt)) / m
 * v(t + Δt) = v(t + Δt/2) + a(t + Δt) * Δt/2
 */
public final class IntegradorVelocityVerlet {

    private final double dt;           // Paso de tiempo en segundos
    private final double softening;    // Factor de suavizado para evitar singularidades
    private final double G;            // Constante gravitacional

    public IntegradorVelocityVerlet(double dt, double G, double softening) {
        this.dt = dt;
        this.G = G;
        this.softening = softening;
    }

    /**
     * Avanza un paso completo de Velocity Verlet.
     *
     * @param cuerpos Lista de cuerpos con masa, posición, velocidad, fuerza actual
     */
    public void avanzarPaso(List<CuerpoFisico> cuerpos) {
        double dtMedio = dt * 0.5;

        // Paso 1: v(t + Δt/2) = v(t) + a(t) * Δt/2
        // Paso 2: x(t + Δt) = x(t) + v(t + Δt/2) * Δt
        for (CuerpoFisico cuerpo : cuerpos) {
            if (!cuerpo.esSimulado()) continue;

            Vector2D aceleracionActual = cuerpo.getFuerza().dividir(cuerpo.getMasa());
            Vector2D velocidadMedio = cuerpo.getVelocidad().sumar(aceleracionActual.multiplicar(dtMedio));
            Vector2D nuevaPosicion = cuerpo.getPosicion().sumar(velocidadMedio.multiplicar(dt));

            // Guardar velocidad a medio paso y nueva posición temporalmente
            cuerpo.setVelocidadMedioPaso(velocidadMedio);
            cuerpo.setNuevaPosicion(nuevaPosicion);
        }

        // Calcular nuevas fuerzas en las nuevas posiciones
        calcularFuerzas(cuerpos);

        // Paso 3: v(t + Δt) = v(t + Δt/2) + a(t + Δt) * Δt/2
        for (CuerpoFisico cuerpo : cuerpos) {
            if (!cuerpo.esSimulado()) continue;

            Vector2D nuevaAceleracion = cuerpo.getFuerza().dividir(cuerpo.getMasa());
            Vector2D nuevaVelocidad = cuerpo.getVelocidadMedioPaso().sumar(nuevaAceleracion.multiplicar(dtMedio));

            // Confirmar posición y velocidad finales
            cuerpo.confirmarPaso(cuerpo.getNuevaPosicion(), nuevaVelocidad);
        }
    }

    /**
     * Calcula fuerzas gravitacionales entre todos los pares de cuerpos (N-body).
     * O(n²) - para >200 cuerpos considerar Barnes-Hut o spatial hashing.
     *
     * F = G * m1 * m2 / (r² + softening²) * dirección
     */
    private void calcularFuerzas(List<CuerpoFisico> cuerpos) {
        // Resetear fuerzas
        for (CuerpoFisico c : cuerpos) {
            c.resetearFuerza();
        }

        double softeningCuadrado = softening * softening;

        // Pares únicos (i < j) para evitar doble cálculo
        for (int i = 0; i < cuerpos.size(); i++) {
            CuerpoFisico ci = cuerpos.get(i);
            if (!ci.esSimulado()) continue;

            for (int j = i + 1; j < cuerpos.size(); j++) {
                CuerpoFisico cj = cuerpos.get(j);
                if (!cj.esSimulado()) continue;

                Vector2D r = cj.getPosicion().restar(ci.getPosicion());
                double r2 = r.magnitudCuadrado() + softeningCuadrado;
                double rMag = Math.sqrt(r2);

                // Fuerza magnitud: G * m1 * m2 / r²
                double fuerzaMag = G * ci.getMasa() * cj.getMasa() / r2;

                // Vector fuerza sobre ci (hacia cj)
                Vector2D direccion = r.dividir(rMag);
                Vector2D fuerzaSobreI = direccion.multiplicar(fuerzaMag);
                Vector2D fuerzaSobreJ = fuerzaSobreI.multiplicar(-1); // Tercera ley de Newton

                ci.aplicarFuerza(fuerzaSobreI);
                cj.aplicarFuerza(fuerzaSobreJ);
            }
        }
    }

    // Getters para configuración
    public double getDt() { return dt; }
    public double getG() { return G; }
    public double getSoftening() { return softening; }
}