package org.example;
import org.example.game.cuerpo.Estrella;
import org.example.game.cuerpo.Planeta;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.ConstantesFisicas;
import org.example.game.render.CalculadorIndicadoresSol;
import org.example.game.render.CamaraZoom;
import org.example.game.render.DesplazamientoCamara;
import org.example.game.render.IndicadorSolBorde;
import org.example.game.ui.CapaIndicadoresSol;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
class LimitesCamaraEIndicadoresSolTest {

    @Test
    void testCamaraLimitaDesplazamientoAlAreaDelMundo() {
        CamaraZoom camara = new CamaraZoom();
        double limite = ConstantesFisicas.RADIO_MUNDO_DEFAULT;
        assertEquals(limite, camara.getDesplazamiento().getLimiteMundo(), 1e-6);

        camara.desplazarPorPantalla(80_000.0, -95_000.0);
        assertEquals(limite, camara.getDesplazamiento().getX(), 1e-6);
        assertEquals(-limite, camara.getDesplazamiento().getY(), 1e-6);

        camara.getDesplazamiento().setPosicion(-50_000.0, 42_000.0);
        assertEquals(-limite, camara.getDesplazamiento().getX(), 1e-6);
        assertEquals(limite, camara.getDesplazamiento().getY(), 1e-6);

        camara.getDesplazamiento().setLimiteMundo(3500.0);
        assertEquals(-3500.0, camara.getDesplazamiento().getX(), 1e-6);
        assertEquals(3500.0, camara.getDesplazamiento().getY(), 1e-6);
    }

    @Test
    void testSolVisibleEnPantallaNoGeneraIndicadorDeBorde() {
        CamaraZoom camara = new CamaraZoom();
        CalculadorIndicadoresSol calculador = new CalculadorIndicadoresSol();

        Estrella solCentral = new Estrella("Sol Central", TipoCuerpo.ESTRELLA.masaBase, 0.0, 0.0);
        Planeta planetaLejano = new Planeta("Planeta Lejano", TipoCuerpo.PLANETA_ROCOSO.masaBase, 4500.0, 0.0, TipoCuerpo.PLANETA_ROCOSO);

        List<IndicadorSolBorde> indicadores = calculador.calcularIndicadores(
                List.of(solCentral, planetaLejano),
                camara,
                1280.0,
                720.0
        );
        assertTrue(indicadores.isEmpty(), "Un sol en pantalla ni planetas fuera de pantalla deben generar indicador");
    }

    @Test
    void testIndicadoresSenalanCadaSolFueraDePantallaEnDireccionesCorrectas() {
        CamaraZoom camara = new CamaraZoom();
        CalculadorIndicadoresSol calculador = new CalculadorIndicadoresSol();

        Estrella solVisible = new Estrella("Sol Alfa", TipoCuerpo.ESTRELLA.masaBase, 0.0, 0.0);
        Estrella solDerecha = new Estrella("Sol Beta", TipoCuerpo.ESTRELLA.masaBase, 3200.0, 0.0);
        Estrella solArribaIzq = new Estrella("Sol Gamma", TipoCuerpo.ESTRELLA.masaBase, -2800.0, 2400.0);

        List<IndicadorSolBorde> indicadores = calculador.calcularIndicadores(
                List.of(solVisible, solDerecha, solArribaIzq),
                camara,
                1280.0,
                720.0
        );

        assertEquals(2, indicadores.size(), "Debe generarse un indicador por cada sol fuera de pantalla");

        IndicadorSolBorde indDerecha = indicadores.get(0);
        assertSame(solDerecha, indDerecha.sol());
        assertEquals(1280.0 - CalculadorIndicadoresSol.MARGEN_BORDE_POR_DEFECTO, indDerecha.pantallaX(), 1e-4);
        assertEquals(360.0, indDerecha.pantallaY(), 1e-4);
        assertEquals(0.0, indDerecha.anguloGrados(), 1e-4);

        IndicadorSolBorde indArribaIzq = indicadores.get(1);
        assertSame(solArribaIzq, indArribaIzq.sol());
        assertTrue(indArribaIzq.pantallaX() < 640.0);
        assertTrue(indArribaIzq.pantallaY() < 360.0);
        assertTrue(indArribaIzq.anguloGrados() < -90.0 && indArribaIzq.anguloGrados() > -180.0);
    }

    @Test
    void testClicEnIndicadorDeBordeViajaHastaElSolCorrespondiente() {
        CamaraZoom camara = new CamaraZoom();
        camara.setNivelZoom(1.3);
        CapaIndicadoresSol capa = new CapaIndicadoresSol(camara);

        Estrella solOriente = new Estrella("Sol Oriente", TipoCuerpo.ESTRELLA.masaBase, 2600.0, -1400.0);
        Estrella solOccidente = new Estrella("Sol Occidente", TipoCuerpo.ESTRELLA.masaBase, -3100.0, 1100.0);

        capa.actualizar(List.of(solOriente, solOccidente), 1280.0, 720.0);
        assertEquals(2, capa.getBotonesIndicadores().size());
        assertEquals("Sol Oriente", capa.getBotonesIndicadores().get(0).getText());

        capa.getBotonesIndicadores().get(0).fire();

        assertEquals(640.0, camara.mundoAPantallaX(solOriente.getPosicion().x(), 1280.0), 1e-6);
        assertEquals(360.0, camara.mundoAPantallaY(solOriente.getPosicion().y(), 720.0), 1e-6);
        assertEquals(1, capa.getIndicadoresActivos().size(), "Sol Oriente ya esta centrado; solo queda Sol Occidente fuera");
        assertSame(solOccidente, capa.getIndicadoresActivos().get(0).sol());

        capa.getBotonesIndicadores().get(0).fire();
        assertEquals(640.0, camara.mundoAPantallaX(solOccidente.getPosicion().x(), 1280.0), 1e-6);
        assertEquals(360.0, camara.mundoAPantallaY(solOccidente.getPosicion().y(), 720.0), 1e-6);
    }

    @Test
    void testSolNoSimuladoNoGeneraIndicador() {
        CamaraZoom camara = new CamaraZoom();
        CalculadorIndicadoresSol calculador = new CalculadorIndicadoresSol();

        Estrella solInactivo = new Estrella("Sol Consumido", TipoCuerpo.ESTRELLA.masaBase, 4000.0, 4000.0);
        solInactivo.setSimulado(false);

        assertTrue(calculador.calcularIndicadores(List.of(solInactivo), camara, 1280.0, 720.0).isEmpty());
        assertEquals(DesplazamientoCamara.LIMITE_MUNDO_POR_DEFECTO, camara.getDesplazamiento().getLimiteMundo(), 1e-6);
    }
}
