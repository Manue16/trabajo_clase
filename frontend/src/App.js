import react, { useEffect, useState } from "react";

const App = () => {
  const [mensaje, setMensaje] = useState("");

  useEffect(() => {
    fetch('http://localhost:5000/api/mensaje')
      .then(res => res.json())
      .then(data => setMensaje(data.mensaje))
      .catch(err => console.error('Error de conexion:', err));
  }, []);
   
    return (
      <div>
        <h1>Mensaje del backend:</h1>
        <p>{mensaje}</p>
      </div>
    );
  
};
export default App;