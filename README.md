# Alert Operations Challenge

A focused FullStack implementation for registering and querying operational alerts linked to an order. It demonstrates a contract-first workflow, a Spring Boot API, a Next.js TypeScript client, unit tests, controlled error handling, and AWS infrastructure as code.

## Technology choices

| Layer | Choice | Reason |
| --- | --- | --- |
| API | Java 17, Spring Boot 3, JPA | Mature validation, centralized error handling, testability, and clear layering. |
| Local persistence | H2 | Zero-install execution for the evaluator; repository boundary remains replaceable. |
| Web client | Next.js 16, React, TypeScript | Typed client contract with a compact, accessible interface. |
| AWS | API Gateway, Lambda, Secrets Manager, SAM | Fits the requested reference architecture with explicit least-privilege access. |

## Quick start

### Prerequisites

- Java 17 JDK or newer
- Node.js 20.9 or newer
- Internet access on the first run to download Maven and npm dependencies

No local database, Docker, AWS account, or Spring installation is required.

### 1. Run the backend

```bash
cd backend
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd spring-boot:run`. The API starts on `http://localhost:8080`. Health is available at `GET /actuator/health`.
The default settings use H2. To override them, export `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` in your shell.

### 2. Run the frontend

```bash
cd frontend
cp .env.example .env.local
npm install
npm run dev
```

Open `http://localhost:3000`. Use `ORD-1001` or `ORD-2001`, which are seeded for the demo.

### 3. Verify quality gates

```bash
cd backend && ./mvnw verify
cd ../frontend && npm run lint && npm run typecheck && npm run build && npm audit --omit=dev --audit-level=high
```

## API examples

```bash
curl -X POST http://localhost:8080/api/v1/orders/ORD-1001/alerts \
  -H 'Content-Type: application/json' \
  -d '{"severity":"HIGH","message":"Temperature exceeded threshold","source":"iot-monitor"}'

curl 'http://localhost:8080/api/v1/orders/ORD-1001/alerts?severity=HIGH'
```

## Error contract

The API returns one stable error envelope: `timestamp`, `status`, `code`, `message`, `path`, and optional `fieldErrors`. It maps malformed requests to `400`, unknown orders to `404`, and storage failures to sanitized `500` responses. The browser never receives stack traces or connection details.

## AWS and secrets

`infra/template.yaml` deploys the API through API Gateway and a Java Lambda adapter. It receives an exact `DatabaseSecretArn` parameter and grants only `secretsmanager:GetSecretValue` for that resource. In AWS, the secret JSON has this shape:

```json
{
  "url": "jdbc:postgresql://database.internal:5432/alerts",
  "username": "application_user",
  "password": "managed-outside-git"
}
```

To package and deploy the backend reference, install the AWS SAM CLI and run from the repository root:

```bash
sam build --template-file infra/template.yaml
sam deploy --guided --template-file .aws-sam/build/template.yaml
```

During guided deployment, provide the existing database secret ARN for `DatabaseSecretArn` and the deployed frontend URL for `AllowedOrigin`. The database must be reachable from Lambda (for example, through the organization's VPC and RDS Proxy); network resources remain owned by the target AWS environment rather than being invented by this challenge stack. See [ARCHITECTURE.md](ARCHITECTURE.md).

## Repository layout

```text
backend/        Spring Boot API, tests, Lambda adapter
frontend/       Next.js TypeScript user interface
infra/          AWS SAM template
SPEC.md         Contract and acceptance criteria
AI_RULES.md     Versioned rules for AI-assisted work
INCIDENT.md     Production incident diagnosis
```

## Usage and supervision of AI

- I used AI to accelerate initial scaffolding, documentation drafts, and candidate test cases after defining the specification and repository rules.
- I reviewed the API contract, validation constraints, error behavior, dependency boundaries, and every IAM statement manually.
- I corrected the overly broad permissions proposed in an initial draft, replacing them with `GetSecretValue` for one parameterized secret ARN.
- I also corrected an initial error-handling draft so storage failures are logged by category without returning raw exception text to the client.

## Senior-level implementation notes

- The application uses the same API contract locally and in Lambda; configuration changes by environment rather than code branch.
- It intentionally avoids scope creep such as authentication, order CRUD, or extra cloud services that the task did not request.
- The incident analysis distinguishes evidence from hypothesis and includes a safe mitigation plus preventative operational controls.
- The CI workflow tests, lints, type-checks, and builds both layers on every pull request and push to `main`.

