package com.miro.hw.artexnet.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ApiDocsController {

    @RequestMapping("/api-docs")
    public String getDocs() {
        return "redirect:/swagger-ui.html";
    }

}
