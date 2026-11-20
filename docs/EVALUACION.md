<!-- evaluacion:inicio -->
# Evaluación — centuriol/Proyecto_final_programacion2

> Documento **generado automáticamente** por la cátedra de Programación II (RepoSense + análisis de calidad POO/SOLID). **No editar a mano**: se regenera en cada evaluación.
> Rama evaluada: `main`. Análisis **heurístico** (regex + métricas): es una criba para orientar la revisión, **no una nota**.
> Generado: 2026-11-20.

## 1. Veredicto

| Aspecto | Resultado |
|---|---|
| Índice de calidad POO/SOLID | **77/100** |
| Código (LOC) | 24,028 |
| Archivos de código | 369 |
| Clases (bien usadas) | 429 (256 OK) |
| Código dentro de clases (%POO) | 100% |
| Funciones fuera de clases | 0 |
| Duplicación lógica | 0.5% |
| Señales S/O/L/I/D | 28/0/0/0/101 |

> Este informe **describe** lo que el análisis midió con los criterios ya establecidos de la materia. No es una nota.

### Semáforo de aprobación

Criterios de `CRITERIOS-CALIDAD.md`. **Aprobar** se juega con el perfil `estandar`; **destacar**, con el `exigente` (meta de excelencia).

| Semáforo | Resultado |
|---|---|
| Aprobar (`estandar`) | **no cumple** — 3/4 |
| Destacar (`exigente`) | **no cumple** — 4/5 |

**Criterios para aprobar**

| Criterio | Requisito | Tu resultado | Cumple |
|---|---|---|---|
| Código dentro de clases (%POO) | ≥ 85% | 100% | sí |
| Duplicación lógica | ≤ 10% | 0.5% | sí |
| Funciones fuera de clases | ≤ 1 | 0 | sí |
| Señales POO/SOLID críticas | ninguna | Baja cohesión (LCOM), Acoplamiento alto (CBO) | no |

**Criterios para destacar**

| Criterio | Requisito | Tu resultado | Cumple |
|---|---|---|---|
| Código dentro de clases (%POO) | ≥ 95% | 100% | sí |
| Duplicación lógica | ≤ 5% | 0.5% | sí |
| Funciones fuera de clases | ≤ 1 | 0 | sí |
| Señales POO/SOLID críticas | ninguna | Clase demasiado grande (SRP), Baja cohesión (LCOM), Complejidad alta, Acoplamiento alto (CBO) | no |
| Índice de calidad POO/SOLID | ≥ 60 | 77/100 | sí |

### Cómo se calcula el %POO (y por qué no sube solo)

`%POO = líneas de lógica dentro de clases ÷ líneas de lógica totales`.

Se cuentan las líneas de **lógica y campos**; no cuentan `package`/`import`/`using`/anotaciones ni comentarios, Javadoc o docstrings. Es una **proporción**: si agregás código nuevo dentro de una clase, el numerador y el denominador crecen juntos y el porcentaje casi no cambia.

Sube cuando el código que hoy está **fuera** de clases pasa a ser método de una clase (o desaparece): funciones sueltas, `main`, constantes de módulo, scripts y lógica de archivos sueltos. También importa que ese trabajo esté **integrado en `main`**: la evaluación siempre mide `main`, así que lo que sigue en otra rama no se refleja todavía.

## 2. Autoría (RepoSense)

| Autor | Commits | Código (LOC) | Tests (LOC) | Docs (LOC) | Días activos | % del código |
|---|---:|---:|---:|---:|---:|---:|
| minibautista14-tech | 15 | 12,635 | 3,639 | 153 | 12 | 61% |
| mateovarelac | 11 | 8,201 | 3,928 | 0 | 5 | 39% |

> _El LOC de autoría lo calcula RepoSense sobre el historial y puede diferir del LOC de código del análisis de calidad (que mira solo el estado de `main`)._

> **Reparto del trabajo:** minibautista14-tech concentra el 61% de las líneas de código. Un reparto muy desigual suele indicar que la integración no fue realmente en equipo; vale la pena reconstruir cómo se coordinó.

## 3. Fallas detectadas

Reúne lo que el análisis marcó y lo que todavía falta para cada semáforo. Cuando la señal se puede asociar a archivos, se indican para orientar la revisión. No es una nota.

| Falla | Tu resultado | Requisito | Archivos |
|---|---|---|---|
| Señales POO/SOLID críticas | Baja cohesión (LCOM), Acoplamiento alto (CBO) | Aprobar: ninguna<br>Destacar: ninguna | `app/src/main/java/org/example/App.java`, `app/src/main/java/org/example/EnsambladorAplicacion.java`, `app/src/main/java/org/example/game/civilizacion/CivilizacionComponent.java`, `app/src/main/java/org/example/game/colonizacion/GestorColonizacion.java`, `app/src/main/java/org/example/game/colonizacion/ServicioPoblacionPlaneta.java`, `app/src/main/java/org/example/game/controlador/ConfiguradorAtajosSesion.java` (+99 más) |
| Métodos/funciones largos o con muchos parámetros | **95** método(s)/función(es) largos o con muchos parámetros | Sin señales | `app/src/main/java/org/example/game/civilizacion/CondicionesHabitabilidad.java`, `app/src/main/java/org/example/game/colonizacion/GestorColonizacion.java`, `app/src/main/java/org/example/game/controlador/CombinacionTeclas.java`, `app/src/main/java/org/example/game/controlador/SeleccionadorCuerpo.java`, `app/src/main/java/org/example/game/controlador/SeleccionadorCuerpoMouse.java`, `app/src/main/java/org/example/game/cuerpo/AgujeroNegro.java` (+68 más) |

## 4. Aspectos evaluados

Cada aspecto indica **qué mide**, **por qué importa** y **tu resultado**. Cuando el análisis reportó una señal, se agregan preguntas para **investigar la causa** (no hay recetas: entender el porqué es parte de la evaluación).

### 1. Índice de calidad POO/SOLID (indice)

- **Qué mide:** Un resumen numérico (0-100) que combina la riqueza de clases bien usadas con la penalización por señales POO/SOLID y duplicación, y luego aplica topes duros.
- **Por qué importa:** Ordena y prioriza la revisión: un índice bajo indica varias señales que conviene mirar juntas.
- **Tu resultado:** índice **77/100**

### 2. Duplicación lógica (DRY) (duplicacion) — sin señales

- **Qué mide:** Porcentaje de bloques de LÓGICA repetidos. Quedan fuera imports/includes, anotaciones, declaraciones de campos y comentarios/Javadoc/docstrings: esos bloques son boilerplate, no lógica.
- **Por qué importa:** Cada copia de lógica se mantiene por separado: un arreglo hay que repetirlo y los defectos se multiplican.
- **Tu resultado:** **0.5%** de duplicación lógica

### 3. Adopción de POO (% de código en clases) (poo) — sin señales

