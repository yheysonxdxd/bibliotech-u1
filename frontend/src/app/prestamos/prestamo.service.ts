import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api.config';
import { Prestamo } from '../models';

@Injectable({ providedIn: 'root' })
export class PrestamoService {
  private http = inject(HttpClient);
  readonly url = `${API_URL}/prestamos`;

  listar(): Observable<Prestamo[]> {
    return this.http.get<Prestamo[]>(this.url);
  }

  registrar(estudianteId: number, libroId: number): Observable<Prestamo> {
    return this.http.post<Prestamo>(this.url, { estudianteId, libroId });
  }

  devolver(prestamoId: number): Observable<Prestamo> {
    return this.http.put<Prestamo>(`${this.url}/${prestamoId}/devolucion`, {});
  }
}
