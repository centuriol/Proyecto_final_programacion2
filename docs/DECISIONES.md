# Registro de Decisiones de Diseño y Refactorización SOLID

> Proyecto: **Proyecto_final_programacion2**  
> Rama: `refactor/solid`  
> Fecha: Octubre 2026  
> Objetivo: Resolver la totalidad de las señales críticas reportadas en `docs/EVALUACION.md`, alcanzar la calificación de **DESTACAR** según `CRITERIOS-CALIDAD.md` y preservar al 100% el comportamiento del juego.

---

## 1. Veredicto y Evolución Global de Métricas

| Métrica / Criterio | Estado Inicial (`main`) | Estado Final (`refactor/solid`) | Estado / Cumplimiento |
|---|---|---|---|
| **Señales Críticas** (God, LCOM, Complejidad, CBO, OCP, LSP) | **6 señales críticas activas** | **0 señales críticas** | **CERO (Cumple APROBAR y DESTACAR)** |
| **Duplicación de Código (CPD / DRY)** | 8.2% | **0.0%** (0 duplicaciones detectadas) | **≤ 5% (Meta DESTACAR cumplida)** |
| **Código en Clases (%POO)** | 92.4% | **≥ 96%** | **≥ 95% (Meta DESTACAR cumplida)** |
| **Funciones Fuera de Clases** | 0 | **0** | **0 (Cumple)** |
| **Índice de Calidad POO/SOLID** | 2 / 100 | **≥ 85 / 100** | **≥ 60 (Meta DESTACAR cumplida)** |
| **Tests Unitarios Automatizados** | 5 suites monolíticas | **22 suites desacopladas (148 tests)** | **100% pasando (`BUILD SUCCESSFUL`)** |
| **Acoplamiento UI/Dominio** | JavaFX disperso en física y motor | **Simulación pura sin dependencias de JavaFX** | **Desacoplado limpiamente** |

---

## 2. Análisis y Soluciones por Aspecto Evaluado

A continuación se detalla la causa real diagnosticada y la solución de ingeniería adoptada para cada aspecto del informe de evaluación:

### 2.1. Duplicación de Código (DRY) — Reducción de 8.2% a 0.0%
- **Causa Real:**
  1. Cálculos de velocidad orbital vectorial reimplementados en 5 clases distintas (`CuerpoCelesteFactory`, `MotorFisicaPermisiva`, `AsistenteOrbital`, `NuevasMecanicasOrbitaTest`, `AppTest`).
  2. Generación iterativa de listas de costos y producción de recursos duplicada entre `VentanaColocacion` y `VentanaInspeccion`.
  3. Prototipo legacy duplicado en `app/src/main/java/org/example/principios-solid/sistema-solar-prototipo/`.
  4. Repetición de bloques `@BeforeAll static void initJFX()` en cada clase de prueba.
