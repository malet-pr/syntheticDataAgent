package org.acme.syntheticdata;

import org.springframework.boot.SpringApplication;

public class TestSyntheticDataApplication {

    public static void main(String[] args) {
        SpringApplication.from(SyntheticDataApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
