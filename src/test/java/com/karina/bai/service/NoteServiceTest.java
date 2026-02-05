package com.karina.bai.service;

import com.karina.bai.model.Note;
import com.karina.bai.model.User;
import com.karina.bai.repository.NoteRepository;
import com.karina.bai.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NoteService noteService;

    @Test
    void myNotesReturnsNotesForCurrentUser() {
        User user = new User("user", "user@example.com", "hashed");
        user.setId(42L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(noteRepository.findAllByUserIdOrderByCreatedAtDesc(42L)).thenReturn(List.of());

        List<Note> notes = noteService.myNotes("user@example.com");

        assertThat(notes).isEmpty();
        verify(noteRepository).findAllByUserIdOrderByCreatedAtDesc(42L);
    }

    @Test
    void createStoresNoteWithUserId() {
        User user = new User("user", "user@example.com", "hashed");
        user.setId(7L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Note note = noteService.create("user@example.com", "Title", "Content");

        assertThat(note.getUserId()).isEqualTo(7L);
        assertThat(note.getTitle()).isEqualTo("Title");
    }

    @Test
    void getMineOrThrowRejectsUnauthorizedAccess() {
        User user = new User("user", "user@example.com", "hashed");
        user.setId(7L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(noteRepository.findByIdAndUserId(99L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.getMineOrThrow("user@example.com", 99L))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Access denied or note not found");
    }

    @Test
    void updateChangesNoteAndSaves() {
        User user = new User("user", "user@example.com", "hashed");
        user.setId(7L);
        Note note = new Note(7L, "Old", "Old content");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(noteRepository.findByIdAndUserId(5L, 7L)).thenReturn(Optional.of(note));

        noteService.update("user@example.com", 5L, "New", "New content");

        assertThat(note.getTitle()).isEqualTo("New");
        assertThat(note.getContent()).isEqualTo("New content");
        verify(noteRepository).save(note);
    }

    @Test
    void deleteUsesUserScopedRepositoryMethod() {
        User user = new User("user", "user@example.com", "hashed");
        user.setId(7L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        noteService.delete("user@example.com", 12L);

        verify(noteRepository).deleteByIdAndUserId(12L, 7L);
    }
}