package org.example.game.motor.colision;

import org.example.game.cuerpo.Civilizable;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.Meteorito;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.Vector2D;
import org.example.game.simulacion.ConfiguracionColisiones;
import org.example.game.simulacion.MensajeEvento;

/**
 * Regla de colisión para impactos balísticos de meteoritos y coexistencia de satélites.
 */
public class ReglaColisionMeteorito implements ReglaColision {

    @Override
    public boolean aplica(CuerpoCeleste a, CuerpoCeleste b) {
        TipoCuerpo ta = a.getTipoCuerpo();
        TipoCuerpo tb = b.getTipoCuerpo();
        if (ta == TipoCuerpo.SATELITE && tb == TipoCuerpo.SATELITE) return true;
        return (ta == TipoCuerpo.METEORITO || tb == TipoCuerpo.METEORITO);
    }

    @Override
    public void resolver(CuerpoCeleste a, CuerpoCeleste b, ContextoColision ctx) {
        TipoCuerpo ta = a.getTipoCuerpo();
        TipoCuerpo tb = b.getTipoCuerpo();

        if (ta == TipoCuerpo.SATELITE && tb == TipoCuerpo.SATELITE) {
            return;
        }

        if (ta == TipoCuerpo.METEORITO && tb == TipoCuerpo.METEORITO) {
            UtilidadColision.desactivarYEliminar(a, ctx.aEliminar());
            UtilidadColision.desactivarYEliminar(b, ctx.aEliminar());
            ctx.emitirMensaje("Destrucción mutua de meteoritos por impacto directo.");
            return;
        }

        Meteorito meteorito = (ta == TipoCuerpo.METEORITO) ? (Meteorito) a : (Meteorito) b;
        CuerpoCeleste objetivo = (meteorito == a) ? b : a;

        UtilidadColision.desactivarYEliminar(meteorito, ctx.aEliminar());

        if (objetivo.getTipoCuerpo() != null && objetivo.getTipoCuerpo().esPlaneta()) {
            double masaInicial = objetivo.getMasaInicial();
            double factorDano = ConfiguracionColisiones.DANO_METEORITO_MIN_FRACCION
                    + (Math.random() * (ConfiguracionColisiones.DANO_METEORITO_MAX_FRACCION - ConfiguracionColisiones.DANO_METEORITO_MIN_FRACCION));
            double danoMasa = masaInicial * factorDano;
            double nuevaMasa = Math.max(0.0, objetivo.getMasa() - danoMasa);
            objetivo.setMasa(nuevaMasa);

            if (objetivo instanceof Civilizable civ && civ.tieneCivilizacion() && civ.getCivilizacion() != null) {
                civ.getCivilizacion().recalcularPoblacionMaxima();
                civ.getCivilizacion().recibirImpactoMeteorito();
            }

            boolean esPlanetaInicialFragil = masaInicial <= TipoCuerpo.PLANETA_ROCOSO.masaBase * 0.6 + 1e-6;
            if (nuevaMasa < masaInicial * ConfiguracionColisiones.UMBRAL_EXPLOSION_MASA_FRACCION || esPlanetaInicialFragil) {
                UtilidadColision.desactivarYEliminar(objetivo, ctx.aEliminar());
                ctx.emitirExplosion(objetivo.getPosicion(), objetivo.getRadio() * 2.5);
                ctx.emitirEvento("¡CATASTROFE! El impacto de meteorito masivo destruyó completamente al planeta " + objetivo.getNombre() + ".", MensajeEvento.TipoMensaje.PELIGRO);
                return;
            }

            ctx.emitirMensaje(meteorito.getNombre() + " impactó en " + objetivo.getNombre() + " reduciendo su masa en " + String.format("%.1f%%", factorDano * 100) + ".");
            return;
        }

        if (objetivo.getMasa() > 0) {
            Vector2D momentoMeteorito = meteorito.getVelocidad().multiplicar(meteorito.getMasa());
            Vector2D nuevaVelocidad = objetivo.getVelocidad().sumar(momentoMeteorito.dividir(objetivo.getMasa()));
            objetivo.setVelocidad(nuevaVelocidad);
        }

        if (objetivo instanceof Civilizable civ && civ.tieneCivilizacion() && civ.getCivilizacion() != null) {
            civ.getCivilizacion().recibirImpactoMeteorito();
        }

        ctx.emitirMensaje(meteorito.getNombre() + " colisionó contra " + objetivo.getNombre() + ".");
    }
}
