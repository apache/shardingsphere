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

import com.google.common.base.Strings;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.advanced.parse.ParseStatement;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.sql.parser.exception.SQLParsingException;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Parse dist sql backend handler.
 */
@RequiredArgsConstructor
@Getter
public final class ParseDistSQLBackendHandler implements TextProtocolBackendHandler {
    
    private final DatabaseType databaseType;
    
    private final ParseStatement previewStatement;
    
    private final BackendConnection backendConnection;
    
    private final List<QueryHeader> queryHeaders = new ArrayList<>(1);
    
    private Iterator<String> data = Collections.emptyIterator();
    
    @Override
    public ResponseHeader execute() {
        Optional<SQLParserRule> sqlParserRule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getGlobalRuleMetaData().findSingleRule(SQLParserRule.class);
        SQLStatement sqlStatement;
        try {
            sqlStatement = new ShardingSphereSQLParserEngine(getBackendDatabaseType(databaseType, backendConnection).getName(), sqlParserRule.orElse(null)).parse(previewStatement.getSql(), false);
        } catch (SQLParsingException ex) {
            throw new SQLParsingException("You have an error in your SQL syntax that you are parsed");
        }
        data = Collections.singleton(new Gson().toJson(sqlStatement)).iterator();
        queryHeaders.add(new QueryHeader("", "", sqlStatement.getClass().getSimpleName(), "", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        return new QueryResponseHeader(queryHeaders);
    }
    
    private static DatabaseType getBackendDatabaseType(final DatabaseType defaultDatabaseType, final BackendConnection backendConnection) {
        String schemaName = backendConnection.getSchemaName();
        return Strings.isNullOrEmpty(schemaName) || !ProxyContext.getInstance().schemaExists(schemaName)
                ? defaultDatabaseType : ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(schemaName).getResource().getDatabaseType();
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return Collections.singleton(data.next());
    }
}
