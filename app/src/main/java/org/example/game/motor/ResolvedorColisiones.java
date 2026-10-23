package org.example.game.motor;

import org.example.AgujeroNegro;
import org.example.CuerpoCeleste;
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.cuerpo.Meteorito;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.simulacion.ConfiguracionSimulacion;
import org.example.game.simulacion.MensajeEvento;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Resolvedor desacoplado de colisiones e interacciones físicas y destructivas entre cuerpos celestes.
 *
 * Principios SOLID:
 * - SRP: Responsable exclusivo de la resolución cinemática y destructiva de colisiones
 *   (absorciones, impactos de meteoritos, defensas de escudo, escombros, colisiones estelares y rebotes).
 * - OCP: Permite extender la respuesta ante colisiones y configurar listeners de eventos.
 * - LSP: Utiliza las propiedades y métodos polimórficos de CuerpoCeleste sin downcasting frágil.
 */
public class ResolvedorColisiones {

    private Consumer<String> listenerMensaje;
    private BiConsumer<String, MensajeEvento.TipoMensaje> listenerNotificacion;
    private BiConsumer<Vector2D, Double> listenerExplosion;

    public void setListenerMensaje(Consumer<String> listener) {
        this.listenerMensaje = listener;
    }

    public void setListenerNotificacion(BiConsumer<String, MensajeEvento.TipoMensaje> listener) {
        this.listenerNotificacion = listener;
    }

    public void setListenerExplosion(BiConsumer<Vector2D, Double> listener) {
        this.listenerExplosion = listener;
    }

