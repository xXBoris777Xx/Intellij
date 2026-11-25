package boris.com.herramientasmatutuino.producto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombre;

    private String descripcion;

    @Column(nullable = false)
    private Integer stock;

    protected Producto() {
    }

    public Producto(String nombre, String descripcion, Integer stock) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.stock = stock;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Integer getStock() {
        return stock;
    }

    public void actualizar(String nombre, String descripcion, Integer stock) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.stock = stock;
    }

    public void descontarStock(int cantidad) {
        this.stock -= cantidad;
    }
}
