# Employee Registration — Prueba técnica Parameta

Servicio REST que valida los datos de un empleado, lo registra a través de un
servicio SOAP que persiste en MySQL, y devuelve el empleado en JSON con su
**edad** y su **tiempo de vinculación** calculados en años, meses y días.

---

## Arquitectura

```
                    contracts/employee.xsd
                    (fuente de verdad del contrato)
                       │                    │
              genera   │                    │   genera
                       ▼                    ▼
  ┌─────────────────────────┐      ┌──────────────────────────┐      ┌─────────┐
  │   employee-rest-api     │ SOAP │  employee-soap-service   │ JPA  │  MySQL  │
  │        :8080            │─────▶│         :8081            │─────▶│  :3306  │
  │  valida · calcula       │      │  valida · persiste       │      │         │
  └─────────────────────────┘      └──────────────────────────┘      └─────────┘
```

Dos aplicaciones Spring Boot **independientes**, cada una con su propio `pom.xml`.
No comparten ningún módulo Java: **el contrato entre ellas es el XSD**, del que
ambas generan sus clases con XJC en tiempo de construcción.

Cada servicio se organiza **por funcionalidad**, con las capas dentro:

```
employee/
├── domain/            Java puro — sin Spring, sin JPA, sin anotaciones de framework
├── application/       casos de uso · frontera transaccional
└── infrastructure/    rest/ · soap/ · persistence/
```

Las dependencias apuntan hacia adentro: `domain` no depende de nada.

---

## Cómo ejecutarlo

### Con Docker (recomendado)

```bash
docker compose up --build -d
```

Levanta MySQL, el servicio SOAP y la API REST. La primera construcción tarda
varios minutos porque descarga las dependencias Maven.

Verifica que todo quedó arriba:

```bash
docker compose ps
```

### En local, sin Docker

Requiere **Java 21**. No necesitas instalar Maven: cada proyecto trae su wrapper.

```bash
docker compose up -d mysql

cd employee-soap-service && ./mvnw spring-boot:run
cd employee-rest-api     && ./mvnw spring-boot:run
```

---

## El endpoint

```
GET /api/v1/employees
```

```bash
curl 'http://localhost:8080/api/v1/employees?firstName=Andres&lastName=Lopez&documentType=CC&documentNumber=1020304050&birthDate=1995-03-15&hireDate=2020-08-01&position=Backend%20Developer&salary=8500000.00'
```

```json
{
  "id": 1,
  "firstName": "Andres",
  "lastName": "Lopez",
  "documentType": "CC",
  "documentNumber": "1020304050",
  "birthDate": "1995-03-15",
  "hireDate": "2020-08-01",
  "position": "Backend Developer",
  "salary": 8500000.00,
  "age":    { "years": 31, "months": 5, "days": 26 },
  "tenure": { "years": 6,  "months": 1, "days": 9  }
}
```

**Documentación interactiva:** http://localhost:8080/swagger-ui.html
**Contrato SOAP (WSDL):** http://localhost:8081/ws/employees.wsdl

---

## Equivalencia con los atributos del enunciado

El código está íntegramente en inglés. Esta es la correspondencia con los
nombres del enunciado:

| Enunciado | API | Tipo | Formato |
|---|---|---|---|
| Nombres | `firstName` | String | máx. 100 caracteres |
| Apellidos | `lastName` | String | máx. 100 caracteres |
| Tipo de Documento | `documentType` | Enum | `CC` · `CE` · `TI` · `PA` |
| Número de Documento | `documentNumber` | String | máx. 20 caracteres |
| Fecha de Nacimiento | `birthDate` | Date | `AAAA-MM-DD` (ISO-8601) |
| Fecha de Vinculación | `hireDate` | Date | `AAAA-MM-DD` (ISO-8601) |
| Cargo | `position` | String | máx. 100 caracteres |
| Salario | `salary` | **BigDecimal** | mayor que cero, 2 decimales |
| Edad actual | `age` | Objeto | `{ years, months, days }` |
| Tiempo de Vinculación | `tenure` | Objeto | `{ years, months, days }` |

