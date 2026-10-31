package org.example;
import org.example.game.civilizacion.EstadoCivilizacion;
import org.example.game.cuerpo.TipoCuerpo;
import org.example.game.guardado.*;
import org.example.game.jugador.TipoRecurso;
import org.example.game.motor.Vector2D;
import org.example.game.progresion.RamaProgresion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
class RepositorioYSerializacionPartidaTest {

    @TempDir
    Path tempDir;

    @Test
    void testRepositorioCreaCarpetaDedicadaSiNoExiste() throws Exception {
        Path carpetaNueva = tempDir.resolve("subnivel").resolve("partidas_dedicadas");
        assertFalse(Files.exists(carpetaNueva));

        RepositorioPartidasArchivo repo = new RepositorioPartidasArchivo(carpetaNueva);
        assertTrue(Files.isDirectory(carpetaNueva), "Debe crear la carpeta dedicada al inicializar si no existe");

        Files.delete(carpetaNueva);
        assertFalse(Files.exists(carpetaNueva));

        DatosPartida partida = construirPartidaEjemplo("slot_alfa");
        Path archivoEscrito = repo.guardar("slot_alfa", partida);

        assertTrue(Files.isDirectory(carpetaNueva), "Debe recrear la carpeta dedicada al guardar si fue eliminada");
        assertTrue(Files.isRegularFile(archivoEscrito), "El archivo guardado debe existir en disco");
        assertEquals(carpetaNueva.toAbsolutePath().normalize(), archivoEscrito.getParent());
    }

    @Test
    void testSerializacionYDeserializacionIdempotenteCompleta() {
        SerializadorPartida serializador = new SerializadorPartidaTexto();
        DatosPartida original = construirPartidaEjemplo("Partida = Especial #1");

        String texto = serializador.serializar(original);
        assertNotNull(texto);
        assertTrue(texto.contains("formato.version=1"));

        DatosPartida restaurada = serializador.deserializar(texto);
        assertEquals(original, restaurada);
        assertEquals(texto, serializador.serializar(restaurada));
    }

    @Test
    void testRepositorioListarVerificarExistenciaYEliminar() {
        Path carpeta = tempDir.resolve("partidas_test");
        RepositorioPartidas repo = new RepositorioPartidasArchivo(carpeta);

        assertTrue(repo.listarPartidas().isEmpty());
        assertFalse(repo.existe("partida_uno"));

        repo.guardar("partida_dos", construirPartidaEjemplo("partida_dos"));
        repo.guardar("partida_uno", construirPartidaEjemplo("partida_uno"));

        assertEquals(List.of("partida_dos", "partida_uno"), repo.listarPartidas());
        assertTrue(repo.existe("partida_uno"));

        Optional<DatosPartida> cargada = repo.cargar("partida_uno");
        assertTrue(cargada.isPresent());
        assertEquals("partida_uno", cargada.get().nombrePartida());

        assertTrue(repo.eliminar("partida_uno"));
        assertFalse(repo.existe("partida_uno"));
        assertEquals(List.of("partida_dos"), repo.listarPartidas());
    }

    @Test
    void testCargarPartidaInexistenteRetornaOptionalVacio() {
        RepositorioPartidas repo = new RepositorioPartidasArchivo(tempDir.resolve("inexistente"));
        Optional<DatosPartida> resultado = repo.cargar("no_existe");
        assertTrue(resultado.isEmpty());
    }

    @Test
    void testSerializadorRechazaContenidoCorruptoOVersionInvalida() {
        SerializadorPartida serializador = new SerializadorPartidaTexto();

        assertThrows(ExcepcionPersistenciaPartida.class, () -> serializador.deserializar(""));
        assertThrows(ExcepcionPersistenciaPartida.class, () -> serializador.deserializar(null));
        assertThrows(ExcepcionPersistenciaPartida.class,
                () -> serializador.deserializar("formato.version=99\npartida.nombre=test\n"));
        assertThrows(ExcepcionPersistenciaPartida.class,
                () -> serializador.deserializar("formato.version=1\npartida.nombre=test\nreloj.tick=no_numero\n"));
    }

