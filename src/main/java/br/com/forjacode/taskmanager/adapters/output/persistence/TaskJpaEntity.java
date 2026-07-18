package br.com.forjacode.taskmanager.adapters.output.persistence;

import br.com.forjacode.taskmanager.domain.model.enums.Priority;
import br.com.forjacode.taskmanager.domain.model.enums.Status;
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
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Table(name = "tasks")
public class TaskJpaEntity {

    @Id
    private UUID id;
    private String title;
    private String description;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Enumerated(EnumType.STRING)
    private Priority priority;
    private LocalDateTime dueDate;
    private Instant createdAt;
    private Instant updatedAt;

}
