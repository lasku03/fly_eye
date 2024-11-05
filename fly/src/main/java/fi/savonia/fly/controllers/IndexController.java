package fi.savonia.fly.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import fi.savonia.fly.services.PerimeterService;

import org.springframework.ui.Model;

@Controller
public class IndexController {
    
    @Autowired
    PerimeterService perimeterService;

    @GetMapping("/")
    public ModelAndView index(Model model) {
        model.addAttribute("perimeters", perimeterService.getRadarPerimeterList());
        return new ModelAndView("radar");
    }
}