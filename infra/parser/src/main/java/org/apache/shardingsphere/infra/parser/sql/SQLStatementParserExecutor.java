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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.engine.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.engine.api.SQLStatementVisitorEngine;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;

/**
 * SQL statement parser executor.
 */
public final class SQLStatementParserExecutor {
    
    private final SQLParserEngine parserEngine;
    
    private final SQLStatementVisitorEngine visitorEngine;
    
    public SQLStatementParserExecutor(final DatabaseType databaseType, final CacheOption parseTreeCacheOption) {
        parserEngine = new SQLParserEngine(databaseType, parseTreeCacheOption);
        visitorEngine = new SQLStatementVisitorEngine(databaseType);
    }
    
    /**
     * Update cache option.
     *
     * @param parseTreeCacheOption parse tree cache option
     */
    public void updateCacheOption(final CacheOption parseTreeCacheOption) {
        parserEngine.updateCacheOption(parseTreeCacheOption);
    }
    
    /**
     * Parse to SQL statement.
     *
     * @param sql SQL to be parsed
     * @return SQL statement
     */
    public SQLStatement parse(final String sql) {
        return visitorEngine.visit(parserEngine.parse(sql, false));
    }
}
