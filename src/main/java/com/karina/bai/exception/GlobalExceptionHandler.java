package com.karina.bai.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        String path = request.getRequestURI();

        //  Rejestracja -> register.html z błędem
        if ("/register".equals(path)) {
            ModelAndView mv = new ModelAndView("register");
            mv.addObject("error", ex.getMessage());
            return mv;
        }
        
        if ("/login".equals(path)) {
            ModelAndView mv = new ModelAndView("login");
            mv.addObject("error", ex.getMessage());
            return mv;
        }

        //Domyślnie: bezpieczna strona błędu
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("error", ex.getMessage());
        return mv;
    }

    @ExceptionHandler(SecurityException.class)
    public ModelAndView handleSecurity(SecurityException ex) {
        ModelAndView mv = new ModelAndView("403");
        mv.addObject("error", "Brak dostępu.");
        return mv;
    }
}
