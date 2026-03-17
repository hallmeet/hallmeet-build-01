package com.example.HAllTicket.service;

import java.util.List;

import com.example.HAllTicket.model.HallTicketModel;

public interface HallTicketService {

	HallTicketModel createHall(HallTicketModel hallTicketModel);

	List<HallTicketModel> listAll();

	HallTicketModel get(int id);

	void delete(int id);

	void save(HallTicketModel hall);

	HallTicketModel getUserByEmail(String email);

	/**
	 * Returns true if the student already has a pending request for this exam.
	 */
	boolean hasPendingRequest(String email, String examName);

	List<String> findAllocatedSeatsByExamName(String examName);
}
