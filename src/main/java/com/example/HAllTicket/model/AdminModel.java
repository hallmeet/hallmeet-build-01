package com.example.HAllTicket.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.Where;

@Data
@Entity
@Where(clause = "deleted = false")
public class AdminModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String fullName;
	
	private String email;

	private String password;

	private String mobileNo;
	
	private String role;

	private boolean deleted = false;
}
