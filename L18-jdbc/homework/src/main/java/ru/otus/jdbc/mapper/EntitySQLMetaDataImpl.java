package ru.otus.jdbc.mapper;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

public class EntitySQLMetaDataImpl implements EntitySQLMetaData {
    private final EntityClassMetaData<?> entityClassMetaData;

    private final String SELECT_ALL = "select %s from %s";
    private final String SELECT_BY_ID = "select %s from %s where %s = ?";
    private final String INSERT = "insert into %s (%s) values (%s)";
    private final String UPDATE = "update %s set %s where %s = ?";

    private final List<Field> allFields;
    private final List<Field> fieldsWithoutId;
    private final Field idField;
    private final String className;


    public EntitySQLMetaDataImpl(EntityClassMetaData<?> entityClassMetaData) {
        this.entityClassMetaData = entityClassMetaData;
        allFields = entityClassMetaData.getAllFields();
        fieldsWithoutId = entityClassMetaData.getFieldsWithoutId();
        idField = entityClassMetaData.getIdField();
        className = entityClassMetaData.getName();

    }

    @Override
    public String getSelectAllSql() {
        return String.format(SELECT_ALL,
                allFields.stream().map(Field::getName).collect(Collectors.joining(",")),
                className);
    }

    @Override
    public String getSelectByIdSql() {

        return String.format(SELECT_BY_ID,
                allFields.stream().map(Field::getName).collect(Collectors.joining(",")),
                className,
                idField.getName());
    }

    @Override
    public String getInsertSql() {
        return String.format(INSERT,
                className,
                fieldsWithoutId.stream().map(Field::getName).collect(Collectors.joining(",")),
                fieldsWithoutId.stream().map(fld -> "?").collect(Collectors.joining(",")));
    }

    @Override
    public String getUpdateSql() {
        return String.format(UPDATE,
                className,
                fieldsWithoutId.stream().map(fld -> fld.getName() + " = ?").collect(Collectors.joining(",")),
                idField.getName());
    }

}
