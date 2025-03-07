package com.sena._2.digitalDream.modelos;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.*;

@Embeddable
public class ReciboProductoId implements Serializable {

    @Column(name = "id_recibo")
    private int idRecibo;

    @Column(name = "id_producto")
    private int idProducto;

    public ReciboProductoId() {
    }

    public ReciboProductoId(int idRecibo, int idProducto) {
        this.idRecibo = idRecibo;
        this.idProducto = idProducto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReciboProductoId that = (ReciboProductoId) o;
        return idRecibo == that.idRecibo && idProducto == that.idProducto;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idRecibo, idProducto);
    }
}
