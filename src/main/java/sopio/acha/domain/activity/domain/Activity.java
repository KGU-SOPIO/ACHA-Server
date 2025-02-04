package sopio.acha.domain.activity.domain;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn
@SuperBuilder
@NoArgsConstructor
public abstract class Activity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Boolean available;

    @Column(nullable = false)
    private String title;

    @Column(length = 500)
    private String link;

    private String deadline;
}
