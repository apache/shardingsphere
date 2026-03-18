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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminUpdateExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLKillStatement;

import java.sql.SQLException;

/**
 * Kill process executor for MySQL.
 */
@RequiredArgsConstructor
public final class MySQLKillProcessExecutor implements DatabaseAdminUpdateExecutor {
    
    private static final String QUERY_SCOPE = "QUERY";
    
    private final MySQLKillStatement killStatement;
    
    @Override
    public void execute(final ConnectionSession connectionSession, final ShardingSphereMetaData metaData) throws SQLException {
        ShardingSpherePreconditions.checkState(QUERY_SCOPE.equalsIgnoreCase(killStatement.getScope()),
                () -> new UnsupportedSQLOperationException("Only `KILL QUERY <processId>` SQL syntax is supported"));
        String processId = killStatement.getProcessId();
        ProxyContext.getInstance().getContextManager().getPersistServiceFacade().getModeFacade().getProcessService().killProcess(processId);
    }
}
