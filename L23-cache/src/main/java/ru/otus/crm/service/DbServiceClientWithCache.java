package ru.otus.crm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.cachehw.HwCache;
import ru.otus.cachehw.HwListener;
import ru.otus.cachehw.MyCache;
import ru.otus.core.repository.DataTemplate;
import ru.otus.core.sessionmanager.TransactionRunner;
import ru.otus.crm.model.Client;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class DbServiceClientWithCache implements DBServiceClient {
  private static final Logger log = LoggerFactory.getLogger(ru.otus.crm.service.DbServiceClientImpl.class);

  private final DataTemplate<Client> dataTemplate;
  private final TransactionRunner transactionRunner;
  private final HwCache<Long, Client> cache;

  public DbServiceClientWithCache(TransactionRunner transactionRunner, DataTemplate<Client> dataTemplate) {
    this.transactionRunner = transactionRunner;
    this.dataTemplate = dataTemplate;
    cache = new MyCache<>();
    HwListener<Long, Client> listener = (key, value, action) -> log.info("key:{}, value:{}, action: {}", key, value, action);
    cache.addListener(listener);
  }

  @Override
  public Client saveClient(Client client) {
    return transactionRunner.doInTransaction(connection -> {
      if (client.getId() == null) {
        var clientId = dataTemplate.insert(connection, client);
        var createdClient = new Client(clientId, client.getName());
        log.info("created client: {}", createdClient);

        cache.put(clientId, createdClient);

        return createdClient;
      }
      dataTemplate.update(connection, client);
      log.info("updated client: {}", client);

      cache.remove(client.getId());

      return client;
    });
  }

  @Override
  public Optional<Client> getClient(long id) {
    try {
      return Optional.of(cache.get(id));
    } catch (NoSuchElementException e) {
      return transactionRunner.doInTransaction(connection -> {
        var clientOptional = dataTemplate.findById(connection, id);
        log.info("client: {}", clientOptional);

        if (clientOptional.isPresent()) {
          Client client = clientOptional.get();
          cache.put(client.getId(), client);
        }

        return clientOptional;
      });
    }
  }

  @Override
  public List<Client> findAll() {
    return transactionRunner.doInTransaction(connection -> {
      var clientList = dataTemplate.findAll(connection);
      log.info("clientList:{}", clientList);
      return clientList;
    });
  }
}
