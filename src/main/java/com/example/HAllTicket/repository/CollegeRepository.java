package com.example.HAllTicket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.HAllTicket.model.CollegeModel;

@Repository
public interface CollegeRepository extends JpaRepository<CollegeModel, Integer> {

    @Query("SELECT c FROM CollegeModel c WHERE c.instituteName = :instituteName")
    CollegeModel findTop1ByInstituteName(@Param("instituteName") String instituteName);
}
