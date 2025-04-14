# Sistema de Gestión de Usuarios - GraphQL + Azure Functions

Este proyecto es parte de la asignatura **Desarrollo Cloud Native II (DSY2207)** de Duoc UC y consiste en un sistema CRUD de usuarios utilizando arquitectura **Serverless**, **GraphQL**, **Azure Functions** y base de datos **Oracle**.

---

## 🧱 Tecnologías Utilizadas

- Java 17
- Azure Functions
- GraphQL Java
- Oracle DB + Wallet
- Spring Context
- Maven
- Postman (para pruebas)
- Docker (para entorno local opcional)

---

## ⚙️ Estructura del Proyecto

```
graphql_user/
├── src/
│   ├── main/
│   │   ├── java/com/function/   # Código principal
│   │   └── resources/
│   │       ├── schema.graphqls  # Definición del esquema GraphQL
│   │       └── wallet/          # Oracle Wallet
├── target/
│   └── azure-functions/         # Carpeta generada para deploy
└── pom.xml                      # Configuración Maven
```

---

## 🚀 Ejecución Local

1. **Instalar requisitos:**
   - Java 17
   - Maven
   - Azure Functions Core Tools (`npm install -g azure-functions-core-tools@4`)
   - Oracle Wallet en `src/main/resources/wallet`

2. **Configurar variables en `local.settings.json`:**
```json
{
  "IsEncrypted": false,
  "Values": {
    "AzureWebJobsStorage": "UseDevelopmentStorage=true",
    "FUNCTIONS_WORKER_RUNTIME": "java",
    "ORACLE_USER": "back_vet",
    "ORACLE_PASSWORD": "********",
    "ORACLE_TNS_NAME": "xxxxx_high",
    "ORACLE_WALLET_PATH": "C:/ruta/a/tu/wallet"
  }
}
```

3. **Ejecutar localmente:**
```bash
mvn clean package
cd target/azure-functions/azf-GraphQL-CreateUser
func start
```

4. **Probar desde Postman:**

- Crear:
```graphql
mutation {
  createUser(nombre: "Pedro", email: "pedro@example.com", password: "123456")
}
```

- Listar:
```graphql
{
  listUsers { id nombre email estado }
}
```

---

## ☁️ Despliegue en Azure

1. **Editar en `pom.xml`:**
```xml
<functionAppName>azf-GraphQL-CreateUser</functionAppName>
<resourceGroup>java-functions-group</resourceGroup>
<region>westus</region>
```

2. **Deploy:**
```bash
mvn azure-functions:deploy
```

3. **En Azure Portal:**
   - Ir a tu Function App > Configuration
   - Agregar las variables de entorno
   - Asegurar que `ORACLE_WALLET_PATH` apunte a `D:\home\site\wwwroot\wallet`

---

## 🧠 Consideraciones sobre el campo `estado`

El campo `estado` permite controlar la visibilidad del usuario:

- `1` = Activo
- `0` = Inactivo o eliminado lógico (recomendado para producción)

---

## ✅ Buenas Prácticas Aplicadas

- Arquitectura desacoplada (GraphQL separado por capa)
- Uso de Oracle Wallet seguro
- Variables de entorno para configuración sensible
- Código comentado y documentado (Javadoc + logs)
- SQL parametrizado para evitar inyección
- `try-with-resources` en JDBC
