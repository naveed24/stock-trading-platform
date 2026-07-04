package com.tradex.api_gateway.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayStatusController {


    @GetMapping("/gateway/status")
    public String status(){
        return "Api gateway is running successfully";
    }
}
