package fi.savonia.fly.repositories;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fi.savonia.fly.domain.perimeter.model.Perimeter;

@Repository
public interface PerimeterRepository extends JpaRepository<Perimeter, Integer>{
    
}
