package org.equiporon.DAO;

import org.equiporon.Conexion.ConexionBD;
import java.sql.Connection;
import java.sql.SQLException;

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
