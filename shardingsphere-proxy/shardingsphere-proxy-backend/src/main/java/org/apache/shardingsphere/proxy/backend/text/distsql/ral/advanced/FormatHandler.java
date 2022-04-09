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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.advanced;

import org.apache.shardingsphere.distsql.parser.statement.ral.advanced.FormatStatement;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.QueryableRALBackendHandler;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.api.SQLParserEngine;
import org.apache.shardingsphere.sql.parser.api.SQLVisitorEngine;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Format handler.
 */
public final class FormatHandler extends QueryableRALBackendHandler<FormatStatement, FormatHandler> {
    
    private static final String FORMATTED_RESULT = "formatted_result";
    
    @Override
    protected Collection<String> getColumnNames() {
        return Collections.singletonList(FORMATTED_RESULT);
    }
    
    @Override
    protected Collection<List<Object>> getRows(final ContextManager contextManager) throws SQLException {
        CacheOption cacheOption = new CacheOption(128, 1024L, 4);
        SQLParserEngine parserEngine = new SQLParserEngine("MySQL", cacheOption);
        ParseASTNode parseASTNode;
        try {
            parseASTNode = parserEngine.parse(sqlStatement.getSql(), false);
        } catch (SQLParsingException ex) {
            throw new SQLParsingException("You have a syntax error in your formatted statement");
        }
        Properties props = new Properties();
        props.setProperty("parameterized", "false");
        SQLVisitorEngine visitorEngine = new SQLVisitorEngine("MySQL", "FORMAT", false, props);
        return Collections.singleton(Collections.singletonList(visitorEngine.visit(parseASTNode)));
    }
}
