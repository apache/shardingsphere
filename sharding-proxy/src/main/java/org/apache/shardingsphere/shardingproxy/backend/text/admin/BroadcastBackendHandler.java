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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.shardingproxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.result.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.result.common.FailureResponse;
import org.apache.shardingsphere.shardingproxy.backend.result.common.SuccessResponse;
import org.apache.shardingsphere.shardingproxy.backend.result.query.QueryData;
import org.apache.shardingsphere.shardingproxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Backend handler for broadcast.
 *
 * @author chenqingyang
 * @author zhaojun
 */
@RequiredArgsConstructor
public final class BroadcastBackendHandler implements TextProtocolBackendHandler {
    
    private final DatabaseCommunicationEngineFactory databaseCommunicationEngineFactory = DatabaseCommunicationEngineFactory.getInstance();
    
    private final String sql;
    
    private final BackendConnection backendConnection;
    
    @Override
    public BackendResponse execute() {
        Collection<BackendResponse> responses = new LinkedList<>();
        for (String each : GlobalRegistry.getInstance().getSchemaNames()) {
            responses.add(databaseCommunicationEngineFactory.newTextProtocolInstance(GlobalRegistry.getInstance().getLogicSchema(each), sql, backendConnection).execute());
        }
        for (BackendResponse each : responses) {
            if (each instanceof FailureResponse) {
                return each;
            }
        }
        return new SuccessResponse();
    }
    
    @Override
    public boolean next() {
        return false;
    }
    
    @Override
    public QueryData getQueryData() {
        return null;
    }
}
