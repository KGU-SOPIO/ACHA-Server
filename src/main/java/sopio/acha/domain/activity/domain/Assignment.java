package sopio.acha.domain.activity.domain;

import jakarta.persistence.*;
import jdk.jfr.Timestamp;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@DiscriminatorValue("assignment")
@SuperBuilder
@NoArgsConstructor
public class Assignment extends Activity{

    @Column(nullable = false)
    private Long code;

    @Column(nullable = false)
    private String gradingStatus;

    @Timestamp
    private LocalDateTime timeLeft;

    private String submitStatus;

    public static Assignment createAssignment(Boolean available, String title, String link, String deadline, Long code, String gradingStatus, LocalDateTime timeLeft, String submitStatus) {
        return Assignment.builder()
                .available(available)
                .title(title)
                .link(link)
                .deadline(deadline)
                .code(code)
                .gradingStatus(gradingStatus)
                .timeLeft(timeLeft)
                .submitStatus(submitStatus)
                .build();
    }
}
