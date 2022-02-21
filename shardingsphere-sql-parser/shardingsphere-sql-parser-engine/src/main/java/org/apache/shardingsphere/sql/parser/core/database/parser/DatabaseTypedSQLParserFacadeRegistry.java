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

package org.apache.shardingsphere.sql.parser.core.database.parser;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.spi.DatabaseTypedSQLParserFacade;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Database type based SQL parser facade registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseTypedSQLParserFacadeRegistry {
    
    private static final Map<String, DatabaseTypedSQLParserFacade> FACADES = new HashMap<>();
    
    static {
        for (DatabaseTypedSQLParserFacade each : ServiceLoader.load(DatabaseTypedSQLParserFacade.class)) {
            FACADES.put(each.getDatabaseType(), each);
        }
    }
    
    /**
     * Get database type based SQL parser facade.
     * 
     * @param databaseType database type
     * @return database type based SQL parser facade
     */
    public static DatabaseTypedSQLParserFacade getFacade(final String databaseType) {
        Preconditions.checkArgument(FACADES.containsKey(databaseType), "Cannot support database type '%s'", databaseType);
        return FACADES.get(databaseType);
    }
}
