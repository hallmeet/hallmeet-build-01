package com.example.HAllTicket.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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

import com.example.HAllTicket.converter.SubjectListConverter;
import com.example.HAllTicket.dto.SubjectDTO;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Where(clause = "deleted = false")
public class HallTicketModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String SeatNo;
    private String AdminName;
    private String instituteName;
    private String StudentName;
    private String ExamName;
    private String Status;
    private String email;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private StudentModel student;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id")
    private ExamModel exam;
    private String sub1;
    private String sub2;
    private String sub3;
    private String sub4;
    private String sub5;
    private String sub6;

    // JSON-based subjects list (supports up to 20 subjects)
    @Column(columnDefinition = "TEXT")
    @Convert(converter = SubjectListConverter.class)
    private List<SubjectDTO> subjects = new ArrayList<>();

    private String imageName;
    private String qrName;
    private boolean deleted = false;

    public List<SubjectDTO> getSubjects() {
        return subjects != null ? subjects : new ArrayList<>();
    }

    public void setSubjects(List<SubjectDTO> subjects) {
        this.subjects = subjects;
    }
    
    public String getQrName() {
    	return qrName;
    }
    public void setQrName(String qrName) {
    	this.qrName = qrName;
    }
    
    public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	public String getSeatNo() {
		return SeatNo;
	}
	public void setSeatNo(String SeatNo) {
		this.SeatNo=SeatNo;
	}
	
}
