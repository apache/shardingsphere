package io.shardingjdbc.spring.boot.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;

import io.shardingjdbc.core.exception.ShardingJdbcException;

public class PropertyUtil {

    private static int SPRING_BOOT_VERSION = 1;

    static {
        try {
            Class.forName("org.springframework.boot.bind.RelaxedPropertyResolver");
        } catch (Exception e) {
            SPRING_BOOT_VERSION = 2;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T handle(Environment environment, String prefix, Class<T> targetClass) {
        switch (SPRING_BOOT_VERSION) {
        case 1:
            return (T) v1(environment, prefix, targetClass);
        default:
            return (T) v2(environment, prefix, targetClass);
        }
    }

    /**
     * Spring Boot 1.x
     * @code {
     * RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(environment);
     * return resolver.getSubProperties(prefix);
     * }
     */
    private static Object v1(Environment environment, String prefix, Class<?> targetClass) {
        try {
            Class<?> resolverClass = Class.forName("org.springframework.boot.bind.RelaxedPropertyResolver");
            Constructor<?> resolverConstructor = resolverClass.getDeclaredConstructor(PropertyResolver.class);
            Method getSubPropertiesMethod = resolverClass.getDeclaredMethod("getSubProperties", String.class);
            Object resolverObject = resolverConstructor.newInstance(environment);
            prefix = prefix.endsWith(".") ? prefix : prefix + ".";
            return getSubPropertiesMethod.invoke(resolverObject, prefix);
        } catch (Exception e) {
            throw new ShardingJdbcException(e.getMessage(), e);
        }
    }

    /**
     * Spring Boot 2.x
     * @code {
     * Binder binder = Binder.get(environment);
     * return binder.bind(prefix, targetClass).get();
     * }
     */
    private static Object v2(Environment environment, String prefix, Class<?> targetClass) {
        try {
            Class<?> binderClass = Class.forName("org.springframework.boot.context.properties.bind.Binder");
            Method getMethod = binderClass.getDeclaredMethod("get", Environment.class);
            Method bindMethod = binderClass.getDeclaredMethod("bind", String.class, Class.class);
            Object binderObject = getMethod.invoke(null, environment);
            prefix = prefix.endsWith(".") ? prefix.substring(0, prefix.length() - 1) : prefix;
            Object bindResultObject = bindMethod.invoke(binderObject, prefix, targetClass);
            Method resultGetMethod = bindResultObject.getClass().getDeclaredMethod("get");
            return resultGetMethod.invoke(bindResultObject);
        } catch (Exception e) {
            throw new ShardingJdbcException(e.getMessage(), e);
        }
    }
}
