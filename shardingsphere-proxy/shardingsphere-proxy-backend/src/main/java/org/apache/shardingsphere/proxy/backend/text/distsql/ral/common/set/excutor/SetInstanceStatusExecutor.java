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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.set.excutor;

import lombok.AllArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.status.SetInstanceStatusStatement;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.set.SetStatementExecutor;

import java.util.Optional;

/**
 * Set instance status executor.
 */
@AllArgsConstructor
public final class SetInstanceStatusExecutor implements SetStatementExecutor {
    
    private final SetInstanceStatusStatement sqlStatement;
    
    private final BackendConnection backendConnection;
    
    @Override
    public ResponseHeader execute() throws DistSQLException {
        String ip = sqlStatement.getIp();
        String port = sqlStatement.getPort();
        Optional<MetaDataPersistService> persistService = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaDataPersistService();
        //TODO Need to circuit breaker API support.
        return new UpdateResponseHeader(sqlStatement);
    }
}
