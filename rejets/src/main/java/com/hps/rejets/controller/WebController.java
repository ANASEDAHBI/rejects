package com.hps.rejets.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebController {

    @RequestMapping(value = "/{path:[^\\.]*}") // Redirige toutes les routes sauf celles contenant un point (fichiers statiques)
    public String redirect() {
        return "forward:/index.html";
    }
}