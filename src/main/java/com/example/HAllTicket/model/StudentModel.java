package com.example.HAllTicket.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Where;

@Data
@Entity
@Where(clause = "deleted = false")
public class StudentModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String fullName;

    private String email;

    private String mobileNo;

    public void setEmail(String email) {
        if (email != null) {
            this.email = email.toLowerCase().trim();
        } else {
            this.email = null;
        }
    }

    private String DateOfBirth;

    private String instituteName;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "college_id")
    private CollegeModel college;

    private String Course;

    private String AdmissionForYear;

    private String Semister;
    
    private String imageName;

    private boolean deleted = false;
    public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
    
}
