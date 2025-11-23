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

package org.apache.shardingsphere.proxy.backend.handler.database.type;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.DatabaseCreateExistsException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.CreateDatabaseStatement;

/**
 * Create database proxy backend handler.
 */
@RequiredArgsConstructor
public final class CreateDatabaseProxyBackendHandler implements ProxyBackendHandler {
    
    private final CreateDatabaseStatement sqlStatement;
    
    private final ContextManager contextManager;
    
    @Override
    public ResponseHeader execute() {
        ShardingSpherePreconditions.checkState(sqlStatement.isIfNotExists() || !contextManager.getMetaDataContexts().getMetaData().containsDatabase(sqlStatement.getDatabaseName()),
                () -> new DatabaseCreateExistsException(sqlStatement.getDatabaseName()));
        contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService().createDatabase(sqlStatement.getDatabaseName());
        return new UpdateResponseHeader(sqlStatement);
    }
}
