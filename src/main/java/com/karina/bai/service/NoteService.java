package com.karina.bai.service;

import com.karina.bai.model.Note;
import com.karina.bai.model.User;
import com.karina.bai.repository.NoteRepository;
import com.karina.bai.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NoteService {

    private final NoteRepository noteRepo;
    private final UserRepository userRepo;

    public NoteService(NoteRepository noteRepo, UserRepository userRepo) {
        this.noteRepo = noteRepo;
        this.userRepo = userRepo;
    }

    private Long currentUserId(String email) {
        User u = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Logged user not found in DB"));
        return u.getId();
    }

    public List<Note> myNotes(String email) {
        return noteRepo.findAllByUserIdOrderByCreatedAtDesc(currentUserId(email));
    }

    @Transactional
    public Note create(String email, String title, String content) {
        Long uid = currentUserId(email);
        return noteRepo.save(new Note(uid, title, content));
    }

    public Note getMineOrThrow(String email, Long noteId) {
        Long uid = currentUserId(email);
        return noteRepo.findByIdAndUserId(noteId, uid)
                .orElseThrow(() -> new SecurityException("Access denied or note not found"));
    }

    @Transactional
    public void update(String email, Long noteId, String title, String content) {
        Note n = getMineOrThrow(email, noteId);
        n.setTitle(title);
        n.setContent(content);
        noteRepo.save(n);
    }

    @Transactional
    public void delete(String email, Long noteId) {
        Long uid = currentUserId(email);
        noteRepo.deleteByIdAndUserId(noteId, uid);
    }
}
