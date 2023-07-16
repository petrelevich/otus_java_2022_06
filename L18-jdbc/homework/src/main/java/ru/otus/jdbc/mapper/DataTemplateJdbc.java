package ru.otus.jdbc.mapper;

import ru.otus.core.repository.DataTemplate;
import ru.otus.core.repository.DataTemplateException;
import ru.otus.core.repository.executor.DbExecutor;
import ru.otus.crm.model.Client;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
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

    public DataTemplateJdbc(DbExecutor dbExecutor, EntitySQLMetaData entitySQLMetaData) {
        this.dbExecutor = dbExecutor;
        this.entitySQLMetaData = entitySQLMetaData;
    }

    @Override
    public Optional<T> findById(Connection connection, long id) {
        return dbExecutor.executeSelect(connection, entitySQLMetaData.getSelectByIdSql(), List.of(id), rs -> {
            List<T> res = getObjects(rs);
            if(res.size() == 0){
                return null;
            }else{
                return res.get(0);
            }
        });
    }

    private List<T> getObjects(ResultSet rs) {
        List<T> result = new ArrayList<>();
        ResultSetMetaData resultSetMetaData = null;
        try {
            resultSetMetaData = rs.getMetaData();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        final int columnCount;
        try {
            columnCount = resultSetMetaData.getColumnCount();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            while (rs.next()) {
                Object[] values = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    try {
                        values[i - 1] = rs.getObject(i);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
                result.add((T) entitySQLMetaData.getEntityClassMetaData().getConstructor().newInstance(values));
            }
        } catch (SQLException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    @Override
    public List<T> findAll(Connection connection) {
        return dbExecutor.executeSelect(connection, entitySQLMetaData.getSelectAllSql(), Collections.emptyList(), rs -> {
            return getObjects(rs);
        }).orElse(new ArrayList<>());
    }

    @Override
    public long insert(Connection connection, T client) {
        return dbExecutor.executeStatement(connection, entitySQLMetaData.getInsertSql(), getValuesForInsert(client));
    }

    @Override
    public void update(Connection connection, T client) {
        dbExecutor.executeStatement(connection, entitySQLMetaData.getUpdateSql(), getValuesForUpdate(client));
    }

    private List<Object> getValuesForInsert(T client) {
        List<Object> result = new ArrayList<>();
        boolean accessible;
        for (Field fieldData: entitySQLMetaData.getEntityClassMetaData().getFieldsWithoutId()){
            try {
                accessible = fieldData.canAccess(client);
                fieldData.setAccessible(true);
                result.add(fieldData.get(client));
                fieldData.setAccessible(accessible);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    private List<Object> getValuesForUpdate(T client) {
        List<Object> result = new ArrayList<>();
        boolean accessible;
        try {
            for (Field fieldData: entitySQLMetaData.getEntityClassMetaData().getFieldsWithoutId()){
                accessible = fieldData.canAccess(client);
                fieldData.setAccessible(true);
                result.add(fieldData.get(client));
                fieldData.setAccessible(accessible);
            }
            Field fieldId = entitySQLMetaData.getEntityClassMetaData().getIdField();
            accessible = fieldId.canAccess(client);
            fieldId.setAccessible(true);
            result.add(fieldId.get(client));
            fieldId.setAccessible(accessible);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}
