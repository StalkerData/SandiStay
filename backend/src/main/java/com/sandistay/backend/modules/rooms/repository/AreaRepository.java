package com.sandistay.backend.modules.rooms.repository;

import com.sandistay.backend.modules.rooms.model.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AreaRepository extends JpaRepository<Area, Integer> {}