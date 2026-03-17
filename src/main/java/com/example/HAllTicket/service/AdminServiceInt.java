package com.example.HAllTicket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.HAllTicket.model.AdminModel;
import com.example.HAllTicket.model.ExamModel;
import com.example.HAllTicket.repository.AdminRepository;

@Service
public class AdminServiceInt implements AdminService{
	@Autowired
	private AdminRepository adminRepo;

	@Override
	public boolean checkEmail(String email) {
		// TODO Auto-generated method stub
		return adminRepo.existsByEmail(email);
	}

	@Override
	public boolean checkPassword(String password) {
		// TODO Auto-generated method stub
		return adminRepo.existsByPassword(password);
	}

	@Override
	public String getRoleByEmail(String email) {
		// TODO Auto-generated method stub
		AdminModel user = adminRepo.findTop1ByEmail(email);
        if (user != null) {
            return user.getRole();
        } else {
            return null; // Handle the case where the user is not found
        }
	}

	@Override
	public AdminModel getUserByEmail(String email) {
		// TODO Auto-generated method stub
		return adminRepo.findTop1ByEmail(email);
	}

	@Override
	public AdminModel get3(String email) {
		// TODO Auto-generated method stub
		return adminRepo.findTop1ByEmail(email);
	}

	@Override
	public void save(AdminModel admin) {
		adminRepo.save(admin);
	}

	@Override
	public java.util.List<AdminModel> listAll() {
		return adminRepo.findAll();
	}

	@Override
	public AdminModel get(int id) {
		return adminRepo.findById(id).orElse(null);
	}

	@Override
	public void delete(int id) {
		AdminModel admin = get(id);
		if (admin != null) {
			admin.setDeleted(true);
			adminRepo.save(admin);
		}
	}

}
