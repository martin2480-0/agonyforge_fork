package com.agonyforge.mud.demo.model.repository;

import com.agonyforge.mud.demo.model.impl.ReloadedUser;
import com.agonyforge.mud.demo.model.impl.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReloadedUsersRepository extends JpaRepository<ReloadedUser, Long> {

    Optional<ReloadedUser> findByReloadedUser(User user);
    Optional<ReloadedUser> findByReloadedUser_PrincipalName(String principal);

}
