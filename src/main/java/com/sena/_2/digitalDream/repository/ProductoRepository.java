package com.sena._2.digitalDream.repository;

import com.sena._2.digitalDream.modelos.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    @Query("SELECT p FROM Producto p JOIN FETCH p.marca")
    List<Producto> obtenerProductosConMarcaYFoto();
}
