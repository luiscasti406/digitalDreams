package com.sena._2.digitalDream.modelos;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recibo_productos")
@Getter
@Setter
@NoArgsConstructor
public class ReciboProducto {

    @EmbeddedId
    private ReciboProductoId id;

    @ManyToOne
    @MapsId("idRecibo") // Mapea el campo idRecibo de ReciboProductoId
    @JoinColumn(name = "id_recibo", nullable = false)
    private Recibo recibo;

    @ManyToOne
    @MapsId("idProducto") // Mapea el campo idProducto de ReciboProductoId
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    public ReciboProducto(Recibo recibo, Producto producto, int cantidad) {
        this.id = new ReciboProductoId(recibo.getIdRecibo(), producto.getIdProducto());
        this.recibo = recibo;
        this.producto = producto;
        this.cantidad = cantidad;
    }
}

