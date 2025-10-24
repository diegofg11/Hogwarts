package org.equiporon.DAO;

import org.equiporon.Conexion.ConexionBD;
import java.sql.Connection;
import java.sql.SQLException;
/**
 * DAO Asíncrono para gestionar operaciones CRUD en Oracle (Ravenclaw)
 * sobre la tabla ESTUDIANTES, utilizando Modelo_Estudiante.
 *
 * @author Xiker, Unai, Gaizka, Igor
 * @version 2.0
 */
public class OracleDAO extends BaseDAO {

    @Override
    protected String getCasa() {
        return "Ravenclaw";
    }

    @Override
    protected Connection getConnection() throws SQLException {
        return ConexionBD.conectarCasa("Ravenclaw");
    }
}