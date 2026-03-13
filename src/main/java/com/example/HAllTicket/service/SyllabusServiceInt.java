package com.example.HAllTicket.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.HAllTicket.model.SyllabusModel;

import com.example.HAllTicket.repository.SyllabusRepository;

@Service
public class SyllabusServiceInt implements SyllabusService{
	@Autowired
    private SyllabusRepository syllabusRepo;

    @Override
    public List<SyllabusModel> listAll() {
       return syllabusRepo.findAll();
    }

    @Override
    public SyllabusModel get(int id) {
        return syllabusRepo.findById(id).orElse(null);
    }

    @Override
    public void save(SyllabusModel syllabus) {
        syllabusRepo.save(syllabus);
    }

    @Override
    public void delete(int id) {
        SyllabusModel existing = syllabusRepo.findById(id).orElse(null);
        if (existing == null) {
            return;
        }
        existing.setDeleted(true);
        syllabusRepo.save(existing);
    }

	@Override
	public SyllabusModel getUserByDetails(String Course, String Year, String Semister) {
		// TODO Auto-generated method stub
		return syllabusRepo.findByDetails(Course,Year,Semister);
	}

	

	
}
