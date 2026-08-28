package kore.backend.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/test")
@RestController
@RequiredArgsConstructor
public class TestController {

    private final RabbitTemplate rabbitTemplate;

    @Value("${spring.rabbitmq.queue.name}")
    private String QUEUE_NAME;

    @GetMapping
    public String test() {

        rabbitTemplate.convertAndSend("", QUEUE_NAME, "Test message from TestController");

        return "Test OK";
    }

}
