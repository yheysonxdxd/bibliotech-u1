// Flujo 2: registrar un estudiante (Pantalla 2 · /prestamos · RN-02)
describe('Flujo 2 · Registrar un estudiante', () => {
  it('registra un estudiante con estado ACTIVO y aparece en la lista para prestar', () => {
    const dni = String(Date.now()).slice(-8);

    cy.visit('/prestamos');
    cy.get('[data-cy="dni"]').type(dni);
    cy.get('[data-cy="nombre"]').type('María Apaza');
    cy.get('[data-cy="guardar-estudiante"]').click();

    cy.get('[data-cy="mensaje"]').should('contain', 'María Apaza').and('contain', 'ACTIVO');
    cy.get('[data-cy="select-estudiante"]').should('contain', dni);
  });

  it('rechaza un DNI duplicado con el mensaje del backend', () => {
    cy.crearEstudianteApi().then((est) => {
      cy.visit('/prestamos');
      cy.get('[data-cy="dni"]').type(est.dni);
      cy.get('[data-cy="nombre"]').type('Duplicado');
      cy.get('[data-cy="guardar-estudiante"]').click();
      cy.get('[data-cy="error"]').should('contain', 'Ya existe un estudiante');
    });
  });
});
