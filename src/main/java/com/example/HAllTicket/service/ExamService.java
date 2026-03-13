package com.example.HAllTicket.service;

import java.util.List;

import com.example.HAllTicket.model.ExamModel;


public interface ExamService {
	List<ExamModel> listAll();

    ExamModel get(int id);

    void save(ExamModel exam);

    void delete(int id);

	ExamModel get1(String examName);

	/**
	 * Validates exam subject dates: none may be in the past.
	 * @param exam exam with date1-date6
	 * @return null if valid, error message if any date is in the past
	 */
	String validateExamDates(ExamModel exam);
}
