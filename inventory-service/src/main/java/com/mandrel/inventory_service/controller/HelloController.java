package com.mandrel.inventory_service.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {
    
    @RequestMapping("/")
    @ResponseBody
    public String hello() {
        return "Inventory Service";
    }
}
