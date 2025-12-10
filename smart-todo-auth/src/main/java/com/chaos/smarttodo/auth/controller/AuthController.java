package com.chaos.smarttodo.auth.controller;

import com.chaos.smarttodo.common.result.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/hello")
    public R<String> hello() {
        return R.success("Hello from Auth Service (8010)");
    }
}