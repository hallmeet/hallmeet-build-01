package com.example.HAllTicket.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.HAllTicket.model.HallTicketModel;
import com.example.HAllTicket.repository.HallTicketRepository;

@Service
public class HallTicketServiceInt implements HallTicketService{
	@Autowired
	private HallTicketRepository hallTicketRepo;
	@Override
	public HallTicketModel createHall(HallTicketModel hallTicketModel) {
		
		return hallTicketRepo.save(hallTicketModel);
	}
	@Override
	public List<HallTicketModel> listAll() {
		// TODO Auto-generated method stub
		return hallTicketRepo.findAll();
	}
	@Override
	public HallTicketModel get(int id) {
		// TODO Auto-generated method stub
		return hallTicketRepo.findById(id).orElse(null);
	}
	@Override
	public void delete(int id) {
		// TODO Auto-generated method stub
		HallTicketModel existing = hallTicketRepo.findById(id).orElse(null);
		if (existing == null) {
			return;
		}
		existing.setDeleted(true);
		hallTicketRepo.save(existing);
	}
	@Override
	public void save(HallTicketModel hall) {
		// TODO Auto-generated method stub
		hallTicketRepo.save(hall);
	}
	@Override
	public HallTicketModel getUserByEmail(String email) {
		return hallTicketRepo.findTop1ByEmail(email);
	}

	@Override
	public boolean hasPendingRequest(String email, String examName) {
		if (email == null || examName == null) return false;
		return hallTicketRepo.existsByEmailAndExamNameAndStatus(email, examName, "Pending");
	}
}
