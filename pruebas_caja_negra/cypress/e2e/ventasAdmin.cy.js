describe('Pruebas de Caja Negra - Ventas del administrador (UI)', () => {

  // 🔑 Registrar el handler del confirm UNA VEZ, antes de todos los tests
  beforeEach(() => {
    cy.on('window:confirm', () => true);   // Aceptar cualquier confirm
    cy.on('window:alert', () => true);      // Aceptar cualquier alert
    cy.visitarComo('admin', '/VentasAdmin');
  });

  // ============================================
  // CASOS BÁSICOS
  // ============================================
  it('CP-UI-126: Debe cargar la página de ventas del administrador', () => {
    cy.url().should('include', '/VentasAdmin');
  });

  it('CP-UI-127: Debe mostrar el título "Ventas" y las columnas de la tabla', () => {
    cy.contains('h4', 'Ventas').should('be.visible');
    cy.contains('Todas las ventas registradas y su vendedor').should('be.visible');
    ['ID', 'Vendedor', 'Cliente', 'Fecha', 'Total', 'Estado', 'Detalle'].forEach((columna) => {
      cy.contains('th', columna).should('be.visible');
    });
  });

  it('CP-UI-128: La lista debe cargar todas las ventas desde el backend', () => {
    cy.intercept('GET', '**/api/ventas').as('getVentas');
    cy.visitarComo('admin', '/VentasAdmin');

    cy.wait('@getVentas').then(({ response }) => {
      expect(response.statusCode).to.eq(200);
      // 👇 La tabla muestra una fila por cada venta del backend
      cy.get('table tbody tr').should('have.length', response.body.length || 1);
    });
  });

  it('CP-UI-129: El estado de cada venta debe ser "Pagada" o "Pendiente"', () => {
    cy.get('table tbody tr').each(($fila) => {
      // 👇 La columna 6 es "Estado"
      cy.wrap($fila).find('td').eq(5).invoke('text').then((estado) => {
        expect(estado.trim()).to.be.oneOf(['Pagada', 'Pendiente']);
      });
    });
  });

  // ============================================
  // CASOS DE BÚSQUEDA
  // ============================================
  it('CP-UI-130: Debe buscar una venta por su ID', () => {
    cy.get('table tbody tr').first().find('td').first().invoke('text').then((id) => {
      cy.get('#buscar-venta').type(id.trim());
      cy.get('table tbody tr').first().find('td').first().should('have.text', id.trim());
    });
  });

  it('CP-UI-131: Una búsqueda sin coincidencias debe mostrar "No se encontraron ventas"', () => {
    cy.get('#buscar-venta').type('noexiste' + Date.now());
    cy.contains('td', 'No se encontraron ventas').should('be.visible');
  });

  // ============================================
  // CASO DE DETALLE
  // ============================================
  it('CP-UI-132: "Ver detalle" debe abrir el detalle de la venta', () => {
    cy.get('table tbody tr').first().within(() => {
      cy.contains('a', 'Ver detalle').click();
    });

    cy.url().should('include', '/DetallesVenta/');
    cy.contains('Detalle de venta #').should('be.visible');
    cy.contains('strong', 'Vendedor').should('be.visible');
  });

});
