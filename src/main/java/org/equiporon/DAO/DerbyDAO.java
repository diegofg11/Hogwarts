package org.equiporon.DAO;

import org.equiporon.Conexion.ConexionBD;
import java.sql.Connection;
import java.sql.SQLException;
/**
 * DAO Asíncrono para gestionar operaciones CRUD en Apache Derby (Gryffindor)
 * sobre la tabla ESTUDIANTES, utilizando Modelo_Estudiante.
 *
 * @author Xiker, Igor, Gaizka
 * @version 2.0
 */
public class DerbyDAO extends BaseDAO {

    @Override
    protected String getCasa() {
        return "Gryffindor";
    }

    @Override
    protected Connection getConnection() throws SQLException {
        return ConexionBD.conectarCasa("Gryffindor");
    }

}
