package com.example.HAllTicket.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.HAllTicket.model.CollegeModel;
import com.example.HAllTicket.repository.CollegeRepository;

@Service
public class CollegeServiceInt implements CollegeService {
    @Autowired
    private CollegeRepository collegeRepo;

    @Override
    public List<CollegeModel> listAll() {
        return collegeRepo.findAll();
    }

    @Override
    public CollegeModel get(int id) {
        return collegeRepo.findById(id).orElse(null);
    }

    @Override
    public void save(CollegeModel college) {
        collegeRepo.save(college);
    }

    @Override
    public void delete(int id) {
        CollegeModel existing = collegeRepo.findById(id).orElse(null);
        if (existing == null) {
            return;
        }
        existing.setDeleted(true);
        collegeRepo.save(existing);
    }

    @Override
    public CollegeModel getByInstituteName(String instituteName) {
        if (instituteName == null || instituteName.isBlank()) return null;
        return collegeRepo.findTop1ByInstituteName(instituteName.trim());
    }
}
