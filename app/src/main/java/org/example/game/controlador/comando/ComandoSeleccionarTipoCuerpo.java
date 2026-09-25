package org.example.game.controlador.comando;

import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.ui.BarraInventarioHotbar;

/**
 * SRP: Encapsula la acción de seleccionar un tipo de cuerpo celeste en la barra de acceso rápido (hotbar).
 */
public class ComandoSeleccionarTipoCuerpo implements Comando {

    private final BarraInventarioHotbar hotbar;
    private final TipoCuerpo tipoCuerpo;

    public ComandoSeleccionarTipoCuerpo(BarraInventarioHotbar hotbar, TipoCuerpo tipoCuerpo) {
        this.hotbar = hotbar;
        this.tipoCuerpo = tipoCuerpo;
    }

    @Override
    public void ejecutar() {
        if (hotbar != null && tipoCuerpo != null) {
            hotbar.seleccionarTipo(tipoCuerpo);
        }
    }
}
