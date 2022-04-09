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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import org.apache.shardingsphere.distsql.parser.statement.ral.advanced.ParseStatement;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.QueryableRALBackendHandler;
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Parse dist sql backend handler.
 */
public final class ParseDistSQLBackendHandler extends QueryableRALBackendHandler<ParseStatement, ParseDistSQLBackendHandler> {
    
    private static final String PARSED_STATEMENT = "parsed_statement";
    
    private static final String PARSED_STATEMENT_DETAIL = "parsed_statement_detail";
    
    private DatabaseType databaseType;
    
    private ConnectionSession connectionSession;
    
    @Override
    public ParseDistSQLBackendHandler init(final HandlerParameter<ParseStatement> parameter) {
        databaseType = parameter.getDatabaseType();
        connectionSession = parameter.getConnectionSession();
        return super.init(parameter);
    }
    
    @Override
    protected Collection<String> getColumnNames() {
        return Arrays.asList(PARSED_STATEMENT, PARSED_STATEMENT_DETAIL);
    }
    
    @Override
    protected Collection<List<Object>> getRows(final ContextManager contextManager) {
        Optional<SQLParserRule> sqlParserRule = contextManager.getMetaDataContexts().getGlobalRuleMetaData().findSingleRule(SQLParserRule.class);
        Preconditions.checkState(sqlParserRule.isPresent());
        SQLStatement parsedSqlStatement;
        try {
            parsedSqlStatement = new ShardingSphereSQLParserEngine(
                    getBackendDatabaseType(databaseType, connectionSession).getName(), sqlParserRule.get().toParserConfiguration()).parse(sqlStatement.getSql(), false);
        } catch (SQLParsingException ex) {
            throw new SQLParsingException("You have a syntax error in your parsed statement");
        }
        return Collections.singleton(Arrays.asList(parsedSqlStatement.getClass().getSimpleName(), new Gson().toJson(parsedSqlStatement)));
    }
    
    private static DatabaseType getBackendDatabaseType(final DatabaseType defaultDatabaseType, final ConnectionSession connectionSession) {
        String schemaName = connectionSession.getSchemaName();
        return Strings.isNullOrEmpty(schemaName) || !ProxyContext.getInstance().schemaExists(schemaName)
                ? defaultDatabaseType : ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(schemaName).getResource().getDatabaseType();
    }
}
