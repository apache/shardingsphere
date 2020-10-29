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

package org.apache.shardingsphere.infra.parser.standard;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Standard SQL statement parser engine factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StandardSQLStatementParserEngineFactory {
    
    private static final Map<String, StandardSQLStatementParserEngine> ENGINES = new HashMap<>();
    
    /**
     * Get standard SQL statement parser engine.
     *x
     * @param databaseType name of database type
     * @return standard SQL statement parser engine
     */
    public static StandardSQLStatementParserEngine getSQLStatementParserEngine(final String databaseType) {
        if (ENGINES.containsKey(databaseType)) {
            return ENGINES.get(databaseType);
        }
        return createAndCacheSingletonStandardSQLStatementParserEngine(databaseType);
    }
    
    private static StandardSQLStatementParserEngine createAndCacheSingletonStandardSQLStatementParserEngine(final String databaseType) {
        synchronized (ENGINES) {
            if (ENGINES.containsKey(databaseType)) {
                return ENGINES.get(databaseType);
            }
            StandardSQLStatementParserEngine result = new StandardSQLStatementParserEngine(databaseType);
            ENGINES.put(databaseType, result);
            return result;
        }
    }
}
