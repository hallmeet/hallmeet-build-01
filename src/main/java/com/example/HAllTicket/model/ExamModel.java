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
public class ExamModel {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
	//@Column(name = "exam_name")
	private String examName;
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
	private String date1;
	private String date2;
	private String date3;
	private String date4;
	private String date5;
	private String date6;
	private String time1;
	private String time2;
	private String time3;
	private String time4;
	private String time5;
	private String time6;

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

			this.date1 = subjects.size() >= 1 ? subjects.get(1 - 1).getDate() : "";
			this.date2 = subjects.size() >= 2 ? subjects.get(2 - 1).getDate() : "";
			this.date3 = subjects.size() >= 3 ? subjects.get(3 - 1).getDate() : "";
			this.date4 = subjects.size() >= 4 ? subjects.get(4 - 1).getDate() : "";
			this.date5 = subjects.size() >= 5 ? subjects.get(5 - 1).getDate() : "";
			this.date6 = subjects.size() >= 6 ? subjects.get(6 - 1).getDate() : "";

			this.time1 = subjects.size() >= 1 ? subjects.get(1 - 1).getTime() : "";
			this.time2 = subjects.size() >= 2 ? subjects.get(2 - 1).getTime() : "";
			this.time3 = subjects.size() >= 3 ? subjects.get(3 - 1).getTime() : "";
			this.time4 = subjects.size() >= 4 ? subjects.get(4 - 1).getTime() : "";
			this.time5 = subjects.size() >= 5 ? subjects.get(5 - 1).getTime() : "";
			this.time6 = subjects.size() >= 6 ? subjects.get(6 - 1).getTime() : "";
		}
	}
}
