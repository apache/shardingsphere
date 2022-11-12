/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.test.integration.sql.parser.result;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.test.integration.sql.parser.env.IntegrationTestEnvironment;

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
            Class<?> interfaceClazz = Class.forName(SQLParserResultProcessor.class.getPackage().getName() + "." + SQLParserResultProcessor.class.getSimpleName());
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
                    SQLParserResultProcessor result = (SQLParserResultProcessor) constructor.newInstance(databaseType);
                    if (type.equalsIgnoreCase(typeField.get(result).toString())) {
                        return result;
                    }
                }
            }
        } catch (final ReflectiveOperationException ex) {
            log.error("encounter exception when get SQLParserResultProcessor by reflection", ex);
        }
        throw new IllegalArgumentException("The processor type does not supported : " + type);
    }
}
