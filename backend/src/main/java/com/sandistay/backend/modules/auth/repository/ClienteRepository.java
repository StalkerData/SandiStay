package com.sandistay.backend.modules.auth.repository;

import com.sandistay.backend.modules.auth.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByIdPersona(Integer idPersona);
}