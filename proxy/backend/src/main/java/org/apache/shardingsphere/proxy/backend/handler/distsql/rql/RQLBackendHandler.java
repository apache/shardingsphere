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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rql;

import org.apache.shardingsphere.distsql.parser.statement.rql.RQLStatement;
import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.DatabaseRequiredBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RQL backend handler.
 */
public final class RQLBackendHandler extends DatabaseRequiredBackendHandler<RQLStatement> {
    
    private final DatabaseDistSQLResultSet resultSet;
    
    public RQLBackendHandler(final RQLStatement sqlStatement, final ConnectionSession connectionSession, final DatabaseDistSQLResultSet resultSet) {
        super(sqlStatement, connectionSession);
        this.resultSet = resultSet;
    }
    
    @Override
    protected ResponseHeader execute(final String databaseName, final RQLStatement sqlStatement) {
        resultSet.init(ProxyContext.getInstance().getDatabase(databaseName), sqlStatement);
        List<QueryHeader> queryHeaders = resultSet.getColumnNames().stream()
                .map(each -> new QueryHeader(databaseName, "", each, each, Types.CHAR, "CHAR", 255, 0, false, false, false, false)).collect(Collectors.toList());
        return new QueryResponseHeader(queryHeaders);
    }
    
    @Override
    public boolean next() {
        return resultSet.next();
    }
    
    @Override
    public QueryResponseRow getRowData() {
        Collection<Object> rowData = resultSet.getRowData();
        List<QueryResponseCell> result = new ArrayList<>(rowData.size());
        for (Object each : rowData) {
            result.add(new QueryResponseCell(Types.CHAR, each));
        }
        return new QueryResponseRow(result);
    }
}
