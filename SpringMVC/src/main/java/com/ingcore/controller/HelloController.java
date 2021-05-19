package com.ingcore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/user")
public class HelloController {

    @RequestMapping("/login")
    public String login(){
        return "/login";
    }

    @ResponseBody
    @RequestMapping("/login2")
    public String login2(Integer id){
        return "1";
    }
}
