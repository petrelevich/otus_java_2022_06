package ru.otus.jdbc.mapper;

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
