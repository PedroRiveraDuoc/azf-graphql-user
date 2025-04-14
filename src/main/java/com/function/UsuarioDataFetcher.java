package com.function;

import graphql.schema.DataFetcher;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * DataFetchers para operaciones CRUD de usuarios.
 * Cada método representa una Query o Mutation definida en el esquema GraphQL.
 *
 * Buenas prácticas aplicadas:
 * - Validación de parámetros
 * - Manejo de errores claros
 * - Uso de try-with-resources para conexiones seguras
 * - SQL parametrizado para evitar inyecciones
 */
@Component
public class UsuarioDataFetcher {

    /**
     * Mutation: createUser
     * Crea un nuevo usuario en la base de datos.
     */
    public DataFetcher<String> createUser() {
        return environment -> {
            String nombre = environment.getArgument("nombre");
            String email = environment.getArgument("email");
            String password = environment.getArgument("password");

            if (nombre == null || email == null || password == null) {
                throw new RuntimeException("Faltan campos requeridos, formato: nombre, email o contraseña.");
            }

            try (Connection conn = OracleDBConnection.getConnection()) {
                String sql = "INSERT INTO USUARIO (NOMBRE, EMAIL, PASSWORD) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    System.out.println("SQL: " + sql);
                    stmt.setString(1, nombre);
                    stmt.setString(2, email);
                    stmt.setString(3, password);
                    System.out.println("INSERT: " + nombre + ", " + email + "," + password);
                    stmt.executeUpdate();
                    return "Usuario creado exitosamente.";
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error al crear el usuario: " + e.getMessage());
            }
        };
    }

    /**
     * Query: getUser
     * Recupera un usuario por su ID.
     */
    public DataFetcher<Map<String, Object>> getUser() {
        return environment -> {
            String idString = environment.getArgument("id");
            Integer id;

            try {
                id = Integer.parseInt(idString);
                System.out.println("ID proporcionado: " + id);
            } catch (NumberFormatException e) {
                throw new RuntimeException("El id proporcionado no es un número válido: " + idString);
            }

            try (Connection conn = OracleDBConnection.getConnection()) {
                String sql = "SELECT * FROM USUARIO WHERE ID_USUARIO = ?";
                System.out.println("SQL: " + sql);
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, id);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            Map<String, Object> user = new HashMap<>();
                            user.put("id", rs.getInt("ID_USUARIO"));
                            user.put("nombre", rs.getString("NOMBRE"));
                            user.put("email", rs.getString("EMAIL"));
                            user.put("estado", rs.getInt("ESTADO"));
                            System.out.println("Usuario encontrado: " + user);
                            return user;
                        } else {
                            throw new RuntimeException("Usuario con id " + id + " no encontrado.");
                        }
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error al recuperar el usuario: " + e.getMessage());
            }
        };
    }

    /**
     * Mutation: updateUser
     * Permite actualizar campos opcionales de un usuario por su ID.
     */
    public DataFetcher<String> updateUser() {
        return environment -> {
            String idString = environment.getArgument("id");
            Integer id;
            String nombre = environment.getArgument("nombre");
            String email = environment.getArgument("email");
            String password = environment.getArgument("password");
            Integer estado = environment.getArgument("estado");

            try {
                id = Integer.parseInt(idString);
                System.out.println("ID proporcionado: " + id);
            } catch (NumberFormatException e) {
                throw new RuntimeException("El id proporcionado no es un número válido: " + idString);
            }

            // Construcción dinámica del SQL en base a campos no nulos
            StringBuilder sql = new StringBuilder("UPDATE USUARIO SET ");
            List<Object> parameters = new ArrayList<>();

            if (nombre != null) {
                sql.append("NOMBRE = ?, ");
                parameters.add(nombre);
            }
            if (email != null) {
                sql.append("EMAIL = ?, ");
                parameters.add(email);
            }
            if (password != null) {
                sql.append("PASSWORD = ?, ");
                parameters.add(password);
            }
            if (estado != null) {
                sql.append("ESTADO = ?, ");
                parameters.add(estado);
            }

            if (sql.toString().endsWith(", ")) {
                sql.setLength(sql.length() - 2); // Remueve la coma final
            }

            sql.append(" WHERE ID_USUARIO = ?");
            parameters.add(id);

            try (Connection conn = OracleDBConnection.getConnection()) {
                try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                    for (int i = 0; i < parameters.size(); i++) {
                        stmt.setObject(i + 1, parameters.get(i));
                    }

                    int rowsUpdated = stmt.executeUpdate();
                    return rowsUpdated > 0 ? "Usuario actualizado exitosamente." : "Usuario no encontrado.";
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error al actualizar el usuario: " + e.getMessage());
            }
        };
    }

    /**
     * Mutation: deleteUser
     * Elimina un usuario por su ID de manera física (DELETE real).
     * Se puede cambiar a eliminación lógica usando `estado = 0`.
     */
    public DataFetcher<String> deleteUser() {
        return environment -> {
            String idString = environment.getArgument("id");
            Integer id;

            try {
                id = Integer.parseInt(idString);
                System.out.println("ID proporcionado: " + id);
            } catch (NumberFormatException e) {
                throw new RuntimeException("El id proporcionado no es un número válido: " + idString);
            }

            try (Connection conn = OracleDBConnection.getConnection()) {
                String sql = "DELETE FROM USUARIO WHERE ID_USUARIO = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, id);
                    int rowsDeleted = stmt.executeUpdate();
                    return rowsDeleted > 0 ? "Usuario eliminado exitosamente." : "Usuario no encontrado.";
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error al eliminar el usuario: " + e.getMessage());
            }
        };
    }

    /**
     * Query: listUsers
     * Devuelve todos los usuarios de la tabla.
     * (Mejora sugerida: filtrar solo los que tienen estado = 1)
     */
    public DataFetcher<List<Map<String, Object>>> listUsers() {
        return environment -> {
            List<Map<String, Object>> users = new ArrayList<>();

            try (Connection conn = OracleDBConnection.getConnection()) {
                String sql = "SELECT * FROM USUARIO";
                try (PreparedStatement stmt = conn.prepareStatement(sql);
                        ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        Map<String, Object> user = new HashMap<>();
                        user.put("id", rs.getInt("ID_USUARIO"));
                        user.put("nombre", rs.getString("NOMBRE"));
                        user.put("email", rs.getString("EMAIL"));
                        user.put("estado", rs.getInt("ESTADO"));
                        users.add(user);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error al listar usuarios: " + e.getMessage());
            }

            return users;
        };
    }
}