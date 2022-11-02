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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral;

import org.apache.shardingsphere.distsql.parser.statement.ral.RALStatement;
import org.apache.shardingsphere.infra.distsql.query.GlobalRuleDistSQLResultSet;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseCell;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Queryable RAL backend handler for global rule.
 */
public final class QueryableGlobalRuleRALBackendHandler implements ProxyBackendHandler {
    
    private final RALStatement sqlStatement;
    
    private final GlobalRuleDistSQLResultSet resultSet;
    
    public QueryableGlobalRuleRALBackendHandler(final RALStatement sqlStatement, final GlobalRuleDistSQLResultSet resultSet) {
        this.sqlStatement = sqlStatement;
        this.resultSet = resultSet;
    }
    
    @Override
    public ResponseHeader execute() {
        resultSet.init(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData(), sqlStatement);
        List<QueryHeader> queryHeaders = new ArrayList<>();
        for (String each : resultSet.getColumnNames()) {
            queryHeaders.add(new QueryHeader("", "", each, each, Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        }
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
