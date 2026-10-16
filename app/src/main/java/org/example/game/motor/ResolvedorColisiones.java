package org.example.game.motor;

import org.example.CuerpoCeleste;
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.cuerpo.EscudoProtector;
import org.example.game.cuerpo.Meteorito;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.simulacion.ConfiguracionSimulacion;

import java.util.List;
import java.util.function.Consumer;

/**
 * Resolvedor desacoplado de colisiones e interacciones físicas y destructivas entre cuerpos celestes.
 *
 * Principios SOLID:
 * - SRP: Responsable exclusivo de la resolución cinemática y destructiva de colisiones
 *   (absorciones, impactos de meteoritos, defensas de escudo, escombros y rebotes).
 * - OCP: Permite extender la respuesta ante colisiones y configurar listeners de eventos.
 * - LSP: Utiliza las propiedades y métodos polimórficos de CuerpoCeleste sin downcasting frágil.
 * - Reutilización: Compartido entre MotorFisicaPermisiva y MotorFisicaNewtoniana.
 */
public class ResolvedorColisiones {

    private Consumer<String> listenerMensaje;
    private Runnable listenerDefensaEscudo;

    public void setListenerMensaje(Consumer<String> listener) {
        this.listenerMensaje = listener;
    }

    public void setListenerDefensaEscudo(Runnable listener) {
        this.listenerDefensaEscudo = listener;
    }

    /**
     * Resuelve la interacción de defensa si uno de los cuerpos es un EscudoProtector y el otro un Meteorito.
     * Retorna true si la interacción fue interceptada por el escudo.
     */
    public boolean procesarInteraccionEscudo(CuerpoCeleste a, CuerpoCeleste b, List<CuerpoCeleste> aEliminar) {
        if (a.getTipoCuerpo() == TipoCuerpo.ESCUDO_DOME && b.getTipoCuerpo() == TipoCuerpo.METEORITO) {
            return interactuarEscudoMeteorito((EscudoProtector) a, (Meteorito) b, aEliminar);
        }
        if (b.getTipoCuerpo() == TipoCuerpo.ESCUDO_DOME && a.getTipoCuerpo() == TipoCuerpo.METEORITO) {
            return interactuarEscudoMeteorito((EscudoProtector) b, (Meteorito) a, aEliminar);
        }
        return false;
    }

    private boolean interactuarEscudoMeteorito(EscudoProtector escudo, Meteorito meteorito, List<CuerpoCeleste> aEliminar) {
        double dist = escudo.getPosicion().distanciaA(meteorito.getPosicion());
        if (dist <= escudo.getRadioEscudo() && escudo.puedeAbsorberImpacto()) {
            escudo.absorberDano(25.0);
            aEliminar.add(meteorito);
            if (listenerDefensaEscudo != null) {
                try { listenerDefensaEscudo.run(); } catch (Exception ignored) {}
            }
            emitirMensaje("Escudo defensivo desvio y vaporizo meteorito.");
            return true;
        }
        return false;
    }

