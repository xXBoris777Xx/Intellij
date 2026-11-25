package boris.com.herramientasmatutuino.pedido;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    boolean existsByNombreClienteIgnoreCaseAndEstado(String nombreCliente, EstadoPedido estado);

    List<Pedido> findAllByOrderByIdDesc();

    @Query(value = """
            SELECT 1
            FROM (SELECT pg_advisory_xact_lock(hashtext(lower(:nombreCliente)))) AS bloqueo
            """, nativeQuery = true)
    Integer bloquearCliente(@Param("nombreCliente") String nombreCliente);
}
