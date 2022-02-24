package com.heypli.boardsvc.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthorityTestController {

    @GetMapping("/kakao")
    public String kakao() {
        return "kakao";
    }
}