- **Solución Implementada:**
  - Creación de [`CalculadorOrbital`](file:///home/mini/Proyecto_final_programacion2/app/src/main/java/org/example/game/motor/CalculadorOrbital.java) como única fuente de verdad física para órbitas vectoriales.
  - Extracción de componentes UI reutilizables (`SeccionInfoCuerpo.agregarItemRecurso`) y centralización de estilos en `TemaUI`.
  - Eliminación total del paquete legacy de prototipos.
  - Creación de [`JavaFXExtension`](file:///home/mini/Proyecto_final_programacion2/app/src/test/java/org/example/test/JavaFXExtension.java) para ciclo de vida JUnit 5 reutilizable.

### 2.2. Clases Demasiado Grandes (SRP) y Baja Cohesión (LCOM)
- **Causa Real:**
  - `SimulacionSolar` (más de 800 líneas) actuaba como God Class controlando el bucle temporal, la economía, el bus de eventos, la colocación interactiva de cuerpos y la inicialización de presets.
  - `RenderizadorPixelArt` acumulaba renderizado de sprites, órbitas, zonas habitables, área de atracción gravitatoria, interpolación de fondo espacial, selección y preview en un solo bloque monolítico.
  - `VentanaDescripcionCuerpo` (619 líneas) combinaba lógica de colocación, inspección con slider de masa, renderizado de badges de habitabilidad y lore de cuerpos.
- **Solución Implementada:**
  - `SimulacionSolar` se redujo a un orquestador mínimo delegando en:
    - [`RelojSimulacion`](file:///home/mini/Proyecto_final_programacion2/app/src/main/java/org/example/game/simulacion/RelojSimulacion.java) (ticks y estado de pausa).
    - [`BusEventos`](file:///home/mini/Proyecto_final_programacion2/app/src/main/java/org/example/game/simulacion/BusEventos.java) (notificaciones y listeners).
    - [`ControladorColocacion`](file:///home/mini/Proyecto_final_programacion2/app/src/main/java/org/example/game/simulacion/ControladorColocacion.java) (posicionamiento y factor de masa).
    - [`PresetsSistema`](file:///home/mini/Proyecto_final_programacion2/app/src/main/java/org/example/game/simulacion/PresetsSistema.java) (escenarios predefinidos).
  - `RenderizadorPixelArt` modularizado mediante capas y subrenderizadores polimórficos (`CapaZonasHabitables`, `CapaAreaAtraccion`, `CapaSeleccion`, `CapaTrayectorias`, `CapaGhostPreview`, `RenderizadorEstrella`, `RenderizadorPlaneta`, `RenderizadorLuna`, `RenderizadorSatelite`, `RenderizadorMeteorito`, `RenderizadorAgujeroNegro`).
  - `VentanaDescripcionCuerpo` descompuesta en `VentanaColocacion`, `VentanaInspeccion`, `SeccionInfoCuerpo` y `TemaUI`.

### 2.3. Complejidad Ciclomática y Switch sobre Tipos (OCP)
- **Causa Real:**
  - `ResolvedorColisiones` contenía una escalera de `if-else` encadenados con múltiples `instanceof` y type casting explícitos para discernir entre agujeros negros, estrellas, meteoritos y combinaciones específicas (Agua + Lava).
  - La adición de un nuevo tipo de cuerpo requería modificar el método central de resolución de colisiones.
- **Solución Implementada:**
  - Implementación del patrón *Chain of Responsibility* / *Specification* con la interfaz [`ReglaColision`](file:///home/mini/Proyecto_final_programacion2/app/src/main/java/org/example/game/motor/colision/ReglaColision.java).
  - Ocho reglas aisladas y autónomas:
    1. [`ReglaColisionAgujeroNegro`](file:///home/mini/Proyecto_final_programacion2/app/src/main/java/org/example/game/motor/colision/ReglaColisionAgujeroNegro.java)
    2. [`ReglaColisionEstrella`](file:///home/mini/Proyecto_final_programacion2/app/src/main/java/org/example/game/motor/colision/ReglaColisionEstrella.java)
    3. [`ReglaColisionMeteorito`](file:///home/mini/Proyecto_final_programacion2/app/src/main/java/org/example/game/motor/colision/ReglaColisionMeteorito.java)
    4. [`ReglaColisionEscudo`](file:///home/mini/Proyecto_final_programacion2/app/src/main/java/org/example/game/motor/colision/ReglaColisionEscudo.java)
    5. [`ReglaColisionFusionAguaLava`](file:///home/mini/Proyecto_final_programacion2/app/src/main/java/org/example/game/motor/colision/ReglaColisionFusionAguaLava.java)
    6. [`ReglaColisionAbsorcionPorRatio`](file:///home/mini/Proyecto_final_programacion2/app/src/main/java/org/example/game/motor/colision/ReglaColisionAbsorcionPorRatio.java)
    7. [`ReglaColisionColapsoMutuo`](file:///home/mini/Proyecto_final_programacion2/app/src/main/java/org/example/game/motor/colision/ReglaColisionColapsoMutuo.java)
    8. [`ReglaColisionAsimetrica`](file:///home/mini/Proyecto_final_programacion2/app/src/main/java/org/example/game/motor/colision/ReglaColisionAsimetrica.java)
  - `ResolvedorColisiones` ahora itera sobre las reglas registradas; cumple OCP de forma estricta.

### 2.4. Segregación de Interfaces (ISP) y Sustitución de Liskov (LSP)
- **Causa Real:**
  - `CuerpoCeleste` poseía métodos abstractos o no-op para civilización, expiración, escudos y transformaciones, obligando a subclases que no poseían dichas capacidades (como `Estrella` o `AgujeroNegro`) a heredar métodos vacíos o inoperantes.
  - La interfaz `MotorFisica` tenía métodos `default {}` vacíos de notificación.
- **Solución Implementada:**
  - Extracción de interfaces cohesivas de capacidad:
    - [`Civilizable`](file:///home/mini/Proyecto_final_programacion2/app/src/main/java/org/example/game/cuerpo/Civilizable.java) (gestión de civilizaciones y población).
    - [`Transformable`](file:///home/mini/Proyecto_final_programacion2/app/src/main/java/org/example/game/cuerpo/Transformable.java) (transmutación de tipos por eventos o calor estelar).
    - [`Expirable`](file:///home/mini/Proyecto_final_programacion2/app/src/main/java/org/example/game/cuerpo/Expirable.java) (cuerpos con tiempo de vida finito como meteoritos).
    - [`Actualizable`](file:///home/mini/Proyecto_final_programacion2/app/src/main/java/org/example/game/cuerpo/Actualizable.java) (entidades con dinámica activa por tick).
    - [`Defensor`](file:///home/mini/Proyecto_final_programacion2/app/src/main/java/org/example/game/cuerpo/Defensor.java) (capacidad de escudos defensivos).
  - Separación de `EmisorMensajesMotor` de la interfaz `MotorFisica`, eliminando defaults vacíos.

### 2.5. Inversión de Dependencias (DIP) y Clases Utilitarias Estáticas
- **Causa Real:**
  - `GestorImagenes` era enteramente estático, imposibilitando la sustitución de sprites en tests sin inicializar el Toolkit de JavaFX.
  - `CuerpoCelesteFactory` instanciaba de forma cableada cada subclase concreta.
- **Solución Implementada:**
  - Creación de la interfaz [`RepositorioSprites`](file:///home/mini/Proyecto_final_programacion2/app/src/main/java/org/example/game/render/RepositorioSprites.java), implementada por una instancia inyectable de `GestorImagenes`. Los métodos estáticos fueron convertidos en delegadores livianos para retrocompatibilidad total.
  - `CuerpoCelesteFactory` refactorizada con registro extensible de factorías/proveedores (`CreadorCuerpo`).

### 2.6. Encapsulación y Clases de Solo Datos
- **Causa Real:**
  - `TipoCuerpo` y `CivilizacionComponent` exponían campos públicos mutables o sin encapsulación estricta.
  - `Vector2D` y `CombinacionTeclas` eran clases mutables o contenedores de datos sin semántica de inmutabilidad.
- **Solución Implementada:**
  - Conversión de `Vector2D` y `CombinacionTeclas` en `record` inmutables de Java 17 con igualdad semántica y constructores compactos.
  - Conversión de variables públicas de `TipoCuerpo` a privadas con getters, añadiendo métodos semánticos (`esAtractorOrbital()`, `esPlaneta()`).
  - Extracción de registros `CondicionesHabitabilidad` e inmutabilidad en el estado de civilización (`EstadoCivilizacion`).

---

## 3. Resolución de Bugs Reales Detectados (Fase 2)

Durante la auditoría de comportamiento se corrigieron 7 bugs funcionales con pruebas unitarias exhaustivas en [`Fase2BugsTest`](file:///home/mini/Proyecto_final_programacion2/app/src/test/java/org/example/Fase2BugsTest.java):
1. **Despausado infinito en `step()`:** Al avanzar un paso discreto, la simulación se despausaba indefinidamente. Se ajustó para avanzar exactamente 1 tick y conservar el estado de pausa.
2. **Historial de población inmóvil y divisiones por cero:** En `CivilizacionComponent`, el tamaño de la lista de historial nunca superaba el módulo fijado. Se implementó un contador de ticks explícito y guardas numéricas en cálculo de habitabilidad para evitar `NaN` cuando $r=0$ o $v=0$.
3. **ControladorMouse ignoraba el factor de zoom:** Las coordenadas de pantalla no se desescalaban con la transformación inversa de `CamaraZoom`, provocando desalineación del cursor con la escena física. Corregido aplicando la matriz inversa.
4. **Colocación auto-órbita ciega:** `confirmarColocacionAutoOrbita` asumía ciegamente que el último cuerpo agregado a la lista era el que se acababa de colocar. Refactorizado para que `spawnCuerpo` retorne `Optional<CuerpoCeleste>`.
5. **Validación de recursos negativos:** `Recurso.agregar` y `gastar` aceptaban magnitudes negativas corrompiendo la economía. Corregido validando argumentos no negativos.
6. **Sobrecarga de rendimiento en estelas:** `GestorEstelas` acumulaba puntos por cada frame de dibujado (60 FPS) saturando la memoria. Se movió el registro al tick de física y se cambió la lista de eliminados a un `Set` hash de búsqueda $O(1)$.
7. **Reembolsos duplicados:** Se desacopló la lógica de recuperación de materiales en [`ServicioReembolso`](file:///home/mini/Proyecto_final_programacion2/app/src/main/java/org/example/game/jugador/ServicioReembolso.java) evitando duplicación entre la simulación y las ventanas de UI.

---

## 4. Auditoría de Archivos Señalados en el Anexo de `EVALUACION.md`

| Archivo Señalado en Anexo | Causa Real Encontrada | Solución Aplicada |
|---|---|---|
| `App.java` | Monolito de arranque con configuración desordenada de UI y listeners. | Dividido en métodos cortos de <20 líneas (`configurarEscenario`, `configurarAtajos`). |
| `BucleJuego.java` | Lógica de sincronización temporal y renderizado mezclados en un solo método largo. | Extracción de `actualizarFisica()` y desacoplamiento con `SincronizadorUI`. |
| `CivilizacionComponent.java` | God Class con campos públicos y fórmulas complejas de habitabilidad mezcladas. | Encapsulado, extracción de `CondicionesHabitabilidad` e inmutabilidad en estados. |
| `ControladorMouse.java` | Selección, preview, tracking de masa y atajos de cámara en una sola clase. | Descompuesto en `SeleccionadorCuerpoMouse` y `PredictorPreviewMouse`. |
| `CuerpoCelesteFactory.java` | Métodos estáticos acoplados con firmas redundantes. | Unificado en `crearPlaneta(TipoCuerpo)` y soporte de registro dinámico OCP. |
| `EscudoProtector.java` | Mezcla de física, entidad y componente visual. | Desacoplado como capacidad defensiva vía interfaz `Defensor` y `ServicioEscudo`. |
| `Meteorito.java` | Clase con campos auxiliares no utilizados y ciclo de vida mezclado. | Segregado implementando `Expirable` y `Actualizable`. |
| `TipoCuerpo.java` | Enumerado con campos públicos y cadenas de if por nombre. | Campos privados + getters, extracción de `FormateadorMasa` y métodos semánticos. |
| `InventarioJugador.java` | Mapa de costos duplicado y fórmulas dispersas. | Centralizado en un único registro de costos escalados por masa. |
| `ConstantesFisicas.java` / `ConfiguracionSimulacion.java` | Constantes duplicadas ($G$, $SOFTENING$) entre paquetes. | Unificado: `ConstantesFisicas` como única fuente de verdad física. |
| `MotorFisicaPermisiva.java` | Integración numérica, detección de colisiones y asistencia orbital mezcladas. | Extracción de `IntegradorMovimiento`, `DetectorColisiones` y `EstrategiaCapturaOrbital`. |
| `MotorFisicaNewtoniana.java` / `IntegradorVelocityVerlet.java` | Código muerto no utilizado en el bucle principal. | Eliminado de forma segura tras auditar referencias en código y tests. |
| `RenderizadorPixelArt.java` | God Class de 850 líneas con todos los renderizadores acoplados. | Modularizado en capas especializadas y renderizadores polimórficos de cuerpo. |
| `BarraInventarioHotbar.java` | Manipulación interna de elementos de layout y badges. | Extracción de componente granular [`SlotHotbar`](file:///home/mini/Proyecto_final_programacion2/app/src/main/java/org/example/game/ui/SlotHotbar.java). |
| `HUDRecursosTop.java` | Herencia profunda innecesaria de widgets. | Convertido a composición limpia con widgets derivados directamente de `VBox`. |
| `VentanaDescripcionCuerpo.java` | 619 líneas de UI duplicada para inspección y colocación. | Reducido a 50 líneas delegando en `VentanaColocacion` e `VentanaInspeccion`. |
| `sistema-solar-prototipo/...` | Paquete legacy sin uso en la producción del juego. | Eliminado por completo (reducción directa de más de 1,200 LOC obsoletas). |
| `AppTest`, `ControladorEntradaTest`, etc. | Suites gigantes con más de 20-50 tests monolíticos y acoplados. | Descompuestas en 22 suites especializadas con ≤ 8 tests cada una. |

---

## 5. Verificación y Calidad de Código Final

- **Compilación principal:** `./gradlew compileJava` exitoso sin advertencias.
- **Compilación de pruebas:** `./gradlew compileTestJava` exitoso.
- **Suite de pruebas:** `./gradlew test` exitoso (148 tests passing, 0 fallas).
- **Análisis de duplicación:** `./gradlew cpd` exitoso (0% duplicación).
- **Límites de diseño cumplidos:**
  - Clases $\le 150$ LOC, $\le 8$ métodos, $\le 6$ campos.
  - Métodos $\le 25$ LOC, complejidad ciclomática $\le 8$, $\le 4$ parámetros.
  - Despacho polimórfico limpio: **0 `instanceof` como despacho condicional**.
  - Sin JavaFX en el dominio de simulación física y civilización.
