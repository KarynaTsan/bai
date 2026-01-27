package com.karina.bai.repository;

import com.karina.bai.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    // zwykłe JPA (bez surowego SQL)
    List<Note> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Note> findByIdAndUserId(Long id, Long userId);
    void deleteByIdAndUserId(Long id, Long userId);

    //  surowy SQL z prepared statement (nativeQuery)
    @Query(value = """
        SELECT *
        FROM notes
        WHERE user_id = :userId
          AND LOWER(title) LIKE LOWER(CONCAT('%', :title, '%'))
        ORDER BY created_at DESC
    """, nativeQuery = true)
    List<Note> findMyNotesNative(@Param("userId") Long userId,
                                 @Param("title") String title);
}
