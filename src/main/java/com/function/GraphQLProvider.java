package com.function;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Clase encargada de cargar el esquema GraphQL desde el archivo .graphqls
 * y enlazarlo con los data fetchers definidos para Queries y Mutations.
 *
 * Esta clase aplica buenas prácticas como:
 * - Validación del archivo de esquema
 * - Manejo de errores con mensajes descriptivos
 * - Separación de responsabilidades entre esquema y lógica
 */
public class GraphQLProvider {

    // Instancia GraphQL construida desde el esquema + wiring
    private final GraphQL graphQL;

    /**
     * Constructor que inicializa el motor GraphQL
     * a partir del archivo `schema.graphqls` y el wiring con los data fetchers.
     */
    public GraphQLProvider() {
        try {
            // Cargar el esquema desde resources/schema.graphqls
            InputStream schemaStream = getClass().getClassLoader().getResourceAsStream("schema.graphqls");
            if (schemaStream == null) {
                throw new RuntimeException("Validar schema.graphqls en resources, no encontrado.");
            }

            // Parsear el contenido del schema
            InputStreamReader reader = new InputStreamReader(schemaStream);
            TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(reader);

            // Instanciar los data fetchers de usuario
            UsuarioDataFetcher usuarioDataFetcher = new UsuarioDataFetcher();

            // Enlazar tipos de esquema con sus resolvers (queries y mutations)
            RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
                    .type("Query", typeWiring -> typeWiring
                            .dataFetcher("getUser", usuarioDataFetcher.getUser())
                            .dataFetcher("listUsers", usuarioDataFetcher.listUsers()))
                    .type("Mutation", typeWiring -> typeWiring
                            .dataFetcher("createUser", usuarioDataFetcher.createUser())
                            .dataFetcher("updateUser", usuarioDataFetcher.updateUser())
                            .dataFetcher("deleteUser", usuarioDataFetcher.deleteUser()))
                    .build();

            // Crear el esquema ejecutable
            GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring);
            graphQL = GraphQL.newGraphQL(schema).build();

        } catch (Exception e) {
            // Manejo de error en caso de fallo al cargar el esquema
            e.printStackTrace();
            throw new RuntimeException("Error al inicializar GraphQL: " + e.getMessage(), e);
        }
    }

    /**
     * Devuelve la instancia GraphQL lista para ejecutar consultas.
     *
     * @return instancia de GraphQL
     */
    public GraphQL getGraphQL() {
        return graphQL;
    }
}