package com.karina.bai.model.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterFormValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validRegisterFormPasses() {
        RegisterForm form = new RegisterForm();
        form.setUsername("user123");
        form.setEmail("user@example.com");
        form.setPassword("Strong!1A");

        Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);

        assertThat(violations).isEmpty();
    }

    @Test
    void usernameTooShortIsRejected() {
        RegisterForm form = new RegisterForm();
        form.setUsername("ab");
        form.setEmail("user@example.com");
        form.setPassword("Strong!1A");

        Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
    }

    @Test
    void invalidEmailIsRejected() {
        RegisterForm form = new RegisterForm();
        form.setUsername("user123");
        form.setEmail("not-an-email");
        form.setPassword("Strong!1A");

        Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void blankPasswordIsRejected() {
        RegisterForm form = new RegisterForm();
        form.setUsername("user123");
        form.setEmail("user@example.com");
        form.setPassword(" ");

        Set<ConstraintViolation<RegisterForm>> violations = validator.validate(form);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }
}