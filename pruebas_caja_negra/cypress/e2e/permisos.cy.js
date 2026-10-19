describe('Pruebas de Caja Negra - Permisos por rol (UI)', () => {

  // 🔑 Registrar el handler del confirm UNA VEZ, antes de todos los tests
  beforeEach(() => {
    cy.on('window:confirm', () => true);   // Aceptar cualquier confirm
    cy.on('window:alert', () => true);      // Aceptar cualquier alert
  });

  // ============================================
  // TABLA DE DECISIÓN: ¿quién puede entrar a cada pantalla?
  // ============================================
  // 👇 rol null = sin sesión iniciada
  //    permitido = true  → la pantalla se debe mostrar
  //    permitido = false → la aplicación debe sacarlo de esa ruta
  const casos = [
    // Sin sesión
    { cp: 'CP-UI-73', rol: null, ruta: '/InicioAdmin', permitido: false },
    { cp: 'CP-UI-74', rol: null, ruta: '/VerUsuarios', permitido: false },
    { cp: 'CP-UI-75', rol: null, ruta: '/Ventas', permitido: false },
    { cp: 'CP-UI-76', rol: null, ruta: '/checkout', permitido: false },
    { cp: 'CP-UI-77', rol: null, ruta: '/mis-compras', permitido: false },

    // Administrador
    { cp: 'CP-UI-78', rol: 'admin', ruta: '/VerUsuarios', permitido: true },
    { cp: 'CP-UI-79', rol: 'admin', ruta: '/VentasAdmin', permitido: true },
    { cp: 'CP-UI-80', rol: 'admin', ruta: '/CrearVenta', permitido: false },

    // Vendedor
    { cp: 'CP-UI-81', rol: 'vendedor', ruta: '/Ventas', permitido: true },
    { cp: 'CP-UI-82', rol: 'vendedor', ruta: '/CrearVenta', permitido: true },
    { cp: 'CP-UI-83', rol: 'vendedor', ruta: '/VerUsuarios', permitido: false },
    { cp: 'CP-UI-84', rol: 'vendedor', ruta: '/VentasAdmin', permitido: false },

    // Bodeguero
    { cp: 'CP-UI-85', rol: 'bodeguero', ruta: '/InicioBodega', permitido: true },
    { cp: 'CP-UI-86', rol: 'bodeguero', ruta: '/VentasAdmin', permitido: false },
    { cp: 'CP-UI-87', rol: 'bodeguero', ruta: '/CrearEmpleado', permitido: false },

    // Cliente
    { cp: 'CP-UI-88', rol: 'cliente', ruta: '/mis-compras', permitido: true },
    { cp: 'CP-UI-89', rol: 'cliente', ruta: '/InicioAdmin', permitido: false },
    { cp: 'CP-UI-90', rol: 'cliente', ruta: '/Ventas', permitido: false },
  ];

  casos.forEach(({ cp, rol, ruta, permitido }) => {
    const quien = rol ? `El ${rol}` : 'Un visitante sin sesión';
    const accion = permitido ? 'debe poder entrar a' : 'NO debe poder entrar a';

    it(`${cp}: ${quien} ${accion} ${ruta}`, () => {
      if (rol) {
        cy.visitarComo(rol, ruta);
      } else {
        cy.visitarSinSesion(ruta);
      }

      if (permitido) {
        cy.location('pathname').should('eq', ruta);
      } else {
        // 👇 Debe ser redirigido a otra pantalla
        cy.location('pathname').should('not.eq', ruta);
      }
    });
  });

  // ============================================
  // CASOS DE SESIÓN SIN PERMISO → LOGIN
  // ============================================
  it('CP-UI-91: Sin sesión, una pantalla protegida debe llevar al login', () => {
    cy.visitarSinSesion('/VentasAdmin');
    cy.url().should('include', '/login');
  });

});
