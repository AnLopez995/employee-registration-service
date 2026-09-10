# Registro de decisiones de arquitectura

Cada entrada responde: **qué se decidió**, **por qué**, y **qué se descartó**.
Las decisiones marcadas con ⚠️ se apartan de lo literal del enunciado y están justificadas.

---

## 1. Dos servicios independientes, sin módulo Java compartido

`employee-rest-api` (8080) y `employee-soap-service` (8081) son proyectos Maven separados, cada uno con su `pom.xml` y su wrapper.

**Por qué:** el contrato entre ellos es el XSD, no un `.jar`. Un módulo `commons` compartido los ataría en tiempo de compilación y haría falso el argumento de despliegue independiente.

**Descartado:** módulo `commons` con el modelo compartido.

**Consecuencia aceptada:** `LocalDateConverter` está duplicado (12 líneas) en ambos servicios. Cuesta menos que el acoplamiento que evita.

---

## 2. Contract-first con Spring-WS en ambos extremos

El XSD (`contracts/employee.xsd`) se escribe primero; XJC genera las clases Java en ambos proyectos; Spring-WS deriva el WSDL en tiempo de ejecución.

**Por qué:** el contrato es lo que otros equipos consumen y no puede ser un efecto colateral de decisiones internas de implementación. Además el cliente SOAP obliga a generar código de todas formas, así que el costo marginal de hacerlo también en el servidor es bajo.

**Descartado:** code-first (anotar POJOs y dejar que el framework publique el WSDL).

**Descartado:** Apache CXF. Spring-WS es contract-first por diseño y cubre servidor y cliente con un solo modelo mental.

---

## 3. `employee.xsd` es artefacto de build y de runtime; `bindings.xjb` solo de build

| | SOAP (publica) | REST (consume) |
|---|---|---|
| `employee.xsd` | build + runtime (classpath) | solo build |
| `bindings.xjb` | solo build | solo build |

**Por qué:** el servicio SOAP publica su propio esquema al servir el WSDL, así que el XSD debe viajar dentro del `.jar`. El `.xjb` termina su trabajo cuando XJC genera las clases: la instrucción queda grabada en el bytecode como `@XmlJavaTypeAdapter`.

---

## 4. Binding JAXB con `parseMethod` y `printMethod`, no con `adapter`

**Por qué:** `adapter` es una extensión propietaria de XJC y exige activar el modo `-extension`. `parseMethod` y `printMethod` son parte del estándar JAXB y producen el mismo resultado: XJC sintetiza el `XmlAdapter` (`Adapter1`).

**Principio:** estándar sobre extensión de vendor.

---

## 5. Empaquetado por funcionalidad, no por capa

```
employee/
├── domain/            Java puro, sin anotaciones de framework
├── application/       casos de uso
└── infrastructure/    rest/ · soap/ · persistence/
```

**Por qué:** con empaquetado por capa, `domain/` se convierte en un cajón de sastre al agregar `company`, `payroll`, etc. Además impide usar visibilidad package-private como frontera real entre funcionalidades.

**Descartado:** `controllers/`, `services/`, `repositories/` en la raíz.

**Regla de dependencias:** las flechas apuntan hacia adentro. `domain` no depende de nada.

---

## 6. ⚠️ `salary` es `BigDecimal`, no `Double`

El enunciado dice `Double`. Se modeló como `xs:decimal` → `BigDecimal` → `DECIMAL(15,2)`.

**Por qué:** `double` es punto flotante binario (IEEE 754) y no representa exactamente valores decimales. `0.1 + 0.2` da `0.30000000000000004`; mil salarios sumados acumulan error visible. Parameta opera en el sector financiero.

**La exactitud se conserva en las tres capas.** De nada sirve `BigDecimal` en Java si la columna es `DOUBLE`.

**Trampas conocidas:** `new BigDecimal(0.1)` reintroduce el error — siempre construir desde `String`. Y `equals()` compara valor *y* escala: para montos se usa `compareTo()` o `signum()`.

---

## 7. ⚠️ El endpoint es `GET` porque el enunciado lo exige

**Problema de semántica:** `GET` es seguro e idempotente por RFC 9110; este escribe en base de datos. Un pre-fetch del navegador o un reintento automático duplicaría el registro.

**Problema de seguridad:** nombre, documento, fecha de nacimiento y salario viajan en la query string, que queda en logs de acceso, proxies, historial del navegador y cachés intermedias.

**Decisión:** se implementa como `GET` porque es el requisito, y se documenta en el código. El caso de uso está desacoplado del transporte: exponerlo también como `POST` son tres líneas.

---

## 8. `Clock` inyectado en lugar de `LocalDate.now()`

**Por qué:** el tiempo es una dependencia externa, igual que una base de datos. Consultarlo desde la lógica la vuelve no testeable y deja la zona horaria implícita (la del sistema operativo). Se fija `America/Bogota`.

**Evidencia:** los tests verifican la frontera exacta de la mayoría de edad — el día antes del cumpleaños 18 y el día exacto —, imposible con `LocalDate.now()`.

