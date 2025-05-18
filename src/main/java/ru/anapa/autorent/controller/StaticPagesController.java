package ru.anapa.autorent.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class StaticPagesController {

    @GetMapping("/about")
    public ModelAndView about() {
        return new ModelAndView("static/about");
    }

    @GetMapping("/contact")
    public ModelAndView contact() {
        return new ModelAndView("static/contact");
    }

    @GetMapping("/terms")
    public ModelAndView terms() {
        return new ModelAndView("static/terms");
    }
}