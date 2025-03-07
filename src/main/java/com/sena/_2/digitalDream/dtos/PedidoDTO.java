package com.sena._2.digitalDream.dtos;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
public class PedidoDTO {

    private Integer idMarca;

    @NotBlank(message = "El nombre del pedido es obligatorio")
    @Size(max = 20, message = "El nombre del pedido no puede exceder los 20 caracteres")
    private String nomPedido;

    @NotNull(message = "La fecha de envío es obligatoria")
    @FutureOrPresent(message = "La fecha de envío debe ser hoy o en el futuro")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date fechaEnvio;

    @NotNull(message = "La fecha de llegada es obligatoria")
    @FutureOrPresent(message = "La fecha de llegada debe ser hoy o en el futuro")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date fechaLlegada;

}

