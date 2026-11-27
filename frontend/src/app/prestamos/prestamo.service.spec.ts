import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { PrestamoService } from './prestamo.service';
import { EstudianteService } from './estudiante.service';

describe('PrestamoService y EstudianteService (HttpTestingController)', () => {
  let prestamos: PrestamoService;
  let estudiantes: EstudianteService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    prestamos = TestBed.inject(PrestamoService);
    estudiantes = TestBed.inject(EstudianteService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('registrar() hace POST /api/prestamos con estudianteId y libroId', () => {
    prestamos.registrar(1, 10).subscribe();
    const req = http.expectOne('http://localhost:8080/api/prestamos');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ estudianteId: 1, libroId: 10 });
    req.flush({});
  });

  it('devolver() hace PUT /api/prestamos/{id}/devolucion', () => {
    prestamos.devolver(5).subscribe();
    const req = http.expectOne('http://localhost:8080/api/prestamos/5/devolucion');
    expect(req.request.method).toBe('PUT');
    req.flush({});
  });

  it('listar() hace GET /api/prestamos', () => {
    let n = -1;
    prestamos.listar().subscribe((p) => (n = p.length));
    http.expectOne({ method: 'GET', url: 'http://localhost:8080/api/prestamos' }).flush([]);
    expect(n).toBe(0);
  });

  it('EstudianteService.registrar() hace POST /api/estudiantes', () => {
    estudiantes.registrar({ dni: '70123456', nombre: 'Ana' }).subscribe();
    const req = http.expectOne('http://localhost:8080/api/estudiantes');
    expect(req.request.method).toBe('POST');
    req.flush({ id: 1, dni: '70123456', nombre: 'Ana', estado: 'ACTIVO' });
  });
});
