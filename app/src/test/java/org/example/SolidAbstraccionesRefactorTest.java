package org.example;
import javafx.scene.input.KeyCode;
import org.example.game.cuerpo.*;
import org.example.game.motor.MotorFisica;
import org.example.game.controlador.CombinacionTeclas;
import org.example.game.cuerpo.CuerpoCelesteFactory;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.jugador.InventarioJugador;
import org.example.game.jugador.ServicioReembolso;
import org.example.game.jugador.TipoRecurso;
import org.example.game.motor.MotorFisicaBase;
import org.example.game.motor.Vector2D;
import org.example.game.render.GestorEstelas;
import org.example.game.ui.FormateadorNumero;
import org.example.test.JavaFXExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(JavaFXExtension.class)
public class SolidAbstraccionesRefactorTest {

    private InventarioJugador inventario;

    @BeforeEach
    void setUp() {
        inventario = new InventarioJugador();
    }

    @Test
    @DisplayName("FormateadorNumero formatea cantidades numericas y colores hexadecimales correctamente (SRP / DRY)")
    void testFormateadorNumero() {
        assertEquals("500", FormateadorNumero.formatear(500));
        assertEquals("1.5K", FormateadorNumero.formatear(1500));
        assertEquals("2.0M", FormateadorNumero.formatear(2_000_000));
        assertEquals("3.5B", FormateadorNumero.formatear(3_500_000_000L));
        assertEquals("10.0K", FormateadorNumero.formatear(10000.0));

        assertEquals("#FFFFFF", FormateadorNumero.formatearColorHex(0xFFFFFF));
        assertEquals("#000000", FormateadorNumero.formatearColorHex(0x000000));
        assertEquals("#5BE3FF", FormateadorNumero.formatearColorHex(0x5BE3FF));
    }

    @Test
    @DisplayName("GestorEstelas gestiona puntos orbitales, limpieza y limites de memoria (SRP)")
    void testGestorEstelas() {
        GestorEstelas gestor = new GestorEstelas();
        CuerpoCeleste cuerpo = new Planeta("TestPlaneta", 100.0, 10.0, 20.0);

        for (int i = 0; i < 60; i++) {
            cuerpo.setPosicion(new Vector2D(i * 10, i * 10));
            gestor.actualizar(List.of(cuerpo));
        }

        Deque<Vector2D> estela = gestor.getEstelas().get(cuerpo);
        assertNotNull(estela);
        assertTrue(estela.size() <= 45, "La estela debe respetar el maximo de puntos para evitar fugas de memoria");

        gestor.limpiar();
        assertTrue(gestor.getEstelas().isEmpty(), "limpiar debe vaciar los historiales de estelas");
    }

    @Test
    @DisplayName("MotorFisicaBase limpia cuerpos expirados y notifica colisiones de forma polimorfica (LSP / DRY)")
    void testMotorFisicaBase() {
        TestMotorFisica motor = new TestMotorFisica(1000.0);
        AtomicBoolean colisionNotificada = new AtomicBoolean(false);
        motor.agregarListenerColision(() -> colisionNotificada.set(true));

        motor.dispararColision();
        assertTrue(colisionNotificada.get(), "El listener de colision debe ser notificado");

        List<CuerpoCeleste> cuerpos = new ArrayList<>();
        CuerpoCeleste normal = new Planeta("Normal", 10.0, 0, 0);
        CuerpoCeleste expirado = new CuerpoExpirableTest("Expirado", 10.0, 0, 0);

        cuerpos.add(normal);
        cuerpos.add(expirado);

        motor.limpiarExpirados(cuerpos);
        assertEquals(1, cuerpos.size(), "Solo debe quedar el cuerpo que no ha expirado");
        assertSame(normal, cuerpos.get(0));
    }

    @Test
    @DisplayName("ServicioReembolso calcula el 50% de recuperacion y respeta restricciones de transformacion (SRP / LSP)")
    void testServicioReembolso() {
        ServicioReembolso servicioReembolso = new ServicioReembolso();
        CuerpoCeleste planeta = new Planeta("Mundo", TipoCuerpo.PLANETA_ROCOSO.masaBase, 0, 0, TipoCuerpo.PLANETA_ROCOSO);
        Map<TipoRecurso, Double> reembolso = servicioReembolso.calcularReembolso(planeta);

        assertNotNull(reembolso);
        assertFalse(reembolso.isEmpty(), "Debe existir reembolso para un cuerpo normal");

        // El reembolso debe ser exactamente el 50% del costo base
        Map<TipoRecurso, Double> costoBase = TipoCuerpo.PLANETA_ROCOSO.getCostosBase();
        for (Map.Entry<TipoRecurso, Double> entry : reembolso.entrySet()) {
            double esperado = costoBase.getOrDefault(entry.getKey(), 0.0) * 0.5;
            assertEquals(esperado, entry.getValue(), 0.001);
        }

        // Si esta transformado por evento, no permite reembolso (Tarea 5)
        if (planeta instanceof Transformable t) {
            t.setTransformadoPorEvento(true);
        }
        assertFalse(planeta.permiteReembolso());
        Map<TipoRecurso, Double> reembolsoTransformado = servicioReembolso.calcularReembolso(planeta);
        assertTrue(reembolsoTransformado.isEmpty(), "No debe haber reembolso si fue transformado por evento");
    }

