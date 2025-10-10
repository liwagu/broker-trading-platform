package io.github.liwagu.trading;

import io.github.liwagu.trading.repository.BuyingPowerEntity;
import io.github.liwagu.trading.repository.BuyingPowerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner init(BuyingPowerRepository buyingPowerRepository) {
        return args -> {
            buyingPowerRepository.save(new BuyingPowerEntity("portfolio-id-1", new BigDecimal("5000.00")));
            buyingPowerRepository.save(new BuyingPowerEntity("portfolio-id-2", new BigDecimal("5000.00")));
            buyingPowerRepository.save(new BuyingPowerEntity("portfolio-id-3", new BigDecimal("5000.00")));
            buyingPowerRepository.save(new BuyingPowerEntity("portfolio-id-4", new BigDecimal("5000.00")));
        };
    }
}
