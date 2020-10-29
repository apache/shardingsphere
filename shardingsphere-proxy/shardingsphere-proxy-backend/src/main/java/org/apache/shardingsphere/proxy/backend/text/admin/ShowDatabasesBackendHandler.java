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

package org.apache.shardingsphere.proxy.backend.text.admin;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.sql.raw.execute.result.query.QueryHeader;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.query.QueryData;
import org.apache.shardingsphere.proxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.sharding.merge.dal.common.SingleLocalDataMergedResult;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Show databases backend handler.
 */
@RequiredArgsConstructor
public final class ShowDatabasesBackendHandler implements TextProtocolBackendHandler {
    
    private final BackendConnection backendConnection;
    
    private MergedResult mergedResult;
    
    @Override
    public BackendResponse execute() {
        mergedResult = new SingleLocalDataMergedResult(getSchemaNames());
        return new QueryResponse(Collections.singletonList(new QueryHeader("information_schema", "SCHEMATA", "Database", "SCHEMA_NAME", 100, Types.VARCHAR, 0, false, false, false, false)));
    }
    
    private Collection<Object> getSchemaNames() {
        Collection<Object> result = new LinkedList<>(ProxyContext.getInstance().getAllSchemaNames());
        Collection<String> authorizedSchemas = ProxyContext.getInstance().getSchemaContexts().getAuthentication().getUsers().get(backendConnection.getUsername()).getAuthorizedSchemas();
        if (!authorizedSchemas.isEmpty()) {
            result.retainAll(authorizedSchemas);
        }
        return result;
    }
    
    @Override
    public boolean next() throws SQLException {
        return null != mergedResult && mergedResult.next();
    }
    
    @Override
    public QueryData getQueryData() throws SQLException {
        return new QueryData(Collections.singletonList(Types.VARCHAR), Collections.singletonList(mergedResult.getValue(1, Object.class)));
    }
}
