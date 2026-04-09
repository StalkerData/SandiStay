package com.sandistay.backend.modules.auth.repository;

import com.sandistay.backend.modules.auth.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

	/**
	 * Busca un usuario por su nombre de usuario. Retorna un Optional para evitar
	 * NullPointerExceptions si el usuario no existe.
	 * 
	 * @param username El nombre de usuario a buscar.
	 * @return Un Optional que contiene el Usuario si se encuentra.
	 */
	Optional<Usuario> findByUsername(String username);

}