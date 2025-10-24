package org.equiporon.DAO;

import org.equiporon.Conexion.ConexionBD;
import java.sql.Connection;
import java.sql.SQLException;

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