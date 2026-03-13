package com.example.HAllTicket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import com.example.HAllTicket.model.SyllabusModel;
@Repository
public interface SyllabusRepository extends JpaRepository<SyllabusModel, Integer> {
    @Query(value = "SELECT * FROM syllabus_model s WHERE s.course = :Course AND s.academic_year = :Year AND s.semister = :Semister AND (s.deleted = false OR s.deleted IS NULL) LIMIT 1", nativeQuery = true)
    SyllabusModel findByDetails(@Param("Course") String Course, @Param("Year") String Year, @Param("Semister") String Semister);
}
