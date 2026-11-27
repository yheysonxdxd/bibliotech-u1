import { Component, OnInit, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { LibroService } from './libro.service';
import { Libro } from '../models';
import { mensajeDeError } from '../api.config';

/** Pantalla 1 · Catálogo de libros (/libros) · RN-01 */
@Component({
  selector: 'app-libros',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './libros.component.html',
})
export class LibrosComponent implements OnInit {
  private libroService = inject(LibroService);
  private fb = inject(FormBuilder);

  libros: Libro[] = [];
  mensaje = '';
  error = '';

  form = this.fb.nonNullable.group({
    isbn: ['', [Validators.required, Validators.pattern(/\S+/)]],
    titulo: ['', [Validators.required, Validators.pattern(/\S+/)]],
    autor: ['', [Validators.required, Validators.pattern(/\S+/)]],
    ejemplaresDisponibles: [1, [Validators.required, Validators.min(0)]],
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.libroService.listar().subscribe({
      next: (libros) => (this.libros = libros),
      error: (err: HttpErrorResponse) => (this.error = mensajeDeError(err)),
    });
  }

  invalido(campo: 'isbn' | 'titulo' | 'autor' | 'ejemplaresDisponibles'): boolean {
    const control = this.form.controls[campo];
    return control.invalid && (control.touched || control.dirty);
  }

  registrar(): void {
    this.mensaje = '';
    this.error = '';
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.libroService.registrar(this.form.getRawValue()).subscribe({
      next: (libro) => {
        this.mensaje = `Libro "${libro.titulo}" registrado`;
        this.form.reset();
        this.cargar();
      },
      error: (err: HttpErrorResponse) => (this.error = mensajeDeError(err)),
    });
  }
}
