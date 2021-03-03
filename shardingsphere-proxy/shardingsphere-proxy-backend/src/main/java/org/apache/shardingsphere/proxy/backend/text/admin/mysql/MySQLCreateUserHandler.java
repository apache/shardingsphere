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

package org.apache.shardingsphere.proxy.backend.text.admin.mysql;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.governance.core.event.model.auth.UserRuleChangedEvent;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.metadata.auth.model.user.ShardingSphereUser;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dcl.MySQLCreateUserStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.segment.UserSegment;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * MySQL create user handler.
 */
@RequiredArgsConstructor
public final class MySQLCreateUserHandler implements TextProtocolBackendHandler {
    
    private final MySQLCreateUserStatement sqlStatement;
    
    private final String sql;
    
    private final BackendConnection backendConnection;
    
    private final DatabaseCommunicationEngineFactory databaseCommunicationEngineFactory = DatabaseCommunicationEngineFactory.getInstance();
    
    @Override
    public ResponseHeader execute() throws SQLException {
        if (ProxyContext.getInstance().getMetaDataContexts().getAuthentication().findUser(backendConnection.getGrantee()).isPresent()) {
            return new UpdateResponseHeader(sqlStatement);
        }
        DatabaseCommunicationEngine databaseCommunicationEngine = databaseCommunicationEngineFactory.newTextProtocolInstance(sqlStatement, sql, backendConnection);
        databaseCommunicationEngine.execute();
        Collection<ShardingSphereUser> users = generateUsers();
        for (ShardingSphereUser each : ProxyContext.getInstance().getMetaDataContexts().getAuthentication().getAuthentication().keySet()) {
            users.add(each);
        }
        ShardingSphereEventBus.getInstance().post(new UserRuleChangedEvent(users));
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private Collection<ShardingSphereUser> generateUsers() {
        Collection<ShardingSphereUser> result = new LinkedList<>();
        for (UserSegment each : sqlStatement.getUsers()) {
            result.add(new ShardingSphereUser(each.getUser(), each.getAuth(), each.getHost()));
        }
        return result;
    }
}
