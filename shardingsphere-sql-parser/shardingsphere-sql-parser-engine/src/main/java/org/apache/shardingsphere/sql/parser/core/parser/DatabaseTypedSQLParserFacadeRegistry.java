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

package org.apache.shardingsphere.sql.parser.core.parser;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.sql.parser.spi.DatabaseTypedSQLParserFacade;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Database type based SQL parser facade registry.
 */
public final class DatabaseTypedSQLParserFacadeRegistry {
    
    private static final DatabaseTypedSQLParserFacadeRegistry INSTANCE = new DatabaseTypedSQLParserFacadeRegistry();
    
    private final Map<String, DatabaseTypedSQLParserFacade> facades = new LinkedHashMap<>();
    
    private DatabaseTypedSQLParserFacadeRegistry() {
        for (DatabaseTypedSQLParserFacade each : ServiceLoader.load(DatabaseTypedSQLParserFacade.class)) {
            facades.put(each.getDatabaseType(), each);
        }
    }
    
    /**
     * Get instance.
     *
     * @return instance
     */
    public static DatabaseTypedSQLParserFacadeRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get database type based SQL parser facade.
     * 
     * @param databaseType database type
     * @return database type based SQL parser facade
     */
    public DatabaseTypedSQLParserFacade getFacade(final String databaseType) {
        Preconditions.checkArgument(facades.containsKey(databaseType), "Cannot support database type '%s'", databaseType);
        return facades.get(databaseType);
    }
}
