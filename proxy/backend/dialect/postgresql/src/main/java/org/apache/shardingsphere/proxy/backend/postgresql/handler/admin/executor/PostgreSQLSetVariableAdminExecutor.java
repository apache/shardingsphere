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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.SetStatement;

/**
 * Set variable admin executor for PostgreSQL.
 */
@RequiredArgsConstructor
public final class PostgreSQLSetVariableAdminExecutor implements DatabaseAdminUpdateExecutor {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private final SetStatement setStatement;
    
    @Override
    public void execute(final ConnectionSession connectionSession, final ShardingSphereMetaData metaData) {
        VariableAssignSegment variableAssignSegment = setStatement.getVariableAssigns().iterator().next();
        String variableName = variableAssignSegment.getVariable().getVariable().toLowerCase();
        String assignValue = variableAssignSegment.getAssignValue();
        new CharsetSetExecutor(databaseType, connectionSession).set(variableName, assignValue);
        new SessionVariableRecordExecutor(databaseType, connectionSession).recordVariable(variableName, assignValue);
    }
}
