package com.melodymart.controller;

import com.melodymart.service.TickerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private TickerService tickerService;

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        model.addAttribute("tickerConfig", tickerService.getTickerConfig());
    }
}
