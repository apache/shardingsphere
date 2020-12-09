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

package org.apache.shardingsphere.proxy.backend.text.metadata.schema.impl;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.text.metadata.schema.SchemaBackendHandler;
import org.apache.shardingsphere.sharding.merge.dal.common.SingleLocalDataMergedResult;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;

/**
 * Show current database backend handler.
 */
@RequiredArgsConstructor
public final class ShowCurrentDatabaseBackendHandler implements SchemaBackendHandler {
    
    public static final String FUNCTION_NAME = "DATABASE()";
    
    private final BackendConnection backendConnection;
    
    private MergedResult mergedResult;
    
    @Override
    public ResponseHeader execute() {
        mergedResult = new SingleLocalDataMergedResult(Collections.singleton(backendConnection.getSchemaName()));
        return new QueryResponseHeader(Collections.singletonList(new QueryHeader(
                "information_schema", "SCHEMATA", FUNCTION_NAME, FUNCTION_NAME, Types.VARCHAR, "VARCHAR", 100, 0, false, false, false, false)));
    }
    
    @Override
    public boolean next() throws SQLException {
        return mergedResult.next();
    }
    
    @Override
    public Collection<Object> getRowData() throws SQLException {
        return Collections.singletonList(mergedResult.getValue(1, Object.class));
    }
}
