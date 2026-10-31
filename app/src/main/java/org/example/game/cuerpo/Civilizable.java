package org.example.game.cuerpo;
import org.example.game.civilizacion.CivilizacionComponent;
import java.util.List;
/**
 * ISP: solo la implementan los cuerpos que pueden albergar y desarrollar civilización.
 */
public interface Civilizable {
    void desarrollarCivilizacion();
    boolean tieneCivilizacion();
    CivilizacionComponent getCivilizacion();
    void actualizarCivilizacion(List<CuerpoCeleste> cuerpos);
}
