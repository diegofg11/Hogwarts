package org.equiporon.DAO;

import org.equiporon.Conexion.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Consumer;

/**
 * DAO para gestionar operaciones CRUD en Apache Derby (Gryffindor)
 * sobre la tabla ESTUDIANTES.
 *
 * @author Xiker
 * @version 1.4
 */
public class DerbyDAO {

    private final String casa = "Gryffindor";

    /**
     * Ejecuta una operación SQL con verificación de conexión segura.
     *
     * @param sql         Sentencia SQL parametrizada.
     * @param configurador Lambda para configurar el PreparedStatement.
     * @return true si la operación fue exitosa, false en caso contrario.
     */
    private boolean ejecutarOperacion(String sql, Consumer<PreparedStatement> configurador) {
        try (Connection conn = ConexionBD.conectarCasa(casa)) {

            if (conn == null) {
                System.out.println("❌ No se pudo establecer conexión con la base de datos Derby.");
                return false;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                configurador.accept(ps);
                ps.executeUpdate();
                return true;
            }

        } catch (SQLException e) {
            System.out.println("❌ Error al ejecutar operación en Derby: " + e.getMessage());
            return false;
        }
    }

    public boolean aniadir(String id, String nombre, String apellidos, int curso, String patronus) {
        String sql = "INSERT INTO ESTUDIANTES (ID, NOMBRE, APELLIDOS, CURSO, PATRONUS) VALUES (?, ?, ?, ?, ?)";
        return ejecutarOperacion(sql, ps -> {
            try {
                ps.setString(1, id);
                ps.setString(2, nombre);
                ps.setString(3, apellidos);
                ps.setInt(4, curso);
                ps.setString(5, patronus);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public boolean editar(String id, String nombre, String apellidos, int curso, String patronus) {
        String sql = "UPDATE ESTUDIANTES SET NOMBRE = ?, APELLIDOS = ?, CURSO = ?, PATRONUS = ? WHERE ID = ?";
        return ejecutarOperacion(sql, ps -> {
            try {
                ps.setString(1, nombre);
                ps.setString(2, apellidos);
                ps.setInt(3, curso);
                ps.setString(4, patronus);
                ps.setString(5, id);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public boolean borrar(String id) {
        String sql = "DELETE FROM ESTUDIANTES WHERE ID = ?";
        return ejecutarOperacion(sql, ps -> {
            try {
                ps.setString(1, id);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
