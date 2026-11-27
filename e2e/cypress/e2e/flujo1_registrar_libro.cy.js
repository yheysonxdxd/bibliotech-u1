// Flujo 1: registrar un libro y verlo en la lista (Pantalla 1 · /libros · RN-01)
describe('Flujo 1 · Registrar un libro', () => {
  it('registra un libro desde el formulario y aparece en el catálogo', () => {
    const isbn = '978' + String(Date.now()).slice(-10);
    const titulo = `Pruebas de Software ${isbn}`;

    cy.visit('/libros');
    cy.get('[data-cy="isbn"]').type(isbn);
    cy.get('[data-cy="titulo"]').type(titulo);
    cy.get('[data-cy="autor"]').type('Myers');
    cy.get('[data-cy="ejemplares"]').clear().type('4');
    cy.get('[data-cy="guardar-libro"]').click();

    cy.get('[data-cy="mensaje"]').should('contain', 'registrado');
    cy.contains('[data-cy="fila-libro"]', isbn)
      .should('contain', titulo)
      .and('contain', 'DISPONIBLE')
      .find('[data-cy="ejemplares-libro"]')
      .should('have.text', '4');
  });

  it('muestra los errores de validación de RN-01 y no registra', () => {
    cy.visit('/libros');
    cy.get('[data-cy="ejemplares"]').clear().type('-1');
    cy.get('[data-cy="guardar-libro"]').click();

    cy.get('[data-cy="error-isbn"]').should('be.visible');
    cy.get('[data-cy="error-titulo"]').should('be.visible');
    cy.get('[data-cy="error-ejemplares"]').should('be.visible');
    cy.get('[data-cy="mensaje"]').should('not.exist');
  });
});
