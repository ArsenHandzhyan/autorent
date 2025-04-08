package ru.anapa.autorent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.anapa.autorent.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}