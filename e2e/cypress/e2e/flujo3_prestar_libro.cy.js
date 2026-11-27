// Flujo 3: prestar un libro y ver bajar sus ejemplares (Pantalla 2 → Pantalla 1 · RN-03 y RN-04)
describe('Flujo 3 · Prestar un libro', () => {
  it('al prestar baja un ejemplar y al devolver se restituye', () => {
    cy.crearLibroApi(2).then((libro) => {
      cy.crearEstudianteApi().then((est) => {
        // Prestar
        cy.visit('/prestamos');
        cy.get('[data-cy="select-estudiante"]').select(`${est.nombre} (${est.dni}) · ACTIVO`);
        cy.get('[data-cy="select-libro"]').select(`${libro.titulo} · 2 ejemplares`);
        cy.get('[data-cy="prestar"]').click();
        cy.get('[data-cy="mensaje"]').should('contain', 'Préstamo registrado');

        // Ver bajar el ejemplar en el catálogo: 2 → 1
        cy.get('[data-cy="nav-libros"]').click();
        cy.contains('[data-cy="fila-libro"]', libro.isbn)
          .find('[data-cy="ejemplares-libro"]')
          .should('have.text', '1');

        // Devolver (RN-04) y comprobar que vuelve a 2
        cy.get('[data-cy="nav-prestamos"]').click();
        cy.contains('[data-cy="fila-prestamo"]', libro.titulo).find('[data-cy="devolver"]').click();
        cy.get('[data-cy="mensaje"]').should('contain', 'devuelto');
        cy.get('[data-cy="nav-libros"]').click();
        cy.contains('[data-cy="fila-libro"]', libro.isbn)
          .find('[data-cy="ejemplares-libro"]')
          .should('have.text', '2');
      });
    });
  });

  it('un libro sin ejemplares muestra el rechazo de RN-03', () => {
    cy.crearLibroApi(0).then((libro) => {
      cy.crearEstudianteApi().then((est) => {
        cy.visit('/prestamos');
        cy.get('[data-cy="select-estudiante"]').select(`${est.nombre} (${est.dni}) · ACTIVO`);
        cy.get('[data-cy="select-libro"]').select(`${libro.titulo} · 0 ejemplares`);
        cy.get('[data-cy="prestar"]').click();
        cy.get('[data-cy="error"]').should('contain', 'no tiene ejemplares disponibles');
      });
    });
  });
});
