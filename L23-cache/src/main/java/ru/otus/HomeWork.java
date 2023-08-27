package ru.otus;

import org.flywaydb.core.Flyway;
import org.openjdk.jmh.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.core.repository.executor.DbExecutorImpl;
import ru.otus.core.sessionmanager.TransactionRunnerJdbc;
import ru.otus.crm.datasource.DriverManagerDataSource;
import ru.otus.crm.model.Client;
import ru.otus.crm.service.DbServiceClientImpl;
import ru.otus.crm.service.DbServiceClientWithCache;
import ru.otus.jdbc.mapper.*;

import javax.sql.DataSource;
import java.util.LinkedList;
import java.util.List;

public class HomeWork {
    private static final String URL = "jdbc:postgresql://localhost:5400/demoDB";
    private static final String USER = "usr";
    private static final String PASSWORD = "pwd";

    private static final Logger log = LoggerFactory.getLogger(HomeWork.class);

    public static void main(String[] args) {
        cleanWeakHashMapTest();
    }

    @Benchmark
    @Measurement(iterations = 2, time = 1)
    @Warmup(iterations = 0)
    @BenchmarkMode(Mode.AverageTime)
    public static void testWithoutCache() {
        var dataSource = new DriverManagerDataSource(URL, USER, PASSWORD);
        flywayMigrations(dataSource);
        var transactionRunner = new TransactionRunnerJdbc(dataSource);
        var dbExecutor = new DbExecutorImpl();

        EntityClassMetaData<Client> entityClassMetaDataClient = new EntityClassMetaDataImpl<>(Client.class);
        EntitySQLMetaData entitySQLMetaDataClient = new EntitySQLMetaDataImpl<>(entityClassMetaDataClient);
        var dataTemplateClient = new DataTemplateJdbc<>(dbExecutor, entitySQLMetaDataClient, entityClassMetaDataClient);

        var dbServiceClient = new DbServiceClientImpl(transactionRunner, dataTemplateClient);
        Client client = dbServiceClient.saveClient(new Client("dbServiceFirst"));
        Long clientId = client.getId();
        for (int i = 0; i < 1000; i++) {
            dbServiceClient.getClient(clientId);
        }
    }

    @Benchmark
    @Measurement(iterations = 2, time = 1)
    @Warmup(iterations = 0)
    @BenchmarkMode(Mode.AverageTime)
    public static void testWithCache() {
        var dataSource = new DriverManagerDataSource(URL, USER, PASSWORD);
        flywayMigrations(dataSource);
        var transactionRunner = new TransactionRunnerJdbc(dataSource);
        var dbExecutor = new DbExecutorImpl();

        EntityClassMetaData<Client> entityClassMetaDataClient = new EntityClassMetaDataImpl<>(Client.class);
        EntitySQLMetaData entitySQLMetaDataClient = new EntitySQLMetaDataImpl<>(entityClassMetaDataClient);
        var dataTemplateClient = new DataTemplateJdbc<>(dbExecutor, entitySQLMetaDataClient, entityClassMetaDataClient); //реализация DataTemplate, универсальная

        var dbServiceClient = new DbServiceClientWithCache(transactionRunner, dataTemplateClient);
        Client client = dbServiceClient.saveClient(new Client("dbServiceFirst"));
        Long clientId = client.getId();
        for (int i = 0; i < 1000; i++) {
            dbServiceClient.getClient(clientId);
        }
    }

    public static void cleanWeakHashMapTest() {
        var dataSource = new DriverManagerDataSource(URL, USER, PASSWORD);
        flywayMigrations(dataSource);
        var transactionRunner = new TransactionRunnerJdbc(dataSource);
        var dbExecutor = new DbExecutorImpl();

        EntityClassMetaData<Client> entityClassMetaDataClient = new EntityClassMetaDataImpl<>(Client.class);
        EntitySQLMetaData entitySQLMetaDataClient = new EntitySQLMetaDataImpl<>(entityClassMetaDataClient);
        var dataTemplateClient = new DataTemplateJdbc<>(dbExecutor, entitySQLMetaDataClient, entityClassMetaDataClient);
        var dbServiceClient = new DbServiceClientWithCache(transactionRunner, dataTemplateClient);

        List<Integer> results = new LinkedList<>();

        for (int i = 0; i < 1000; i++) {
            dbServiceClient.saveClient(new Client("client" + i));
            if (dbServiceClient.getCacheSize() == 1) {
                results.add(i);
            }
        }


        for (Integer result : results) {
            log.info("Iteration: " + result + " WeakHashMap was cleaned");
        }
    }

    private static void flywayMigrations(DataSource dataSource) {
        log.info("db migration started...");
        var flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:/db/migration")
                .load();
        flyway.migrate();
        log.info("db migration finished.");
        log.info("***");
    }
}
