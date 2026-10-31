package org.example.game.guardado;
import org.example.game.civilizacion.EstadoCivilizacion;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.motor.Vector2D;
/**
 * Codifica y decodifica las entidades individuales (cuerpos celestes y naves colonizadoras)
 * dentro de un DiccionarioPropiedades empleando prefijos configurables (SRP).
 */
public class CodificadorEntidadesPartida {

    private final String prefijoCuerpo;
    private final String prefijoNave;

    public CodificadorEntidadesPartida() {
        this("cuerpo.", "nave.");
    }

    public CodificadorEntidadesPartida(String prefijoCuerpo, String prefijoNave) {
        this.prefijoCuerpo = (prefijoCuerpo != null && !prefijoCuerpo.isBlank()) ? prefijoCuerpo : "cuerpo.";
        this.prefijoNave = (prefijoNave != null && !prefijoNave.isBlank()) ? prefijoNave : "nave.";
    }

    public void escribirCuerpo(DiccionarioPropiedades dic, int indice, DatosCuerpo cuerpo) {
        String p = prefijoCuerpo + indice + ".";
        dic.guardar(p + "tipo", cuerpo.tipo().name());
        dic.guardar(p + "nombre", cuerpo.nombre());
        dic.guardar(p + "masa", cuerpo.masa());
        dic.guardar(p + "simulado", cuerpo.simulado());
        escribirCinematica(dic, p, cuerpo.cinematica());
        escribirDetalle(dic, p, cuerpo.detalle());
    }

    private void escribirCinematica(DiccionarioPropiedades dic, String prefijo, EstadoCinematica cin) {
        dic.guardar(prefijo + "posX", cin.posicion().x());
        dic.guardar(prefijo + "posY", cin.posicion().y());
        dic.guardar(prefijo + "velX", cin.velocidad().x());
        dic.guardar(prefijo + "velY", cin.velocidad().y());
        dic.guardar(prefijo + "orbitado", cin.indiceOrbitado());
        dic.guardar(prefijo + "radioOrbita", cin.radioOrbita());
    }

    private void escribirDetalle(DiccionarioPropiedades dic, String prefijo, DetalleCuerpo det) {
        dic.guardar(prefijo + "transformado", det.transformadoPorEvento());
        dic.guardar(prefijo + "escudo", det.golpesEscudo());
        dic.guardar(prefijo + "cienciaGen", det.cienciaGenerada());
        dic.guardar(prefijo + "energiaGen", det.energiaGenerada());
        dic.guardar(prefijo + "vidaRestante", det.tiempoVidaRestante());
        dic.guardar(prefijo + "lanzadoJugador", det.lanzadoPorJugador());
        dic.guardar(prefijo + "objetivo", det.indiceObjetivo());
        boolean poseeCiv = det.tieneCivilizacionRegistrada();
        dic.guardar(prefijo + "civ.presente", poseeCiv);
        if (poseeCiv) {
            DatosCivilizacion civ = det.civilizacion();
            dic.guardar(prefijo + "civ.poblacion", civ.poblacion());
            dic.guardar(prefijo + "civ.tech", civ.nivelTecnologico());
            dic.guardar(prefijo + "civ.estabilidad", civ.estabilidad());
            dic.guardar(prefijo + "civ.felicidad", civ.felicidad());
            dic.guardar(prefijo + "civ.estado", civ.estado().name());
            dic.guardar(prefijo + "civ.ticksEvento", civ.ticksEfectoEvento());
        }
    }

    public DatosCuerpo leerCuerpo(DiccionarioPropiedades dic, int indice) {
        String p = prefijoCuerpo + indice + ".";
        TipoCuerpo tipo = dic.leerEnum(p + "tipo", TipoCuerpo.class);
        String nombre = dic.leerTexto(p + "nombre");
        double masa = dic.leerReal(p + "masa");
        boolean simulado = dic.leerBooleano(p + "simulado");
        EstadoCinematica cinematica = new EstadoCinematica(
                new Vector2D(dic.leerReal(p + "posX"), dic.leerReal(p + "posY")),
                new Vector2D(dic.leerReal(p + "velX"), dic.leerReal(p + "velY")),
                dic.leerEntero(p + "orbitado"),
                dic.leerReal(p + "radioOrbita")
        );
        return new DatosCuerpo(tipo, nombre, masa, simulado, cinematica, leerDetalle(dic, p));
    }

    private DetalleCuerpo leerDetalle(DiccionarioPropiedades dic, String prefijo) {
        DatosCivilizacion civ = null;
        if (dic.leerBooleano(prefijo + "civ.presente")) {
            civ = new DatosCivilizacion(
                    dic.leerReal(prefijo + "civ.poblacion"),
                    dic.leerEntero(prefijo + "civ.tech"),
                    dic.leerReal(prefijo + "civ.estabilidad"),
                    dic.leerReal(prefijo + "civ.felicidad"),
                    dic.leerEnum(prefijo + "civ.estado", EstadoCivilizacion.class),
                    dic.leerEntero(prefijo + "civ.ticksEvento")
            );
        }
        return new DetalleCuerpo(
                dic.leerBooleano(prefijo + "transformado"),
                dic.leerEntero(prefijo + "escudo"),
                civ,
                dic.leerReal(prefijo + "cienciaGen"),
                dic.leerReal(prefijo + "energiaGen"),
                dic.leerReal(prefijo + "vidaRestante"),
                dic.leerBooleano(prefijo + "lanzadoJugador"),
                dic.leerEntero(prefijo + "objetivo")
        );
    }

    public void escribirNave(DiccionarioPropiedades dic, int indice, DatosNaveColonizadora nave) {
        String p = prefijoNave + indice + ".";
        dic.guardar(p + "posX", nave.posicion().x());
        dic.guardar(p + "posY", nave.posicion().y());
        dic.guardar(p + "destino", nave.indiceDestino());
        dic.guardar(p + "colonos", nave.poblacionColonos());
    }

    public DatosNaveColonizadora leerNave(DiccionarioPropiedades dic, int indice) {
        String p = prefijoNave + indice + ".";
        Vector2D pos = new Vector2D(dic.leerReal(p + "posX"), dic.leerReal(p + "posY"));
        return new DatosNaveColonizadora(pos, dic.leerEntero(p + "destino"), dic.leerReal(p + "colonos"));
    }
}
