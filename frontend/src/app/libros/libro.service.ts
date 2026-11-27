import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_URL } from '../api.config';
import { Libro } from '../models';

@Injectable({ providedIn: 'root' })
export class LibroService {
  private http = inject(HttpClient);
  readonly url = `${API_URL}/libros`;

  listar(): Observable<Libro[]> {
    return this.http.get<Libro[]>(this.url);
  }

  registrar(libro: Libro): Observable<Libro> {
    return this.http.post<Libro>(this.url, libro);
  }
}