    /**
     * Resuelve colisión completa entre dos cuerpos celestes.
     */
    public void resolverColision(CuerpoCeleste a, CuerpoCeleste b, List<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar) {
        TipoCuerpo tipoA = a.getTipoCuerpo();
        TipoCuerpo tipoB = b.getTipoCuerpo();

        // 1. Dos ESTRELLA colisionan: colapso estelar que engendra un Agujero Negro
        if (tipoA == TipoCuerpo.ESTRELLA && tipoB == TipoCuerpo.ESTRELLA) {
            fusionarEstrellasEnAgujeroNegro(a, b, aEliminar, aAgregar);
            return;
        }

        // 2. Agujero negro devora todo
        boolean aEsBH = (tipoA != null && tipoA.esAgujeroNegro());
        boolean bEsBH = (tipoB != null && tipoB.esAgujeroNegro());

        if (aEsBH && bEsBH) {
            CuerpoCeleste mayor = a.getMasa() >= b.getMasa() ? a : b;
            CuerpoCeleste menor = (mayor == a) ? b : a;
            absorber(mayor, menor, aEliminar);
            return;
        }
        if (aEsBH) {
            absorber(a, b, aEliminar);
            return;
        }
        if (bEsBH) {
            absorber(b, a, aEliminar);
            return;
        }

        // 3. Estrella desintegra meteoritos o satélites en su corona
        if (tipoA == TipoCuerpo.ESTRELLA && (tipoB == TipoCuerpo.METEORITO || tipoB == TipoCuerpo.SATELITE)) {
            desactivarYEliminar(b, aEliminar);
            emitirMensaje(a.getNombre() + " desintegró a " + b.getNombre() + " en su corona.");
            return;
        }
        if (tipoB == TipoCuerpo.ESTRELLA && (tipoA == TipoCuerpo.METEORITO || tipoA == TipoCuerpo.SATELITE)) {
            desactivarYEliminar(a, aEliminar);
            emitirMensaje(b.getNombre() + " desintegró a " + a.getNombre() + " en su corona.");
            return;
        }

        // 4. Dos meteoritos: destrucción mutua directa
        if (tipoA == TipoCuerpo.METEORITO && tipoB == TipoCuerpo.METEORITO) {
            desactivarYEliminar(a, aEliminar);
            desactivarYEliminar(b, aEliminar);
            emitirMensaje("Destrucción mutua de meteoritos por impacto directo.");
            return;
        }

        // 4.5. Dos satélites no colapsan entre sí (conviven en la misma órbita planetaria)
        if (tipoA == TipoCuerpo.SATELITE && tipoB == TipoCuerpo.SATELITE) {
            return;
        }

        // 5. Interacción con Escudo Planetario (único punto de decisión defensivo)
        if (resolverProteccionEscudo(a, b, aEliminar)) {
            return;
        }

        // 6. Meteorito impacta un cuerpo no-meteorito (sin escudo protector activo)
        if (tipoA == TipoCuerpo.METEORITO && tipoB != TipoCuerpo.METEORITO) {
            impactarMeteorito((Meteorito) a, b, aEliminar);
            return;
        }
        if (tipoB == TipoCuerpo.METEORITO && tipoA != TipoCuerpo.METEORITO) {
            impactarMeteorito((Meteorito) b, a, aEliminar);
            return;
        }

        // 7. Easter Egg: colisión hidro-magmática entre planeta de agua y planeta de lava
        if (esColisionAguaYLava(tipoA, tipoB)) {
            fusionarPlanetasAguaYLava(a, b, aEliminar);
            return;
        }

        // 8. Colisión astronómica entre cuerpos mayores
        double masaA = Math.max(1e-9, a.getMasa());
        double masaB = Math.max(1e-9, b.getMasa());
        double masaMayor = Math.max(masaA, masaB);
        double masaMenor = Math.min(masaA, masaB);
        double ratioMasa = masaMayor / masaMenor;
        CuerpoCeleste mayor = a.getMasa() >= b.getMasa() ? a : b;
        CuerpoCeleste menor = (mayor == a) ? b : a;

        // Proporción de masa extrema (>= RATIO_ABSORCION_MASA): absorción total
        if (ratioMasa >= ConfiguracionSimulacion.RATIO_ABSORCION_MASA) {
            absorber(mayor, menor, aEliminar);
            emitirMensaje(mayor.getNombre() + " absorbió por gravedad a " + menor.getNombre() + ".");
            return;
        }

        // Masas aproximadamente iguales (<= RATIO_COLISION_COLAPSO): colapso mutuo en fragmentos
        if (ratioMasa <= ConfiguracionSimulacion.RATIO_COLISION_COLAPSO) {
            generarEscombros(a, aEliminar, aAgregar);
            generarEscombros(b, aEliminar, aAgregar);
            emitirMensaje("Colisión catastrófica mutua: " + a.getNombre() + " y " + b.getNombre() + " colapsaron en escombros.");
            return;
        }

        // Caso asimétrico: menor se destruye y el mayor gana un % de masa
        double masaGanada = menor.getMasa() * ConfiguracionSimulacion.PORCENTAJE_GANANCIA_MASA_COLISION;
        mayor.setMasa(mayor.getMasa() + masaGanada);
        generarEscombros(menor, aEliminar, aAgregar);

        if (mayor.tieneCivilizacion() && mayor.getCivilizacion() != null) {
            mayor.getCivilizacion().recibirImpactoMeteorito();
        }

        emitirMensaje("Impacto destructivo asimétrico: " + menor.getNombre() + " destruido al colisionar con " + mayor.getNombre() + ".");
    }

