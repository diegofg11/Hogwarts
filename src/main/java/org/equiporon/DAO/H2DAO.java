package org.equiporon.DAO;

import org.equiporon.Conexion.ConexionBD;
import java.sql.Connection;
import java.sql.SQLException;
/**
 *  Data Access Object (DAO) para la casa Hufflepuff (Base de Datos H2).
 *  NOTA: La conexión a Hufflepuff apunta a la URL de h2 según ConexionBD.
 *  @author  Igor, Gaizka
 *  @version 2.0
 */

public class H2DAO extends BaseDAO {

    @Override
    protected String getCasa() {
        return "Hufflepuff";
    }

    @Override
    protected Connection getConnection() throws SQLException {
        return ConexionBD.conectarCasa("Hufflepuff");
    }
}