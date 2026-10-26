// PRUEBA DE ESTRÉS (Stress): busca el límite del sistema sometiéndolo
// a una carga muy superior a la normal.
import http from 'k6/http';
import { check, sleep } from 'k6';

// Dirección del backend de MJBE Bikes (Express, puerto 5000)
const BASE_URL = 'http://localhost:5000/api';

// 1. Opciones: sube por escalones hasta 200 usuarios y los mantiene
export let options = {
  stages: [
    { duration: '1m', target: 50 },   // Sube de 1 a 50 usuarios en 1 minuto
    { duration: '1m', target: 100 },  // Sube de 50 a 100 usuarios
    { duration: '1m', target: 200 },  // Sube de 100 a 200 usuarios
    { duration: '2m', target: 200 },  // Mantiene 200 usuarios durante 2 minutos
    { duration: '1m', target: 0 },    // Baja de 200 a 0 usuarios
  ],
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
