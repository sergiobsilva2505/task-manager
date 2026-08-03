package br.com.forjacode.taskmanager.adapters.output.persistence;

import br.com.forjacode.taskmanager.domain.model.enums.AuthProvider;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "auth_identities")
public class AuthIdentityJpaEntity {

    @Id
    private UUID id;
    private UUID userId;
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;
    private String passwordHash;
    private String providerUserId;
    private Instant createdAt;
}
