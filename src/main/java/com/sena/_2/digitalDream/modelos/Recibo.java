package com.sena._2.digitalDream.modelos;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recibo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Recibo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recibo")
    private int idRecibo;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "valor_total", nullable = false)
    private int valorTotal;

    @Column(name = "tipo_pago", nullable = false)
    private int tipoPago;

    @Column(name = "numero_tarjeta", length = 20)
    private String numeroTarjeta;

    @Column(name = "fecha_compra", nullable = false)
    private LocalDateTime fechaCompra = LocalDateTime.now();
}
