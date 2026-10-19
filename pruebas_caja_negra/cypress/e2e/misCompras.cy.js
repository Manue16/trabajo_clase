describe('Pruebas de Caja Negra - Mis compras (UI)', () => {

  // 🔑 Registrar el handler del confirm UNA VEZ, antes de todos los tests
  beforeEach(() => {
    cy.on('window:confirm', () => true);   // Aceptar cualquier confirm
    cy.on('window:alert', () => true);      // Aceptar cualquier alert
    cy.visitarComo('cliente', '/mis-compras');
  });

  // ============================================
  // CASOS BÁSICOS
  // ============================================
  it('CP-UI-145: Debe cargar la página "Mis compras"', () => {
    cy.url().should('include', '/mis-compras');
  });

  it('CP-UI-146: Debe mostrar el título "Mis compras"', () => {
    cy.contains('h1', 'Mis compras').should('be.visible');
  });

  it('CP-UI-147: La lista debe cargar las compras desde el backend', () => {
    cy.intercept('GET', '**/api/ventas*').as('getCompras');
    cy.visitarComo('cliente', '/mis-compras');

    cy.wait('@getCompras').its('response.statusCode').should('eq', 200);
    cy.contains('No se pudieron cargar tus compras.').should('not.exist');
  });

  it('CP-UI-148: Debe mostrar las compras o el aviso de que no hay compras', () => {
    cy.get('body').then(($body) => {
      if ($body.text().includes('Aún no tienes compras registradas.')) {
        cy.contains('.alert-info', 'Aún no tienes compras registradas.').should('be.visible');
      } else {
        cy.contains('Compra #').should('be.visible');
      }
    });
  });

  // ============================================
  // CASOS DE CONTENIDO
  // ============================================
  it('CP-UI-149: Una compra nueva debe aparecer en "Mis compras"', () => {
    cy.intercept('POST', '**/api/ventas').as('crearVenta');

    // 1. Comprar un producto desde la tienda
    cy.visitarComo('cliente', '/productos');
    cy.contains('button', 'Agregar al carrito').first().click();
    cy.contains('button', '🛒').click();
    cy.contains('a', 'Finalizar compra').click();
    cy.get('#nombre').type('Cliente Historial');
    cy.get('#correo').type('cliente@test.com');
    cy.get('#telefono').type('3001234567');
    cy.get('#direccion').type('Calle 10 # 20-30');
    cy.contains('button', 'Confirmar compra simulada').click();

    cy.wait('@crearVenta').then(({ response }) => {
      const idVenta = response.body.id;

      // 2. Revisar el historial desde el menú
      cy.contains('a', 'Mis compras').click();
      cy.contains(`Compra #${idVenta}`).should('be.visible');
    });
  });

  // 👇 Se ejecuta después de CP-UI-149, así el cliente tiene al menos una compra
  it('CP-UI-150: Cada compra debe mostrar su estado "Pagada" o "Pendiente"', () => {
    // 👇 Solo las etiquetas de las compras (no el contador del carrito)
    cy.get('main .badge').each(($estado) => {
      expect($estado.text().trim()).to.be.oneOf(['Pagada', 'Pendiente']);
    });
  });

  // ============================================
  // CASO SIN SESIÓN
  // ============================================
  it('CP-UI-151: Sin sesión, "Mis compras" debe llevar al login', () => {
    cy.visitarSinSesion('/mis-compras');
    cy.url().should('include', '/login');
  });

});
