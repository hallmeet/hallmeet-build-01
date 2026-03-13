package com.example.HAllTicket.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.HAllTicket.model.ExamModel;
import com.example.HAllTicket.model.StudentModel;
import com.example.HAllTicket.model.SyllabusModel;
import com.example.HAllTicket.repository.ExamRepository;
import com.example.HAllTicket.repository.SyllabusRepository;
@Service
public class ExamServiceInt implements ExamService{
	@Autowired
    private ExamRepository examRepo;

    @Override
    public List<ExamModel> listAll() {
       return examRepo.findAll();
    }

    @Override
    public ExamModel get(int id) {
        return examRepo.findById(id).orElse(null);
    }

    @Override
    public void save(ExamModel exam) {
        examRepo.save(exam);
    }

    @Override
    public void delete(int id) {
        ExamModel existing = examRepo.findById(id).orElse(null);
        if (existing == null) {
            return;
        }
        existing.setDeleted(true);
        examRepo.save(existing);
    }

	@Override
	public ExamModel get1(String examName) {
		if (examName == null || examName.trim().isEmpty()) return null;
        String trimmed = examName.trim();
        
        // 1. Try exact match
		ExamModel exam = examRepo.findTop1ByExamName(trimmed);
        if (exam != null) return exam;
        
        // 2. Try case-insensitive match
        exam = examRepo.findTop1ByExamNameIgnoreCase(trimmed);
        if (exam != null) return exam;
        
        // 3. Robust fallback: iterate all exams and compare stripped names
        List<ExamModel> all = examRepo.findAll();
        String strippedInput = trimmed.replaceAll("\\s+", "").toLowerCase();
        for (ExamModel e : all) {
            if (e.getExamName() != null) {
                String strippedDb = e.getExamName().replaceAll("\\s+", "").toLowerCase();
                if (strippedDb.equals(strippedInput)) {
                    return e;
                }
            }
        }
        
        return null;
	}

	@Override
	public String validateExamDates(ExamModel exam) {
		if (exam == null) return null;
		LocalDate today = LocalDate.now();
		DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
		String[] dates = { exam.getDate1(), exam.getDate2(), exam.getDate3(), exam.getDate4(), exam.getDate5(), exam.getDate6() };
		for (int i = 0; i < dates.length; i++) {
			String d = dates[i];
			if (d == null || d.isBlank()) continue;
			try {
				LocalDate ld = LocalDate.parse(d.trim(), fmt);
				if (ld.isBefore(today)) {
					return "Exam dates cannot be in the past. Please fix subject " + (i + 1) + " date.";
				}
			} catch (DateTimeParseException ignored) {
				// skip invalid formats; other validation can handle
			}
		}
		return null;
	}
}
