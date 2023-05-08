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

import org.apache.shardingsphere.infra.parser.sql.SQLStatementParserEngine;
import org.apache.shardingsphere.infra.parser.sql.SQLStatementParserEngineFactory;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SimpleSQLStatement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple SQL parser engine.
 */
public final class SimpleSQLParserEngine implements SQLParserEngine {
    
    private final SQLStatementParserEngine sqlStatementParserEngine;
    
    public SimpleSQLParserEngine(final String databaseType, final CacheOption sqlStatementCacheOption, final CacheOption parseTreeCacheOption, final boolean isParseComment) {
        sqlStatementParserEngine = SQLStatementParserEngineFactory.getSQLStatementParserEngine(
                databaseType, sqlStatementCacheOption, parseTreeCacheOption, isParseComment);
    }
    
    private boolean isContainShowDatabases(final String sql) {
        String regex = "^(\\s*)show(\\s+)databases(\\s*)";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        return matcher.matches();
    }
    
    @Override
    public SQLStatement parse(final String sql, final boolean useCache) {
        return isContainShowDatabases(sql) ? sqlStatementParserEngine.parse(sql, useCache) : new SimpleSQLStatement();
    }
}
