import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { PrestamosComponent } from './prestamos.component';
import { Prestamo } from '../models';

describe('PrestamosComponent (Pantalla 2 · /prestamos)', () => {
  let fixture: ComponentFixture<PrestamosComponent>;
  let component: PrestamosComponent;
  let http: HttpTestingController;
  const API = 'http://localhost:8080/api';

  const prestamoActivo: Prestamo = {
    id: 5, fechaPrestamo: '2026-11-26', estado: 'ACTIVO',
    estudiante: { id: 1, dni: '70123456', nombre: 'Ana Quispe', estado: 'ACTIVO' },
    libro: { id: 10, isbn: '9780132350884', titulo: 'Clean Code', autor: 'R. Martin', ejemplaresDisponibles: 1 },
  };

  function responderCarga(prestamos: Prestamo[] = []) {
    http.expectOne(`${API}/estudiantes`).flush([prestamoActivo.estudiante]);
    http.expectOne(`${API}/libros`).flush([prestamoActivo.libro]);
    http.expectOne(`${API}/prestamos`).flush(prestamos);
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PrestamosComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
    fixture = TestBed.createComponent(PrestamosComponent);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
    responderCarga([prestamoActivo]);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  const texto = (sel: string) => fixture.nativeElement.querySelector(sel)?.textContent ?? '';

  it('carga estudiantes, libros y préstamos, con botón Devolver para los ACTIVOS', () => {
    expect(component.estudiantes.length).toBe(1);
    expect(fixture.nativeElement.querySelectorAll('[data-cy="devolver"]').length).toBe(1);
  });

  it('sin seleccionar estudiante y libro no envía nada', () => {
    component.prestar();
    fixture.detectChanges();
    expect(texto('[data-cy="error"]')).toContain('Seleccione');
  });

  it('RN-03 · préstamo exitoso muestra confirmación y recarga', () => {
    component.estudianteId = 1;
    component.libroId = 10;
    component.prestar();
    const req = http.expectOne(`${API}/prestamos`);
    expect(req.request.body).toEqual({ estudianteId: 1, libroId: 10 });
    req.flush(prestamoActivo);
    responderCarga([prestamoActivo]);
    fixture.detectChanges();
    expect(texto('[data-cy="mensaje"]')).toContain('Préstamo registrado');
  });

  it('RN-03 · muestra el mensaje del backend al rechazar (sin ejemplares)', () => {
    component.estudianteId = 1;
    component.libroId = 10;
    component.prestar();
    http.expectOne(`${API}/prestamos`).flush(
      { status: 400, mensaje: 'El libro no tiene ejemplares disponibles', errores: [] },
      { status: 400, statusText: 'Bad Request' },
    );
    fixture.detectChanges();
    expect(texto('[data-cy="error"]')).toContain('no tiene ejemplares');
  });

  it('RN-04 · devolver hace PUT y muestra confirmación', () => {
    component.devolver(prestamoActivo);
    const req = http.expectOne(`${API}/prestamos/5/devolucion`);
    expect(req.request.method).toBe('PUT');
    req.flush({ ...prestamoActivo, estado: 'DEVUELTO' });
    responderCarga();
    fixture.detectChanges();
    expect(texto('[data-cy="mensaje"]')).toContain('devuelto');
  });

  it('RN-04 · préstamo inexistente (404) muestra el error', () => {
    component.devolver({ ...prestamoActivo, id: 99 });
    http.expectOne(`${API}/prestamos/99/devolucion`).flush(
      { status: 404, mensaje: 'El préstamo no existe', errores: [] },
      { status: 404, statusText: 'Not Found' },
    );
    fixture.detectChanges();
    expect(texto('[data-cy="error"]')).toContain('no existe');
  });

  it('RN-02 · registrar estudiante muestra su estado ACTIVO', () => {
    component.nuevoEstudiante = { dni: '70999999', nombre: 'Luis' };
    component.registrarEstudiante();
    http.expectOne({ method: 'POST', url: `${API}/estudiantes` })
      .flush({ id: 2, dni: '70999999', nombre: 'Luis', estado: 'ACTIVO' });
    responderCarga();
    fixture.detectChanges();
    expect(texto('[data-cy="mensaje"]')).toContain('ACTIVO');
    expect(component.nuevoEstudiante.dni).toBe('');
  });
});
