package ru.otus.jdbc.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.core.repository.DataTemplate;
import ru.otus.core.repository.executor.DbExecutor;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Сохратяет объект в базу, читает объект из базы
 */
public class DataTemplateJdbc<T> implements DataTemplate<T> {

  private final DbExecutor dbExecutor;
  private final EntitySQLMetaData entitySQLMetaData;
  private final EntityClassMetaData<T> entityClassMetaData;
  private static final Logger log = LoggerFactory.getLogger(DataTemplateJdbc.class);

  public DataTemplateJdbc(DbExecutor dbExecutor, EntitySQLMetaData entitySQLMetaData, EntityClassMetaData<T> entityClassMetaData) {
    this.dbExecutor = dbExecutor;
    this.entitySQLMetaData = entitySQLMetaData;
    this.entityClassMetaData = entityClassMetaData;
  }

  @Override
  public Optional<T> findById(Connection connection, long id) {
    return dbExecutor.executeSelect(connection, entitySQLMetaData.getSelectByIdSql(), List.of(id), rs -> {
      try {
        if (rs.next()) {
          T result = entityClassMetaData.getConstructor().newInstance();
          List<Field> fields = entityClassMetaData.getAllFields();

          for (Field field : fields) {
            field.setAccessible(true);
            field.set(result, rs.getObject(field.getName()));
          }
          return result;
        }
        return null;
      } catch (Exception e) {
        log.error(e.toString());
        throw new RuntimeException(e);
      }
    });
  }

  @Override
  public List<T> findAll(Connection connection) {
    return dbExecutor.executeSelect(connection, entitySQLMetaData.getSelectAllSql(), Collections.emptyList(), rs -> {
      var results = new ArrayList<T>();
      try {
        while (rs.next()) {
          T result = entityClassMetaData.getConstructor().newInstance();
          List<Field> fields = entityClassMetaData.getAllFields();

          for (Field field : fields) {
            field.setAccessible(true);
            field.set(result, rs.getObject(field.getName()));
          }

          results.add(result);
        }
        return results;
      } catch (Exception e) {
        log.error(e.toString());
        throw new RuntimeException(e);
      }
    }).orElseThrow(() -> new RuntimeException("Unexpected error"));
  }

  @Override
  public long insert(Connection connection, T object) {
    List<Field> fields = entityClassMetaData.getFieldsWithoutId();
    List<Object> params = new ArrayList<>();
    try {
      for (Field field : fields) {
        field.setAccessible(true);
        params.add(field.get(object));
      }
    } catch (Exception e) {
      log.error(e.toString());
      throw new RuntimeException(e);
    }
    return dbExecutor.executeStatement(connection, entitySQLMetaData.getInsertSql(), params);
  }

  @Override
  public void update(Connection connection, T object) {
    List<Field> fields = entityClassMetaData.getFieldsWithoutId();
    List<Object> params = new ArrayList<>();

    try {
      for (Field field : fields) {
        field.setAccessible(true);
        params.add(field.get(object));
      }

      Field id = entityClassMetaData.getIdField();
      id.setAccessible(true);
      params.add(id.get(object));
    } catch (Exception e) {
      log.error(e.toString());
      throw new RuntimeException(e);
    }
    dbExecutor.executeStatement(connection, entitySQLMetaData.getUpdateSql(), params);
  }
}
