package com.sena._2.digitalDream.repository;

import com.sena._2.digitalDream.modelos.ReciboProducto;
import com.sena._2.digitalDream.modelos.ReciboProductoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReciboProductoRepository extends JpaRepository<ReciboProducto, ReciboProductoId> {
    List<ReciboProducto> findByReciboIdRecibo(Integer idRecibo);

    @Modifying
    @Query("DELETE FROM ReciboProducto rp WHERE rp.recibo.idRecibo = :idRecibo")
    void deleteByReciboIdRecibo(@Param("idRecibo") int idRecibo);
}