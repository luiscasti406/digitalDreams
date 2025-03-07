package com.sena._2.digitalDream.modelos;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private int idUsuario;

    @Column(name = "prim_nombre", nullable = false, length = 50)
    private String Prim_nombre;

    @Column(name = "seg_nombre", length = 50)
    private String Seg_nombre;

    @Column(name = "prim_apellido", nullable = false, length = 30)
    private String Prim_apellido;

    @Column(name = "seg_apellido", length = 30)
    private String Seg_apellido;

    @Column(nullable = false, length = 250)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @ManyToOne
    @JoinColumn(name = "id_rol")
    private Rol rol;

    @Column(name = "foto_usuario", length = 255,nullable = true)
    private String fotoUsuario;

    @Column(name = "token_recuperacion", length = 100)
    private String tokenRecuperacion;
}
