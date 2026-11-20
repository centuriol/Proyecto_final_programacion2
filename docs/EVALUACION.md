<!-- evaluacion:inicio -->
# Evaluación — centuriol/Proyecto_final_programacion2

> Documento **generado automáticamente** por la cátedra de Programación II (RepoSense + análisis de calidad POO/SOLID). **No editar a mano**: se regenera en cada evaluación.
> Rama evaluada: `main`. Análisis **heurístico** (regex + métricas): es una criba para orientar la revisión, **no una nota**.
> Generado: 2026-11-20.

## 1. Veredicto

| Aspecto | Resultado |
|---|---|
| Índice de calidad POO/SOLID | **67/100** |
| Código (LOC) | 21,534 |
| Archivos de código | 266 |
| Clases (bien usadas) | 289 (150 OK) |
| Código dentro de clases (%POO) | 100% |
| Funciones fuera de clases | 0 |
| Duplicación lógica | 0.8% |
| Señales S/O/L/I/D | 46/0/0/8/74 |

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
| Duplicación lógica | ≤ 10% | 0.8% | sí |
| Funciones fuera de clases | ≤ 1 | 0 | sí |
| Señales POO/SOLID críticas | ninguna | Baja cohesión (LCOM), Complejidad alta, Acoplamiento alto (CBO), Interfaz con demasiados métodos (ISP) | no |

**Criterios para destacar**

| Criterio | Requisito | Tu resultado | Cumple |
|---|---|---|---|
| Código dentro de clases (%POO) | ≥ 95% | 100% | sí |
| Duplicación lógica | ≤ 5% | 0.8% | sí |
| Funciones fuera de clases | ≤ 1 | 0 | sí |
| Señales POO/SOLID críticas | ninguna | Clase demasiado grande (SRP), Baja cohesión (LCOM), Complejidad alta, Acoplamiento alto (CBO), Interfaz con demasiados métodos (ISP) | no |
| Índice de calidad POO/SOLID | ≥ 60 | 67/100 | sí |

### Cómo se calcula el %POO (y por qué no sube solo)

`%POO = líneas de lógica dentro de clases ÷ líneas de lógica totales`.

Se cuentan las líneas de **lógica y campos**; no cuentan `package`/`import`/`using`/anotaciones ni comentarios, Javadoc o docstrings. Es una **proporción**: si agregás código nuevo dentro de una clase, el numerador y el denominador crecen juntos y el porcentaje casi no cambia.

Sube cuando el código que hoy está **fuera** de clases pasa a ser método de una clase (o desaparece): funciones sueltas, `main`, constantes de módulo, scripts y lógica de archivos sueltos. También importa que ese trabajo esté **integrado en `main`**: la evaluación siempre mide `main`, así que lo que sigue en otra rama no se refleja todavía.

## 2. Autoría (RepoSense)

| Autor | Commits | Código (LOC) | Tests (LOC) | Docs (LOC) | Días activos | % del código |
|---|---:|---:|---:|---:|---:|---:|
| mateovarelac | 11 | 12,341 | 4,082 | 0 | 5 | 67% |
| minibautista14-tech | 14 | 5,986 | 3,007 | 153 | 11 | 33% |

> _El LOC de autoría lo calcula RepoSense sobre el historial y puede diferir del LOC de código del análisis de calidad (que mira solo el estado de `main`)._

> **Reparto del trabajo:** mateovarelac concentra el 67% de las líneas de código. Un reparto muy desigual suele indicar que la integración no fue realmente en equipo; vale la pena reconstruir cómo se coordinó.

## 3. Fallas detectadas

Reúne lo que el análisis marcó y lo que todavía falta para cada semáforo. Cuando la señal se puede asociar a archivos, se indican para orientar la revisión. No es una nota.

| Falla | Tu resultado | Requisito | Archivos |
|---|---|---|---|
| Señales POO/SOLID críticas | Baja cohesión (LCOM), Complejidad alta, Acoplamiento alto (CBO), Interfaz con demasiados métodos (ISP) | Aprobar: ninguna<br>Destacar: ninguna | `app/src/main/java/org/example/App.java`, `app/src/main/java/org/example/game/civilizacion/CivilizacionComponent.java`, `app/src/main/java/org/example/game/colonizacion/GestorColonizacion.java`, `app/src/main/java/org/example/game/controlador/ConfiguradorAtajosSesion.java`, `app/src/main/java/org/example/game/controlador/ConfiguradorAtajosTeclado.java`, `app/src/main/java/org/example/game/controlador/ControladorMouse.java` (+95 más) |
| Métodos/funciones largos o con muchos parámetros | **100** método(s)/función(es) largos o con muchos parámetros | Sin señales | `app/src/main/java/org/example/game/civilizacion/CondicionesHabitabilidad.java`, `app/src/main/java/org/example/game/colonizacion/GestorColonizacion.java`, `app/src/main/java/org/example/game/controlador/CombinacionTeclas.java`, `app/src/main/java/org/example/game/controlador/SeleccionadorCuerpoMouse.java`, `app/src/main/java/org/example/game/cuerpo/AgujeroNegro.java`, `app/src/main/java/org/example/game/cuerpo/CuerpoCeleste.java` (+66 más) |
| Clase solo con métodos estáticos (utilitaria) | **1** clase(s) solo con métodos estáticos | Sin señales | `app/src/main/java/org/example/game/cuerpo/RegistroCapacidades.java` |

## 4. Aspectos evaluados

Cada aspecto indica **qué mide**, **por qué importa** y **tu resultado**. Cuando el análisis reportó una señal, se agregan preguntas para **investigar la causa** (no hay recetas: entender el porqué es parte de la evaluación).

### 1. Índice de calidad POO/SOLID (indice)

- **Qué mide:** Un resumen numérico (0-100) que combina la riqueza de clases bien usadas con la penalización por señales POO/SOLID y duplicación, y luego aplica topes duros.
- **Por qué importa:** Ordena y prioriza la revisión: un índice bajo indica varias señales que conviene mirar juntas.
- **Tu resultado:** índice **67/100**

### 2. Duplicación lógica (DRY) (duplicacion) — sin señales

