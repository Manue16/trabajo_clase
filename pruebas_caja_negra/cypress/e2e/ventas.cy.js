describe('Pruebas de Caja Negra - Ventas del vendedor (UI)', () => {

  // 🔑 Registrar el handler del confirm UNA VEZ, antes de todos los tests
  beforeEach(() => {
    cy.on('window:confirm', () => true);   // Aceptar cualquier confirm
    cy.on('window:alert', () => true);      // Aceptar cualquier alert
    cy.visitarComo('vendedor', '/CrearVenta');
  });

  // 👇 Selecciona el primer cliente y el primer producto disponibles
  const elegirClienteYProducto = () => {
    cy.get('#id_cliente option').should('have.length.greaterThan', 1);
    cy.get('#id_cliente option').eq(1).then(($op) => {
      cy.get('#id_cliente').select($op.val());
    });
    cy.get('select[name="id_producto"]').first().find('option').eq(1).then(($op) => {
      cy.get('select[name="id_producto"]').first().select($op.val());
    });
  };

  // ============================================
  // CASOS BÁSICOS
  // ============================================
  it('CP-UI-109: Debe cargar la página para crear una venta', () => {
    cy.url().should('include', '/CrearVenta');
  });

  it('CP-UI-110: Debe mostrar el título "Crear venta" y sus campos', () => {
    cy.contains('h3, h2', 'Crear venta').should('be.visible');
    cy.contains('label', 'Cliente').should('be.visible');
    cy.contains('label', 'Fecha').should('be.visible');
    cy.contains('label', 'Productos').should('be.visible');
  });

  it('CP-UI-111: Debe cargar los clientes y productos desde el backend', () => {
    cy.intercept('GET', '**/api/clientes').as('getClientes');
    cy.intercept('GET', '**/api/productos').as('getProductos');
    cy.visitarComo('vendedor', '/CrearVenta');

    cy.wait('@getClientes').its('response.statusCode').should('eq', 200);
    cy.wait('@getProductos').its('response.statusCode').should('eq', 200);
    cy.get('#id_cliente option').should('have.length.greaterThan', 1);
    cy.get('select[name="id_producto"] option').should('have.length.greaterThan', 1);
  });

  it('CP-UI-112: La fecha debe venir con el día de hoy', () => {
    const hoy = new Date().toISOString().slice(0, 10);
    cy.get('#fecha').should('have.value', hoy);
  });

  // ============================================
  // CASOS DE PRODUCTOS (agregar / quitar)
  // ============================================
  it('CP-UI-113: Debe agregar y quitar filas de productos', () => {
    cy.get('select[name="id_producto"]').should('have.length', 1);
    // 👇 Con una sola fila no se puede quitar
    cy.contains('button', 'Quitar').should('be.disabled');

    cy.contains('button', 'Agregar producto').click();
    cy.get('select[name="id_producto"]').should('have.length', 2);

    cy.contains('button', 'Quitar').click();
    cy.get('select[name="id_producto"]').should('have.length', 1);
  });

  it('CP-UI-114: Debe calcular el total según la cantidad', () => {
    elegirClienteYProducto();

    // 👇 Tomamos el precio del texto de la opción: "Nombre - $1.234"
    cy.get('select[name="id_producto"]').first().find('option:selected').invoke('text').then((texto) => {
      const precio = Number(texto.split('$').pop().replace(/\D/g, ''));

      cy.get('input[name="cantidad"]').first().clear().type('2');
      cy.contains('h4', 'Total:').invoke('text').then((total) => {
        expect(Number(total.replace(/\D/g, ''))).to.eq(precio * 2);
      });
    });
  });

  // ============================================
  // CASOS DE VALIDACIÓN (valores límite)
  // ============================================
  it('CP-UI-115: NO debe permitir guardar sin elegir un producto', () => {
    cy.contains('button', 'Guardar venta').should('be.disabled');
  });

  it('CP-UI-116: NO debe permitir guardar con cantidad 0', () => {
    elegirClienteYProducto();
    cy.get('input[name="cantidad"]').first().clear().type('0');
    cy.contains('button', 'Guardar venta').should('be.disabled');
  });

  it('CP-UI-117: La cantidad máxima debe ser el stock del producto', () => {
    elegirClienteYProducto();
    cy.get('select[name="id_producto"]').first().invoke('val').then((idProducto) => {
      cy.backend('GET', `/productos/${idProducto}`).then(({ body }) => {
        cy.get('input[name="cantidad"]').first()
          .should('have.attr', 'max', String(body.cant_producto));
      });
    });
  });

  it('CP-UI-118: NO debe registrar la venta sin seleccionar el cliente', () => {
    cy.intercept('POST', '**/api/ventas').as('crearVenta');

    cy.get('select[name="id_producto"]').first().find('option').eq(1).then(($op) => {
      cy.get('select[name="id_producto"]').first().select($op.val());
    });
    cy.contains('button', 'Guardar venta').click();

    cy.get('@crearVenta.all').should('have.length', 0);
    cy.url().should('include', '/CrearVenta');
  });

  // ============================================
  // CASOS DE CREACIÓN
  // ============================================
  it('CP-UI-119: Debe registrar una venta correctamente', () => {
    cy.intercept('POST', '**/api/ventas').as('crearVenta');

    elegirClienteYProducto();
    cy.get('input[name="cantidad"]').first().clear().type('1');
    cy.contains('button', 'Guardar venta').click();

    cy.wait('@crearVenta').its('response.statusCode').should('eq', 201);
    cy.url().should('not.include', '/CrearVenta');
  });

  it('CP-UI-120: La venta debe descontar el stock vendido', () => {
    cy.intercept('POST', '**/api/ventas').as('crearVenta');

    elegirClienteYProducto();
    cy.get('select[name="id_producto"]').first().invoke('val').then((idProducto) => {
      // 1. Stock antes de vender
      cy.backend('GET', `/productos/${idProducto}`).then(({ body: antes }) => {
        // 2. Vender 1 unidad
        cy.get('input[name="cantidad"]').first().clear().type('1');
        cy.contains('button', 'Guardar venta').click();
        cy.wait('@crearVenta');

        // 3. Stock después de vender
        cy.backend('GET', `/productos/${idProducto}`).its('body.cant_producto')
          .should('eq', antes.cant_producto - 1);
      });
    });
  });

  it('CP-UI-121: La venta en el local debe registrarse con pago en efectivo', () => {
    cy.intercept('POST', '**/api/ventas').as('crearVenta');

    elegirClienteYProducto();
    cy.contains('button', 'Guardar venta').click();

    cy.wait('@crearVenta').its('request.body.metodo_pago').should('eq', 'efectivo');
  });

  // ============================================
  // CASOS DE CONSULTA DE VENTAS
  // ============================================
  it('CP-UI-122: Debe mostrar el listado de ventas del vendedor', () => {
    cy.intercept('GET', '**/api/ventas*').as('getVentas');
    cy.visitarComo('vendedor', '/Ventas');

    cy.wait('@getVentas').its('response.statusCode').should('eq', 200);
    cy.contains('h4', 'Ventas').should('be.visible');
    ['ID de la venta', 'Cliente', 'Fecha', 'Total', 'Detalle'].forEach((columna) => {
      cy.contains('th', columna).should('be.visible');
    });
  });

  it('CP-UI-123: Una búsqueda sin coincidencias debe mostrar "No se encontraron ventas"', () => {
    cy.visitarComo('vendedor', '/Ventas');
    cy.get('#buscar-venta').type('noexiste' + Date.now());
    cy.contains('td', 'No se encontraron ventas').should('be.visible');
  });

  it('CP-UI-124: "Ver detalle" debe abrir el detalle de la venta', () => {
    cy.visitarComo('vendedor', '/Ventas');

    cy.get('table tbody tr').first().within(() => {
      cy.contains('a', 'Ver detalle').click();
    });

    cy.url().should('include', '/DetallesVenta/');
    cy.contains('Detalle de venta #').should('be.visible');
    cy.contains('strong', 'Cliente').should('be.visible');
    cy.contains('strong', 'Total').should('be.visible');
  });

  it('CP-UI-125: El botón "Crear venta" debe abrir el formulario', () => {
    cy.visitarComo('vendedor', '/Ventas');
    cy.contains('a', 'Crear venta').click();
    cy.url().should('include', '/CrearVenta');
  });

});
