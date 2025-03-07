package com.sena._2.digitalDream.repository;

import com.sena._2.digitalDream.modelos.Recibo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReciboRepository extends JpaRepository<Recibo, Integer> {
    List<Recibo> findByUsuarioIdUsuario(Integer idUsuario);
}
