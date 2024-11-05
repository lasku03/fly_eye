package fi.savonia.fly.domain.perimeter.model;

import java.util.Date;
import java.util.List;

import fi.savonia.fly.domain.detection.model.Detection;
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
import jakarta.persistence.OneToMany;
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
@Table(name = "Perimeter")
public class Perimeter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int perimeterID;
    private String name;
    private Date date;

    //Relation ManyToMany Perimeter - Point
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
        name = "Perimeter_point",
        joinColumns = @JoinColumn(name = "perimeterID"),
        inverseJoinColumns = @JoinColumn(name = "pointID")
    )
    List<Point> points;

    // Relation between Detection and Perimeter
    @OneToMany(mappedBy = "perimeter", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Detection> detections;

    public Point getAnglePoint(int angle) {
        for (Point point : points) {
            if (point.getAngle() == angle) {
                return point;
            }
        }
        return null;
    }
}
