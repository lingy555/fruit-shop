package com.lingnan.fruitshop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lingnan.fruitshop.mapper")
public class FruitShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(FruitShopApplication.class, args);
    }

}
