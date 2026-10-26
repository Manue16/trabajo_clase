// PRUEBA DE PICO (Spike): simula un aumento repentino y masivo de tráfico
// para ver cómo se recupera el sistema.
import http from 'k6/http';
import { check, sleep } from 'k6';

// Dirección del backend de MJBE Bikes (Express, puerto 5000)
const BASE_URL = 'http://localhost:5000/api';

// 1. Opciones: pasa de 5 a 500 usuarios en segundos y vuelve a 5
export let options = {
  stages: [
    { duration: '30s', target: 5 },   // Tráfico normal: 5 usuarios
    { duration: '10s', target: 500 }, // Pico: de 5 a 500 usuarios en 10 segundos
    { duration: '1m', target: 500 },  // Mantiene el pico durante 1 minuto
    { duration: '10s', target: 5 },   // Vuelve al tráfico normal
    { duration: '30s', target: 5 },   // Observa si el sistema se recupera
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
