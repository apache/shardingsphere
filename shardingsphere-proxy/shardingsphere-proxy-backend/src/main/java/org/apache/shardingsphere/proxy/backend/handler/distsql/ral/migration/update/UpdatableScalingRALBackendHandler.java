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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.migration.update;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.distsql.parser.statement.ral.scaling.UpdatableScalingRALStatement;
import org.apache.shardingsphere.infra.distsql.update.RALUpdaterFactory;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

/**
 * Updatable scaling RAL backend handler factory.
 */
@RequiredArgsConstructor
@Setter
public final class UpdatableScalingRALBackendHandler implements ProxyBackendHandler {
    
    private final UpdatableScalingRALStatement sqlStatement;
    
    private final ConnectionSession connectionSession;
    
    @SuppressWarnings("unchecked")
    @Override
    public ResponseHeader execute() {
        String databaseName = getDatabaseName(connectionSession);
        RALUpdaterFactory.getInstance(sqlStatement.getClass()).executeUpdate(databaseName, sqlStatement);
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private String getDatabaseName(final ConnectionSession connectionSession) {
        return connectionSession.getDatabaseName();
    }
}