- **Qué mide:** Proporción de las **líneas de lógica** que viven dentro de una clase. Quedan fuera `package`/`import`/`using`/anotaciones y comentarios/Javadoc/docstrings: no son código que pueda estar dentro de una clase y, si contaran, el porcentaje quedaría topado por el boilerplate de cada archivo.
- **Por qué importa:** El diseño orientado a objetos es el eje de la materia: el comportamiento debería vivir como método de una clase.
- **Tu resultado:** **99.7%** del código dentro de clases
- **Cómo leerlo:** Es una **proporción**, no un acumulado: agregar código dentro de una clase no la sube por sí solo (numerador y denominador crecen juntos). Sube cuando el código que está **fuera** de clases (funciones sueltas, `main`, constantes de módulo, scripts) pasa a ser método de una clase o deja de existir. La medición se hace sobre `main`: el trabajo que todavía vive en otra rama no se refleja hasta que se integra.

### 4. Funciones fuera de clases (sueltas) — sin señales

- **Qué mide:** Funciones de nivel de módulo que no pertenecen a ninguna clase (en esta materia el comportamiento debe ser método de una clase).
- **Por qué importa:** Son la forma más directa de código procedural en un proyecto que debería ser orientado a objetos.
- **Tu resultado:** **0** función(es) fuera de clases

### 5. Clase demasiado grande (SRP) (god) — con señales

- **Qué mide:** Clases con demasiadas líneas o métodos de LÓGICA. No cuentan los getters/setters triviales ni la cantidad de campos por sí sola.
- **Por qué importa:** Una clase que hace de todo cambia por muchos motivos y cuesta entenderla y probarla.
- **Tu resultado:** **3** clase(s) demasiado grande(s)
- **Para investigar la causa:**
    - ¿La clase creció por acumulación de responsabilidades?
    - ¿Qué partes de la clase se usan siempre juntas y cuáles no?
    - ¿Se podría describir su responsabilidad en una sola frase?

### 6. Baja cohesión (LCOM) (cohesion) — con señales

- **Qué mide:** Métodos y campos de una clase que casi no se relacionan entre sí.
- **Por qué importa:** Una unidad que cambia por motivos distintos y comparte poca información suele esconder más de una responsabilidad.
- **Tu resultado:** **24** clase(s) con baja cohesión
- **Para investigar la causa:**
    - ¿Qué métodos de la clase no comparten estado con el resto?
    - ¿Cambian por motivos distintos según la tarea?
    - ¿Se pensó su responsabilidad antes de escribirla, o se fue agregando?

### 7. Complejidad alta (complejidad) — con señales

- **Qué mide:** Complejidad ciclomática: cantidad de caminos posibles dentro de un método o función (condicionales, bucles, casos).
- **Por qué importa:** Muchos caminos = difícil de seguir y de probar; suelen esconderse casos sin cubrir.
- **Tu resultado:** **1** método(s)/función(es) con complejidad alta
- **Para investigar la causa:**
    - ¿Se agregaron condiciones a medida que aparecían casos nuevos?
    - ¿Se probaron todos los caminos o solo el caso principal?
    - ¿Hay reglas de negocio mezcladas con detalles técnicos?

### 8. Métodos/funciones largos o con muchos parámetros (tamano) — con señales

- **Qué mide:** Tamaño de los métodos/funciones y cantidad de parámetros.
- **Por qué importa:** Una función que hace varias cosas cuesta leerla, nombrarla y reutilizarla.
- **Tu resultado:** **95** método(s)/función(es) largos o con muchos parámetros
- **Para investigar la causa:**
    - ¿La función creció por acumulación de pasos?
    - ¿Agrupa operaciones que podrían describirse por separado?
    - ¿Los parámetros representan una misma idea (un objeto) o cosas sueltas?

### 9. Acoplamiento alto (CBO) (acoplamiento) — con señales

- **Qué mide:** Cuánto depende una clase de otros tipos del proyecto. No cuentan los tipos de la plataforma (biblioteca estándar, DOM, motor Godot) ni los métodos/propiedades propias.
- **Por qué importa:** Piezas muy enlazadas hacen que un cambio se propague y que sea difícil reemplazar partes.
- **Tu resultado:** **101** clase(s) muy acoplada(s)
- **Para investigar la causa:**
    - ¿La clase conoce detalles internos de muchas otras?
    - ¿Se puede cambiar una parte sin tocar las demás?
    - ¿Los colaboradores llegan como parámetros o se crean por dentro?

### 10. Herencia profunda (DIT) (herencia) — sin señales

- **Qué mide:** Profundidad de la cadena de herencia de una clase.
- **Por qué importa:** Jerarquías muy profundas vuelven difícil saber de dónde viene cada comportamiento.
- **Tu resultado:** **0** clase(s) con herencia profunda

### 11. Switch sobre tipos (OCP) (ocp) — sin señales

- **Qué mide:** Cadenas de dispatch por tipo (`else if ... is` / `else if ... instanceof`, `switch` sobre tipo o `typeof`). Un `is Tipo variable` aislado (pattern matching / cast seguro) NO cuenta como switch.
- **Por qué importa:** Cada tipo nuevo obliga a tocar varios lugares, en vez de extender con una implementación nueva.
- **Tu resultado:** **0** clase(s) con switch sobre tipos

### 12. Interfaz con demasiados métodos (ISP) (isp) — sin señales

- **Qué mide:** Interfaces o clases abstractas con muchos métodos declarados.
- **Por qué importa:** Obliga a implementar operaciones que no se usan y acopla a los clientes a métodos que no necesitan.
- **Tu resultado:** **0** interfaz(es) con demasiados métodos

### 13. Override vacío o 'no implementado' (LSP) (lsp) — sin señales

- **Qué mide:** Métodos sobrescritos que quedan vacíos o lanzan 'no implementado'.
- **Por qué importa:** Suele indicar que la jerarquía no representa bien el problema y rompe la sustitución entre clases.
- **Tu resultado:** **0** override(s) vacío(s) o 'no implementado'

### 14. Clase sin métodos (solo datos) (data) — sin señales

- **Qué mide:** Clases que solo acumulan campos y no tienen comportamiento.
- **Por qué importa:** Separar datos de comportamiento reparte por el proyecto la lógica que debería estar cerca de esos datos.
- **Tu resultado:** **0** clase(s) sin métodos (solo datos)

### 15. Campos mutables públicos (encapsulación) (encapsulacion) — sin señales

- **Qué mide:** Clases que exponen estado **modificable** desde afuera. No cuentan constantes (`static final`, `const`), campos `final`/`readonly`, propiedades `val` de Kotlin ni propiedades con `private set`.
- **Por qué importa:** Expone el estado interno y hace que cualquier cambio dependa de quién lo modifica desde afuera.
- **Tu resultado:** **0** clase(s) con campos mutables públicos

### 16. Clase solo con métodos estáticos (utilitaria) (static) — sin señales

- **Qué mide:** Clases donde todos los métodos son estáticos.
- **Por qué importa:** Es comportamiento procedural disfrazado de clase: no se instancia ni participa del modelo de objetos.
- **Tu resultado:** **0** clase(s) solo con métodos estáticos

### Archivos donde se originan las señales

> Se listan **224** hallazgo(s) localizados en **156** archivo(s). La columna `Línea` es la línea del archivo donde el análisis detectó la señal; `—` indica una señal de nivel repositorio (sin archivo propio).

