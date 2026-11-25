package boris.com.herramientasmatutuino.producto;

import boris.com.herramientasmatutuino.pedido.ProductoPedido;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class ProductoService {
    private final ProductoRepository productos;

    public ProductoService(ProductoRepository productos) {
        this.productos = productos;
    }

    @Transactional(transactionManager = "productosTransactionManager", readOnly = true)
    public List<Producto> listar() {
        return productos.findAll();
    }

    @Transactional(transactionManager = "productosTransactionManager")
    public Producto crear(String nombre, String descripcion, Integer stock) {
        return productos.save(new Producto(nombre, descripcion, stock));
    }

    @Transactional(transactionManager = "productosTransactionManager")
    public Producto actualizar(Integer id, String nombre, String descripcion, Integer stock) {
        Producto producto = productos.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        producto.actualizar(nombre, descripcion, stock);
        return productos.save(producto);
    }

    @Transactional(transactionManager = "productosTransactionManager")
    public List<ProductoPedido> reservarStock(Map<Integer, Integer> cantidades) {
        List<ProductoPedido> detalle = new ArrayList<>();
        for (Map.Entry<Integer, Integer> item : new TreeMap<>(cantidades).entrySet()) {
            Producto producto = productos.buscarPorIdConBloqueo(item.getKey())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Producto no encontrado: " + item.getKey()));
            if (producto.getStock() == null || producto.getStock() < item.getValue()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT, "Stock insuficiente para: " + producto.getNombre());
            }
            producto.descontarStock(item.getValue());
            detalle.add(new ProductoPedido(producto.getId(), producto.getNombre(), item.getValue()));
        }
        return detalle;
    }

    @Transactional(transactionManager = "productosTransactionManager")
    public void devolverStock(Map<Integer, Integer> cantidades) {
        for (Map.Entry<Integer, Integer> item : new TreeMap<>(cantidades).entrySet()) {
            Producto producto = productos.buscarPorIdConBloqueo(item.getKey())
                    .orElseThrow(() -> new IllegalStateException(
                            "No se pudo devolver el stock: producto inexistente " + item.getKey()));
            producto.actualizar(producto.getNombre(), producto.getDescripcion(),
                    Math.addExact(producto.getStock(), item.getValue()));
        }
    }
}
