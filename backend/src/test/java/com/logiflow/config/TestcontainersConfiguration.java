package com.logiflow.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;


@SpringBootTest
@Testcontainers
public abstract class TestcontainersConfiguration {

    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    @ServiceConnection
    static final MongoDBContainer mongo = new MongoDBContainer("mongo:latest");

    static {
        mongo.start();
        postgres.start();
    }
}