**Pendiente conocido:** `@Past` y `@PastOrPresent` de Bean Validation usan el reloj del sistema, no este `Clock`. Se resuelve con un `ClockProvider` propio.

---

## 9. Supuesto: personas nacidas el 29 de febrero

`Period.between` corre el aniversario al **1 de marzo** en años no bisiestos. Varios ordenamientos de tradición civil lo fijan el **28 de febrero**.

**Decisión:** se usa el comportamiento por defecto y se documenta el supuesto. No se implementa configuración: es una decisión de negocio con implicaciones legales, no técnica, y afecta a 1 de cada 1461 personas un día al año.

---

## 10. Documento duplicado produce `409 Conflict`, no idempotencia

Restricción única sobre `(document_type, document_number)` en la base de datos.

**Por qué en la base y no en código:** el motor no tiene condiciones de carrera.

**Descartado:** comportamiento idempotente (devolver el `id` existente). Tiene sentido si el llamador reintenta — relevante justamente porque es un `GET` — pero exige una clave de idempotencia explícita que el enunciado no plantea.

**Detalle de implementación:** se usa `saveAndFlush`, no `save`. `save()` puede diferir el `INSERT` hasta el commit de la transacción, que ocurre *después* de retornar el método, y el `catch` nunca se dispararía.

---

## 11. `@Enumerated(EnumType.STRING)`

**Por qué:** el valor por defecto de JPA es `ORDINAL`, que guarda la posición del enum. Reordenar los valores o insertar uno en la mitad cambia el significado de todos los registros existentes, en silencio y sin error.

---

## 12. Los SOAP Faults son controlados; nada técnico cruza la frontera

El `defaultFault` responde `Internal service error`. Las excepciones mapeadas llevan `faultcode` `CLIENT` y un `<errorCode>` legible por máquina en el `detail`.

**Por qué:** antes de esto, el mensaje crudo de Hibernate — tabla, columnas, nombre de la restricción y el `INSERT` completo — viajaba dentro del fault hacia cualquiera que consumiera el servicio SOAP.

**Por qué `errorCode` y no el `faultstring`:** el código es contrato legible por máquina; el mensaje es para humanos y puede cambiar sin previo aviso. Hacer coincidencia de cadenas contra el mensaje es frágil.

**Correspondencia:** `faultcode` `CLIENT`/`SERVER` equivale a HTTP `4xx`/`5xx`.

---

## 13. Errores HTTP con RFC 7807 (`ProblemDetail`)

| Caso | Status |
|---|---|
| Conversión o validación fallida | 400 |
| Regla de negocio violada (menor de edad, fechas incoherentes) | 422 |
| Documento ya registrado | 409 |
| Registro inalcanzable | 503 |
| Registro no responde a tiempo | 504 |
| Registro falló | 502 |
| Cualquier otra | 500 |

**400 vs 422:** `400` es "no pude procesar tu petición"; `422` es "la entendí, pero viola una regla de negocio". Muchos equipos usan `400` para todo; distinguir le dice al cliente si el problema es de forma o de fondo.

**Asimetría deliberada:** los errores de negocio llevan el mensaje real, porque el cliente puede y debe corregir. Los técnicos llevan mensaje genérico y la excepción completa va al log.

**Red de seguridad:** el adaptador captura `WebServiceClientException`, la raíz de la jerarquía del puerto, así que cualquier modo de fallo no anticipado aterriza en `502` y no se escapa como `500`.

---

## 14. Mensajes de validación explícitos, no los de Hibernate Validator

**Por qué:** los mensajes por defecto se resuelven según el locale del servidor — la misma API respondería distinto en otra máquina. **Los mensajes de error son parte del contrato.**

Los `typeMismatch` también se reemplazan: el mensaje de Spring incluye nombres de clases internas (`java.time.LocalDate`), que es una fuga de información en versión pequeña. Se derivan mensajes específicos desde `FieldError.getCodes()`, con degradación a un mensaje genérico si Spring cambia esos códigos.

---

## 15. DTO de entrada mutable, dominio inmutable

`RegisterEmployeeRequest` es una clase con setters; `Employee` es un `record`.

**Por qué:** con enlace por constructor (records), si **un** campo falla la conversión, Spring no puede construir la instancia y Bean Validation **no se ejecuta sobre ningún campo**. El cliente recibiría un error a la vez, en rondas sucesivas.

Con enlace por setters, Spring registra la conversión fallida como error de campo y sigue enlazando y validando el resto: el cliente recibe todos sus errores en una sola respuesta.

**La mutabilidad se queda en el borde**, donde el objeto se construye, se mapea al dominio y se descarta. El modelo de dominio sigue siendo inmutable.

---

## 16. Deuda técnica consciente

| Decisión actual | Correcto en producción |
|---|---|
| `spring.jpa.hibernate.ddl-auto=update` | Flyway o Liquibase: versiona, revierte y no deja columnas huérfanas 
| Sin autenticación | El `GET` expone datos personales sin control de acceso |
| Sin circuit breaker | Hay timeouts explícitos; Resilience4j sería el siguiente paso |
| Credenciales en `application.properties` | Gestor de secretos; hoy se leen de variables de entorno con valor por defecto |
