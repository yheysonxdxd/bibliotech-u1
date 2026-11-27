import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { LibroService } from './libro.service';
import { Libro } from '../models';

describe('LibroService (HttpTestingController)', () => {
  let service: LibroService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(LibroService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('GET /api/libros devuelve la lista de libros', () => {
    // Arrange
    const libros: Libro[] = [
      { id: 1, isbn: '9780132350884', titulo: 'Clean Code', autor: 'R. Martin', ejemplaresDisponibles: 3, estado: 'DISPONIBLE' },
    ];
    let recibido: Libro[] = [];

    // Act
    service.listar().subscribe((l) => (recibido = l));
    const req = http.expectOne('http://localhost:8080/api/libros');
    req.flush(libros);

    // Assert
    expect(req.request.method).toBe('GET');
    expect(recibido.length).toBe(1);
    expect(recibido[0].estado).toBe('DISPONIBLE');
  });

  it('POST /api/libros envía el libro en el cuerpo', () => {
    // Arrange
    const nuevo: Libro = { isbn: '9780134685991', titulo: 'Effective Java', autor: 'J. Bloch', ejemplaresDisponibles: 2 };
    let creado: Libro | undefined;

    // Act
    service.registrar(nuevo).subscribe((l) => (creado = l));
    const req = http.expectOne('http://localhost:8080/api/libros');
    req.flush({ ...nuevo, id: 9, estado: 'DISPONIBLE' }, { status: 201, statusText: 'Created' });

    // Assert
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(nuevo);
    expect(creado?.id).toBe(9);
  });

  it('POST con datos inválidos propaga el error 400 del backend', () => {
    let status = 0;
    service.registrar({ isbn: '', titulo: 'X', autor: 'Y', ejemplaresDisponibles: 1 }).subscribe({
      error: (e) => (status = e.status),
    });
    http.expectOne('http://localhost:8080/api/libros')
      .flush({ status: 400, mensaje: 'El ISBN es obligatorio', errores: [] }, { status: 400, statusText: 'Bad Request' });
    expect(status).toBe(400);
  });
});
