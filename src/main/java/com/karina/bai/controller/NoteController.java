package com.karina.bai.controller;

import com.karina.bai.model.Note;
import com.karina.bai.model.dto.NoteForm;
import com.karina.bai.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public String list(Model model, Principal principal) {
        List<Note> notes = noteService.myNotes(principal.getName()); // email
        model.addAttribute("notes", notes);
        return "notes/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("form", new NoteForm());
        return "notes/create";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") NoteForm form,
                         BindingResult br,
                         Principal principal,
                         RedirectAttributes ra) {

        if (br.hasErrors()) {
            return "notes/create";
        }

        noteService.create(principal.getName(), form.getTitle(), form.getContent());

        ra.addFlashAttribute("msg", "saved");

        return "redirect:/user/notes";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, Principal principal) {
        Note n = noteService.getMineOrThrow(principal.getName(), id);

        NoteForm form = new NoteForm();
        form.setTitle(n.getTitle());
        form.setContent(n.getContent());

        model.addAttribute("noteId", id);
        model.addAttribute("form", form);

        return "notes/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") NoteForm form,
                         BindingResult br,
                         Principal principal,
                         Model model,
                         RedirectAttributes ra) {

        if (br.hasErrors()) {
            model.addAttribute("noteId", id);
            return "notes/edit";
        }

        noteService.update(principal.getName(), id, form.getTitle(), form.getContent());

        ra.addFlashAttribute("msg", "saved");
        return "redirect:/user/notes";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal, RedirectAttributes ra) {
        noteService.delete(principal.getName(), id);

        ra.addFlashAttribute("msg", "🗑 deleted");
        return "redirect:/user/notes";
    }
}
