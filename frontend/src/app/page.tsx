"use client";

import { FormEvent, useCallback, useState } from "react";
import {
  AlertSeverity,
  ApiError,
  CreateAlertPayload,
  OperationalAlert,
  createAlert,
  listAlerts,
} from "@/lib/api";

const severities: { value: AlertSeverity; label: string }[] = [
  { value: "LOW", label: "Baja" },
  { value: "MEDIUM", label: "Media" },
  { value: "HIGH", label: "Alta" },
];

const initialForm: CreateAlertPayload = {
  severity: "MEDIUM",
  message: "",
  source: "",
};

function severityLabel(severity: AlertSeverity): string {
  return severities.find((item) => item.value === severity)?.label ?? severity;
}

function formatDate(value: string): string {
  return new Intl.DateTimeFormat("es-PE", {
    dateStyle: "medium",
    timeStyle: "short",
    timeZone: "America/Lima",
  }).format(new Date(value));
}

export default function AlertsPage() {
  const [orderIdInput, setOrderIdInput] = useState("ORD-1001");
  const [activeOrderId, setActiveOrderId] = useState("ORD-1001");
  const [severityFilter, setSeverityFilter] = useState<AlertSeverity | "">("");
  const [alerts, setAlerts] = useState<OperationalAlert[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [hasQueried, setHasQueried] = useState(false);
  const [isCreating, setIsCreating] = useState(false);
  const [loadError, setLoadError] = useState("");
  const [formError, setFormError] = useState("");
  const [form, setForm] = useState<CreateAlertPayload>(initialForm);

  const loadAlerts = useCallback(async (orderId: string, severity: AlertSeverity | "") => {
    setIsLoading(true);
    setLoadError("");
    try {
      const result = await listAlerts(orderId, severity || undefined);
      setAlerts(result);
    } catch (error) {
      setAlerts([]);
      setLoadError(error instanceof ApiError ? error.message : "No se pudieron cargar las alertas.");
    } finally {
      setIsLoading(false);
    }
  }, []);

  function searchOrder(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const normalizedOrderId = orderIdInput.trim();
    if (!normalizedOrderId) {
      setLoadError("Ingresa un identificador de orden válido.");
      return;
    }
    setActiveOrderId(normalizedOrderId);
    setHasQueried(true);
    void loadAlerts(normalizedOrderId, severityFilter);
  }

  function changeSeverityFilter(value: AlertSeverity | "") {
    setSeverityFilter(value);
    if (hasQueried) {
      void loadAlerts(activeOrderId, value);
    }
  }

  async function submitAlert(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setFormError("");
    if (!form.message.trim() || !form.source.trim()) {
      setFormError("Mensaje y fuente son obligatorios.");
      return;
    }

    setIsCreating(true);
    try {
      await createAlert(activeOrderId, {
        ...form,
        message: form.message.trim(),
        source: form.source.trim(),
      });
      setForm(initialForm);
      setHasQueried(true);
      await loadAlerts(activeOrderId, severityFilter);
    } catch (error) {
      setFormError(error instanceof ApiError ? error.message : "No se pudo registrar la alerta.");
    } finally {
      setIsCreating(false);
    }
  }

  return (
    <main className="shell">
      <section className="hero" aria-labelledby="page-title">
        <p className="eyebrow">OPERACIONES · MONITOREO</p>
        <h1 id="page-title">Alertas operativas</h1>
        <p>Consulta el estado de una orden y registra eventos que requieren seguimiento.</p>
      </section>

      <section className="panel search-panel" aria-label="Consultar alertas por orden">
        <form onSubmit={searchOrder} className="search-form">
          <label htmlFor="orderId">Orden o servicio</label>
          <div className="search-row">
            <input
              id="orderId"
              value={orderIdInput}
              onChange={(event) => setOrderIdInput(event.target.value)}
              maxLength={64}
              placeholder="Ej. ORD-1001"
              autoComplete="off"
            />
            <button type="submit">Consultar</button>
          </div>
          <p className="hint">Ordenes de demostración: <strong>ORD-1001</strong> y <strong>ORD-2001</strong>.</p>
        </form>

        <label className="filter-field" htmlFor="severity-filter">
          <span>Filtrar por severidad</span>
          <select
            id="severity-filter"
            value={severityFilter}
            onChange={(event) => changeSeverityFilter(event.target.value as AlertSeverity | "")}
          >
            <option value="">Todas las severidades</option>
            {severities.map((severity) => (
              <option key={severity.value} value={severity.value}>{severity.label}</option>
            ))}
          </select>
        </label>
      </section>

      <section className="content-grid">
        <section className="panel" aria-labelledby="list-title">
          <div className="section-heading">
            <div>
              <p className="eyebrow">ORDEN ACTIVA</p>
              <h2 id="list-title">{activeOrderId}</h2>
            </div>
            {!isLoading && !loadError && <span className="counter">{alerts.length} alerta{alerts.length === 1 ? "" : "s"}</span>}
          </div>

          {isLoading && <p className="status loading" role="status">Cargando alertas…</p>}
          {!isLoading && loadError && <p className="status error" role="alert">{loadError}</p>}
          {!isLoading && !loadError && !hasQueried && (
            <p className="empty-state">Consulta una orden para visualizar sus alertas operativas.</p>
          )}
          {!isLoading && !loadError && hasQueried && alerts.length === 0 && (
            <p className="empty-state">No hay alertas para esta orden con el filtro seleccionado.</p>
          )}
          {!isLoading && !loadError && alerts.length > 0 && (
            <ul className="alert-list">
              {alerts.map((alert) => (
                <li key={alert.id} className="alert-row">
                  <span className={`severity severity-${alert.severity.toLowerCase()}`}>{severityLabel(alert.severity)}</span>
                  <div className="alert-content">
                    <strong>{alert.message}</strong>
                    <span>{alert.source} · {formatDate(alert.createdAt)}</span>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </section>

        <section className="panel" aria-labelledby="form-title">
          <p className="eyebrow">NUEVO EVENTO</p>
          <h2 id="form-title">Registrar alerta</h2>
          <form onSubmit={submitAlert} className="alert-form">
            <label htmlFor="severity">Severidad</label>
            <select
              id="severity"
              value={form.severity}
              onChange={(event) => setForm((current) => ({ ...current, severity: event.target.value as AlertSeverity }))}
            >
              {severities.map((severity) => (
                <option key={severity.value} value={severity.value}>{severity.label}</option>
              ))}
            </select>

            <label htmlFor="source">Fuente</label>
            <input
              id="source"
              value={form.source}
              onChange={(event) => setForm((current) => ({ ...current, source: event.target.value }))}
              maxLength={100}
              placeholder="Ej. iot-monitor"
            />

            <label htmlFor="message">Mensaje</label>
            <textarea
              id="message"
              value={form.message}
              onChange={(event) => setForm((current) => ({ ...current, message: event.target.value }))}
              maxLength={500}
              rows={5}
              placeholder="Describe el evento que requiere seguimiento"
            />
            <p className="character-count">{form.message.length}/500</p>
            {formError && <p className="form-error" role="alert">{formError}</p>}
            <button type="submit" disabled={isCreating}>
              {isCreating ? "Registrando…" : "Registrar alerta"}
            </button>
          </form>
        </section>
      </section>
    </main>
  );
}

