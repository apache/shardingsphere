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

import org.apache.shardingsphere.sql.parser.spi.SQLParserFacade;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * SQL parser facade registry.
 */
public final class SQLParserFacadeRegistry {
    
    private static final SQLParserFacadeRegistry INSTANCE = new SQLParserFacadeRegistry();
    
    private final Map<String, SQLParserFacade> facades;
    
    private SQLParserFacadeRegistry() {
        facades = new HashMap<>();
        for (SQLParserFacade each : ServiceLoader.load(SQLParserFacade.class)) {
            facades.put(each.getDatabaseType(), each);
        }
    }
    
    /**
     * Get instance.
     *
     * @return instance
     */
    public static SQLParserFacadeRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get SQL parser facade.
     * 
     * @param databaseType database type
     * @return SQL parser facade
     */
    public SQLParserFacade getSQLParserFacade(final String databaseType) {
        if (facades.containsKey(databaseType)) {
            return facades.get(databaseType);
        }
        throw new UnsupportedOperationException(String.format("Cannot support database type '%s'", databaseType));
    }
}
