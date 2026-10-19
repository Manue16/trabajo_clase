// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add('login', (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add('drag', { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add('dismiss', { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite('visit', (originalFn, url, options) => { ... })

// ============================================
// COMANDOS DEL PROYECTO MJBE BIKES
// ============================================

// 🔑 Dirección del backend (Express). Se puede cambiar con:
//    npx cypress run --env apiUrl=http://localhost:5000/api
const API_URL = Cypress.env('apiUrl') || 'http://localhost:5000/api';

// ============================================
// cy.visitarComo(rol, ruta)
// ============================================
// Abre una pantalla con la sesión de un rol ya iniciada.
// 👇 Guarda en localStorage las mismas claves que guarda Login.jsx
//    (auth.js → saveSession), ANTES de que cargue la aplicación.
// Se usa como PRECONDICIÓN en los módulos que no son el login,
// para que un fallo del login no bloquee las demás pruebas.
//
// rol: 'admin' | 'vendedor' | 'bodeguero' | 'cliente'
//      (los datos salen de cypress/fixtures/usuarios.json)
Cypress.Commands.add('visitarComo', (rol, ruta) => {
  cy.fixture('usuarios').then((usuarios) => {
    const usuario = usuarios[rol];

    cy.visit(ruta, {
      onBeforeLoad(win) {
        win.localStorage.setItem('mjbe_auth', 'true');
        win.localStorage.setItem('mjbe_user_id', String(usuario.id));
        win.localStorage.setItem('mjbe_user_login', usuario.login);
        win.localStorage.setItem('mjbe_user_email', usuario.email);
        win.localStorage.setItem('mjbe_rol_id', String(usuario.rolId));
        win.localStorage.setItem('mjbe_role', String(usuario.rolId));
      },
    });
  });
});

// ============================================
// cy.visitarSinSesion(ruta)
// ============================================
// Abre una pantalla asegurando que NO haya sesión guardada.
Cypress.Commands.add('visitarSinSesion', (ruta) => {
  cy.visit(ruta, {
    onBeforeLoad(win) {
      win.localStorage.clear();
    },
  });
});

// ============================================
// cy.backend(metodo, ruta, body)
// ============================================
// Llama directamente al backend para PREPARAR o VERIFICAR datos
// (por ejemplo, revisar el stock de un producto antes y después
// de una venta). No reemplaza la prueba de la pantalla.
Cypress.Commands.add('backend', (metodo, ruta, body) => {
  return cy.request({
    method: metodo,
    url: `${API_URL}${ruta}`,
    body,
    failOnStatusCode: false,
  });
});
