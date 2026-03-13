# Hall Ticket Generation System - Project Details

## 📋 Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Database Schema](#database-schema)
- [API Endpoints](#api-endpoints)
- [Models & Entities](#models--entities)
- [Technologies Used](#technologies-used)
- [Project Structure](#project-structure)
- [User Roles & Workflows](#user-roles--workflows)
- [QR Code Generation](#qr-code-generation)
- [File Storage](#file-storage)

---

## Overview

The **Hall Ticket Generation System** is a comprehensive web application built with Spring Boot for managing student hall tickets with QR code generation. The system facilitates exam management, student registration, and automated hall ticket generation with QR codes for easy verification.

### Key Capabilities
- ✅ Student registration and profile management
- ✅ Admin dashboard for system management
- ✅ Exam creation and management
- ✅ Hall ticket generation with QR codes
- ✅ College and syllabus management
- ✅ Role-based access control (Admin, Student, User)
- ✅ Image upload and storage
- ✅ QR code generation and storage

---

## Features

### Admin Features
- **Dashboard**: Statistics overview, quick actions
- **Student Management**: View, add, edit, delete students
- **College Management**: Manage college information
- **Syllabus Management**: Create and manage course syllabi
- **Exam Management**: Create exams with subjects, dates, and times
- **Hall Ticket Management**: 
  - View hall ticket requests
  - Generate hall tickets with QR codes
  - Approve/reject requests
  - Delete hall tickets

### Student Features
- **Registration**: Create student account with profile photo
- **Dashboard**: View all hall tickets with QR codes
- **Exam Application**: Apply for available exams
- **Profile Management**: Update personal details
- **Hall Ticket Viewing**: Download and view hall tickets with QR codes

### User Features (General Users)
- **Registration**: Create general user account
- **Profile Management**: Update user details

---

## Architecture

### Technology Stack

#### Backend
- **Framework**: Spring Boot 3.4.1
- **Language**: Java 21
- **ORM**: Spring Data JPA / Hibernate
- **Template Engine**: Thymeleaf
- **Build Tool**: Maven

#### Frontend
- **UI Framework**: AdminLTE 3 (Bootstrap 5 based)
- **Icons**: Font Awesome
- **JavaScript**: Vanilla JS with Bootstrap components

#### Database
- **Database**: MySQL 8.0
- **Container**: Docker
- **ORM**: JPA/Hibernate

#### Additional Libraries
- **QR Code Generation**: ZXing (Google)
- **Validation**: Hibernate Validator
- **Utilities**: Lombok

### Architecture Pattern
- **MVC (Model-View-Controller)**: Spring MVC pattern
- **Repository Pattern**: Spring Data JPA repositories
- **Service Layer**: Business logic separation
- **Entity Models**: JPA entities for database mapping

---

## Database Schema

### Tables

#### 1. `admin_model`
| Column | Type | Description |
|--------|------|-------------|
| id | INT (PK, Auto) | Admin ID |
| full_name | VARCHAR | Admin full name |
| email | VARCHAR | Admin email (unique) |
| password | VARCHAR | Admin password |
| mobile_no | VARCHAR | Mobile number |
| role | VARCHAR | Role (admin) |

#### 2. `student_model`
| Column | Type | Description |
|--------|------|-------------|
| id | INT (PK, Auto) | Student ID |
| full_name | VARCHAR | Student full name |
| email | VARCHAR | Student email (unique) |
| mobile_no | VARCHAR | Mobile number |
| date_of_birth | VARCHAR | Date of birth |
| college_code | VARCHAR | College code |
| college_name | VARCHAR | College name |
| course | VARCHAR | Course (MCA, MBA, BE, B.TECH) |
| admission_for_year | VARCHAR | Admission year |
| semister | VARCHAR | Semester |
| image_name | VARCHAR | Profile photo filename |

#### 3. `hall_model`
| Column | Type | Description |
|--------|------|-------------|
| id | INT (PK, Auto) | User ID |
| full_name | VARCHAR | User full name |
| email | VARCHAR | User email (unique) |
| password | VARCHAR | User password |
| mobile_no | VARCHAR | Mobile number |
| role | VARCHAR | User role |

#### 4. `college_model`
| Column | Type | Description |
|--------|------|-------------|
| id | INT (PK, Auto) | College ID |
| college_name | VARCHAR | College name |
| address | VARCHAR | College address |

#### 5. `syllabus_model`
| Column | Type | Description |
|--------|------|-------------|
| id | INT (PK, Auto) | Syllabus ID |
| course | VARCHAR | Course name |
| year | VARCHAR | Year |
| semister | VARCHAR | Semester |
| sub1 | VARCHAR | Subject 1 |
| sub2 | VARCHAR | Subject 2 |
| sub3 | VARCHAR | Subject 3 |
| sub4 | VARCHAR | Subject 4 |
| sub5 | VARCHAR | Subject 5 |
| sub6 | VARCHAR | Subject 6 |

#### 6. `exam_model`
| Column | Type | Description |
|--------|------|-------------|
| id | INT (PK, Auto) | Exam ID |
| exam_name | VARCHAR | Exam name |
| course | VARCHAR | Course |
| year | VARCHAR | Year |
| semister | VARCHAR | Semester |
| sub1 | VARCHAR | Subject 1 |
| sub2 | VARCHAR | Subject 2 |
| sub3 | VARCHAR | Subject 3 |
| sub4 | VARCHAR | Subject 4 |
| sub5 | VARCHAR | Subject 5 |
| sub6 | VARCHAR | Subject 6 |
| date1 | VARCHAR | Subject 1 date |
| date2 | VARCHAR | Subject 2 date |
| date3 | VARCHAR | Subject 3 date |
| date4 | VARCHAR | Subject 4 date |
| date5 | VARCHAR | Subject 5 date |
| date6 | VARCHAR | Subject 6 date |
| time1 | VARCHAR | Subject 1 time |
| time2 | VARCHAR | Subject 2 time |
| time3 | VARCHAR | Subject 3 time |
| time4 | VARCHAR | Subject 4 time |
| time5 | VARCHAR | Subject 5 time |
| time6 | VARCHAR | Subject 6 time |

#### 7. `hall_ticket_model`
| Column | Type | Description |
|--------|------|-------------|
| id | INT (PK, Auto) | Hall Ticket ID |
| seat_no | VARCHAR | Seat number |
| admin_name | VARCHAR | Admin name |
| college_name | VARCHAR | College name |
| student_name | VARCHAR | Student name |
| exam_name | VARCHAR | Exam name |
| status | VARCHAR | Status (Pending, Approved) |
| email | VARCHAR | Student email |
| sub1 | VARCHAR | Subject 1 details |
| sub2 | VARCHAR | Subject 2 details |
| sub3 | VARCHAR | Subject 3 details |
| sub4 | VARCHAR | Subject 4 details |
| sub5 | VARCHAR | Subject 5 details |
| sub6 | VARCHAR | Subject 6 details |
| image_name | VARCHAR | Student photo filename |
| qr_name | VARCHAR | QR code image filename |

### Relationships
- **Student** → **HallTicket**: One-to-Many (email)
- **Exam** → **HallTicket**: One-to-Many (exam_name)
- **Syllabus** → **Exam**: Used for exam creation

---

## API Endpoints

### Public Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Home page |
| GET | `/registration` | Registration page |
| POST | `/createUser` | Create user account |
| GET | `/signin_page` | Student login page |
| GET | `/signin` | Student login |
| GET | `/AdminLoginPage` | Admin login page |
| POST | `/Alogin` | Admin login |

### Student Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/UserHomePage` | Student dashboard |
| GET | `/ExistingUser` | Existing user page |
| POST | `/UpdateDetails` | Update student details |
| POST | `/examAp` | Apply for exam |
| GET | `/getHall` | Get hall tickets |

### Admin Endpoints

#### Student Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/StudentList_form` | List all students |
| GET | `/Student_form` | Add student form |
| POST | `/save` | Save student |
| GET | `/edit/{id}` | Edit student |
| GET | `/delete/{id}` | Delete student |

#### College Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/collegeList-form` | List all colleges |
| GET | `/college-form` | Add college form |
| POST | `/save_college` | Save college |
| GET | `/edit1/{id}` | Edit college |
| GET | `/delete1/{id}` | Delete college |

#### Syllabus Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/Subject-form` | List all syllabi |
| GET | `/new1` | Add syllabus form |
| POST | `/save_syllabus` | Save syllabus |
| GET | `/edit2/{id}` | Edit syllabus |
| GET | `/delete2/{id}` | Delete syllabus |

#### Exam Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/Exam_form` | Create exam form |
| GET | `/exam-form` | List all exams |
| GET | `/new3` | New exam form |
| POST | `/save_ex` | Save exam |
| GET | `/edit3/{id}` | Edit exam |
| GET | `/delete3/{id}` | Delete exam |

#### Hall Ticket Management
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/hall-form` | List hall ticket requests |
| GET | `/edit4/{id}/{ExamName}/{email}` | Generate hall ticket |
| POST | `/save_hall` | Save hall ticket with QR code |
| GET | `/delete4/{id}` | Delete hall ticket |

---

## Models & Entities

### 1. AdminModel
```java
@Entity
public class AdminModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String fullName;
    private String email;
    private String password;
    private String mobileNo;
    private String role;
}
```

### 2. StudentModel
```java
@Entity
public class StudentModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String fullName;
    private String email;
    private String mobileNo;
    private String DateOfBirth;
    private String CollegeCode;
    private String CollegeName;
    private String Course;
    private String AdmissionForYear;
    private String Semister;
    private String imageName;
}
```

### 3. HallModel
```java
@Entity
public class HallModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String fullName;
    private String email;
    private String password;
    private String mobileNo;
    private String role;
}
```

### 4. CollegeModel
```java
@Entity
public class CollegeModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String CollegeName;
    private String Address;
}
```

### 5. SyllabusModel
```java
@Entity
public class SyllabusModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String Course;
    private String Year;
    private String Semister;
    private String Sub1;
    private String Sub2;
    private String Sub3;
    private String Sub4;
    private String Sub5;
    private String Sub6;
}
```

### 6. ExamModel
```java
@Entity
public class ExamModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String examName;
    private String Course;
    private String Year;
    private String Semister;
    private String Sub1, Sub2, Sub3, Sub4, Sub5, Sub6;
    private String date1, date2, date3, date4, date5, date6;
    private String time1, time2, time3, time4, time5, time6;
}
```

### 7. HallTicketModel
```java
@Entity
public class HallTicketModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String SeatNo;
    private String AdminName;
    private String CollegeName;
    private String StudentName;
    private String ExamName;
    private String Status;
    private String email;
    private String sub1, sub2, sub3, sub4, sub5, sub6;
    private String imageName;
    private String qrName;
}
```

---

## Technologies Used

### Backend
- **Spring Boot 3.4.1**: Main framework
- **Spring MVC**: Web framework
- **Spring Data JPA**: Data access layer
- **Hibernate**: ORM implementation
- **Lombok**: Code generation
- **Hibernate Validator**: Validation

### Frontend
- **AdminLTE 3**: Admin dashboard template
- **Bootstrap 5**: CSS framework
- **Thymeleaf 3**: Template engine
- **Font Awesome**: Icons
- **jQuery**: JavaScript library (if used)

### Database
- **MySQL 8.0**: Relational database
- **Docker**: Containerization

### Build & Tools
- **Maven**: Build tool
- **Java 21**: Programming language
- **Spring Boot DevTools**: Hot reload

### Libraries
- **ZXing (Google)**: QR code generation
  - `com.google.zxing:core:3.4.1`
  - `com.google.zxing:javase:3.4.1`

---

## Project Structure

```
Hall_Ticket/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/HAllTicket/
│   │   │       ├── controller/
│   │   │       │   └── HallController.java      # Main controller
│   │   │       ├── model/                       # Entity models
│   │   │       │   ├── AdminModel.java
│   │   │       │   ├── StudentModel.java
│   │   │       │   ├── HallModel.java
│   │   │       │   ├── CollegeModel.java
│   │   │       │   ├── SyllabusModel.java
│   │   │       │   ├── ExamModel.java
│   │   │       │   └── HallTicketModel.java
│   │   │       ├── repository/                 # Data repositories
│   │   │       │   ├── AdminRepository.java
│   │   │       │   ├── StudentRepository.java
│   │   │       │   ├── HallRepository.java
│   │   │       │   ├── CollegeRepository.java
│   │   │       │   ├── SyllabusRepository.java
│   │   │       │   ├── ExamRepository.java
│   │   │       │   └── HallTicketRepository.java
│   │   │       ├── service/                    # Business logic
│   │   │       │   ├── AdminService.java
│   │   │       │   ├── AdminServiceInt.java
│   │   │       │   ├── StudentService.java
│   │   │       │   ├── StudentServiceInt.java
│   │   │       │   └── [other services]
│   │   │       ├── config/
│   │   │       │   └── WebConfig.java          # Static resource config
│   │   │       └── HallTicketApplication.java  # Main application
│   │   └── resources/
│   │       ├── templates/                      # Thymeleaf templates
│   │       │   ├── base.html                  # Base template
│   │       │   ├── index.html                 # Home page
│   │       │   ├── login.html                 # Student login
│   │       │   ├── AdminLoginPage.html        # Admin login
│   │       │   ├── adminPage.html             # Admin dashboard
│   │       │   ├── UserHomePage.html          # Student dashboard
│   │       │   └── [other templates]
│   │       ├── static/                        # Static resources
│   │       │   ├── img/                       # Student photos
│   │       │   └── qr/                       # QR code images
│   │       └── application.properties         # Configuration
│   └── test/                                  # Test files
├── docker/
│   └── mysql/
│       └── init/
│           └── 01-init.sql                    # DB initialization
├── docker-compose.yml                         # Docker setup
├── pom.xml                                    # Maven config
├── mvnw, mvnw.cmd                            # Maven wrapper
├── start.sh                                  # Production script
├── dev.sh                                    # Development script
├── stop.sh                                   # Stop script
├── SETUP.md                                  # Setup guide
└── PROJECT_DETAILS.md                        # This file
```

---

## User Roles & Workflows

### Admin Workflow
1. **Login** → `/AdminLoginPage` → `/Alogin`
2. **Dashboard** → View statistics and quick actions
3. **Manage Students** → Add/edit/delete students
4. **Manage Colleges** → Add/edit/delete colleges
5. **Manage Syllabus** → Create syllabi for courses
6. **Create Exams** → Select course/year/semester → Fill exam details
7. **Manage Hall Tickets** → View requests → Generate tickets → Approve

### Student Workflow
1. **Registration** → `/registration` → Create account
2. **Login** → `/signin_page` → `/signin`
3. **Dashboard** → View hall tickets with QR codes
4. **Apply for Exam** → Select exam → Submit application
5. **View Hall Ticket** → Download/print hall ticket with QR code

### Hall Ticket Generation Flow
1. Student applies for exam → Creates `HallTicketModel` with status "Pending"
2. Admin views requests → `/hall-form`
3. Admin clicks "Create" → `/edit4/{id}/{ExamName}/{email}`
4. Admin fills hall ticket details → `/save_hall`
5. System generates QR code → Saves to `/static/qr/`
6. Hall ticket saved with QR code path → Status changed to "Approved"
7. Student can view approved ticket on dashboard

---

## QR Code Generation

### Implementation
- **Library**: ZXing (Google)
- **Format**: PNG image
- **Content**: Hall ticket details (ID, Student Name, Exam Name, Seat No, etc.)
- **Storage**: `src/main/resources/static/qr/img-{SeatNo}.jpg`

### QR Code Content Structure
```
Hall Ticket ID: {id}
Student: {StudentName}
Exam: {ExamName}
Seat No: {SeatNo}
College: {CollegeName}
Status: {Status}
```

### Generation Process
1. Create `QRCodeWriter` instance
2. Encode hall ticket data to string
3. Generate `BitMatrix` with error correction
4. Convert to `BufferedImage`
5. Save to file system
6. Store path in `HallTicketModel.qrName`

---

## File Storage

### Directory Structure
```
src/main/resources/static/
├── img/                    # Student profile photos
│   ├── profil_photo.jpg
│   └── [other images]
└── qr/                     # QR code images
    ├── img-MCA1117.jpg
    └── [other QR codes]
