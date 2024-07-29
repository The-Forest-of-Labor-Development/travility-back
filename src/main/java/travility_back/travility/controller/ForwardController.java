package travility_back.travility.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ForwardController {
    @GetMapping(value = {"/", "/login", "/signup", "/main", "/dashboard/**", "/accountbook/**", "/settlement/**", "/forgot-password", "/admin/**", "/loading"})
    public String forward() {
        return "forward:/index.html";
    }
}
