package ru.otus.jdbc.mapper;


import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

public class EntitySQLMetaDataImpl<T> implements EntitySQLMetaData {
    private final EntityClassMetaData<T> entityClassMetaData;
    private final String fieldsList;
    private final String fieldsListWoId;

    public EntitySQLMetaDataImpl(EntityClassMetaData<T> entityClassMetaDataClient) {
        this.entityClassMetaData = entityClassMetaDataClient;
        List<Field> fields = entityClassMetaDataClient.getAllFields();
        this.fieldsList = fields.stream()
                .map(Field::getName)
                .collect(Collectors.joining(", "));

        List<Field> fieldsWoId = entityClassMetaDataClient.getFieldsWithoutId();

        this.fieldsListWoId = fieldsWoId.stream()
                .map(Field::getName)
                .collect(Collectors.joining(", "));
    }

    @Override
    public String getSelectAllSql() {
        return "SELECT " + fieldsList + " FROM " + entityClassMetaData.getName();
    }

    @Override
    public String getSelectByIdSql() {
        return "SELECT " + fieldsList + " FROM " + entityClassMetaData.getName() + " WHERE " +
                entityClassMetaData.getIdField().getName() + " = ?";
    }

    @Override
    public String getInsertSql() {
       return "INSERT INTO " + entityClassMetaData.getName() + "(" + fieldsListWoId + ") VALUES(" +
               fieldsListWoId.replaceAll("[^,]", "").replace(",", "?,").concat("?") + ")";
    }

    @Override
    public String getUpdateSql() {
        List<Field> fields = entityClassMetaData.getFieldsWithoutId();

        String fieldsList = fields.stream()
                .map(Field::getName)
                .collect(Collectors.joining(" = ?, "));
        fieldsList = fieldsList.concat(" = ?");
        return "UPDATE " + entityClassMetaData.getName() + " SET " + fieldsList + " WHERE " + entityClassMetaData.getIdField().getName() + " = ?";
    }
}
