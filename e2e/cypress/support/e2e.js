// Comandos reutilizables para preparar datos por la API (datos controlados y únicos por ejecución)

const sufijo = () => String(Date.now()).slice(-8);

Cypress.Commands.add('crearLibroApi', (ejemplares = 2) => {
  const isbn = '978' + sufijo() + '00';
  return cy
    .request('POST', `${Cypress.env('api')}/libros`, {
      isbn,
      titulo: `Libro E2E ${isbn}`,
      autor: 'Autor E2E',
      ejemplaresDisponibles: ejemplares,
    })
    .its('body');
});

Cypress.Commands.add('crearEstudianteApi', () => {
  const dni = sufijo();
  return cy
    .request('POST', `${Cypress.env('api')}/estudiantes`, { dni, nombre: `Estudiante ${dni}` })
    .its('body');
});