- **Qué mide:** Porcentaje de bloques de LÓGICA repetidos. Quedan fuera imports/includes, anotaciones, declaraciones de campos y comentarios/Javadoc/docstrings: esos bloques son boilerplate, no lógica.
- **Por qué importa:** Cada copia de lógica se mantiene por separado: un arreglo hay que repetirlo y los defectos se multiplican.
- **Tu resultado:** **0.8%** de duplicación lógica

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
- **Tu resultado:** **17** clase(s) demasiado grande(s)
- **Para investigar la causa:**
    - ¿La clase creció por acumulación de responsabilidades?
    - ¿Qué partes de la clase se usan siempre juntas y cuáles no?
    - ¿Se podría describir su responsabilidad en una sola frase?

### 6. Baja cohesión (LCOM) (cohesion) — con señales

- **Qué mide:** Métodos y campos de una clase que casi no se relacionan entre sí.
- **Por qué importa:** Una unidad que cambia por motivos distintos y comparte poca información suele esconder más de una responsabilidad.
- **Tu resultado:** **31** clase(s) con baja cohesión
- **Para investigar la causa:**
    - ¿Qué métodos de la clase no comparten estado con el resto?
    - ¿Cambian por motivos distintos según la tarea?
    - ¿Se pensó su responsabilidad antes de escribirla, o se fue agregando?

### 7. Complejidad alta (complejidad) — con señales

- **Qué mide:** Complejidad ciclomática: cantidad de caminos posibles dentro de un método o función (condicionales, bucles, casos).
- **Por qué importa:** Muchos caminos = difícil de seguir y de probar; suelen esconderse casos sin cubrir.
- **Tu resultado:** **6** método(s)/función(es) con complejidad alta
- **Para investigar la causa:**
    - ¿Se agregaron condiciones a medida que aparecían casos nuevos?
    - ¿Se probaron todos los caminos o solo el caso principal?
    - ¿Hay reglas de negocio mezcladas con detalles técnicos?

### 8. Métodos/funciones largos o con muchos parámetros (tamano) — con señales

- **Qué mide:** Tamaño de los métodos/funciones y cantidad de parámetros.
- **Por qué importa:** Una función que hace varias cosas cuesta leerla, nombrarla y reutilizarla.
- **Tu resultado:** **100** método(s)/función(es) largos o con muchos parámetros
- **Para investigar la causa:**
    - ¿La función creció por acumulación de pasos?
    - ¿Agrupa operaciones que podrían describirse por separado?
    - ¿Los parámetros representan una misma idea (un objeto) o cosas sueltas?

### 9. Acoplamiento alto (CBO) (acoplamiento) — con señales

- **Qué mide:** Cuánto depende una clase de otros tipos del proyecto. No cuentan los tipos de la plataforma (biblioteca estándar, DOM, motor Godot) ni los métodos/propiedades propias.
- **Por qué importa:** Piezas muy enlazadas hacen que un cambio se propague y que sea difícil reemplazar partes.
- **Tu resultado:** **74** clase(s) muy acoplada(s)
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

### 12. Interfaz con demasiados métodos (ISP) (isp) — con señales

- **Qué mide:** Interfaces o clases abstractas con muchos métodos declarados.
- **Por qué importa:** Obliga a implementar operaciones que no se usan y acopla a los clientes a métodos que no necesitan.
- **Tu resultado:** **8** interfaz(es) con demasiados métodos
- **Para investigar la causa:**
    - ¿Qué subconjunto de métodos usa realmente cada implementación?
    - ¿Se agruparon métodos de necesidades distintas en la misma interfaz?
    - ¿Hay implementaciones que dejan métodos sin uso?

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

### 16. Clase solo con métodos estáticos (utilitaria) (static) — con señales

- **Qué mide:** Clases donde todos los métodos son estáticos.
- **Por qué importa:** Es comportamiento procedural disfrazado de clase: no se instancia ni participa del modelo de objetos.
- **Tu resultado:** **1** clase(s) solo con métodos estáticos
- **Para investigar la causa:**
    - ¿Por qué no hace falta una instancia en esa clase?
    - ¿Los métodos operan sobre estado que podría vivir en un objeto?
    - ¿Se usó clase solo para agrupar funciones sueltas?

### Archivos donde se originan las señales

> Se listan **237** hallazgo(s) localizados en **136** archivo(s). La columna `Línea` es la línea del archivo donde el análisis detectó la señal; `—` indica una señal de nivel repositorio (sin archivo propio).

