package io.shardingjdbc.spring.datasource;

import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Data source bean util.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class DataSourceBeanUtil {
    
    private static Collection<Class<?>> generalClassType;
    
    static {
        generalClassType = Sets.<Class<?>>newHashSet(boolean.class, Boolean.class, int.class, Integer.class, long.class, Long.class, String.class);
    }
    
    static void createDataSourceBean(final ApplicationContext applicationContext, final String dataSourceName, final DataSource dataSource) {
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        BeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClassName(dataSource.getClass().getName());
        beanFactory.registerBeanDefinition(dataSourceName, beanDefinition);
        Method[] methods = dataSource.getClass().getDeclaredMethods();
        Map<String, Method> getterMethods = new TreeMap<>();
        Map<String, Method> setterMethods = new TreeMap<>();
        for (Method each : methods) {
            if (isGetterMethod(each)) {
                getterMethods.put(getPropertyName(each), each);
            } else if (isSetterMethod(each)) {
                setterMethods.put(getPropertyName(each), each);
            }
        }
        Map<String, Method> getterPairedGetterMethods = getPairedGetterMethods(getterMethods, setterMethods);
        for (Map.Entry<String, Method> entry : getterPairedGetterMethods.entrySet()) {
            Object getterResult = null;
            try {
                getterResult = entry.getValue().invoke(dataSource);
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
            }
            // CHECKSTYLE:ON
            if (null != getterResult) {
                beanDefinition.setAttribute(entry.getKey(), getterResult);
            }
        }
    }
    
    private static boolean isGetterMethod(final Method method) {
        return method.getName().startsWith("get") && 0 == method.getParameterTypes().length && isGeneralClassType(method.getReturnType());
    }
    
    private static boolean isSetterMethod(final Method method) {
        return method.getName().startsWith("set") && 1 == method.getParameterTypes().length && isGeneralClassType(method.getParameterTypes()[0]) && isVoid(method.getReturnType());
    }
    
    private static boolean isGeneralClassType(final Class<?> clazz) {
        return generalClassType.contains(clazz);
    }
    
    private static boolean isVoid(final Class<?> clazz) {
        return void.class == clazz || Void.class == clazz;
    }
    
    private static String getPropertyName(final Method method) {
        return String.valueOf(method.getName().charAt(3)).toLowerCase() + method.getName().substring(4, method.getName().length());
    }
    
    private static Map<String, Method> getPairedGetterMethods(final Map<String, Method> getterMethods, final Map<String, Method> setterMethods) {
        Map<String, Method> result = new TreeMap<>();
        for (Map.Entry<String, Method> entry : getterMethods.entrySet()) {
            if (setterMethods.containsKey(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}
