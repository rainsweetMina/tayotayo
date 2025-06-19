package kroryi.bus2.repository.jpa;

import kroryi.bus2.entity.LowFloorBus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LowFloorBusRepository extends JpaRepository<LowFloorBus, Long> {

    @Query("SELECT l FROM LowFloorBus l LEFT JOIN FETCH l.files ORDER BY l.createdDate DESC")
    List<LowFloorBus> findAllByOrderByCreatedDateDesc();

    @Query("SELECT l FROM LowFloorBus l LEFT JOIN FETCH l.files WHERE l.id = :id")
    Optional<LowFloorBus> findByIdWithFiles(Long id);
} 