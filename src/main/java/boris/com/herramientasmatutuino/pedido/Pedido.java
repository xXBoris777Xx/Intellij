package boris.com.herramientasmatutuino.pedido;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre_cliente", nullable = false, length = 150)
    private String nombreCliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado;

    @Column(name = "codigo_verificacion", nullable = false, length = 6)
    private String codigoVerificacion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "productos", nullable = false, columnDefinition = "jsonb")
    private List<ProductoPedido> productos;

    protected Pedido() {
    }

    public Pedido(String nombreCliente, String codigoVerificacion, List<ProductoPedido> productos) {
        this.nombreCliente = nombreCliente;
        this.codigoVerificacion = codigoVerificacion;
        this.productos = productos;
        this.estado = EstadoPedido.PENDIENTE;
    }

    public Integer getId() {
        return id;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public String getCodigoVerificacion() {
        return codigoVerificacion;
    }

    public List<ProductoPedido> getProductos() {
        return productos;
    }

    public void enviar() {
        estado = EstadoPedido.ENVIADO;
    }

    public void recibir(String codigo) {
        if (estado != EstadoPedido.ENVIADO) {
            throw new IllegalStateException("El pedido debe estar enviado para marcarse como recibido");
        }
        if (!codigoVerificacion.equals(codigo)) {
            throw new IllegalArgumentException("Codigo de verificacion incorrecto");
        }
        estado = EstadoPedido.RECIBIDO;
    }
}
