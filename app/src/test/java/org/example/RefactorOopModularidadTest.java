package org.example;

import org.example.game.cuerpo.*;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.TipoRecurso;
import org.example.game.motor.*;
import org.example.game.render.UtilidadRender;
import org.example.game.simulacion.*;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(JavaFXExtension.class)
public class RefactorOopModularidadTest {

    @Test
    @DisplayName("GestorCicloVidaCuerpos gestiona conteo, spawn y cascada de satelites (SRP / LSP)")
    void testGestorCicloVidaCuerpos() {
        SistemaSolar sistemaSolar = new SistemaSolar(new MotorFisicaPermisiva(1000));
        ValidadorColocacionCuerpos validador = new ValidadorColocacionCuerpos();
        InventarioJugador inventario = new InventarioJugador();
        inventario.reiniciar();
        inventario.agregarRecurso(TipoRecurso.MINERALES, 10000);
        inventario.agregarRecurso(TipoRecurso.ENERGIA, 10000);

        BusEventos busEventos = new BusEventos();
        GestorCicloVidaCuerpos gestor = new GestorCicloVidaCuerpos(
                sistemaSolar, validador, null, inventario, busEventos
        );

        assertEquals(0, gestor.getCantidadPlanetasActuales());

        Planeta planeta = new Planeta("Tierra", 5.97e24, 0, 0);
        gestor.agregarCuerpo(planeta);

        assertEquals(1, gestor.getCantidadPlanetasActuales());

        Satelite satelite = new Satelite("Sat1", 1000, 50, 0);
        satelite.setCuerpoOrbitado(planeta);
        gestor.agregarCuerpo(satelite);

        assertEquals(2, sistemaSolar.getCuerpos().size());

        // Al eliminar el planeta anfitrion, el satelite debe eliminarse en cascada
        gestor.eliminarCuerpo(planeta);

        assertEquals(0, gestor.getCantidadPlanetasActuales());
        assertFalse(sistemaSolar.getCuerpos().contains(planeta));
        assertFalse(sistemaSolar.getCuerpos().contains(satelite));
    }

    @Test
    @DisplayName("GestorColocacionCuerpos enlaza auto-orbita al detectar cuerpo masivo cercano (SRP / DIP)")
    void testGestorColocacionCuerpos() {
        SimulacionSolar simulacion = new SimulacionSolar(2000, 2000);
        simulacion.getInventario().reiniciar();
        simulacion.getInventario().agregarRecurso(TipoRecurso.MINERALES, 50000);
        simulacion.getInventario().agregarRecurso(TipoRecurso.ENERGIA, 50000);
        simulacion.getProgresion().desbloquearTodo();

        Estrella sol = new Estrella("Sol", 1.989e30, 0, 0);
        simulacion.agregarCuerpo(sol);

        simulacion.entrarModoColocacion(TipoCuerpo.PLANETA_ROCOSO, 1.0, "NuevoPlaneta");
        simulacion.actualizarPosicionPreview(1000 + 150, 1000); // 150 px a la derecha

        GestorColocacionCuerpos colocacion = simulacion.getGestorColocacion();
        Optional<CuerpoCeleste> colocado = colocacion.confirmarColocacionAutoOrbita(simulacion);

        assertTrue(colocado.isPresent(), "El planeta debe ser colocado");
        CuerpoCeleste planeta = colocado.get();
        assertEquals(sol, planeta.getCuerpoOrbitado(), "El nuevo planeta debe quedar orbitando el sol automaticamente");
    }

    @Test
    @DisplayName("CoordinadorPasoSimulacion avanza civilizaciones y fisica ordenadamente (SRP)")
    void testCoordinadorPasoSimulacion() {
        SimulacionSolar simulacion = new SimulacionSolar(2000, 2000);
        Planeta planeta = new Planeta("Colonia", 5.97e24, 100, 100);
        simulacion.agregarCuerpo(planeta);

        CoordinadorPasoSimulacion coordinador = simulacion.getCoordinadorPaso();
        assertNotNull(coordinador);

        AtomicBoolean notificado = new AtomicBoolean(false);
        assertDoesNotThrow(() -> coordinador.actualizarCivilizaciones(msg -> notificado.set(true)));
    }

