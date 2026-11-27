import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { LibrosComponent } from './libros.component';
import { Libro } from '../models';

describe('LibrosComponent (Pantalla 1 · /libros)', () => {
  let fixture: ComponentFixture<LibrosComponent>;
  let component: LibrosComponent;
  let http: HttpTestingController;
  const URL = 'http://localhost:8080/api/libros';

  const libros: Libro[] = [
    { id: 1, isbn: '9780132350884', titulo: 'Clean Code', autor: 'R. Martin', ejemplaresDisponibles: 3, estado: 'DISPONIBLE' },
    { id: 2, isbn: '9780201633610', titulo: 'Design Patterns', autor: 'Gamma', ejemplaresDisponibles: 0, estado: 'NO_DISPONIBLE' },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LibrosComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(LibrosComponent);
    component = fixture.componentInstance;
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges(); // ngOnInit → GET
    http.expectOne(URL).flush(libros);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  const el = (sel: string): HTMLElement | null => fixture.nativeElement.querySelector(sel);

  function llenar(isbn: string, titulo: string, autor: string, ejemplares: number) {
    component.form.setValue({ isbn, titulo, autor, ejemplaresDisponibles: ejemplares });
  }

  it('muestra la lista de libros del backend con su estado', () => {
    const filas = fixture.nativeElement.querySelectorAll('[data-cy="fila-libro"]');
    expect(filas.length).toBe(2);
    expect(filas[1].textContent).toContain('NO_DISPONIBLE');
  });

  it('RN-01 · ISBN obligatorio: no envía la petición y muestra el error', () => {
    // Arrange
    llenar('', 'Clean Code', 'R. Martin', 2);
    // Act
    component.registrar();
    fixture.detectChanges();
    // Assert
    expect(component.form.controls.isbn.hasError('required')).toBeTrue();
    expect(el('[data-cy="error-isbn"]')?.textContent).toContain('El ISBN es obligatorio');
    http.expectNone(URL);
  });

  it('RN-01 · ejemplares negativos son inválidos', () => {
    llenar('9780132350884', 'Clean Code', 'R. Martin', -1);
    component.registrar();
    fixture.detectChanges();
    expect(component.form.controls.ejemplaresDisponibles.hasError('min')).toBeTrue();
    expect(el('[data-cy="error-ejemplares"]')).not.toBeNull();
    http.expectNone(URL);
  });

  it('RN-01 · borde: 0 ejemplares es válido', () => {
    llenar('9780132350884', 'Clean Code', 'R. Martin', 0);
    expect(component.form.valid).toBeTrue();
  });

  it('título y autor con solo espacios son inválidos', () => {
    llenar('9780132350884', '   ', '   ', 1);
    component.registrar();
    fixture.detectChanges();
    expect(el('[data-cy="error-titulo"]')).not.toBeNull();
    expect(el('[data-cy="error-autor"]')).not.toBeNull();
    http.expectNone(URL);
  });

  it('registro válido: hace POST, muestra mensaje y recarga la lista', () => {
    // Arrange
    llenar('9780134685991', 'Effective Java', 'J. Bloch', 2);
    // Act
    component.registrar();
    const post = http.expectOne((r) => r.method === 'POST' && r.url === URL);
    post.flush({ id: 3, isbn: '9780134685991', titulo: 'Effective Java', autor: 'J. Bloch', ejemplaresDisponibles: 2, estado: 'DISPONIBLE' });
    http.expectOne((r) => r.method === 'GET' && r.url === URL).flush([...libros]);
    fixture.detectChanges();
    // Assert
    expect(el('[data-cy="mensaje"]')?.textContent).toContain('Effective Java');
    expect(component.form.controls.isbn.value).toBe('');
  });

  it('muestra el mensaje del backend cuando el ISBN está duplicado (400)', () => {
    llenar('9780132350884', 'Clean Code', 'R. Martin', 1);
    component.registrar();
    http.expectOne((r) => r.method === 'POST').flush(
      { status: 400, mensaje: 'Ya existe un libro con el ISBN 9780132350884', errores: [] },
      { status: 400, statusText: 'Bad Request' },
    );
    fixture.detectChanges();
    expect(el('[data-cy="error"]')?.textContent).toContain('Ya existe un libro');
  });

  it('muestra un aviso si el backend no responde', () => {
    component.cargar();
    http.expectOne(URL).error(new ProgressEvent('error'), { status: 0 });
    fixture.detectChanges();
    expect(el('[data-cy="error"]')?.textContent).toContain('No se pudo conectar');
  });
});
