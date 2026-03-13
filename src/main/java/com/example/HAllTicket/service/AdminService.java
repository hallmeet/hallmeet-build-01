package com.example.HAllTicket.service;

import com.example.HAllTicket.model.AdminModel;
import com.example.HAllTicket.model.ExamModel;


public interface AdminService {
	
	
	public boolean checkEmail(String email);
	public boolean checkPassword(String password);
	String getRoleByEmail(String email);
	AdminModel getUserByEmail(String email);
	AdminModel get3(String email);
}
