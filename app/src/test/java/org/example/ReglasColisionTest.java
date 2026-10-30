package org.example;

import org.example.game.cuerpo.*;
import org.example.game.motor.Vector2D;
import org.example.game.motor.colision.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ReglasColisionTest {

    @Test
    @DisplayName("ReglaColisionAgujeroNegro devora cuerpos celestes")
    void testReglaAgujeroNegroDevora() {
        ReglaColisionAgujeroNegro regla = new ReglaColisionAgujeroNegro();
        AgujeroNegro bh = new AgujeroNegro("BH", 1e28, 0, 0);
        Planeta planeta = new Planeta("Tierra", 1e24, 0, 0);

        assertTrue(regla.aplica(bh, planeta));
        Set<CuerpoCeleste> aEliminar = new HashSet<>();
        List<CuerpoCeleste> aAgregar = new ArrayList<>();
        ContextoColision ctx = new ContextoColision(aEliminar, aAgregar, null, null, null);

        regla.resolver(bh, planeta, ctx);
        assertTrue(aEliminar.contains(planeta));
        assertTrue(bh.getMasa() > 1e28);
    }

    @Test
    @DisplayName("ReglaColisionEstrella desintegra meteoritos")
    void testReglaEstrellaDesintegraMeteorito() {
        ReglaColisionEstrella regla = new ReglaColisionEstrella();
        Estrella sol = new Estrella("Sol", 1e27, 0, 0);
        Meteorito meteorito = new Meteorito("Meteor", 1e18, 0, 0, Vector2D.cero(), 500, false);

        assertTrue(regla.aplica(sol, meteorito));
        Set<CuerpoCeleste> aEliminar = new HashSet<>();
        List<CuerpoCeleste> aAgregar = new ArrayList<>();
        ContextoColision ctx = new ContextoColision(aEliminar, aAgregar, null, null, null);

        regla.resolver(sol, meteorito, ctx);
        assertTrue(aEliminar.contains(meteorito));
    }

    @Test
    @DisplayName("ReglaColisionMeteorito destruye mutuamente dos meteoritos")
    void testReglaMeteoritosDestruccionMutua() {
        ReglaColisionMeteorito regla = new ReglaColisionMeteorito();
        Meteorito m1 = new Meteorito("M1", 1e18, 0, 0, Vector2D.cero(), 500, false);
        Meteorito m2 = new Meteorito("M2", 1e18, 0, 0, Vector2D.cero(), 500, false);

        assertTrue(regla.aplica(m1, m2));
        Set<CuerpoCeleste> aEliminar = new HashSet<>();
        List<CuerpoCeleste> aAgregar = new ArrayList<>();
        ContextoColision ctx = new ContextoColision(aEliminar, aAgregar, null, null, null);

        regla.resolver(m1, m2, ctx);
        assertTrue(aEliminar.contains(m1));
        assertTrue(aEliminar.contains(m2));
    }

    @Test
    @DisplayName("ReglaColisionEscudo absorbe impacto de meteorito")
    void testReglaEscudoAbsorbe() {
        ReglaColisionEscudo regla = new ReglaColisionEscudo();
        Planeta planeta = new Planeta("Defendido", 1e24, 0, 0);
        planeta.activarEscudo(5);
        Meteorito m = new Meteorito("Meteor", 1e18, 0, 0, Vector2D.cero(), 500, false);

        assertTrue(regla.aplica(planeta, m));
        Set<CuerpoCeleste> aEliminar = new HashSet<>();
        List<CuerpoCeleste> aAgregar = new ArrayList<>();
        ContextoColision ctx = new ContextoColision(aEliminar, aAgregar, null, null, null);

        regla.resolver(planeta, m, ctx);
        assertTrue(aEliminar.contains(m));
        assertEquals(4, planeta.getGolpesEscudoRestantes());
    }

    @Test
    @DisplayName("ReglaColisionFusionAguaLava fusiona en planeta rocoso")
    void testReglaFusionAguaLava() {
        ReglaColisionFusionAguaLava regla = new ReglaColisionFusionAguaLava();
        Planeta agua = new Planeta("Agua", 1e24, 0, 0, TipoCuerpo.PLANETA_AGUA);
        Planeta lava = new Planeta("Lava", 1e24, 0, 0, TipoCuerpo.PLANETA_LAVA);

        assertTrue(regla.aplica(agua, lava));
        Set<CuerpoCeleste> aEliminar = new HashSet<>();
        List<CuerpoCeleste> aAgregar = new ArrayList<>();
        ContextoColision ctx = new ContextoColision(aEliminar, aAgregar, null, null, null);

        regla.resolver(agua, lava, ctx);
        assertTrue(aEliminar.contains(lava));
        assertEquals(TipoCuerpo.PLANETA_ROCOSO, agua.getTipoCuerpo());
        assertEquals(2e24, agua.getMasa(), 1e20);
    }

    @Test
    @DisplayName("ReglaColisionAbsorcionPorRatio absorbe con diferencia extrema de masa")
    void testReglaAbsorcionExtrema() {
        ReglaColisionAbsorcionPorRatio regla = new ReglaColisionAbsorcionPorRatio();
        Planeta grande = new Planeta("Grande", 1e26, 0, 0);
        Planeta chico = new Planeta("Chico", 1e24, 0, 0);

        assertTrue(regla.aplica(grande, chico));
        Set<CuerpoCeleste> aEliminar = new HashSet<>();
        List<CuerpoCeleste> aAgregar = new ArrayList<>();
        ContextoColision ctx = new ContextoColision(aEliminar, aAgregar, null, null, null);

        regla.resolver(grande, chico, ctx);
        assertTrue(aEliminar.contains(chico));
        assertEquals(1e26 + 1e24, grande.getMasa(), 1e20);
    }

    @Test
    @DisplayName("ReglaColisionColapsoMutuo genera escombros para masas similares")
    void testReglaColapsoMutuo() {
        ReglaColisionColapsoMutuo regla = new ReglaColisionColapsoMutuo();
        Planeta p1 = new Planeta("P1", 1e24, 0, 0);
        Planeta p2 = new Planeta("P2", 1.1e24, 0, 0);

        assertTrue(regla.aplica(p1, p2));
        Set<CuerpoCeleste> aEliminar = new HashSet<>();
        List<CuerpoCeleste> aAgregar = new ArrayList<>();
        ContextoColision ctx = new ContextoColision(aEliminar, aAgregar, null, null, null);

        regla.resolver(p1, p2, ctx);
        assertTrue(aEliminar.contains(p1));
        assertTrue(aEliminar.contains(p2));
        assertFalse(aAgregar.isEmpty());
    }

    @Test
    @DisplayName("ReglaColisionAsimetrica gana 30% de masa")
    void testReglaAsimetrica() {
        ReglaColisionAsimetrica regla = new ReglaColisionAsimetrica();
        Planeta mayor = new Planeta("Mayor", 3e24, 0, 0);
        Planeta menor = new Planeta("Menor", 1e24, 0, 0);

        assertTrue(regla.aplica(mayor, menor));
        Set<CuerpoCeleste> aEliminar = new HashSet<>();
        List<CuerpoCeleste> aAgregar = new ArrayList<>();
        ContextoColision ctx = new ContextoColision(aEliminar, aAgregar, null, null, null);

        regla.resolver(mayor, menor, ctx);
        assertTrue(aEliminar.contains(menor));
        assertEquals(3e24 + 0.3 * 1e24, mayor.getMasa(), 1e20);
    }
}
