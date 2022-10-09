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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rul.sql;

import com.google.gson.Gson;
import org.apache.shardingsphere.distsql.parser.statement.rul.sql.ParseStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rul.SQLRULBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Parse Dist SQL handler.
 */
public final class ParseDistSQLHandler extends SQLRULBackendHandler<ParseStatement> {
    
    private static final String PARSED_STATEMENT = "parsed_statement";
    
    private static final String PARSED_STATEMENT_DETAIL = "parsed_statement_detail";
    
    @Override
    protected Collection<String> getColumnNames() {
        return Arrays.asList(PARSED_STATEMENT, PARSED_STATEMENT_DETAIL);
    }
    
    @Override
    protected Collection<LocalDataQueryResultRow> getRows(final ContextManager contextManager) {
        SQLStatement parsedSqlStatement = parseSQL(contextManager);
        return Collections.singleton(new LocalDataQueryResultRow(parsedSqlStatement.getClass().getSimpleName(), new Gson().toJson(parsedSqlStatement)));
    }
    
    private SQLStatement parseSQL(final ContextManager contextManager) {
        SQLParserRule sqlParserRule = contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        String databaseType = getConnectionSession().getDatabaseType().getType();
        return sqlParserRule.getSQLParserEngine(databaseType).parse(getSqlStatement().getSql(), false);
    }
}
