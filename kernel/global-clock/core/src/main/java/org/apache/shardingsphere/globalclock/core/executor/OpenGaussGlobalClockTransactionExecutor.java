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

package org.apache.shardingsphere.globalclock.core.executor;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

/**
 * OpenGauss global clock transaction executor.
 */
public final class OpenGaussGlobalClockTransactionExecutor implements GlobalClockTransactionExecutor {
    
    @Override
    public void sendSnapshotTimestamp(final Collection<Connection> connections, final long globalTimestamp) throws SQLException {
        String setSnapshotTimestampSQL = String.format("SELECT %d AS SETSNAPSHOTCSN", globalTimestamp);
        for (Connection each : connections) {
            try (Statement statement = each.createStatement()) {
                statement.execute(setSnapshotTimestampSQL);
            }
        }
    }
    
    @Override
    public void sendCommitTimestamp(final Collection<Connection> connections, final long globalTimestamp) throws SQLException {
        String setCommitTimestampSQL = String.format("SELECT %d AS SETCOMMITCSN", globalTimestamp);
        for (Connection each : connections) {
            try (Statement statement = each.createStatement()) {
                statement.execute(setCommitTimestampSQL);
            }
        }
    }
    
    @Override
    public String getDatabaseType() {
        return "openGauss";
    }
}
