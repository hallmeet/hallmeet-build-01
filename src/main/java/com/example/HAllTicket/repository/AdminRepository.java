package com.example.HAllTicket.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.HAllTicket.model.AdminModel;

public interface AdminRepository extends JpaRepository<AdminModel, Integer>{
	
	AdminModel findTop1ByEmail(String email);

	boolean existsByEmail(String email);

	boolean existsByPassword(String password);
}
