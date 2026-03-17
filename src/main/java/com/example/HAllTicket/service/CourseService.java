package com.example.HAllTicket.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.HAllTicket.model.CourseModel;
import com.example.HAllTicket.repository.CourseRepository;

@Service
public class CourseService {
    
    @Autowired
    private CourseRepository repo;

    public List<CourseModel> listAll() {
        return repo.findAll();
    }

    public void save(CourseModel course) {
        repo.save(course);
    }

    public CourseModel get(int id) {
        return repo.findById(id).get();
    }

    public void delete(int id) {
        repo.deleteById(id);
    }
}
