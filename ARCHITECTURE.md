# Architecture decisions

## Local and cloud execution
The same Spring Boot application runs locally and behind API Gateway on Lambda. Locally it reads H2 connection settings from environment variables. In AWS, `DB_SECRET_ARN` activates the Secrets Manager provider, which reads a JSON secret containing `url`, `username`, and `password`.

## Scope boundary
Orders are deliberately seeded rather than implemented as a separate CRUD module. This makes `404` behavior real while preserving the requested scope: operational alerts associated with an existing order.

## Security boundary
The SAM role can execute only `secretsmanager:GetSecretValue` against the exact parameterized secret ARN. No secret values, database URLs, stack traces, or raw exception messages are returned to the browser.

## Production evolution
For a production RDS deployment, the Lambda would use VPC connectivity and RDS Proxy. This is documented as an operational extension rather than simulated with incomplete infrastructure in a short technical challenge.

