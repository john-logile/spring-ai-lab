package com.example.ailab.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serves the single-page lab UI at {@code /}.
 * The page calls the REST APIs with fetch() — open the browser devtools
 * Network tab while clicking around to see the API in action.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
