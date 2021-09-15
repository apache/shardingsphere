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

package org.apache.shardingsphere.infra.parser.sql;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQL statement parser engine factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLStatementParserEngineFactory {
    
    private static final Map<String, SQLStatementParserEngine> ENGINES = new ConcurrentHashMap<>();
    
    /**
     * Get SQL statement parser engine.
     *
     * @param databaseType name of database type
     * @return SQL statement parser engine
     */
    public static SQLStatementParserEngine getSQLStatementParserEngine(final String databaseType) {
        return ENGINES.getOrDefault(databaseType, ENGINES.computeIfAbsent(databaseType, SQLStatementParserEngine::new));
    }
}
