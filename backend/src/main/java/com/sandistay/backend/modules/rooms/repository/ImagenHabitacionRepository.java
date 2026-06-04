package com.sandistay.backend.modules.rooms.repository;

import com.sandistay.backend.modules.rooms.model.ImagenHabitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImagenHabitacionRepository extends JpaRepository<ImagenHabitacion, Integer> {
    java.util.List<ImagenHabitacion> findByIdHabitacion(Integer idHabitacion);
}