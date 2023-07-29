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

package org.apache.shardingsphere.proxy.backend.hbase.handler;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.proxy.backend.hbase.result.HBaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.hbase.result.query.HBaseQueryResultSet;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Backend handler for HBase.
 */
@RequiredArgsConstructor
public final class HBaseBackendQueryHandler implements HBaseBackendHandler {
    
    private final SQLStatement sqlStatement;
    
    private final HBaseQueryResultSet resultSet;
    
    @Override
    public ResponseHeader execute() {
        SQLStatementContext sqlStatementContext = new SQLBindEngine(null, "").bind(sqlStatement, Collections.emptyList());
        resultSet.init(sqlStatementContext);
        List<QueryHeader> queryHeaders = resultSet.getColumnNames().stream().map(each -> new QueryHeader("", "", each, each, Types.CHAR, "CHAR", 255, 0, false, false, false, false))
                .collect(Collectors.toList());
        return new QueryResponseHeader(queryHeaders);
    }
    
    @Override
    public boolean next() {
        return resultSet.next();
    }
    
    @Override
    public Collection<Object> getRowDataObjects() {
        return resultSet.getRowData();
    }
}
