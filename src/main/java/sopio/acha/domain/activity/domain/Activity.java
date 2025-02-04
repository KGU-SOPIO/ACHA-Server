package sopio.acha.domain.activity.domain;

import jakarta.persistence.*;
import lombok.Getter;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn
public abstract class Activity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private Boolean available;

    private String title;

    private String link;

    private String deadline;

    public Activity(Boolean available, String title, String link, String deadline) {
        this.available = available;
        this.title = title;
        this.link = link;
        this.deadline = deadline;
    }

    public Activity() {

    }
}
