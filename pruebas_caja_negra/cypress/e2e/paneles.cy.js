describe('Pruebas de Caja Negra - Paneles de inicio (UI)', () => {

  // 🔑 Registrar el handler del confirm UNA VEZ, antes de todos los tests
  beforeEach(() => {
    cy.on('window:confirm', () => true);   // Aceptar cualquier confirm
    cy.on('window:alert', () => true);      // Aceptar cualquier alert
  });

  // 👇 Lee el número que aparece debajo de un título <h6> de una tarjeta
  const valorDeTarjeta = (titulo) =>
    cy.contains('h6', titulo).siblings('h2').invoke('text').then((texto) => Number(texto.replace(/\D/g, '')));

  // ============================================
  // PANEL DEL ADMINISTRADOR
  // ============================================
  it('CP-UI-152: El panel del administrador debe cargar el resumen desde el backend', () => {
    cy.intercept('GET', '**/api/reportes/dashboard').as('getDashboard');
    cy.visitarComo('admin', '/InicioAdmin');

    cy.wait('@getDashboard').its('response.statusCode').should('eq', 200);
    cy.contains('h3', 'Resumen General').should('be.visible');
  });

  it('CP-UI-153: El panel del administrador debe mostrar sus indicadores', () => {
    cy.visitarComo('admin', '/InicioAdmin');

    ['Total Productos', 'Total Clientes', 'Total Ventas', 'Ventas Hoy', 'Ventas del Mes', 'Stock Bajo']
      .forEach((titulo) => {
        cy.contains('h6', titulo).should('be.visible');
      });
    cy.contains('h4', 'Reportes').should('be.visible');
  });

  it('CP-UI-154: "Total Productos" debe coincidir con los productos registrados', () => {
    cy.visitarComo('admin', '/InicioAdmin');

    cy.backend('GET', '/productos').then(({ body }) => {
      valorDeTarjeta('Total Productos').should('eq', body.length);
    });
  });

  it('CP-UI-155: "Stock Bajo" debe contar los productos con 5 unidades o menos', () => {
    cy.visitarComo('admin', '/InicioAdmin');

    cy.backend('GET', '/productos').then(({ body }) => {
      const stockBajo = body.filter((p) => Number(p.cant_producto) <= 5).length;
      valorDeTarjeta('Stock Bajo').should('eq', stockBajo);
    });
  });

  it('CP-UI-156: El menú del administrador debe llevar a sus módulos', () => {
    cy.visitarComo('admin', '/InicioAdmin');

    cy.get('nav').contains('a', 'Usuarios').click();
    cy.url().should('include', '/VerUsuarios');
  });

  // ============================================
  // PANEL DEL BODEGUERO (alertas de stock)
  // ============================================
  it('CP-UI-157: El panel de bodega debe mostrar sus indicadores', () => {
    cy.visitarComo('bodeguero', '/InicioBodega');

    cy.contains('h3', 'Resumen de Bodega').should('be.visible');
    ['Total Productos', 'Productos Activos', 'Stock Bajo', 'Sin Stock'].forEach((titulo) => {
      cy.contains('h6', titulo).should('be.visible');
    });
  });

  it('CP-UI-158: "Sin Stock" debe contar los productos agotados', () => {
    cy.visitarComo('bodeguero', '/InicioBodega');

    cy.backend('GET', '/productos').then(({ body }) => {
      const sinStock = body.filter((p) => Number(p.cant_producto) === 0).length;
      valorDeTarjeta('Sin Stock').should('eq', sinStock);
    });
  });

  it('CP-UI-159: "Stock Bajo" en bodega debe contar los productos entre 1 y 5 unidades', () => {
    cy.visitarComo('bodeguero', '/InicioBodega');

    cy.backend('GET', '/productos').then(({ body }) => {
      const stockBajo = body.filter((p) => Number(p.cant_producto) > 0 && Number(p.cant_producto) <= 5).length;
      valorDeTarjeta('Stock Bajo').should('eq', stockBajo);
    });
  });

  // ============================================
  // PANEL DEL VENDEDOR
  // ============================================
  it('CP-UI-160: El panel del vendedor debe mostrar el resumen de sus ventas', () => {
    cy.intercept('GET', '**/api/ventas*').as('getVentas');
    cy.visitarComo('vendedor', '/InicioVentas');

    cy.wait('@getVentas').its('response.statusCode').should('eq', 200);
    cy.contains('h2', 'Inicio de ventas').should('be.visible');
    cy.contains('Panel del vendedor').should('be.visible');
    ['Ventas realizadas', 'Ventas completadas', 'Ingresos generados', 'Ventas de hoy'].forEach((titulo) => {
      cy.contains('h6', titulo).should('be.visible');
    });
  });

  it('CP-UI-161: "Ventas realizadas" debe coincidir con las ventas del vendedor', () => {
    cy.fixture('usuarios').then((usuarios) => {
      cy.visitarComo('vendedor', '/InicioVentas');

      cy.backend('GET', `/ventas?id_vendedor=${usuarios.vendedor.id}`).then(({ body }) => {
        valorDeTarjeta('Ventas realizadas').should('eq', body.length);
      });
    });
  });

  it('CP-UI-162: "Ver historial" debe abrir el historial de ventas', () => {
    cy.visitarComo('vendedor', '/InicioVentas');

    cy.contains('a', 'Ver historial').click();
    cy.url().should('include', '/DetallesVenta');
  });

});
