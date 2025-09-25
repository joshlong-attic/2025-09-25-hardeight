package com.example.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClient;

import java.util.function.Function;

@SpringBootApplication
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }


    interface Announcer {
        String message(int x);
    }

    void foo() {
        Announcer a = _ -> "hi";
        Function<String, Integer> func1 = String::length;

        var func = new Function<String, Integer>() {
            @Override
            public Integer apply(String s) {
                return s.length();
            }
        };

    }
    //    Executor executor = Executors.newVirtualThreadPerTaskExecutor();

}

@Controller
@ResponseBody
class HelloController {

    private final RestClient http;

    HelloController(RestClient.Builder http) {
        this.http = http.build();
    }

    @GetMapping("/delay")
    String delay() {
        var msg = Thread.currentThread() + " :: ";
        var reply = this.http
                .get()
                .uri("http://localhost:80/delay/5")
                .retrieve()
                .body(String.class);
        msg += Thread.currentThread();
        System.out.println(msg);
        return reply;
    }
}