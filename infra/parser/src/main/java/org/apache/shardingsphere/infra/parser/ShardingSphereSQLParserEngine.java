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

package org.apache.shardingsphere.infra.parser;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.sql.DialectSQLParsingException;
import org.apache.shardingsphere.distsql.parser.engine.api.DistSQLStatementParserEngine;
import org.apache.shardingsphere.infra.parser.sql.SQLStatementParserEngine;
import org.apache.shardingsphere.infra.parser.sql.SQLStatementParserEngineFactory;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.engine.exception.SQLParsingException;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.util.SQLUtils;

/**
 * ShardingSphere SQL parser engine.
 */
public final class ShardingSphereSQLParserEngine implements SQLParserEngine {
    
    private final SQLStatementParserEngine sqlStatementParserEngine;
    
    private final DistSQLStatementParserEngine distSQLStatementParserEngine;
    
    public ShardingSphereSQLParserEngine(final DatabaseType databaseType, final CacheOption sqlStatementCacheOption, final CacheOption parseTreeCacheOption) {
        sqlStatementParserEngine = SQLStatementParserEngineFactory.getSQLStatementParserEngine(databaseType, sqlStatementCacheOption, parseTreeCacheOption);
        distSQLStatementParserEngine = new DistSQLStatementParserEngine();
    }
    
    /*
     * To make sure SkyWalking will be available at the next release of ShardingSphere, a new plugin should be provided to SkyWalking project if this API changed.
     *
     * @see <a href="https://github.com/apache/skywalking/blob/master/docs/en/guides/Java-Plugin-Development-Guide.md#user-content-plugin-development-guide">Plugin Development Guide</a>
     */
    @Override
    public SQLStatement parse(final String sql, final boolean useCache) {
        try {
            return sqlStatementParserEngine.parse(sql, useCache);
        } catch (final SQLParsingException | ParseCancellationException originalEx) {
            try {
                String trimSQL = SQLUtils.trimComment(sql);
                return distSQLStatementParserEngine.parse(trimSQL);
            } catch (final SQLParsingException ignored) {
                throw getException(originalEx);
            }
        }
    }
    
    private RuntimeException getException(final RuntimeException originalEx) {
        return originalEx instanceof SQLParsingException
                ? new DialectSQLParsingException(originalEx.getMessage(), ((SQLParsingException) originalEx).getSymbol(), ((SQLParsingException) originalEx).getLine())
                : originalEx;
    }
}
