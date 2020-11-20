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
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.UnknownDatabaseException;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLUseStatement;

import java.util.Collection;

/**
 * Use database backend handler.
 */
@RequiredArgsConstructor
public final class UseDatabaseBackendHandler implements TextProtocolBackendHandler {
    
    private final MySQLUseStatement useStatement;
    
    private final BackendConnection backendConnection;
    
    @Override
    public BackendResponse execute() {
        String schema = SQLUtil.getExactlyValue(useStatement.getSchema());
        if (ProxyContext.getInstance().schemaExists(schema) && isAuthorizedSchema(schema)) {
            backendConnection.setCurrentSchema(schema);
            return new UpdateResponse();
        }
        throw new UnknownDatabaseException(schema);
        
    }
    
    private boolean isAuthorizedSchema(final String schema) {
        Collection<String> authorizedSchemas = ProxyContext.getInstance().getMetaDataContexts().getAuthentication().getUsers().get(backendConnection.getUsername()).getAuthorizedSchemas();
        return authorizedSchemas.isEmpty() || authorizedSchemas.contains(schema);
    }
    
    @Override
    public boolean next() {
        return false;
    }
    
    @Override
    public Collection<Object> getRowData() {
        return null;
    }
}
