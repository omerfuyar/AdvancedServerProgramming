package com.dsu.hello_server;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// One row per refresh token handed out by login. Only the SHA-256 of the token is stored,
// so whoever reads the table cannot use the tokens in it.
@Entity
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    protected RefreshToken() {
    } // for Hibernate

    public RefreshToken(String tokenHash, AppUser user, LocalDateTime expiresAt) {
        this.tokenHash = tokenHash;
        this.user = user;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return !LocalDateTime.now().isBefore(expiresAt);
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }
}
