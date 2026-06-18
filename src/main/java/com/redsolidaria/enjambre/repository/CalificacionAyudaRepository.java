package com.redsolidaria.enjambre.repository;

import com.redsolidaria.enjambre.model.CalificacionAyuda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CalificacionAyudaRepository extends JpaRepository<CalificacionAyuda, Long> {
    Optional<CalificacionAyuda> findByHistorialAyuda_Id(Long historialAyudaId);
}
