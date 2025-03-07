package com.sena._2.digitalDream.repository;

import com.sena._2.digitalDream.modelos.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    @Query("SELECT p FROM Pedido p JOIN FETCH p.marca JOIN FETCH p.usuario")
    List<Pedido> findAllWithDetails();
}