    @Test
    @DisplayName("CombinacionTeclas identifica teclas y modificadores con igualdad estructural (SRP)")
    void testCombinacionTeclas() {
        CombinacionTeclas c1 = CombinacionTeclas.de(KeyCode.SPACE);
        CombinacionTeclas c2 = CombinacionTeclas.de(KeyCode.SPACE);
        CombinacionTeclas cCtrlM1 = CombinacionTeclas.ctrl(KeyCode.M);
        CombinacionTeclas cCtrlM2 = CombinacionTeclas.ctrl(KeyCode.M);
        CombinacionTeclas cShiftA = CombinacionTeclas.shift(KeyCode.A);

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
        assertEquals(cCtrlM1, cCtrlM2);
        assertNotEquals(c1, cCtrlM1);
        assertNotEquals(cCtrlM1, cShiftA);
        assertTrue(cCtrlM1.isControl());
        assertTrue(cShiftA.isShift());
        assertFalse(c1.isControl() || c1.isShift() || c1.isAlt());
    }

    @Test
    @DisplayName("CuerpoCelesteFactory permite registrar y crear cuerpos dinamicamente via CreadorCuerpo (OCP)")
    void testCuerpoCelesteFactoryExtensible() {
        // Creacion por registro estandar
        CuerpoCeleste sol = CuerpoCelesteFactory.crearCuerpo(TipoCuerpo.ESTRELLA, 100, 200, 1.0, Vector2D.cero());
        assertNotNull(sol);
        assertEquals(TipoCuerpo.ESTRELLA, sol.getTipoCuerpo());

        CuerpoCeleste planeta = CuerpoCelesteFactory.crearCuerpo(TipoCuerpo.PLANETA_ROCOSO, 50, 60, 1.2, new Vector2D(0, 5));
        assertNotNull(planeta);
        assertEquals(TipoCuerpo.PLANETA_ROCOSO, planeta.getTipoCuerpo());

        // Registro de un creador personalizado para validar OCP
        AtomicBoolean creadorInvocado = new AtomicBoolean(false);
        CuerpoCelesteFactory.registrarCreador(TipoCuerpo.SATELITE, (x, y, factor, vel) -> {
            creadorInvocado.set(true);
            return new Planeta("SatCustom", 10.0, x, y);
        });

        CuerpoCeleste custom = CuerpoCelesteFactory.crearCuerpo(TipoCuerpo.SATELITE, 10, 20, 1.0, Vector2D.cero());
        assertTrue(creadorInvocado.get(), "El creador registrado debe invocarse polimorficamente");
        assertNotNull(custom);

        // Restaurar creador de satelite para no afectar otros tests
        CuerpoCelesteFactory.registrarCreador(TipoCuerpo.SATELITE, CuerpoCelesteFactory::crearSatelite);
    }

    @Test
    @DisplayName("CuerpoCeleste segrega capacidades en interfaces chicas (ISP / LSP)")
    void testPolimorfismoCuerpoCeleste() {
        CuerpoCeleste cuerpo = new Planeta("PocoComun", 100.0, 0, 0);

        assertTrue(cuerpo.permiteReembolso());
        assertTrue(cuerpo instanceof Civilizable);
        assertTrue(cuerpo instanceof Defensor);
        Defensor defensor = (Defensor) cuerpo;
        assertEquals(65.0, defensor.getRadioEscudo(), 0.001);
        assertEquals(0.0, defensor.getPulsoVisual(), 0.001);

        Civilizable civilizable = (Civilizable) cuerpo;
        assertDoesNotThrow(() -> civilizable.actualizarCivilizacion(List.of()));
    }

    // Helper classes para tests
    private static class TestMotorFisica extends MotorFisicaBase {
        public TestMotorFisica(double radioMundo) {
            super(radioMundo);
        }
        @Override
        public void avanzarPaso(List<CuerpoCeleste> cuerpos) {
            limpiarCuerposExpirados(cuerpos);
        }
        public void dispararColision() {
            notificarColision(null);
        }
        public void limpiarExpirados(List<CuerpoCeleste> cuerpos) {
            limpiarCuerposExpirados(cuerpos);
        }
    }

    private static class CuerpoExpirableTest extends Planeta implements Expirable {
        public CuerpoExpirableTest(String nombre, double masa, double x, double y) {
            super(nombre, masa, x, y);
        }
        @Override
        public boolean estaExpirado() {
            return true;
        }
        @Override
        public java.util.Optional<Expirable> comoExpirable() {
            return java.util.Optional.of(this);
        }
    }
}
