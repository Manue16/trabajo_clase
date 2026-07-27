import React, { useState, useEffect, useCallback } from 'react';
import { Modal, Button, Form, Table, Pagination, Alert, InputGroup } from 'react-bootstrap';

const API_URL = 'http://localhost:5000/api/clientes';

const Clientes = () => {
  // Estados para datos
  const [clientes, setClientes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  // Estados para paginación
  const [pagination, setPagination] = useState({
    currentPage: 1,
    totalPages: 1,
    totalItems: 0,
    limit: 10
  });

  // Estados para búsqueda
  const [searchTerm, setSearchTerm] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');

  // Estados para formulario
  const [showModal, setShowModal] = useState(false);
  const [editingClient, setEditingClient] = useState(null);
  const [formData, setFormData] = useState({
    documentoCliente: '',
    tipoDocumentoCliente: '',
    nombreCliente: '',
    apellidoCliente: '',
    emailCliente: '',
    telefonoCliente: '',
    direccionCliente: ''
  });

  // Estados para validación
  const [validated, setValidated] = useState(false);

  // Debounce para búsqueda
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearch(searchTerm);
    }, 500);
    return () => clearTimeout(timer);
  }, [searchTerm]);

  // Cargar clientes
  const loadClientes = useCallback(async (page = 1) => {
    setLoading(true);
    setError(null);
    try {
      const response = await fetch(
        `${API_URL}?page=${page}&limit=10&search=${debouncedSearch}`
      );
      if (!response.ok) throw new Error('Error al cargar clientes');
      const data = await response.json();
      setClientes(data.clientes);
      setPagination(data.pagination);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [debouncedSearch]);

  // Cargar al montar y cuando cambie la búsqueda
  useEffect(() => {
    loadClientes(1);
  }, [loadClientes]);

  // Manejar cambio de página
  const handlePageChange = (page) => {
    loadClientes(page);
  };

  // Manejar búsqueda
  const handleSearch = (e) => {
    setSearchTerm(e.target.value);
  };

  // Abrir modal para nuevo cliente
  const handleNewClient = () => {
    setEditingClient(null);
    setFormData({
      documentoCliente: '',
      tipoDocumentoCliente: '',
      nombreCliente: '',
      apellidoCliente: '',
      emailCliente: '',
      telefonoCliente: '',
      direccionCliente: ''
    });
    setValidated(false);
    setShowModal(true);
  };

  // Abrir modal para editar cliente
  const handleEditClient = (cliente) => {
    setEditingClient(cliente.documentoCliente);
    setFormData({
      documentoCliente: cliente.documentoCliente,
      tipoDocumentoCliente: cliente.tipoDocumentoCliente || '',
      nombreCliente: cliente.nombreCliente,
      apellidoCliente: cliente.apellidoCliente,
      emailCliente: cliente.emailCliente || '',
      telefonoCliente: cliente.telefonoCliente || '',
      direccionCliente: cliente.direccionCliente || ''
    });
    setValidated(false);
    setShowModal(true);
  };

  // Manejar cambios en el formulario
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // Guardar cliente (crear o actualizar)
  const handleSaveClient = async (e) => {
    e.preventDefault();
    const form = e.currentTarget;
    
    if (form.checkValidity() === false) {
      e.stopPropagation();
      setValidated(true);
      return;
    }

    setValidated(true);
    
    try {
      const url = editingClient 
        ? `${API_URL}/${editingClient}` 
        : API_URL;
      
      const method = editingClient ? 'PUT' : 'POST';
      
      const response = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.error || 'Error al guardar cliente');
      }

      setSuccess(editingClient ? 'Cliente actualizado exitosamente' : 'Cliente creado exitosamente');
      setShowModal(false);
      loadClientes(pagination.currentPage);
      
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError(err.message);
      setTimeout(() => setError(null), 5000);
    }
  };

  // Eliminar cliente
  const handleDeleteClient = async (documento) => {
    if (!window.confirm(`¿Está seguro de eliminar el cliente con documento ${documento}?`)) {
      return;
    }

    try {
      const response = await fetch(`${API_URL}/${documento}`, {
        method: 'DELETE'
      });

      if (!response.ok) {
        const data = await response.json();
        throw new Error(data.error || 'Error al eliminar cliente');
      }

      setSuccess('Cliente eliminado exitosamente');
      loadClientes(pagination.currentPage);
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError(err.message);
      setTimeout(() => setError(null), 5000);
    }
  };

  // Generar items de paginación
  const renderPagination = () => {
    const items = [];
    for (let number = 1; number <= pagination.totalPages; number++) {
      items.push(
        <Pagination.Item
          key={number}
          active={number === pagination.currentPage}
          onClick={() => handlePageChange(number)}
        >
          {number}
        </Pagination.Item>
      );
    }
    return items;
  };

  return (
    <div className="container-fluid">
      <h2 className="mb-4">Gestión de Clientes</h2>

      {/* Alertas */}
      {error && (
        <Alert variant="danger" onClose={() => setError(null)} dismissible>
          {error}
        </Alert>
      )}
      
      {success && (
        <Alert variant="success" onClose={() => setSuccess(null)} dismissible>
          {success}
        </Alert>
      )}

      {/* Barra de herramientas */}
      <div className="row mb-3">
        <div className="col-md-6">
          <InputGroup>
            <InputGroup.Text>
              <i className="bi bi-search"></i>
            </InputGroup.Text>
            <Form.Control
              type="text"
              placeholder="Buscar por documento, nombre, email..."
              value={searchTerm}
              onChange={handleSearch}
            />
          </InputGroup>
        </div>
        <div className="col-md-6 text-end">
          <Button variant="primary" onClick={handleNewClient}>
            <i className="bi bi-plus-circle me-2"></i>
            Nuevo Cliente
          </Button>
        </div>
      </div>

      {/* Tabla de clientes */}
      <Table striped bordered hover responsive>
        <thead className="table-dark">
          <tr>
            <th>Documento</th>
            <th>Tipo Doc.</th>
            <th>Nombre</th>
            <th>Apellido</th>
            <th>Email</th>
            <th>Teléfono</th>
            <th>Dirección</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          {loading ? (
            <tr>
              <td colSpan="8" className="text-center">
                <div className="spinner-border text-primary" role="status">
                  <span className="visually-hidden">Cargando...</span>
                </div>
              </td>
            </tr>
          ) : clientes.length === 0 ? (
            <tr>
              <td colSpan="8" className="text-center">
                No se encontraron clientes
              </td>
            </tr>
          ) : (
            clientes.map((cliente) => (
              <tr key={cliente.documentoCliente}>
                <td>{cliente.documentoCliente}</td>
                <td>{cliente.tipoDocumentoCliente}</td>
                <td>{cliente.nombreCliente}</td>
                <td>{cliente.apellidoCliente}</td>
                <td>{cliente.emailCliente}</td>
                <td>{cliente.telefonoCliente}</td>
                <td>{cliente.direccionCliente}</td>
                <td>
                  <Button
                    variant="warning"
                    size="sm"
                    className="me-2"
                    onClick={() => handleEditClient(cliente)}
                    title="Editar"
                  >
                    <i className="bi bi-pencil"></i>
                  </Button>
                  <Button
                    variant="danger"
                    size="sm"
                    onClick={() => handleDeleteClient(cliente.documentoCliente)}
                    title="Eliminar"
                  >
                    <i className="bi bi-trash"></i>
                  </Button>
                </td>
              </tr>
            ))
          )}
        </tbody>
      </Table>

      {/* Paginación */}
      {pagination.totalPages > 1 && (
        <div className="d-flex justify-content-between align-items-center">
          <div>
            Mostrando {clientes.length} de {pagination.totalItems} clientes
          </div>
          <Pagination>
            <Pagination.First
              onClick={() => handlePageChange(1)}
              disabled={pagination.currentPage === 1}
            />
            <Pagination.Prev
              onClick={() => handlePageChange(pagination.currentPage - 1)}
              disabled={!pagination.hasPrevPage}
            />
            {renderPagination()}
            <Pagination.Next
              onClick={() => handlePageChange(pagination.currentPage + 1)}
              disabled={!pagination.hasNextPage}
            />
            <Pagination.Last
              onClick={() => handlePageChange(pagination.totalPages)}
              disabled={pagination.currentPage === pagination.totalPages}
            />
          </Pagination>
        </div>
      )}

      {/* Modal para formulario */}
      <Modal show={showModal} onHide={() => setShowModal(false)} size="lg">
        <Modal.Header closeButton>
          <Modal.Title>
            {editingClient ? 'Editar Cliente' : 'Nuevo Cliente'}
          </Modal.Title>
        </Modal.Header>
        <Form noValidate validated={validated} onSubmit={handleSaveClient}>
          <Modal.Body>
            <div className="row">
              <div className="col-md-6 mb-3">
                <Form.Group>
                  <Form.Label>Documento *</Form.Label>
                  <Form.Control
                    type="text"
                    name="documentoCliente"
                    value={formData.documentoCliente}
                    onChange={handleInputChange}
                    required
                    disabled={editingClient} // No permitir cambiar documento en edición
                    placeholder="Número de documento"
                    maxLength="20"
                  />
                  <Form.Control.Feedback type="invalid">
                    El documento es requerido
                  </Form.Control.Feedback>
                </Form.Group>
              </div>

              <div className="col-md-6 mb-3">
                <Form.Group>
                  <Form.Label>Tipo Documento</Form.Label>
                  <Form.Control
                    type="text"
                    name="tipoDocumentoCliente"
                    value={formData.tipoDocumentoCliente}
                    onChange={handleInputChange}
                    placeholder="Ej: CC, TI, NIT"
                  />
                </Form.Group>
              </div>

              <div className="col-md-6 mb-3">
                <Form.Group>
                  <Form.Label>Nombre *</Form.Label>
                  <Form.Control
                    type="text"
                    name="nombreCliente"
                    value={formData.nombreCliente}
                    onChange={handleInputChange}
                    required
                    placeholder="Nombre del cliente"
                  />
                  <Form.Control.Feedback type="invalid">
                    El nombre es requerido
                  </Form.Control.Feedback>
                </Form.Group>
              </div>

              <div className="col-md-6 mb-3">
                <Form.Group>
                  <Form.Label>Apellido *</Form.Label>
                  <Form.Control
                    type="text"
                    name="apellidoCliente"
                    value={formData.apellidoCliente}
                    onChange={handleInputChange}
                    required
                    placeholder="Apellido del cliente"
                  />
                  <Form.Control.Feedback type="invalid">
                    El apellido es requerido
                  </Form.Control.Feedback>
                </Form.Group>
              </div>

              <div className="col-md-6 mb-3">
                <Form.Group>
                  <Form.Label>Email</Form.Label>
                  <Form.Control
                    type="email"
                    name="emailCliente"
                    value={formData.emailCliente}
                    onChange={handleInputChange}
                    placeholder="correo@ejemplo.com"
                  />
                </Form.Group>
              </div>

              <div className="col-md-6 mb-3">
                <Form.Group>
                  <Form.Label>Teléfono</Form.Label>
                  <Form.Control
                    type="text"
                    name="telefonoCliente"
                    value={formData.telefonoCliente}
                    onChange={handleInputChange}
                    placeholder="Número telefónico"
                  />
                </Form.Group>
              </div>

              <div className="col-12 mb-3">
                <Form.Group>
                  <Form.Label>Dirección</Form.Label>
                  <Form.Control
                    type="text"
                    name="direccionCliente"
                    value={formData.direccionCliente}
                    onChange={handleInputChange}
                    placeholder="Dirección del cliente"
                  />
                </Form.Group>
              </div>
            </div>
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={() => setShowModal(false)}>
              Cancelar
            </Button>
            <Button variant="primary" type="submit">
              {editingClient ? 'Actualizar' : 'Guardar'}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>
    </div>
  );
};

export default Clientes;