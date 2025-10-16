package org.equiporon;

import org.equiporon.Conexion.ConexionBD;
import org.equiporon.Conexion.ConexionH2;
import org.equiporon.Conexion.ConexionHSQLDB;

import java.sql.Connection;
import java.sql.SQLException;

// 1. [NUEVO] Interfaz Funcional Personalizada
//    Esta interfaz es idéntica a Supplier, pero declara explícitamente throws SQLException.
@FunctionalInterface
interface DBConnectionSupplier {
    Connection get() throws SQLException;
}

public class TestConexiones {

    public static void main(String[] args) {
        System.out.println("--- INICIANDO PRUEBAS DE CONEXIÓN ---");

        // Los métodos de las clases de conexión cumplen con la nueva interfaz.
        testConnection("H2 (Hufflepuff)", ConexionH2::getConnection);
        testConnection("HSQLDB (Slytherin)", ConexionHSQLDB::getConnection);
        testConnection("MariaDB (Global)", ConexionBD::getConnection);
    }

    /**
     * Intenta obtener una conexión y la cierra.
     * Ahora usa DBConnectionSupplier para declarar que SQLException es posible.
     */
    // 2. [CORRECCIÓN] Cambiamos Supplier<Connection> por DBConnectionSupplier
    private static void testConnection(String dbName, DBConnectionSupplier connectionSupplier) {
        System.out.printf("\n[TEST] Probando conexión a %s...\n", dbName);
        Connection conn = null;
        try {
            // El compilador ahora sabe que .get() puede lanzar SQLException.
            conn = connectionSupplier.get();
            System.out.printf("✅ ÉXITO: Conexión a %s establecida correctamente.\n", dbName);
        } catch (SQLException e) { // Este catch ahora es alcanzable y obligatorio.
            System.out.printf("❌ FALLO: No se pudo conectar a %s.\n", dbName);
            System.err.println("Mensaje de Error SQL: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ ERROR INESPERADO en la conexión a " + dbName + ": " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                    System.out.printf("   Conexión a %s cerrada.\n", dbName);
                } catch (SQLException e) {
                    System.err.println("Error al cerrar la conexión: " + e.getMessage());
                }
            }
        }
    }
}