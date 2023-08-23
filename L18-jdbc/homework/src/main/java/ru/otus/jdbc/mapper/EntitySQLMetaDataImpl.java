package ru.otus.jdbc.mapper;

import java.lang.reflect.Field;
import java.util.List;

public class EntitySQLMetaDataImpl<T> implements EntitySQLMetaData {
  private static final String SELECT_ALL = "select * from %s";
  private static final String SELECT_BY_ID = "select %s from %s where %s  = ?";
  private static final String INSERT = "insert into %s(%s) values (%s)";
  private static final String UPDATE = "update %s set %s = ? where %s = ?";

  private final EntityClassMetaData<T> entityClassMetaData;
  private final String allFieldsName;
  private final String tableName;
  private final String allFieldWithoutIdName;
  private final String idFieldName;

  public EntitySQLMetaDataImpl(EntityClassMetaData<T> entityClassMetaData) {
    this.entityClassMetaData = entityClassMetaData;
    allFieldsName = getFieldsName(entityClassMetaData.getAllFields());
    tableName = entityClassMetaData.getName().toLowerCase();
    allFieldWithoutIdName = getFieldsName(entityClassMetaData.getFieldsWithoutId());
    idFieldName = entityClassMetaData.getIdField().getName().toLowerCase();
  }

  @Override
  public String getSelectAllSql() {
    return String.format(SELECT_ALL, tableName);
  }

  @Override
  public String getSelectByIdSql() {
    return String.format(SELECT_BY_ID, allFieldsName, tableName, idFieldName);
  }

  @Override
  public String getInsertSql() {
    return String.format(INSERT, tableName, allFieldWithoutIdName, transformFieldsToQuestionMarks());
  }

  @Override
  public String getUpdateSql() {
    return String.format(UPDATE, tableName, getFieldsNameWithQuestionMark(entityClassMetaData.getFieldsWithoutId()), idFieldName);
  }

  private String getFieldsName(List<Field> fields) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < fields.size(); i++) {
      result.append(fields.get(i).getName());
      if (i < fields.size() - 1) {
        result.append(", ");
      }
    }
    return result.toString();
  }

  private String transformFieldsToQuestionMarks() {
    return allFieldWithoutIdName.replaceAll("[^,]", "").replace(",", "?,").concat("?");
  }

  private String getFieldsNameWithQuestionMark(List<Field> fields) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < fields.size(); i++) {
      result.append(fields.get(i).getName());
      result.append(" = ?");
      if (i < fields.size() - 1) {
        result.append(", ");
      }
    }
    return result.toString();
  }
}
