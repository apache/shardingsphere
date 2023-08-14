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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.handler.ral.update.RALUpdater;
import org.apache.shardingsphere.distsql.parser.statement.ral.UpdatableRALStatement;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.updater.ConnectionSessionRequiredRALUpdater;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.sql.SQLException;

/**
 * Updatable RAL backend handler.
 * 
 * @param <T> type of SQL statement
 */
@RequiredArgsConstructor
public final class UpdatableRALBackendHandler<T extends UpdatableRALStatement> implements RALBackendHandler {
    
    private final UpdatableRALStatement sqlStatement;
    
    private final ConnectionSession connectionSession;
    
    @SuppressWarnings("unchecked")
    @Override
    public ResponseHeader execute() throws SQLException {
        RALUpdater<T> updater = TypedSPILoader.getService(RALUpdater.class, sqlStatement.getClass());
        if (updater instanceof ConnectionSessionRequiredRALUpdater) {
            ((ConnectionSessionRequiredRALUpdater<T>) updater).executeUpdate(connectionSession, (T) sqlStatement);
        } else {
            updater.executeUpdate(connectionSession.getDatabaseName(), (T) sqlStatement);
        }
        return new UpdateResponseHeader(sqlStatement);
    }
}
