package com.registration.repository;

import com.registration.entity.JwtToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface JwtTokenRepository extends JpaRepository<JwtToken, Integer> {
    Optional<JwtToken> findByToken(String token);
    List<JwtToken> findByUserId(Integer userId);

    @Transactional
    void deleteByUserId(Integer userId);

    @Transactional
    void deleteByToken(String token);
}
