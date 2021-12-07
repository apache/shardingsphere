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
import org.apache.shardingsphere.distsql.parser.statement.ral.common.RefreshTableMetadataStatement;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCConnectionSession;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;

import java.sql.SQLException;

/**
 * Refresh table metadata handler.
 */
@Getter
public final class RefreshTableMetadataHandler extends SchemaRequiredBackendHandler<RefreshTableMetadataStatement> {
    
    public RefreshTableMetadataHandler(final RefreshTableMetadataStatement sqlStatement, final JDBCConnectionSession connectionSession) {
        super(sqlStatement, connectionSession);
    }
    
    @Override
    protected ResponseHeader execute(final String schemaName, final RefreshTableMetadataStatement sqlStatement) throws SQLException {
        ContextManager contextManager = ProxyContext.getInstance().getContextManager();
        if (sqlStatement.getResourceName().isPresent()) {
            contextManager.reloadMetaData(schemaName, sqlStatement.getTableName().get(), sqlStatement.getResourceName().get());
            return new UpdateResponseHeader(sqlStatement);
        }
        if (sqlStatement.getTableName().isPresent()) {
            contextManager.reloadMetaData(schemaName, sqlStatement.getTableName().get());
            return new UpdateResponseHeader(sqlStatement);
        }
        contextManager.reloadMetaData(schemaName);
        return new UpdateResponseHeader(sqlStatement);
    }
}
