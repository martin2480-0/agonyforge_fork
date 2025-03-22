package com.agonyforge.mud.demo.model.repository;

import com.agonyforge.mud.demo.model.impl.BannedUser;
import com.agonyforge.mud.demo.model.impl.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BannedUsersRepository extends JpaRepository<BannedUser, Long> {

    Optional<BannedUser> findByBannedUser(User user);
    Optional<BannedUser> findByBannedUser_PrincipalName(String principal);
    List<BannedUser> findAllByOrderByBannedOnAsc();
}
