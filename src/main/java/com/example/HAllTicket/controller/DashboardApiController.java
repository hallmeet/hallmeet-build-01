package com.example.HAllTicket.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.HAllTicket.model.ExamModel;
import com.example.HAllTicket.model.HallTicketModel;
import com.example.HAllTicket.model.StudentModel;
import com.example.HAllTicket.service.ExamService;
import com.example.HAllTicket.service.HallTicketService;
import com.example.HAllTicket.service.StudentService;

import jakarta.servlet.http.HttpSession;

/**
 * REST API for admin dashboard stats (Requirement #7 - auto-refresh).
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

    @Autowired
    private StudentService studentService;
    @Autowired
    private ExamService examService;
    @Autowired
    private HallTicketService hallTicketService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats(HttpSession session) {
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        String role = (String) session.getAttribute("role");
        if (loggedIn == null || !loggedIn || role == null || !role.equalsIgnoreCase("admin")) {
            return ResponseEntity.status(401).build();
        }
        try {
            List<StudentModel> students = studentService.listAll();
            List<ExamModel> exams = examService.listAll();
            List<HallTicketModel> hallTickets = hallTicketService.listAll();
            long totalStudents = (students != null) ? students.size() : 0;
            long totalExams = (exams != null) ? exams.size() : 0;
            long totalHallTickets = (hallTickets != null) ? hallTickets.size() : 0;
            long pendingHallTickets = 0;
            if (hallTickets != null) {
                pendingHallTickets = hallTickets.stream()
                    .filter(ht -> ht != null && "Pending".equals(ht.getStatus()))
                    .count();
            }
            Map<String, Long> stats = new HashMap<>();
            stats.put("totalStudents", totalStudents);
            stats.put("totalExams", totalExams);
            stats.put("totalHallTickets", totalHallTickets);
            stats.put("pendingHallTickets", pendingHallTickets);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
