package sopio.acha.domain.activity.domain;

import jakarta.persistence.*;
import jdk.jfr.Timestamp;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
@DiscriminatorValue("video")
public class Video extends Activity{

    @Timestamp
    private LocalDateTime startTime;

    @Timestamp
    private LocalDateTime lectureTime;

    public Video(Boolean available, String title, String link, String deadline, LocalDateTime startTime, LocalDateTime lectureTime) {
        super(available, title, link, deadline);
        this.startTime = startTime;
        this.lectureTime = lectureTime;
    }

    public Video() {

    }
}
