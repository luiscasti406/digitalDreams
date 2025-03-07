package com.sena._2.digitalDream.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UsuarioDTO {

    @NotBlank(message = "El primer nombre es obligatorio")
    @Size(max = 50, message = "El primer nombre no puede exceder los 50 caracteres")
    private String prim_nombre;

    @Size(max = 50, message = "El segundo nombre no puede exceder los 50 caracteres")
    private String seg_nombre;

    @NotBlank(message = "El primer apellido es obligatorio")
    @Size(max = 30, message = "El primer apellido no puede exceder los 30 caracteres")
    private String prim_apellido;

    @Size(max = 30, message = "El segundo apellido no puede exceder los 30 caracteres")
    private String seg_apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ingresar un email válido")
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @NotNull(message = "El rol es obligatorio")
    private Integer id_rol;

    private MultipartFile fotoUsuario;
}
