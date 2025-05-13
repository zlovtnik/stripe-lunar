package com.lunar.stripelunar.repository;

import com.lunar.stripelunar.model.ETLJobHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ETLJobHistoryRepository extends JpaRepository<ETLJobHistory, Long> {
    
    List<ETLJobHistory> findByJobNameOrderByStartTimeDesc(String jobName);
    
    @Query("SELECT e FROM ETLJobHistory e WHERE e.jobName = :jobName AND e.status = :status ORDER BY e.startTime DESC")
    List<ETLJobHistory> findByJobNameAndStatus(@Param("jobName") String jobName, @Param("status") String status);
    
    @Query("SELECT e FROM ETLJobHistory e WHERE e.startTime >= :startDate ORDER BY e.startTime DESC")
    List<ETLJobHistory> findJobsAfterDate(@Param("startDate") LocalDateTime startDate);
    
    Optional<ETLJobHistory> findTopByJobNameOrderByStartTimeDesc(String jobName);
    
    @Query("SELECT COUNT(e) FROM ETLJobHistory e WHERE e.jobName = :jobName AND e.status = :status")
    long countByJobNameAndStatus(@Param("jobName") String jobName, @Param("status") String status);
}
