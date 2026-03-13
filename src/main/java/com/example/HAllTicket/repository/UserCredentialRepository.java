package com.example.HAllTicket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.HAllTicket.model.UserCredential;

@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential, Integer> {
    boolean existsByEmail(String email);
    boolean existsByPassword(String password);
    UserCredential findTop1ByEmail(String email);
}
