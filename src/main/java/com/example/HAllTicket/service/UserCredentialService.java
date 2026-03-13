package com.example.HAllTicket.service;

import com.example.HAllTicket.model.UserCredential;

public interface UserCredentialService {
    UserCredential createUser(UserCredential user);
    boolean checkEmail(String email);
    boolean checkPassword(String password);
    String getRoleByEmail(String email);
    UserCredential getUserByEmail(String email);
}