    /**
     * Resuelve las defensas proporcionadas por escudos planetarios.
     * Retorna true si el escudo interceptó o modificó la colisión.
     */
    private boolean resolverProteccionEscudo(CuerpoCeleste a, CuerpoCeleste b, List<CuerpoCeleste> aEliminar) {
        // Escudo vs Meteorito: el escudo absorbe el impacto y destruye el meteorito sin daño a masa ni población
        if (a.getTipoCuerpo() == TipoCuerpo.METEORITO && b.tieneEscudo()) {
            b.absorberGolpeEscudo();
            desactivarYEliminar(a, aEliminar);
            emitirMensaje("El escudo planetario de " + b.getNombre() + " absorbió un impacto de meteorito (" + b.getGolpesEscudoRestantes() + " golpes restantes).");
            return true;
        }
        if (b.getTipoCuerpo() == TipoCuerpo.METEORITO && a.tieneEscudo()) {
            a.absorberGolpeEscudo();
            desactivarYEliminar(b, aEliminar);
            emitirMensaje("El escudo planetario de " + a.getNombre() + " absorbió un impacto de meteorito (" + a.getGolpesEscudoRestantes() + " golpes restantes).");
            return true;
        }

        // Escudo vs Planeta/Luna: si alguno tiene escudo, consumen un golpe y rebotan elásticamente
        boolean aEsPlanetaOLuna = a.getTipoCuerpo() != null && (a.getTipoCuerpo().esPlaneta() || a.getTipoCuerpo().esLuna());
        boolean bEsPlanetaOLuna = b.getTipoCuerpo() != null && (b.getTipoCuerpo().esPlaneta() || b.getTipoCuerpo().esLuna());

        if (aEsPlanetaOLuna && bEsPlanetaOLuna && (a.tieneEscudo() || b.tieneEscudo())) {
            if (a.tieneEscudo()) a.absorberGolpeEscudo();
            if (b.tieneEscudo()) b.absorberGolpeEscudo();
            reboteElastico(a, b);
            emitirMensaje("Interacción orbital amortiguada por escudo planetario: rebote elástico entre " + a.getNombre() + " y " + b.getNombre() + ".");
            return true;
        }

        return false;
    }

    private void fusionarEstrellasEnAgujeroNegro(CuerpoCeleste a, CuerpoCeleste b, List<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar) {
        double masaTotal = a.getMasa() + b.getMasa();
        Vector2D posCM = (a.getPosicion().multiplicar(a.getMasa()).sumar(b.getPosicion().multiplicar(b.getMasa()))).dividir(Math.max(1e-9, masaTotal));
        Vector2D velCM = (a.getVelocidad().multiplicar(a.getMasa()).sumar(b.getVelocidad().multiplicar(b.getMasa()))).dividir(Math.max(1e-9, masaTotal));

        AgujeroNegro bh = CuerpoCelesteFactory.crearAgujeroNegroPersonalizado(posCM.x, posCM.y, masaTotal, velCM);

        desactivarYEliminar(a, aEliminar);
        desactivarYEliminar(b, aEliminar);
        aAgregar.add(bh);

        if (listenerExplosion != null) {
            listenerExplosion.accept(posCM, bh.getRadio() * 3.0);
        }

        emitirEvento("¡COLAPSO ESTELAR! Choque entre dos Soles dio origen a un temible Agujero Negro.", MensajeEvento.TipoMensaje.PELIGRO);
    }

    public void absorber(CuerpoCeleste absorbente, CuerpoCeleste victima, List<CuerpoCeleste> aEliminar) {
        double masaTotal = absorbente.getMasa() + victima.getMasa();
        if (masaTotal > 0) {
            Vector2D momentoAbsorbente = absorbente.getVelocidad().multiplicar(absorbente.getMasa());
            Vector2D momentoVictima = victima.getVelocidad().multiplicar(victima.getMasa());
            Vector2D nuevaVelocidad = momentoAbsorbente.sumar(momentoVictima).dividir(masaTotal);
            absorbente.setVelocidad(nuevaVelocidad);
        }

        absorbente.setMasa(masaTotal);
        desactivarYEliminar(victima, aEliminar);
    }

    private boolean esColisionAguaYLava(TipoCuerpo tipoA, TipoCuerpo tipoB) {
        return (tipoA == TipoCuerpo.PLANETA_AGUA && tipoB == TipoCuerpo.PLANETA_LAVA)
                || (tipoA == TipoCuerpo.PLANETA_LAVA && tipoB == TipoCuerpo.PLANETA_AGUA);
    }

