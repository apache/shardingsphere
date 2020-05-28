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

package org.apache.shardingsphere.sql.parser.core;

import org.apache.shardingsphere.sql.parser.spi.SQLParserConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * SQL parser configuration registry.
 */
public final class SQLParserConfigurationRegistry {
    
    private static final SQLParserConfigurationRegistry INSTANCE = new SQLParserConfigurationRegistry();
    
    private final Map<String, SQLParserConfiguration> configurations;
    
    private SQLParserConfigurationRegistry() {
        configurations = new HashMap<>();
        for (SQLParserConfiguration each : ServiceLoader.load(SQLParserConfiguration.class)) {
            configurations.put(each.getDatabaseTypeName(), each);
        }
    }
    
    /**
     * Get instance.
     *
     * @return instance
     */
    public static SQLParserConfigurationRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get SQL parser configuration.
     * 
     * @param databaseTypeName database type name
     * @return SQL parser configuration
     */
    public SQLParserConfiguration getSQLParserConfiguration(final String databaseTypeName) {
        if (configurations.containsKey(databaseTypeName)) {
            return configurations.get(databaseTypeName);
        }
        throw new UnsupportedOperationException(String.format("Cannot support database type '%s'", databaseTypeName));
    }
}
