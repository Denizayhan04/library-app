package com.library.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Kullanıcı adı veya şifre hatalı!");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Başarıyla çıkış yaptınız.");
        }
        return "auth/login";
    }
}
