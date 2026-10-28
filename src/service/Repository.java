package service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import model.annotation.Id;

public final class Repository<T> {

    private final HashMap<String, T> entities = new HashMap<>();

    public T save(T entity) {
        Objects.requireNonNull(entity, "Entity cannot be null.");
        entities.put(findId(entity), entity);
        return entity;
    }

    @SafeVarargs
    public final void saveAll(T... items) {
        Objects.requireNonNull(items, "Items cannot be null.");
        for (T item : items) {
            save(item);
        }
    }

    public T findById(String id) {
        return entities.get(id);
    }

    public ArrayList<T> findAll() {
        return new ArrayList<>(entities.values());
    }

    public boolean delete(String id) {
        return entities.remove(id) != null;
    }

    public static <N extends Number> double average(N[] values) {
        Objects.requireNonNull(values, "Values cannot be null.");
        if (values.length == 0) {
            throw new IllegalArgumentException("Cannot calculate the average of an empty array.");
        }

        double sum = 0;
        for (N value : values) {
            sum += Objects.requireNonNull(value, "Values cannot contain null.").doubleValue();
        }
        return sum / values.length;
    }

    private String findId(T entity) {
        Field idField = null;
        for (Class<?> type = entity.getClass(); type != null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    if (idField != null) {
                        throw new IllegalArgumentException(
                                "Entity type has more than one @Id field: " + entity.getClass().getName()
                        );
                    }
                    idField = field;
                }
            }
        }

        if (idField == null) {
            throw new IllegalArgumentException(
                    "Entity type has no @Id field: " + entity.getClass().getName()
            );
        }

        try {
            idField.setAccessible(true);
            Object value = idField.get(entity);
            if (value == null) {
                throw new IllegalArgumentException("@Id field cannot be null: " + idField.getName());
            }
            return value.toString();
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access @Id field: " + idField.getName(), e);
        }
    }
}
