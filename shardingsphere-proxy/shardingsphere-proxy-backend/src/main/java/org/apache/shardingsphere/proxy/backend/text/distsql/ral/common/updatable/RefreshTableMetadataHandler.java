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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable;

import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.RefreshTableMetadataStatement;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.exception.UnknownDatabaseException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.UpdatableRALBackendHandler;

/**
 * Refresh table metadata handler.
 */
public final class RefreshTableMetadataHandler extends UpdatableRALBackendHandler<RefreshTableMetadataStatement, RefreshTableMetadataHandler> {
    
    private ConnectionSession connectionSession;
    
    @Override
    public RefreshTableMetadataHandler init(final HandlerParameter<RefreshTableMetadataStatement> parameter) {
        sqlStatement = parameter.getStatement();
        connectionSession = parameter.getConnectionSession();
        return this;
    }
    
    @Override
    protected void update(final ContextManager contextManager, final RefreshTableMetadataStatement sqlStatement) throws DistSQLException {
        String schemaName = connectionSession.getSchemaName();
        checkSchema(schemaName);
        if (sqlStatement.getResourceName().isPresent()) {
            contextManager.reloadMetaData(schemaName, sqlStatement.getTableName().get(), sqlStatement.getResourceName().get());
            return;
        }
        if (sqlStatement.getTableName().isPresent()) {
            contextManager.reloadMetaData(schemaName, sqlStatement.getTableName().get());
            return;
        }
        contextManager.reloadMetaData(schemaName);
    }
    
    private void checkSchema(final String schemaName) {
        if (null == schemaName) {
            throw new NoDatabaseSelectedException();
        }
        if (!ProxyContext.getInstance().schemaExists(schemaName)) {
            throw new UnknownDatabaseException(schemaName);
        }
    }
}
