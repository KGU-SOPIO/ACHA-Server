package sopio.acha.domain.activity.domain;

import jakarta.persistence.*;
import jdk.jfr.Timestamp;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
@DiscriminatorValue("assignment")
public class Assignment extends Activity{

    private Long code;

    private String gradingStatus;

    @Timestamp
    private LocalDateTime timeLeft;

    private String submitStatus;

    public Assignment(Boolean available, String title, String link, String deadline, Long code, String gradingStatus, LocalDateTime timeLeft, String submitStatus) {
        super(available, title, link, deadline);
        this.code = code;
        this.gradingStatus = gradingStatus;
        this.timeLeft = timeLeft;
        this.submitStatus = submitStatus;
    }

    public Assignment() {

    }
}
