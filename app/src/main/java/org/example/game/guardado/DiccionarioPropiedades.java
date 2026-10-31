package org.example.game.guardado;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;
/**
 * Contenedor tipado de pares clave-valor para lectura y escritura segura de propiedades de partida (SRP).
 */
public class DiccionarioPropiedades {

    private final Properties propiedades;

    public DiccionarioPropiedades() {
        this.propiedades = new Properties();
    }

    private DiccionarioPropiedades(Properties propiedades) {
        this.propiedades = propiedades;
    }

    public static DiccionarioPropiedades desdeTexto(String contenido) {
        if (contenido == null || contenido.isBlank()) {
            throw new ExcepcionPersistenciaPartida("El contenido de la partida guardada esta vacio");
        }
        Properties cargadas = new Properties();
        try {
            cargadas.load(new StringReader(contenido));
        } catch (IOException e) {
            throw new ExcepcionPersistenciaPartida("Error al leer el formato de propiedades", e);
        }
        if (cargadas.isEmpty()) {
            throw new ExcepcionPersistenciaPartida("El archivo de partida no contiene propiedades validas");
        }
        return new DiccionarioPropiedades(cargadas);
    }

    public void guardar(String clave, Object valor) {
        if (clave != null && valor != null) {
            propiedades.setProperty(clave, String.valueOf(valor));
        }
    }

    public boolean contiene(String clave) {
        return clave != null && propiedades.getProperty(clave) != null;
    }

    public String leerTexto(String clave) {
        String valor = propiedades.getProperty(clave);
        if (valor == null || valor.isBlank()) {
            throw new ExcepcionPersistenciaPartida("Propiedad obligatoria ausente o vacia: " + clave);
        }
        return valor;
    }

    public double leerReal(String clave) {
        String texto = leerTexto(clave);
        try {
            double valor = Double.parseDouble(texto.trim());
            if (!Double.isFinite(valor)) {
                throw new ExcepcionPersistenciaPartida("Valor numerico no finito en: " + clave);
            }
            return valor;
        } catch (NumberFormatException e) {
            throw new ExcepcionPersistenciaPartida("Numero decimal invalido en: " + clave, e);
        }
    }

    public int leerEntero(String clave) {
        String texto = leerTexto(clave);
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new ExcepcionPersistenciaPartida("Numero entero invalido en: " + clave, e);
        }
    }

    public long leerLargo(String clave) {
        String texto = leerTexto(clave);
        try {
            return Long.parseLong(texto.trim());
        } catch (NumberFormatException e) {
            throw new ExcepcionPersistenciaPartida("Numero largo invalido en: " + clave, e);
        }
    }

    public boolean leerBooleano(String clave) {
        String texto = leerTexto(clave).trim();
        if ("true".equalsIgnoreCase(texto)) return true;
        if ("false".equalsIgnoreCase(texto)) return false;
        throw new ExcepcionPersistenciaPartida("Valor booleano invalido en: " + clave);
    }

    public <E extends Enum<E>> E leerEnum(String clave, Class<E> tipoEnum) {
        String texto = leerTexto(clave).trim();
        try {
            return Enum.valueOf(tipoEnum, texto);
        } catch (IllegalArgumentException e) {
            throw new ExcepcionPersistenciaPartida("Valor de enumeracion invalido en: " + clave, e);
        }
    }

    public String exportarTexto() {
        StringWriter writer = new StringWriter();
        try {
            propiedades.store(writer, null);
        } catch (IOException e) {
            throw new ExcepcionPersistenciaPartida("No se pudo exportar el diccionario de partida", e);
        }
        return Arrays.stream(writer.toString().split("\\R"))
                .filter(linea -> !linea.startsWith("#") && !linea.isBlank())
                .sorted()
                .collect(Collectors.joining("\n")) + "\n";
    }
}