    @Test
    void testDatosGuardadosInmutablesYValidanInvariantes() {
        DatosPartida partida = construirPartidaEjemplo("Inmutable");
        assertThrows(UnsupportedOperationException.class, () -> partida.cuerpos().clear());
        assertThrows(UnsupportedOperationException.class, () -> partida.naves().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> partida.jugador().recursos().put(TipoRecurso.CIENCIA, 999.0));

        EstadoCinematica cin = new EstadoCinematica(Vector2D.cero(), Vector2D.cero(), -1, 0);
        assertThrows(IllegalArgumentException.class,
                () -> new DatosCuerpo(TipoCuerpo.ESTRELLA, "  ", 100.0, true, cin, DetalleCuerpo.basico()));
        assertThrows(IllegalArgumentException.class,
                () -> new DatosCuerpo(TipoCuerpo.ESTRELLA, "Sol", -5.0, true, cin, DetalleCuerpo.basico()));
    }

    @Test
    void testRepositorioValidaIdentificadoresYSanitizaRutas() {
        Path carpeta = tempDir.resolve("segura");
        RepositorioPartidasArchivo repo = new RepositorioPartidasArchivo(carpeta);
        DatosPartida datos = construirPartidaEjemplo("segura");

        assertThrows(ExcepcionPersistenciaPartida.class, () -> repo.guardar("   ", datos));
        assertThrows(ExcepcionPersistenciaPartida.class, () -> repo.guardar("..", datos));

        Path guardado = repo.guardar("../intento_escape", datos);
        assertTrue(guardado.startsWith(repo.getDirectorioGuardado()));
    }

    private DatosPartida construirPartidaEjemplo(String nombre) {
        EstadoRelojGuardado reloj = new EstadoRelojGuardado(340L, true, 2.0, 1.3, 1800);
        Map<TipoRecurso, Double> recursos = new EnumMap<>(TipoRecurso.class);
        recursos.put(TipoRecurso.MINERALES, 520.5);
        recursos.put(TipoRecurso.ENERGIA, 310.0);
        recursos.put(TipoRecurso.POBLACION, 1450.0);
        recursos.put(TipoRecurso.CIENCIA, 275.25);

        Map<RamaProgresion, Integer> ramas = new EnumMap<>(RamaProgresion.class);
        ramas.put(RamaProgresion.PLANETAS, 3);
        ramas.put(RamaProgresion.MASA, 2);
        ramas.put(RamaProgresion.CANTIDAD_PLANETAS, 1);
        EstadoJugadorGuardado jugador = new EstadoJugadorGuardado(recursos, ramas, 2);

        List<DatosCuerpo> cuerpos = new ArrayList<>();
        cuerpos.add(new DatosCuerpo(
                TipoCuerpo.ESTRELLA, "Sol Central", 1.989e30, true,
                new EstadoCinematica(new Vector2D(0, 0), Vector2D.cero(), -1, 0),
                DetalleCuerpo.basico()
        ));
        DatosCivilizacion civ = new DatosCivilizacion(1450.0, 3, 0.88, 0.91, EstadoCivilizacion.PRÓSPERA, 12);
        cuerpos.add(new DatosCuerpo(
                TipoCuerpo.PLANETA_ROCOSO, "Tierra = Nueva", 5.97e24, true,
                new EstadoCinematica(new Vector2D(180, 0), new Vector2D(0, -12.5), 0, 180.0),
                DetalleCuerpo.dePlaneta(false, 3, civ)
        ));
        cuerpos.add(new DatosCuerpo(
                TipoCuerpo.SATELITE, "Sonda-7", 5000.0, true,
                new EstadoCinematica(new Vector2D(205, 0), new Vector2D(0, -14.0), 1, 25.0),
                DetalleCuerpo.deSatelite(42.5, 18.0)
        ));
        cuerpos.add(new DatosCuerpo(
                TipoCuerpo.METEORITO, "Bolido-9", 1e20, true,
                new EstadoCinematica(new Vector2D(-400, 300), new Vector2D(4.0, -3.0), -1, 0.0),
                DetalleCuerpo.deMeteorito(850.0, false, 1)
        ));

        List<DatosNaveColonizadora> naves = List.of(
                new DatosNaveColonizadora(new Vector2D(90.0, 45.0), 1, 250.0)
        );
        return new DatosPartida(nombre, reloj, jugador, cuerpos, naves);
    }
}
