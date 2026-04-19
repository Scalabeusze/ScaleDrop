# ScaleDrop IAM

---

## Local Development

### Running tests

```bash
./gradlew clean build
```

### Running the application locally

```bash
./gradlew bootRun
```

### Running with Docker

Build the image:

```bash
docker build -t sd-iam:local .
```

Create a local env file:

```bash
cp .env.example .env
```

`.env.example` intentionally contains dummy values. Update `.env` with values for your external PostgreSQL instance and replace password values for anything beyond a throwaway local setup. Then run:

```bash
docker compose up --build
```

The service will be available at:

```text
http://localhost:7300/sd-iam
```

Account management endpoints:

```text
http://localhost:7300/sd-iam/api/v1/accounts/*
```

Health endpoint:

```text
http://localhost:7300/sd-iam/actuator/health
```

Detailed documentation (SwaggerUI):

```text
http://localhost:7300/sd-iam/swagger-ui/index.html
```

### Runtime configuration

Required environment variables for containerized runs:

- `DB_URL` in the format `host:port/database_name`
- `DB_USERNAME`
- `DB_PASSWORD`

The following values are defaulted by Spring in `application.yml` and do not need to be set in Compose unless you want to override them outside this setup:

- `DB_SCHEMA` defaults to `sd_iam`
- `SERVER_SERVLET_CONTEXT_PATH` defaults to `/sd-iam`
- `SECURITY_ACCESS_DOCUMENTATION_USERNAME` defaults to `documentation`
- `SECURITY_ACCESS_INTERNAL_USERNAME` defaults to `scaledrop`

**NOTE:** A PostgreSQL instance is an external dependency to this repository and is not included in `compose.yaml`.
