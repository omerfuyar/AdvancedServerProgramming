package com.dsu.hello_server;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    // where username = ?  -> login, Basic and the owner of a new reservation
    Optional<AppUser> findByUsername(String username);

    // select count(...) > 0 ... where username = ?  -> 409 on sign-up
    boolean existsByUsername(String username);
}
