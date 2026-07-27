import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Navbar from "./components/Navbar";
import Clientes from "./pages/clientes";

function App() {
  return(
    <Router>
      <Navbar/>
      <div className="container mt-4">
        <Routes>
          <Route path="/clientes" element={<Clientes/>}/>
          <Route path="/" element={
            <div className="text-center mt-5">
              <h1>Bienvenido al Sistema de Almacén</h1>
              <p className="lead">Seleccione una opción del menú para comenzar</p>
            </div>
          }/>
        </Routes>
      </div>
    </Router>
  );
}