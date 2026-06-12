package com.sandistay.backend.modules.auth.repository;

import com.sandistay.backend.modules.auth.model.UsuarioView;
import org.springframework.data.repository.Repository;
import java.util.List;

@org.springframework.stereotype.Repository
public interface UsuarioViewRepository extends Repository<UsuarioView, Integer> {
    List<UsuarioView> findAll();
}