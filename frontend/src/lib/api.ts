export type AlertSeverity = "LOW" | "MEDIUM" | "HIGH";

export interface OperationalAlert {
  id: string;
  orderId: string;
  severity: AlertSeverity;
  message: string;
  source: string;
  createdAt: string;
}

export interface CreateAlertPayload {
  severity: AlertSeverity;
  message: string;
  source: string;
}

interface ApiErrorBody {
  message?: string;
  fieldErrors?: Record<string, string>;
}

export class ApiError extends Error {
  constructor(
    message: string,
    public readonly status: number,
    public readonly fieldErrors: Record<string, string> = {},
  ) {
    super(message);
    this.name = "ApiError";
  }
}

const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response;
  try {
    response = await fetch(`${apiBaseUrl}${path}`, {
      ...init,
      headers: {
        "Content-Type": "application/json",
        ...init?.headers,
      },
      cache: "no-store",
    });
  } catch {
    throw new ApiError("No fue posible conectar con el servicio de alertas.", 0);
  }

  if (!response.ok) {
    const body = (await response.json().catch(() => ({}))) as ApiErrorBody;
    throw new ApiError(body.message ?? "La solicitud no pudo completarse.", response.status, body.fieldErrors ?? {});
  }

  return response.json() as Promise<T>;
}

export function listAlerts(orderId: string, severity?: AlertSeverity): Promise<OperationalAlert[]> {
  const query = severity ? `?severity=${encodeURIComponent(severity)}` : "";
  return request<OperationalAlert[]>(`/api/v1/orders/${encodeURIComponent(orderId)}/alerts${query}`);
}

export function createAlert(orderId: string, payload: CreateAlertPayload): Promise<OperationalAlert> {
  return request<OperationalAlert>(`/api/v1/orders/${encodeURIComponent(orderId)}/alerts`, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