    @Test
    @DisplayName("Clases utilitarias convertidas a objetos inyectables funcionan mediante instancia y delegates (SRP / OCP)")
    void testClasesUtilitariasInyectables() {
        CalculadorOrbital calcOrb = new CalculadorOrbital();
        double vOrb = calcOrb.rapidezOrbital(1.989e30, 100.0);
        assertTrue(vOrb > 0, "Rapidez orbital calculada debe ser positiva");

        EscaladorAmenazaMeteorito escalador = new EscaladorAmenazaMeteorito();
        double dano = escalador.fraccionDano(0.5);
        assertTrue(dano >= 0 && dano <= 1.0, "Dano de meteorito debe estar en [0, 1]");

        BuscadorOrbitaLibre buscador = new BuscadorOrbitaLibre();
        Estrella sol = new Estrella("Sol", 1.989e30, 0, 0);
        Planeta planeta = new Planeta("Tierra", 5.97e24, 100, 0);
        boolean libre = buscador.esPosicionLibre(new Vector2D(500, 500), planeta, List.of(sol, planeta));
        assertTrue(libre, "Posicion lejana debe estar libre");

        UtilidadRender utilRender = new UtilidadRender();
        assertNotNull(utilRender.obtenerColorWeb("#FFFFFF"));
        assertNotNull(utilRender.obtenerColorRgb(255, 0, 0));

        FormateadorMasa formMasa = new FormateadorMasa();
        assertEquals("5.97 MT", formMasa.formatear(5.97e24));

        CatalogoDescriptoresCuerpo catDesc = new CatalogoDescriptoresCuerpo();
        DescriptorTipoCuerpo desc = catDesc.descriptor(TipoCuerpo.PLANETA_ROCOSO);
        assertNotNull(desc);
        assertEquals("Planeta Porro", desc.nombrePorDefecto());
    }

    @Test
    @DisplayName("Polimorfismo sin instanceof: capacidades de CuerpoCeleste responden correctamente")
    void testCapacidadesPolimorficasSinInstanceOf() {
        CuerpoCeleste planeta = new Planeta("Planeta1", 100, 0, 0);
        CuerpoCeleste meteorito = new Meteorito("Met1", 50, 10, 10, Vector2D.cero(), 100.0, false);
        CuerpoCeleste agujeroNegro = new AgujeroNegro("BH1", 1000, 0, 0);
        CuerpoCeleste satelite = new Satelite("Sat1", 10, 5, 5);
        CuerpoCeleste estrella = new Estrella("Sol1", 5000, 0, 0);

        assertTrue(planeta.esPlaneta());
        assertFalse(planeta.esMeteorito());
        assertTrue(planeta.comoPlaneta().isPresent());
        assertTrue(planeta.comoCivilizable().isPresent());
        assertTrue(planeta.comoDefensor().isPresent());
        assertTrue(planeta.comoTransformable().isPresent());
        assertTrue(planeta.comoColonizable().isPresent());
        assertFalse(planeta.comoAgujeroNegro().isPresent());

        assertTrue(meteorito.esMeteorito());
        assertTrue(meteorito.comoExpirable().isPresent());
        assertTrue(meteorito.comoActualizable().isPresent());
        assertFalse(meteorito.comoPlaneta().isPresent());

        assertTrue(agujeroNegro.esAgujeroNegro());
        assertTrue(agujeroNegro.comoAgujeroNegro().isPresent());
        assertFalse(agujeroNegro.comoCivilizable().isPresent());

        assertTrue(satelite.esSatelite());
        assertTrue(satelite.esCuerpoMenor());
        assertTrue(satelite.comoActualizable().isPresent());

        assertTrue(estrella.esEstrella());
        assertTrue(estrella.esAtractorMasivo());
        assertTrue(estrella.esAtractorOrbital());
    }
}
