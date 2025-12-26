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

import com.google.common.base.Strings;
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
 * MySQL KILL executor.
 *
 * <p>
 * This executor operates strictly on cluster-wide {@code processId}.
 * MySQL protocol-level connection IDs are resolved in the MySQL frontend layer
 * before reaching this executor.
 * </p>
 */
@RequiredArgsConstructor
public final class MySQLKillProcessExecutor implements DatabaseAdminUpdateExecutor {
    
    private static final String QUERY_SCOPE = "QUERY";
    
    private final MySQLKillStatement killStatement;

    @Override
    public void execute(final ConnectionSession connectionSession,
                        final ShardingSphereMetaData metaData) throws SQLException {

        String scope = killStatement.getScope();

        // MySQL supports: KILL <id> and KILL QUERY <id>
        ShardingSpherePreconditions.checkState(
                Strings.isNullOrEmpty(scope) || QUERY_SCOPE.equalsIgnoreCase(scope),
                () -> new UnsupportedSQLOperationException(
                        "Only `KILL <id>` or `KILL QUERY <id>` SQL syntax is supported"));

        String processId = killStatement.getProcessId();

        ShardingSpherePreconditions.checkNotNull(
                processId,
                () -> new UnsupportedSQLOperationException("Invalid process id"));

        ProxyContext.getInstance()
                .getContextManager()
                .getPersistServiceFacade()
                .getModeFacade()
                .getProcessService()
                .killProcess(processId);
    }
}
