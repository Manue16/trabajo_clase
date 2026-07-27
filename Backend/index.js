import "dotenv/config";
import express from "express";
import cors from "cors";
import pool from "./db.js";

import clienteRoutes from './routes/clientes.js';

const app = express();
const PORT = process.env.PORT || 5000;

app.use (cors());
app.use(express.json()); 

// app.get('/', (req, res) => {
//     res.send('Bienvenido al backend de la aplicacion!');
// });

// app.get('/api/mensaje', (req, res) => {
//     res.json({ mensaje: "Hola, este es un mensaje del backend!" });
// });

app.use('api/clientes', clienteRoutes);

app.listen(PORT, () => {
    console.log(`Server is running on port http://localhost:${PORT}`);
});






























































