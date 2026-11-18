# Employee Management

A REST API for managing employees, built with Spring Boot 4 and Kotlin, backed by MySQL.

## Tech stack

- Kotlin 2.3 on Java 21
- Spring Boot 4.1: Web MVC, Data JPA (Hibernate 7), Actuator, Jackson 3
- MySQL 8.0, with the schema managed by Flyway 12
- springdoc-openapi 3 (Swagger UI)
- MapStruct 1.6 for DTO mapping
- Gradle 9.9 (Kotlin DSL)
- Tests: JUnit 5, MockK, Testcontainers 2, JaCoCo

## Project layout

```
src/main/kotlin/com/tm/employeemanagement/
├── controller/   EmployeeController: REST endpoints under /employee
├── service/      EmployeeService: business logic
├── repo/         EmployeeRepo (JPA) and CustomTraceRepository (actuator HTTP traces)
├── model/        Employee entity
├── dto/          EmployeeRequest (create/update body) and EmployeeResponse (returned to clients)
├── mapper/       EmployeeMapper: MapStruct mapper between the Employee entity and the DTOs
├── advice/       MyControllerAdvice: maps exceptions to HTTP responses
└── utility/      CorsUtility: CORS config for the Angular frontend (http://localhost:4200)
```

## Running the app

### With Docker Compose (recommended)

```bash
docker compose up
```

This starts:

- `mysql`: MySQL 8 on port `3306`, with the `employee-management` database and data kept in the `mysql-data` volume
- `app`: the API on <http://localhost:8080>, started once MySQL is healthy

The root password defaults to `root`. To change it, set `MYSQL_ROOT_PASSWORD` in your shell or in a `.env` file.

### Locally

You need JDK 21 (Gradle 9 itself needs Java 17 or newer, so make sure `JAVA_HOME` points at one) and a MySQL server on `localhost:3306` with a database named `employee-management`.

```bash
export MYSQL_ROOT_PASSWORD=yourpassword   # defaults to "root"
./gradlew bootRun
```

Flyway creates the schema on startup (see [Database migrations](#database-migrations)).

> If you get `permission denied: ./gradlew`, either run `sh ./gradlew ...` or mark the wrapper executable with
> `git update-index --chmod=+x gradlew && chmod +x gradlew`.

## Database migrations

Flyway manages the schema. Migrations live in `src/main/resources/db/migration` and run automatically on startup.

- To change the schema, add a new file named `V<next number>__<description>.sql`, for example
  `V2__add_department_to_employee.sql`. Don't edit a migration that has already run; Flyway will refuse to start
  because its checksum changed.
- Hibernate runs with `ddl-auto=validate`, so it no longer changes the schema. If an entity doesn't match the tables,
  the app fails at startup instead of drifting.
- A database created before Flyway was added already has the `employee` tables. Flyway baselines it at V1 and only
  runs newer migrations (`spring.flyway.baseline-on-migrate=true`).

## API

Interactive docs are at <http://localhost:8080/swagger-ui.html> while the app is running. The OpenAPI spec is
at <http://localhost:8080/v3/api-docs>.

Base path: `/employee`

| Method   | Path                  | Description                                         |
|----------|-----------------------|-----------------------------------------------------|
| `GET`    | `/all`                | Paginated list of employees                         |
| `GET`    | `/find/{id}`          | Get one employee by id                              |
| `POST`   | `/add`                | Create an employee (returns `201`)                  |
| `PUT`    | `/update/{id}`        | Update an employee                                  |
| `DELETE` | `/delete/{id}`        | Delete an employee; returns the number of rows deleted |

Query parameters for `GET /all`:

| Param        | Default | Description                                  |
|--------------|---------|----------------------------------------------|
| `pageSize`   | `10`    | Items per page                               |
| `pageNumber` | `0`     | Zero-based page index                        |
| `sortBy`     | `id`    | Field to sort by                             |
| `email`      | none    | Return only employees whose email contains this value |

Create and update requests take the same body: `name`, `email`, `jobTitle`, `phone` and `imageUrl`. Responses also
include `id` and `employeeCode`. The server assigns both: `employeeCode` is a UUID generated on create, and neither can
be changed through the API. If a request includes them, they're ignored.

### Examples

```bash
# Create
curl -X POST http://localhost:8080/employee/add \
  -H "Content-Type: application/json" \
  -d '{
        "name": "Dave Rogers",
        "email": "dave@tm.com",
        "jobTitle": "Python",
        "phone": "4325678900",
        "imageUrl": "https://bootdey.com/img/Content/avatar/avatar2.png"
      }'

# List, filtered by email
curl "http://localhost:8080/employee/all?pageSize=5&pageNumber=0&email=tm.com"

# Get by id
curl http://localhost:8080/employee/find/1

# Update
curl -X PUT http://localhost:8080/employee/update/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "Dave Rogers", "email": "dave.rogers@tm.com", "jobTitle": "Python", "phone": "4325678900"}'

# Delete
curl -X DELETE http://localhost:8080/employee/delete/1
```

### Errors

| Situation                 | Status | Body                  |
|---------------------------|--------|-----------------------|
| Employee not found        | `404`  | `Results not found`   |
| `IllegalArgumentException` | `400` | The exception message |

## Monitoring

These Spring Boot Actuator endpoints are exposed:

- `GET /actuator/health`: health, with full details
- `GET /actuator/metrics`: application metrics
- `GET /actuator/httpexchanges`: the last 1000 HTTP requests

## Testing

- **Unit tests** (`EmployeeServiceTest`) mock the repository with MockK.
- **Integration tests** (`EmployeeControllerIntegrationTest`, `OpenApiIntegrationTest`,
  `EmployeeManagementApplicationTests`) extend
  `AbstractIntegrationTest`. It uses [Testcontainers](https://testcontainers.com) to start a real `mysql:8.0`
  container, which every test class shares, and Flyway applies the real migrations to it. Docker must be running.

### Run the tests locally

```bash
./gradlew test
```

### Run the tests in Docker

```bash
docker compose run --rm tests
```

The `tests` service sits behind the `test` profile, so `docker compose up` doesn't start it. It mounts the host's Docker
socket so Testcontainers can start MySQL alongside it.

### Code coverage

Every `test` run also generates a JaCoCo report and prints a summary:

```
===== Code Coverage =====
INSTRUCTION   92.10%  (408/443)
BRANCH       100.00%  (2/2)
LINE          95.16%  (59/62)
...
```

Open the full HTML report at `build/reports/jacoco/test/html/index.html`. The XML version is in the same folder, for
CI tools. When you run the tests through Docker Compose, the reports are written to the same `./build/reports` folder
on your machine.
