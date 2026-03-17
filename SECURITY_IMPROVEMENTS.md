# Security Improvements for HallMeet

## ⚠️ Current Security Status

The authentication system has been **FIXED** but still has security gaps:

### ✅ Fixed Issues:
1. **Authentication Logic** - Now properly validates email AND password belong to the same user
2. **Error Messages** - Login errors are now displayed to users
3. **Logging** - Failed login attempts are logged for security monitoring

### 🔴 Security Gaps (Not Yet Implemented):

1. **Plain Text Passwords** - Passwords are stored in plain text (CRITICAL)
2. **No CSRF Protection** - Forms vulnerable to cross-site request forgery
3. **No Rate Limiting** - Vulnerable to brute force attacks
4. **No Session Timeout** - Sessions persist indefinitely
5. **No Input Validation** - Risk of SQL injection

---

## 🔒 How to Add BCrypt Password Encryption

### Step 1: Add Spring Security Dependency

Add to `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

### Step 2: Create SecurityConfig.java

```java
package com.example.HAllTicket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable() // Disable for now, enable with proper tokens later
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/signin_page", "/signin", "/registration", "/AdminLoginPage", "/Alogin").permitAll()
                .requestMatchers("/static/**", "/css/**", "/js/**", "/img/**").permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
```

### Step 3: Update Login Methods

**Admin Login (HallController.java line ~296):**

```java
@Autowired
private PasswordEncoder passwordEncoder; // Add this field

// In AdminLogin method, change:
if (!user.getPassword().equals(password)) {
// To:
if (!passwordEncoder.matches(password, user.getPassword())) {
```

**Student Login (HallController.java line ~390):**

```java
// Change:
if (!user.getPassword().equals(password)) {
// To:
if (!passwordEncoder.matches(password, user.getPassword())) {
```

### Step 4: Hash Existing Passwords

**Option A: Manual Update (MySQL)**

```java
// Create a utility class to hash passwords
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHasher {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode("123456789");
        System.out.println("Hashed password: " + hashedPassword);
    }
}
```

Then update your database:

```sql
UPDATE admin_model SET password = '$2a$10$...' WHERE email = 'swapnil@gmail.com';
```

**Option B: Create Migration Endpoint**

```java
@GetMapping("/admin/hash-passwords") // Remove after use!
public String hashPasswords() {
    List<AdminModel> admins = adminService.listAll();
    for (AdminModel admin : admins) {
        if (!admin.getPassword().startsWith("$2a$")) { // Not already hashed
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
            adminService.save(admin);
        }
    }
    return "Passwords hashed";
}
```

### Step 5: Update Registration

```java
// In registration method:
student.setPassword(passwordEncoder.encode(student.getPassword()));
```

---

## 🛡️ Additional Security Measures

### 1. Add CSRF Protection

Enable CSRF in SecurityConfig and add tokens to forms:

```html
<form th:action="@{/Alogin}" method="post">
    <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
    <!-- rest of form -->
</form>
```

### 2. Add Rate Limiting

Use Bucket4j or similar:

```xml
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.0.0</version>
</dependency>
```

### 3. Add Session Timeout

In `application.properties`:

```properties
server.servlet.session.timeout=30m
```

### 4. Add Input Validation

```java
@PostMapping("/Alogin")
public String AdminLogin(
    @RequestParam("username") @Email String username,
    @RequestParam("password") @Size(min=8) String password,
    HttpSession session, Model model) {
    // ... validation happens automatically
}
```

### 5. Prevent SQL Injection

Already using JPA (safe), but add validation:

```java
if (!username.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
    model.addAttribute("loginError", "Invalid email format");
    return "AdminLoginPage";
}
```

---

## 📊 Security Checklist

- [x] Fix authentication logic (email + password for same user)
- [x] Add error message display
- [x] Add security logging
- [ ] Implement BCrypt password hashing
- [ ] Enable CSRF protection
- [ ] Add rate limiting
- [ ] Configure session timeout
- [ ] Add input validation
- [ ] Implement password strength requirements
- [ ] Add "Remember Me" functionality (secure)
- [ ] Implement account lockout after failed attempts
- [ ] Add two-factor authentication (optional)

---

## 🚀 Priority Fixes

### CRITICAL (Do First):
1. **BCrypt Password Hashing** - Protects passwords if database is compromised
2. **CSRF Protection** - Prevents unauthorized actions
3. **Rate Limiting** - Prevents brute force attacks

### IMPORTANT (Do Soon):
4. Session Timeout
5. Input Validation
6. Password Strength Requirements

### NICE TO HAVE:
7. Account Lockout
8. Remember Me
9. Two-Factor Authentication

---

## 📝 Testing Security

After implementing:

1. Test login with correct credentials
2. Test login with wrong password
3. Test login with non-existent email
4. Test CSRF protection (try form submission without token)
5. Test rate limiting (multiple failed logins)
6. Test session timeout (wait and try to access protected page)

---

**Note:** The current implementation is functional but **NOT production-ready** without these security measures!
