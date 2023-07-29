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
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rul.executor.ConnectionSessionRequiredRULExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Parse DistSQL executor.
 */
public final class ParseDistSQLExecutor implements ConnectionSessionRequiredRULExecutor<ParseStatement> {
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("parsed_statement", "parsed_statement_detail");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereMetaData metaData, final ConnectionSession connectionSession, final ParseStatement sqlStatement) {
        SQLStatement parsedSqlStatement = parseSQL(metaData, connectionSession, sqlStatement);
        return Collections.singleton(new LocalDataQueryResultRow(parsedSqlStatement.getClass().getSimpleName(), new Gson().toJson(parsedSqlStatement)));
    }
    
    private SQLStatement parseSQL(final ShardingSphereMetaData metaData, final ConnectionSession connectionSession, final ParseStatement sqlStatement) {
        SQLParserRule sqlParserRule = metaData.getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        return sqlParserRule.getSQLParserEngine(connectionSession.getProtocolType()).parse(sqlStatement.getSql(), false);
    }
    
    @Override
    public String getType() {
        return ParseStatement.class.getName();
    }
}
