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

package org.apache.shardingsphere.shardingproxy.backend.text.admin;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.shardingsphere.core.merge.MergedResult;
import org.apache.shardingsphere.core.merge.dal.show.ShowTablesMergedResult;
import org.apache.shardingsphere.core.parse.sql.statement.dal.dialect.mysql.ShowTablesStatement;
import org.apache.shardingsphere.core.parse.util.SQLUtil;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.shardingproxy.backend.exception.UnknownDatabaseException;
import org.apache.shardingsphere.shardingproxy.backend.response.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryData;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryHeader;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchemas;
import org.apache.shardingsphere.shardingproxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.shardingproxy.context.ShardingProxyContext;

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Show tables backend handler.
 *
 * @author sunbufu
 */
@RequiredArgsConstructor
public final class ShowTablesBackendHandler implements TextProtocolBackendHandler {
    
    private final ShowTablesStatement showTablesStatement;
    
    private final BackendConnection backendConnection;
    
    private MergedResult mergedResult;
    
    @Override
    public BackendResponse execute() {
        String schema = getSchema();
        if (null == schema) {
            return new ErrorResponse(new NoDatabaseSelectedException());
        }
        if (!LogicSchemas.getInstance().schemaExists(schema) || !isAuthorizedSchema(schema)) {
            return new ErrorResponse(new UnknownDatabaseException(schema));
        }
        List<String> tableNames = filterWithPattern(getTableNames(schema), getPattern());
        mergedResult = new ShowTablesMergedResult(tableNames, showTablesStatement.isFull() ? getTableTypes(tableNames) : Collections.<String>emptyList());
        return new QueryResponse(getQueryHeaders(schema));
    }
    
    private String getSchema() {
        String schema = SQLUtil.getExactlyValue(showTablesStatement.getSchema());
        if (null == schema) {
            schema = backendConnection.getSchemaName();
        }
        return schema;
    }
    
    private boolean isAuthorizedSchema(final String schema) {
        Collection<String> authorizedSchemas = ShardingProxyContext.getInstance().getAuthentication().getUsers().get(backendConnection.getUserName()).getAuthorizedSchemas();
        return authorizedSchemas.isEmpty() || authorizedSchemas.contains(schema);
    }
    
    private List<String> getTableNames(final String schema) {
        LogicSchema logicSchema = LogicSchemas.getInstance().getLogicSchema(schema);
        if (null == logicSchema || null == logicSchema.getShardingRule()) {
            return Collections.emptyList();
        } else {
            return new ArrayList<>(logicSchema.getShardingRule().getLogicTableNames());
        }
    }
    
    private List<String> getTableTypes(final List<String> tableNames) {
        return Lists.transform(tableNames, new Function<String, String>() {
            @Override
            public String apply(final String input) {
                return "BASE TABLE";
            }
        });
    }
    
    private String getPattern() {
        return SQLUtil.getExactlyValue(showTablesStatement.getPattern());
    }
    
    private List<String> filterWithPattern(final List<String> list, final String pattern) {
        if (Strings.isNullOrEmpty(pattern)) {
            return list;
        }
        List<String> result = new LinkedList<>();
        String regex = pattern;
        regex = regex.replace("?", ".");
        regex = regex.replace("%", ".*");
        for (String each : list) {
            if (!each.matches(regex)) {
                continue;
            }
            result.add(each);
        }
        return result;
    }
    
    private List<QueryHeader> getQueryHeaders(final String schema) {
        List<QueryHeader> result = Lists.newArrayListWithExpectedSize(showTablesStatement.isFull() ? 2 : 1);
        StringBuilder columnLabel = new StringBuilder().append("Tables_in_").append(schema);
        if (showTablesStatement.isFull()) {
            columnLabel.append(" (").append(showTablesStatement.getPattern()).append(")");
        }
        result.add(new QueryHeader("information_schema", "SCHEMATA", columnLabel.toString(), "TABLE_NAME", 100, Types.VARCHAR, 0));
        if (showTablesStatement.isFull()) {
            result.add(new QueryHeader("information_schema", "SCHEMATA", "Table_type", "TABLE_TYPE", 100, Types.VARCHAR, 0));
        }
        return result;
    }
    
    @Override
    public boolean next() throws SQLException {
        return null != mergedResult && mergedResult.next();
    }
    
    @Override
    public QueryData getQueryData() throws SQLException {
        if (showTablesStatement.isFull()) {
            return new QueryData(Lists.newArrayList(Types.VARCHAR, Types.VARCHAR), Lists.newArrayList(mergedResult.getValue(1, Object.class), mergedResult.getValue(2, Object.class)));
        } else {
            return new QueryData(Lists.newArrayList(Types.VARCHAR), Lists.newArrayList(mergedResult.getValue(1, Object.class)));
        }
    }
}
