package ru.otus.jdbc.mapper;

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
    private final EntityClassMetaData entityClassMetaData;
    private final List<Field> fieldsWithOutId;

    public DataTemplateJdbc(DbExecutor dbExecutor, EntitySQLMetaData entitySQLMetaData, EntityClassMetaData<T> entityClassMetaData) {
        this.dbExecutor = dbExecutor;
        this.entitySQLMetaData = entitySQLMetaData;
        this.entityClassMetaData = entityClassMetaData;
        fieldsWithOutId = entityClassMetaData.getFieldsWithoutId();
    }

    @Override
    public Optional<T> findById(Connection connection, long id) {
        return dbExecutor.executeSelect(connection, entitySQLMetaData.getSelectByIdSql(), List.of(id), rs -> {
            try {
                if (rs.next()) {
                    T inst = (T) entityClassMetaData.getConstructor().newInstance();
                    for (Field field : inst.getClass().getFields()) {
                        boolean flag = true;
                        if (!field.canAccess(inst)) {
                            flag = false;
                            field.setAccessible(true);
                        }
                        field.set(inst, rs.getObject(field.getName()));
                        field.setAccessible(flag);
                    }

                    return inst;
                }
                throw new RuntimeException("Object not found.");
            } catch (Exception e) {
                throw new UnsupportedOperationException();
            }
        });
    }

    @Override
    public List<T> findAll(Connection connection) {

        return dbExecutor.executeSelect(connection, entitySQLMetaData.getSelectAllSql(), Collections.emptyList(), rs -> {
            var clientList = new ArrayList<T>();
            try {
                while (rs.next()) {
                    T inst = (T) entityClassMetaData.getConstructor().newInstance();

                    for (Field field : inst.getClass().getFields()) {
                        boolean flag = true;
                        if (!field.canAccess(inst)) {
                            flag = false;
                            field.setAccessible(true);
                        }
                        field.set(inst, rs.getObject(field.getName()));
                        field.setAccessible(flag);
                    }

                    clientList.add(inst);
                }
                return clientList;
            } catch (Exception e) {
                e.printStackTrace();
                throw new UnsupportedOperationException();
            }
        }).orElseThrow(() -> new RuntimeException("Unexpected error"));

    }


    @Override
    public long insert(Connection connection, T object) {
        List<Object> params = new ArrayList<>();

        try {
            for (Field field : fieldsWithOutId) {
                boolean flag = true;
                if (!field.isAccessible()) {
                    flag = false;
                    field.setAccessible(true);
                }
                params.add(field.get(object));
                field.setAccessible(flag);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnsupportedOperationException();
        }
        return dbExecutor.executeStatement(connection, entitySQLMetaData.getInsertSql(), params);
    }

    @Override
    public void update(Connection connection, T object) {

        List<Object> params = new ArrayList<>();

        try {
            for (Field field : fieldsWithOutId) {
                boolean flag = true;
                if (!field.isAccessible()) {
                    flag = false;
                    field.setAccessible(true);
                }
                params.add(field.get(object));
                field.setAccessible(flag);
            }

            Field fieldId = entityClassMetaData.getIdField();
            boolean flag = true;
            if (!fieldId.isAccessible()) {
                flag = false;
                fieldId.setAccessible(true);
            }
            params.add(fieldId.get(object));
            fieldId.setAccessible(flag);
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnsupportedOperationException();
        }
        dbExecutor.executeStatement(connection, entitySQLMetaData.getUpdateSql(), params);
        throw new UnsupportedOperationException();
    }
}




