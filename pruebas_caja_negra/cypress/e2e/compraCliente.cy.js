describe('Pruebas de Caja Negra - Finalizar compra del cliente (UI)', () => {

  // 🔑 Registrar el handler del confirm UNA VEZ, antes de todos los tests
  beforeEach(() => {
    cy.on('window:confirm', () => true);   // Aceptar cualquier confirm
    cy.on('window:alert', () => true);      // Aceptar cualquier alert
    cy.visitarComo('cliente', '/productos');
  });

  // 👇 El carrito vive en la memoria de React: se llena desde el catálogo
  //    y se llega al checkout con los enlaces (sin recargar la página)
  const agregarProductoEIrAlCheckout = () => {
    cy.contains('button', 'Agregar al carrito').first().click();
    cy.contains('button', '🛒').click();
    cy.contains('a', 'Finalizar compra').click();
    cy.url().should('include', '/checkout');
  };

  // 👇 Llena los datos de entrega con valores válidos
  const llenarDatosEntrega = (nombre) => {
    cy.get('#nombre').type(nombre);
    cy.get('#correo').type('cliente@test.com');
    cy.get('#telefono').type('3001234567');
    cy.get('#direccion').type('Calle 10 # 20-30');
  };

  // ============================================
  // CASOS BÁSICOS
  // ============================================
  it('CP-UI-133: Con el carrito vacío debe avisar y ofrecer ver productos', () => {
    cy.visitarComo('cliente', '/checkout');

    cy.contains('h1', 'Finalizar compra').should('be.visible');
    cy.contains('Tu carrito está vacío.').should('be.visible');
    cy.contains('a', 'Ver productos').click();
    cy.url().should('include', '/productos');
  });

  it('CP-UI-134: Agregar un producto debe aumentar el contador del carrito', () => {
    cy.contains('button', '🛒').find('.badge').should('have.text', '0');
    cy.contains('button', 'Agregar al carrito').first().click();
    cy.contains('button', '🛒').find('.badge').should('have.text', '1');
  });

  it('CP-UI-135: Debe mostrar el formulario de entrega y el resumen del pedido', () => {
    agregarProductoEIrAlCheckout();

    cy.contains('h2', 'Datos de entrega').should('be.visible');
    cy.contains('h2', 'Resumen del pedido').should('be.visible');
    cy.contains('x 1').should('be.visible');
  });

  // ============================================
  // CASOS DE FORMA DE PAGO
  // ============================================
  it('CP-UI-136: Debe ofrecer las formas de pago y "Tarjeta" debe venir marcada', () => {
    agregarProductoEIrAlCheckout();

    cy.contains('legend', 'Método de pago simulado').should('be.visible');
    cy.get('#tarjeta').should('be.checked');
    cy.get('#transferencia').should('not.be.checked');
    cy.get('#contraentrega').should('not.be.checked');
  });

  it('CP-UI-137: Solo debe quedar marcada una forma de pago', () => {
    agregarProductoEIrAlCheckout();

    cy.get('#transferencia').check();
    cy.get('#transferencia').should('be.checked');
    cy.get('#tarjeta').should('not.be.checked');
  });

  // ============================================
  // CASOS DE VALIDACIÓN
  // ============================================
  it('CP-UI-138: NO debe confirmar la compra con los datos de entrega vacíos', () => {
    cy.intercept('POST', '**/api/ventas').as('crearVenta');

    agregarProductoEIrAlCheckout();
    cy.contains('button', 'Confirmar compra simulada').click();

    cy.get('@crearVenta.all').should('have.length', 0);
    cy.contains('Compra simulada realizada').should('not.exist');
  });

  it('CP-UI-139: NO debe confirmar la compra con un correo inválido', () => {
    cy.intercept('POST', '**/api/ventas').as('crearVenta');

    agregarProductoEIrAlCheckout();
    llenarDatosEntrega('Cliente Prueba');
    cy.get('#correo').clear().type('correo-invalido');
    cy.contains('button', 'Confirmar compra simulada').click();

    cy.get('@crearVenta.all').should('have.length', 0);
  });

  // ============================================
  // CASOS DE COMPRA
  // ============================================
  it('CP-UI-140: Debe registrar la compra y mostrar la confirmación', () => {
    const nombre = 'Cliente ' + Date.now();

    cy.intercept('POST', '**/api/ventas').as('crearVenta');

    agregarProductoEIrAlCheckout();
    llenarDatosEntrega(nombre);
    cy.get('#transferencia').check();
    cy.contains('button', 'Confirmar compra simulada').click();

    cy.wait('@crearVenta').its('response.statusCode').should('eq', 201);
    cy.contains('h2', 'Compra simulada realizada').should('be.visible');
    cy.contains(`Gracias, ${nombre}.`).should('be.visible');

    // 👇 Después de comprar el carrito queda vacío
    cy.contains('button', '🛒').find('.badge').should('have.text', '0');
  });

  it('CP-UI-141: La compra debe enviar la forma de pago elegida', () => {
    cy.intercept('POST', '**/api/ventas').as('crearVenta');

    agregarProductoEIrAlCheckout();
    llenarDatosEntrega('Cliente Pago');
    cy.get('#contraentrega').check();
    cy.contains('button', 'Confirmar compra simulada').click();

    cy.wait('@crearVenta').its('request.body.metodo_pago').should('eq', 'contraentrega');
  });

  it('CP-UI-142: "Volver al inicio" debe llevar a la tienda', () => {
    cy.intercept('POST', '**/api/ventas').as('crearVenta');

    agregarProductoEIrAlCheckout();
    llenarDatosEntrega('Cliente Volver');
    cy.contains('button', 'Confirmar compra simulada').click();
    cy.wait('@crearVenta');

    cy.contains('button', 'Volver al inicio').click();
    cy.location('pathname').should('eq', '/');
  });

  it('CP-UI-143: Si el backend falla debe mostrar un mensaje de error', () => {
    // 👇 Simulamos una falla del servidor solo en esta prueba
    cy.intercept('POST', '**/api/ventas', { statusCode: 500, body: {} }).as('crearVenta');

    agregarProductoEIrAlCheckout();
    llenarDatosEntrega('Cliente Error');
    cy.contains('button', 'Confirmar compra simulada').click();

    cy.wait('@crearVenta');
    cy.contains('.alert-danger', 'No se pudo registrar la compra. Intenta nuevamente.').should('be.visible');
    // 👇 El carrito NO se debe vaciar
    cy.contains('button', '🛒').find('.badge').should('have.text', '1');
  });

  // ============================================
  // CASO SIN SESIÓN
  // ============================================
  it('CP-UI-144: Sin sesión, el checkout debe llevar al login', () => {
    cy.visitarSinSesion('/checkout');
    cy.url().should('include', '/login');
  });

});
