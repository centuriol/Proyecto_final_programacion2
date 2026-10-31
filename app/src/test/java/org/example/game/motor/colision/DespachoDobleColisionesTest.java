package org.example.game.motor.colision;

import org.example.game.cuerpo.AgujeroNegro;
import org.example.game.cuerpo.CuerpoCeleste;
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.cuerpo.Estrella;
import org.example.game.cuerpo.Meteorito;
import org.example.game.cuerpo.Planeta;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.Vector2D;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DespachoDobleColisionesTest {

    @Test
    @DisplayName("ReglaColisionFusionAguaLava: doble despacho polimorfico entre Planeta Agua y Lava")
    void testFusionAguaLavaDobleDespacho() {
        ReglaColisionFusionAguaLava regla = new ReglaColisionFusionAguaLava();
        Planeta agua = CuerpoCelesteFactory.crearPlanetaAgua(0, 0, 1.0, Vector2D.cero());
        Planeta lava = CuerpoCelesteFactory.crearPlanetaLava(10, 0, 1.0, Vector2D.cero());
        Planeta rocoso = CuerpoCelesteFactory.crearPlanetaRocoso(20, 0, 1.0, Vector2D.cero());

        assertTrue(regla.aplica(agua, lava));
        assertTrue(regla.aplica(lava, agua));
        assertFalse(regla.aplica(agua, rocoso));

        Set<CuerpoCeleste> aEliminar = new HashSet<>();
        List<CuerpoCeleste> aAgregar = new ArrayList<>();
        ContextoColision ctx = new ContextoColision(aEliminar, aAgregar, null, null, null);

        regla.resolver(agua, lava, ctx);
        assertTrue(aEliminar.contains(lava));
        assertEquals("Planeta Porro", agua.getNombre());
    }

    @Test
    @DisplayName("ReglaColisionEstrella: doble despacho con meteorito en corona solar")
    void testEstrellaDesintegraMeteoritoDobleDespacho() {
        ReglaColisionEstrella regla = new ReglaColisionEstrella();
        Estrella sol = CuerpoCelesteFactory.crearEstrella(0, 0, 1.0);
        Meteorito met = CuerpoCelesteFactory.crearMeteoritoJugadorFisica(10, 0, Vector2D.cero());

        assertTrue(regla.aplica(sol, met));
        assertTrue(regla.aplica(met, sol));

        Set<CuerpoCeleste> aEliminar = new HashSet<>();
        ContextoColision ctx = new ContextoColision(aEliminar, new ArrayList<>(), null, null, null);

        regla.resolver(sol, met, ctx);
        assertTrue(aEliminar.contains(met));
    }

    @Test
    @DisplayName("ReglaColisionAgujeroNegro: doble despacho con absorcion de planeta y colapso binario")
    void testAgujeroNegroDobleDespacho() {
        ReglaColisionAgujeroNegro regla = new ReglaColisionAgujeroNegro();
        Estrella sol1 = CuerpoCelesteFactory.crearEstrella(-50, 0, 1.0);
        Estrella sol2 = CuerpoCelesteFactory.crearEstrella(50, 0, 1.0);
        AgujeroNegro bh = CuerpoCelesteFactory.crearAgujeroNegroPersonalizado(0, 0, 1e31, Vector2D.cero());
        Planeta planeta = CuerpoCelesteFactory.crearPlanetaRocoso(10, 0, 1.0, Vector2D.cero());

        assertTrue(regla.aplica(sol1, sol2));
        assertTrue(regla.aplica(bh, planeta));

        Set<CuerpoCeleste> aEliminar = new HashSet<>();
        List<CuerpoCeleste> aAgregar = new ArrayList<>();
        ContextoColision ctx = new ContextoColision(aEliminar, aAgregar, null, null, null);

        regla.resolver(sol1, sol2, ctx);
        assertEquals(2, aEliminar.size());
        assertEquals(1, aAgregar.size());
        assertTrue(aAgregar.get(0) instanceof AgujeroNegro);
    }
}
