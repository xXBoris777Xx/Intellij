package boris.com.herramientasmatutuino.pedido;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public List<PedidoResponse> listar() {
        return pedidoService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoCreadoResponse crear(@RequestBody CrearPedidoRequest request) {
        return pedidoService.crear(request);
    }

    @PatchMapping("/{id}/enviar")
    public PedidoResponse enviar(@PathVariable Integer id) {
        return pedidoService.enviar(id);
    }

    @PatchMapping("/{id}/recibir")
    public PedidoResponse recibir(@PathVariable Integer id, @RequestBody RecibirPedidoRequest request) {
        return pedidoService.recibir(id, request == null ? null : request.codigoVerificacion());
    }

    public record CrearPedidoRequest(String nombreCliente, List<ItemRequest> productos) {
    }

    public record ItemRequest(Integer productoId, Integer cantidad) {
    }

    public record RecibirPedidoRequest(String codigoVerificacion) {
    }

    public record PedidoResponse(
            Integer id,
            String nombreCliente,
            EstadoPedido estado,
            List<ProductoPedido> productos
    ) {
        static PedidoResponse desde(Pedido pedido) {
            return new PedidoResponse(pedido.getId(), pedido.getNombreCliente(), pedido.getEstado(), pedido.getProductos());
        }
    }

    public record PedidoCreadoResponse(PedidoResponse pedido, String codigoVerificacion) {
    }
}
