package com.redsolidaria.enjambre.repository;

import com.redsolidaria.enjambre.model.IncidenciaAyuda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncidenciaAyudaRepository extends JpaRepository<IncidenciaAyuda, Long> {
}
