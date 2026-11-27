# BiblioTech · Estrategia integral de pruebas del software

Producto Académico · Unidad 1 · Universidad Peruana Unión

Sistema de biblioteca (libros, estudiantes y préstamos) con pruebas en todos los niveles de la pirámide:
JUnit 5, camino básico, Mockito, JaCoCo, @WebMvcTest, @SpringBootTest, Angular, Cypress y Postman/Newman.

## Entorno

| Herramienta | Versión probada |
|---|---|
| Java (JDK) | 17 o 21 |
| Maven | 3.9+ |
| Spring Boot | 3.3.5 |
| Base de datos | H2 en memoria (no se instala nada). Opcional: MySQL con el perfil `mysql` |
| Node.js | 18.19+ / 20 / 22 |
| Angular | 18 |
| Cypress | 13 |
| Newman | 6 (`npm i -g newman` o `npx newman`) |
| Navegador | Google Chrome (para Karma y Cypress) |

## Estructura

```
bibliotech/
├── backend/                Spring Boot (controller, service, repository, model)
│   └── src/test/java/pe/edu/upeu/bibliotech/
│       ├── unitarias/      JUnit 5 + camino básico
│       ├── mockito/        PrestamoService, LibroService, EstudianteService con @Mock
│       └── integracion/    @WebMvcTest y @SpringBootTest
├── frontend/               Angular
│   └── src/app/
│       ├── libros/         Pantalla 1 (/libros) + *.spec.ts
│       └── prestamos/      Pantalla 2 (/prestamos) + *.spec.ts
├── e2e/                    Cypress (3 flujos)
├── postman/                coleccion.json y entorno.json
└── docs/                   informe, camino básico (grafos), casos CP01–CP07, capturas
```

## Cómo levantar la aplicación

```bash
# 1) Backend: http://localhost:8080 (carga 3 libros y 3 estudiantes de ejemplo)
cd backend
mvn spring-boot:run

# 2) Frontend: http://localhost:4200
cd frontend
npm install
npm start
```

Con MySQL: `mvn spring-boot:run -Dspring-boot.run.profiles=mysql` (ajusten el usuario y la clave en `application-mysql.properties`).

## Comandos de revisión

| Comando (desde) | Qué comprueba |
|---|---|
| `mvn test` (backend/) | Pruebas unitarias, de camino básico y con Mockito |
| `mvn verify` (backend/) | Lo anterior + @WebMvcTest/@SpringBootTest + reporte JaCoCo; falla si la cobertura baja del 80 % |
| `ng test --watch=false --code-coverage` (frontend/) | Pruebas Angular (TestBed y HttpTestingController) |
| `npx cypress run` (e2e/, con backend y frontend levantados) | Flujos E2E 1, 2 y 3 |
| `newman run postman/coleccion.json` (raíz, con el backend levantado) | 16 requests con tests de estado y de cuerpo |

El reporte de JaCoCo queda en `backend/target/site/jacoco/index.html`.
Para Cypress, la primera vez ejecuten `npm install` dentro de `e2e/`.

## API

| Método | Ruta | Respuesta | Regla |
|---|---|---|---|
| GET | `/api/libros` | 200, lista con `estado` calculado | RN-01 |
| GET | `/api/libros/{id}` | 200 / 404 | RN-01 |
| POST | `/api/libros` | 201 / 400 | RN-01 |
| GET / POST | `/api/estudiantes` | 200 / 201 / 400 | RN-02 |
| GET / POST | `/api/prestamos` (`{estudianteId, libroId}`) | 200 / 201 / 400 / 404 | RN-03 |
| PUT | `/api/prestamos/{id}/devolucion` | 200 / 400 / 404 | RN-04 |

Los errores responden `{ "status": 400, "mensaje": "...", "errores": ["..."] }`.

## Resumen de pruebas

| Nivel | Archivo(s) | Cantidad |
|---|---|---|
| Unitarias (AAA, CP01–CP06) | `LibroValidadorTest`, `EstudianteValidadorTest` | 17 métodos |
| Camino básico | `CaminoBasicoRegistrarPrestamoTest` (V(G)=5), `CaminoBasicoDevolverLibroTest` (V(G)=3) | 8 |
| Mockito | `PrestamoServiceTest`, `LibroServiceTest`, `EstudianteServiceTest` | 17 |
| Controlador | `LibroControllerTest` (200/201/400/404), `PrestamoControllerTest` | 12 |
| Integración | `FlujoPrestamoIntegracionTest` | 5 |
| Angular | `libro.service.spec`, `libros.component.spec`, `prestamo.service.spec`, `prestamos.component.spec` | 22 |
| E2E | `flujo1_registrar_libro`, `flujo2_registrar_estudiante`, `flujo3_prestar_libro` | 6 |
| API | `postman/coleccion.json` | 16 requests / 32 aserciones |
