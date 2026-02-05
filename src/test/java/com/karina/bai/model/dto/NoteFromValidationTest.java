package com.karina.bai.model.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class NoteFormValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validNoteFormPasses() {
        NoteForm form = new NoteForm();
        form.setTitle("My note");
        form.setContent("Content");

        Set<ConstraintViolation<NoteForm>> violations = validator.validate(form);

        assertThat(violations).isEmpty();
    }

    @Test
    void blankTitleIsRejected() {
        NoteForm form = new NoteForm();
        form.setTitle(" ");
        form.setContent("Content");

        Set<ConstraintViolation<NoteForm>> violations = validator.validate(form);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("title"));
    }

    @Test
    void contentTooLongIsRejected() {
        NoteForm form = new NoteForm();
        form.setTitle("Valid title");
        form.setContent("a".repeat(6000));

        Set<ConstraintViolation<NoteForm>> violations = validator.validate(form);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("content"));
    }
}