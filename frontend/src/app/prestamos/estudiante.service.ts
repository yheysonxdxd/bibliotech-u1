import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api.config';
import { Estudiante } from '../models';

@Injectable({ providedIn: 'root' })
export class EstudianteService {
  private http = inject(HttpClient);
  readonly url = `${API_URL}/estudiantes`;

  listar(): Observable<Estudiante[]> {
    return this.http.get<Estudiante[]>(this.url);
  }

  registrar(estudiante: Estudiante): Observable<Estudiante> {
    return this.http.post<Estudiante>(this.url, estudiante);
  }
}
