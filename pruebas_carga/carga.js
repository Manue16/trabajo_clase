// PRUEBA DE CARGA (Load): simula la carga típica de uso del sistema.
import http from 'k6/http';
import { check, sleep } from 'k6';

// Dirección del backend de MJBE Bikes (Express, puerto 5000)
const BASE_URL = 'http://localhost:5000/api';

// 1. Opciones: define el patrón de carga con "etapas" (stages)
export let options = {
  stages: [
    { duration: '30s', target: 10 }, // Sube de 1 a 10 usuarios virtuales en 30s
    { duration: '1m', target: 10 },  // Mantiene 10 usuarios durante 1 minuto
    { duration: '30s', target: 0 },  // Baja de 10 a 0 usuarios en 30s
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
