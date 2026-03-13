package com.example.HAllTicket.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.HAllTicket.model.ExamModel;
import com.example.HAllTicket.model.StudentModel;
@Repository
public interface ExamRepository extends JpaRepository<ExamModel, Integer>{

	ExamModel findTop1ByExamName(String examName);
	ExamModel findTop1ByExamNameIgnoreCase(String examName);
	
}
