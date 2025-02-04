package sopio.acha.domain.activity.domain;

import jakarta.persistence.*;
import jdk.jfr.Timestamp;
import lombok.Getter;

import java.time.LocalDateTime;

@Embeddable
@Table(name = "video")
@Getter
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Timestamp
    private LocalDateTime startTime;

    @Timestamp
    private LocalDateTime endTime;

    @Timestamp
    private LocalDateTime lectureTime;

    public Video(String title, LocalDateTime startTime, LocalDateTime endTime, LocalDateTime lectureTime) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.lectureTime = lectureTime;
    }

    protected Video() {

    }
}
