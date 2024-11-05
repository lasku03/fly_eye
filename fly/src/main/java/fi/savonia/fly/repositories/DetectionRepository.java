package fi.savonia.fly.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.savonia.fly.domain.detection.model.Detection;

@Repository
public interface DetectionRepository extends JpaRepository<Detection, Integer>{
    
}
