package com.wjh.heimaoai;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HeimaoAiApplication {

    public static void main(String[] args) {
        //加载.env文件
        Dotenv dotenv = Dotenv.configure()
                .directory("./src/main/resources")
                .load();

        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );

        SpringApplication.run(HeimaoAiApplication.class, args);
    }

}
