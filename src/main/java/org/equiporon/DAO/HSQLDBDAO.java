package org.equiporon.DAO;

import org.equiporon.Conexion.ConexionBD;
import java.sql.Connection;
import java.sql.SQLException;
/**
 * Data Access Object (DAO) Asíncrono para gestionar operaciones CRUD en HSQLDB (Slytherin)
 * sobre la tabla ESTUDIANTES, utilizando Modelo_Estudiante.
 *
 * @author Igor, Gaizka, Diego, Unai
 * @version 2.0
 */
public class HSQLDBDAO extends BaseDAO {

    // 🔹 Pool de hilos para operaciones en segundo plano

    @Override
    protected String getCasa() {
        return "Slytherin";
    }

    @Override
    protected Connection getConnection() throws SQLException {
        return ConexionBD.conectarCasa("Slytherin");
    }
}