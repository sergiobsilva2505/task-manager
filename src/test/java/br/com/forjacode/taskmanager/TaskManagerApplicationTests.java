package br.com.forjacode.taskmanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class TaskManagerApplicationTests extends IntegrationTestSupport {

    /**
     * Smoke test que verifica a inicialização completa da aplicação Spring Boot com Testcontainers.
     * Este teste garante que o contexto da aplicação é carregado com sucesso, o container PostgreSQL
     * é iniciado corretamente e todas as dependências são resolvidas. A validação ocorre implicitamente
     * através das anotações @SpringBootTest e @Testcontainers, que lançam exceção em caso de falha
     * na configuração, injeção de dependências ou inicialização de componentes.
     */
    @Test
    void contextLoads() {
    }

}
