package com.example.HAllTicket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.HAllTicket.model.CourseModel;

@Repository
public interface CourseRepository extends JpaRepository<CourseModel, Integer> {
}
