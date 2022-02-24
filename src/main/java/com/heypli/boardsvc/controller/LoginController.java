package com.heypli.boardsvc.controller;

import com.heypli.boardsvc.annotation.SocialUser;
import com.heypli.boardsvc.domain.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // 인증 성공 후 호출되는 API
    @GetMapping(value = "/loginSuccess")
    public String loginComplete(@SocialUser User user) {
        return "redirect:/board/list";
    }

}
