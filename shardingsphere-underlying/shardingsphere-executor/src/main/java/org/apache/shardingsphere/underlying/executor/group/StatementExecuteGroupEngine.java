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

package org.apache.shardingsphere.underlying.executor.group;

import org.apache.shardingsphere.underlying.executor.connection.ExecutionConnection;
import org.apache.shardingsphere.underlying.executor.connection.StatementOption;
import org.apache.shardingsphere.underlying.executor.constant.ConnectionMode;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Execute group builder for statement.
 */
public final class StatementExecuteGroupEngine extends ExecuteGroupEngine {
    
    public StatementExecuteGroupEngine(final int maxConnectionsSizePerQuery) {
        super(maxConnectionsSizePerQuery);
    }
    
    @Override
    protected Statement createStatement(final String sql, final List<Object> parameters, final ExecutionConnection executionConnection, final Connection connection, 
                                        final ConnectionMode connectionMode, final StatementOption statementOption) throws SQLException {
        return executionConnection.createStatement(connection, connectionMode, statementOption);
    }
}
