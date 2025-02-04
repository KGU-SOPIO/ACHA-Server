package sopio.acha.domain.activity.domain;

import jakarta.persistence.*;
import jdk.jfr.Timestamp;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@SuperBuilder
@DiscriminatorValue("video")
@NoArgsConstructor
public class Video extends Activity{

    @Timestamp
    private LocalDateTime startTime;

    @Timestamp
    private LocalDateTime lectureTime;

    public static Video createVideo(Boolean available, String title, String link, String deadline, LocalDateTime startTime, LocalDateTime lectureTime) {
        return Video.builder()
                .available(available)
                .title(title)
                .link(link)
                .deadline(deadline)
                .startTime(startTime)
                .lectureTime(lectureTime)
                .build();
    }

}
