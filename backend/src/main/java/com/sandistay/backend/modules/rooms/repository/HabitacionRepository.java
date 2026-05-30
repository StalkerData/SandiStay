package com.sandistay.backend.modules.rooms.repository;

import com.sandistay.backend.modules.rooms.model.Habitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HabitacionRepository extends JpaRepository<Habitacion, Integer> {
    // JpaRepository ya nos regala save(), findById(), y delete()
    // No necesitamos escribir SQL manual aquí.
}