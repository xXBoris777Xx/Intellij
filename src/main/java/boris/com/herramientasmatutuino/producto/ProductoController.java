package boris.com.herramientasmatutuino.producto;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    private final ProductoService productos;

    public ProductoController(ProductoService productos) {
        this.productos = productos;
    }

    @GetMapping
    public List<Producto> listar() {
        return productos.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Producto crear(@RequestBody ProductoRequest request) {
        validar(request);
        return productos.crear(request.nombre(), request.descripcion(), request.stock());
    }

    @PutMapping("/{id}")
    public Producto actualizar(@PathVariable Integer id, @RequestBody ProductoRequest request) {
        validar(request);
        return productos.actualizar(id, request.nombre(), request.descripcion(), request.stock());
    }

    private void validar(ProductoRequest request) {
        if (request == null || request.nombre() == null || request.nombre().isBlank()
                || request.nombre().length() > 100
                || request.stock() == null || request.stock() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre (maximo 100 caracteres) y stock valido son obligatorios");
        }
    }

    public record ProductoRequest(String nombre, String descripcion, Integer stock) {
    }
}