    public void fusionarPlanetasAguaYLava(CuerpoCeleste a, CuerpoCeleste b, List<CuerpoCeleste> aEliminar) {
        CuerpoCeleste sobreviviente = (a.getMasa() >= b.getMasa()) ? a : b;
        CuerpoCeleste absorbido = (sobreviviente == a) ? b : a;

        double masaA = Math.max(1e-9, a.getMasa());
        double masaB = Math.max(1e-9, b.getMasa());
        double masaTotal = masaA + masaB;

        Vector2D posA = a.getPosicion();
        Vector2D posB = b.getPosicion();
        Vector2D nuevaPos = (posA.multiplicar(masaA).sumar(posB.multiplicar(masaB))).dividir(masaTotal);

        Vector2D velA = a.getVelocidad();
        Vector2D velB = b.getVelocidad();
        Vector2D nuevoMomento = velA.multiplicar(masaA).sumar(velB.multiplicar(masaB));
        Vector2D nuevaVel = nuevoMomento.dividir(masaTotal);

        sobreviviente.setPosicion(nuevaPos);
        sobreviviente.setVelocidad(nuevaVel);
        sobreviviente.setMasa(masaTotal);
        sobreviviente.setNombre("Planeta Porro");
        sobreviviente.transformarA(TipoCuerpo.PLANETA_ROCOSO);
        sobreviviente.setTransformadoPorEvento(true);

        if (sobreviviente.getCuerpoOrbitado() == null && absorbido.getCuerpoOrbitado() != null) {
            sobreviviente.setCuerpoOrbitado(absorbido.getCuerpoOrbitado());
        }

        desactivarYEliminar(absorbido, aEliminar);

        emitirMensaje("Reacción hidro-magmática: Colisión entre Agua y Lava formó un nuevo Planeta Porro inerte.");
    }

    private void desactivarYEliminar(CuerpoCeleste victima, List<CuerpoCeleste> aEliminar) {
        victima.setSimulado(false);
        victima.setMasa(0);
        destruirCivilizacionSiExiste(victima);

        if (!aEliminar.contains(victima)) {
            aEliminar.add(victima);
        }
    }

    public void impactarMeteorito(Meteorito meteorito, CuerpoCeleste objetivo, List<CuerpoCeleste> aEliminar) {
        desactivarYEliminar(meteorito, aEliminar);

        // Si el objetivo es un planeta sin escudo
        if (objetivo.getTipoCuerpo() != null && objetivo.getTipoCuerpo().esPlaneta()) {
            double masaInicial = objetivo.getMasaInicial();
            double factorDano = ConfiguracionSimulacion.DANO_METEORITO_MIN_FRACCION
                    + (Math.random() * (ConfiguracionSimulacion.DANO_METEORITO_MAX_FRACCION - ConfiguracionSimulacion.DANO_METEORITO_MIN_FRACCION));
            double danoMasa = masaInicial * factorDano;
            double nuevaMasa = Math.max(0.0, objetivo.getMasa() - danoMasa);
            objetivo.setMasa(nuevaMasa);

            if (objetivo.tieneCivilizacion() && objetivo.getCivilizacion() != null) {
                objetivo.getCivilizacion().recalcularPoblacionMaxima();
                objetivo.getCivilizacion().recibirImpactoMeteorito();
            }

            // Si queda por debajo del umbral de explosión (30% masa inicial), o si es un planeta vulnerable del principio (<= 0.6x masaBase), explota
            boolean esPlanetaInicialFragil = masaInicial <= TipoCuerpo.PLANETA_ROCOSO.masaBase * 0.6 + 1e-6;
            if (nuevaMasa < masaInicial * ConfiguracionSimulacion.UMBRAL_EXPLOSION_MASA_FRACCION || esPlanetaInicialFragil) {
                desactivarYEliminar(objetivo, aEliminar);

                if (listenerExplosion != null) {
                    listenerExplosion.accept(objetivo.getPosicion(), objetivo.getRadio() * 2.5);
                }

                emitirEvento("¡CATASTROFE! El impacto de meteorito masivo destruyó completamente al planeta " + objetivo.getNombre() + ".", MensajeEvento.TipoMensaje.PELIGRO);
                return;
            }

            emitirMensaje(meteorito.getNombre() + " impactó en " + objetivo.getNombre() + " reduciendo su masa en " + String.format("%.1f%%", factorDano * 100) + ".");
            return;
        }

        // Conservar momento para otros cuerpos (lunas, etc.)
        if (objetivo.getMasa() > 0) {
            Vector2D momentoMeteorito = meteorito.getVelocidad().multiplicar(meteorito.getMasa());
            Vector2D nuevaVelocidad = objetivo.getVelocidad().sumar(momentoMeteorito.dividir(objetivo.getMasa()));
            objetivo.setVelocidad(nuevaVelocidad);
        }

        if (objetivo.tieneCivilizacion() && objetivo.getCivilizacion() != null) {
            objetivo.getCivilizacion().recibirImpactoMeteorito();
        }

        emitirMensaje(meteorito.getNombre() + " colisionó contra " + objetivo.getNombre() + ".");
    }

