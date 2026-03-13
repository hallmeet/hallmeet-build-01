package com.example.HAllTicket.service;

import java.util.List;

import com.example.HAllTicket.model.StudentModel;

public interface StudentService {
	StudentModel createStudent(StudentModel user);

	boolean checkEmail(String email);

	StudentModel getUserByEmail(String email);

	StudentModel updateStudent(StudentModel studentModel);

	StudentModel getStudentById(int id);

	List<StudentModel> listAll();

	void save(StudentModel std);

	void delete(int id);

	StudentModel get(int id);

	StudentModel get1(String email);

	/**
	 * Validates date of birth: must be in the past and student must be at least 10
	 * years old.
	 * 
	 * @param dob date string (yyyy-MM-dd)
	 * @return null if valid, error message if invalid
	 */
	String validateDateOfBirth(String dob);

}
