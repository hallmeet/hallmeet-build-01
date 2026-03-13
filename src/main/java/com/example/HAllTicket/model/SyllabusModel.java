package com.example.HAllTicket.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.Where;

import com.example.HAllTicket.converter.SubjectListConverter;
import com.example.HAllTicket.dto.SubjectDTO;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Where(clause = "deleted = false")
public class SyllabusModel {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
	private String Course;
	@Column(name = "academic_year") // avoid reserved word 'year' in H2/MySQL
	private String Year;
	private String Semister;
	private String Sub1;
	private String Sub2;
	private String Sub3;
	private String Sub4;
	private String Sub5;
	private String Sub6;

	// JSON-based subjects list (supports up to 20 subjects)
	@Column(columnDefinition = "TEXT")
	@Convert(converter = SubjectListConverter.class)
	private List<SubjectDTO> subjects = new ArrayList<>();

	private boolean deleted = false;

	public List<SubjectDTO> getSubjects() {
		return subjects != null ? subjects : new ArrayList<>();
	}

	public void setSubjects(List<SubjectDTO> subjects) {
		this.subjects = subjects;
		if (subjects != null) {
			this.Sub1 = subjects.size() >= 1 ? subjects.get(0).getName() : "";
			this.Sub2 = subjects.size() >= 2 ? subjects.get(1).getName() : "";
			this.Sub3 = subjects.size() >= 3 ? subjects.get(2).getName() : "";
			this.Sub4 = subjects.size() >= 4 ? subjects.get(3).getName() : "";
			this.Sub5 = subjects.size() >= 5 ? subjects.get(4).getName() : "";
			this.Sub6 = subjects.size() >= 6 ? subjects.get(5).getName() : "";
		}
	}
}
