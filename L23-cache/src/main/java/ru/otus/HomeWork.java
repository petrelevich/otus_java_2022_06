package ru.otus;

import org.flywaydb.core.Flyway;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.core.repository.executor.DbExecutorImpl;
import ru.otus.core.sessionmanager.TransactionRunnerJdbc;
import ru.otus.crm.datasource.DriverManagerDataSource;
import ru.otus.crm.model.Client;
import ru.otus.crm.service.DbServiceClientImpl;
import ru.otus.crm.service.DbServiceClientWithCache;
import ru.otus.jdbc.mapper.DataTemplateJdbc;
import ru.otus.jdbc.mapper.EntityClassMetaData;
import ru.otus.jdbc.mapper.EntityClassMetaDataImpl;
import ru.otus.jdbc.mapper.EntitySQLMetaData;
import ru.otus.jdbc.mapper.EntitySQLMetaDataImpl;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HomeWork {
    private static final String URL = "jdbc:postgresql://localhost:5400/demoDB";
    private static final String USER = "usr";
    private static final String PASSWORD = "pwd";

    private static final Logger log = LoggerFactory.getLogger(HomeWork.class);

    @Fork(1)
    @Measurement(iterations = 1, time = 15, timeUnit = TimeUnit.SECONDS)
    @BenchmarkMode(Mode.AverageTime)
    @Threads(1)
    public static void main(String[] args) throws IOException {
       org.openjdk.jmh.Main.main(args);
        testWithoutCache();
    }

    @Benchmark
    @Fork(1)
    @Measurement(iterations = 2, time = 1, batchSize = 1)
    @Threads(1)
    @Warmup(iterations = 0)
    @BenchmarkMode(Mode.AverageTime)
    public static void testWithoutCache() {
        var dataSource = new DriverManagerDataSource(URL, USER, PASSWORD);
        flywayMigrations(dataSource);
        var transactionRunner = new TransactionRunnerJdbc(dataSource);
        var dbExecutor = new DbExecutorImpl();

        EntityClassMetaData<Client> entityClassMetaDataClient = new EntityClassMetaDataImpl<>(Client.class);
        EntitySQLMetaData entitySQLMetaDataClient = new EntitySQLMetaDataImpl<>(entityClassMetaDataClient);
        var dataTemplateClient = new DataTemplateJdbc<>(dbExecutor, entitySQLMetaDataClient, entityClassMetaDataClient); //реализация DataTemplate, универсальная

        var dbServiceClient = new DbServiceClientImpl(transactionRunner, dataTemplateClient);
        Client clientFirst = dbServiceClient.saveClient(new Client("dbServiceFirst"));
        Long clientFirstId = clientFirst.getId();
        for(int i = 0; i < 1000; i++) {
            dbServiceClient.getClient(clientFirstId);
        }

        var clientSecond = dbServiceClient.saveClient(new Client("dbServiceSecond"));
        var clientSecondSelected = dbServiceClient.getClient(clientSecond.getId())
            .orElseThrow(() -> new RuntimeException("Client not found, id:" + clientSecond.getId()));
        log.info("clientSecondSelected:{}", clientSecondSelected);
    }

    @Benchmark
    @Fork(1)
    @Measurement(iterations = 2, time = 1, batchSize = 1)
    @Threads(1)
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
        Client clientFirst = dbServiceClient.saveClient(new Client("dbServiceFirst"));
        Long clientFirstId = clientFirst.getId();
        for(int i = 0; i < 1000; i++) {
            dbServiceClient.getClient(clientFirstId);
        }

        var clientSecond = dbServiceClient.saveClient(new Client("dbServiceSecond"));
        var clientSecondSelected = dbServiceClient.getClient(clientSecond.getId())
            .orElseThrow(() -> new RuntimeException("Client not found, id:" + clientSecond.getId()));
        log.info("clientSecondSelected:{}", clientSecondSelected);
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
