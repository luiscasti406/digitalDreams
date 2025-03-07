package com.sena._2.digitalDream.dtos;

import com.sena._2.digitalDream.modelos.Producto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductoDTO {

    private Integer idProducto;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 255, message = "El nombre del producto no puede exceder los 255 caracteres")
    private String nomProducto;

    private Integer idMarca;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 1, message = "El precio debe ser mayor a 0")
    private Integer precio;

    private String marca;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    private Short cantidad;

    @NotBlank(message = "El estado es obligatorio")
    @Size(max = 10, message = "El estado no puede exceder los 10 caracteres")
    private String estado;

    @NotBlank(message = "Los detalles son obligatorios")
    @Size(max = 250, message = "Los detalles no pueden exceder los 250 caracteres")
    private String detalles;

    private MultipartFile fotoProducto;

    private String fotoUrl;

    public ProductoDTO() {}

    // Constructor con parámetros
    public ProductoDTO(String nomProducto, Integer idMarca, Integer precio, Short cantidad, String estado, String detalles, MultipartFile fotoProducto) {
        this.nomProducto = nomProducto;
        this.idMarca = idMarca;
        this.precio = precio;
        this.cantidad = cantidad;
        this.estado = estado;
        this.detalles = detalles;
        this.fotoProducto = fotoProducto;
    }

    // Constructor para obtener productos desde la BD (usa String)
    public ProductoDTO(Integer idProducto,String nomProducto, int idMarca, String marca, int precio, short cantidad, String estado, String detalles, String fotoUrl) {
        this.idProducto = idProducto;
        this.nomProducto = nomProducto;
        this.idMarca = idMarca;
        this.marca = marca;
        this.precio = precio;
        this.cantidad = cantidad;
        this.estado = estado;
        this.detalles = detalles;
        this.fotoUrl = fotoUrl;
    }

    // Constructor para convertir desde la entidad Producto a ProductoDTO
    public ProductoDTO(Producto producto) {
        this.idProducto = producto.getIdProducto();
        this.nomProducto = producto.getNomProducto();
        this.idMarca = producto.getMarca().getIdMarca();
        this.marca = producto.getMarca().getMarca();
        this.precio = producto.getPrecio();
        this.cantidad = producto.getCantidad();
        this.estado = producto.getEstado();
        this.detalles = producto.getDetalles();
        this.fotoUrl = producto.getFotoProducto();
    }
}


