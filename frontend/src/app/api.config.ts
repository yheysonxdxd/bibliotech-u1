export const API_URL = 'http://localhost:8080/api';

import { HttpErrorResponse } from '@angular/common/http';

/** Extrae el mensaje de negocio de un error HTTP del backend. */
export function mensajeDeError(err: HttpErrorResponse): string {
  if (err.status === 0) {
    return 'No se pudo conectar con el servidor';
  }
  return err.error?.mensaje ?? `Error ${err.status}`;
}
