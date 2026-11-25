# API de pedidos

La aplicación usa dos bases de datos PostgreSQL: `productos_db` almacena los
productos y `pedidos_db` almacena los pedidos. Flyway crea las tablas de cada
base al iniciar. Los productos de un pedido se guardan como JSONB en la tabla
`pedidos`; no hay tabla intermedia.

## Ejecutar desde IntelliJ IDEA

1. Abre `pom.xml` en la ventana del proyecto y selecciona **Add as Maven Project**
   si IntelliJ todavía no ha importado Maven. También puedes abrir la ventana
   **Maven** y pulsar **Reload All Maven Projects**.
2. Espera a que termine la importación. En **Run → Edit Configurations…** crea
   una configuración **Spring Boot** y selecciona el módulo
   `Herramientas-Matutuino` en **Use classpath of module**. La clase principal es
   `boris.com.herramientasmatutuino.HerramientasMatutuinoApplication`.
3. En **Modify options → Environment variables**, configura localmente:

   ```text
   PRODUCTOS_DB_URL=jdbc:postgresql://localhost:5432/productos_db
   PEDIDOS_DB_URL=jdbc:postgresql://localhost:5432/pedidos_db
   DB_USERNAME=postgres
   DB_PASSWORD=la_contraseña_de_PostgreSQL
   ```

   No guardes ni compartas la contraseña en el repositorio. Ambas bases deben
   existir en PostgreSQL y el servicio debe estar iniciado.
4. Ejecuta la configuración. Flyway aplicará una migración a cada base al
   iniciar la aplicación, disponible en `http://localhost:8081`.

Si IntelliJ no muestra el módulo, verifica que abriste la carpeta del proyecto
que contiene `pom.xml`, agrégalo como proyecto Maven y vuelve a recargar Maven.
Como alternativa, puedes ejecutar `mvnw.cmd spring-boot:run` desde la terminal
de IntelliJ después de definir allí las mismas variables de entorno.

## Separación y disponibilidad

Separar productos y pedidos en dos bases permite que las operaciones que usan
solo una base sigan funcionando si la otra queda fuera de servicio. Crear un
pedido requiere ambas: reservar stock en `productos_db` y guardar el pedido en
`pedidos_db`. La aplicación intenta devolver el stock si no puede guardar el
pedido, pero las dos bases no comparten una transacción distribuida.

Las dos bases creadas en pgAdmin están en el mismo servidor PostgreSQL. Esto
separa los datos, pero no protege contra la caída del servidor completo. Para
eso harían falta servidores distintos, replicación y un mecanismo de failover.

## Endpoints

- `GET /api/productos`: listar productos.
- `POST /api/productos`: crear un producto (`nombre`, `descripcion`, `stock`).
- `PUT /api/productos/{id}`: actualizar un producto.
- `GET /api/pedidos`: listar pedidos.
- `POST /api/pedidos`: crear un pedido con `nombreCliente` y una lista `productos`
  de `{ "productoId": 1, "cantidad": 2 }`. La respuesta incluye el código de
  verificación generado; compártelo con el cliente.
- `PATCH /api/pedidos/{id}/enviar`: cambiar de pendiente a enviado.
- `PATCH /api/pedidos/{id}/recibir`: cambiar de enviado a recibido enviando
  `{ "codigoVerificacion": "123456" }`.

El stock se descuenta al crear el pedido. Un cliente no puede tener más de un
pedido pendiente; el stock se bloquea durante la transacción para evitar
descontarlo dos veces en pedidos simultáneos.
