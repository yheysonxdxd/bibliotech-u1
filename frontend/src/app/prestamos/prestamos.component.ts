import { Component, OnInit, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { PrestamoService } from './prestamo.service';
import { EstudianteService } from './estudiante.service';
import { LibroService } from '../libros/libro.service';
import { Estudiante, Libro, Prestamo } from '../models';
import { mensajeDeError } from '../api.config';

/** Pantalla 2 · Registrar préstamo (/prestamos) · RN-03 y RN-04 (y alta rápida de estudiante, RN-02) */
@Component({
  selector: 'app-prestamos',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './prestamos.component.html',
})
export class PrestamosComponent implements OnInit {
  private prestamoService = inject(PrestamoService);
  private estudianteService = inject(EstudianteService);
  private libroService = inject(LibroService);

  estudiantes: Estudiante[] = [];
  libros: Libro[] = [];
  prestamos: Prestamo[] = [];

  estudianteId: number | null = null;
  libroId: number | null = null;
  nuevoEstudiante: Estudiante = { dni: '', nombre: '' };

  mensaje = '';
  error = '';

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.estudianteService.listar().subscribe((e) => (this.estudiantes = e));
    this.libroService.listar().subscribe((l) => (this.libros = l));
    this.prestamoService.listar().subscribe((p) => (this.prestamos = p));
  }

  private limpiarMensajes(): void {
    this.mensaje = '';
    this.error = '';
  }

  private fallo = (err: HttpErrorResponse) => {
    this.error = mensajeDeError(err);
  };

  prestar(): void {
    this.limpiarMensajes();
    if (this.estudianteId == null || this.libroId == null) {
      this.error = 'Seleccione un estudiante y un libro';
      return;
    }
    this.prestamoService.registrar(this.estudianteId, this.libroId).subscribe({
      next: (p) => {
        this.mensaje = `Préstamo registrado: "${p.libro.titulo}" para ${p.estudiante.nombre}`;
        this.cargar();
      },
      error: this.fallo,
    });
  }

  devolver(prestamo: Prestamo): void {
    this.limpiarMensajes();
    this.prestamoService.devolver(prestamo.id).subscribe({
      next: (p) => {
        this.mensaje = `Libro "${p.libro.titulo}" devuelto`;
        this.cargar();
      },
      error: this.fallo,
    });
  }

  registrarEstudiante(): void {
    this.limpiarMensajes();
    this.estudianteService.registrar(this.nuevoEstudiante).subscribe({
      next: (e) => {
        this.mensaje = `Estudiante ${e.nombre} registrado (${e.estado})`;
        this.nuevoEstudiante = { dni: '', nombre: '' };
        this.cargar();
      },
      error: this.fallo,
    });
  }
}
