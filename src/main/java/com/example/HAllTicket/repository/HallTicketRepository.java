package com.example.HAllTicket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.HAllTicket.model.HallTicketModel;

@Repository
public interface HallTicketRepository extends JpaRepository<HallTicketModel, Integer> {
	HallTicketModel findTop1ByEmail(String email);

	@Query("SELECT COUNT(h) > 0 FROM HallTicketModel h WHERE h.email = :email AND h.ExamName = :examName AND h.Status = :status")
	boolean existsByEmailAndExamNameAndStatus(@Param("email") String email, @Param("examName") String examName, @Param("status") String status);

	@Query("SELECT h.SeatNo FROM HallTicketModel h WHERE h.ExamName = :examName AND h.SeatNo IS NOT NULL AND h.deleted = false")
	java.util.List<String> findAllocatedSeatsByExamName(@Param("examName") String examName);
}
