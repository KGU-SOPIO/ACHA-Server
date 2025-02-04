package sopio.acha.domain.activity.domain;

import jakarta.persistence.*;
import jdk.jfr.Timestamp;
import lombok.Getter;

import java.time.LocalDateTime;

@Embeddable
@Table(name = "assignment")
@Getter
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String link;

    private Long code;

    private String gradingStatus;

    @Timestamp
    private LocalDateTime deadline;

    @Timestamp
    private LocalDateTime timeLeft;

    private String submitStatus;

    public Assignment(String title, String link, Long code, String gradingStatus, LocalDateTime deadline, LocalDateTime timeLeft, String submitStatus) {
        this.title = title;
        this.link = link;
        this.code = code;
        this.gradingStatus = gradingStatus;
        this.deadline = deadline;
        this.timeLeft = timeLeft;
        this.submitStatus = submitStatus;
    }

    protected Assignment() {

    }
}
