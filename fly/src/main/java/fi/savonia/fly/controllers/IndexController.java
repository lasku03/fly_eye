package fi.savonia.fly.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import fi.savonia.fly.domain.perimeter.model.Perimeter;
import fi.savonia.fly.domain.perimeter.model.RadarPerimeter;
import fi.savonia.fly.services.PerimeterService;

import org.springframework.ui.Model;

@Controller
public class IndexController {
    
    @Autowired
    PerimeterService perimeterService;

    @GetMapping("/")
    public ModelAndView index(Model model) {
        Perimeter currentPerimeter = RadarState.getCurrentPerimeter();
        RadarPerimeter currentRadarPerimeter = currentPerimeter == null ? null : new RadarPerimeter(currentPerimeter);
        model.addAttribute("perimeters", perimeterService.getRadarPerimeterList());
        model.addAttribute("currentPerimeter", currentRadarPerimeter);
        model.addAttribute("page", "radar");
        return new ModelAndView("index");
    }

    @GetMapping("/history")
    public ModelAndView history(Model model) {
        model.addAttribute("perimeters", perimeterService.getRadarPerimeterList());
        model.addAttribute("page", "history");
        return new ModelAndView("history");
    }
}
