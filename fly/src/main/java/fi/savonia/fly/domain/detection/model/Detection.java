package fi.savonia.fly.domain.detection.model;

import java.util.Date;
import java.util.List;

import fi.savonia.fly.domain.perimeter.model.Perimeter;
import fi.savonia.fly.domain.point.model.Point;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
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
@Table(name = "Detection")

public class Detection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int detectionID;
    private Date date;
    private String direction;

    //Relation ManyToMany Detection - Point
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
        name = "Detection_point",
        joinColumns = @JoinColumn(name = "detectionID"),
        inverseJoinColumns = @JoinColumn(name = "pointID")
    )
    List<Point> points;

    // Relation between Detection and Perimeter
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "perimeterID")
    private Perimeter perimeter;

    public Point getAnglePoint(int angle) {
        for (Point point : points) {
            if (point.getAngle() == angle) {
                return point;
            }
        }
        return null;
    }
}
