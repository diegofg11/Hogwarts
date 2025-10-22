package org.equiporon.DAO;

import org.equiporon.Conexion.ConexionBD;
import java.sql.Connection;
import java.sql.SQLException;

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
