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

package org.apache.shardingsphere.proxy.backend.communication.vertx;

import io.vertx.core.Future;
import io.vertx.sqlclient.Query;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.vertx.ExecutorVertxStatementManager;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.vertx.VertxExecutionContext;

/**
 * Vert.x backend statement.
 */
public final class VertxBackendStatement implements ExecutorVertxStatementManager {
    
    @Override
    public Future<Query<RowSet<Row>>> createStorageResource(final Future<? extends SqlClient> connection, final ConnectionMode connectionMode, final VertxExecutionContext option) {
        return Future.failedFuture(new UnsupportedOperationException("Vert.x query is not like JDBC statement."));
    }
    
    @Override
    public Future<Query<RowSet<Row>>> createStorageResource(final ExecutionUnit executionUnit, final Future<? extends SqlClient> connection,
                                                            final ConnectionMode connectionMode, final VertxExecutionContext option) {
        return Future.failedFuture(new UnsupportedOperationException("Vert.x prepared query is not like JDBC prepared statement."));
    }
}
