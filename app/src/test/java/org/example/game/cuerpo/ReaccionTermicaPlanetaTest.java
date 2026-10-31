package org.example.game.cuerpo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReaccionTermicaPlanetaTest {

    @Test
    void interactuanAguaYLavaExclusivamente() {
        ReaccionTermicaPlaneta agua = TipoCuerpo.PLANETA_AGUA.getReaccionTermica();
        ReaccionTermicaPlaneta lava = TipoCuerpo.PLANETA_LAVA.getReaccionTermica();
        ReaccionTermicaPlaneta inerte = TipoCuerpo.PLANETA_ROCOSO.getReaccionTermica();

        assertTrue(agua.interactuar(lava));
        assertTrue(lava.interactuar(agua));

        assertFalse(agua.interactuar(agua));
        assertFalse(lava.interactuar(lava));
        assertFalse(inerte.interactuar(agua));
        assertFalse(inerte.interactuar(lava));
        assertFalse(agua.interactuar(inerte));
    }
}
