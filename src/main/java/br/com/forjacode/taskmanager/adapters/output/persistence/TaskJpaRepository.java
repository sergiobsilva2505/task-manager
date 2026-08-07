package br.com.forjacode.taskmanager.adapters.output.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface TaskJpaRepository extends JpaRepository<TaskJpaEntity, UUID> {

    @Modifying
    @Transactional
    void deleteByIdAndOwnerId(UUID id, UUID ownerId);

    Page<TaskJpaEntity> findAllByOwnerId(UUID ownerId, Pageable pageable);

    List<TaskJpaEntity> findAllByOwnerId(UUID ownerId);
}
