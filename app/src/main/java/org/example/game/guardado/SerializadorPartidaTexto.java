package org.example.game.guardado;
import org.example.game.jugador.TipoRecurso; import org.example.game.motor.Vector2D; import org.example.game.progresion.RamaProgresion; import java.util.ArrayList; import java.util.EnumMap; import java.util.List; import java.util.Map;
/**
 * Implementacion textual determinista de SerializadorPartida basada en pares clave-valor estructurados.
 *
 * Principios SOLID:
 * - SRP: Transforma DatosPartida a texto y viceversa sin realizar operaciones de sistema de archivos.
 * - DIP: Implementa el contrato SerializadorPartida y delega la codificacion de entidades.
 */
public class SerializadorPartidaTexto implements SerializadorPartida {

    public static final int VERSION_FORMATO = 1;

    private final CodificadorEntidadesPartida codificadorEntidades;

    public SerializadorPartidaTexto() {
        this(new CodificadorEntidadesPartida());
    }

    public SerializadorPartidaTexto(CodificadorEntidadesPartida codificadorEntidades) {
        this.codificadorEntidades = codificadorEntidades != null
                ? codificadorEntidades
                : new CodificadorEntidadesPartida();
    }

    @Override
    public String serializar(DatosPartida datos) {
        if (datos == null) {
            throw new ExcepcionPersistenciaPartida("No se puede serializar una partida nula");
        }
        DiccionarioPropiedades dic = new DiccionarioPropiedades();
        dic.guardar("formato.version", VERSION_FORMATO);
        dic.guardar("partida.nombre", datos.nombrePartida());
        dic.guardar("partida.marcaTiempo", datos.marcaTiempoGuardado());
        escribirReloj(dic, datos.reloj());
        escribirJugador(dic, datos.jugador());
        escribirColecciones(dic, datos.cuerpos(), datos.naves());
        return dic.exportarTexto();
    }

    @Override
    public DatosPartida deserializar(String contenido) {
        DiccionarioPropiedades dic = DiccionarioPropiedades.desdeTexto(contenido);
        int version = dic.leerEntero("formato.version");
        if (version != VERSION_FORMATO) {
            throw new ExcepcionPersistenciaPartida("Version de formato no soportada: " + version);
        }
        try {
            String nombre = dic.leerTexto("partida.nombre");
            long marcaTiempo = dic.contiene("partida.marcaTiempo") ? dic.leerLargo("partida.marcaTiempo") : 0L;
            EstadoRelojGuardado reloj = leerReloj(dic);
            EstadoJugadorGuardado jugador = leerJugador(dic);
            List<DatosCuerpo> cuerpos = leerCuerpos(dic);
            List<DatosNaveColonizadora> naves = leerNaves(dic);
            return new DatosPartida(nombre, marcaTiempo, reloj, jugador, cuerpos, naves);
        } catch (IllegalArgumentException e) {
            throw new ExcepcionPersistenciaPartida("Datos de partida corruptos o invalidos", e);
        }
    }

    private void escribirReloj(DiccionarioPropiedades dic, EstadoRelojGuardado reloj) {
        dic.guardar("reloj.tick", reloj.tickActual());
        dic.guardar("reloj.pausa", reloj.enPausa());
        dic.guardar("reloj.velocidad", reloj.velocidadSimulacion());
        dic.guardar("reloj.zoom", reloj.nivelZoom());
        dic.guardar("reloj.ticksEvento", reloj.ticksSiguienteEvento());
        dic.guardar("reloj.camaraX", reloj.desplazamientoCamara().x());
        dic.guardar("reloj.camaraY", reloj.desplazamientoCamara().y());
    }

    private EstadoRelojGuardado leerReloj(DiccionarioPropiedades dic) {
        double camX = dic.contiene("reloj.camaraX") ? dic.leerReal("reloj.camaraX") : 0.0;
        double camY = dic.contiene("reloj.camaraY") ? dic.leerReal("reloj.camaraY") : 0.0;
        return new EstadoRelojGuardado(
                dic.leerLargo("reloj.tick"),
                dic.leerBooleano("reloj.pausa"),
                dic.leerReal("reloj.velocidad"),
                dic.leerReal("reloj.zoom"),
                dic.leerEntero("reloj.ticksEvento"),
                new Vector2D(camX, camY)
        );
    }

    private void escribirJugador(DiccionarioPropiedades dic, EstadoJugadorGuardado jugador) {
        dic.guardar("progresion.nivelEscudo", jugador.nivelEscudo());
        for (TipoRecurso tr : TipoRecurso.values()) {
            dic.guardar("recurso." + tr.name(), jugador.obtenerRecurso(tr));
        }
        for (RamaProgresion rama : RamaProgresion.values()) {
            dic.guardar("progresion.rama." + rama.name(), jugador.obtenerNivelRama(rama));
        }
    }

    private EstadoJugadorGuardado leerJugador(DiccionarioPropiedades dic) {
        Map<TipoRecurso, Double> recursos = new EnumMap<>(TipoRecurso.class);
        for (TipoRecurso tr : TipoRecurso.values()) {
            recursos.put(tr, dic.leerReal("recurso." + tr.name()));
        }
        Map<RamaProgresion, Integer> ramas = new EnumMap<>(RamaProgresion.class);
        for (RamaProgresion rama : RamaProgresion.values()) {
            ramas.put(rama, dic.leerEntero("progresion.rama." + rama.name()));
        }
        int escudo = dic.leerEntero("progresion.nivelEscudo");
        return new EstadoJugadorGuardado(recursos, ramas, escudo);
    }

    private void escribirColecciones(DiccionarioPropiedades dic, List<DatosCuerpo> cuerpos, List<DatosNaveColonizadora> naves) {
        dic.guardar("cuerpos.cantidad", cuerpos.size());
        for (int i = 0; i < cuerpos.size(); i++) {
            codificadorEntidades.escribirCuerpo(dic, i, cuerpos.get(i));
        }
        dic.guardar("naves.cantidad", naves.size());
        for (int i = 0; i < naves.size(); i++) {
            codificadorEntidades.escribirNave(dic, i, naves.get(i));
        }
    }

    private List<DatosCuerpo> leerCuerpos(DiccionarioPropiedades dic) {
        int cantidad = dic.leerEntero("cuerpos.cantidad");
        if (cantidad < 0) {
            throw new ExcepcionPersistenciaPartida("Cantidad de cuerpos negativa");
        }
        List<DatosCuerpo> resultado = new ArrayList<>(cantidad);
        for (int i = 0; i < cantidad; i++) {
            resultado.add(codificadorEntidades.leerCuerpo(dic, i));
        }
        return resultado;
    }

    private List<DatosNaveColonizadora> leerNaves(DiccionarioPropiedades dic) {
        int cantidad = dic.leerEntero("naves.cantidad");
        if (cantidad < 0) {
            throw new ExcepcionPersistenciaPartida("Cantidad de naves negativa");
        }
        List<DatosNaveColonizadora> resultado = new ArrayList<>(cantidad);
        for (int i = 0; i < cantidad; i++) {
            resultado.add(codificadorEntidades.leerNave(dic, i));
        }
        return resultado;
    }
}