| Aspecto | Archivo | Línea | Hallazgo |
|---|---|---:|---|
| Clase demasiado grande (SRP) | `app/src/main/java/org/example/game/civilizacion/CivilizacionComponent.java` | 16 | clase 'CivilizacionComponent' grande (LOC lógico=126, métodos=19, campos=14, accesores=10 (no cuentan)) |
| Clase demasiado grande (SRP) | `app/src/main/java/org/example/game/controlador/ControladorMouse.java` | 23 | clase 'ControladorMouse' grande (LOC lógico=145, métodos=21, campos=11, accesores=6 (no cuentan)) |
| Clase demasiado grande (SRP) | `app/src/main/java/org/example/game/controlador/CoordinadorSesionJuego.java` | 20 | clase 'CoordinadorSesionJuego' grande (LOC lógico=129, métodos=18, campos=11, accesores=1 (no cuentan)) |
| Clase demasiado grande (SRP) | `app/src/main/java/org/example/game/cuerpo/CuerpoCelesteFactory.java` | 14 | clase 'CuerpoCelesteFactory' grande (LOC lógico=115, métodos=26, campos=4) |
| Clase demasiado grande (SRP) | `app/src/main/java/org/example/game/cuerpo/Planeta.java` | 7 | clase 'Planeta' grande (LOC lógico=113, métodos=20, campos=7, accesores=6 (no cuentan)) |
| Clase demasiado grande (SRP) | `app/src/main/java/org/example/game/guardado/GestorGuardadoPartida.java` | 18 | clase 'GestorGuardadoPartida' grande (LOC lógico=116, métodos=16, campos=6, accesores=2 (no cuentan)) |
| Clase demasiado grande (SRP) | `app/src/main/java/org/example/game/jugador/InventarioJugador.java` | 8 | clase 'InventarioJugador' grande (LOC lógico=116, métodos=17, campos=2, accesores=4 (no cuentan)) |
| Clase demasiado grande (SRP) | `app/src/main/java/org/example/game/motor/AsistenteOrbital.java` | 13 | clase 'AsistenteOrbital' grande (LOC lógico=220, métodos=14, campos=3) |
| Clase demasiado grande (SRP) | `app/src/main/java/org/example/game/motor/TransformadorCuerpos.java` | 23 | clase 'TransformadorCuerpos' grande (LOC lógico=153, métodos=18, campos=22, accesores=19 (no cuentan)) |
| Clase demasiado grande (SRP) | `app/src/main/java/org/example/game/motor/colision/ReglaColisionMeteorito.java` | 13 | clase 'ReglaColisionMeteorito' grande (LOC lógico=101, métodos=15, campos=3) |
| Clase demasiado grande (SRP) | `app/src/main/java/org/example/game/progresion/SistemaProgresion.java` | 18 | clase 'SistemaProgresion' grande (LOC lógico=135, métodos=21, campos=2, accesores=2 (no cuentan)) |
| Clase demasiado grande (SRP) | `app/src/main/java/org/example/game/render/RenderizadorFondo.java` | 15 | clase 'RenderizadorFondo' grande (LOC lógico=126, métodos=15, campos=14, accesores=4 (no cuentan)) |
| Clase demasiado grande (SRP) | `app/src/main/java/org/example/game/ui/BarraInventarioHotbar.java` | 20 | clase 'BarraInventarioHotbar' grande (LOC lógico=140, métodos=17, campos=8, accesores=2 (no cuentan)) |
| Clase demasiado grande (SRP) | `app/src/main/java/org/example/game/ui/PantallaMenuPrincipal.java` | 26 | clase 'PantallaMenuPrincipal' grande (LOC lógico=156, métodos=15, campos=17, accesores=10 (no cuentan)) |
| Clase demasiado grande (SRP) | `app/src/main/java/org/example/game/ui/SeccionInfoCuerpo.java` | 27 | clase 'SeccionInfoCuerpo' grande (LOC lógico=147, métodos=17, campos=1) |
| Clase demasiado grande (SRP) | `app/src/main/java/org/example/game/ui/VentanaInspeccion.java` | 24 | clase 'VentanaInspeccion' grande (LOC lógico=225, métodos=12, campos=4) |
| Clase demasiado grande (SRP) | `app/src/test/java/org/example/ColonizacionYEscudosTest.java` | 16 | clase 'ColonizacionYEscudosTest' grande (LOC lógico=218, métodos=12, campos=1) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/App.java` | 29 | clase 'App' con baja cohesión (36%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/controlador/ControladorMouse.java` | 23 | clase 'ControladorMouse' con baja cohesión (43%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/controlador/CoordinadorSesionJuego.java` | 20 | clase 'CoordinadorSesionJuego' con baja cohesión (42%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/cuerpo/CuerpoCeleste.java` | 12 | clase 'CuerpoCeleste' con baja cohesión (11%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/cuerpo/CuerpoCelesteFactory.java` | 14 | clase 'CuerpoCelesteFactory' con baja cohesión (23%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/cuerpo/Meteorito.java` | 7 | clase 'Meteorito' con baja cohesión (18%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/cuerpo/Planeta.java` | 7 | clase 'Planeta' con baja cohesión (44%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/efectos/GestorEfectos.java` | 17 | clase 'GestorEfectos' con baja cohesión (25%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/guardado/PoliticaRanurasGuardado.java` | 13 | clase 'PoliticaRanurasGuardado' con baja cohesión (33%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/guardado/ReconstructorCuerpos.java` | 14 | clase 'ReconstructorCuerpos' con baja cohesión (17%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/guardado/RepositorioPartidasArchivo.java` | 19 | clase 'RepositorioPartidasArchivo' con baja cohesión (46%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/motor/MotorFisicaBase.java` | 22 | clase 'MotorFisicaBase' con baja cohesión (39%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/motor/MotorFisicaPermisiva.java` | 20 | clase 'MotorFisicaPermisiva' con baja cohesión (27%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/motor/ResolvedorColisiones.java` | 29 | clase 'ResolvedorColisiones' con baja cohesión (44%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/motor/TransformadorCuerpos.java` | 23 | clase 'TransformadorCuerpos' con baja cohesión (38%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/render/DesplazamientoCamara.java` | 11 | clase 'DesplazamientoCamara' con baja cohesión (47%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/render/EscenaFondoMenu.java` | 18 | clase 'EscenaFondoMenu' con baja cohesión (29%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/render/GestorImagenes.java` | 21 | clase 'GestorImagenes' con baja cohesión (22%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/render/RegistroEstrategiasVisuales.java` | 18 | clase 'RegistroEstrategiasVisuales' con baja cohesión (4%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/render/RegistroEstrategiasVisuales.java` | 211 | clase 'EstrategiaBase' con baja cohesión (0%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/render/RenderizadorPixelArt.java` | 20 | clase 'RenderizadorPixelArt' con baja cohesión (14%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/render/UtilidadRender.java` | 9 | clase 'UtilidadRender' con baja cohesión (33%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/simulacion/BusEventos.java` | 9 | clase 'BusEventos' con baja cohesión (19%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/simulacion/ControladorColocacion.java` | 11 | clase 'ControladorColocacion' con baja cohesión (43%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/simulacion/SimulacionSolar.java` | 25 | clase 'SimulacionSolar' con baja cohesión (48%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/ui/BarraInventarioHotbar.java` | 20 | clase 'BarraInventarioHotbar' con baja cohesión (29%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/ui/CapaIndicadoresSol.java` | 26 | clase 'CapaIndicadoresSol' con baja cohesión (29%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/ui/ControlTiempoWidget.java` | 23 | clase 'ControlTiempoWidget' con baja cohesión (40%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/ui/PantallaMenuPrincipal.java` | 26 | clase 'PantallaMenuPrincipal' con baja cohesión (22%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/ui/SlotHotbar.java` | 16 | clase 'SlotHotbar' con baja cohesión (33%) |
| Baja cohesión (LCOM) | `app/src/main/java/org/example/game/ui/VentanaProgresion.java` | 33 | clase 'VentanaProgresion' con baja cohesión (46%) |
| Complejidad alta | `app/src/main/java/org/example/game/motor/AsistenteOrbital.java` | 108 | método 'AsistenteOrbital.aplicarDinamicaOrbitalEstable' con complejidad 12 |
| Complejidad alta | `app/src/main/java/org/example/game/motor/AsistenteOrbital.java` | 187 | método 'AsistenteOrbital.aplicarAsistenciaOrbital' con complejidad 11 |
| Complejidad alta | `app/src/main/java/org/example/game/motor/DetectorColisiones.java` | 41 | método 'DetectorColisiones.detectarYResolver' con complejidad 10 |
| Complejidad alta | `app/src/main/java/org/example/game/motor/TransformadorCuerpos.java` | 205 | método 'TransformadorCuerpos.procesarTransformaciones' con complejidad 16 |
| Complejidad alta | `app/src/main/java/org/example/game/render/GestorImagenes.java` | 166 | método 'GestorImagenes.cargarImagen' con complejidad 13 |
| Complejidad alta | `app/src/main/java/org/example/game/simulacion/GestorEconomia.java` | 17 | método 'GestorEconomia.actualizarEconomia' con complejidad 10 |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/civilizacion/CondicionesHabitabilidad.java` | 6 | método 'CondicionesHabitabilidad.CondicionesHabitabilidad' con 9 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/colonizacion/GestorColonizacion.java` | 153 | método 'GestorColonizacion.lanzar' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/colonizacion/GestorColonizacion.java` | 153 | método 'GestorColonizacion.lanzar' de 40 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/controlador/CombinacionTeclas.java` | 9 | método 'CombinacionTeclas.CombinacionTeclas' de 47 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/controlador/SeleccionadorCuerpoMouse.java` | 25 | método 'SeleccionadorCuerpoMouse.encontrarCuerpo' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/AgujeroNegro.java` | 9 | método 'AgujeroNegro.AgujeroNegro' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/CuerpoCeleste.java` | 24 | método 'CuerpoCeleste.CuerpoCeleste' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/CuerpoCelesteFactory.java` | 57 | método 'CuerpoCelesteFactory.crearCuerpo' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/CuerpoCelesteFactory.java` | 80 | método 'CuerpoCelesteFactory.crearPlaneta' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/CuerpoCelesteFactory.java` | 97 | método 'CuerpoCelesteFactory.crearPlanetaPersonalizado' con 6 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/CuerpoCelesteFactory.java` | 126 | método 'CuerpoCelesteFactory.crearLunaPersonalizada' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/CuerpoCelesteFactory.java` | 141 | método 'CuerpoCelesteFactory.crearAgujeroNegro' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/DescriptorTipoCuerpo.java` | 12 | método 'DescriptorTipoCuerpo.DescriptorTipoCuerpo' con 11 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/FabricaCuerpos.java` | 10 | método 'FabricaCuerpos.crearCuerpo' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/FabricaCuerpos.java` | 12 | método 'FabricaCuerpos.crearPlaneta' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/FabricaCuerpos.java` | 14 | método 'FabricaCuerpos.crearPlanetaPersonalizado' con 6 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/FabricaCuerpos.java` | 24 | método 'FabricaCuerpos.crearLunaPersonalizada' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/FabricaCuerpos.java` | 28 | método 'FabricaCuerpos.crearAgujeroNegro' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/FabricaMeteoritos.java` | 28 | método 'FabricaMeteoritos.crearHaciaObjetivo' de 35 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/FabricaPlanetas.java` | 19 | método 'FabricaPlanetas.crearPlaneta' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/FabricaPlanetas.java` | 27 | método 'FabricaPlanetas.crearPlanetaPersonalizado' con 6 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/cuerpo/Planeta.java` | 16 | método 'Planeta.Planeta' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/guardado/DatosCivilizacion.java` | 7 | método 'DatosCivilizacion.DatosCivilizacion' con 6 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/guardado/DatosCuerpo.java` | 7 | método 'DatosCuerpo.DatosCuerpo' con 6 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/guardado/DatosPartida.java` | 11 | método 'DatosPartida.DatosPartida' con 6 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/guardado/DatosPartida.java` | 11 | método 'DatosPartida.DatosPartida' de 31 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/guardado/DetalleCuerpo.java` | 5 | método 'DetalleCuerpo.DetalleCuerpo' con 8 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/guardado/EstadoJugadorGuardado.java` | 10 | método 'EstadoJugadorGuardado.EstadoJugadorGuardado' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/guardado/EstadoRelojGuardado.java` | 7 | método 'EstadoRelojGuardado.EstadoRelojGuardado' con 6 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/guardado/GestorGuardadoPartida.java` | 92 | método 'GestorGuardadoPartida.escribirEnSlot' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/guardado/InfoRanuraGuardado.java` | 9 | método 'InfoRanuraGuardado.InfoRanuraGuardado' de 33 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/AsistenteOrbital.java` | 108 | método 'AsistenteOrbital.aplicarDinamicaOrbitalEstable' de 68 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/AsistenteOrbital.java` | 187 | método 'AsistenteOrbital.aplicarAsistenciaOrbital' de 39 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/CalculadorGravedad.java` | 85 | método 'CalculadorGravedad.procesarGravedadPlanetaLuna' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/CalculadorGravedad.java` | 112 | método 'CalculadorGravedad.aplicarGravedadNewtoniana' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/CapturaOrbitalMenorPlaneta.java` | 37 | método 'CapturaOrbitalMenorPlaneta.ejecutarCaptura' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/CapturaOrbitalPlanetaAtractor.java` | 27 | método 'CapturaOrbitalPlanetaAtractor.ejecutarCaptura' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/DetectorColisiones.java` | 41 | método 'DetectorColisiones.detectarYResolver' de 30 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/EstrategiaCapturaOrbital.java` | 9 | método 'EstrategiaCapturaOrbital.ejecutarCaptura' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/MotorFisicaPermisiva.java` | 86 | método 'MotorFisicaPermisiva.avanzarPaso' de 38 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/TransformadorCuerpos.java` | 127 | método 'TransformadorCuerpos.TransformadorCuerpos' de 30 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/TransformadorCuerpos.java` | 205 | método 'TransformadorCuerpos.procesarTransformaciones' de 56 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/Vector2D.java` | 7 | método 'Vector2D.Vector2D' de 50 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/colision/ContextoColision.java` | 12 | método 'ContextoColision.ContextoColision' con 7 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/colision/ReglaColisionAgujeroNegro.java` | 34 | método 'ReglaColisionAgujeroNegro.aplica' de 37 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/colision/ReglaColisionAgujeroNegro.java` | 79 | método 'ReglaColisionAgujeroNegro.resolver' de 38 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/motor/colision/ReglaColisionFusionAguaLava.java` | 43 | método 'ReglaColisionFusionAguaLava.resolver' de 32 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/GestorImagenes.java` | 166 | método 'GestorImagenes.cargarImagen' de 47 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/RenderizadorCuerpo.java` | 14 | método 'RenderizadorCuerpo.dibujar' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/RenderizadorFondo.java` | 36 | método 'EstrellaFondo.EstrellaFondo' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/RenderizadorFondo.java` | 36 | método 'RenderizadorFondo.EstrellaFondo' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/RenderizadorFondo.java` | 105 | método 'RenderizadorFondo.dibujarGrilla' de 30 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/RenderizadorPixelArt.java` | 194 | método 'RenderizadorPixelArt.dibujarCuerpoPixelArt' de 38 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/UtilidadRender.java` | 37 | método 'UtilidadRender.discoConContorno' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/UtilidadRender.java` | 56 | método 'UtilidadRender.dibujarDiscoConContorno' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/capa/CapaEscudoPlanetario.java` | 26 | método 'CapaEscudoPlanetario.dibujarCupula' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/capa/CapaGhostPreview.java` | 54 | método 'CapaGhostPreview.dibujarSpriteFantasma' con 6 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/capa/CapaGhostPreview.java` | 77 | método 'CapaGhostPreview.dibujarAreaColapsoYReferencias' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/capa/CapaGhostPreview.java` | 96 | método 'CapaGhostPreview.dibujarTagInformacion' con 7 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/cuerpo/RenderizadorAgujeroNegro.java` | 15 | método 'RenderizadorAgujeroNegro.dibujar' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/cuerpo/RenderizadorEstrella.java` | 21 | método 'RenderizadorEstrella.dibujar' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/cuerpo/RenderizadorLuna.java` | 21 | método 'RenderizadorLuna.dibujar' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/cuerpo/RenderizadorMeteorito.java` | 15 | método 'RenderizadorMeteorito.dibujar' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/cuerpo/RenderizadorPlaneta.java` | 41 | método 'RenderizadorPlaneta.dibujar' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/render/cuerpo/RenderizadorSatelite.java` | 14 | método 'RenderizadorSatelite.dibujar' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/simulacion/GestorCicloVidaCuerpos.java` | 55 | método 'GestorCicloVidaCuerpos.spawnCuerpo' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/simulacion/GestorCicloVidaCuerpos.java` | 59 | método 'GestorCicloVidaCuerpos.spawnCuerpo' con 6 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/simulacion/GestorColocacionCuerpos.java` | 66 | método 'GestorColocacionCuerpos.confirmarColocacionAutoOrbita' de 37 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/simulacion/GestorEconomia.java` | 17 | método 'GestorEconomia.actualizarEconomia' de 30 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/simulacion/SimulacionSolar.java` | 56 | método 'SimulacionSolar.SimulacionSolar' de 39 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/BarraInventarioHotbar.java` | 197 | método 'BarraInventarioHotbar.formatearMapaRecursos' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/ControlTiempoWidget.java` | 46 | método 'ControlTiempoWidget.ControlTiempoWidget' de 36 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/InspectorTecnologiaArbol.java` | 43 | método 'InspectorTecnologiaArbol.InspectorTecnologiaArbol' de 49 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/InspectorTecnologiaArbol.java` | 111 | método 'InspectorTecnologiaArbol.actualizar' de 46 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/LienzoArbolOrbital.java` | 83 | método 'LienzoArbolOrbital.refrescar' de 44 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/LienzoArbolOrbital.java` | 213 | método 'LienzoArbolOrbital.crearLineaConectora' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/PantallaArbolProgresion.java` | 29 | método 'NodoArbol.NodoArbol' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/PantallaArbolProgresion.java` | 29 | método 'PantallaArbolProgresion.NodoArbol' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/PantallaArbolProgresion.java` | 43 | método 'PantallaArbolProgresion.PantallaArbolProgresion' de 79 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/ProveedorInfoCuerpo.java` | 32 | método 'ProveedorInfoCuerpo.agregarFilaPropiedad' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/SeccionInfoCuerpo.java` | 152 | método 'SeccionInfoCuerpo.agregarFilaPropiedad' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/SlotHotbar.java` | 30 | método 'SlotHotbar.SlotHotbar' de 39 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/VentanaColocacion.java` | 43 | método 'VentanaColocacion.VentanaColocacion' con 5 parámetros |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/VentanaColocacion.java` | 94 | método 'VentanaColocacion.construirFormulario' de 36 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/VentanaInspeccion.java` | 120 | método 'VentanaInspeccion.construirTarjetaPoblar' de 46 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/main/java/org/example/game/ui/WidgetFactorMasa.java` | 31 | método 'WidgetFactorMasa.WidgetFactorMasa' de 34 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/CivilizacionSinergiaTest.java` | 26 | método 'CivilizacionSinergiaTest.testGeneracionRecursosPorCuerpoYSlowGrowth' de 44 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/ColonizacionYEscudosTest.java` | 237 | método 'ColonizacionYEscudosTest.testCrecimientoMasLentoEnOtrosPlanetasYBonusMaterialesX1_3AlEstarPoblado' de 30 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/CorreccionesMeteoritosAutoguardadoEconomiaTest.java` | 74 | método 'CorreccionesMeteoritosAutoguardadoEconomiaTest.testEscombrosDePlanetasDestruidosRepartenMasaRespetandoTopes' de 33 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/CostosProporcionalesYSatelitesMenuTest.java` | 28 | método 'CostosProporcionalesYSatelitesMenuTest.testTablaCostosOrdenCoherenteYProporcionalALaProduccion' de 36 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/CostosProporcionalesYSatelitesMenuTest.java` | 157 | método 'CostosProporcionalesYSatelitesMenuTest.testClicEnSateliteDelMenuIniciaCaidaConGravedadPromedioHastaSalirDePantalla' de 35 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/EasterEggFusionYRadiosTest.java` | 51 | método 'EasterEggFusionYRadiosTest.testEasterEggColisionAguaYLavaFormaPlanetaPorroInerte' de 37 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/EconomiaRecursosTest.java` | 92 | método 'EconomiaRecursosTest.testEspecificacionMaterialesParaPonerYPorTick' de 36 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/GuardadoYCargaSimulacionTest.java` | 29 | método 'GuardadoYCargaSimulacionTest.testGuardarYCargarRestauraSistemaSolarCompletoYRelacionesOrbitales' de 75 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/ProgresionYArbolTest.java` | 28 | método 'ProgresionYArbolTest.testArbolTecnologicoTresRamasYCostosEscalonados' de 43 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/ProgresionYArbolTest.java` | 146 | método 'ProgresionYArbolTest.testItemSateliteDesbloqueoAntesDelEscudoYProduccionFija' de 30 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/ReembolsoYColocacionTest.java` | 148 | método 'ReembolsoYColocacionTest.testNuevosItemsPlanetaAguaYPlanetaLavaYSolX2' de 37 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/RepositorioYSerializacionPartidaTest.java` | 126 | método 'RepositorioYSerializacionPartidaTest.construirPartidaEjemplo' de 39 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/UIWidgetsYFormatoTest.java` | 118 | método 'UIWidgetsYFormatoTest.testControlTiempoWidgetCompactoYVelocidades' de 34 líneas |
| Métodos/funciones largos o con muchos parámetros | `app/src/test/java/org/example/game/ui/ConstructorFichaInvestigacionTest.java` | 30 | método 'ConstructorFichaInvestigacionTest.construyeFichaInvestigadaDisponibleYBloqueada' de 39 líneas |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/App.java` | 29 | clase 'App' muy acoplada (CBO=32) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/civilizacion/CivilizacionComponent.java` | 16 | clase 'CivilizacionComponent' muy acoplada (CBO=9) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/colonizacion/GestorColonizacion.java` | 27 | clase 'GestorColonizacion' muy acoplada (CBO=11) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/controlador/ConfiguradorAtajosSesion.java` | 14 | clase 'ConfiguradorAtajosSesion' muy acoplada (CBO=13) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/controlador/ConfiguradorAtajosTeclado.java` | 12 | clase 'ConfiguradorAtajosTeclado' muy acoplada (CBO=16) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/controlador/ControladorMouse.java` | 23 | clase 'ControladorMouse' muy acoplada (CBO=13) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/controlador/ControladorTeclado.java` | 20 | clase 'ControladorTeclado' muy acoplada (CBO=11) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/controlador/CoordinadorSesionJuego.java` | 20 | clase 'CoordinadorSesionJuego' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/cuerpo/CuerpoCelesteFactory.java` | 14 | clase 'CuerpoCelesteFactory' muy acoplada (CBO=17) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/cuerpo/RegistroCapacidades.java` | 16 | clase 'RegistroCapacidades' muy acoplada (CBO=11) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/guardado/CodificadorEntidadesPartida.java` | 9 | clase 'CodificadorEntidadesPartida' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/guardado/DiccionarioPropiedades.java` | 11 | clase 'DiccionarioPropiedades' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/guardado/ExtractorDatosCuerpo.java` | 11 | clase 'ExtractorDatosCuerpo' muy acoplada (CBO=14) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/guardado/GestorGuardadoPartida.java` | 18 | clase 'GestorGuardadoPartida' muy acoplada (CBO=12) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/guardado/MapeadorEstadoSimulacion.java` | 21 | clase 'MapeadorEstadoSimulacion' muy acoplada (CBO=20) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/guardado/ReconstructorCuerpos.java` | 14 | clase 'ReconstructorCuerpos' muy acoplada (CBO=19) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/guardado/SerializadorPartidaTexto.java` | 16 | clase 'SerializadorPartidaTexto' muy acoplada (CBO=14) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/motor/DetectorColisiones.java` | 9 | clase 'DetectorColisiones' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/motor/MotorFisicaPermisiva.java` | 20 | clase 'MotorFisicaPermisiva' muy acoplada (CBO=13) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/motor/ResolvedorColisiones.java` | 29 | clase 'ResolvedorColisiones' muy acoplada (CBO=18) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/motor/TransformadorCuerpos.java` | 23 | clase 'TransformadorCuerpos' muy acoplada (CBO=9) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/motor/colision/ReglaColisionAgujeroNegro.java` | 15 | clase 'ReglaColisionAgujeroNegro' muy acoplada (CBO=13) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/motor/colision/ReglaColisionEscudo.java` | 16 | clase 'ReglaColisionEscudo' muy acoplada (CBO=12) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/motor/colision/ReglaColisionEstrella.java` | 14 | clase 'ReglaColisionEstrella' muy acoplada (CBO=11) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/motor/colision/ReglaColisionMeteorito.java` | 13 | clase 'ReglaColisionMeteorito' muy acoplada (CBO=9) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/render/EscenaFondoMenu.java` | 18 | clase 'EscenaFondoMenu' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/render/GestorImagenes.java` | 21 | clase 'GestorImagenes' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/render/RenderizadorPixelArt.java` | 20 | clase 'RenderizadorPixelArt' muy acoplada (CBO=28) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/simulacion/CoordinadorPasoSimulacion.java` | 20 | clase 'CoordinadorPasoSimulacion' muy acoplada (CBO=14) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/simulacion/GestorCicloVidaCuerpos.java` | 20 | clase 'GestorCicloVidaCuerpos' muy acoplada (CBO=11) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/simulacion/GestorColocacionCuerpos.java` | 14 | clase 'GestorColocacionCuerpos' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/simulacion/SimulacionSolar.java` | 25 | clase 'SimulacionSolar' muy acoplada (CBO=28) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/BarraInventarioHotbar.java` | 20 | clase 'BarraInventarioHotbar' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/ConstructorOverlayUI.java` | 17 | clase 'ConstructorOverlayUI' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/ControlTiempoWidget.java` | 23 | clase 'ControlTiempoWidget' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/InspectorTecnologiaArbol.java` | 25 | clase 'InspectorTecnologiaArbol' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/LienzoArbolOrbital.java` | 29 | clase 'LienzoArbolOrbital' muy acoplada (CBO=12) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/PantallaArbolProgresion.java` | 27 | clase 'PantallaArbolProgresion' muy acoplada (CBO=11) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/PantallaGameOver.java` | 17 | clase 'PantallaGameOver' muy acoplada (CBO=11) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/PantallaMenuPausa.java` | 15 | clase 'PantallaMenuPausa' muy acoplada (CBO=11) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/PantallaMenuPrincipal.java` | 26 | clase 'PantallaMenuPrincipal' muy acoplada (CBO=16) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/SeccionInfoCuerpo.java` | 27 | clase 'SeccionInfoCuerpo' muy acoplada (CBO=12) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/VentanaColocacion.java` | 27 | clase 'VentanaColocacion' muy acoplada (CBO=15) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/VentanaDescripcionCuerpo.java` | 13 | clase 'VentanaDescripcionCuerpo' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/VentanaInspeccion.java` | 24 | clase 'VentanaInspeccion' muy acoplada (CBO=18) |
| Acoplamiento alto (CBO) | `app/src/main/java/org/example/game/ui/VentanaProgresion.java` | 33 | clase 'VentanaProgresion' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/AgujeroNegroFisicaTest.java` | 14 | clase 'AgujeroNegroFisicaTest' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/ArrastreCamaraYColocacionTest.java` | 25 | clase 'ArrastreCamaraYColocacionTest' muy acoplada (CBO=13) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/ColonizacionYEscudosTest.java` | 16 | clase 'ColonizacionYEscudosTest' muy acoplada (CBO=11) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/ControladorMouseYHelpersTest.java` | 25 | clase 'ControladorMouseYHelpersTest' muy acoplada (CBO=11) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/ControladorTecladoYComandosTest.java` | 19 | clase 'ControladorTecladoYComandosTest' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/CorreccionesMeteoritosAutoguardadoEconomiaTest.java` | 49 | clase 'CorreccionesMeteoritosAutoguardadoEconomiaTest' muy acoplada (CBO=27) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/CostosProporcionalesYSatelitesMenuTest.java` | 25 | clase 'CostosProporcionalesYSatelitesMenuTest' muy acoplada (CBO=9) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/EasterEggFusionYRadiosTest.java` | 4 | clase 'EasterEggFusionYRadiosTest' muy acoplada (CBO=9) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/EventosCosmicosYSatelitesTest.java` | 4 | clase 'EventosCosmicosYSatelitesTest' muy acoplada (CBO=12) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/Fase2BugsTest.java` | 22 | clase 'Fase2BugsTest' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/FondoAnimadoMenuPrincipalTest.java` | 17 | clase 'FondoAnimadoMenuPrincipalTest' muy acoplada (CBO=9) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/GuardadoYCargaSimulacionTest.java` | 23 | clase 'GuardadoYCargaSimulacionTest' muy acoplada (CBO=22) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/MecanicasColisionYVidaTest.java` | 4 | clase 'MecanicasColisionYVidaTest' muy acoplada (CBO=11) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/MenuPausaYDisposicionUITest.java` | 22 | clase 'MenuPausaYDisposicionUITest' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/MenuPrincipalYDerrotaFlujoTest.java` | 19 | clase 'MenuPrincipalYDerrotaFlujoTest' muy acoplada (CBO=12) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/OrbitasYDinamicaTest.java` | 4 | clase 'OrbitasYDinamicaTest' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/RanurasYAutoguardadoTest.java` | 18 | clase 'RanurasYAutoguardadoTest' muy acoplada (CBO=15) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/ReembolsoYColocacionTest.java` | 16 | clase 'ReembolsoYColocacionTest' muy acoplada (CBO=9) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/RefactorOopModularidadTest.java` | 22 | clase 'RefactorOopModularidadTest' muy acoplada (CBO=24) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/ReglasColisionTest.java` | 9 | clase 'ReglasColisionTest' muy acoplada (CBO=14) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/RendimientoRegresionTest.java` | 21 | clase 'RendimientoRegresionTest' muy acoplada (CBO=9) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/RepositorioYSerializacionPartidaTest.java` | 18 | clase 'RepositorioYSerializacionPartidaTest' muy acoplada (CBO=16) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/SolidAbstraccionesRefactorTest.java` | 27 | clase 'SolidAbstraccionesRefactorTest' muy acoplada (CBO=16) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/TransformacionCuerposTest.java` | 15 | clase 'TransformacionCuerposTest' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/UIWidgetsYFormatoTest.java` | 17 | clase 'UIWidgetsYFormatoTest' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/game/motor/colision/DespachoDobleColisionesTest.java` | 21 | clase 'DespachoDobleColisionesTest' muy acoplada (CBO=10) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/game/render/capa/CapaNavesYEscudoTest.java` | 18 | clase 'CapaNavesYEscudoTest' muy acoplada (CBO=8) |
| Acoplamiento alto (CBO) | `app/src/test/java/org/example/game/ui/FabricaPantallasModalesTest.java` | 16 | clase 'FabricaPantallasModalesTest' muy acoplada (CBO=9) |
| Interfaz con demasiados métodos (ISP) | `app/src/main/java/org/example/game/cuerpo/Defensor.java` | 8 | interfaz 'Defensor' con 12 métodos |
| Interfaz con demasiados métodos (ISP) | `app/src/main/java/org/example/game/cuerpo/FabricaCuerpos.java` | 8 | interfaz 'FabricaCuerpos' con 20 métodos |
| Interfaz con demasiados métodos (ISP) | `app/src/main/java/org/example/game/motor/CuerpoFisico.java` | 9 | interfaz 'CuerpoFisico' con 14 métodos |
| Interfaz con demasiados métodos (ISP) | `app/src/main/java/org/example/game/render/ControlZoom.java` | 5 | interfaz 'ControlZoom' con 11 métodos |
| Interfaz con demasiados métodos (ISP) | `app/src/main/java/org/example/game/render/EstrategiaVisualTipo.java` | 10 | interfaz 'EstrategiaVisualTipo' con 7 métodos |
| Interfaz con demasiados métodos (ISP) | `app/src/main/java/org/example/game/render/RepositorioSprites.java` | 13 | interfaz 'RepositorioSprites' con 11 métodos |
| Interfaz con demasiados métodos (ISP) | `app/src/main/java/org/example/game/ui/ProveedorInfoCuerpo.java` | 18 | interfaz 'ProveedorInfoCuerpo' con 13 métodos |
| Interfaz con demasiados métodos (ISP) | `app/src/main/java/org/example/game/ui/VisualizadorDescripcion.java` | 13 | interfaz 'VisualizadorDescripcion' con 8 métodos |
| Clase solo con métodos estáticos (utilitaria) | `app/src/main/java/org/example/game/cuerpo/RegistroCapacidades.java` | 16 | clase 'RegistroCapacidades' solo con métodos estáticos (¿utilitaria?) |

### Desglose de la columna S (SRP)

> La columna **S** no es un puntaje: cuenta **señales** de SRP. Aquí suma **46** entrada(s) sobre **43** clase(s) distinta(s).
> 3 clase(s) acumulan más de una señal: `AsistenteOrbital`, `GestorImagenes`, `TransformadorCuerpos`.

| Señal | Entradas | Regla (perfil exigente) |
|---|---:|---|
| Clase grande | 17 | ≥ 200 LOC lógico, o ≥ 15 métodos con lógica y ≥ 100 LOC lógico. No cuentan getters/setters. |
| Cohesión baja | 24 | < 50 % de los pares de métodos comparten algún campo (con ≥ 4 métodos con lógica y ≥ 3 campos). |
| Complejidad alta | 5 | Algún método con complejidad ciclomática ≥ 10. |

**Clases grandes (17)**

- `VentanaInspeccion`: clase grande (LOC lógico=225, métodos=12, campos=4)
- `AsistenteOrbital`: clase grande (LOC lógico=220, métodos=14, campos=3)
- `ColonizacionYEscudosTest`: clase grande (LOC lógico=218, métodos=12, campos=1)
- `PantallaMenuPrincipal`: clase grande (LOC lógico=156, métodos=15, campos=17, accesores=10 (no cuentan))
- `TransformadorCuerpos`: clase grande (LOC lógico=153, métodos=18, campos=22, accesores=19 (no cuentan))
- `SeccionInfoCuerpo`: clase grande (LOC lógico=147, métodos=17, campos=1)
- `ControladorMouse`: clase grande (LOC lógico=145, métodos=21, campos=11, accesores=6 (no cuentan))
- `BarraInventarioHotbar`: clase grande (LOC lógico=140, métodos=17, campos=8, accesores=2 (no cuentan))
- `SistemaProgresion`: clase grande (LOC lógico=135, métodos=21, campos=2, accesores=2 (no cuentan))
- `CoordinadorSesionJuego`: clase grande (LOC lógico=129, métodos=18, campos=11, accesores=1 (no cuentan))
- `RenderizadorFondo`: clase grande (LOC lógico=126, métodos=15, campos=14, accesores=4 (no cuentan))
- `CivilizacionComponent`: clase grande (LOC lógico=126, métodos=19, campos=14, accesores=10 (no cuentan))
- `GestorGuardadoPartida`: clase grande (LOC lógico=116, métodos=16, campos=6, accesores=2 (no cuentan))
- `InventarioJugador`: clase grande (LOC lógico=116, métodos=17, campos=2, accesores=4 (no cuentan))
- `CuerpoCelesteFactory`: clase grande (LOC lógico=115, métodos=26, campos=4)
- `Planeta`: clase grande (LOC lógico=113, métodos=20, campos=7, accesores=6 (no cuentan))
- `ReglaColisionMeteorito`: clase grande (LOC lógico=101, métodos=15, campos=3)

**Clases con baja cohesión (24)**

- `EstrategiaBase`: cohesión baja (0% de pares comparten campos)
- `RegistroEstrategiasVisuales`: cohesión baja (4% de pares comparten campos)
- `CuerpoCeleste`: cohesión baja (11% de pares comparten campos)
- `RenderizadorPixelArt`: cohesión baja (14% de pares comparten campos)
- `ReconstructorCuerpos`: cohesión baja (17% de pares comparten campos)
- `Meteorito`: cohesión baja (18% de pares comparten campos)
- `BusEventos`: cohesión baja (19% de pares comparten campos)
- `GestorImagenes`: cohesión baja (22% de pares comparten campos)
- `GestorEfectos`: cohesión baja (25% de pares comparten campos)
- `MotorFisicaPermisiva`: cohesión baja (27% de pares comparten campos)
- `EscenaFondoMenu`: cohesión baja (29% de pares comparten campos)
- `CapaIndicadoresSol`: cohesión baja (29% de pares comparten campos)
- `UtilidadRender`: cohesión baja (33% de pares comparten campos)
- `PoliticaRanurasGuardado`: cohesión baja (33% de pares comparten campos)
- `SlotHotbar`: cohesión baja (33% de pares comparten campos)
- `App`: cohesión baja (36% de pares comparten campos)
- `MotorFisicaBase`: cohesión baja (39% de pares comparten campos)
- `ControlTiempoWidget`: cohesión baja (40% de pares comparten campos)
- `ControladorColocacion`: cohesión baja (43% de pares comparten campos)
- `ResolvedorColisiones`: cohesión baja (44% de pares comparten campos)
- `RepositorioPartidasArchivo`: cohesión baja (46% de pares comparten campos)
- `VentanaProgresion`: cohesión baja (46% de pares comparten campos)
- `DesplazamientoCamara`: cohesión baja (47% de pares comparten campos)
- `SimulacionSolar`: cohesión baja (48% de pares comparten campos)

**Complejidad alta (5)**

- `TransformadorCuerpos`: complejidad alta (máx=16)
- `GestorImagenes`: complejidad alta (máx=13)
- `AsistenteOrbital`: complejidad alta (máx=12)
- `GestorEconomia`: complejidad alta (máx=10)
- `DetectorColisiones`: complejidad alta (máx=10)

> Los **100** hallazgo(s) de métodos largos o con muchos parámetros se listan en «Métodos/funciones largos o con muchos parámetros» y **no suman** a la columna S.

> El análisis no excluye tests: una clase de test que supere los umbrales también suma.

## 5. Evolución por commit

Métricas calculadas sobre el estado de `main` en **cada commit** (21 commits con trabajo), del primero al último. La calidad usa el perfil `exigente`; el aporte de código lo mide RepoSense. El índice puede **bajar** al crecer el proyecto: se acumulan señales.

```mermaid
xychart-beta
    title "Índice POO/SOLID a lo largo de los commits"
    x-axis ["f322f1e", "c2ce669", "9675cae", "3f34b49", "35383e8", "052c78e", "1b7118e", "1dacc07", "4ce6d34", "da0b4ed", "ae8da1f", "a8cddc8", "aad481c", "5fcf7a4", "d1dc1e2", "4dd77de", "90a21eb", "fc53423", "e02655f", "69c59c8", "37af155"]
    y-axis "Índice" 0 --> 100
    line [0, 0, 67, 65, 69, 67, 66, 70, 68, 67, 67, 67, 67, 67, 67, 67, 67, 67, 67, 67, 67]
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

**12/23 clases con actividad** · 15 commits · 5 merges

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
| 2026-11-20 | No | sin actividad | 0 / 0 |

#### Aportes fuera del calendario de clases — minibautista14-tech

_Sin aportes fuera del calendario de clases para minibautista14-tech._

<!-- actividad-clases:fin -->

---
_Informe generado por la cátedra. Los criterios y umbrales de medición corresponden al perfil `exigente` de `calidad.py`; el semáforo de aprobación, a `CRITERIOS-CALIDAD.md` (ver `README.md`). Ante dudas, revisá el archivo señalado._
<!-- evaluacion:fin -->