    /**
     * Resuelve colisión completa entre dos cuerpos celestes.
     */
    public void resolverColision(CuerpoCeleste a, CuerpoCeleste b, List<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar) {
        TipoCuerpo tipoA = a.getTipoCuerpo();
        TipoCuerpo tipoB = b.getTipoCuerpo();

        // 1. Agujero negro devora todo
        boolean aEsBH = (tipoA == TipoCuerpo.AGUJERO_NEGRO || tipoA == TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO);
        boolean bEsBH = (tipoB == TipoCuerpo.AGUJERO_NEGRO || tipoB == TipoCuerpo.AGUJERO_NEGRO_SUPERMASIVO);

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

        // 2. Estrella devora meteoritos o satélites en su corona
        if (tipoA == TipoCuerpo.ESTRELLA && (tipoB == TipoCuerpo.METEORITO || tipoB == TipoCuerpo.SATELITE)) {
            aEliminar.add(b);
            emitirMensaje(a.getNombre() + " desintegro a " + b.getNombre() + " en su corona.");
            return;
        }
        if (tipoB == TipoCuerpo.ESTRELLA && (tipoA == TipoCuerpo.METEORITO || tipoA == TipoCuerpo.SATELITE)) {
            aEliminar.add(a);
            emitirMensaje(b.getNombre() + " desintegro a " + a.getNombre() + " en su corona.");
            return;
        }

        // 3. Dos meteoritos: destrucción mutua directa sin fragmentación infinita
        if (tipoA == TipoCuerpo.METEORITO && tipoB == TipoCuerpo.METEORITO) {
            aEliminar.add(a);
            aEliminar.add(b);
            emitirMensaje("Destruccion mutua de meteoritos por impacto directo.");
            return;
        }

        // 4. Meteorito impacta un cuerpo no-meteorito
        if (tipoA == TipoCuerpo.METEORITO && tipoB != TipoCuerpo.METEORITO) {
            impactarMeteorito((Meteorito) a, b, aEliminar);
            return;
        }
        if (tipoB == TipoCuerpo.METEORITO && tipoA != TipoCuerpo.METEORITO) {
            impactarMeteorito((Meteorito) b, a, aEliminar);
            return;
        }

        // Escudo protector no colisiona de forma destructiva con planetas o estrellas normales
        if (tipoA == TipoCuerpo.ESCUDO_DOME || tipoB == TipoCuerpo.ESCUDO_DOME) {
            return;
        }

        // Easter Egg: colisión hidro-magmática entre planeta de agua y planeta de lava
        if (esColisionAguaYLava(tipoA, tipoB)) {
            fusionarPlanetasAguaYLava(a, b, aEliminar);
            return;
        }

        // 5. Colisión entre cuerpos astronómicos no-meteoritos
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
        generarEscombros(menor, aEliminar, aAgregar);
        destruirCivilizacionSiExiste(menor);
        double masaGanada = menor.getMasa() * ConfiguracionSimulacion.PORCENTAJE_GANANCIA_MASA_COLISION;
        mayor.setMasa(mayor.getMasa() + masaGanada);

        if (mayor.tieneCivilizacion() && mayor.getCivilizacion() != null) {
            mayor.getCivilizacion().recibirImpactoMeteorito();
        }

        emitirMensaje("Impacto destructivo asimetrico: " + menor.getNombre() + " destruido al colisionar con " + mayor.getNombre() + ". Masa absorbida: " + String.format("%.0f%%", ConfiguracionSimulacion.PORCENTAJE_GANANCIA_MASA_COLISION * 100));
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
        if (!aEliminar.contains(meteorito)) {
            aEliminar.add(meteorito);
        }

        // Conservar momento: v' = v_obj + (m_met * v_met) / m_obj
        if (objetivo.getMasa() > 0) {
            Vector2D momentoMeteorito = meteorito.getVelocidad().multiplicar(meteorito.getMasa());
            Vector2D nuevaVelocidad = objetivo.getVelocidad().sumar(momentoMeteorito.dividir(objetivo.getMasa()));
            objetivo.setVelocidad(nuevaVelocidad);
        }

        // Daño a civilización si el cuerpo la alberga
        if (objetivo.tieneCivilizacion() && objetivo.getCivilizacion() != null) {
            objetivo.getCivilizacion().recibirImpactoMeteorito();
        }

        emitirMensaje(meteorito.getNombre() + " colisiono contra " + objetivo.getNombre() + ".");
    }

    public void generarEscombros(CuerpoCeleste destruido, List<CuerpoCeleste> aEliminar, List<CuerpoCeleste> aAgregar) {
        if (!aEliminar.contains(destruido)) {
            aEliminar.add(destruido);
        }
        destruirCivilizacionSiExiste(destruido);

        if (destruido.getTipoCuerpo() == TipoCuerpo.METEORITO) {
            return;
        }

        double masaTotalFragmentos = destruido.getMasa() * (1.0 - ConfiguracionSimulacion.FRACCION_MASA_DISIPADA);
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
}
