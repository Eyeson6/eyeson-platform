package com.eyeson.startrun;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartRun implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        System.out.println("-----启动就执行这个方法------");
    }
}
