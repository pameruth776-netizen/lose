package com.redsolidaria.enjambre.repository;

import com.redsolidaria.enjambre.model.ComentarioAyuda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ComentarioAyudaRepository extends JpaRepository<ComentarioAyuda, Long> {
    List<ComentarioAyuda> findByHistorialAyuda_Id(Long historialAyudaId);
}
