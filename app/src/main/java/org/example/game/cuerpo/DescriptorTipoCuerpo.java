package org.example.game.cuerpo;
import org.example.game.jugador.TipoRecurso;
import java.util.Collections;
import java.util.Map;
/**
 * Descriptor inmutable de metadatos economicos, narrativos y visuales para un TipoCuerpo.
 *
 * Principios SOLID:
 * - SRP: Responsabilidad exclusiva de albergar las especificaciones de costos, produccion, lore e icono.
 * - OCP: Elimina sentencias switch procedurales descentralizadas para cada tipo de cuerpo celeste.
 */
public record DescriptorTipoCuerpo( Map<TipoRecurso, Double> costosBase, Map<TipoRecurso, Double> produccionBase, double radioAtraccionMin, double radioAtraccionMax, String nombrePorDefecto, String icono, String subtitulo, String lore, String rutaAudio ) {
    public static final DescriptorTipoCuerpo DEFECTO = new DescriptorTipoCuerpo(
            Collections.emptyMap(),
            Collections.emptyMap(),
            20.0,
            35.0,
            "Cuerpo",
            "[???]",
            "Objeto Astronomico",
            "Cuerpo celeste en el sistema.",
            null
    );

    public DescriptorTipoCuerpo {
        costosBase = costosBase != null ? Collections.unmodifiableMap(costosBase) : Collections.emptyMap();
        produccionBase = produccionBase != null ? Collections.unmodifiableMap(produccionBase) : Collections.emptyMap();
    }
}
