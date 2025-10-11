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

package org.apache.shardingsphere.test.e2e.env.container.util.spi.impl;

import org.apache.shardingsphere.test.e2e.env.container.util.spi.SQLBatchExecutionStrategy;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

/**
 * Hive SQL batch execution strategy.
 */
public final class HiveSQLBatchExecutionStrategy implements SQLBatchExecutionStrategy {
    
    @Override
    public String getType() {
        return "Hive";
    }
    
    @Override
    public boolean supports(final String jdbcDriverName) {
        return null != jdbcDriverName && jdbcDriverName.toLowerCase().contains("hive");
    }
    
    @Override
    public void execute(final Connection connection, final Statement statement, final Collection<String> sqls) throws SQLException {
        for (String each : sqls) {
            statement.execute(each);
        }
    }
}
