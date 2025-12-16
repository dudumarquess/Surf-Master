package com.surfmaster.repository;


import com.surfmaster.entities.Forecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.List;

public interface ForecastRepository extends JpaRepository<Forecast, Long> {

    @Query("select f from Forecast f where f.spot.id = :spotId and f.timestamp >= :from order by f.timestamp asc")
    public List<Forecast> findBySpotIdAfter(Long spotId, OffsetDateTime from);

    @Query("""
        select f from Forecast f
        join fetch f.spot
        where f.timestamp >= :from
            and f.timestamp <= :to
    """)
    List<Forecast> findAllInRangeWithSpot(OffsetDateTime from, OffsetDateTime to);


}
