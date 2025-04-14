package com.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;

import java.util.Optional;
import java.util.Map;

/**
 * Function principal para exponer un endpoint GraphQL.
 * Esta Function está diseñada para ser ejecutada en Azure Functions (FaaS).
 * Buenas prácticas aplicadas:
 * - Manejo de errores controlado.
 * - Separación de responsabilidades.
 * - Uso de constantes y variables finales.
 */
public class GraphQLFunction {

    /**
     * Instancia de GraphQL inicializada por GraphQLProvider.
     * Buenas prácticas:
     * - Usar instancia final y static para performance (singleton).
     */
    private static final GraphQL graphql = new GraphQLProvider().getGraphQL();

    /**
     * Azure Function expuesta en la ruta /api/graphql con método POST.
     * Esta función recibe las queries/mutations y las ejecuta contra el schema.
     *
     * @param request HttpRequestMessage con el body en formato JSON.
     * @param context ExecutionContext provisto por Azure Functions para logs y trazabilidad.
     * @return HttpResponseMessage con el resultado de la ejecución GraphQL.
     */
    @FunctionName("GraphQLHandler")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "graphql"
            )
            HttpRequestMessage<Optional<Map<String, Object>>> request,
            final ExecutionContext context
    ) {
        try {
            // Recuperamos el body, si no existe lanzamos excepción controlada.
            Map<String, Object> body = request.getBody()
                    .orElseThrow(() -> new IllegalArgumentException("Missing body"));

            // Obtenemos la query enviada por el cliente.
            String query = (String) body.get("query");

            // Obtenemos las variables, si no existen enviamos un Map vacío.
            Map<String, Object> variables = (Map<String, Object>) body.getOrDefault("variables", Map.of());

            // Construimos la entrada de ejecución para GraphQL.
            ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                    .query(query)
                    .variables(variables)
                    .build();

            // Ejecutamos la query/mutation contra el esquema.
            ExecutionResult result = graphql.execute(executionInput);

            // Devolvemos resultado exitoso.
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(result.toSpecification())
                    .build();

        } catch (Exception e) {
            // En caso de error devolvemos BAD_REQUEST con detalle.
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()))
                    .build();
        }
    }
}