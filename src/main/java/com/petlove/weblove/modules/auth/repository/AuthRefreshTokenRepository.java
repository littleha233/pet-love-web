package com.petlove.weblove.modules.auth.repository;

import com.petlove.weblove.modules.auth.entity.AuthRefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthRefreshTokenRepository extends JpaRepository<AuthRefreshToken, Long> {

    Optional<AuthRefreshToken> findByTokenHash(String tokenHash);
}
