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
import org.apache.shardingsphere.distsql.parser.statement.ral.common.status.SetStatusStatement;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.exception.SchemaNotExistedException;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.set.SetStatementExecutor;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Set status statement executor.
 */
@AllArgsConstructor
public final class SetStatusExecutor implements SetStatementExecutor {
    
    private static final String READWRITE_SPLITTING = "READWRITE_SPLITTING";
    
    private final SetStatusStatement sqlStatement;
    
    private final BackendConnection backendConnection;
    
    @Override
    public ResponseHeader execute() throws DistSQLException {
        String schemaName = sqlStatement.getSchema().isPresent() ? sqlStatement.getSchema().get().getIdentifier().getValue() : backendConnection.getSchemaName();
        if (null == schemaName) {
            throw new NoDatabaseSelectedException();
        }
        if (!ProxyContext.getInstance().getAllSchemaNames().contains(schemaName)) {
            throw new SchemaNotExistedException(schemaName);
        }
        String resourceName = sqlStatement.getResourceName();
        Collection<String> notExistedResources = ProxyContext.getInstance().getMetaData(schemaName).getResource().getNotExistedResources(Collections.singleton(resourceName));
        DistSQLException.predictionThrow(notExistedResources.isEmpty(), new RequiredResourceMissedException(schemaName, Collections.singleton(resourceName)));
        if (sqlStatement.getFeatureName().equalsIgnoreCase(READWRITE_SPLITTING)) {
            handleReadwriteSplitting(schemaName, resourceName);
        } else {
            throw new UnsupportedOperationException(sqlStatement.getFeatureName());
        }
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void handleReadwriteSplitting(final String schemaName, final String resourceName) {
        Optional<MetaDataPersistService> persistService = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaDataPersistService();
        //TODO Need to disable slave data source API support
    }
}
