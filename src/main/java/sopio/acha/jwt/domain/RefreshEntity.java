package sopio.acha.jwt.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "RefreshEntity")
@Table(name = "refresh_entity")
@Setter
@Getter
public class RefreshEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String username;

    @Column(length = 512)
    private String refresh;

    @Column
    private String expiration;

}
