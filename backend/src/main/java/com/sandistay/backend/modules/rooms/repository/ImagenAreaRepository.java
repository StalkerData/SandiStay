package com.sandistay.backend.modules.rooms.repository;

import com.sandistay.backend.modules.rooms.model.ImagenArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImagenAreaRepository extends JpaRepository<ImagenArea, Integer> {}