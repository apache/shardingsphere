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

package org.apache.shardingsphere.proxy.backend.postgresql.handler.admin.executor;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminUpdateExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.variable.charset.CharsetSetExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.variable.session.SessionVariableRecordExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dal.PostgreSQLResetParameterStatement;

/**
 * Reset variable admin executor for PostgreSQL.
 */
@RequiredArgsConstructor
public final class PostgreSQLResetVariableAdminExecutor implements DatabaseAdminUpdateExecutor {
    
    private static final String DEFAULT = "DEFAULT";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private final PostgreSQLResetParameterStatement resetParameterStatement;
    
    @Override
    public void execute(final ConnectionSession connectionSession, final ShardingSphereMetaData metaData) {
        String variableName = resetParameterStatement.getConfigurationParameter();
        new CharsetSetExecutor(databaseType, connectionSession).set(variableName, DEFAULT);
        new SessionVariableRecordExecutor(databaseType, connectionSession).recordVariable(variableName, DEFAULT);
    }
}
