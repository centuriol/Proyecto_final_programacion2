package org.example.game.motor;

import org.example.game.motor.colision.*;
import java.util.List;

/**
 * Fábrica de ensamblaje por defecto del motor físico y sus reglas de colisión/captura (Composition Root para DIP).
 */
public class FabricaMotorFisica {

    public List<ReglaColision> crearReglasColisionPorDefecto(UtilidadColision utilidad) {
        UtilidadColision u = utilidad != null ? utilidad : new UtilidadColision();
        return List.of(
                new ReglaColisionAgujeroNegro(u),
                new ReglaColisionEstrella(u),
                new ReglaColisionEscudo(u),
                new ReglaColisionMeteorito(u),
                new ReglaColisionFusionAguaLava(u),
                new ReglaColisionAbsorcionPorRatio(u),
                new ReglaColisionColapsoMutuo(u),
                new ReglaColisionAsimetrica(u)
        );
    }

    public List<EstrategiaCapturaOrbital> crearEstrategiasCapturaPorDefecto() {
        return List.of(
                new CapturaOrbitalMenorPlaneta(),
                new CapturaOrbitalPlanetaAtractor()
        );
    }

    public BuscadorOrbita crearBuscadorOrbitaPorDefecto() {
        return new BuscadorOrbitaLibre();
    }

    public InterceptorAmenazas crearInterceptorPorDefecto(ResolucionColision resolvedor) {
        return new InterceptorMeteoritosSatelite(resolvedor);
    }

    public AsistenciaOrbital crearAsistenciaOrbitalPorDefecto() {
        return new AsistenteOrbital();
    }

    public SeparadorContactoOrbital crearSeparadorContactoPorDefecto() {
        return new SeparadorContactoOrbital(crearAsistenciaOrbitalPorDefecto());
    }

    public PipelineFisicaPermisiva crearPipelinePorDefecto(
            CalculadorFuerzaGravitatoria cg, AsistenciaOrbital ao, ResolucionColision rc,
            ProcesadorAgujeroNegro gan, IntegradorFisico im, DetectorContactoFisico dc) {
        double g = ConstantesFisicas.G_ESCALADO;
        ResolucionColision resolvedor = rc != null ? rc : new ResolvedorColisiones();
        ConfiguracionEventosColision configEventos = resolvedor instanceof ConfiguracionEventosColision cfg
                ? cfg : null;
        return new PipelineFisicaPermisiva(
                cg != null ? cg : new CalculadorGravedad(g, 35.0),
                ao != null ? ao : new AsistenteOrbital(g),
                resolvedor,
                configEventos,
                gan != null ? gan : new GestorAgujeroNegro(),
                im != null ? im : new IntegradorMovimiento(1.0, 1200.0),
                dc != null ? dc : new DetectorColisiones(resolvedor, crearSeparadorContactoPorDefecto())
        );
    }

    public org.example.game.cuerpo.CreadorMeteoritos crearCreadorMeteoritosPorDefecto() {
        return new org.example.game.cuerpo.CuerpoCelesteFactory();
    }
}
