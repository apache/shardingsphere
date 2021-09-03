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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.ShowDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.variable.ShowVariableStatement;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.enums.VariableEnum;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.exception.UnsupportedVariableException;
import org.apache.shardingsphere.sharding.merge.dal.common.MultipleLocalDataMergedResult;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Show dist sql backend handler.
 */
@RequiredArgsConstructor
@Getter
public final class ShowDistSQLBackendHandler implements TextProtocolBackendHandler {
    
    private final ShowDistSQLStatement sqlStatement;
    
    private final BackendConnection backendConnection;
    
    private MergedResult mergedResult;
    
    @Override
    public ResponseHeader execute() {
        ShowVariableStatement showVariableStatement = (ShowVariableStatement) sqlStatement;
        switch (VariableEnum.getValueOf(showVariableStatement.getName())) {
            case TRANSACTION_TYPE:
                return createResponsePackets(VariableEnum.TRANSACTION_TYPE.name(), backendConnection.getTransactionStatus().getTransactionType().name());
            case CACHED_CONNECTIONS:
                return createResponsePackets(VariableEnum.CACHED_CONNECTIONS.name(), backendConnection.getConnectionSize());
            default:
                throw new UnsupportedVariableException(showVariableStatement.getName());
        }
    }
    
    @Override
    public boolean next() throws SQLException {
        return null != mergedResult && mergedResult.next();
    }
    
    @Override
    public Collection<Object> getRowData() throws SQLException {
        return Collections.singletonList(mergedResult.getValue(1, Object.class));
    }
    
    private ResponseHeader createResponsePackets(final String columnName, final Object... values) {
        mergedResult = new MultipleLocalDataMergedResult(Collections.singletonList(Arrays.asList(values)));
        return new QueryResponseHeader(Collections.singletonList(new QueryHeader("", "", columnName.toLowerCase(), columnName, Types.VARCHAR, "VARCHAR", 100, 0, false, false, false, false)));
    }
}
