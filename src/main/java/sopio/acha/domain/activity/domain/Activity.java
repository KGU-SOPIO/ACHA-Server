package sopio.acha.domain.activity.domain;

import jakarta.persistence.*;
import lombok.Getter;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "activity")
@Getter
public class Activity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private Boolean available;

    @Embedded
    private Assignment assignment;

    @Embedded
    private Video video;

    public Activity(Boolean available, Assignment assignment, Video video) {
        this.available = available;
        this.assignment = assignment;
        this.video = video;
    }

    protected Activity() {

    }
}
