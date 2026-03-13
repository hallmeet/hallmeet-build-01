package com.example.HAllTicket.service;

import java.util.List;

import com.example.HAllTicket.model.CollegeModel;

public interface CollegeService {
    List<CollegeModel> listAll();

    CollegeModel get(int id);

    void save(CollegeModel college);

    void delete(int id);

    CollegeModel getByInstituteName(String instituteName);
}
