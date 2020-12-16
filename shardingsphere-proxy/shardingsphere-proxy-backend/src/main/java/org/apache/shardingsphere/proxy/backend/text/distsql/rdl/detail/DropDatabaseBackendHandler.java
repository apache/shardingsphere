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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.detail;

import org.apache.shardingsphere.governance.core.event.model.schema.SchemaNamePersistEvent;
import org.apache.shardingsphere.infra.binder.statement.ddl.DropDatabaseStatementContext;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.DBCreateExistsException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;

/**
 * Drop database backend handler.
 */
public final class DropDatabaseBackendHandler implements RDLBackendDetailHandler<DropDatabaseStatementContext> {
    
    @Override
    public ResponseHeader execute(final BackendConnection backendConnection, final DropDatabaseStatementContext sqlStatementContext) {
        check(sqlStatementContext);
        post(sqlStatementContext);
        return new UpdateResponseHeader(sqlStatementContext.getSqlStatement());
    }
    
    private void check(final DropDatabaseStatementContext sqlStatementContext) {
        if (!ProxyContext.getInstance().getAllSchemaNames().contains(sqlStatementContext.getSqlStatement().getDatabaseName())) {
            throw new DBCreateExistsException(sqlStatementContext.getSqlStatement().getDatabaseName());
        }
    }
    
    private void post(final DropDatabaseStatementContext sqlStatementContext) {
        // TODO Need to get the executed feedback from registry center for returning.
        ShardingSphereEventBus.getInstance().post(new SchemaNamePersistEvent(sqlStatementContext.getSqlStatement().getDatabaseName(), true));
    }
}
