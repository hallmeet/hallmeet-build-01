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
public class CollegeModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String instituteName;
	private String Address;

	private boolean deleted = false;
	
	
	
   
    
	
	
	

	
	
}
