package com.example.jobportal.repository;

import com.example.jobportal.entity.Company;
import com.example.jobportal.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    // Open jobs whose title contains the keyword AND whose location contains the location.
    // LOWER(...) makes the match ignore capital letters. '%' means "anything before/after".
    @Query("SELECT j FROM Job j WHERE j.status = com.example.jobportal.entity.JobStatus.OPEN "
            + "AND LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "AND LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    Page<Job> search(@Param("keyword") String keyword,
                     @Param("location") String location,
                     Pageable pageable);

    List<Job> findByCompanyOrderByPostedAtDesc(Company company);
}