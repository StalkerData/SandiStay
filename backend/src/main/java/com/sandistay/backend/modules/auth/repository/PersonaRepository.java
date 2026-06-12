package com.sandistay.backend.modules.auth.repository;

import com.sandistay.backend.modules.auth.model.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Integer> {
	// Busca si la persona ya existe por su documento
    java.util.Optional<Persona> findByNumeroDocumento(String numeroDocumento);
}