> **`salary` se modeló como `BigDecimal`, no como `Double`.** El punto flotante
> binario no representa exactamente valores decimales y el error se acumula al
> sumar. La exactitud se conserva en las tres capas:
> `xs:decimal` → `BigDecimal` → `DECIMAL(15,2)`.
> Ver `docs/DECISIONES.md`, entrada 6.

---

## Validaciones

Las que pide el enunciado:

- Ningún atributo vacío
- Formato de fechas `AAAA-MM-DD`, validado por el contrato y por el enlace de datos
- El empleado debe ser **mayor de edad** (18 años cumplidos)

Las que no pide y se agregaron:

- `birthDate` no puede ser futura
- `hireDate` no puede ser futura
- `hireDate` no puede ser anterior a `birthDate`
- `salary` debe ser mayor que cero
- `documentType` debe pertenecer al catálogo
- `(documentType, documentNumber)` es único: no se registra dos veces al mismo empleado

La validación ocurre en **ambos** servicios. No es redundancia: el servicio SOAP
está expuesto en la red y no puede confiar en su llamador.

---

## Respuestas de error

Todos los errores siguen **RFC 7807 (`application/problem+json`)**.

| Situación | Status |
|---|---|
| Formato inválido o validación fallida | `400` |
| Regla de negocio violada (menor de edad, fechas incoherentes) | `422` |
| Documento ya registrado | `409` |
| Registro de empleados inalcanzable | `503` |
| Registro de empleados sin responder a tiempo | `504` |
| Registro de empleados falló | `502` |
| Error inesperado | `500` |

Los errores de validación incluyen el detalle por campo:

```json
{
  "type": "https://parameta.com/problems/validation-failed",
  "title": "Validation failed",
  "status": 400,
  "detail": "One or more fields are invalid.",
  "errors": [
    { "field": "birthDate", "message": "must follow the format YYYY-MM-DD", "rejectedValue": "15-03-1995" },
    { "field": "salary",    "message": "must be greater than zero",         "rejectedValue": "-5" }
  ]
}
```

Los errores técnicos devuelven un mensaje genérico al cliente y registran la
excepción completa en el log. Ningún detalle de la base de datos, del esquema ni
de las clases internas sale hacia el consumidor.

---

## Tests

```bash
cd employee-rest-api && ./mvnw test
```

| Suite | Qué verifica | Infraestructura |
|---|---|---|
| `ElapsedTimeTest` | Cálculo de periodos y casos límite (mismo día, 29 de febrero, orden invertido) | ninguna |
| `EmployeeTest` | Invariantes del dominio y frontera de la mayoría de edad | ninguna |
| `RegisterEmployeeUseCaseTest` | Que un menor **no** llega al registro; zona horaria de Bogotá | ninguna |
| `EmployeeControllerTest` | La tabla de errores completa vía `MockMvc` | solo capa web |

Los tests de reglas de negocio no levantan Spring, MySQL ni el servicio SOAP —
corren en milisegundos. Es la ventaja práctica de mantener el dominio aislado.

---

## Supuestos documentados

- **Mayoría de edad: 18 años**, calculada contra la fecha actual en zona horaria
  `America/Bogota`. El reloj se inyecta como dependencia (`Clock`) para que los
  cálculos sean deterministas y testeables.
- **Nacidos un 29 de febrero:** en años no bisiestos, `Period` corre el
  aniversario al 1 de marzo. Varios ordenamientos de tradición civil lo fijan el
  28 de febrero. Es una decisión de negocio con implicaciones legales, no técnica:
  queda señalada para validación.
- **El endpoint es `GET`** porque el enunciado lo exige. En producción sería
  `POST`: la operación no es segura ni idempotente, y los datos personales y
  salariales no deberían viajar en la query string. Ver `docs/DECISIONES.md`,
  entrada 7.

---

## Decisiones de arquitectura

El razonamiento completo — qué se decidió, por qué, y qué se descartó — está en
**[`docs/DECISIONES.md`](docs/DECISIONES.md)**.

---

## Stack

Java 21 · Spring Boot 4.1.1 · Spring Web Services 5.0.2 · JAXB 4 · Hibernate ·
MySQL 8.4 · Maven (wrapper) · JUnit 5 · Mockito · Docker Compose