    public void generarEscombros(CuerpoCeleste destruido, List<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar) {
        if (destruido.getTipoCuerpo() == TipoCuerpo.METEORITO) {
            desactivarYEliminar(destruido, aEliminar);
            return;
        }

        double masaTotalFragmentos = destruido.getMasa() * (1.0 - ConfiguracionSimulacion.FRACCION_MASA_DISIPADA);
        desactivarYEliminar(destruido, aEliminar);

        if (masaTotalFragmentos <= 0) {
            return;
        }

        int numFragmentos = ConfiguracionSimulacion.FRAGMENTOS_POR_COLAPSO;
        if (aAgregar.size() >= ConfiguracionSimulacion.MAX_METEORITOS_SIMULTANEOS) {
            numFragmentos = 1;
        }

        double masaPorFragmento = masaTotalFragmentos / numFragmentos;
        for (int i = 0; i < numFragmentos; i++) {
            Meteorito fragmento = CuerpoCelesteFactory.crearFragmentoEscombro(
                    destruido.getPosicionX(), destruido.getPosicionY(),
                    destruido.getVelocidad(), masaPorFragmento
            );
            aAgregar.add(fragmento);
        }
    }

    public static void reboteElastico(CuerpoFisico a, CuerpoFisico b) {
        Vector2D normal = b.getPosicion().restar(a.getPosicion()).normalizar();
        Vector2D relativo = a.getVelocidad().restar(b.getVelocidad());
        double velocidadNormal = relativo.productoPunto(normal);

        if (velocidadNormal > 0) return;

        double e = 0.5;
        double masaTotalInv = (1.0 / Math.max(1e-9, a.getMasa())) + (1.0 / Math.max(1e-9, b.getMasa()));
        double j = -(1 + e) * velocidadNormal / masaTotalInv;

        Vector2D impulso = normal.multiplicar(j);
        a.setVelocidad(a.getVelocidad().restar(impulso.dividir(a.getMasa())));
        b.setVelocidad(b.getVelocidad().sumar(impulso.dividir(b.getMasa())));

        double overlap = (a.getRadioFisico() + b.getRadioFisico()) - a.getPosicion().distanciaA(b.getPosicion());
        if (overlap > 0) {
            Vector2D separacion = normal.multiplicar(overlap * 0.5);
            a.setPosicion(a.getPosicion().restar(separacion));
            b.setPosicion(b.getPosicion().sumar(separacion));
        }
    }

    public void reboteElastico(CuerpoCeleste a, CuerpoCeleste b) {
        reboteElastico((CuerpoFisico) a, (CuerpoFisico) b);
    }

    private void destruirCivilizacionSiExiste(CuerpoCeleste objetivo) {
        if (objetivo != null && objetivo.tieneCivilizacion() && objetivo.getCivilizacion() != null) {
            objetivo.getCivilizacion().destruir();
        }
    }

    private void emitirMensaje(String mensaje) {
        if (listenerMensaje != null) {
            listenerMensaje.accept(mensaje);
        }
    }

    private void emitirEvento(String mensaje, MensajeEvento.TipoMensaje tipo) {
        emitirMensaje(mensaje);
        if (listenerNotificacion != null) {
            listenerNotificacion.accept(mensaje, tipo);
        }
    }
}
