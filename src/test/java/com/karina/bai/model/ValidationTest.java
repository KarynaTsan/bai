package com.karina.bai.model;

import com.karina.bai.model.dto.NoteForm;
import com.karina.bai.model.dto.RegisterForm;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void registerFormRejectsInvalidValues() {
        RegisterForm form = new RegisterForm();
        form.setUsername("ab");
        form.setEmail("not-an-email");
        form.setPassword("");

        Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);

        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("username"))
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"))
                .anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    void registerFormAcceptsValidValues() {
        RegisterForm form = new RegisterForm();
        form.setUsername("validUser");
        form.setEmail("user@example.com");
        form.setPassword("Valid1!Pass");

        Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);

        assertThat(violations).isEmpty();
    }

    @Test
    void noteFormValidatesTitleAndContent() {
        NoteForm form = new NoteForm();
        form.setTitle("ab");
        form.setContent("");

        Set<ConstraintViolation<NoteForm>> violations = validator.validate(form);

        assertThat(violations).isNotEmpty();
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("title"))
                .anyMatch(v -> v.getPropertyPath().toString().equals("content"));
    }
}