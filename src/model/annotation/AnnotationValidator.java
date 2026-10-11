package model.annotation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class AnnotationValidator {

    public static String[] validate(Object obj) {

        if (obj == null) {
            return new String[] { "Object cannot be null" };
        }

        List<String> errors = new ArrayList<>();
        Class<?> type = obj.getClass();

        while (type != null) {
            for (Field field : type.getDeclaredFields()) {
                field.setAccessible(true);

                if (field.isAnnotationPresent(Positive.class)) {
                    try {
                        Object value = field.get(obj);
                        if (value instanceof Number number && number.doubleValue() <= 0) {
                            errors.add(field.getName() + " " + field.getAnnotation(Positive.class).message());
                        }
                    } catch (IllegalAccessException e) {
                        errors.add("Unable to access " + field.getName());
                    }
                }

                if (field.isAnnotationPresent(MaxLength.class)) {
                    try {
                        Object value = field.get(obj);
                        if (value instanceof CharSequence sequence) {
                            int maxLength = field.getAnnotation(MaxLength.class).value();
                            if (sequence.length() > maxLength) {
                                errors.add(field.getName() + " length must be <= " + maxLength);
                            }
                        }
                    } catch (IllegalAccessException e) {
                        errors.add("Unable to access " + field.getName());
                    }
                }
            }
            type = type.getSuperclass();
        }

        return errors.toArray(new String[0]);
    }
}
