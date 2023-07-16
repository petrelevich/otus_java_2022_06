package ru.otus.jdbc.mapper;

import ru.otus.annotation.Id;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EntityClassMetaDataImpl<T> implements EntityClassMetaData<T>{
    private Class<T> clazz;
    public EntityClassMetaDataImpl(Class<T> cl)
    {
        clazz = cl;
    }

    @Override
    public String getName() {
        return clazz.getSimpleName().toLowerCase();
    }

    @Override
    public Constructor<T> getConstructor() {
        try {

            Class<?>[] classes = new Class<?>[clazz.getDeclaredFields().length];
            for (int ii = 0; ii < clazz.getDeclaredFields().length; ++ii) {
                classes[ii] = clazz.getDeclaredFields()[ii].getType();
            }
            return clazz.getConstructor(classes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Field getIdField() {
        for(Field fl: getAllFields()){
            if(fl.isAnnotationPresent(Id.class)){
                return fl;
            }
        }
        return null;
    }

    @Override
    public List<Field> getAllFields() {
        return Arrays.stream(clazz.getDeclaredFields()).toList();
    }

    @Override
    public List<Field> getFieldsWithoutId() {
        return getAllFields().stream().filter(fl->!fl.isAnnotationPresent(Id.class)).toList();
    }
}
