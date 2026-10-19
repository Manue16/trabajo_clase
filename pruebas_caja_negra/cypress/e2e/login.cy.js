describe('Pruebas de Caja Negra - Inicio de sesión (UI)', () => {

  // 🔑 Registrar el handler del confirm UNA VEZ, antes de todos los tests
  beforeEach(() => {
    cy.on('window:confirm', () => true);   // Aceptar cualquier confirm
    cy.on('window:alert', () => true);      // Aceptar cualquier alert
    cy.visitarSinSesion('/login');
  });

  // ============================================
  // CASOS BÁSICOS
  // ============================================
  it('CP-UI-51: Debe cargar la página de inicio de sesión', () => {
    cy.url().should('include', '/login');
  });

  it('CP-UI-52: Debe mostrar el título "Inicio de sesión"', () => {
    cy.contains('h2, h1', 'Inicio de sesión').should('be.visible');
  });

  // ============================================
  // CASOS DE VALIDACIÓN (campos vacíos)
  // ============================================
  it('CP-UI-53: NO debe enviar el formulario con los campos vacíos', () => {
    cy.intercept('POST', '**/api/login').as('login');

    cy.contains('button', 'Iniciar sesión').click();

    cy.contains('.alert-danger', 'Por favor ingresa el correo y la contraseña.').should('be.visible');
    // 👇 No se debe llamar al backend
    cy.get('@login.all').should('have.length', 0);
  });

  it('CP-UI-54: NO debe enviar el formulario si falta la contraseña', () => {
    cy.intercept('POST', '**/api/login').as('login');

    cy.get('#loginEmail').type('admin@mjbebikes.com');
    cy.contains('button', 'Iniciar sesión').click();

    cy.contains('.alert-danger', 'Por favor ingresa el correo y la contraseña.').should('be.visible');
    cy.get('@login.all').should('have.length', 0);
  });

  // ============================================
  // CASOS DE CREDENCIALES
  // ============================================
  it('CP-UI-55: NO debe iniciar sesión con credenciales incorrectas', () => {
    cy.intercept('POST', '**/api/login').as('login');

    cy.get('#loginEmail').type('noexiste@mjbebikes.com');
    cy.get('#loginPassword').type('claveIncorrecta');
    cy.contains('button', 'Iniciar sesión').click();

    cy.wait('@login').its('response.statusCode').should('be.oneOf', [400, 401, 404]);
    cy.get('.alert-danger').should('be.visible');
    cy.url().should('include', '/login');
  });

  it('CP-UI-56: La contraseña NO debe viajar en texto plano', () => {
    cy.intercept('POST', '**/api/login').as('login');

    cy.get('#loginEmail').type('admin@mjbebikes.com');
    cy.get('#loginPassword').type('admin123');
    cy.contains('button', 'Iniciar sesión').click();

    // 👇 Se envía el hash SHA-256 (64 caracteres hexadecimales), no la clave
    cy.wait('@login').its('request.body').then((body) => {
      expect(body.password_harsh).to.not.eq('admin123');
      expect(body.password_harsh).to.match(/^[0-9a-f]{64}$/);
    });
  });

  it('CP-UI-57: Debe enviar el correo en minúsculas y sin espacios', () => {
    cy.intercept('POST', '**/api/login').as('login');

    cy.get('#loginEmail').type('  ADMIN@MJBEBIKES.COM  ');
    cy.get('#loginPassword').type('admin123');
    cy.contains('button', 'Iniciar sesión').click();

    cy.wait('@login').its('request.body.email').should('eq', 'admin@mjbebikes.com');
  });

  // ============================================
  // CASOS DE REDIRECCIÓN POR ROL
  // ============================================
  // 👇 Cada rol debe llegar a su propia pantalla de inicio
  const destinos = [
    { cp: 'CP-UI-58', rol: 'admin', ruta: '/InicioAdmin' },
    { cp: 'CP-UI-59', rol: 'vendedor', ruta: '/InicioVentas' },
    { cp: 'CP-UI-60', rol: 'bodeguero', ruta: '/InicioBodega' },
  ];

  destinos.forEach(({ cp, rol, ruta }) => {
    it(`${cp}: El ${rol} debe iniciar sesión y llegar a ${ruta}`, () => {
      cy.fixture('usuarios').then((usuarios) => {
        cy.intercept('POST', '**/api/login').as('login');

        cy.get('#loginEmail').type(usuarios[rol].email);
        cy.get('#loginPassword').type(usuarios[rol].password);
        cy.contains('button', 'Iniciar sesión').click();

        cy.wait('@login').its('response.statusCode').should('eq', 200);
        cy.url().should('include', ruta);
      });
    });
  });

  it('CP-UI-61: El cliente debe iniciar sesión y llegar a la tienda', () => {
    cy.fixture('usuarios').then((usuarios) => {
      cy.intercept('POST', '**/api/login').as('login');

      cy.get('#loginEmail').type(usuarios.cliente.email);
      cy.get('#loginPassword').type(usuarios.cliente.password);
      cy.contains('button', 'Iniciar sesión').click();

      cy.wait('@login').its('response.statusCode').should('eq', 200);
      cy.location('pathname').should('eq', '/');
    });
  });

  // ============================================
  // CASOS DE NAVEGACIÓN Y CIERRE DE SESIÓN
  // ============================================
  it('CP-UI-62: El enlace "Regístrate aquí" debe abrir el registro', () => {
    cy.contains('a', 'Regístrate aquí').click();
    cy.url().should('include', '/register');
  });

  it('CP-UI-63: "Cerrar sesión" debe borrar la sesión y volver al login', () => {
    cy.visitarComo('admin', '/InicioAdmin');

    cy.contains('button', 'Cerrar sesión').click();

    cy.url().should('include', '/login');
    // 👇 La sesión ya no debe existir
    cy.window().its('localStorage').invoke('getItem', 'mjbe_auth').should('be.null');
  });

});
