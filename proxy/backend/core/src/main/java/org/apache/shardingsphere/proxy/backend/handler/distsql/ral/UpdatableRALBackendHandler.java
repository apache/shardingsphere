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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral;

import org.apache.shardingsphere.distsql.handler.type.ral.update.UpdatableRALExecuteEngine;
import org.apache.shardingsphere.distsql.statement.ral.updatable.UpdatableRALStatement;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.DistSQLBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.sql.SQLException;

/**
 * Updatable RAL backend handler.
 */
public final class UpdatableRALBackendHandler extends UpdatableRALExecuteEngine implements DistSQLBackendHandler {
    
    private final UpdatableRALStatement sqlStatement;
    
    public UpdatableRALBackendHandler(final UpdatableRALStatement sqlStatement, final ConnectionSession connectionSession) {
        super(sqlStatement, connectionSession.getDatabaseName(), ProxyContext.getInstance().getContextManager());
        this.sqlStatement = sqlStatement;
    }
    
    @Override
    public ResponseHeader execute() throws SQLException {
        executeUpdate();
        return new UpdateResponseHeader(sqlStatement);
    }
    
    @Override
    protected ShardingSphereDatabase getDatabase(final String databaseName) {
        return ProxyContext.getInstance().getDatabase(databaseName);
    }
}
