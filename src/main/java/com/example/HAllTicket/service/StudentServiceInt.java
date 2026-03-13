package com.example.HAllTicket.service;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.HAllTicket.model.StudentModel;
import com.example.HAllTicket.repository.StudentRepository;

@Service
public class StudentServiceInt implements StudentService {
    @Autowired
    private StudentRepository studRepo;

    @Override
    public StudentModel createStudent(StudentModel user) {
        return studRepo.save(user);
    }

    @Override
    public boolean checkEmail(String email) {
        return studRepo.existsByEmail(email);
    }

    @Override
    public StudentModel getUserByEmail(String email) {
        return studRepo.findTop1ByEmail(email);
    }
    

    @Override
    public StudentModel updateStudent(StudentModel studentModel) {
        return studRepo.save(studentModel);
    }

    @Override
    public StudentModel getStudentById(int id) {
        return studRepo.findById(id).orElse(null);
    }
    @Override
    public List<StudentModel> listAll() {
        return studRepo.findAll();
    }
    
    public void save(StudentModel std) {
    	studRepo.save(std);
    }
    
    public StudentModel  get(int id) {
        return studRepo.findById(id).get();
    }
    
    public void delete(int id) {
    	StudentModel existing = studRepo.findById(id).orElse(null);
    	if (existing == null) {
    		return;
    	}
    	existing.setDeleted(true);
    	studRepo.save(existing);
    }

	@Override
	public StudentModel get1(String email) {
		return studRepo.findTop1ByEmail(email);
	}

	@Override
	public String validateDateOfBirth(String dob) {
		if (dob == null || dob.isBlank()) {
			return "Date of birth is required.";
		}
		LocalDate dateOfBirth;
		try {
			dateOfBirth = LocalDate.parse(dob.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
		} catch (DateTimeParseException e) {
			return "Invalid date format. Use yyyy-MM-dd.";
		}
		LocalDate today = LocalDate.now();
		if (dateOfBirth.isAfter(today)) {
			return "Date of birth cannot be in the future.";
		}
		int age = Period.between(dateOfBirth, today).getYears();
		if (age < 10) {
			return "Student must be at least 10 years old.";
		}
		return null;
	}
}

