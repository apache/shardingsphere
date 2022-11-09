package org.apache.shardingsphere.sql.parser.result;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.sql.parser.env.IntegrationTestEnvironment;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Objects;

/**
 * Get the corresponding result processor through config.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SQLParserResultProcessorManager {
    
    /**
     * Get the SQL parser result processor.
     *
     * @param databaseType database type
     * @return the implementation of SQLParserResultProcessor
     */
    public static SQLParserResultProcessor getProcessor(final String databaseType) {
        String type = IntegrationTestEnvironment.getInstance().getResultProcessorType();
        try {
            Class<?> interfaceClazz = Class.forName(SQLParserResultProcessor.class.getPackage().getName());
            String packageName = interfaceClazz.getPackage().getName();
            URL packagePath = Thread.currentThread().getContextClassLoader().getResource(packageName.replace(".", "/"));
            File[] classFiles = new File(Objects.requireNonNull(packagePath).getFile()).listFiles((dir, name) -> name.endsWith(".class"));
            for (File file : Objects.requireNonNull(classFiles)) {
                String className = file.getName().replaceAll(".class$", "");
                Class<?> clazz = Class.forName(packageName + "." + className);
                if (SQLParserResultProcessor.class.isAssignableFrom(clazz)) {
                    Field typeField = clazz.getDeclaredField("type");
                    typeField.setAccessible(true);
                    Constructor<?> constructor = clazz.getConstructor(String.class);
                    SQLParserResultProcessor instance = (SQLParserResultProcessor) constructor.newInstance(databaseType);
                    if (type.equalsIgnoreCase(typeField.get(instance).toString())) {
                        return instance;
                    }
                }
            }
        } catch (ReflectiveOperationException ex) {
            log.error("encounter exception when get SQLParserResultProcessor by reflection", ex);
        }
        throw new IllegalArgumentException("The processor type does not supported : " + type);
    }
}
