package boris.com.herramientasmatutuino.pedido;

import boris.com.herramientasmatutuino.producto.ProductoService;
import boris.com.herramientasmatutuino.pedido.PedidoController.CrearPedidoRequest;
import boris.com.herramientasmatutuino.pedido.PedidoController.ItemRequest;
import boris.com.herramientasmatutuino.pedido.PedidoController.PedidoCreadoResponse;
import boris.com.herramientasmatutuino.pedido.PedidoController.PedidoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class PedidoService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PedidoRepository pedidos;
    private final ProductoService productos;

    public PedidoService(PedidoRepository pedidos, ProductoService productos) {
        this.pedidos = pedidos;
        this.productos = productos;
    }

    @Transactional(transactionManager = "pedidosTransactionManager", readOnly = true)
    public List<PedidoResponse> listar() {
        return pedidos.findAllByOrderByIdDesc().stream().map(PedidoResponse::desde).toList();
    }

    @Transactional(transactionManager = "pedidosTransactionManager")
    public PedidoCreadoResponse crear(CrearPedidoRequest request) {
        if (request == null || request.nombreCliente() == null || request.nombreCliente().isBlank()
                || request.productos() == null || request.productos().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente y al menos un producto son obligatorios");
        }
        String nombreCliente = request.nombreCliente().trim();
        if (nombreCliente.length() > 150) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del cliente no puede superar 150 caracteres");
        }
        pedidos.bloquearCliente(nombreCliente);
        if (pedidos.existsByNombreClienteIgnoreCaseAndEstado(nombreCliente, EstadoPedido.PENDIENTE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El cliente ya tiene un pedido pendiente");
        }

        Map<Integer, Integer> cantidades = new TreeMap<>();
        for (ItemRequest item : request.productos()) {
            if (item == null || item.productoId() == null || item.cantidad() == null || item.cantidad() < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto y cantidad positiva son obligatorios");
            }
            try {
                cantidades.merge(item.productoId(), item.cantidad(), Math::addExact);
            } catch (ArithmeticException exception) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad solicitada es demasiado grande");
            }
        }

        List<ProductoPedido> detalle = productos.reservarStock(cantidades);

        String codigo = String.format("%06d", RANDOM.nextInt(1_000_000));
        Pedido pedido;
        try {
            pedido = pedidos.saveAndFlush(new Pedido(nombreCliente, codigo, detalle));
        } catch (RuntimeException exception) {
            Map<Integer, Integer> stockADevolver = new TreeMap<>();
            detalle.forEach(item -> stockADevolver.merge(item.productoId(), item.cantidad(), Integer::sum));
            productos.devolverStock(stockADevolver);
            throw exception;
        }
        return new PedidoCreadoResponse(PedidoResponse.desde(pedido), codigo);
    }

    @Transactional(transactionManager = "pedidosTransactionManager")
    public PedidoResponse enviar(Integer id) {
        Pedido pedido = buscarPedido(id);
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se puede enviar un pedido pendiente");
        }
        pedido.enviar();
        return PedidoResponse.desde(pedido);
    }

    @Transactional(transactionManager = "pedidosTransactionManager")
    public PedidoResponse recibir(Integer id, String codigo) {
        Pedido pedido = buscarPedido(id);
        try {
            pedido.recibir(codigo == null ? "" : codigo);
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, exception.getMessage());
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
        return PedidoResponse.desde(pedido);
    }

    private Pedido buscarPedido(Integer id) {
        return pedidos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
    }
}
