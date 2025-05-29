package com.alberto.mpesa.api.store.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "guest_sessions")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Data
public class Guest {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    private String sessionId;

}
