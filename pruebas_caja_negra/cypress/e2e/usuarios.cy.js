describe('Pruebas de Caja Negra - Administración de usuarios (UI)', () => {

  // 🔑 Registrar el handler del confirm UNA VEZ, antes de todos los tests
  beforeEach(() => {
    cy.on('window:confirm', () => true);   // Aceptar cualquier confirm
    cy.on('window:alert', () => true);      // Aceptar cualquier alert
    cy.visitarComo('admin', '/VerUsuarios');
  });

  // 👇 Crea un empleado desde la pantalla "Registrar Empleado"
  const crearEmpleado = (login, rol = '2') => {
    cy.visitarComo('admin', '/CrearEmpleado');
    cy.get('input[name="login"]').type(login);
    cy.get('input[name="email"]').type(`${login}@test.com`);
    cy.get('input[name="password_harsh"]').type('Clave123');
    cy.get('select[name="rol_id"]').select(rol);
    cy.contains('button', 'Guardar Empleado').click();
  };

  // ============================================
  // CASOS BÁSICOS
  // ============================================
  it('CP-UI-92: Debe cargar la página de usuarios', () => {
    cy.url().should('include', '/VerUsuarios');
  });

  it('CP-UI-93: Debe mostrar el título "Usuarios" y las columnas de la tabla', () => {
    cy.contains('h4, h3, h2', 'Usuarios').should('be.visible');
    ['ID', 'Usuario', 'Email', 'Rol', 'Estado', 'Acciones'].forEach((columna) => {
      cy.contains('th', columna).should('be.visible');
    });
  });

  it('CP-UI-94: La lista debe cargar los datos desde el backend', () => {
    cy.intercept('GET', '**/api/usuarios').as('getUsuarios');
    cy.visitarComo('admin', '/VerUsuarios');
    cy.wait('@getUsuarios').its('response.statusCode').should('eq', 200);
    cy.get('table tbody tr').should('have.length.greaterThan', 0);
    cy.contains('No hay usuarios disponibles').should('not.exist');
  });

  // ============================================
  // CASOS DE FILTRO
  // ============================================
  it('CP-UI-95: Debe filtrar los usuarios por nombre de usuario', () => {
    // 👇 Tomamos el usuario de la primera fila y filtramos por él
    cy.get('table tbody tr').first().find('td').eq(1).invoke('text').then((login) => {
      cy.contains('button', 'Filtrar').click();
      cy.get('.dropdown-menu select').select('login');
      cy.get('input[placeholder="Ingrese el valor"]').type(login.trim());
      cy.contains('button', 'Aplicar filtro').click();

      cy.get('table tbody tr').each(($fila) => {
        cy.wrap($fila).find('td').eq(1).should('contain', login.trim());
      });
    });
  });

  it('CP-UI-96: Un filtro sin coincidencias debe mostrar "No hay usuarios disponibles"', () => {
    cy.contains('button', 'Filtrar').click();
    cy.get('.dropdown-menu select').select('email');
    cy.get('input[placeholder="Ingrese el valor"]').type('noexiste' + Date.now());
    cy.contains('button', 'Aplicar filtro').click();

    cy.contains('td', 'No hay usuarios disponibles').should('be.visible');
  });

  // ============================================
  // CASOS DE CREACIÓN
  // ============================================
  it('CP-UI-97: El botón "Crear Empleado" debe abrir el formulario', () => {
    cy.contains('a', 'Crear Empleado').click();
    cy.url().should('include', '/CrearEmpleado');
    cy.contains('h3', 'Registrar Empleado').should('be.visible');
  });

  it('CP-UI-98: Debe crear un nuevo empleado correctamente', () => {
    const loginUnico = 'empleado' + Date.now();

    cy.intercept('POST', '**/api/usuarios').as('crearUsuario');

    crearEmpleado(loginUnico);

    cy.wait('@crearUsuario').its('response.statusCode').should('eq', 201);

    // 👇 El nuevo empleado debe aparecer en la lista
    cy.visitarComo('admin', '/VerUsuarios');
    cy.contains('td', loginUnico).should('be.visible');
  });

  it('CP-UI-99: NO debe crear un empleado con los campos vacíos', () => {
    cy.intercept('POST', '**/api/usuarios').as('crearUsuario');

    cy.visitarComo('admin', '/CrearEmpleado');
    cy.contains('button', 'Guardar Empleado').click();

    cy.get('@crearUsuario.all').should('have.length', 0);
    cy.url().should('include', '/CrearEmpleado');
  });

  it('CP-UI-100: NO debe crear un empleado sin seleccionar el rol', () => {
    const loginUnico = 'sinrol' + Date.now();

    cy.intercept('POST', '**/api/usuarios').as('crearUsuario');

    cy.visitarComo('admin', '/CrearEmpleado');
    cy.get('input[name="login"]').type(loginUnico);
    cy.get('input[name="email"]').type(`${loginUnico}@test.com`);
    cy.get('input[name="password_harsh"]').type('Clave123');
    cy.contains('button', 'Guardar Empleado').click();

    cy.get('@crearUsuario.all').should('have.length', 0);
  });

  it('CP-UI-101: NO debe crear un empleado con un usuario repetido', () => {
    const loginUnico = 'repetido' + Date.now();

    cy.intercept('POST', '**/api/usuarios').as('crearUsuario');

    crearEmpleado(loginUnico);
    cy.wait('@crearUsuario').its('response.statusCode').should('eq', 201);

    crearEmpleado(loginUnico);
    cy.wait('@crearUsuario').its('response.statusCode').should('eq', 400);
  });

  // ============================================
  // CASOS DE EDICIÓN
  // ============================================
  it('CP-UI-102: Debe abrir la edición de un usuario con sus datos cargados', () => {
    cy.get('table tbody tr').first().within(() => {
      cy.contains('a', 'Actualizar').click();
    });

    cy.url().should('include', '/ActualizarEmpleados/');
    cy.contains('h3', 'Actualizar Empleado').should('be.visible');
    cy.get('input[name="login"]').invoke('val').should('not.be.empty');
    cy.get('input[name="email"]').invoke('val').should('not.be.empty');
  });

  it('CP-UI-103: Debe actualizar el correo de un empleado', () => {
    const loginUnico = 'editar' + Date.now();

    cy.intercept('POST', '**/api/usuarios').as('crearUsuario');
    cy.intercept('PUT', '**/api/usuarios/**').as('actualizarUsuario');

    crearEmpleado(loginUnico);
    cy.wait('@crearUsuario');

    cy.visitarComo('admin', '/VerUsuarios');
    cy.contains('tr', loginUnico).within(() => {
      cy.contains('a', 'Actualizar').click();
    });

    cy.get('input[name="email"]').clear().type(`${loginUnico}.nuevo@test.com`);
    cy.contains('button', 'Actualizar Empleado').click();

    cy.wait('@actualizarUsuario').its('response.statusCode').should('eq', 200);
    cy.url().should('include', '/VerUsuarios');
    cy.contains('tr', loginUnico).should('contain', `${loginUnico}.nuevo@test.com`);
  });

  it('CP-UI-104: Si la contraseña se deja vacía, NO debe enviarse', () => {
    cy.intercept('PUT', '**/api/usuarios/**').as('actualizarUsuario');

    cy.get('table tbody tr').first().within(() => {
      cy.contains('a', 'Actualizar').click();
    });
    cy.get('input[name="password_harsh"]').should('have.value', '');
    cy.contains('button', 'Actualizar Empleado').click();

    cy.wait('@actualizarUsuario').its('request.body').should('not.have.property', 'password_harsh');
  });

  it('CP-UI-105: "Cancelar" en la edición debe volver a la lista sin guardar', () => {
    cy.intercept('PUT', '**/api/usuarios/**').as('actualizarUsuario');

    cy.get('table tbody tr').first().within(() => {
      cy.contains('a', 'Actualizar').click();
    });
    cy.contains('button', 'Cancelar').click();

    cy.url().should('include', '/VerUsuarios');
    cy.get('@actualizarUsuario.all').should('have.length', 0);
  });

  // ============================================
  // CASOS DE DESACTIVACIÓN Y ELIMINACIÓN
  // ============================================
  it('CP-UI-106: El botón "Acción" debe abrir la gestión del usuario', () => {
    cy.get('table tbody tr').first().within(() => {
      cy.contains('button', 'Acción').click();
    });

    cy.contains('.modal-title', 'Gestión de usuario').should('be.visible');
    cy.contains('.modal button', 'Desactivar').should('be.visible');
    cy.contains('.modal button', 'Eliminar').should('be.visible');

    // 👇 "Cancelar" cierra la ventana
    cy.contains('.modal button', 'Cancelar').click();
    cy.get('.modal').should('not.exist');
  });

  it('CP-UI-107: Debe desactivar un usuario existente', () => {
    const loginUnico = 'desactivar' + Date.now();

    cy.intercept('POST', '**/api/usuarios').as('crearUsuario');
    cy.intercept({ method: /PUT|PATCH/, url: '**/api/usuarios/**' }).as('desactivarUsuario');

    crearEmpleado(loginUnico);
    cy.wait('@crearUsuario');

    cy.visitarComo('admin', '/VerUsuarios');
    cy.contains('tr', loginUnico).within(() => {
      cy.contains('button', 'Acción').click();
    });
    cy.contains('.modal button', 'Desactivar').click();

    cy.wait('@desactivarUsuario').its('response.statusCode').should('eq', 200);
    cy.contains('tr', loginUnico).should('contain', 'inactivo');
  });

  it('CP-UI-108: Debe eliminar un usuario existente', () => {
    const loginUnico = 'eliminar' + Date.now();

    cy.intercept('POST', '**/api/usuarios').as('crearUsuario');
    cy.intercept('DELETE', '**/api/usuarios/**').as('borrarUsuario');

    crearEmpleado(loginUnico);
    cy.wait('@crearUsuario');

    cy.visitarComo('admin', '/VerUsuarios');
    cy.contains('tr', loginUnico).within(() => {
      cy.contains('button', 'Acción').click();
    });
    cy.contains('.modal button', 'Eliminar').click();

    cy.wait('@borrarUsuario').its('response.statusCode').should('be.oneOf', [200, 204]);
    cy.contains(loginUnico).should('not.exist');
  });

});
