package com.example.HAllTicket.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.HAllTicket.model.AdminModel;
import com.example.HAllTicket.config.WebConfig;
import com.example.HAllTicket.model.CollegeModel;
import com.example.HAllTicket.model.ExamModel;
import com.example.HAllTicket.model.HallTicketModel;
import com.example.HAllTicket.model.StudentModel;
import com.example.HAllTicket.model.SyllabusModel;
import com.example.HAllTicket.model.UserCredential;
import com.example.HAllTicket.repository.StudentRepository;
import com.example.HAllTicket.repository.UserCredentialRepository;

import com.example.HAllTicket.service.AdminService;
import com.example.HAllTicket.service.CollegeService;
import com.example.HAllTicket.service.ExamService;
import com.example.HAllTicket.service.HallTicketService;
import com.example.HAllTicket.service.StudentService;
import com.example.HAllTicket.service.PdfGeneratorService;
import com.example.HAllTicket.service.CourseService;
import com.example.HAllTicket.service.SyllabusService;
import com.example.HAllTicket.service.UserCredentialService;
import com.example.HAllTicket.util.FormTokenUtil;
import com.example.HAllTicket.util.PasswordUtil;
import com.example.HAllTicket.dto.SubjectDTO;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class HallController {
    private static final Logger logger = LoggerFactory.getLogger(HallController.class);

    @Autowired
    private UserCredentialService userService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private AdminService adminService;
    @Autowired
    private UserCredentialRepository userCredentialRepo;
    @Autowired
    private StudentRepository studentRepo;
    @Autowired
    private CollegeService collegeService;
    @Autowired
    private SyllabusService syllabusService;
    @Autowired
    private ExamService examService;
    @Autowired
    private HallTicketService hallTicketService;
    @Autowired
    private PdfGeneratorService pdfGeneratorService;
    @Autowired
    private CourseService courseService;

    @ModelAttribute("courseList")
    public List<com.example.HAllTicket.model.CourseModel> getCourseList() {
        return courseService.listAll();
    }

    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        model.addAttribute("formToken", FormTokenUtil.generateToken(session));
        return "index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("pageTitle", "About HallMeet");
        return "About";
    }

    @GetMapping("/UserHomePage")
    public String UserHomePage(@RequestParam(value = "username", required = false) String username,
            Model model, HttpSession session) {
        model.addAttribute("formToken", FormTokenUtil.generateToken(session));
        if (username != null && !username.isEmpty()) {
            session.setAttribute("username", username);
            session.setAttribute("loggedIn", true);
            StudentModel user = studentService.getUserByEmail(username);
            if (user != null) {
                model.addAttribute("user", user);

                // Get all hall tickets for this student (show all, not just approved)
                List<HallTicketModel> hallTickets = hallTicketService.listAll();
                List<HallTicketModel> studentHallTickets = hallTickets.stream()
                        .filter(ht -> ht.getEmail() != null && ht.getEmail().equals(username))
                        // Sort: Approved first, then by QR code presence, then by ID
                        .sorted((ht1, ht2) -> {
                            if (ht1.getStatus() != null && ht1.getStatus().equals("Approved") &&
                                    !(ht2.getStatus() != null && ht2.getStatus().equals("Approved"))) {
                                return -1;
                            }
                            if (ht2.getStatus() != null && ht2.getStatus().equals("Approved") &&
                                    !(ht1.getStatus() != null && ht1.getStatus().equals("Approved"))) {
                                return 1;
                            }
                            return Integer.compare(ht2.getId(), ht1.getId());
                        })
                        .collect(Collectors.toList());
                model.addAttribute("hall", studentHallTickets);
                List<HallTicketModel> approvedHall = studentHallTickets.stream()
                        .filter(ht -> "Approved".equals(ht.getStatus())).collect(Collectors.toList());
                List<HallTicketModel> pendingHall = studentHallTickets.stream()
                        .filter(ht -> !"Approved".equals(ht.getStatus())).collect(Collectors.toList());
                model.addAttribute("approvedHall", approvedHall);
                model.addAttribute("pendingHall", pendingHall);
                Set<String> appliedExamNames = studentHallTickets.stream()
                        .map(HallTicketModel::getExamName)
                        .filter(n -> n != null && !n.isEmpty())
                        .collect(Collectors.toSet());
                model.addAttribute("appliedExamNames", appliedExamNames);
                List<ExamModel> listexam = examService.listAll();
                model.addAttribute("listexam", listexam);
                model.addAttribute("pageTitle", "Dashboard");
            }
        }
        return "UserHomePage";
    }

    @GetMapping("/getHall")
    public String getHall(@RequestParam("username") String username, Model model, HttpSession session) {
        model.addAttribute("formToken", FormTokenUtil.generateToken(session));
        StudentModel user = studentService.getUserByEmail(username);
        if (user != null) {
            model.addAttribute("user", user);

            // Get all hall tickets for this student (show all, not just approved)
            List<HallTicketModel> hallTickets = hallTicketService.listAll();
            List<HallTicketModel> studentHallTickets = hallTickets.stream()
                    .filter(ht -> ht.getEmail() != null && ht.getEmail().equals(username))
                    // Sort: Approved first, then by QR code presence, then by ID
                    .sorted((ht1, ht2) -> {
                        if (ht1.getStatus() != null && ht1.getStatus().equals("Approved") &&
                                !(ht2.getStatus() != null && ht2.getStatus().equals("Approved"))) {
                            return -1;
                        }
                        if (ht2.getStatus() != null && ht2.getStatus().equals("Approved") &&
                                !(ht1.getStatus() != null && ht1.getStatus().equals("Approved"))) {
                            return 1;
                        }
                        return Integer.compare(ht2.getId(), ht1.getId());
                    })
                    .collect(Collectors.toList());
            model.addAttribute("hall", studentHallTickets);
            List<HallTicketModel> approvedHall = studentHallTickets.stream()
                    .filter(ht -> "Approved".equals(ht.getStatus())).collect(Collectors.toList());
            List<HallTicketModel> pendingHall = studentHallTickets.stream()
                    .filter(ht -> !"Approved".equals(ht.getStatus())).collect(Collectors.toList());
            model.addAttribute("approvedHall", approvedHall);
            model.addAttribute("pendingHall", pendingHall);
            Set<String> appliedExamNames = studentHallTickets.stream()
                    .map(HallTicketModel::getExamName)
                    .filter(n -> n != null && !n.isEmpty())
                    .collect(Collectors.toSet());
            model.addAttribute("appliedExamNames", appliedExamNames);
            List<ExamModel> listexam = examService.listAll();
            model.addAttribute("listexam", listexam);
            model.addAttribute("pageTitle", "Dashboard");
        }
        return "UserHomePage";
    }

    @GetMapping("/StudentList_form")
    public String viewHomePage(Model model) {
        List<StudentModel> liststudent = studentService.listAll();
        model.addAttribute("liststudent", liststudent);
        System.out.print("Get / ");
        return "StudentList";
    }

    @GetMapping("/new")
    public String add(Model model, HttpSession session) {
        model.addAttribute("student", new StudentModel());
        model.addAttribute("formToken", FormTokenUtil.generateToken(session));
        return "EditStudent";
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveStudent(@ModelAttribute("student") StudentModel std,
            @RequestParam(value = "formToken", required = false) String formToken,
            HttpSession session, RedirectAttributes redirectAttributes) {
        if (!FormTokenUtil.validateAndInvalidate(session, formToken)) {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired form. Please try again.");
            return "redirect:/StudentList_form";
        }
        String dobError = studentService.validateDateOfBirth(std.getDateOfBirth());
        if (dobError != null) {
            redirectAttributes.addFlashAttribute("error", dobError);
            if (std.getId() > 0) {
                return "redirect:/edit/" + std.getId();
            }
            return "redirect:/new";
        }
        if (std.getInstituteName() != null && !std.getInstituteName().isBlank()) {
            CollegeModel college = collegeService.getByInstituteName(std.getInstituteName());
            std.setCollege(college);
        }
        try {
            studentService.save(std);
            redirectAttributes.addFlashAttribute("success", "Student saved successfully!");
        } catch (Exception e) {
            System.err.println("Error saving student: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to save student: " + e.getMessage());
        }
        return "redirect:/StudentList_form";
    }

    @RequestMapping("/edit/{id}")
    public ModelAndView showEditStudentPage(@PathVariable(name = "id") int id, HttpSession session) {
        ModelAndView mav = new ModelAndView("EditStudent");
        StudentModel std = studentService.get(id);
        mav.addObject("student", std);
        mav.addObject("formToken", FormTokenUtil.generateToken(session));
        return mav;
    }

    @RequestMapping("/delete/{id}")
    public String deletestudent(@PathVariable(name = "id") int id, RedirectAttributes redirectAttributes) {
        try {
            studentService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Student deleted successfully!");
        } catch (Exception e) {
            System.err.println("Error deleting student: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to delete student: " + e.getMessage());
        }
        return "redirect:/StudentList_form";
    }

    @PostMapping("/change-student-password")
    public String changeStudentPassword(@RequestParam("email") String email,
            @RequestParam("newPassword") String newPassword,
            RedirectAttributes redirectAttributes) {
        try {
            String normalizedEmail = email.toLowerCase().trim();
            UserCredential user = userService.getUserByEmail(normalizedEmail);
            
            if (user == null) {
                logger.warn("UserCredential not found for existing student {}, creating brand new account.", normalizedEmail);
                user = new UserCredential();
                user.setEmail(normalizedEmail);
                user.setRole("student"); // Assuming student role
                // Get name from StudentModel if possible
                StudentModel student = studentService.getUserByEmail(normalizedEmail);
                if (student != null) {
                    user.setFullName(student.getFullName());
                }
            }
            
            // Set the new raw password; createUser service implementation will handle hashing
            user.setPassword(newPassword);
            userService.createUser(user);
            
            logger.info("Admin successfully changed/created password for student: {}", normalizedEmail);
            redirectAttributes.addFlashAttribute("success", "Password updated successfully for " + normalizedEmail);
        } catch (Exception e) {
            logger.error("Error changing student password: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to change password: " + e.getMessage());
        }
        return "redirect:/StudentList_form";
    }

    @GetMapping("/AdminLoginPage")
    public String AdminLoginPage(HttpSession session) {
        // Clear session on login page access - commented out for debugging
        // session.invalidate();
        return "redirect:/?section=admin";
    }

    @GetMapping("/Alogin")
    public String AloginGet(HttpSession session) {
        // Check if user is already logged in as admin
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        String role = (String) session.getAttribute("role");

        if (loggedIn != null && loggedIn && role != null && role.equalsIgnoreCase("admin")) {
            // Already logged in, redirect to dashboard
            return "redirect:/adminPage";
        } else {
            // Not logged in, show home page with admin section
            return "redirect:/?section=admin";
        }
    }

    @GetMapping("/adminPage")
    public String adminPage(HttpSession session, Model model) {
        // Check if user is logged in as admin
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        String role = (String) session.getAttribute("role");

        if (loggedIn == null || !loggedIn || role == null || !role.equalsIgnoreCase("admin")) {
            return "redirect:/AdminLoginPage";
        }

        String username = (String) session.getAttribute("username");
        if (username != null) {
            AdminModel user = adminService.getUserByEmail(username);
            if (user != null) {
                model.addAttribute("user", user);
            }
        }

        // Add statistics for dashboard with null safety
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
                        .filter(ht -> ht != null && ht.getStatus() != null && ht.getStatus().equals("Pending"))
                        .count();
            }

            model.addAttribute("totalStudents", totalStudents);
            model.addAttribute("totalExams", totalExams);
            model.addAttribute("totalHallTickets", totalHallTickets);
            model.addAttribute("pendingHallTickets", pendingHallTickets);

            // Add courses for the "Create Exam" section
            List<com.example.HAllTicket.model.CourseModel> courseList = courseService.listAll();
            model.addAttribute("courseList", courseList);
        } catch (Exception e) {
            logger.error("Error calculating statistics: {}", e.getMessage(), e);
            // Set default values if there's an error
            model.addAttribute("totalStudents", 0);
            model.addAttribute("totalExams", 0);
            model.addAttribute("totalHallTickets", 0);
            model.addAttribute("pendingHallTickets", 0);
        }

        model.addAttribute("pageTitle", "Admin Dashboard");
        return "adminPage";
    }

    @GetMapping("/admin-profile")
    public String adminProfile(HttpSession session, Model model) {
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        String role = (String) session.getAttribute("role");

        if (loggedIn == null || !loggedIn || role == null || !role.equalsIgnoreCase("admin")) {
            return "redirect:/AdminLoginPage";
        }

        String username = (String) session.getAttribute("username");
        if (username != null) {
            AdminModel user = adminService.getUserByEmail(username);
            if (user != null) {
                model.addAttribute("user", user);
            }
        }
        model.addAttribute("pageTitle", "Admin Profile");
        model.addAttribute("formToken", FormTokenUtil.generateToken(session));
        return "AdminProfile";
    }

    @PostMapping("/save-admin-profile")
    public String saveAdminProfile(@ModelAttribute("user") AdminModel admin,
            @RequestParam(value = "formToken", required = false) String formToken,
            HttpSession session, RedirectAttributes redirectAttributes) {
        if (!FormTokenUtil.validateAndInvalidate(session, formToken)) {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired form. Please try again.");
            return "redirect:/admin-profile";
        }

        try {
            // Find existing admin to preserve fields not in form (like password if not
            // changed)
            AdminModel existingAdmin = adminService.get3(admin.getEmail());
            if (existingAdmin != null) {
                existingAdmin.setFullName(admin.getFullName());
                existingAdmin.setMobileNo(admin.getMobileNo());
                // Handle password change if needed, for now just basic info
                adminService.save(existingAdmin);
                session.setAttribute("fullName", existingAdmin.getFullName());
                redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            }
        } catch (Exception e) {
            logger.error("Error updating admin profile: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to update profile: " + e.getMessage());
        }
        return "redirect:/admin-profile";
    }

    @GetMapping("/manage-admins")
    public String manageAdmins(HttpSession session, Model model) {
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        String role = (String) session.getAttribute("role");

        if (loggedIn == null || !loggedIn || role == null || !role.equalsIgnoreCase("admin")) {
            return "redirect:/AdminLoginPage";
        }

        List<AdminModel> admins = adminService.listAll();
        model.addAttribute("admins", admins);
        model.addAttribute("newAdmin", new AdminModel());
        model.addAttribute("pageTitle", "Manage Administrators");
        model.addAttribute("formToken", FormTokenUtil.generateToken(session));
        return "ManageAdmins";
    }

    @PostMapping("/save-new-admin")
    public String saveNewAdmin(@ModelAttribute("newAdmin") AdminModel admin,
            @RequestParam(value = "formToken", required = false) String formToken,
            HttpSession session, RedirectAttributes redirectAttributes) {
        if (!FormTokenUtil.validateAndInvalidate(session, formToken)) {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired form. Please try again.");
            return "redirect:/manage-admins";
        }

        try {
            if (adminService.checkEmail(admin.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "Email already exists!");
                return "redirect:/manage-admins";
            }
            // Hash the password
            admin.setPassword(PasswordUtil.encode(admin.getPassword()));
            admin.setRole("admin");
            adminService.save(admin);
            redirectAttributes.addFlashAttribute("success", "New administrator created successfully!");
        } catch (Exception e) {
            logger.error("Error creating new admin: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to create administrator: " + e.getMessage());
        }
        return "redirect:/manage-admins";
    }

    @GetMapping("/delete-admin/{id}")
    public String deleteAdmin(@PathVariable("id") int id, HttpSession session, RedirectAttributes redirectAttributes) {
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        String role = (String) session.getAttribute("role");
        String currentUsername = (String) session.getAttribute("username");

        if (loggedIn == null || !loggedIn || role == null || !role.equalsIgnoreCase("admin")) {
            return "redirect:/AdminLoginPage";
        }

        try {
            AdminModel adminToDelete = adminService.get(id);
            if (adminToDelete != null && adminToDelete.getEmail().equals(currentUsername)) {
                redirectAttributes.addFlashAttribute("error", "You cannot delete your own account!");
            } else {
                adminService.delete(id);
                redirectAttributes.addFlashAttribute("success", "Administrator deleted successfully!");
            }
        } catch (Exception e) {
            logger.error("Error deleting admin: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to delete administrator.");
        }
        return "redirect:/manage-admins";
    }

    @GetMapping("/course-form")
    public String manageCourses(HttpSession session, Model model) {
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");
        String role = (String) session.getAttribute("role");
        if (loggedIn == null || !loggedIn || role == null || !role.equalsIgnoreCase("admin")) {
            return "redirect:/AdminLoginPage";
        }

        List<com.example.HAllTicket.model.CourseModel> courses = courseService.listAll();
        model.addAttribute("courses", courses);
        model.addAttribute("newCourse", new com.example.HAllTicket.model.CourseModel());
        model.addAttribute("pageTitle", "Manage Courses");
        model.addAttribute("formToken", FormTokenUtil.generateToken(session));
        return "CourseList";
    }

    @PostMapping("/save-course")
    public String saveCourse(@ModelAttribute("newCourse") com.example.HAllTicket.model.CourseModel course,
            @RequestParam(value = "formToken", required = false) String formToken,
            HttpSession session, RedirectAttributes redirectAttributes) {
        if (!FormTokenUtil.validateAndInvalidate(session, formToken)) {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired form. Please try again.");
            return "redirect:/course-form";
        }
        try {
            courseService.save(course);
            redirectAttributes.addFlashAttribute("success", "Course saved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to save course: " + e.getMessage());
        }
        return "redirect:/course-form";
    }

    @GetMapping("/delete-course/{id}")
    public String deleteCourse(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        try {
            courseService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Course deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete course: " + e.getMessage());
        }
        return "redirect:/course-form";
    }

    @PostMapping("/change-admin-password")
    public String changeAdminPassword(@RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam(value = "formToken", required = false) String formToken,
            HttpSession session, RedirectAttributes redirectAttributes) {
        if (!FormTokenUtil.validateAndInvalidate(session, formToken)) {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired form. Please try again.");
            return "redirect:/admin-profile";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match!");
            return "redirect:/admin-profile";
        }

        String username = (String) session.getAttribute("username");
        try {
            AdminModel admin = adminService.getUserByEmail(username);
            if (admin != null) {
                if (PasswordUtil.matches(currentPassword, admin.getPassword())) {
                    admin.setPassword(PasswordUtil.encode(newPassword));
                    adminService.save(admin);
                    redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Incorrect current password!");
                }
            }
        } catch (Exception e) {
            logger.error("Error changing password: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to change password.");
        }
        return "redirect:/admin-profile";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/student-logout")
    public String studentLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/signin_page";
    }

    @PostMapping("/Alogin")
    public String AdminLogin(@RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpSession session, RedirectAttributes redirectAttributes) {

        username = (username != null) ? username.trim() : "";
        password = (password != null) ? password.trim() : "";

        String normalizedEmail = username.toLowerCase();
        AdminModel user = adminService.getUserByEmail(normalizedEmail);

        if (user == null) {
            logger.warn("Login attempt with non-existent email: {}", normalizedEmail);
            redirectAttributes.addFlashAttribute("loginError", "Invalid email or password");
            return "redirect:/?section=admin";
        }

        boolean passwordMatches = PasswordUtil.matches(password, user.getPassword());

        if (!passwordMatches) {
            logger.warn("Login attempt with incorrect password for email: {}", normalizedEmail);
            redirectAttributes.addFlashAttribute("loginError", "Invalid email or password");
            return "redirect:/?section=admin";
        }

        // Authentication successful
        String role = user.getRole();

        // Verify admin role
        if (role == null || !role.equalsIgnoreCase("admin")) {
            logger.warn("Login attempt with invalid role: {} for email: {}", role, normalizedEmail);
            redirectAttributes.addFlashAttribute("loginError", "Invalid role. Admin access only.");
            return "redirect:/?section=admin";
        }

        logger.info("Successful login for user: {} with role: {}", normalizedEmail, role);

        session.setAttribute("loggedIn", true);
        session.setAttribute("role", role);
        session.setAttribute("username", normalizedEmail);
        session.setAttribute("fullName", user.getFullName());

        return "redirect:/adminPage";
    }

    @PostMapping("/UpdateDetails")
    public String UpdateDetails(@RequestParam("username") String username,
            @RequestParam(value = "formToken", required = false) String formToken,
            HttpSession session, RedirectAttributes redirectAttributes) {
        if (!FormTokenUtil.validateAndInvalidate(session, formToken)) {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired form. Please try again.");
            return "redirect:/UserHomePage?username=" + (username != null ? username : "");
        }
        if (!studentService.checkEmail(username)) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/ExistingUser";
        }
        session.setAttribute("loggedIn", true);
        redirectAttributes.addFlashAttribute("username", username);
        return "redirect:/ExistingUser?username=" + (username != null ? username : "");
    }

    @GetMapping("/ExistingUser")
    public String existingUserPage(@RequestParam(value = "username", required = false) String usernameParam,
            Model model, HttpSession session) {
        String username = (usernameParam != null && !usernameParam.isEmpty()) ? usernameParam
                : (String) session.getAttribute("username");
        if (username == null || username.isEmpty()) {
            return "redirect:/signin_page";
        }

        StudentModel user = studentService.getUserByEmail(username);
        if (user == null) {
            logger.warn("Student profile not found for username: {}", username);
            return "redirect:/UserHomePage?username=" + username;
        }

        model.addAttribute("user", user);
        model.addAttribute("courseList", courseService.listAll());
        model.addAttribute("formToken", FormTokenUtil.generateToken(session));
        model.addAttribute("pageTitle", "Edit Profile");
        return "ExistingUser";
    }

    @GetMapping("/signin_page")
    public String userloginpage() {
        return "redirect:/?section=student";
    }

    @PostMapping("/signin")
    public String loginUser(@RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpSession session, Model model, RedirectAttributes redirectAttributes) {

        if (username == null) username = "";
        // Normalize email to lowercase
        final String normalizedUsername = username.toLowerCase();

        // Check credentials in UserCredential (student login credentials)
        UserCredential hallUser = userService.getUserByEmail(normalizedUsername);

        if (hallUser == null) {
            logger.warn("Student login attempt with non-existent email: {}", username);
            redirectAttributes.addFlashAttribute("loginError", "Invalid email or password");
            return "redirect:/?section=student";
        }

        if (!PasswordUtil.matches(password, hallUser.getPassword())) {
            logger.warn("Student login attempt with incorrect password for email: {}", username);
            redirectAttributes.addFlashAttribute("loginError", "Invalid email or password");
            return "redirect:/?section=student";
        }

        // Check if student profile exists
        StudentModel user = studentService.getUserByEmail(normalizedUsername);
        if (user == null) {
            // User has credentials but no student profile - redirect to complete profile
            logger.info("Student credentials valid but profile incomplete: {}", normalizedUsername);
            session.setAttribute("loggedIn", true);
            session.setAttribute("username", normalizedUsername);
            session.setAttribute("fullName", hallUser.getFullName());
            // Redirect to the form page so a fresh formToken is generated properly
            return "redirect:/userLoginPage_form";
        }

        // Authentication successful with complete profile
        logger.info("Successful student login for: {}", username);

        if (user != null) {
            model.addAttribute("user", user);

            // Get all hall tickets for this student (show all, but prioritize approved
            // ones)
            List<HallTicketModel> hallTickets = hallTicketService.listAll();
            List<HallTicketModel> studentHallTickets = hallTickets.stream()
                    .filter(ht -> ht.getEmail() != null && ht.getEmail().equals(normalizedUsername))
                    // Show all tickets, but prioritize those with QR codes (which means they're
                    // processed)
                    .sorted((ht1, ht2) -> {
                        // Sort: Approved first, then by QR code presence, then by ID
                        if (ht1.getStatus() != null && ht1.getStatus().equals("Approved") &&
                                !(ht2.getStatus() != null && ht2.getStatus().equals("Approved"))) {
                            return -1;
                        }
                        if (ht2.getStatus() != null && ht2.getStatus().equals("Approved") &&
                                !(ht1.getStatus() != null && ht1.getStatus().equals("Approved"))) {
                            return 1;
                        }
                        // If both have QR codes or both don't, sort by ID
                        return Integer.compare(ht2.getId(), ht1.getId());
                    })
                    .collect(Collectors.toList());
            model.addAttribute("hall", studentHallTickets);
            List<HallTicketModel> approvedHall = studentHallTickets.stream()
                    .filter(ht -> "Approved".equals(ht.getStatus())).collect(Collectors.toList());
            List<HallTicketModel> pendingHall = studentHallTickets.stream()
                    .filter(ht -> !"Approved".equals(ht.getStatus())).collect(Collectors.toList());
            model.addAttribute("approvedHall", approvedHall);
            model.addAttribute("pendingHall", pendingHall);
            Set<String> appliedExamNames = studentHallTickets.stream()
                    .map(HallTicketModel::getExamName)
                    .filter(n -> n != null && !n.isEmpty())
                    .collect(Collectors.toSet());
            model.addAttribute("appliedExamNames", appliedExamNames);
            List<ExamModel> listexam = examService.listAll();
            model.addAttribute("listexam", listexam);
            model.addAttribute("exam", new ExamModel());
            model.addAttribute("pageTitle", "Student Dashboard");
            session.setAttribute("loggedIn", true);
            session.setAttribute("username", normalizedUsername);
            session.setAttribute("fullName", user.getFullName());

            return "redirect:/UserHomePage?username=" + normalizedUsername;
        }

        // If we reach here, something went wrong (shouldn't happen with new logic)
        redirectAttributes.addFlashAttribute("loginError", "An unexpected error occurred");
        return "redirect:/?section=student";
    }

    @GetMapping("/registration")
    public String register(Model model, HttpSession session) {
        return "redirect:/?section=register";
    }

    @GetMapping("/userLoginPage_form")
    public String userLoginPage(HttpSession session, Model model) {
        // Always generate a fresh token — this makes page refresh safe
        String token = FormTokenUtil.generateToken(session);
        model.addAttribute("formToken", token);

        String username = (String) session.getAttribute("username");
        if (username != null) {
            UserCredential cred = userService.getUserByEmail(username);
            if (cred != null) {
                // Check if student profile is already complete — redirect to dashboard
                StudentModel existing = studentService.getUserByEmail(username);
                if (existing != null && existing.getInstituteName() != null && !existing.getInstituteName().isBlank()) {
                    logger.info("Student profile already complete for: {}, redirecting to dashboard", username);
                    return "redirect:/UserHomePage?username=" + username;
                }
                StudentModel newStudent = new StudentModel();
                newStudent.setEmail(cred.getEmail());
                newStudent.setFullName(cred.getFullName());
                newStudent.setMobileNo(cred.getMobileNo());
                model.addAttribute("user", newStudent);
            }
        }
        return "userReDetailsPage";
    }

    @PostMapping("/createUser")
    public String createUser(@ModelAttribute UserCredential userCredential,
            @RequestParam(value = "formToken", required = false) String formToken,
            HttpSession session, RedirectAttributes redirectAttributes) {
        if (!FormTokenUtil.validateAndInvalidate(session, formToken)) {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired form. Please try again.");
            return "redirect:/?section=register";
        }
        logger.info("Registration attempt for email: {}",
                userCredential.getEmail() != null ? userCredential.getEmail() : "(null)");
        try {
            if (userCredential.getEmail() == null || userCredential.getEmail().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Email is required");
                return "redirect:/?section=register";
            }

            // Case-sensitive validation for email
            if (!userCredential.getEmail().equals(userCredential.getEmail().toLowerCase())) {
                redirectAttributes.addFlashAttribute("error", "Email address must be in lowercase only. Uppercase letters are not accepted.");
                return "redirect:/?section=register";
            }

            if (userCredential.getPassword() == null || userCredential.getPassword().length() < 8) {
                redirectAttributes.addFlashAttribute("error", "Password must be at least 8 characters");
                return "redirect:/?section=register";
            }

            boolean emailExists = userService.checkEmail(userCredential.getEmail());
            if (emailExists) {
                redirectAttributes.addFlashAttribute("error", "Email ID already exists. Please use a different email.");
                return "redirect:/?section=register";
            }
            if (userCredential.getRole() == null || userCredential.getRole().isEmpty()) {
                userCredential.setRole("student");
            }
            UserCredential userDtls = userService.createUser(userCredential);
            if (userDtls != null) {
                logger.info("Registration successful for email: {}, redirecting to profile form", userDtls.getEmail());
                session.setAttribute("loggedIn", true);
                session.setAttribute("username", userDtls.getEmail());
                session.setAttribute("fullName", userDtls.getFullName());
                redirectAttributes.addFlashAttribute("info", "Account created! Please complete your profile.");
                return "redirect:/userLoginPage_form";
            } else {
                logger.warn("Registration failed: createUser returned null for email: {}", userCredential.getEmail());
                redirectAttributes.addFlashAttribute("error", "Something went wrong on the server. Please try again.");
            }
        } catch (Exception e) {
            logger.error("Registration failed for email: {} - {}", userCredential.getEmail(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
        }
        return "redirect:/?section=register";
    }

    @PostMapping("/update-student/{id}")
    public String updateStudent(@ModelAttribute("user") StudentModel user,
            @RequestParam("username") String username,
            @PathVariable("id") int id,
            @RequestParam(value = "formToken", required = false) String formToken,
            HttpSession session, RedirectAttributes redirectAttributes,
            @RequestParam(value = "img", required = false) MultipartFile img) {
        try {
            username = (username != null) ? username.toLowerCase().trim() : "";
            StudentModel existingStudent = studentService.getStudentById(id);
            if (existingStudent == null) {
                redirectAttributes.addFlashAttribute("error", "Student profile not found.");
                return "redirect:/UserHomePage?username=" + username;
            }

            // Validate DOB
            String dobError = studentService.validateDateOfBirth(user.getDateOfBirth());
            if (dobError != null) {
                redirectAttributes.addFlashAttribute("error", dobError);
                return "redirect:/ExistingUser?username=" + username;
            }

            // Update fields
            existingStudent.setFullName(user.getFullName());
            existingStudent.setMobileNo(user.getMobileNo());
            existingStudent.setDateOfBirth(user.getDateOfBirth());
            existingStudent.setInstituteName(user.getInstituteName());
            existingStudent.setCourse(user.getCourse());
            existingStudent.setAdmissionForYear(user.getAdmissionForYear());
            existingStudent.setSemister(user.getSemister());

            // Handle image upload
            if (img != null && !img.isEmpty()) {
                try {
                    String originalFileName = img.getOriginalFilename();
                    String extension = "";
                    if (originalFileName != null && originalFileName.contains(".")) {
                        extension = originalFileName.substring(originalFileName.lastIndexOf("."));
                    }
                    String fileName = "profile_" + existingStudent.getId() + "_" + System.currentTimeMillis()
                            + extension;

                    // Use persistent uploads/img/ dir — NEVER wiped by Maven
                    File imgDir = new File(com.example.HAllTicket.config.WebConfig.getUploadsDir() + "img");
                    if (!imgDir.exists()) imgDir.mkdirs();

                    Path path = Paths.get(imgDir.getAbsolutePath() + File.separator + fileName);
                    Files.copy(img.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                    existingStudent.setImageName(fileName);
                    logger.info("Profile image saved to: {}", path);
                } catch (Exception e) {
                    logger.error("Error saving profile image: {}", e.getMessage());
                }
            }

            // Update college association
            if (existingStudent.getInstituteName() != null && !existingStudent.getInstituteName().isBlank()) {
                CollegeModel college = collegeService.getByInstituteName(existingStudent.getInstituteName());
                existingStudent.setCollege(college);
            }

            studentService.save(existingStudent);

            // Sync with UserCredential
            try {
                UserCredential credentials = userService.getUserByEmail(existingStudent.getEmail());
                if (credentials != null) {
                    credentials.setFullName(existingStudent.getFullName());
                    credentials.setMobileNo(existingStudent.getMobileNo());
                    userCredentialRepo.save(credentials);
                }
            } catch (Exception e) {
                logger.error("Error syncing credentials: {}", e.getMessage());
            }

            session.setAttribute("fullName", existingStudent.getFullName());
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        } catch (Exception e) {
            logger.error("Error updating student profile: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error",
                    "An error occurred while updating your profile. Please try again.");
        }
        return "redirect:/UserHomePage?username=" + username + "#profile-section";
    }

    @PostMapping("/stud")
    public String createStudent(@ModelAttribute StudentModel user, @RequestParam(required = false) MultipartFile img,
            @RequestParam("username") String username,
            @RequestParam(value = "formToken", required = false) String formToken,
            Model model, HttpSession session, RedirectAttributes redirectAttributes) {

        if (username != null) username = username.toLowerCase().trim();
        logger.info("Profile completion POST - username: {}, Institute: {}", username, user.getInstituteName());

        // Token validation: if expired (e.g. page refresh), redirect back to form
        // rather than showing a hard error — form will get a fresh token on reload.
        if (!FormTokenUtil.validateAndInvalidate(session, formToken)) {
            logger.warn("Form token expired for username: {} — redirecting back to profile form", username);
            // Redirect back to profile form; a fresh token will be generated there
            return "redirect:/userLoginPage_form";
        }

        try {
            if (user.getEmail() == null || user.getEmail().isBlank()) {
                user.setEmail(username);
            }

            // Idempotency: if student profile already complete, just redirect to dashboard
            StudentModel alreadyExists = studentService.getUserByEmail(username);
            if (alreadyExists != null && alreadyExists.getInstituteName() != null
                    && !alreadyExists.getInstituteName().isBlank()) {
                logger.info("Student profile already exists for: {}, redirecting to dashboard", username);
                session.setAttribute("username", username);
                session.setAttribute("loggedIn", true);
                return "redirect:/UserHomePage?username=" + username;
            }
            // Double check DOB
            String dobError = studentService.validateDateOfBirth(user.getDateOfBirth());
            if (dobError != null) {
                logger.warn("DOB validation failed for {}: {}", username, dobError);
                redirectAttributes.addFlashAttribute("error", dobError);
                return "redirect:/userLoginPage_form";
            }

            if (user.getInstituteName() != null && !user.getInstituteName().isBlank()) {
                CollegeModel college = collegeService.getByInstituteName(user.getInstituteName());
                user.setCollege(college);
            }

            // Set imageName early (fallback)
            if (img != null && !img.isEmpty()) {
                user.setImageName(img.getOriginalFilename());
            } else {
                user.setImageName(null);
            }

            logger.info("Saving student to DB: {}", user.getEmail());
            user = studentService.createStudent(user);

            if (user != null && img != null && !img.isEmpty()) {
                try {
                    // Use persistent uploads/img/ dir — NEVER wiped by Maven
                    File imgDir = new File(com.example.HAllTicket.config.WebConfig.getUploadsDir() + "img");
                    if (!imgDir.exists()) imgDir.mkdirs();

                    String fileName = user.getId() + "_" + System.currentTimeMillis() + "_" + img.getOriginalFilename();
                    Path path = Paths.get(imgDir.getAbsolutePath() + File.separator + fileName);

                    Files.copy(img.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                    user.setImageName(fileName);
                    studentService.updateStudent(user);
                    logger.info("Profile image saved to: {}", path);
                } catch (Exception e) {
                    logger.error("Error during image upload: {}", e.getMessage(), e);
                }
            }

            if (user != null) {
                user = studentService.getUserByEmail(username);
                if (user != null) {
                    session.setAttribute("username", username);
                    session.setAttribute("loggedIn", true);
                    logger.info("Profile completion SUCCESS for: {}", username);
                    return "redirect:/UserHomePage?username=" + username;
                }
            }

            logger.warn("Profile completion failed for username: {}", username);
            redirectAttributes.addFlashAttribute("error", "Something went wrong. Please try again.");
            return "redirect:/userLoginPage_form";

        } catch (Exception e) {
            logger.error("CRITICAL ERROR in createStudent: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Server error: " + e.getMessage());
            return "redirect:/userLoginPage_form";
        }
    }

    ///////////////////////////// College/////////////////////////////////
    @GetMapping("/collegeList-form")
    public String viewCollegeList(Model model) {
        List<CollegeModel> listCollege = collegeService.listAll();
        model.addAttribute("listCollege", listCollege);
        System.out.print("Get / ");
        return "CollegeList";
    }

    @GetMapping("/new1")
    public String add1(Model model) {
        model.addAttribute("college", new CollegeModel());
        return "CollegeEdit";
    }

    @PostMapping("/save_new")
    public String saveCollege(@ModelAttribute("college") CollegeModel college, RedirectAttributes redirectAttributes) {
        try {
            collegeService.save(college);
            redirectAttributes.addFlashAttribute("success", "College saved successfully!");
        } catch (Exception e) {
            System.err.println("Error saving college: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to save college: " + e.getMessage());
        }
        return "redirect:/collegeList-form";
    }

    @GetMapping("/edit1/{id}")
    public String collegeEdit(@PathVariable(name = "id") int id, Model model) {
        CollegeModel college = collegeService.get(id);
        model.addAttribute("college", college);
        return "CollegeEdit";
    }

    @GetMapping("/delete1/{id}")
    public String deleteCollege(@PathVariable(name = "id") int id, RedirectAttributes redirectAttributes) {
        try {
            collegeService.delete(id);
            redirectAttributes.addFlashAttribute("success", "College deleted successfully!");
        } catch (Exception e) {
            System.err.println("Error deleting college: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to delete college: " + e.getMessage());
        }
        return "redirect:/collegeList-form";
    }

    ///////////////////// Syllabus////////////////////////////////
    @GetMapping("/Subject-form")
    public String viewSyllabusList(Model model) {
        List<SyllabusModel> listSyllabus = syllabusService.listAll();
        model.addAttribute("listSyllabus", listSyllabus);
        model.addAttribute("courseList", courseService.listAll());
        System.out.print("Get / ");
        return "SyllabusList";
    }

    @GetMapping("/new2")
    public String add2(Model model) {
        model.addAttribute("syllabus", new SyllabusModel());
        model.addAttribute("courseList", courseService.listAll());
        return "SyllabusEdit";
    }

    @PostMapping("/save_sy")
    public String saveSyllabus(@ModelAttribute("syllabus") SyllabusModel syllabus,
            @RequestParam(value = "subjectsJson", required = false) String subjectsJson,
            RedirectAttributes redirectAttributes) {
        // Parse JSON subjects if provided
        if (subjectsJson != null && !subjectsJson.isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.List<com.example.HAllTicket.dto.SubjectDTO> subjects = mapper.readValue(subjectsJson,
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.List<com.example.HAllTicket.dto.SubjectDTO>>() {
                        });

                // Validate max 20 subjects
                if (subjects.size() > 20) {
                    redirectAttributes.addFlashAttribute("error", "Maximum 20 subjects allowed.");
                    return "redirect:/Subject-form";
                }

                syllabus.setSubjects(subjects);
                logger.info("Parsed {} subjects from JSON for syllabus", subjects.size());
            } catch (Exception e) {
                logger.error("Error parsing subjects JSON: {}", e.getMessage());
                redirectAttributes.addFlashAttribute("error", "Invalid subjects data. Please try again.");
                return "redirect:/Subject-form";
            }
        }

        try {
            syllabusService.save(syllabus);
            redirectAttributes.addFlashAttribute("success", "Syllabus saved successfully!");
        } catch (Exception e) {
            System.err.println("Error saving syllabus: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to save syllabus: " + e.getMessage());
        }
        return "redirect:/Subject-form";
    }

    @GetMapping("/edit2/{id}")
    public String syllabusEdit(@PathVariable(name = "id") int id, Model model) {
        SyllabusModel syllabus = syllabusService.get(id);
        model.addAttribute("syllabus", syllabus);
        model.addAttribute("courseList", courseService.listAll());
        return "SyllabusEdit";
    }

    @GetMapping("/delete2/{id}")
    public String deleteSyllabus(@PathVariable(name = "id") int id, RedirectAttributes redirectAttributes) {
        try {
            syllabusService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Syllabus deleted successfully!");
        } catch (Exception e) {
            System.err.println("Error deleting syllabus: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to delete syllabus: " + e.getMessage());
        }
        return "redirect:/Subject-form";
    }

    ////////////////// Exam/////////////////////////
    @GetMapping("/Exam_form")
    public String examForm(@RequestParam("Course") String Course,
            @RequestParam("Year") String Year,
            @RequestParam("Semister") String Semister,
            HttpSession session, Model model) {
        model.addAttribute("formToken", FormTokenUtil.generateToken(session));
        model.addAttribute("minExamDate", LocalDate.now().plusDays(1).toString());
        System.out.println(Course + " here it is");
        System.out.println(Year);
        System.out.println(Semister);
        SyllabusModel syllabus = syllabusService.getUserByDetails(Course, Year, Semister);

        // Always create an ExamModel - never null
        ExamModel exam = new ExamModel();

        // Check if syllabus exists, if not create a new ExamModel with the provided
        // details
        if (syllabus == null) {
            // Set the course, year, and semister from the form parameters
            exam.setCourse(Course != null ? Course : "");
            exam.setYear(Year != null ? Year : "");
            exam.setSemister(Semister != null ? Semister : "");
            model.addAttribute("error",
                    "No syllabus found for the selected Course, Year, and Semester. Please create the syllabus first or fill in the exam details manually.");
        } else {
            // Convert SyllabusModel to ExamModel for the form
            exam.setCourse(syllabus.getCourse() != null ? syllabus.getCourse() : "");
            exam.setYear(syllabus.getYear() != null ? syllabus.getYear() : "");
            exam.setSemister(syllabus.getSemister() != null ? syllabus.getSemister() : "");
        }

        // Always add exam to model - ensures it's never null
        model.addAttribute("exam", exam);
        model.addAttribute("courseList", courseService.listAll());
        return "ExamEdit";
    }

    // REST API endpoint to fetch syllabus subjects
    @GetMapping("/api/syllabus/subjects")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSyllabusSubjects(
            @RequestParam("course") String course,
            @RequestParam("year") String year,
            @RequestParam("semester") String semester) {

        logger.info("Fetching syllabus subjects for Course={}, Year={}, Semester={}", course, year, semester);

        SyllabusModel syllabus = syllabusService.getUserByDetails(course, year, semester);

        Map<String, Object> response = new HashMap<>();

        if (syllabus == null) {
            response.put("found", false);
            response.put("subjects", new ArrayList<>());
            logger.warn("No syllabus found for Course={}, Year={}, Semester={}", course, year, semester);
            return ResponseEntity.ok(response);
        }

        // Return subjects (prefer JSON subjects, fallback to Sub1-Sub6)
        List<com.example.HAllTicket.dto.SubjectDTO> subjects = syllabus.getSubjects();
        if (subjects == null || subjects.isEmpty()) {
            // Fallback: Convert Sub1-Sub6 to SubjectDTO list
            subjects = convertLegacySubjects(syllabus);
            logger.info("Using legacy Sub1-Sub6 format, found {} subjects", subjects.size());
        } else {
            logger.info("Using JSON subjects format, found {} subjects", subjects.size());
        }

        response.put("found", true);
        response.put("subjects", subjects);
        return ResponseEntity.ok(response);
    }

    // Helper method to convert legacy Sub1-Sub6 to SubjectDTO list
    private List<com.example.HAllTicket.dto.SubjectDTO> convertLegacySubjects(SyllabusModel syllabus) {
        List<com.example.HAllTicket.dto.SubjectDTO> subjects = new ArrayList<>();
        if (syllabus.getSub1() != null && !syllabus.getSub1().isEmpty())
            subjects.add(new com.example.HAllTicket.dto.SubjectDTO(syllabus.getSub1(), "", ""));
        if (syllabus.getSub2() != null && !syllabus.getSub2().isEmpty())
            subjects.add(new com.example.HAllTicket.dto.SubjectDTO(syllabus.getSub2(), "", ""));
        if (syllabus.getSub3() != null && !syllabus.getSub3().isEmpty())
            subjects.add(new com.example.HAllTicket.dto.SubjectDTO(syllabus.getSub3(), "", ""));
        if (syllabus.getSub4() != null && !syllabus.getSub4().isEmpty())
            subjects.add(new com.example.HAllTicket.dto.SubjectDTO(syllabus.getSub4(), "", ""));
        if (syllabus.getSub5() != null && !syllabus.getSub5().isEmpty())
            subjects.add(new com.example.HAllTicket.dto.SubjectDTO(syllabus.getSub5(), "", ""));
        if (syllabus.getSub6() != null && !syllabus.getSub6().isEmpty())
            subjects.add(new com.example.HAllTicket.dto.SubjectDTO(syllabus.getSub6(), "", ""));
        return subjects;
    }

    @GetMapping("/exam-form")
    public String viewExamList(Model model) {
        List<ExamModel> listExam = examService.listAll();
        model.addAttribute("listExam", listExam);
        System.out.print("Get / ");
        return "ExamList";
    }

    @GetMapping("/new3")
    public String add3(Model model, HttpSession session) {
        model.addAttribute("exam", new ExamModel());
        model.addAttribute("courseList", courseService.listAll());
        model.addAttribute("formToken", FormTokenUtil.generateToken(session));
        model.addAttribute("minExamDate", LocalDate.now().plusDays(1).toString());
        return "ExamEdit";
    }

    @PostMapping("/save_ex")
    public String saveExam(@ModelAttribute("exam") ExamModel exam,
            @RequestParam(value = "formToken", required = false) String formToken,
            @RequestParam(value = "subjectsJson", required = false) String subjectsJson,
            HttpSession session, RedirectAttributes redirectAttributes) {
        if (!FormTokenUtil.validateAndInvalidate(session, formToken)) {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired form. Please try again.");
            return "redirect:/exam-form";
        }

        // Parse JSON subjects if provided
        if (subjectsJson != null && !subjectsJson.isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.List<com.example.HAllTicket.dto.SubjectDTO> subjects = mapper.readValue(subjectsJson,
                        new com.fasterxml.jackson.core.type.TypeReference<java.util.List<com.example.HAllTicket.dto.SubjectDTO>>() {
                        });

                // Validate max 20 subjects
                if (subjects.size() > 20) {
                    redirectAttributes.addFlashAttribute("error", "Maximum 20 subjects allowed.");
                    if (exam.getId() > 0) {
                        return "redirect:/edit3/" + exam.getId();
                    }
                    return "redirect:/new3";
                }

                exam.setSubjects(subjects);
                logger.info("Parsed {} subjects from JSON", subjects.size());
            } catch (Exception e) {
                logger.error("Error parsing subjects JSON: {}", e.getMessage());
                redirectAttributes.addFlashAttribute("error", "Invalid subjects data. Please try again.");
                return "redirect:/exam-form";
            }
        }

        // Validate exam dates (only for subjects that have dates)
        if (exam.getSubjects() != null && !exam.getSubjects().isEmpty()) {
            java.time.LocalDate today = java.time.LocalDate.now();
            for (int i = 0; i < exam.getSubjects().size(); i++) {
                com.example.HAllTicket.dto.SubjectDTO subject = exam.getSubjects().get(i);
                if (subject.getDate() != null && !subject.getDate().isEmpty()) {
                    try {
                        java.time.LocalDate examDate = java.time.LocalDate.parse(subject.getDate());
                        if (examDate.isBefore(today)) {
                            redirectAttributes.addFlashAttribute("error",
                                    "Exam date for '" + subject.getName() + "' cannot be in the past.");
                            if (exam.getId() > 0) {
                                return "redirect:/edit3/" + exam.getId();
                            }
                            return "redirect:/new3";
                        }
                    } catch (Exception ignored) {
                        // Skip invalid date formats
                    }
                }
            }
        }

        try {
            examService.save(exam);
            redirectAttributes.addFlashAttribute("success", "Exam saved successfully!");
        } catch (Exception e) {
            System.err.println("Error saving exam: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to save exam: " + e.getMessage());
        }
        return "redirect:/exam-form";
    }

    @GetMapping("/edit3/{id}")
    public String examEdit(@PathVariable(name = "id") int id, Model model, HttpSession session) {
        ExamModel exam = examService.get(id);
        model.addAttribute("exam", exam);
        model.addAttribute("courseList", courseService.listAll());
        model.addAttribute("formToken", FormTokenUtil.generateToken(session));
        model.addAttribute("minExamDate", LocalDate.now().plusDays(1).toString());
        return "ExamEdit1";
    }

    @GetMapping("/delete3/{id}")
    public String deleteExam(@PathVariable(name = "id") int id, RedirectAttributes redirectAttributes) {
        try {
            examService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Exam deleted successfully!");
        } catch (Exception e) {
            System.err.println("Error deleting exam: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to delete exam: " + e.getMessage());
        }
        return "redirect:/exam-form";
    }

    @PostMapping("/examAp")
    public String examAp(@ModelAttribute HallTicketModel hallTicketModel, @ModelAttribute StudentModel user,
            @RequestParam("username") String username,
            @RequestParam(value = "formToken", required = false) String formToken,
            HttpSession session, RedirectAttributes redirectAttributes) {
        if (!FormTokenUtil.validateAndInvalidate(session, formToken)) {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired form. Please try again.");
            return "redirect:/UserHomePage?username=" + (username != null ? username : "");
        }
        String email = (hallTicketModel.getEmail() != null && !hallTicketModel.getEmail().isEmpty())
                ? hallTicketModel.getEmail()
                : username;
        String examName = hallTicketModel.getExamName();
        if (examName != null && hallTicketService.hasPendingRequest(email, examName)) {
            redirectAttributes.addFlashAttribute("error",
                    "You already have a pending application for this exam. Please wait for approval.");
            return "redirect:/UserHomePage?username=" + (username != null ? username : "");
        }
        try {
            if (hallTicketModel.getEmail() == null || hallTicketModel.getEmail().isEmpty()) {
                hallTicketModel.setEmail(username);
                logger.info("Setting email in exam application: {}", username);
            }
            StudentModel student = studentService.getUserByEmail(hallTicketModel.getEmail());
            ExamModel exam = hallTicketModel.getExamName() != null ? examService.get1(hallTicketModel.getExamName())
                    : null;
            hallTicketModel.setStudent(student);
            hallTicketModel.setExam(exam);
            hallTicketService.createHall(hallTicketModel);
            session.setAttribute("username", username);
            redirectAttributes.addFlashAttribute("success", "Exam application submitted successfully!");
        } catch (Exception e) {
            logger.error("Error creating exam application: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to submit exam application. Please try again.");
        }
        return "redirect:/UserHomePage?username=" + (username != null ? username : "") + "#hall-tickets-section";
    }

    @PostMapping("/cancel-request/{id}")
    public String cancelRequest(@PathVariable("id") int id,
            @RequestParam(value = "username", required = false) String usernameParam,
            HttpSession session, RedirectAttributes redirectAttributes) {
        String username = (usernameParam != null && !usernameParam.isEmpty()) ? usernameParam
                : (String) session.getAttribute("username");
        if (username == null || username.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Session expired. Please sign in again.");
            return "redirect:/signin_page";
        }
        HallTicketModel hall = hallTicketService.get(id);
        if (hall == null || hall.isDeleted()) {
            redirectAttributes.addFlashAttribute("error", "Application not found.");
            return "redirect:/UserHomePage?username=" + username;
        }
        if (!username.equals(hall.getEmail())) {
            redirectAttributes.addFlashAttribute("error", "You can only cancel your own application.");
            return "redirect:/UserHomePage?username=" + username;
        }
        if ("Approved".equals(hall.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Approved tickets cannot be cancelled.");
            return "redirect:/UserHomePage?username=" + username;
        }
        hallTicketService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Application cancelled.");
        return "redirect:/UserHomePage?username=" + username + "#hall-tickets-section";
    }

    @GetMapping("/hall-form")
    public String hallList(Model model) {
        List<HallTicketModel> listhall = hallTicketService.listAll();
        model.addAttribute("listhall", listhall);
        model.addAttribute("pageTitle", "All Hall Tickets");
        return "HallTicketRequest";
    }

    @GetMapping("/requested-halltickets")
    public String requestedHallTickets(Model model) {
        List<HallTicketModel> listhall = hallTicketService.listAll().stream()
                .filter(h -> h.getStatus() == null || !h.getStatus().equalsIgnoreCase("Approved"))
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("listhall", listhall);
        model.addAttribute("pageTitle", "Create Hall Ticket (Requests)");
        return "HallTicketRequest";
    }

    @GetMapping("/issued-halltickets")
    public String issuedHallTickets(Model model) {
        List<HallTicketModel> listhall = hallTicketService.listAll().stream()
                .filter(h -> h.getStatus() != null && h.getStatus().equalsIgnoreCase("Approved"))
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("listhall", listhall);
        model.addAttribute("pageTitle", "Issued Hall Tickets");
        return "HallTicketRequest";
    }

    @GetMapping("/edit4/{id}/{ExamName}/{email}")
    public String hallEdit(@PathVariable(name = "id") int id, @PathVariable(name = "ExamName") String ExamName,
            @PathVariable(name = "email") String email, Model model, HttpSession session) {
        model.addAttribute("formToken", FormTokenUtil.generateToken(session));
        HallTicketModel hall = hallTicketService.get(id);
        ExamModel exam = examService.get1(ExamName);
        StudentModel stud = studentService.get1(email);

        // Check for null values and provide appropriate error messages
        if (hall == null) {
            model.addAttribute("error", "Hall ticket request with ID " + id + " not found.");
            List<HallTicketModel> listhall = hallTicketService.listAll();
            model.addAttribute("listhall", listhall);
            return "HallTicketRequest";
        }

        if (exam == null) {
            model.addAttribute("error", "Exam with name '" + ExamName + "' not found. Please create the exam first.");
            model.addAttribute("hall", hall);
            List<HallTicketModel> listhall = hallTicketService.listAll();
            model.addAttribute("listhall", listhall);
            return "HallTicketRequest";
        }

        if (stud == null) {
            model.addAttribute("error", "Student with email '" + email + "' not found.");
            model.addAttribute("hall", hall);
            model.addAttribute("exam", exam);
            List<HallTicketModel> listhall = hallTicketService.listAll();
            model.addAttribute("listhall", listhall);
            return "HallTicketRequest";
        }

        // All objects are valid, proceed with hall ticket generation
        // CRITICAL: Set the email in the hall ticket - this MUST be set before form
        // binding
        hall.setEmail(email);
        logger.info("Setting email in hall ticket - ID: {}, Email: {}", hall.getId(), email);

        // Assign alphabetical series seat number if not already assigned
        if (hall.getSeatNo() == null || hall.getSeatNo().isEmpty() || hall.getSeatNo().equals("null")) {
            // Determine series letter from exam's position (sorted by id ascending)
            // 1st exam → A, 2nd exam → B, 3rd exam → C, ...
            String seriesLetter = "A"; // default
            try {
                List<ExamModel> allExams = examService.listAll();
                // Sort by id to get consistent ordering
                allExams.sort(java.util.Comparator.comparingInt(ExamModel::getId));
                for (int i = 0; i < allExams.size(); i++) {
                    if (allExams.get(i).getId() == (exam != null ? exam.getId() : -1)) {
                        // Convert index to letter: 0→A, 1→B, ..., 25→Z, 26→AA, etc.
                        int idx = i;
                        if (idx < 26) {
                            seriesLetter = String.valueOf((char) ('A' + idx));
                        } else {
                            // For more than 26 exams: AA, AB, ...
                            seriesLetter = String.valueOf((char) ('A' + (idx / 26) - 1))
                                         + String.valueOf((char) ('A' + (idx % 26)));
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                logger.warn("Could not determine exam series letter, defaulting to A: {}", e.getMessage());
            }

            String prefix = seriesLetter + "-";

            // Find the next available seat number for this exam
            java.util.List<String> allocatedSeats = hallTicketService.findAllocatedSeatsByExamName(hall.getExamName());
            java.util.Set<Integer> usedNumbers = new java.util.HashSet<>();
            for (String seat : allocatedSeats) {
                if (seat != null && seat.startsWith(prefix)) {
                    try {
                        String numStr = seat.substring(prefix.length());
                        usedNumbers.add(Integer.parseInt(numStr));
                    } catch (NumberFormatException e) {
                        // ignore malformed entries
                    }
                }
            }

            int nextNum = 1;
            while (usedNumbers.contains(nextNum)) {
                nextNum++;
            }

            // Zero-pad to 2 digits: A-01, A-02, ... A-99, A-100
            String paddedNum = nextNum < 100 ? String.format("%02d", nextNum) : String.valueOf(nextNum);
            hall.setSeatNo(prefix + paddedNum);
            logger.info("Generated alphabetical seat number: {} for Hall Ticket ID: {} (Exam: {})",
                    hall.getSeatNo(), hall.getId(), hall.getExamName());
        }

        // Also save it to database immediately to ensure it persists
        try {
            hallTicketService.save(hall);
            logger.info("Hall ticket email saved to database - ID: {}, Email: {}", hall.getId(), email);
        } catch (Exception e) {
            logger.error("Error saving email to database: {}", e.getMessage(), e);
        }

        model.addAttribute("hall", hall);
        model.addAttribute("exam", exam);
        model.addAttribute("stud", stud);

        return "HallTicketGeneration";
    }

    @GetMapping("/HallTicketGeneration")
    public String hallTicket() {
        return "HallTicketGeneration";
    }

    @GetMapping("/delete4/{id}")
    public String deleteHall(@PathVariable(name = "id") int id, RedirectAttributes redirectAttributes) {
        try {
            hallTicketService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Hall ticket request deleted successfully!");
        } catch (Exception e) {
            System.err.println("Error deleting hall ticket: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to delete hall ticket: " + e.getMessage());
        }
        return "redirect:/hall-form";
    }

    @PostMapping("/save_hall")
    public String saveHall(@ModelAttribute("hall") HallTicketModel hall,
            @RequestParam(value = "formToken", required = false) String formToken,
            HttpSession session, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if (!FormTokenUtil.validateAndInvalidate(session, formToken)) {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired form. Please try again.");
            return "redirect:/hall-form";
        }
        logger.info("Saving hall ticket - ID: {}, SeatNo: {}, ExamName: {}, StudentName: {}, Email: {}",
                hall.getId(), hall.getSeatNo(), hall.getExamName(), hall.getStudentName(), hall.getEmail());

        // CRITICAL: Ensure email is set - this is required for student dashboard
        // filtering
        if (hall.getEmail() == null || hall.getEmail().isEmpty()) {
            logger.error("Email is missing in hall ticket! This will prevent it from showing on student dashboard.");
            redirectAttributes.addFlashAttribute("error", "Email is required. Please go back and try again.");
            return "redirect:/hall-form";
        }
        StudentModel student = studentService.getUserByEmail(hall.getEmail());
        ExamModel exam = hall.getExamName() != null ? examService.get1(hall.getExamName()) : null;
        if (exam != null) {
            hall.setExamName(exam.getExamName()); // Normalize name from official DB record
            hall.setExam(exam); // Set direct link
        }
        hall.setStudent(student);

        // SYNC: Copy subjects from exam to hall ticket list for permanent storage
        if (exam != null && exam.getSubjects() != null && !exam.getSubjects().isEmpty()) {
            hall.setSubjects(new ArrayList<>(exam.getSubjects()));
            // Sync to legacy fields for max PDF compatibility
            List<SubjectDTO> subjects = exam.getSubjects();
            if (subjects.size() >= 1)
                hall.setSub1(subjects.get(0).getName());
            if (subjects.size() >= 2)
                hall.setSub2(subjects.get(1).getName());
            if (subjects.size() >= 3)
                hall.setSub3(subjects.get(2).getName());
            if (subjects.size() >= 4)
                hall.setSub4(subjects.get(3).getName());
            if (subjects.size() >= 5)
                hall.setSub5(subjects.get(4).getName());
            if (subjects.size() >= 6)
                hall.setSub6(subjects.get(5).getName());
            logger.info("Copied {} subjects from exam to hall ticket (JSON + Legacy)", exam.getSubjects().size());
        } else if (exam != null) {
            // Fallback for legacy exam fields
            List<SubjectDTO> legacySubjects = new ArrayList<>();
            if (exam.getSub1() != null && !exam.getSub1().isEmpty()) {
                legacySubjects.add(new SubjectDTO(exam.getSub1(), exam.getDate1(), exam.getTime1()));
                hall.setSub1(exam.getSub1());
            }
            if (exam.getSub2() != null && !exam.getSub2().isEmpty()) {
                legacySubjects.add(new SubjectDTO(exam.getSub2(), exam.getDate2(), exam.getTime2()));
                hall.setSub2(exam.getSub2());
            }
            if (exam.getSub3() != null && !exam.getSub3().isEmpty()) {
                legacySubjects.add(new SubjectDTO(exam.getSub3(), exam.getDate3(), exam.getTime3()));
                hall.setSub3(exam.getSub3());
            }
            if (exam.getSub4() != null && !exam.getSub4().isEmpty()) {
                legacySubjects.add(new SubjectDTO(exam.getSub4(), exam.getDate4(), exam.getTime4()));
                hall.setSub4(exam.getSub4());
            }
            if (exam.getSub5() != null && !exam.getSub5().isEmpty()) {
                legacySubjects.add(new SubjectDTO(exam.getSub5(), exam.getDate5(), exam.getTime5()));
                hall.setSub5(exam.getSub5());
            }
            if (exam.getSub6() != null && !exam.getSub6().isEmpty()) {
                legacySubjects.add(new SubjectDTO(exam.getSub6(), exam.getDate6(), exam.getTime6()));
                hall.setSub6(exam.getSub6());
            }
            hall.setSubjects(legacySubjects);
            logger.info("Copied {} legacy subjects from exam to hall ticket", legacySubjects.size());
        }

        // Restore SeatNo from database if lost during form mapping
        if (hall.getId() > 0 && (hall.getSeatNo() == null || hall.getSeatNo().isEmpty() || "null".equals(hall.getSeatNo()))) {
            HallTicketModel existing = hallTicketService.get(hall.getId());
            if (existing != null && existing.getSeatNo() != null && !existing.getSeatNo().isEmpty()) {
                hall.setSeatNo(existing.getSeatNo());
                logger.info("Restored SeatNo {} from database for Hall Ticket ID: {}", hall.getSeatNo(), hall.getId());
            }
        }

        // Validate and set SeatNo if missing (redundant check, just in case)
        if (hall.getSeatNo() == null || hall.getSeatNo().isEmpty() || hall.getSeatNo().equals("null")) {
            // Fallback: generate alphabetical seat using exam position
            String fallbackSeries = "A";
            try {
                if (exam != null) {
                    List<ExamModel> allExams = examService.listAll();
                    allExams.sort(java.util.Comparator.comparingInt(ExamModel::getId));
                    for (int i = 0; i < allExams.size(); i++) {
                        if (allExams.get(i).getId() == exam.getId()) {
                            fallbackSeries = i < 26 ? String.valueOf((char) ('A' + i))
                                    : String.valueOf((char) ('A' + (i / 26) - 1)) + String.valueOf((char) ('A' + (i % 26)));
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {}
            String seatNo = fallbackSeries + "-" + String.format("%02d", hall.getId() > 0 ? hall.getId() : 1);
            hall.setSeatNo(seatNo);
            logger.info("Generated fallback SeatNo {} for Hall Ticket ID: {}", hall.getSeatNo(), hall.getId());
        }

        // Generate QR code BEFORE saving to ensure we have all data
        try {
            logger.info("Generating QR code for SeatNo: {}", hall.getSeatNo());
            String baseUrl = ServletUriComponentsBuilder.fromRequest(request).replacePath(null).build().toUriString();
            BufferedImage bufferedImage = generateQRCodeImage(hall, baseUrl);
            String imagePath = "img-" + hall.getSeatNo().replaceAll("[^a-zA-Z0-9]", "-") + ".jpg";

            // Persist QR code in the external uploads directory for immediate availability
            File qrDir = new File(WebConfig.getUploadsDir() + "qr");
            if (!qrDir.exists()) {
                boolean created = qrDir.mkdirs();
                logger.info("Created missing QR directory: {}", qrDir.getAbsolutePath());
            }

            File outputfile = new File(qrDir, imagePath);
            ImageIO.write(bufferedImage, "jpg", outputfile);
            logger.info("QR code saved to: {}", outputfile.getAbsolutePath());

            // Save QR code path to database
            hall.setQrName(imagePath);
            logger.info("QR code path saved to hall ticket: {}", imagePath);

            // CRITICAL: Set status to "Approved" when QR code is successfully generated
            hall.setStatus("Approved");
            logger.info("Status changed to 'Approved' for hall ticket ID: {}", hall.getId());

        } catch (Exception e) {
            logger.error("QR Code generation failed for hall ticket ID: {}, SeatNo: {}",
                    hall.getId(), hall.getSeatNo(), e);
            e.printStackTrace();
            // Continue to save hall ticket even if QR generation fails
            redirectAttributes.addFlashAttribute("warning",
                    "Hall ticket saved, but QR code generation failed: " + e.getMessage());
        }

        try {
            logger.info("Saving hall ticket to database - ID: {}, Email: {}, QR: {}",
                    hall.getId(), hall.getEmail(), hall.getQrName());
            hallTicketService.save(hall);
            logger.info("Hall ticket saved successfully - ID: {}, Email: {}, QR: {}",
                    hall.getId(), hall.getEmail(), hall.getQrName());
            if (!redirectAttributes.getFlashAttributes().containsKey("success") &&
                    !redirectAttributes.getFlashAttributes().containsKey("warning")) {
                redirectAttributes.addFlashAttribute("success", "Hall ticket saved successfully with QR code!");
            }
        } catch (Exception e) {
            logger.error("Error saving hall ticket: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to save hall ticket. Please try again.");
            return "redirect:/requested-halltickets";
        }

        return "redirect:/issued-halltickets";
    }

    /** Public view of an approved hall ticket (linked from QR code). */
    @GetMapping("/hall-ticket/view/{id}")
    public String viewHallTicket(@PathVariable("id") int id, Model model) {
        HallTicketModel hall = hallTicketService.get(id);
        if (hall == null || hall.isDeleted()) {
            model.addAttribute("error", "Hall ticket not found.");
            return "HallTicketView";
        }
        if (!"Approved".equals(hall.getStatus())) {
            model.addAttribute("error", "This hall ticket is not yet approved.");
            return "HallTicketView";
        }
        ExamModel exam = hall.getExam() != null ? hall.getExam()
                : (hall.getExamName() != null ? examService.get1(hall.getExamName()) : null);
        StudentModel stud = hall.getStudent() != null ? hall.getStudent()
                : (hall.getEmail() != null ? studentService.getUserByEmail(hall.getEmail()) : null);
        model.addAttribute("hall", hall);
        model.addAttribute("exam", exam);
        model.addAttribute("stud", stud);
        return "HallTicketView";
    }

    /** Download hall ticket as PDF. */
    @GetMapping("/hall-ticket/download/{id}")
    public ResponseEntity<byte[]> downloadHallTicketPdf(@PathVariable("id") int id) {
        HallTicketModel hall = hallTicketService.get(id);
        if (hall == null || hall.isDeleted()) {
            return ResponseEntity.notFound().build();
        }
        if (!"Approved".equals(hall.getStatus())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        ExamModel exam = hall.getExam() != null ? hall.getExam()
                : (hall.getExamName() != null ? examService.get1(hall.getExamName()) : null);
        StudentModel stud = hall.getStudent() != null ? hall.getStudent()
                : (hall.getEmail() != null ? studentService.getUserByEmail(hall.getEmail()) : null);
        byte[] pdf = pdfGeneratorService.generateHallTicketPdf(hall, exam, stud);
        if (pdf == null) {
            return ResponseEntity.internalServerError().build();
        }
        String filename = "hall-ticket-" + (hall.getSeatNo() != null ? hall.getSeatNo() : id) + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    public static BufferedImage generateQRCodeImage(HallTicketModel hallTicket, String baseUrl) throws Exception {
        if (hallTicket == null) {
            throw new IllegalArgumentException("Hall ticket cannot be null");
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("--- E-HALL TICKET OFFICE COPY ---\n");
        sb.append("Institute: ").append(hallTicket.getInstituteName() != null ? hallTicket.getInstituteName() : "N/A").append("\n");
        sb.append("Exam: ").append(hallTicket.getExamName() != null ? hallTicket.getExamName() : "N/A").append("\n");
        sb.append("Student: ").append(hallTicket.getStudentName() != null ? hallTicket.getStudentName() : "N/A").append("\n");
        sb.append("Seat No: ").append(hallTicket.getSeatNo() != null ? hallTicket.getSeatNo() : "N/A").append("\n\n");
        
        // Include Schedule
        List<com.example.HAllTicket.dto.SubjectDTO> subjects = hallTicket.getSubjects();
        if (subjects != null && !subjects.isEmpty()) {
            sb.append("EXAM SCHEDULE:\n");
            int count = 0;
            for (com.example.HAllTicket.dto.SubjectDTO s : subjects) {
                if (count++ >= 10) break; // Limit to keep QR readable
                sb.append("- ").append(s.getName()).append(" (")
                  .append(s.getDate() != null ? s.getDate() : "Date TBD").append(")\n");
            }
            sb.append("\n");
        }
        
        if (baseUrl != null && !baseUrl.isEmpty() && hallTicket.getId() > 0) {
            sb.append("Verify: ").append(baseUrl).append("/hall-ticket/view/").append(hallTicket.getId());
        }
        
        String qrContent = sb.toString();
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        // Slightly higher margin for better scanning
        BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 380, 380);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
    /// th:value="${syllabus.CourseName} + ' ' + ${syllabus.Year}"
}
