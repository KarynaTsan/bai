package com.karina.bai.service;

import com.karina.bai.model.Note;
import com.karina.bai.model.User;
import com.karina.bai.repository.NoteRepository;
import com.karina.bai.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    private NoteService noteService;
    @BeforeEach
    void setUp() {
        noteService = new NoteService(noteRepository, userRepository);
    }

    @Test
    void myNotesUsesCurrentUserId() {
        User user = new User("user", "user@example.com", "hashed");
        user.setId(11L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(noteRepository.findAllByUserIdOrderByCreatedAtDesc(11L)).thenReturn(List.of());

        List<Note> result = noteService.myNotes("user@example.com");

        assertThat(result).isEmpty();
        verify(noteRepository).findAllByUserIdOrderByCreatedAtDesc(42L);
    }

    @Test
    void createPersistsNoteForCurrentUser() {
        User user = new User("user", "user@example.com", "hashed");
        user.setId(7L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Note saved = noteService.create("user@example.com", "Title", "Content");

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(captor.capture());
        Note captured = captor.getValue();
        assertThat(captured.getUserId()).isEqualTo(7L);
        assertThat(captured.getTitle()).isEqualTo("Title");
        assertThat(saved.getContent()).isEqualTo("Content");
    }

    @Test
    void getMineOrThrowRejectsOtherUsers() {
        User user = new User("user", "user@example.com", "hashed");
        user.setId(5L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(noteRepository.findByIdAndUserId(99L, 5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.getMineOrThrow("user@example.com", 99L))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Access denied or note not found");
    }

    @Test
    void updateChangesFieldsAndSaves() {
        User user = new User("user", "user@example.com", "hashed");
        user.setId(3L);
        Note note = new Note(3L, "Old", "Old content");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(noteRepository.findByIdAndUserId(9L, 3L)).thenReturn(Optional.of(note));

        noteService.update("user@example.com", 9L, "New", "New content");

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(captor.capture());
        Note saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("New");
        assertThat(saved.getContent()).isEqualTo("New content");
    }

    @Test
    void deleteUsesUserScopedRepository() {
        User user = new User("user", "user@example.com", "hashed");
        user.setId(12L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        noteService.delete("user@example.com", 45L);

        verify(noteRepository).deleteByIdAndUserId(45L, 12L);
    }

    @Test
    void currentUserIdFailsWhenUserMissing() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());


        assertThatThrownBy(() -> noteService.myNotes("missing@example.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Logged user not found in DB");
    }
}