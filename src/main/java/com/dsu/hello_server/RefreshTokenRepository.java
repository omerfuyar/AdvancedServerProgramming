package com.dsu.hello_server;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // join fetch: the user is needed right away to sign the new access token
    @Query("""
            select t from RefreshToken t
            join fetch t.user
            where t.tokenHash = :tokenHash
            """)
    Optional<RefreshToken> findByTokenHash(@Param("tokenHash") String tokenHash);
}
