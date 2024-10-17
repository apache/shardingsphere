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
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dal.KillStatement;

import java.sql.SQLException;

/**
 * Kill process executor.
 */
@RequiredArgsConstructor
public final class KillProcessExecutor implements DatabaseAdminExecutor {
    
    private static final String QUERY_SCOPE = "QUERY";
    
    private final KillStatement killStatement;
    
    /**
     * Execute.
     *
     * @param connectionSession connection session
     */
    @Override
    public void execute(final ConnectionSession connectionSession) throws SQLException {
        ShardingSpherePreconditions.checkState(QUERY_SCOPE.equalsIgnoreCase(killStatement.getScope()),
                () -> new UnsupportedSQLOperationException("Only `KILL QUERY <processId>` SQL syntax is supported"));
        String processId = killStatement.getProcessId();
        ProxyContext.getInstance().getContextManager().getPersistServiceFacade().getProcessPersistService().killProcess(processId);
    }
}
