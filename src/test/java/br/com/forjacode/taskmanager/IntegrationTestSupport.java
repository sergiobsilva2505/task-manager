package br.com.forjacode.taskmanager;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SuppressWarnings("java:S2187")
public abstract class IntegrationTestSupport {

    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16")
            .withReuse(true);

    static {
        postgres.start();
    }
}