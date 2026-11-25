# AI Rules and Engineering Guardrails

## Required workflow
1. Read `SPEC.md` before proposing or changing code. Keep API compatibility unless the specification changes first.
2. Propose the smallest implementation that satisfies the requirement; do not introduce microservices, authentication, queues, or cloud resources that are outside scope.
3. Prefer clear names, immutable DTOs, constructor injection, and focused methods. Keep controllers thin and business rules in services.
4. Validate all external input. Return typed, stable error responses; never leak exception messages, stack traces, JDBC URLs, API keys, or secret values.
5. Read sensitive configuration only from environment variables or AWS Secrets Manager. Never add `.env`, credentials, tokens, or real ARNs to Git.
6. Before completing a change, add or update unit tests for happy path, invalid input, and relevant dependency failure. Run backend tests, frontend lint, and frontend build.
7. Review generated IaC for least privilege: no wildcard action or resource when a concrete secret ARN can be supplied.
8. Treat generated code as a draft. A human must review contracts, IAM permissions, error handling, concurrency implications, and dependency versions before committing.

