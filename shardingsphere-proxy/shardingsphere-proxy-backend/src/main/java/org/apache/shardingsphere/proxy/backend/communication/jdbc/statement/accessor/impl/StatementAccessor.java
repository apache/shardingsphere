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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.accessor.impl;

import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.accessor.JDBCAccessor;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

/**
 * Statement accessor.
 */
public final class StatementAccessor implements JDBCAccessor {
    
    @Override
    public StatementExecutionPrepareEngine getExecutionPrepareEngine(final BackendConnection backendConnection,
                                                                     final int maxConnectionsSizePerQuery, final StatementOption option, final Collection<ShardingSphereRule> rules) {
        return new StatementExecutionPrepareEngine(maxConnectionsSizePerQuery, backendConnection, option, rules);
    }
    
    @Override
    public boolean execute(final Statement statement, final String sql, final boolean isReturnGeneratedKeys) throws SQLException {
        return statement.execute(sql, isReturnGeneratedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
    }
}
