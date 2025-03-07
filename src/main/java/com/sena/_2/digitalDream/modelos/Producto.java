package com.sena._2.digitalDream.modelos;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private int idProducto;

    @Column(name = "nom_producto", nullable = false, length = 255)
    private String nomProducto;

    @ManyToOne
    @JoinColumn(name = "id_marca")
    private Marca marca;

    @Column(nullable = false)
    private Integer precio;

    @Column(nullable = false)
    private Short cantidad;

    @Column(nullable = false, length = 10)
    private String estado;

    @Column(nullable = false, length = 250)
    private String detalles;

    @Column(name = "foto_producto", length = 255)
    private String fotoProducto;
}
