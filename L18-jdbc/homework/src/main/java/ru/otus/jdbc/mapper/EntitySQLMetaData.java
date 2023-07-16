package ru.otus.jdbc.mapper;

import java.lang.reflect.Constructor;

/**
 * Создает SQL - запросы
 */
public interface EntitySQLMetaData {
    EntityClassMetaData<?> getEntityClassMetaData();
    String getSelectAllSql();

    String getSelectByIdSql();

    String getInsertSql();

    String getUpdateSql();
}
