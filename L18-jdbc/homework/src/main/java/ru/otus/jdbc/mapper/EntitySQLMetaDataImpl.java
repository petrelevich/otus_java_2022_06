package ru.otus.jdbc.mapper;

import java.util.stream.Collectors;

public class EntitySQLMetaDataImpl implements EntitySQLMetaData {
    private final EntityClassMetaData<?> entityClassMetaData;

    public EntitySQLMetaDataImpl(EntityClassMetaData<?> entityClassMetaData) {
        this.entityClassMetaData = entityClassMetaData;
    }

    public EntityClassMetaData<?> getEntityClassMetaData() {
        return entityClassMetaData;
    }

    @Override
    public String getSelectAllSql() {
        return String.format("select %s from %s",
                entityClassMetaData.getAllFields().stream().map(fl -> fl.getName()).collect(Collectors.joining(",")),
                entityClassMetaData.getName());
    }

    @Override
    public String getSelectByIdSql() {
        return String.format("select %s from %s where %s = ?",
                entityClassMetaData.getAllFields().stream().map(fl -> fl.getName()).collect(Collectors.joining(",")),
                entityClassMetaData.getName(),
                entityClassMetaData.getIdField().getName());
    }

    @Override
    public String getInsertSql() {
        return String.format("insert into %s (%s) values (%s)",
                entityClassMetaData.getName(),
                entityClassMetaData.getFieldsWithoutId().stream().map(fl -> fl.getName()).collect(Collectors.joining(",")),
                entityClassMetaData.getFieldsWithoutId().stream().map(fl -> "?").collect(Collectors.joining(",")));
    }

    @Override
    public String getUpdateSql() {
        return String.format("update %s set %s where %s = ?",
                entityClassMetaData.getName(),
                entityClassMetaData.getFieldsWithoutId().stream().map(fl -> fl.getName() + " = ?").collect(Collectors.joining(",")),
                entityClassMetaData.getIdField().getName());
    }

}
