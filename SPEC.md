# Alert Operations API Specification

## Scope
The application records and queries operational alerts associated with an existing order. Local demo orders are seeded as `ORD-1001` and `ORD-2001`; order maintenance is outside this challenge.

## Functional requirements
- Register one alert for an existing order with `severity`, `message`, and `source`.
- List alerts for an existing order and optionally filter them by severity.
- Provide explicit loading, empty, validation, and network-error states in the web client.
- Keep local persistence replaceable and keep credentials out of source control.

## API contract
| Method and route | Request | Success | Errors |
| --- | --- | --- | --- |
| `POST /api/v1/orders/{orderId}/alerts` | `{ "severity": "LOW|MEDIUM|HIGH", "message": "1-500 chars", "source": "1-100 chars" }` | `201` with the created alert | `400` invalid input; `404` unknown order; `500` controlled internal error |
| `GET /api/v1/orders/{orderId}/alerts?severity=HIGH` | Optional valid severity | `200` with an array ordered by newest first | `400` invalid filter; `404` unknown order; `500` controlled internal error |

## Data model
`orders(id, created_at)` and `alerts(id UUID, order_id FK, severity, message, source, created_at)`. Every alert belongs to one existing order.

## Acceptance criteria
1. A valid POST for `ORD-1001` returns `201`; malformed fields never persist data.
2. GET returns all alerts or only the requested severity, newest first.
3. Errors use a stable JSON envelope and never expose stack traces, database URLs, or secret values.
4. Local execution needs only Java 21 and Node 20+; AWS configuration uses environment variables or an exact Secrets Manager ARN.

