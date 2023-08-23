package ru.otus.jdbc.mapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.crm.model.Id;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EntityClassMetaDataImpl<T> implements EntityClassMetaData<T> {
  private static final Logger log = LoggerFactory.getLogger(EntityClassMetaDataImpl.class);
  private final Class<T> clazz;

  public EntityClassMetaDataImpl(Class<T> clazz) {
    this.clazz = clazz;
  }

  @Override
  public String getName() {
    return clazz.getSimpleName();
  }

  @Override
  public Constructor<T> getConstructor() {
    try {
      return clazz.getConstructor();
    } catch (NoSuchMethodException e) {
      log.error("There is not constructor on Client class. Exception: " + e.getMessage());
      return null;
    }
  }

  @Override
  public Field getIdField() {
    Optional<Field> first = Arrays.stream(clazz.getDeclaredFields()).filter(field -> field.isAnnotationPresent(Id.class)).findFirst();
    if (first.isPresent()) {
      return first.get();
    } else {
      log.error("There is not Id field on Client class.");
      return null;
    }
  }

  @Override
  public List<Field> getAllFields() {
    return Arrays.asList(clazz.getDeclaredFields());
  }

  @Override
  public List<Field> getFieldsWithoutId() {
    return getAllFields()
        .stream()
        .filter(field -> !field.equals(getIdField()))
        .collect(Collectors.toList());
  }
}
