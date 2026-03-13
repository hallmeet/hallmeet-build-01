package com.example.HAllTicket.service;

import java.util.List;


import com.example.HAllTicket.model.SyllabusModel;

public interface SyllabusService {
	List<SyllabusModel> listAll();

    SyllabusModel get(int id);

    void save(SyllabusModel syllabus);

    void delete(int id);

	SyllabusModel getUserByDetails(String Course, String Year, String Semister);

	
	
}