```

### File Naming Conventions
- **Student Photos**: Original filename or `profil_photo.jpg`
- **QR Codes**: `img-{SeatNo}.jpg` (e.g., `img-MCA1117.jpg`)

### File Upload Configuration
```properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

---

## Security Considerations

### Current Implementation
- Session-based authentication
- Role-based access control (Admin, Student, User)
- Password validation (minimum 8 characters)
- Email uniqueness validation

### Recommendations for Production
- Implement Spring Security
- Password encryption (BCrypt)
- HTTPS/SSL configuration
- CSRF protection
- Input validation and sanitization
- SQL injection prevention (JPA handles this)
- File upload validation

---

## Performance Considerations

### Database
- Indexes on frequently queried fields (email, exam_name)
- Connection pooling (HikariCP default)
- Query optimization

### Caching
- Thymeleaf template caching (disabled in dev, enable in prod)
- Consider Redis for session management

### File Storage
- Consider external storage (S3, Azure Blob) for production
- Implement file cleanup for old QR codes

---

## Future Enhancements

### Potential Features
- Email notifications
- PDF generation for hall tickets
- Mobile app integration
- Advanced search and filtering
- Bulk operations
- Audit logging
- Report generation
- Multi-language support

---

## Support & Maintenance

### Logging
- Application logs in console
- Consider implementing proper logging framework (Logback/SLF4J)

### Monitoring
- Health check endpoints
- Database connection monitoring
- File storage monitoring

### Backup
- Database backup strategy
- File backup strategy
- Configuration backup

---

## License

This project is for educational purposes.

---

For setup instructions, see [SETUP.md](SETUP.md)


