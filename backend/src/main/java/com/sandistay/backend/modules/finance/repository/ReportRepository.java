package com.sandistay.backend.modules.finance.repository;

import com.sandistay.backend.modules.bookings.model.Reserva; 
import com.sandistay.backend.modules.finance.model.OcupacionProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

@org.springframework.stereotype.Repository
public interface ReportRepository extends Repository<Reserva, Integer> { 

    @Query(value = "SELECT * FROM fn_reporte_ocupacion(CAST(:fechaInicio AS DATE), CAST(:fechaFin AS DATE))", nativeQuery = true)
    List<OcupacionProjection> getReporteOcupacion(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );
}