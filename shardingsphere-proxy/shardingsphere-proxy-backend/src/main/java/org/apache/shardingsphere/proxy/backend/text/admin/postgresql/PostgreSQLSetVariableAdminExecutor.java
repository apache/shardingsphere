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

package org.apache.shardingsphere.proxy.backend.text.admin.postgresql;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;

import java.sql.SQLException;
import java.util.Iterator;

/**
 * Set variable admin executor for PostgreSQL.
 */
@RequiredArgsConstructor
public final class PostgreSQLSetVariableAdminExecutor implements DatabaseAdminExecutor {
    
    private final SetStatement setStatement;
    
    @Override
    public void execute(final ConnectionSession connectionSession) throws SQLException {
        PostgreSQLSessionVariableHandlerFactory.getHandler(getSetConfigurationParameter(setStatement)).handle(connectionSession, setStatement);
    }
    
    private String getSetConfigurationParameter(final SetStatement setStatement) {
        Iterator<VariableAssignSegment> iterator = setStatement.getVariableAssigns().iterator();
        return iterator.hasNext() ? iterator.next().getVariable().getVariable().toLowerCase() : "";
    }
}
