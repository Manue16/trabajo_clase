import { Router } from'express';
import pool from '../db.js';

const router = Router();

router.get('/', async (req, res) => {
    try {
        const page = parseInt(req.query.page) || 1;
        const limit = parseInt(req.query.limit) || 10;
        const search = req.query.search || '';
        const offset = (page - 1) * limit;

        let query = 'SELECT * FROM clientes';
        let countQuery = 'SELECT COUNT(*) AS total FROM clientes';
        const params = [];
        if (search) {
            const searchCondition = ` WHERE
            documentoCliente LIKE ? OR
            nombreCliente LIKE ? OR
            apellidoCliente LIKE ?`;
        
        query += searchCondition;
        countQuery += searchCondition;

        const searchParam = `%${search}%`;
        params.push(searchParam, searchParam, searchParam);
    }

    query += ' ORDER BY apellidosCliente ASC LIMIT ? OFFSET ?';

    const [rows] = await pool.query(query, [...params, limit, offset]);
    const [countResult] = await pool.query(countQuery, params);
    const total = countResult[0].total;
    const totalPages = Math.ceil(total / limit);
    res.json({
        clientes: rows,
        pagination: {
            currentPage: page,
            totalPages,
            totalItems: total,
            limit,
            hasNextPage: page < totalPages,
            hasPrevPage: page > 1
        },
    });
} catch (error){
    console.error('error al obtener clientes:', error);
    res.status(500).json({ error:error.message });
}
});

// Funcion obtener un cliente por documento
router.get('/:documento',async (req,res) => {
    try{
        const [rows] = await pool.query(
            'SELECT * FROM clientes WHERE documentoCliente = ?',
            [req.params.documento]
        );

        if (rows.length === 0 ){
            return res.status(404).json({mensaje: 'Cliente no encontrado'});
        }
        res.json(rows[0]);
    }catch (error){
        res.status(500).json({ error: error.message });
    }
});

// Crear un nuevo cliente
router.post ('/', async (req,res) => {
    try{
        const {
            documentoCliente,
            tipoDocumentoCliente,
            nombreCliente,
            apellidoCLiente,
            emailCliente,
            telefonoCliente,
            direccionCliente
        } = req.body;

        // Validar campos requeridos
        if(!documentoCliente || !nombreCliente || !apellidoCLiente) {
            return res.status(400).json({
                error: 'Documento, nombre y apellido son campos requeridos'
            });
        }

        // Verificar si ya existe el documento
        const [existing] = await pool.query(
            'SELECT documentoCliente FROM clientes WHERE documentoCliente = ?',
            [documentoCliente]
        );

        if (existing.length > 0) {
            return res.status(400).json({
                error: 'Ya existe un cliente con ese documento'
            });
        }

        await pool.query(
            `INSERT INTO clientes ( 
                documentoCliente,
                tipoDocumentoCliente,
                nombreCliente,
                apellidoCliente,
                emailCliente,
                telefonoCliente,
                direccionCliente
            ) VALUES (?,?,?,?,?,?,?)`,
            [documentoCliente, tipoDocumentoCliente, nombreCliente, apellidoCLiente,
                emailCliente, telefonoCliente, direccionCliente]
        );

        res.status(201).json({
            mensaje: 'Cliente creado exitosamente',
            cliente: req.body
        });
    } catch (error) {
        console.error('Error al crear cliente:', error);
        res.status(500).json({error: error.message});
    }
});

// Actualizar cliente
router.put('/:documento', async (req, res) => {

    try{
        const{
            tipoDocumentoCliente,
            nombreCliente,
            apellidoCLiente,
            emailCliente,
            telefonoCliente,
            direccionCliente
        } = req.body;

        const [result] = await pool.query(
            `UPDATE clientes SET
            tipoDocumentoCliente = ?,
            nombreCliente = ?,
            apellidoCLiente = ?,
            emailCliente = ?,
            telefonoCliente = ?,
            direccionCliente = ?`,
            [tipoDocumentoCliente, nombreCliente, apellidoCLiente,
                emailCliente, telefonoCliente, direccionCliente, req.params.documento]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({ error: 'Cliente no encontrado' });
        }

        res.json({ mensaje: 'Cliente actualizado exitosamente'})
    } catch (error) {
        res.status(500).json({ error: error.message})
    }
});


// Eliminar cliente 

router.delete('/:documento', async (req, res) => {
    try{
        const [result] = await pool.query(
            'DELETE FROM clientes WHERE documentoCliente = ?', 
            [req.params.documento]
        );

        if (result.affectedRows === 0) {
            return res.status(404).json({error: 'Cliente no encontrado'});
        }

        res.json({mensaje: 'Cliente eliminado exitosamente'});
    } catch (error) {
        res.status(500).json({ error: error.message})
    }
});

export default router;