| Aspecto | Archivo | Línea | Hallazgo |
|---|---|---:|---|
| Clase demasiado grande (SRP) | `app/src/main/java/org/example/game/civilizacion/CivilizacionComponent.java` | 15 | clase 'CivilizacionComponent' grande (LOC lógico=103, métodos=19, campos=6, accesores=10 (no cuentan)) |
| Clase demasiado grande (SRP) | `app/src/main/java/org/example/game/progresion/SistemaProgresion.java` | 14 | clase 'SistemaProgresion' grande (LOC lógico=100, métodos=37, campos=3, accesores=6 (no cuentan)) |
| Clase demasiado grande (SRP) | `app/src/test/java/org/example/TransformacionCuerposTest.java` | 15 | clase 'TransformacionCuerposTest' grande (LOC lógico=219, métodos=12, campos=1) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/colonizacion/GestorColonizacion.java` | 19 | clase 'GestorColonizacion' con baja cohesión (34%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/efectos/GestorEfectos.java` | 17 | clase 'GestorEfectos' con baja cohesión (47%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/guardado/PoliticaRanurasGuardado.java` | 13 | clase 'PoliticaRanurasGuardado' con baja cohesión (33%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/jugador/InventarioJugador.java` | 13 | clase 'InventarioJugador' con baja cohesión (38%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/motor/AsistenteOrbital.java` | 10 | clase 'AsistenteOrbital' con baja cohesión (49%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/motor/PipelineFisicaPermisiva.java` | 19 | clase 'PipelineFisicaPermisiva' con baja cohesión (29%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/motor/ResolvedorColisiones.java` | 17 | clase 'ResolvedorColisiones' con baja cohesión (49%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/progresion/MejoraSecundaria.java` | 13 | clase 'MejoraSecundaria' con baja cohesión (11%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/render/DesplazamientoCamara.java` | 9 | clase 'DesplazamientoCamara' con baja cohesión (47%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/render/EscenaFondoMenu.java` | 19 | clase 'EscenaFondoMenu' con baja cohesión (28%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/render/GestorImagenes.java` | 14 | clase 'GestorImagenes' con baja cohesión (35%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/render/RegistroEstrategiasVisuales.java` | 14 | clase 'RegistroEstrategiasVisuales' con baja cohesión (33%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/render/RenderizadorPixelArt.java` | 18 | clase 'RenderizadorPixelArt' con baja cohesión (35%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/simulacion/BusEventos.java` | 11 | clase 'BusEventos' con baja cohesión (31%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/simulacion/GeneradorEventosCosmicos.java` | 13 | clase 'GeneradorEventosCosmicos' con baja cohesión (40%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/simulacion/GestorCicloVidaCuerpos.java` | 17 | clase 'GestorCicloVidaCuerpos' con baja cohesión (42%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/ui/BarraInventarioHotbar.java` | 20 | clase 'BarraInventarioHotbar' con baja cohesión (42%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/ui/CapaIndicadoresSol.java` | 25 | clase 'CapaIndicadoresSol' con baja cohesión (33%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/ui/ControlTiempoWidget.java` | 17 | clase 'ControlTiempoWidget' con baja cohesión (36%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/ui/LienzoArbolOrbital.java` | 22 | clase 'LienzoArbolOrbital' con baja cohesión (17%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/ui/PantallaGameOver.java` | 19 | clase 'PantallaGameOver' con baja cohesión (43%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/ui/PantallaMenuPausa.java` | 16 | clase 'PantallaMenuPausa' con baja cohesión (43%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/ui/PantallaMenuPrincipal.java` | 21 | clase 'PantallaMenuPrincipal' con baja cohesión (38%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/ui/SeccionInfoCuerpo.java` | 26 | clase 'SeccionInfoCuerpo' con baja cohesión (38%) |
| Complejidad alta | `app/src/main/java/org/example/game/progresion/MejoraSecundaria.java` | 234 | método 'MejoraSecundaria.formatearEfecto' con complejidad 10 |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/civilizacion/CondicionesHabitabilidad.java` | 6 | método 'CondicionesHabitabilidad.CondicionesHabitabilidad' con 9 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/colonizacion/GestorColonizacion.java` | 89 | método 'GestorColonizacion.lanzar' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/controlador/CombinacionTeclas.java` | 9 | método 'CombinacionTeclas.CombinacionTeclas' de 47 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/controlador/SeleccionadorCuerpo.java` | 10 | método 'SeleccionadorCuerpo.encontrarCuerpo' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/controlador/SeleccionadorCuerpoMouse.java` | 26 | método 'SeleccionadorCuerpoMouse.encontrarCuerpo' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/AgujeroNegro.java` | 9 | método 'AgujeroNegro.AgujeroNegro' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/CreadorAgujerosNegros.java` | 9 | método 'CreadorAgujerosNegros.crearAgujeroNegro' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/CreadorAstrosMenoresYEstrellas.java` | 12 | método 'CreadorAstrosMenoresYEstrellas.crearLunaPersonalizada' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/CreadorPlanetas.java` | 9 | método 'CreadorPlanetas.crearPlaneta' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/CreadorPlanetas.java` | 10 | método 'CreadorPlanetas.crearPlanetaPersonalizado' con 6 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/CuerpoCeleste.java` | 17 | método 'CuerpoCeleste.CuerpoCeleste' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/CuerpoCelesteFactory.java` | 28 | método 'CuerpoCelesteFactory.crearCuerpo' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/CuerpoCelesteFactory.java` | 33 | método 'CuerpoCelesteFactory.crearPlaneta' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/CuerpoCelesteFactory.java` | 37 | método 'CuerpoCelesteFactory.crearPlanetaPersonalizado' con 6 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/CuerpoCelesteFactory.java` | 51 | método 'CuerpoCelesteFactory.crearLunaPersonalizada' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/CuerpoCelesteFactory.java` | 65 | método 'CuerpoCelesteFactory.crearAgujeroNegro' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/DescriptorTipoCuerpo.java` | 12 | método 'DescriptorTipoCuerpo.DescriptorTipoCuerpo' con 11 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/FabricaCuerpos.java` | 15 | método 'FabricaCuerpos.crearCuerpo' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/FabricaMeteoritos.java` | 28 | método 'FabricaMeteoritos.crearHaciaObjetivo' de 35 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/FabricaPlanetas.java` | 19 | método 'FabricaPlanetas.crearPlaneta' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/FabricaPlanetas.java` | 27 | método 'FabricaPlanetas.crearPlanetaPersonalizado' con 6 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/Planeta.java` | 13 | método 'Planeta.Planeta' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/RegistroCreadoresCuerpo.java` | 42 | método 'RegistroCreadoresCuerpo.crear' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/guardado/DatosCivilizacion.java` | 7 | método 'DatosCivilizacion.DatosCivilizacion' con 6 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/guardado/DatosCuerpo.java` | 7 | método 'DatosCuerpo.DatosCuerpo' con 6 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/guardado/DatosPartida.java` | 11 | método 'DatosPartida.DatosPartida' con 6 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/guardado/DatosPartida.java` | 11 | método 'DatosPartida.DatosPartida' de 31 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/guardado/DetalleCuerpo.java` | 5 | método 'DetalleCuerpo.DetalleCuerpo' con 8 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/guardado/EstadoRelojGuardado.java` | 7 | método 'EstadoRelojGuardado.EstadoRelojGuardado' con 6 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/guardado/InfoRanuraGuardado.java` | 9 | método 'InfoRanuraGuardado.InfoRanuraGuardado' de 33 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/guardado/ServicioPersistenciaSlot.java` | 27 | método 'ServicioPersistenciaSlot.escribir' con 6 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/CalculadorGravedad.java` | 91 | método 'CalculadorGravedad.procesarGravedadPlanetaLuna' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/CalculadorGravedad.java` | 116 | método 'CalculadorGravedad.aplicarGravedadNewtoniana' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/CapturaOrbitalMenorPlaneta.java` | 49 | método 'CapturaOrbitalMenorPlaneta.ejecutarCaptura' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/CapturaOrbitalPlanetaAtractor.java` | 26 | método 'CapturaOrbitalPlanetaAtractor.ejecutarCaptura' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/EstrategiaCapturaOrbital.java` | 9 | método 'EstrategiaCapturaOrbital.ejecutarCaptura' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/IntegradorMovimiento.java` | 21 | método 'IntegradorMovimiento.integrar' de 33 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/Vector2D.java` | 7 | método 'Vector2D.Vector2D' de 50 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/colision/ContextoColision.java` | 12 | método 'ContextoColision.ContextoColision' con 7 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/colision/ReglaColisionAgujeroNegro.java` | 34 | método 'ReglaColisionAgujeroNegro.aplica' de 37 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/colision/ReglaColisionAgujeroNegro.java` | 79 | método 'ReglaColisionAgujeroNegro.resolver' de 38 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/colision/ReglaColisionFusionAguaLava.java` | 43 | método 'ReglaColisionFusionAguaLava.resolver' de 32 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/CapaEstrellasFondo.java` | 14 | método 'CapaEstrellasFondo.EstrellaFondo' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/CapaEstrellasFondo.java` | 14 | método 'EstrellaFondo.EstrellaFondo' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/CapaGrillaEspacial.java` | 27 | método 'CapaGrillaEspacial.dibujarLineasCeldas' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/DespachadorRenderCuerpo.java` | 101 | método 'DespachadorRenderCuerpo.dibujarNombre' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/RenderizadorCuerpo.java` | 14 | método 'RenderizadorCuerpo.dibujar' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/UtilidadRender.java` | 38 | método 'UtilidadRender.discoConContorno' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/UtilidadRender.java` | 48 | método 'UtilidadRender.dibujarDiscoConContorno' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/capa/CapaEscudoPlanetario.java` | 27 | método 'CapaEscudoPlanetario.dibujarCupula' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/capa/CapaGhostPreview.java` | 57 | método 'CapaGhostPreview.dibujarSpriteFantasma' con 6 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/capa/CapaGhostPreview.java` | 80 | método 'CapaGhostPreview.dibujarAreaColapsoYReferencias' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/capa/CapaGhostPreview.java` | 99 | método 'CapaGhostPreview.dibujarTagInformacion' con 7 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/cuerpo/RenderizadorAgujeroNegro.java` | 15 | método 'RenderizadorAgujeroNegro.dibujar' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/cuerpo/RenderizadorEstrella.java` | 21 | método 'RenderizadorEstrella.dibujar' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/cuerpo/RenderizadorLuna.java` | 21 | método 'RenderizadorLuna.dibujar' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/cuerpo/RenderizadorMeteorito.java` | 15 | método 'RenderizadorMeteorito.dibujar' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/cuerpo/RenderizadorPlaneta.java` | 41 | método 'RenderizadorPlaneta.dibujar' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/cuerpo/RenderizadorSatelite.java` | 14 | método 'RenderizadorSatelite.dibujar' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/simulacion/GestorCicloVidaCuerpos.java` | 28 | método 'GestorCicloVidaCuerpos.ServiciosSpawn' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/simulacion/GestorCicloVidaCuerpos.java` | 28 | método 'ServiciosSpawn.ServiciosSpawn' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/simulacion/GestorCicloVidaCuerpos.java` | 36 | método 'GestorCicloVidaCuerpos.validar' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/simulacion/GestorCicloVidaCuerpos.java` | 36 | método 'ServiciosSpawn.validar' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/simulacion/GestorCicloVidaCuerpos.java` | 70 | método 'GestorCicloVidaCuerpos.spawnCuerpo' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/simulacion/GestorCicloVidaCuerpos.java` | 74 | método 'GestorCicloVidaCuerpos.spawnCuerpo' con 6 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/BarraResumenProgresion.java` | 36 | método 'BarraResumenProgresion.configurarPill' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/ConstructorOrbeNodoArbol.java` | 255 | método 'ConstructorOrbeNodoArbol.configurarPrincipalBloqueado' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/ConstructorPanelCaracteristicas.java` | 52 | método 'ConstructorPanelCaracteristicas.agregarFilaPropiedad' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/ConstructorTarjetasInspeccion.java` | 61 | método 'ConstructorTarjetasInspeccion.construirTarjetaPoblar' de 33 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/FormateadorHotbar.java` | 42 | método 'FormateadorHotbar.formatearMapaRecursos' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/InspectorTecnologiaArbol.java` | 47 | método 'InspectorTecnologiaArbol.InspectorTecnologiaArbol' de 37 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/PantallaArbolProgresion.java` | 23 | método 'NodoArbol.NodoArbol' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/PantallaArbolProgresion.java` | 23 | método 'PantallaArbolProgresion.NodoArbol' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/ProveedorInfoCuerpo.java` | 18 | método 'ProveedorInfoCuerpo.agregarFilaPropiedad' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/RenderizadorGeometriaArbol.java` | 82 | método 'RenderizadorGeometriaArbol.crearDendritaCurva' con 6 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/SeccionInfoCuerpo.java` | 103 | método 'SeccionInfoCuerpo.agregarFilaPropiedad' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/SlotHotbar.java` | 33 | método 'SlotHotbar.SlotHotbar' de 37 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/VentanaColocacion.java` | 45 | método 'VentanaColocacion.VentanaColocacion' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/VentanaColocacion.java` | 96 | método 'VentanaColocacion.construirFormulario' de 36 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/WidgetFactorMasa.java` | 31 | método 'WidgetFactorMasa.WidgetFactorMasa' de 34 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/CivilizacionSinergiaTest.java` | 26 | método 'CivilizacionSinergiaTest.testGeneracionRecursosPorCuerpoYSlowGrowth' de 44 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/CorreccionesMeteoritosAutoguardadoEconomiaTest.java` | 74 | método 'CorreccionesMeteoritosAutoguardadoEconomiaTest.testEscombrosDePlanetasDestruidosRepartenMasaRespetandoTopes' de 33 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/CostosProporcionalesYSatelitesMenuTest.java` | 28 | método 'CostosProporcionalesYSatelitesMenuTest.testTablaCostosOrdenCoherenteYProporcionalALaProduccion' de 36 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/CostosProporcionalesYSatelitesMenuTest.java` | 157 | método 'CostosProporcionalesYSatelitesMenuTest.testClicEnSateliteDelMenuIniciaCaidaConGravedadPromedioHastaSalirDePantalla' de 35 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/EasterEggFusionYRadiosTest.java` | 51 | método 'EasterEggFusionYRadiosTest.testEasterEggColisionAguaYLavaFormaPlanetaPorroInerte' de 37 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/EconomiaRecursosTest.java` | 92 | método 'EconomiaRecursosTest.testEspecificacionMaterialesParaPonerYPorTick' de 36 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/EventosCosmicosYSatelitesTest.java` | 193 | método 'EventosCosmicosYSatelitesTest.testLunaNoEsInterrumpidaPorSatelitesAlEntrarEnOrbitaNiAlOrbitarPlanetaEnMovimiento' de 56 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/GuardadoYCargaSimulacionTest.java` | 31 | método 'GuardadoYCargaSimulacionTest.testGuardarYCargarRestauraSistemaSolarCompletoYRelacionesOrbitales' de 82 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/ProgresionMejorasSecundariasTest.java` | 68 | método 'ProgresionMejorasSecundariasTest.testMultiplicadoresMejorasSecundariasEnEconomiaEInventario' de 45 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/ProgresionYArbolTest.java` | 31 | método 'ProgresionYArbolTest.testArbolTecnologicoTresRamasYCostosEscalonados' de 43 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/ReembolsoYColocacionTest.java` | 148 | método 'ReembolsoYColocacionTest.testNuevosItemsPlanetaAguaYPlanetaLavaYSolX2' de 37 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/RepositorioYSerializacionPartidaTest.java` | 145 | método 'RepositorioYSerializacionPartidaTest.construirPartidaEjemplo' de 41 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/TransformacionCuerposTest.java` | 252 | método 'TransformacionCuerposTest.testPlanetaPorroColocadoMuyCercaDelSolSeConvierteEnPlanetaLavaInerteSinGenerarRecursos' de 39 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/UIWidgetsYFormatoTest.java` | 118 | método 'UIWidgetsYFormatoTest.testControlTiempoWidgetCompactoYVelocidades' de 34 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/game/ui/ConstructorFichaInvestigacionTest.java` | 30 | método 'ConstructorFichaInvestigacionTest.construyeFichaInvestigadaDisponibleYBloqueada' de 39 líneas |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/App.java` | 22 | clase 'App' muy acoplada (CBO=20) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/EnsambladorAplicacion.java` | 19 | clase 'EnsambladorAplicacion' muy acoplada (CBO=28) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/EnsambladorAplicacion.java` | 21 | clase 'ComponentesApp' muy acoplada (CBO=20) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/civilizacion/CivilizacionComponent.java` | 15 | clase 'CivilizacionComponent' muy acoplada (CBO=11) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/colonizacion/GestorColonizacion.java` | 19 | clase 'GestorColonizacion' muy acoplada (CBO=12) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/colonizacion/ServicioPoblacionPlaneta.java` | 21 | clase 'ServicioPoblacionPlaneta' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/controlador/ConfiguradorAtajosSesion.java` | 14 | clase 'ConfiguradorAtajosSesion' muy acoplada (CBO=13) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/controlador/ConfiguradorAtajosTeclado.java` | 12 | clase 'ConfiguradorAtajosTeclado' muy acoplada (CBO=16) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/controlador/ControladorMouse.java` | 21 | clase 'ControladorMouse' muy acoplada (CBO=14) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/controlador/ControladorMouse.java` | 25 | clase 'EstadoMouse' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/controlador/ControladorTeclado.java` | 20 | clase 'ControladorTeclado' muy acoplada (CBO=11) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/controlador/CoordinadorSesionJuego.java` | 18 | clase 'CoordinadorSesionJuego' muy acoplada (CBO=9) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/controlador/ManejadorClicksCanvas.java` | 18 | clase 'ManejadorClicksCanvas' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/cuerpo/CuerpoCelesteFactory.java` | 12 | clase 'CuerpoCelesteFactory' muy acoplada (CBO=14) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/cuerpo/RegistroCapacidades.java` | 16 | clase 'RegistroCapacidades' muy acoplada (CBO=12) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/cuerpo/RegistroCreadoresCuerpo.java` | 13 | clase 'RegistroCreadoresCuerpo' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/guardado/CodificadorEntidadesPartida.java` | 9 | clase 'CodificadorEntidadesPartida' muy acoplada (CBO=9) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/guardado/DiccionarioPropiedades.java` | 11 | clase 'DiccionarioPropiedades' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/guardado/ExtractorDatosCuerpo.java` | 11 | clase 'ExtractorDatosCuerpo' muy acoplada (CBO=14) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/guardado/FabricaGuardado.java` | 8 | clase 'FabricaGuardado' muy acoplada (CBO=15) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/guardado/GestorGuardadoPartida.java` | 15 | clase 'GestorGuardadoPartida' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/guardado/MapeadorEstadoSimulacion.java` | 21 | clase 'MapeadorEstadoSimulacion' muy acoplada (CBO=21) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/guardado/ReconstructorCuerpos.java` | 16 | clase 'ReconstructorCuerpos' muy acoplada (CBO=19) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/guardado/RepositorioPartidasArchivo.java` | 15 | clase 'RepositorioPartidasArchivo' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/guardado/SerializadorPartidaTexto.java` | 16 | clase 'SerializadorPartidaTexto' muy acoplada (CBO=15) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/motor/ActualizadorProcesosTransformacion.java` | 16 | clase 'ActualizadorProcesosTransformacion' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/motor/AsistenteOrbital.java` | 10 | clase 'AsistenteOrbital' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/motor/FabricaMotorFisica.java` | 9 | clase 'FabricaMotorFisica' muy acoplada (CBO=32) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/motor/MotorFisicaPermisiva.java` | 13 | clase 'MotorFisicaPermisiva' muy acoplada (CBO=14) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/motor/MotorReglasColision.java` | 20 | clase 'MotorReglasColision' muy acoplada (CBO=9) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/motor/PipelineFisicaPermisiva.java` | 19 | clase 'PipelineFisicaPermisiva' muy acoplada (CBO=16) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/motor/PipelineFisicaPermisiva.java` | 41 | clase 'ComponentesFisica' muy acoplada (CBO=11) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/motor/ResolvedorColisiones.java` | 17 | clase 'ResolvedorColisiones' muy acoplada (CBO=12) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/motor/TransformadorCuerpos.java` | 13 | clase 'TransformadorCuerpos' muy acoplada (CBO=11) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/motor/colision/ReglaColisionAgujeroNegro.java` | 15 | clase 'ReglaColisionAgujeroNegro' muy acoplada (CBO=13) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/motor/colision/ReglaColisionEscudo.java` | 16 | clase 'ReglaColisionEscudo' muy acoplada (CBO=12) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/motor/colision/ReglaColisionEstrella.java` | 14 | clase 'ReglaColisionEstrella' muy acoplada (CBO=11) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/progresion/RegistroMejorasSecundarias.java` | 16 | clase 'RegistroMejorasSecundarias' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/progresion/SistemaProgresion.java` | 14 | clase 'SistemaProgresion' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/render/CapasVisualesSimulacion.java` | 18 | clase 'CapasVisualesSimulacion' muy acoplada (CBO=21) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/render/CapasVisualesSimulacion.java` | 22 | clase 'ConjuntoCapas' muy acoplada (CBO=12) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/render/DespachadorRenderCuerpo.java` | 19 | clase 'DespachadorRenderCuerpo' muy acoplada (CBO=14) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/render/EscenaFondoMenu.java` | 19 | clase 'EscenaFondoMenu' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/render/FabricaRender.java` | 10 | clase 'FabricaRender' muy acoplada (CBO=14) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/render/GestorImagenes.java` | 14 | clase 'GestorImagenes' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/render/RegistroEstrategiasVisuales.java` | 14 | clase 'RegistroEstrategiasVisuales' muy acoplada (CBO=9) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/render/RenderizadorFondo.java` | 10 | clase 'RenderizadorFondo' muy acoplada (CBO=9) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/render/RenderizadorPixelArt.java` | 18 | clase 'RenderizadorPixelArt' muy acoplada (CBO=19) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/simulacion/CoordinadorPasoSimulacion.java` | 20 | clase 'CoordinadorPasoSimulacion' muy acoplada (CBO=14) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/simulacion/FabricaSimulacion.java` | 14 | clase 'FabricaSimulacion' muy acoplada (CBO=12) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/simulacion/GestorCicloVidaCuerpos.java` | 17 | clase 'GestorCicloVidaCuerpos' muy acoplada (CBO=11) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/simulacion/GestorColocacionCuerpos.java` | 14 | clase 'GestorColocacionCuerpos' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/simulacion/SimulacionSolar.java` | 17 | clase 'SimulacionSolar' muy acoplada (CBO=19) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/simulacion/SubsistemaSimulacion.java` | 23 | clase 'SubsistemaSimulacion' muy acoplada (CBO=32) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/BarraInventarioHotbar.java` | 20 | clase 'BarraInventarioHotbar' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/ConstructorOrbeNodoArbol.java` | 26 | clase 'ConstructorOrbeNodoArbol' muy acoplada (CBO=9) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/ConstructorOverlayUI.java` | 17 | clase 'ConstructorOverlayUI' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/ConstructorTarjetasInspeccion.java` | 28 | clase 'ConstructorTarjetasInspeccion' muy acoplada (CBO=14) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/ControlTiempoWidget.java` | 17 | clase 'ControlTiempoWidget' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/InspectorTecnologiaArbol.java` | 28 | clase 'InspectorTecnologiaArbol' muy acoplada (CBO=12) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/LienzoArbolOrbital.java` | 22 | clase 'LienzoArbolOrbital' muy acoplada (CBO=12) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/PantallaArbolProgresion.java` | 21 | clase 'PantallaArbolProgresion' muy acoplada (CBO=13) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/PantallaGameOver.java` | 19 | clase 'PantallaGameOver' muy acoplada (CBO=11) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/PantallaMenuPausa.java` | 16 | clase 'PantallaMenuPausa' muy acoplada (CBO=11) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/PantallaMenuPrincipal.java` | 21 | clase 'PantallaMenuPrincipal' muy acoplada (CBO=17) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/SeccionInfoCuerpo.java` | 26 | clase 'SeccionInfoCuerpo' muy acoplada (CBO=13) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/SlotHotbar.java` | 21 | clase 'SlotHotbar' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/VentanaColocacion.java` | 27 | clase 'VentanaColocacion' muy acoplada (CBO=15) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/VentanaDescripcionCuerpo.java` | 13 | clase 'VentanaDescripcionCuerpo' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/VentanaInspeccion.java` | 24 | clase 'VentanaInspeccion' muy acoplada (CBO=13) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/VentanaProgresion.java` | 26 | clase 'VentanaProgresion' muy acoplada (CBO=11) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/AgujeroNegroFisicaTest.java` | 14 | clase 'AgujeroNegroFisicaTest' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/ArrastreCamaraYColocacionTest.java` | 25 | clase 'ArrastreCamaraYColocacionTest' muy acoplada (CBO=13) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/ColonizacionYEscudosTest.java` | 23 | clase 'ColonizacionYEscudosTest' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/ControladorMouseYHelpersTest.java` | 25 | clase 'ControladorMouseYHelpersTest' muy acoplada (CBO=11) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/ControladorTecladoYComandosTest.java` | 19 | clase 'ControladorTecladoYComandosTest' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/CorreccionesMeteoritosAutoguardadoEconomiaTest.java` | 49 | clase 'CorreccionesMeteoritosAutoguardadoEconomiaTest' muy acoplada (CBO=27) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/CostosProporcionalesYSatelitesMenuTest.java` | 25 | clase 'CostosProporcionalesYSatelitesMenuTest' muy acoplada (CBO=9) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/EasterEggFusionYRadiosTest.java` | 4 | clase 'EasterEggFusionYRadiosTest' muy acoplada (CBO=9) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/EventosCosmicosYSatelitesTest.java` | 4 | clase 'EventosCosmicosYSatelitesTest' muy acoplada (CBO=12) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/Fase2BugsTest.java` | 22 | clase 'Fase2BugsTest' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/FondoAnimadoMenuPrincipalTest.java` | 17 | clase 'FondoAnimadoMenuPrincipalTest' muy acoplada (CBO=9) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/GuardadoComandosYFlujoTest.java` | 24 | clase 'GuardadoComandosYFlujoTest' muy acoplada (CBO=15) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/GuardadoYCargaSimulacionTest.java` | 25 | clase 'GuardadoYCargaSimulacionTest' muy acoplada (CBO=15) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/MecanicasColisionYVidaTest.java` | 4 | clase 'MecanicasColisionYVidaTest' muy acoplada (CBO=11) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/MenuPausaYDisposicionUITest.java` | 22 | clase 'MenuPausaYDisposicionUITest' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/MenuPrincipalYDerrotaFlujoTest.java` | 19 | clase 'MenuPrincipalYDerrotaFlujoTest' muy acoplada (CBO=12) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/OrbitasYDinamicaTest.java` | 4 | clase 'OrbitasYDinamicaTest' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/ProgresionMejorasSecundariasTest.java` | 26 | clase 'ProgresionMejorasSecundariasTest' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/RanurasYAutoguardadoTest.java` | 18 | clase 'RanurasYAutoguardadoTest' muy acoplada (CBO=15) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/ReembolsoYColocacionTest.java` | 16 | clase 'ReembolsoYColocacionTest' muy acoplada (CBO=9) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/RefactorOopModularidadTest.java` | 22 | clase 'RefactorOopModularidadTest' muy acoplada (CBO=24) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/ReglasColisionTest.java` | 9 | clase 'ReglasColisionTest' muy acoplada (CBO=14) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/RendimientoRegresionTest.java` | 21 | clase 'RendimientoRegresionTest' muy acoplada (CBO=9) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/RepositorioYSerializacionPartidaTest.java` | 19 | clase 'RepositorioYSerializacionPartidaTest' muy acoplada (CBO=16) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/SolidAbstraccionesRefactorTest.java` | 27 | clase 'SolidAbstraccionesRefactorTest' muy acoplada (CBO=16) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/TransformacionCuerposTest.java` | 15 | clase 'TransformacionCuerposTest' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/UIWidgetsYFormatoTest.java` | 17 | clase 'UIWidgetsYFormatoTest' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/game/motor/colision/DespachoDobleColisionesTest.java` | 21 | clase 'DespachoDobleColisionesTest' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/game/render/capa/CapaNavesYEscudoTest.java` | 18 | clase 'CapaNavesYEscudoTest' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/game/ui/FabricaPantallasModalesTest.java` | 16 | clase 'FabricaPantallasModalesTest' muy acoplada (CBO=9) |

### Desglose de la columna S (SRP)

> La columna **S** no es un puntaje: cuenta **señales** de SRP. Aquí suma **28** entrada(s) sobre **27** clase(s) distinta(s).
> 1 clase(s) acumulan más de una señal: `MejoraSecundaria`.

| Señal | Entradas | Regla (perfil exigente) |
|---|---:|---|
| Clase grande | 3 | ≥ 200 LOC lógico, o ≥ 15 métodos con lógica y ≥ 100 LOC lógico. No cuentan getters/setters. |
| Cohesión baja | 24 | < 50 % de los pares de métodos comparten algún campo (con ≥ 4 métodos con lógica y ≥ 3 campos). |
| Complejidad alta | 1 | Algún método con complejidad ciclomática ≥ 10. |

**Clases grandes (3)**

- `TransformacionCuerposTest`: clase grande (LOC lógico=219, métodos=12, campos=1)
- `CivilizacionComponent`: clase grande (LOC lógico=103, métodos=19, campos=6, accesores=10 (no cuentan))
- `SistemaProgresion`: clase grande (LOC lógico=100, métodos=37, campos=3, accesores=6 (no cuentan))

**Clases con baja cohesión (24)**

- `MejoraSecundaria`: cohesión baja (11% de pares comparten campos)
- `LienzoArbolOrbital`: cohesión baja (17% de pares comparten campos)
- `EscenaFondoMenu`: cohesión baja (28% de pares comparten campos)
- `PipelineFisicaPermisiva`: cohesión baja (29% de pares comparten campos)
- `BusEventos`: cohesión baja (31% de pares comparten campos)
- `RegistroEstrategiasVisuales`: cohesión baja (33% de pares comparten campos)
- `PoliticaRanurasGuardado`: cohesión baja (33% de pares comparten campos)
- `CapaIndicadoresSol`: cohesión baja (33% de pares comparten campos)
- `GestorColonizacion`: cohesión baja (34% de pares comparten campos)
- `RenderizadorPixelArt`: cohesión baja (35% de pares comparten campos)
- `GestorImagenes`: cohesión baja (35% de pares comparten campos)
- `ControlTiempoWidget`: cohesión baja (36% de pares comparten campos)
- `SeccionInfoCuerpo`: cohesión baja (38% de pares comparten campos)
- `PantallaMenuPrincipal`: cohesión baja (38% de pares comparten campos)
- `InventarioJugador`: cohesión baja (38% de pares comparten campos)
- `GeneradorEventosCosmicos`: cohesión baja (40% de pares comparten campos)
- `GestorCicloVidaCuerpos`: cohesión baja (42% de pares comparten campos)
- `BarraInventarioHotbar`: cohesión baja (42% de pares comparten campos)
- `PantallaMenuPausa`: cohesión baja (43% de pares comparten campos)
- `PantallaGameOver`: cohesión baja (43% de pares comparten campos)
- `DesplazamientoCamara`: cohesión baja (47% de pares comparten campos)
- `GestorEfectos`: cohesión baja (47% de pares comparten campos)
- `AsistenteOrbital`: cohesión baja (49% de pares comparten campos)
- `ResolvedorColisiones`: cohesión baja (49% de pares comparten campos)

**Complejidad alta (1)**

- `MejoraSecundaria`: complejidad alta (máx=10)

> Los **95** hallazgo(s) de métodos largos o con muchos parámetros se listan en «Métodos/funciones largos o con muchos parámetros» y **no suman** a la columna S.

> El análisis no excluye tests: una clase de test que supere los umbrales también suma.

## 5. Evolución por commit

Métricas calculadas sobre el estado de `main` en **cada commit** (23 commits con trabajo), del primero al último. La calidad usa el perfil `exigente`; el aporte de código lo mide RepoSense. El índice puede **bajar** al crecer el proyecto: se acumulan señales.

```mermaid
xychart-beta
    title "Índice POO/SOLID a lo largo de los commits"
    x-axis ["f322f1e", "c2ce669", "9675cae", "3f34b49", "35383e8", "052c78e", "1b7118e", "1dacc07", "4ce6d34", "da0b4ed", "ae8da1f", "a8cddc8", "aad481c", "5fcf7a4", "d1dc1e2", "4dd77de", "90a21eb", "fc53423", "e02655f", "69c59c8", "37af155", "63ec24b", "51310da"]
    y-axis "Índice" 0 --> 100
    line [0, 0, 67, 65, 69, 67, 66, 70, 68, 67, 67, 67, 67, 67, 67, 67, 67, 67, 67, 67, 67, 67, 77]
```

| # | Fecha | Commit | Autor | LOC | Índice | %POO | Duplic. | Sueltas |
|---|---|---|---|---:|---:|---:|---:|---:|
| 1 | 2026-08-14 | `f322f1e` | minibautista14-tech | 0 | 0 | 0% | 0.0% | 0 |
| 2 | 2026-08-24 | `c2ce669` | dqmdz-etec | 0 | 0 | 0% | 0.0% | 0 |
| 3 | 2026-08-28 | `9675cae` | minibautista14-tech | 4,178 | 67 | 99% | 2.2% | 0 |
| 4 | 2026-09-07 | `3f34b49` | minibautista14-tech | 6,476 | 65 | 99% | 2.0% | 0 |
| 5 | 2026-09-25 | `35383e8` | mateovarelac | 7,073 | 69 | 100% | 1.9% | 0 |
| 6 | 2026-10-05 | `052c78e` | minibautista14-tech | 7,965 | 67 | 100% | 2.6% | 0 |
| 7 | 2026-10-19 | `1b7118e` | mateovarelac | 10,628 | 66 | 99% | 1.1% | 0 |
| 8 | 2026-10-30 | `1dacc07` | minibautista14-tech | 12,819 | 70 | 100% | 1.1% | 0 |
| 9 | 2026-10-31 | `4ce6d34` | mateovarelac | 20,666 | 68 | 100% | 0.8% | 0 |
| 10 | 2026-11-06 | `da0b4ed` | minibautista14-tech | 21,534 | 67 | 100% | 0.8% | 0 |
| 11 | 2026-11-07 | `ae8da1f` | dqmdz-etec | 21,534 | 67 | 100% | 0.8% | 0 |
| 12 | 2026-11-07 | `a8cddc8` | dqmdz-etec | 21,534 | 67 | 100% | 0.8% | 0 |
| 13 | 2026-11-07 | `aad481c` | dqmdz-etec | 21,534 | 67 | 100% | 0.8% | 0 |
| 14 | 2026-11-07 | `5fcf7a4` | dqmdz-etec | 21,534 | 67 | 100% | 0.8% | 0 |
| 15 | 2026-11-13 | `d1dc1e2` | dqmdz-etec | 21,534 | 67 | 100% | 0.8% | 0 |
| 16 | 2026-11-14 | `4dd77de` | dqmdz-etec | 21,534 | 67 | 100% | 0.8% | 0 |
| 17 | 2026-11-16 | `90a21eb` | dqmdz-etec | 21,534 | 67 | 100% | 0.8% | 0 |
| 18 | 2026-11-16 | `fc53423` | dqmdz-etec | 21,534 | 67 | 100% | 0.8% | 0 |
| 19 | 2026-11-17 | `e02655f` | dqmdz-etec | 21,534 | 67 | 100% | 0.8% | 0 |
| 20 | 2026-11-20 | `69c59c8` | dqmdz-etec | 21,534 | 67 | 100% | 0.8% | 0 |
| 21 | 2026-11-20 | `37af155` | dqmdz-etec | 21,534 | 67 | 100% | 0.8% | 0 |
| 22 | 2026-11-20 | `63ec24b` | dqmdz-etec | 21,534 | 67 | 100% | 0.8% | 0 |
| 23 | 2026-11-20 | `51310da` | dqmdz-etec | 24,028 | 77 | 100% | 0.5% | 0 |

<!-- actividad-clases:inicio -->
## Actividad en días de clase

> Período evaluado **2026-08-14 → 2026-11-20** · **23 clases** (lunes, viernes) · días de clase del calendario, extendidos hasta la fecha de corte.
>
> `Commits` = commits de trabajo (no-merge); `Merges` = merges de PR (también son push). Los días sin actividad dicen **sin actividad**. El cuadro principal usa sólo las fechas de clase del calendario oficial.
>
> **Aportes fuera del calendario:** cada alumno tiene un cuadro aparte con las fechas no lectivas con actividad, desde **2026-08-14** hasta **2026-11-20** (ventana de aportes). Esas fechas no forman parte del cuadro principal, pero se muestran para no perder trabajo hecho en fines de semana, feriados o después del período.
>
> Se recorre `git log --all` sobre un mirror (todas las ramas y `refs/pull/*`), con deduplicación por commit. Fecha = fecha de autor en UTC-3. Los commits locales no pusheados no se ven. Si el trabajo fue en pareja y el commit es de otra persona, figura a nombre del autor del commit; para reflejarlo, usar `Co-authored-by` o una nota manual.
>
> Sección informativa: no modifica el semáforo de aprobación.

### mateovarelac

**5/23 clases con actividad** · 5 commits · 2 merges

| Fecha | Marca | Repositorios | Commits / merges |
|---|:--:|---|--:|
| 2026-08-14 | No | sin actividad | 0 / 0 |
| 2026-08-21 | No | sin actividad | 0 / 0 |
| 2026-08-24 | No | sin actividad | 0 / 0 |
| 2026-08-28 | **Sí** | Proyecto_final_programacion2 | 1 / 0 |
| 2026-08-31 | No | sin actividad | 0 / 0 |
| 2026-09-04 | No | sin actividad | 0 / 0 |
| 2026-09-07 | No | sin actividad | 0 / 0 |
| 2026-09-14 | No | sin actividad | 0 / 0 |
| 2026-09-25 | **Sí** | Proyecto_final_programacion2 | 1 / 1 |
| 2026-09-28 | No | sin actividad | 0 / 0 |
| 2026-10-02 | No | sin actividad | 0 / 0 |
| 2026-10-05 | No | sin actividad | 0 / 0 |
| 2026-10-09 | No | sin actividad | 0 / 0 |
| 2026-10-16 | **Sí** | Proyecto_final_programacion2 | 1 / 0 |
| 2026-10-19 | **Sí** | Proyecto_final_programacion2 | 0 / 1 |
| 2026-10-23 | No | sin actividad | 0 / 0 |
| 2026-10-26 | No | sin actividad | 0 / 0 |
| 2026-10-30 | **Sí** | Proyecto_final_programacion2 | 2 / 0 |
| 2026-11-02 | No | sin actividad | 0 / 0 |
| 2026-11-06 | No | sin actividad | 0 / 0 |
| 2026-11-13 | No | sin actividad | 0 / 0 |
| 2026-11-16 | No | sin actividad | 0 / 0 |
| 2026-11-20 | No | sin actividad | 0 / 0 |

#### Aportes fuera del calendario de clases — mateovarelac

**mateovarelac — 1 fecha fuera del calendario** · 8 commits · 1 merge

| Alumno | Fecha | Repositorios | Commits / merges |
|---|---|---|---|
| mateovarelac (`mateovarelac`) | 2026-10-31 | Proyecto_final_programacion2 | 8 / 1 |

### minibautista14-tech

**13/23 clases con actividad** · 16 commits · 5 merges

| Fecha | Marca | Repositorios | Commits / merges |
|---|:--:|---|--:|
| 2026-08-14 | **Sí** | Proyecto_final_programacion2 | 1 / 0 |
| 2026-08-21 | No | sin actividad | 0 / 0 |
| 2026-08-24 | No | sin actividad | 0 / 0 |
| 2026-08-28 | **Sí** | Proyecto_final_programacion2 | 0 / 1 |
| 2026-08-31 | No | sin actividad | 0 / 0 |
| 2026-09-04 | **Sí** | Proyecto_final_programacion2 | 1 / 0 |
| 2026-09-07 | **Sí** | Proyecto_final_programacion2 | 1 / 1 |
| 2026-09-14 | No | sin actividad | 0 / 0 |
| 2026-09-25 | No | sin actividad | 0 / 0 |
| 2026-09-28 | **Sí** | Proyecto_final_programacion2 | 1 / 0 |
| 2026-10-02 | **Sí** | Proyecto_final_programacion2 | 1 / 0 |
| 2026-10-05 | **Sí** | Proyecto_final_programacion2 | 0 / 1 |
| 2026-10-09 | No | sin actividad | 0 / 0 |
| 2026-10-16 | No | sin actividad | 0 / 0 |
| 2026-10-19 | No | sin actividad | 0 / 0 |
| 2026-10-23 | **Sí** | Proyecto_final_programacion2 | 1 / 0 |
| 2026-10-26 | **Sí** | Proyecto_final_programacion2 | 2 / 0 |
| 2026-10-30 | **Sí** | Proyecto_final_programacion2 | 5 / 1 |
| 2026-11-02 | No | sin actividad | 0 / 0 |
| 2026-11-06 | **Sí** | Proyecto_final_programacion2 | 1 / 1 |
| 2026-11-13 | No | sin actividad | 0 / 0 |
| 2026-11-16 | **Sí** | Proyecto_final_programacion2 | 1 / 0 |
| 2026-11-20 | **Sí** | Proyecto_final_programacion2 | 1 / 0 |

#### Aportes fuera del calendario de clases — minibautista14-tech

_Sin aportes fuera del calendario de clases para minibautista14-tech._

<!-- actividad-clases:fin -->

---
_Informe generado por la cátedra. Los criterios y umbrales de medición corresponden al perfil `exigente` de `calidad.py`; el semáforo de aprobación, a `CRITERIOS-CALIDAD.md` (ver `README.md`). Ante dudas, revisá el archivo señalado._
<!-- evaluacion:fin -->
