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

package org.apache.shardingsphere.proxy.backend.text.distsql.rql;

import org.apache.shardingsphere.distsql.parser.statement.rql.RQLStatement;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;

import java.sql.Types;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RQL backend handler.
 */
public final class RQLBackendHandler extends SchemaRequiredBackendHandler<RQLStatement> {
    
    private final DistSQLResultSet resultSet;
    
    public RQLBackendHandler(final RQLStatement sqlStatement, final ConnectionSession connectionSession, final DistSQLResultSet resultSet) {
        super(sqlStatement, connectionSession);
        this.resultSet = resultSet;
    }
    
    @Override
    protected ResponseHeader execute(final String schemaName, final RQLStatement sqlStatement) {
        resultSet.init(ProxyContext.getInstance().getMetaData(schemaName), sqlStatement);
        List<QueryHeader> queryHeaders = resultSet.getColumnNames().stream().map(
            each -> new QueryHeader(schemaName, "", each, each, Types.CHAR, "CHAR", 255, 0, false, false, false, false)).collect(Collectors.toList());
        return new QueryResponseHeader(queryHeaders);
    }
    
    @Override
    public boolean next() {
        return resultSet.next();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return resultSet.getRowData();
    }
}
