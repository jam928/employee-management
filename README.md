# Employee Management

A REST API for managing employees, built with Spring Boot 4 and Kotlin, backed by MySQL, with employee images stored in MinIO.

## Tech stack

- Kotlin 2.3 on Java 21
- Spring Boot 4.1: Web MVC, Data JPA (Hibernate 7), Actuator, Jackson 3
- MySQL 8.0, with the schema managed by Flyway 12
- MinIO (S3-compatible object storage) for employee images, via the MinIO Java SDK 9
- springdoc-openapi 3 (Swagger UI)
- MapStruct 1.6 for DTO mapping
- Gradle 9.9 (Kotlin DSL)
- Tests: JUnit 5, MockK, Testcontainers 2, JaCoCo

## Project layout

```
src/main/kotlin/com/tm/employeemanagement/
├── controller/   EmployeeController: REST endpoints under /employee
├── service/      EmployeeService: business logic; ImageStorageService: stores images in MinIO
├── repo/         EmployeeRepo (JPA) and CustomTraceRepository (actuator HTTP traces)
├── model/        Employee entity
├── dto/          EmployeeRequest (create/update body) and EmployeeResponse (returned to clients)
├── mapper/       EmployeeMapper: MapStruct mapper between the Employee entity and the DTOs
├── config/      MinioConfig (MinIO client + minio.* properties) and OpenApiConfig
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
- `minio`: MinIO image storage. The S3 API is on <http://localhost:9000> (images are served from here) and the web
  console is on <http://localhost:9001>. Files are kept in the `minio-data` volume.
- `app`: the API on <http://localhost:8080>, started once MySQL and MinIO are healthy

The MySQL root password defaults to `root`, and the MinIO login defaults to `minioadmin` / `minioadmin`. To change them,
set `MYSQL_ROOT_PASSWORD`, `MINIO_ROOT_USER` and `MINIO_ROOT_PASSWORD` in your shell or in a `.env` file.

The official `minio/minio` image is no longer published, so Compose uses `pgsty/minio`, a maintained community build of
the same server. To use a different image, set `MINIO_IMAGE`.

### Locally

You need JDK 21 (Gradle 9 itself needs Java 17 or newer, so make sure `JAVA_HOME` points at one), a MySQL server on `localhost:3306` with a database named `employee-management`, and MinIO on `localhost:9000`. The easiest way to get both is `docker compose up -d mysql minio`.

```bash
export MYSQL_ROOT_PASSWORD=yourpassword   # defaults to "root"
./gradlew bootRun
```

Flyway creates the schema on startup (see [Database migrations](#database-migrations)).

## Image storage (MinIO)

Employee images are uploaded through the API and stored in the `employee-images` bucket. The app creates the bucket on
the first upload and gives it an anonymous read-only policy, so the frontend can show an image by putting the returned
`imageUrl` in an `<img>` tag. Uploads are only possible through the API.

| Property            | Env var              | Default                 | Description                                   |
|---------------------|----------------------|-------------------------|-----------------------------------------------|
| `minio.endpoint`    | `MINIO_ENDPOINT`     | `http://localhost:9000` | Where the app reaches MinIO                   |
| `minio.public-url`  | `MINIO_PUBLIC_URL`   | `http://localhost:9000` | Base URL browsers use to load images          |
| `minio.access-key`  | `MINIO_ROOT_USER`    | `minioadmin`            | MinIO access key                              |
| `minio.secret-key`  | `MINIO_ROOT_PASSWORD`| `minioadmin`            | MinIO secret key                              |
| `minio.bucket`      | `MINIO_BUCKET`       | `employee-images`       | Bucket for employee images                    |

`endpoint` and `public-url` differ under Docker Compose: the app talks to `http://minio:9000` on the Compose network,
while browsers load images from `http://localhost:9000`.

Uploads must be JPEG, PNG, GIF or WebP, up to 5 MB. The type is checked from the file's contents, not from its name or
the content type the client sends. SVG isn't accepted because it can contain scripts. When an employee's image is
replaced or removed, or the employee is deleted, the old file is deleted from MinIO. Image URLs that point somewhere
else, such as an external avatar link, are never touched.

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
| `DELETE` | `/delete/{id}`        | Delete an employee and its stored image; returns the number of rows deleted |
| `PUT`    | `/{id}/image`         | Upload or replace the employee's image (multipart, part named `file`) |
| `DELETE` | `/{id}/image`         | Remove the employee's image                         |

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

# Upload or replace an image (returns the updated employee, with imageUrl pointing at MinIO)
curl -X PUT http://localhost:8080/employee/1/image -F "file=@avatar.png"

# Remove the image
curl -X DELETE http://localhost:8080/employee/1/image

# Delete
curl -X DELETE http://localhost:8080/employee/delete/1
```

### Errors

| Situation                 | Status | Body                  |
|---------------------------|--------|-----------------------|
| Employee not found        | `404`  | `Results not found`   |
| `IllegalArgumentException` | `400` | The exception message |
| Image upload missing, empty or not a supported image | `400` | Why it was rejected |
| Image larger than 5 MB    | `413`  | `Image is too large`  |
| MinIO unreachable          | `503`  | `Image storage is unavailable` |

## Monitoring

These Spring Boot Actuator endpoints are exposed:

- `GET /actuator/health`: health, with full details
- `GET /actuator/metrics`: application metrics
- `GET /actuator/httpexchanges`: the last 1000 HTTP requests

## Testing

- **Unit tests** (`EmployeeServiceTest`, `ImageStorageServiceTest`) mock the repository and the MinIO client with MockK.
- **Integration tests** (`EmployeeControllerIntegrationTest`, `OpenApiIntegrationTest`,
  `EmployeeManagementApplicationTests`) extend
  `AbstractIntegrationTest`. It uses [Testcontainers](https://testcontainers.com) to start a real `mysql:8.4`
  container and a MinIO container, which every test class shares, and Flyway applies the real migrations to MySQL.
  `EmployeeImageIntegrationTest` uploads real images to MinIO and fetches them back. Docker must be running.

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
