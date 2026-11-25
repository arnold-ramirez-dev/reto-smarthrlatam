# Incident diagnosis

Probable root cause: the execution role lacks effective `secretsmanager:GetSecretValue` access to `prod/rds/orders-db`.
The application cannot obtain or refresh database credentials, while the connection pool is already saturated (`active=20/20`).
That combination causes waits above 3 seconds and the API Gateway integration reaches 29 seconds, producing the 504.
The logs establish correlation, not sole causation; verify CloudTrail, the Lambda/task role, secret ARN, KMS key policy, and RDS connection metrics.
Immediate mitigation: add read permission for the exact secret ARN and `kms:Decrypt` if a customer-managed key is used.
Validate secret retrieval, recycle unhealthy execution environments, and watch pool usage and API Gateway 5xx while traffic recovers.
Prevent recurrence with a startup secret-access check, bounded pool and query timeouts, and alarms for AccessDenied, pool saturation, integration latency, and 5xx.
For Lambda with RDS, introduce RDS Proxy before scaling concurrent connections.

