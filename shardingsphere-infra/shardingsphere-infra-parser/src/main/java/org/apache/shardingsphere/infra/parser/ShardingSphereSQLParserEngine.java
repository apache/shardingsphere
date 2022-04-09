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

import com.google.common.util.concurrent.UncheckedExecutionException;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.shardingsphere.distsql.parser.engine.api.DistSQLStatementParserEngine;
import org.apache.shardingsphere.infra.parser.sql.SQLStatementParserEngine;
import org.apache.shardingsphere.infra.parser.sql.SQLStatementParserEngineFactory;
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;

/**
 * ShardingSphere SQL parser engine.
 */
public final class ShardingSphereSQLParserEngine {
    
    private final SQLStatementParserEngine sqlStatementParserEngine;
    
    private final DistSQLStatementParserEngine distSQLStatementParserEngine;
    
    public ShardingSphereSQLParserEngine(final String databaseTypeName, final ParserConfiguration config) {
        sqlStatementParserEngine = SQLStatementParserEngineFactory.getSQLStatementParserEngine(
                databaseTypeName, config.getSqlStatementCacheOption(), config.getParseTreeCacheOption(), config.isParseComment());
        distSQLStatementParserEngine = new DistSQLStatementParserEngine();
    }
    
    /*
     * To make sure SkyWalking will be available at the next release of ShardingSphere, a new plugin should be provided to SkyWalking project if this API changed.
     *
     * @see <a href="https://github.com/apache/skywalking/blob/master/docs/en/guides/Java-Plugin-Development-Guide.md#user-content-plugin-development-guide">Plugin Development Guide</a>
     */
    /**
     * Parse to SQL statement.
     *
     * @param sql SQL to be parsed
     * @param useCache whether use cache
     * @return SQL statement
     */
    public SQLStatement parse(final String sql, final boolean useCache) {
        try {
            return sqlStatementParserEngine.parse(sql, useCache);
        } catch (final SQLParsingException | ParseCancellationException | UncheckedExecutionException originalEx) {
            try {
                String trimSQL = SQLUtil.trimComment(sql);
                return distSQLStatementParserEngine.parse(trimSQL);
            } catch (final SQLParsingException ignored) {
                throw originalEx;
            }
        }
    }
}
