package com.example.HAllTicket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.HAllTicket.model.UserCredential;
import com.example.HAllTicket.repository.UserCredentialRepository;
import com.example.HAllTicket.util.PasswordUtil;

@Service
public class UserCredentialServiceInt implements UserCredentialService {
    @Autowired
    private UserCredentialRepository userCredentialRepo;

    @Override
    public UserCredential createUser(UserCredential user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()
                && !user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2b$")) {
            user.setPassword(PasswordUtil.encode(user.getPassword()));
        }
        return userCredentialRepo.save(user);
    }

    @Override
    public boolean checkEmail(String email) {
        return userCredentialRepo.existsByEmail(email);
    }

    @Override
    public boolean checkPassword(String password) {
        return userCredentialRepo.existsByPassword(password);
    }

    @Override
    public String getRoleByEmail(String email) {
        UserCredential user = userCredentialRepo.findTop1ByEmail(email);
        return user != null ? user.getRole() : null;
    }

    @Override
    public UserCredential getUserByEmail(String email) {
        return userCredentialRepo.findTop1ByEmail(email);
    }
}
