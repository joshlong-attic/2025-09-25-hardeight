package com.example.boot4;

import org.springframework.beans.factory.BeanRegistrar;
import org.springframework.beans.factory.BeanRegistry;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Import(MyBeanRegistrar.class)
@EnableScheduling
//@EnableAsync
@EnableResilientMethods
@SpringBootApplication
public class Boot4Application {

    public static void main(String[] args) {
        SpringApplication.run(Boot4Application.class, args);
    }

    @Bean
    ApplicationRunner riskRunner(ResilienceDemo resilienceDemo) {
        return _ -> IO.println(resilienceDemo.flaky());
    }

    //@Bean
    ApplicationRunner concurrencyLimitApplicationRunner(
            ResilienceDemo demo, SimpleAsyncTaskScheduler executor) {
        return args -> {
            executor.scheduleAtFixedRate(demo::debug, Duration.ofMillis(1500));
            for (var i = 0; i < 1000; i++)
                executor.execute(() -> {
                    try {
                        demo.write();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        };
    }

}

class Oops extends Exception {
}

@Component
class ResilienceDemo {

    private final List<String> threads = new CopyOnWriteArrayList<>();

    private final AtomicInteger counter = new AtomicInteger(0);

    @Retryable(includes = {Oops.class}, maxAttempts = 6)
    String flaky() throws Oops {
        // todo call some exgernal service that might fail
        var msg = "call # " + counter.incrementAndGet();
        IO.println(msg);
        if (counter.get() < 5)
            throw new Oops();

        return "result";

    }

    void debug() {
        StringBuilder msg = new StringBuilder("-----------------");
        for (var thread : threads) {
            msg.append(System.lineSeparator()).append(thread).append(System.lineSeparator());
        }
        IO.println(msg.toString());
    }

    @ConcurrencyLimit(5)
    void write() throws Exception {
        Thread.sleep(5000);
        this.threads.add(Thread.currentThread().getName());
    }


}


record Dog(int id, String name, String description) {
}

@Controller
@ResponseBody
class AgingApiController {

    private final Set<Dog> dogs = Set.of(new Dog(1, "A", "a description"),
            new Dog(2, "B", "b description"));

    @GetMapping(value = "/dogs", version = "1.0")
    Collection<Map<String, Object>> dogsLegacy() {
        return this.dogs.stream()
                .map(dog -> Map.of("name", (Object) dog.name()))
                .toList();
    }

    @GetMapping(value = "/dogs", version = "1.1")
    Collection<Dog> dogs() {
        return this.dogs;
    }
}
/*
@Component
class JmsDemo implements ApplicationRunner {

    private final JmsClient jms;

    JmsDemo(ConnectionFactory connectionFactory) {
        this.jms = JmsClient.builder(connectionFactory)
                .build();
    }

    @JmsListener(destination = "test")
    void on(String msg) {
        IO.println("got a message: " + msg);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.jms.destination("test").send("hello");
    }
}
*/


class Vet {
}

class MyBeanRegistrar implements BeanRegistrar {

    @Override
    public void register(BeanRegistry registry, Environment env) {
        registry.registerBean("vet", Vet.class);
        for (var i = 0; i < 1000; i++) {
            var catName = "cat" + i;
            registry.registerBean(catName, CatService.class,
                    catServiceSpec -> catServiceSpec.supplier(
                            ctx -> new CatService(ctx.bean(Vet.class), catName)));
        }
    }
}

class CatService {

    CatService(Vet vet, String name) {
        IO.println("cats " + name + " with vet");
    }
}

// todo
// X | api versioning
// X | jmsclient
// X | modularized starters
// X | resilience methods
// beanregistrars

