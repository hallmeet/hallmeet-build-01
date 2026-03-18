package com.example.HAllTicket.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.HAllTicket.model.ExamModel;
import com.example.HAllTicket.model.HallTicketModel;
import com.example.HAllTicket.model.StudentModel;
import com.example.HAllTicket.model.UserCredential;
import com.example.HAllTicket.service.ExamService;
import com.example.HAllTicket.service.HallTicketService;
import com.example.HAllTicket.service.StudentService;
import com.example.HAllTicket.service.UserCredentialService;

import jakarta.servlet.http.HttpSession;

/**
 * REST API for admin dashboard stats and utilities.
 */
@RestController
@RequestMapping("/api")
public class DashboardApiController {

    @Autowired private StudentService studentService;
    @Autowired private ExamService examService;
    @Autowired private HallTicketService hallTicketService;
    @Autowired private UserCredentialService userService;

    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Long>> getStats(HttpSession session) {
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        String role = (String) session.getAttribute("role");
        if (loggedIn == null || !loggedIn || role == null || !role.equalsIgnoreCase("admin")) {
            return ResponseEntity.status(401).build();
        }
        try {
            List<StudentModel> students       = studentService.listAll();
            List<ExamModel> exams             = examService.listAll();
            List<HallTicketModel> hallTickets = hallTicketService.listAll();

            long totalStudents      = students    != null ? students.size()    : 0;
            long totalExams         = exams       != null ? exams.size()       : 0;
            long totalHallTickets   = hallTickets != null ? hallTickets.size() : 0;
            long pendingHallTickets = 0;
            if (hallTickets != null) {
                pendingHallTickets = hallTickets.stream()
                    .filter(ht -> ht != null && "Pending".equals(ht.getStatus()))
                    .count();
            }
            Map<String, Long> stats = new HashMap<>();
            stats.put("totalStudents",      totalStudents);
            stats.put("totalExams",         totalExams);
            stats.put("totalHallTickets",   totalHallTickets);
            stats.put("pendingHallTickets", pendingHallTickets);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Returns the stored (BCrypt hashed) password for a student email.
     * Admin-only — used by the Change Password modal in StudentList.
     */
    @GetMapping("/student-password")
    public ResponseEntity<Map<String, String>> getStudentHashedPassword(
            @RequestParam("email") String email, HttpSession session) {

        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        String role = (String) session.getAttribute("role");
        if (loggedIn == null || !loggedIn || role == null || !role.equalsIgnoreCase("admin")) {
            return ResponseEntity.status(401).build();
        }
        try {
            UserCredential cred = userService.getUserByEmail(email.toLowerCase().trim());
            Map<String, String> resp = new HashMap<>();
            if (cred != null) {
                resp.put("hashedPassword", cred.getPassword());
                resp.put("email", cred.getEmail());
            } else {
                resp.put("hashedPassword", "(no account found)");
            }
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
