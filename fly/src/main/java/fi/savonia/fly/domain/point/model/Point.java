package fi.savonia.fly.domain.point.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fi.savonia.fly.domain.detection.model.Detection;
import fi.savonia.fly.domain.perimeter.model.Perimeter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "Point")

public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int pointID;
    private int angle;
    private double distance;

    //Relation ManyToMany Perimeter - Point
    @ManyToMany(mappedBy = "points", cascade = CascadeType.ALL)
    @JsonIgnore
    List<Perimeter> perimeters;

    //Relation ManyToMany Detection - Point
    @ManyToMany(mappedBy = "points", cascade = CascadeType.ALL)
    @JsonIgnore
    List<Detection> detections;
}
