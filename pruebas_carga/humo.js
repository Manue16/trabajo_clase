// PRUEBA DE HUMO (Smoke): carga mínima para verificar que el script
// y el sistema funcionan. Es la primera que se debe ejecutar.
import http from 'k6/http';
import { check, sleep } from 'k6';

// Dirección del backend de MJBE Bikes (Express, puerto 5000)
const BASE_URL = 'http://localhost:5000/api';

// 1. Opciones: 1 usuario virtual durante 30 segundos
export let options = {
  vus: 1,
  duration: '30s',
};

// 2. Función principal: el código que ejecuta cada usuario virtual
//    (solo consultas GET, para no llenar la base de datos de registros de prueba)
export default function () {
  // Catálogo de productos (lo usan la tienda, el inventario y las ventas)
  let productos = http.get(`${BASE_URL}/productos`);
  check(productos, {
    'productos: el estado es 200': (r) => r.status === 200,
  });

  // Historial de ventas
  let ventas = http.get(`${BASE_URL}/ventas`);
  check(ventas, {
    'ventas: el estado es 200': (r) => r.status === 200,
  });

  // Listado de clientes
  let clientes = http.get(`${BASE_URL}/clientes`);
  check(clientes, {
    'clientes: el estado es 200': (r) => r.status === 200,
  });

  // Panel del administrador (hace 8 consultas a la base de datos)
  let dashboard = http.get(`${BASE_URL}/reportes/dashboard`);
  check(dashboard, {
    'dashboard: el estado es 200': (r) => r.status === 200,
  });

  sleep(1); // Pausa de 1 segundo entre peticiones
}
