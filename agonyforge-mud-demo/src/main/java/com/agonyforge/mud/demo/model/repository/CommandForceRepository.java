package com.agonyforge.mud.demo.model.repository;

import com.agonyforge.mud.demo.model.impl.CommandForce;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommandForceRepository extends JpaRepository<CommandForce, Boolean> {

    Optional<CommandForce> findByCommand_NameIgnoreCase(String name);
    Optional<CommandForce> findFirstByCommand_NameStartingWith(String name, Sort sort);
}
