package com.function;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Clase encargada de gestionar la conexión a la base de datos Oracle.
 * Buenas prácticas aplicadas:
 * - Centralización de la conexión
 * - Manejo seguro de credenciales vía variables de entorno
 * - Logs informativos para depuración
 */
public class OracleDBConnection {

    // Logger propio de la clase
    private static final Logger logger = Logger.getLogger(OracleDBConnection.class.getName());

    /**
     * Método utilitario para probar si la conexión a la base de datos funciona correctamente.
     *
     * @return true si la conexión es válida, false si falla.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            boolean isValid = conn.isValid(5); // Timeout 5 segundos
            logger.info("Conexión a la base de datos probada: " + (isValid ? "Válida" : "Inválida"));
            return isValid;
        } catch (SQLException e) {
            logger.severe("Error al probar la conexión: " + e.getMessage());
            return false;
        }
    }

    /**
     * Método principal que construye y devuelve una conexión activa a Oracle DB.
     * Utiliza el Wallet configurado y las credenciales desde variables de entorno.
     *
     * @return Connection activa hacia Oracle.
     * @throws SQLException en caso de error de conexión.
     */
    public static Connection getConnection() throws SQLException {

        // Construcción de URL de conexión usando variables de entorno
        String url = "jdbc:oracle:thin:@" + System.getenv("ORACLE_TNS_NAME")
                + "?TNS_ADMIN=" + System.getenv("ORACLE_WALLET_PATH");

        String user = System.getenv("ORACLE_USER");
        String password = System.getenv("ORACLE_PASSWORD");
        String walletLocation = System.getenv("ORACLE_WALLET_PATH");

        // Propiedades específicas para conexión segura con wallet
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("oracle.net.ssl_version", "1.2");
        props.setProperty("oracle.net.wallet_location",
                "(SOURCE=(METHOD=file)(METHOD_DATA=(DIRECTORY=" + walletLocation + ")))");

        // Logs informativos de la configuración
        logger.info("Configurando conexión a la base de datos:");
        logger.info("URL: " + url);
        logger.info("Usuario: " + user);
        logger.info("Ubicación del wallet: " + props.getProperty("oracle.net.wallet_location"));

        try {
            // Conexión final
            Connection conn = DriverManager.getConnection(url, props);
            logger.info("OK, Conexión establecida correctamente.");
            return conn;
        } catch (SQLException e) {
            logger.severe("Error al establecer la conexión: " + e.getMessage());
            throw e;
        }
    }
}
