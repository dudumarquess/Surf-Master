package com.surfmaster.repository;

import com.surfmaster.entities.SurfSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


public interface SurfSummaryRepository extends JpaRepository<SurfSummary, Long> {

    @Query("""
           select s from SurfSummary s
           where s.spot.id = :spotId and s.window = :window
           order by s.generatedAt desc
           limit 1
           """)
    SurfSummary findLatestForSpotAndDay(Long spotId, String window);
}
